VERSION 1.0 CLASS
BEGIN
  MultiUse = -1  'True
END
Attribute VB_Name = "cOrbImpl"
Attribute VB_GlobalNameSpace = False
Attribute VB_Creatable = True
Attribute VB_PredeclaredId = False
Attribute VB_Exposed = True
'Copyright (c) 1999-2003 Martin.Both

'This library is free software; you can redistribute it and/or
'modify it under the terms of the GNU Library General Public
'License as published by the Free Software Foundation; either
'version 2 of the License, or (at your option) any later version.

'This library is distributed in the hope that it will be useful,
'but WITHOUT ANY WARRANTY; without even the implied warranty of
'MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
'Library General Public License for more details.

Option Explicit

Private sOrbId As String
Private iOrbState As Integer
Const ORBST_NOTEXISTS As Integer = 0
Const ORBST_ISINIT As Integer = 1
Const ORBST_ISACTIVE As Integer = 2
Const ORBST_SHUTDOWN As Integer = 3
Const ORBST_ISDOWN As Integer = 4

Private sDefaultInitRef As String
Private colInitRefObjs As Collection
Private colInitRefIds As Collection

Private colCachedTCs As Collection

Private lThreadsInUse As Long
Private lTimeOutConnReuse As Long 'TimeOut of unused outgoing connections
Private lTimeOutReplyEnd As Long
Private lTimeOutServUnused As Long
Private bOnewayRebind As Boolean

'Log file name or equal "" to suppress logging
Private sLogFile As String
Private iLogLevel As Integer
Const ORBLL_ERR As Integer = 1
Const ORBLL_WARN As Integer = 2
Const ORBLL_INFO As Integer = 4
Const ORBLL_DEBUG As Integer = 8
Private bVisiWorkaround As Boolean
'TimeOut check counter
Private iTOCnt As Integer

'ORB object adapter is listening on port "sOAPort" at "sOAHost"
'The sListenPoint contains dot notation of IP address or host name and OAPort
Private Type tOrbLPnt
    sOAHost As String 'Dot notation of IP address or host name or "" if free
    sOAPort As String
    oSock As cOrbSocket 'Socket or nothing if closed
    lMsgTime As Long 'Last access time if oSock is not nothing
    iGIOPVersion As Integer '&H100, &H101, &H102
End Type
Private lOrbLPntCnt As Long 'Number of listen points <= UBound(oOrbLPnts) + 1
'oOrbLPnts(0...lOrbLPntCnt-1) are slots for listen points of this ORB
Private oOrbLPnts() As tOrbLPnt

'Object implementation map
Private collImpls As New Collection

Private Type tOrbConn 'Incoming (served) and outgoing connections
    lListenPointCnt As Long 'Number of foreign listen points or 0 if free
                            'or -1 if incoming unidirectional connection
    sListenPoints() As String 'Listen points of a foreign ORB
    lOCIdCnt As Long 'How many object references are holding an Id of this slot
    bSameProcess As Boolean 'co-location, short-circuit call
    oSock As cOrbSocket 'Socket or nothing if closed or bSameProcess
    iGIOPVersion As Integer '&H0=not yet known, &H100, &H101, &H102
    bBiDir As Boolean
    oReqsOI As cOrbRequest  'Outgoing request(s) waiting for an
                            'answer or fragment; this blocks timeout
                            'Incoming request(s) waiting for more fragments
                            '; also blocking timeout
    lMsgTime As Long 'Last access time if oSock is not nothing
End Type
Private lOrbConnCnt As Long ' <= UBound(oOrbConns) + 1
'oOrbConns(0...lOrbConnCnt-1) are slots for connections of this ORB
Private oOrbConns() As tOrbConn

'Seed for Request IDs
Private lReqId As Long

'Structure used in select() call
'struct timeval {
'    long    tv_sec;         /* seconds */
'    long    tv_usec;        /* and microseconds */
'};
Private Type tTimeVal
    tv_sec As Long
    tv_usec As Long
End Type

'int PASCAL FAR select (int nfds, fd_set FAR *readfds, fd_set FAR *writefds,
'   fd_set FAR *exceptfds, const struct timeval FAR *timeout);
Private Declare Function dllSelect Lib "Ws2_32.dll" Alias "select" _
    (ByVal nfds As Long, ByRef readFDs As Any, ByRef writeFDs As Any, _
    ByRef exceptFDs As Any, ByRef timeOut As tTimeVal) As Long
'Array of socket file descriptors used by select() call
Private lSelFDs() As Long
Private lSelFDUseId As Long

Private colValFactories As Collection

'Produce a new connection originator even valued Request ID.
'If using the other side of a bi-directionally connection add 1 if required.
Public Function getNextReqId() As Long
    lReqId = lReqId + 2 'Produce only even valued Request IDs
    getNextReqId = lReqId 'Later we add 1 if we are not the connection originator
End Function

'urlAny()
'??? The object reference should know the original string to
'do a better recovery if an error occurs.
Public Function stringToObject(sURL As String) As cOrbObject
    On Error GoTo ErrHandler
    'Assert that init was successfully called
    If iOrbState = ORBST_NOTEXISTS Then
        Call mVBOrb.VBOrb.raiseBADINVORDER(0, mVBOrb.VBOrb.CompletedNO)
    End If
    If InStr(sURL, "IOR:") = 1 Then
        Set stringToObject = urlIor(sURL)
    ElseIf InStr(sURL, "file://") = 1 Then
        Set stringToObject = urlFileToObject(sURL)
    ElseIf InStr(sURL, "http://") = 1 Then
        Set stringToObject = urlHttpToObject(sURL)
    ElseIf InStr(sURL, "iioploc://") = 1 Then
        Dim oObjRef As cOrbObjRef
        Set oObjRef = New cOrbObjRef
        Call oObjRef.initByURL(Me, ":" & Mid$(sURL, 11))
        Set stringToObject = oObjRef
    ElseIf InStr(sURL, "corbaloc:") = 1 Then
        Set stringToObject = urlCorbaloc(Mid$(sURL, 10))
    ElseIf InStr(sURL, "corbaname:") = 1 Then
        Set stringToObject = urlCorbaname(Mid$(sURL, 11))
    ElseIf InStr(sURL, "ias_ejb:") = 1 Then
        Set stringToObject = urlIas_ejb(Mid$(sURL, 9))
    Else
        Call mVBOrb.VBOrb.raiseBADPARAM(7, mVBOrb.VBOrb.CompletedNO, _
            "Unknown URL schema name: " & sURL)
    End If
    Exit Function
ErrHandler:
    Set stringToObject = Nothing
    Call mVBOrb.ErrReraise("stringToObject")
End Function

'sIOR contains "file://"
Private Function urlFileToObject(ByVal sURL As String) As cOrbObjRef
    On Error GoTo ErrHandler
    Dim sFileName As String
    Dim iFileNo As Integer
    Dim sLine As String
   
    sFileName = Mid$(sURL, 8)
    iFileNo = FreeFile
    Open sFileName For Input As #iFileNo
    If Not EOF(iFileNo) Then
        Line Input #iFileNo, sLine
    End If
    Close #iFileNo
    
    Dim pos As Long
    pos = InStr(sLine, "IOR:")
    If pos = 0 Then
        Call mVBOrb.VBOrb.raiseBADPARAM(10, mVBOrb.VBOrb.CompletedNO, _
            "File doesn't contain an IOR. URL: " & sURL)
    End If
    Set urlFileToObject = urlIor(Mid$(sLine, pos))
    Exit Function
ErrHandler:
    Set urlFileToObject = Nothing
    Call mVBOrb.ErrReraise("urlFileToObject")
End Function

'sIOR contains "http://"
Private Function urlHttpToObject(ByVal sURL As String) As cOrbObjRef
    On Error GoTo ErrHandler
    Dim pos As Long
    Dim sHostPort As String
    Dim sPath As String
    
    pos = InStr(8, sURL & "/", "/")
    sHostPort = Mid$(sURL, 8, pos - 8)
    sPath = Mid$(sURL, pos)
    
    Dim oSock As cOrbSocket
    Set oSock = New cOrbSocket
    Call oSock.initConnect(sHostPort, "80")
    Call oSock.sendString("GET " & sPath & " HTTP/1.0" & vbCrLf & vbCrLf _
        & "User-Agent: VB-IIOP VisualBasic 24-08-1999" & vbCrLf & vbCrLf _
        & "Accept: text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2" & vbCrLf & vbCrLf _
        & "Connection: keep -alive" & vbCrLf & vbCrLf & vbCrLf & vbCrLf)
    
    Dim webpage As String
    webpage = oSock.recvString()
    Call oSock.termConnect
    
    pos = InStr(webpage, "IOR:")
    If pos = 0 Then
        Call mVBOrb.VBOrb.raiseBADPARAM(10, mVBOrb.VBOrb.CompletedNO, _
            "Webpage doesn't contain an IOR. URL: " & sURL)
    End If
    Set urlHttpToObject = urlIor(Mid$(webpage, pos))
    Exit Function
ErrHandler:
    Set urlHttpToObject = Nothing
    Call mVBOrb.ErrReraise("urlHttpToObject")
End Function

'sIOR contains "IOR:..."
Private Function urlIor(ByRef sIOR As String) As cOrbObject
    On Error GoTo ErrHandler
    Dim oObj As cOrbObject
    Dim oIn As cOrbStream
    Set oIn = New cOrbStream
    Call oIn.initStream(Me, &H100, (Len(sIOR) - 3) \ 2)
    Call oIn.recvFromIOR(sIOR)
    Call oIn.readEncapOpen((Len(sIOR) - 3) \ 2)
    Set oObj = oIn.readObject()
    Call oIn.readEncapClose
    Call oIn.destroy
    Set urlIor = oObj
    Exit Function
InvalidIOR:
    Call mVBOrb.VBOrb.raiseBADPARAM(10, mVBOrb.VBOrb.CompletedNO, _
        "IOR unmarshalling")
ErrHandler:
    If mVBOrb.ErrIsSystemEx() And _
        Err.Number = (mVBOrb.ITF_E_MARSHAL_NO Or vbObjectError) Then
        Resume InvalidIOR
    End If
    Call mVBOrb.ErrReraise("urlIor")
End Function

'objectToUrl()
Public Function objectToString(ByVal Obj As cOrbObject) As String
    On Error GoTo ErrHandler
    Dim oOut As cOrbStream
    Set oOut = New cOrbStream
    
    'CDR stream
    Call oOut.initStream(Me, &H100)
    
    'Like oOut.writeEncapOpen() without len
    Call oOut.writeBoolean(oOut.littleEndian)
    Call oOut.writeObject(Obj)
    
    Call oOut.sendToIOR(objectToString)
    
    Call oOut.destroy
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("objectToString")
End Function

'IN:    sURL            AddrList [ '/' ObjKey ] [ '#' StringName ]
'I/O:   sKeyString      Key after / character
'I/O:   sStringName     Name after # sign
'RET:   sAddrList       AddrList
Private Function urlIiopAddrList(ByRef sURL As String, ByRef sKeyString As String, _
    ByRef sStringName As String) As String
    On Error GoTo ErrHandler
    Dim lStartPos As Long
    Dim lNextPos As Long
    Dim lEndPos As Long
    
    lStartPos = 1
    lEndPos = Len(sURL)
    
    lNextPos = InStr(lStartPos, sURL, "#")
    If lNextPos >= lStartPos And lNextPos <= lEndPos Then
        sStringName = Mid$(sURL, lNextPos + 1, lEndPos - lNextPos)
        lEndPos = lNextPos - 1
    End If
    
    lNextPos = InStr(lStartPos, sURL, "/")
    If lNextPos >= lStartPos And lNextPos <= lEndPos Then
        sKeyString = Mid$(sURL, lNextPos + 1, lEndPos - lNextPos)
        lEndPos = lNextPos - 1
    End If
    
    urlIiopAddrList = Mid$(sURL, lStartPos, lEndPos - lStartPos + 1)
    
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("urlIiopAddrList")
End Function

'corbaloc:obj_addr_list[/key_string]
'Characters outside of US-ASCII alphanumeric and ;/:?@&=+$,-_!~*’()
'in key_string are escaped.
'IN:    sURL        obj_addr_list[/key_string]
Private Function urlCorbaloc(ByRef sURL As String) As cOrbObjRef
    On Error GoTo ErrHandler
    Dim sAddrList As String
    Dim sKeyString As String
    Dim sStringName As String
    sAddrList = urlIiopAddrList(sURL, sKeyString, sStringName)
    If Len(sStringName) <> 0 Then
        Call mVBOrb.VBOrb.raiseBADPARAM(9, mVBOrb.VBOrb.CompletedNO, _
            "Inadequate # character in 'corbaloc:" & sURL & "'")
    End If
    If Len(sAddrList) <= 0 Then
        Call mVBOrb.VBOrb.raiseBADPARAM(8, mVBOrb.VBOrb.CompletedNO, _
            "Missing an address in 'corbaloc:" & sURL & "'")
    ElseIf InStr(1, sAddrList, ":") <= 0 Then
        Call mVBOrb.VBOrb.raiseBADPARAM(9, mVBOrb.VBOrb.CompletedNO, _
            "Missing a protocol identifier in address '" & sAddrList & _
            "' of 'corbaloc:" & sURL & "'")
    ElseIf sAddrList = "rir:" Then
        'There is no version or address information when rir is used.
        Set urlCorbaloc = resolveInitialReferences(sKeyString)
    Else
        Set urlCorbaloc = New cOrbObjRef
        Call urlCorbaloc.initByURL(Me, sAddrList & "/" & sKeyString)
    End If
    Exit Function
ErrHandler:
    Set urlCorbaloc = Nothing
    Call mVBOrb.ErrReraise("urlCorbaloc")
End Function

'corbaname:obj_addr_list[/key_string][#string_name]
'Characters outside of US-ASCII alphanumeric and ;/:?@&=+$,-_.!~*’()
'in string_name are escaped.
'IN:    sURL        obj_addr_list[/key_string][#string_name]
Private Function urlCorbaname(ByRef sURL As String) As cOrbObjRef
    On Error GoTo ErrHandler
    Dim sAddrList As String
    Dim sKeyString As String
    Dim sStringName As String 'A stringified Name with URL escapes
    'If a ‘%’ is not followed by two hex digits, the stringified name is syntactically
    'invalid.
    sAddrList = urlIiopAddrList(sURL, sKeyString, sStringName)
    If Len(sKeyString) = 0 Then
        sKeyString = "NameService" 'Default key_string for corbaname URL
    End If
    'Get the object reference of the naming context
    Dim oNmCtxRef As cOrbObjRef
    Set oNmCtxRef = str2NmCtxRef("corbaloc:" & _
        sAddrList & "/" & sKeyString)
    'Call the resolve operation of the NamingContext
    Dim sName As String
    sName = mVBOrb.nameUrl2NameStr(sStringName)
    Set urlCorbaname = nameContextResolveName(oNmCtxRef, sName)
    Exit Function
ErrHandler:
    Call mVBOrb.ErrSave
    Resume ErrLoadRaise
ErrLoadRaise:
    On Error GoTo 0
    Call mVBOrb.ErrLoad
    Call mVBOrb.ErrReraise("urlCorbaname")
End Function

Private Function urlIas_ejb(ByRef sURL As String) As cOrbObjRef
    On Error GoTo ErrHandler
    Dim startPos As Long
    Dim nextPos As Long
    Dim sAddrList As String
    Dim sRmiId As String
    Dim sEjbId As String
    
    startPos = 1
    nextPos = InStr(startPos, sURL, "/RMI:")
    If nextPos <= 0 Then
        Call mVBOrb.ErrRaise(1, "Missing /RMI Id: " & sURL)
    End If
    sAddrList = Mid$(sURL, startPos, nextPos - startPos)
    startPos = nextPos + 1
    nextPos = InStr(startPos, sURL, "/")
    If nextPos <= 0 Then
        Call mVBOrb.ErrRaise(1, "Missing /EJB Id: " & sURL)
    End If
    sRmiId = Mid$(sURL, startPos, nextPos - startPos)
    sEjbId = Mid$(sURL, nextPos + 1, Len(sURL) - nextPos)
    
    Dim oOutIn As cOrbStream
    Set oOutIn = New cOrbStream
    Call oOutIn.initStream(Me, &H100)
    Call oOutIn.writeGIOPHead
    Call oOutIn.writeEncapOpen(False)
    Call oOutIn.writeOctet(Asc("P"))
    Call oOutIn.writeOctet(Asc("M"))
    Call oOutIn.writeOctet(Asc("C"))
    Call oOutIn.writeOctet(0)
    Call oOutIn.writeString(sRmiId)
    Call oOutIn.writeString(sEjbId)
    Call oOutIn.writeString("/persistent")
    Call oOutIn.writeEncapClose
    Call oOutIn.sendGIOPToReadAgain
    Dim baObjKey() As Byte
    Call oOutIn.readSeqOctet(baObjKey)
    Dim sKeyString As String
    sKeyString = mVBOrb.objKey2String(baObjKey)
    Call oOutIn.destroy
    
    Dim oObjRef As cOrbObjRef
    Set oObjRef = New cOrbObjRef
    Call oObjRef.initByURL(Me, sAddrList & "/" & sKeyString)
    Set urlIas_ejb = oObjRef
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("urlIas_ejb")
End Function

'One more connection slot please
Private Sub ConnIncCnt()
    On Error GoTo ErrHandler
    If lOrbConnCnt >= 1000 Then
        'Max. Connection limit reached
        Call mVBOrb.VBOrb.raiseIMPLIMIT(99, mVBOrb.VBOrb.CompletedNO, _
            "More than 1000 connections")
    End If
    If lOrbConnCnt > UBound(oOrbConns) Then
        ReDim Preserve oOrbConns(0 To lOrbConnCnt + 2) As tOrbConn
        lSelFDUseId = lSelFDUseId + 1
        ReDim lSelFDs(0 To (lOrbLPntCnt + lOrbConnCnt + 2) * 3 + 5) As Long
    End If
    lOrbConnCnt = lOrbConnCnt + 1
    If iLogLevel >= 4 Then
        Call logMsg("I One more ORB connection, now: " & lOrbConnCnt)
    End If
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("ConnIncCnt")
End Sub

'Allocate an outgoing connection slot
'IN:    sHostPort   Dot notation or hostname and port
'RET:   lOCId       Outgoing connection slot Id
Public Function ConnOCIdAlloc(ByRef sHostPort As String) As Long
    On Error GoTo ErrHandler
    If sHostPort = "" Then
        Call mVBOrb.VBOrb.raiseINVOBJREF(1, mVBOrb.VBOrb.CompletedNO)
    End If
    Dim lFirstFreeId As Long
    Dim lFirstSuitId As Long
    lFirstFreeId = lOrbConnCnt
    lFirstSuitId = lOrbConnCnt
    Dim lOCId As Long
    Dim lLP As Long
    'Find a suitable slot
    For lOCId = 0 To lOrbConnCnt - 1
        If oOrbConns(lOCId).lListenPointCnt > 0 Then 'An allocated outgoing slot
            For lLP = 0 To oOrbConns(lOCId).lListenPointCnt - 1
                If oOrbConns(lOCId).sListenPoints(lLP) = sHostPort Then
                    'Found a suitable slot
                    If Not oOrbConns(lOCId).oSock Is Nothing _
                        Or oOrbConns(lOCId).bSameProcess Then
                        'Use immediately
                        oOrbConns(lOCId).lOCIdCnt = oOrbConns(lOCId).lOCIdCnt + 1
                        ConnOCIdAlloc = lOCId
                        Exit Function
                    End If
                    If oOrbConns(lOCId).lListenPointCnt = 1 _
                        And lFirstSuitId > lOCId Then
                        lFirstSuitId = lOCId
                    End If
                End If
            Next lLP
        ElseIf oOrbConns(lOCId).lListenPointCnt = 0 Then 'A free slot
            If lFirstFreeId > lOCId Then
                lFirstFreeId = lOCId
            End If
        End If
    Next lOCId
    lOCId = lFirstSuitId
    If lOCId < lOrbConnCnt Then 'Found a suitable slot?
        oOrbConns(lOCId).lOCIdCnt = oOrbConns(lOCId).lOCIdCnt + 1
        ConnOCIdAlloc = lOCId
        Exit Function
    End If
    lOCId = lFirstFreeId
    If lOCId = lOrbConnCnt Then 'One more slot please
        Call ConnIncCnt
    End If
    'Allocate free slot
    oOrbConns(lOCId).lListenPointCnt = 1
    ReDim oOrbConns(lOCId).sListenPoints(0 To 0) As String
    oOrbConns(lOCId).sListenPoints(0) = sHostPort
    oOrbConns(lOCId).bSameProcess = False
    Set oOrbConns(lOCId).oSock = Nothing
    oOrbConns(lOCId).lOCIdCnt = 1
    ConnOCIdAlloc = lOCId
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("ConnOCIdAlloc")
End Function

'Release an outgoing connection slot
'IN:    ConnId      ORB connection slot Id
'IN:    MarkOnly    Don't touch global Err object
Public Sub ConnOCIdFree(ByVal ConnId As Long, ByVal MarkOnly As Boolean)
    oOrbConns(ConnId).lOCIdCnt = oOrbConns(ConnId).lOCIdCnt - 1
    If Not MarkOnly And oOrbConns(ConnId).lOCIdCnt = 0 And _
        Not oOrbConns(ConnId).bBiDir Then
        Call ConnSendCloseAndClose(ConnId, 1005)
    End If
End Sub

'Check timeout, Get an [open] socket connection
'IN:    ConnId      ORB connection slot Id
'I/O:   AutoOpen    IN: Open or reopen if closed / OUT: New connection?
'OUT:   SameProcess co-location?
'RET:               Socket object or Nothing e.g. if same process
Public Function ConnGet(ByRef SameProcess As Boolean, ByVal ConnId As Long, _
    ByRef AutoOpen As Boolean) As cOrbSocket
    On Error GoTo ErrHandler
    'co-location?
    SameProcess = oOrbConns(ConnId).bSameProcess
    If SameProcess Then
        AutoOpen = False
        Set ConnGet = Nothing
        Exit Function
    End If
    Dim lTime As Long
    lTime = mVBOrb.dllGetTickCount() 'or mvborb.getTime()
    'Connection is closed or timed out?
    If ConnIsOpen(ConnId, lTime) Then
        AutoOpen = False
    Else
        If Not oOrbConns(ConnId).oReqsOI Is Nothing Then
            Call mVBOrb.VBOrb.raiseINTERNAL(1, mVBOrb.VBOrb.CompletedNO, _
                "bad lock")
        End If
        If Not AutoOpen Then
            Call mVBOrb.VBOrb.raiseREBIND(98, mVBOrb.VBOrb.CompletedNO, _
                "Connection is closed, NO_RECONNECT")
        End If
        'Open or reopen a connection on the first listen point
        If oOrbConns(ConnId).lListenPointCnt <> 1 Then
            'Force rebind because this ORB don't remember the right listen point
            Call mVBOrb.VBOrb.raiseREBIND(99, mVBOrb.VBOrb.CompletedNO, _
                "Please open a new connection")
        End If
        Dim newSocket As cOrbSocket 'Don't use As New cOrbSocket
        Set newSocket = New cOrbSocket
        Call newSocket.openSocket(oOrbConns(ConnId).sListenPoints(0), "683")
        If newSocket.socketHost = oOrbLPnts(0).sOAHost _
            And newSocket.socketPort = oOrbLPnts(0).sOAPort Then
            If iLogLevel >= 4 Then
                Call logMsg("I c" & CStr(ConnId + 1) & " Open ORB internal connection")
            End If
            'co-location, short-circuit call
            oOrbConns(ConnId).bSameProcess = True
            SameProcess = True
            Set newSocket = Nothing
        Else
            If iLogLevel >= 4 Then
                Call logMsg("I c" & CStr(ConnId + 1) & " Open ORB connection to " _
                    & newSocket.socketHost & ":" & newSocket.socketPort)
            End If
            Call newSocket.connectSocket
        End If
        'Register new connection
        Set oOrbConns(ConnId).oSock = newSocket
        AutoOpen = True
    End If
    'Set access time
    oOrbConns(ConnId).lMsgTime = lTime
    Set ConnGet = oOrbConns(ConnId).oSock
    Exit Function
ErrHandler:
    SameProcess = False
    Set ConnGet = Nothing
    Call mVBOrb.ErrReraise("Orb.ConnGet")
End Function

'Get the GIOPVersion of an open connection
Private Function ConnGIOPVersion(ByVal ConnId As Long) As Integer
    ConnGIOPVersion = oOrbConns(ConnId).iGIOPVersion
    If ConnGIOPVersion = 0 Then
        ConnGIOPVersion = oOrbLPnts(0).iGIOPVersion
    End If
End Function

'Check connection if no timeout is happen
'IN:    ConnId      Connection slot Id
'IN:    lTime       GetTickCount() or mvborb.getTime()
'RET:               Connection is still open and usable?
Private Function ConnIsOpen(ByVal ConnId As Long, ByVal lTime As Long) As Boolean
    On Error GoTo ErrHandler
    If oOrbConns(ConnId).bSameProcess Then
        ConnIsOpen = True
    ElseIf oOrbConns(ConnId).oSock Is Nothing Then
        ConnIsOpen = False
    ElseIf Not oOrbConns(ConnId).oSock.isOpen Then
        Set oOrbConns(ConnId).oSock = Nothing
        ConnIsOpen = False
    Else
        Dim lAge As Long
        lAge = mVBOrb.getLongDiff(oOrbConns(ConnId).lMsgTime, lTime)
        If Not oOrbConns(ConnId).oReqsOI Is Nothing Then
            If oOrbConns(ConnId).oReqsOI.isOutgoing() And lAge > lTimeOutReplyEnd Then
                On Error GoTo ErrLog 'TimeOut
                Call ConnSendCloseAndClose(ConnId, 1100)
                On Error GoTo ErrHandler
                ConnIsOpen = False
            Else
                ConnIsOpen = True
            End If
        ElseIf oOrbConns(ConnId).oSock.isOutgoing() _
            And Not oOrbConns(ConnId).bBiDir Then
            If lAge > lTimeOutConnReuse Then
                On Error GoTo ErrLog 'TimeOut
                Call ConnSendCloseAndClose(ConnId, 1100)
                On Error GoTo ErrHandler
                ConnIsOpen = False
            Else
                ConnIsOpen = True
            End If
        Else
            If lAge > lTimeOutServUnused Then
                On Error GoTo ErrLog 'TimeOut
                Call ConnSendCloseAndClose(ConnId, 1100)
                On Error GoTo ErrHandler
                ConnIsOpen = False
            Else
                ConnIsOpen = True
            End If
        End If
    End If
    Exit Function
ErrLog:
    Call logErr("Orb.ConnIsOpen")
    Resume Next
ErrHandler:
    Call mVBOrb.ErrReraise("Orb.ConnIsOpen")
End Function

'Close a connection including foreign listen points
'Can be called twice
'oReqOut is set to Nothing but will not be deleted
'oReqsOI will be deleted and set to Nothing
'IN:    ConnId          Connection slot Id
'IN:    ReqCancelType   1005 = SendCloseMsg, 1006 = SendErrMsg
'                       1090 = InternalException, 1100 = TimeOut
Public Sub ConnClose(ByVal ConnId As Long, ByVal ReqCancelType As Integer)
    On Error GoTo ErrLog
    If Not oOrbConns(ConnId).oReqsOI Is Nothing Then
        Dim thisReq As cOrbRequest
        Set thisReq = oOrbConns(ConnId).oReqsOI
        If thisReq.isOutgoing() Then
            'Cancel all pending requests
            Dim nextReq As cOrbRequest
            Do
                Call thisReq.setRes(ReqCancelType, Nothing)
                Set nextReq = thisReq.NextRequest
                If nextReq Is Nothing Then Exit Do
                Set thisReq = nextReq
            Loop
        End If
        Set oOrbConns(ConnId).oReqsOI = Nothing
    End If
    If oOrbConns(ConnId).bSameProcess Then
        If iLogLevel >= 4 Then
            Call logMsg("I c" & CStr(ConnId + 1) & " Close ORB internal connection")
        End If
        oOrbConns(ConnId).bSameProcess = False
    ElseIf Not oOrbConns(ConnId).oSock Is Nothing Then
        If iLogLevel >= 4 Then
            Call logMsg("I c" & CStr(ConnId + 1) & " Close ORB socket connection")
        End If
        If oOrbConns(ConnId).oSock.isOpen Then
            If oOrbConns(ConnId).oSock.isOutgoing Then
                Call oOrbConns(ConnId).oSock.termConnect
            ElseIf oOrbConns(ConnId).oSock.isIncoming Then
                Call oOrbConns(ConnId).oSock.termAccept
            End If
        End If
    End If
    oOrbConns(ConnId).iGIOPVersion = 0
    Set oOrbConns(ConnId).oSock = Nothing
    If oOrbConns(ConnId).lListenPointCnt < 0 Then
        oOrbConns(ConnId).lListenPointCnt = 0
    ElseIf oOrbConns(ConnId).lListenPointCnt > 0 Then
        If oOrbConns(ConnId).lOCIdCnt <= 0 Then
            Erase oOrbConns(ConnId).sListenPoints
            oOrbConns(ConnId).lListenPointCnt = 0
        End If
    End If
    Exit Sub
ErrLog:
    Call logErr("ConnClose")
    Resume Next
End Sub

'Free a connection slot
'IN:    ConnId       Connection slot Id
Private Sub ConnSendCloseAndClose(ByVal ConnId As Long, _
    ByVal ReqCancelType As Integer)
    On Error GoTo ErrLog
    Call ConnSendCloseConnection(ConnId)
    Call ConnClose(ConnId, ReqCancelType)
    Exit Sub
ErrLog:
    Call logErr("Orb.ConnSendCloseAndClose")
    Resume Next
End Sub

'Send MessageError if socket is open
'IN:    ConnId      Connection slot Id
Private Sub ConnSendMessageError(ByVal ConnId As Long)
    Dim oOut As cOrbStream
    On Error GoTo ErrHandler
    If oOrbConns(ConnId).bSameProcess Or oOrbConns(ConnId).oSock Is Nothing Then
        Exit Sub
    End If
    On Error Resume Next
    If oOrbConns(ConnId).oSock.isOpen Then
        Set oOut = New cOrbStream
        Call oOut.initStream(Me, ConnGIOPVersion(ConnId))
        If Err.Number <> 0 Then Call mVBOrb.ErrSave: GoTo ErrLoad1
        'Prepare GIOP Message Header
        Call oOut.writeGIOPHead
        If Err.Number <> 0 Then Call mVBOrb.ErrSave: GoTo ErrLoad2
        '6 = MessageError
        Call logMsg("E c" & CStr(ConnId + 1) & " Send MessageError")
        Call oOut.sendGIOPToSocket(6, oOrbConns(ConnId).oSock)
        If Err.Number <> 0 Then Call mVBOrb.ErrSave: GoTo ErrLoad2
        Call oOut.destroy
        If Err.Number <> 0 Then Call mVBOrb.ErrSave: GoTo ErrLoad1
        Set oOut = Nothing
    End If
    Exit Sub
ErrLoad2:
    Call oOut.destroy
ErrLoad1:
    Set oOut = Nothing
    On Error GoTo 0
    Call mVBOrb.ErrLoad
ErrHandler:
    Call mVBOrb.ErrReraise("ConnSendMessageError")
End Sub

'Send CloseConnection if socket is open
'IN:    ConnId      Connection slot Id
Private Sub ConnSendCloseConnection(ByVal ConnId As Long)
    Dim oOut As cOrbStream
    On Error GoTo ErrHandler
    If oOrbConns(ConnId).bSameProcess Or oOrbConns(ConnId).oSock Is Nothing Then
        Exit Sub
    End If
    On Error Resume Next
    If oOrbConns(ConnId).oSock.isOpen Then
        Dim iGIOPVersion As Integer
        iGIOPVersion = ConnGIOPVersion(ConnId)
        If (oOrbConns(ConnId).oSock.isIncoming Or iGIOPVersion >= &H102) _
            And Not VisiWorkaround Then 'fix for Visi5 by Andreas Roth
            'Use VisiWorkaround if Visi5 closes ALL connections
            'after receiving a CloseConnection message
            Set oOut = New cOrbStream
            Call oOut.initStream(Me, iGIOPVersion)
            If Err.Number <> 0 Then Call mVBOrb.ErrSave: GoTo ErrLoad1
            'Prepare GIOP Message Header
            Call oOut.writeGIOPHead
            If Err.Number <> 0 Then Call mVBOrb.ErrSave: GoTo ErrLoad2
            '5 = CloseConnection
            If iLogLevel >= 4 Then
                Call logMsg("I c" & CStr(ConnId + 1) & " Send CloseConnection")
            End If
            Call oOut.sendGIOPToSocket(5, oOrbConns(ConnId).oSock)
            If Err.Number <> 0 Then Call mVBOrb.ErrSave: GoTo ErrLoad2
            Call oOut.destroy
            If Err.Number <> 0 Then Call mVBOrb.ErrSave: GoTo ErrLoad1
            Set oOut = Nothing
        End If
    End If
    Exit Sub
ErrLoad2:
    Call oOut.destroy
ErrLoad1:
    Set oOut = Nothing
    On Error GoTo 0
    Call mVBOrb.ErrLoad
ErrHandler:
    Call mVBOrb.ErrReraise("ConnSendCloseConnection")
End Sub

'Receive GIOP message
'IN:    ConnId      Connection slot Id
Private Sub ConnRecvMsg(ByVal ConnId As Long)
    On Error Resume Next
    Dim oIn As cOrbStream
    Dim oOut As cOrbStream
    Set oIn = New cOrbStream
    Call oIn.initStream(Me, &H100)
    If Err.Number <> 0 Then Call mVBOrb.ErrSave: GoTo ErrLoad1
    Dim msgType As Integer
    'Receive GIOP Message Header
    msgType = oIn.recvGIOPFromSocket(oOrbConns(ConnId).oSock)
    If Err.Number <> 0 Then
        Call mVBOrb.ErrSave
        If Err.Number = mVBOrb.VBOrb.ITF_E_MARSHAL_NO Then
            On Error GoTo ErrLog
            Call ConnSendMessageError(ConnId)
            Call ConnClose(ConnId, 1006) 'Is deleting oOrbConns().oReqsOi
            On Error Resume Next
        End If
        GoTo ErrLoad1
    End If
    Dim iGIOPVersion As Integer
    iGIOPVersion = oIn.getGIOPVersion()
    oOrbConns(ConnId).iGIOPVersion = iGIOPVersion
    Dim lHeadPos As Long
    Dim lReqstId As Long
    Dim oReqst As cOrbRequest
    Dim seqSC As Long, i1 As Long
    Select Case msgType
    Case 0 '= GIOP Request received
        lHeadPos = oIn.getPos()
        'Read GIOP Request Header
        If iGIOPVersion < &H102 Then
            'Unfortunately we must read over the IOP::ServiceContextList
            'just to get the following request id.
            seqSC = oIn.readUlong()
            For i1 = 1 To seqSC
                Call oIn.readUlong
                Call oIn.readSkip(oIn.readUlong())
            Next i1
        End If
        'unsigned long request_id;
        lReqstId = oIn.readUlong()
        Call oIn.setPos(lHeadPos)
        Set oReqst = New cOrbRequest
        If oReqst.initInRequest(Me, lReqstId, oIn) Then 'If oIn.isComplete() Then
            If iLogLevel >= 8 Then
                Call logMsg("D c" & CStr(ConnId + 1) & " Request '" & oReqst.Operation & "' received")
            End If
            Set oOut = oReqst.replyRequest()
            If Err.Number <> 0 Then Call mVBOrb.ErrSave: GoTo ErrLoad2
            If Not oOut Is Nothing Then
                '1 = Send Reply
                Call oOut.sendGIOPToSocket(1, oOrbConns(ConnId).oSock)
                If Err.Number <> 0 Then Call mVBOrb.ErrSave: GoTo ErrLoad2
                Call oOut.destroy
                Set oOut = Nothing
                If Err.Number <> 0 Then Call mVBOrb.ErrSave: GoTo ErrLoad1
            End If
        Else
            'Set wait lock
            Set oReqst.NextRequest = oOrbConns(ConnId).oReqsOI
            Set oOrbConns(ConnId).oReqsOI = oReqst
        End If
    Case 1 '= GIOP Reply received
        lHeadPos = oIn.getPos()
        'Read GIOP Reply Header
        If iGIOPVersion < &H102 Then
            'Unfortunately we must read over the IOP::ServiceContextList
            'just to get the following request id.
            seqSC = oIn.readUlong()
            For i1 = 1 To seqSC
                Call oIn.readUlong
                Call oIn.readSkip(oIn.readUlong())
            Next i1
        End If
        'unsigned long request_id;
        lReqstId = oIn.readUlong()
        Call oIn.setPos(lHeadPos)
        'If incomplete Then First fragment
        Set oReqst = ConnReqCut(ConnId, lReqstId, _
            oIn.isComplete()) 'Unset wait lock if final fragment
        If oReqst Is Nothing Then
            On Error GoTo ErrLog
            Call mVBOrb.ErrRaise(1, "Reply for unknown request " & _
                CStr(lReqstId) & " received")
            Call oIn.destroy
            On Error Resume Next
        Else
            Call oReqst.setRes(msgType, oIn)
            If iLogLevel >= 8 Then
                If oIn.isComplete() Then
                    Call logMsg("D c" & CStr(ConnId + 1) & " Reply '" & oReqst.Operation & "' received")
                End If
            End If
        End If
    Case 2 '= CancelRequest received
        'Read GIOP CancelRequestHeader
        'unsigned long request_id;
        lReqstId = oIn.readUlong()
        Set oReqst = ConnReqCut(ConnId, lReqstId, True) 'Unset wait lock
        If oReqst Is Nothing Then
            On Error GoTo ErrLog
            Call mVBOrb.ErrRaise(1, "CancelRequest for unknown request " & _
                CStr(lReqstId) & " received")
            On Error Resume Next
        Else
            Call oReqst.setRes(msgType, Nothing)
        End If
        Call oIn.destroy
    Case 3 '= Locate Request received
        Set oOut = New cOrbStream
        Call replyLocateRequest(oIn, oOut)
        If Err.Number <> 0 Then Call mVBOrb.ErrSave: GoTo ErrLoad2
        If Not oOut Is Nothing Then
            '4 = Send LocateReply
            Call oOut.sendGIOPToSocket(4, oOrbConns(ConnId).oSock)
            If Err.Number <> 0 Then Call mVBOrb.ErrSave: GoTo ErrLoad2
            Call oOut.destroy
            Set oOut = Nothing
            If Err.Number <> 0 Then Call mVBOrb.ErrSave: GoTo ErrLoad1
        End If
    Case 4 '= GIOP LocateReply received
        lHeadPos = oIn.getPos()
        'Read GIOP LocateReply Header
        'unsigned long request_id;
        lReqstId = oIn.readUlong()
        Call oIn.setPos(lHeadPos)
        Set oReqst = ConnReqCut(ConnId, lReqstId, True) 'Unset wait lock
        If oReqst Is Nothing Then
            Call mVBOrb.ErrRaise(1, "Unknown LocateReply received")
            Call mVBOrb.ErrSave: GoTo ErrLoad1
        Else
            Call oReqst.setRes(msgType, oIn)
        End If
    Case 5 '5 = GIOP CloseConnection received
        If iLogLevel >= 4 Then
            Call logMsg("I CloseConnection message received")
        End If
        Call ConnClose(ConnId, msgType) 'Is deleting oOrbConns().oReqsOI
        Call oIn.destroy
    Case 6 '= MessageError received
        If iLogLevel >= 2 Then
            Call logMsg("W MessageError message received")
        End If
        Call ConnClose(ConnId, msgType) 'Is deleting oOrbConns().oReqsOI
        Call oIn.destroy
    Case 7 '= Fragment received
        If iGIOPVersion < &H102 Then
            Call logMsg("E Unsupported GIOP " & _
                mVBOrb.GIOPVersion2Str(iGIOPVersion) & " fragment message received")
            Call ConnClose(ConnId, 1007) 'Is deleting oOrbConns().oReqsOI
            Call oIn.destroy
        Else
            'struct FragmentHeader_1_2 {unsigned long request_id;};
            lReqstId = oIn.readUlong()
            'If complete Then Final fragment
            Set oReqst = ConnReqCut(ConnId, lReqstId, _
                oIn.isComplete()) 'Unset wait lock if final fragment
            If oReqst Is Nothing Then
                On Error GoTo ErrLog
                Call mVBOrb.ErrRaise(1, "Fragment for unknown request " & _
                    CStr(lReqstId) & " received")
                Call oIn.destroy
                On Error Resume Next
            Else
                If oReqst.addFragment(oIn) Then 'If oIn.isComplete() Then
                    If oReqst.isIncoming() Then
                        If iLogLevel >= 8 Then
                            Call logMsg("D c" & CStr(ConnId + 1) & " Fragmented Request '" & oReqst.Operation & "' received")
                        End If
                        Set oOut = oReqst.replyRequest()
                        If Err.Number <> 0 Then Call mVBOrb.ErrSave: GoTo ErrLoad2
                        If Not oOut Is Nothing Then
                            '1 = Send Reply
                            Call oOut.sendGIOPToSocket(1, oOrbConns(ConnId).oSock)
                            If Err.Number <> 0 Then Call mVBOrb.ErrSave: GoTo ErrLoad2
                            Call oOut.destroy
                            Set oOut = Nothing
                            If Err.Number <> 0 Then Call mVBOrb.ErrSave: GoTo ErrLoad1
                        End If
                    Else
                        If iLogLevel >= 8 Then
                            Call logMsg("D c" & CStr(ConnId + 1) & " Fragmented Reply '" & oReqst.Operation & "' received")
                        End If
                    End If
                End If
            End If
        End If
    Case 2005 'received len is 0
        If iLogLevel >= 2 Then
            Call logMsg("W Empty message received")
        End If
        Call ConnClose(ConnId, msgType) 'Is deleting oOrbConns().oReqsOI
        Call oIn.destroy
    Case Else
        Call mVBOrb.ErrRaise(1, "Unknown GIOP msgType: " & CStr(msgType))
        Call mVBOrb.ErrSave
        On Error GoTo ErrLog
        Call ConnSendMessageError(ConnId)
        On Error Resume Next
        GoTo ErrLoad1
    End Select
    Exit Sub
ErrLoad2:
    On Error GoTo ErrLog
    If Not oOut Is Nothing Then
        Call oOut.destroy
        Set oOut = Nothing
    End If
ErrLoad1:
    On Error GoTo ErrLog
    Call oIn.destroy
    Set oIn = Nothing
    On Error GoTo 0
    Call mVBOrb.ErrLoad
ErrHandler:
    Call mVBOrb.ErrReraise("ConnRecvMsg")
ErrLog:
    Call logErr("ConnRecvMsg")
    Resume Next
End Sub

'Find (and cut) a waiting request
Private Function ConnReqCut(ByVal lConnId As Long, ByVal lReqId As Long, _
    ByVal bCut As Boolean) As cOrbRequest
    Dim oPrevReq As cOrbRequest, oReq As cOrbRequest
    Set oReq = oOrbConns(lConnId).oReqsOI
    Do Until oReq Is Nothing
        If oReq.ReqId = lReqId Then
            If bCut Then
                If oPrevReq Is Nothing Then
                    Set oOrbConns(lConnId).oReqsOI = oReq.NextRequest
                Else
                    Set oPrevReq.NextRequest = oReq.NextRequest
                End If
                Set oReq.NextRequest = Nothing
            End If
            Exit Do
        End If
        Set oPrevReq = oReq
        Set oReq = oReq.NextRequest
    Loop
    Set ConnReqCut = oReq
End Function

'IN:    lOCId       Host connection Id
Public Function ConnReqWait(ByVal lOCId As Long, ByVal oReqOut As cOrbRequest)
    On Error GoTo ErrHandler
    Dim lTime As Long
    'Set wait lock
    Set oReqOut.NextRequest = oOrbConns(lOCId).oReqsOI
    Set oOrbConns(lOCId).oReqsOI = oReqOut
    
    Dim bData As Boolean
    Do
        lTime = mVBOrb.dllGetTickCount() 'or mvborb.getTime()
        If oOrbConns(lOCId).oSock Is Nothing Then
            Call oReqOut.setRes(1090, Nothing)
            'Unset wait lock
            Call ConnReqCut(lOCId, oReqOut.ReqId, True)
            Exit Do
        End If
        bData = oOrbConns(lOCId).oSock.recvWait(2)
        If bData Then
            oOrbConns(lOCId).lMsgTime = lTime
            Call ConnRecvMsg(lOCId)
            If oReqOut.isRes Then Exit Do
        End If
        If mVBOrb.getLongDiff(oOrbConns(lOCId).lMsgTime, lTime) > lTimeOutReplyEnd Then
            'TimeOut
            Call oReqOut.setRes(1100, Nothing)
            'Unset wait lock
            Call ConnReqCut(lOCId, oReqOut.ReqId, True)
            Call mVBOrb.VBOrb.raiseTIMEOUT(0, mVBOrb.VBOrb.CompletedMAYBE, _
                "ReplyEndTime " & CStr(lTimeOutReplyEnd) & "ms")
        End If
        Call performWork(10)
        If oOrbConns(lOCId).oReqsOI Is Nothing Then Exit Do
    Loop
    
    'Call mvborb.ErrRaise(2, "Test") 'See Class_Terminate of cOrbObjRef
    Exit Function
ErrHandler:
    Call mVBOrb.ErrSave
    If iLogLevel >= 4 Then
        Call logMsg("I " & Err.Description)
    End If
    Resume ErrLoadRaise
ErrLoadRaise:
    On Error Resume Next
    If Not oReqOut.isRes() Then
        'Internal exception
        Call oReqOut.setRes(1090, Nothing)
    End If
    'Unset wait lock
    Call ConnReqCut(lOCId, oReqOut.ReqId, True)
    On Error GoTo 0
    Call mVBOrb.ErrLoad
    Call mVBOrb.ErrReraise("ConnReqWait")
End Function

Public Property Get localHost() As String
    localHost = oOrbLPnts(0).sOAHost
End Property

'ORB Initialization (is called by cVBOrb.init via mVBOrb.init, see there)
'Note: Mosts applications need only one global instance of cOrbImpl.
Public Sub init(ByVal ORBId As String, ByVal OAHost As String, _
    ByVal OAPort As String, ByVal OAVersion As Integer, _
    ByVal ORBDefaultInitRef As String, ByVal ORBInitRef As String, _
    ByVal LogFile As String, ByVal LogLevel As Integer, ByVal VisiWorkaround As Boolean)
    On Error GoTo ErrHandler
    sOrbId = ORBId
    lThreadsInUse = 0
    'Setting socket connection time out values in units of milli seconds
    lTimeOutConnReuse = 20000&
    lTimeOutServUnused = 600000
    lTimeOutReplyEnd = 7200000
    bOnewayRebind = True
    'Setting log file name
    sLogFile = LogFile
    iLogLevel = IIf(Len(sLogFile) = 0, 0, LogLevel)
    bVisiWorkaround = VisiWorkaround
    'Init connection arrays
    lOrbLPntCnt = 1
    ReDim oOrbLPnts(0 To lOrbLPntCnt - 1) As tOrbLPnt
    lOrbConnCnt = 2
    ReDim oOrbConns(0 To lOrbConnCnt - 1) As tOrbConn
    ReDim lSelFDs(0 To (lOrbLPntCnt + lOrbConnCnt) * 3 + 5) As Long
    'Create and bind socket for listener
    Set oOrbLPnts(0).oSock = New cOrbSocket
    If OAHost = "" Then
        'Get real name of localhost
        OAHost = oOrbLPnts(0).oSock.getHostName(True)
    End If
    'Bind the server socket immediately to retrieve the actual port
    On Error Resume Next
    Call oOrbLPnts(0).oSock.initBind(OAHost, OAPort)
    If Err.Number <> 0 Then
        Call mVBOrb.ErrSave
        If Err.Number = vbObjectError + 10048 Then 'WSAEADDRINUSE
            'Maybe WSA was not terminated last time
            Call oOrbLPnts(0).oSock.initTermAll(True)
        End If
        On Error GoTo 0 'Leaving "Resume Next" mode and is calling Err.Clear
        GoTo InitRollback
    End If
    On Error GoTo ErrHandler 'Leaving "Resume Next" mode
    'Get actual host and port
    oOrbLPnts(0).sOAHost = oOrbLPnts(0).oSock.socketHost
    If oOrbLPnts(0).sOAHost = "127.0.0.1" Then
        'Get real name of localhost
        oOrbLPnts(0).sOAHost = oOrbLPnts(0).oSock.getHostName(True)
    End If
    oOrbLPnts(0).sOAPort = oOrbLPnts(0).oSock.socketPort
    'GIOP version
    oOrbLPnts(0).iGIOPVersion = OAVersion
    'Initialize the initial references resolver
    sDefaultInitRef = ORBDefaultInitRef
    Do While ORBInitRef <> ""
        Dim lNextPos As Long
        lNextPos = InStr(1, ORBInitRef, " ")
        Dim sInitRef As String
        If lNextPos <= 0 Then
            sInitRef = ORBInitRef
            ORBInitRef = ""
        Else
            sInitRef = Mid$(ORBInitRef, 1, lNextPos - 1)
            ORBInitRef = Mid$(ORBInitRef, lNextPos + 1)
        End If
        lNextPos = InStr(1, sInitRef, "=")
        If lNextPos <= 0 Then
            Call mVBOrb.VBOrb.raiseBADPARAM(99, mVBOrb.VBOrb.CompletedNO, _
                "Missing equal sign in ORBInitRef parameter")
        End If
        Call registerInitRefStr(Mid$(sInitRef, 1, lNextPos - 1), _
            Mid$(sInitRef, lNextPos + 1))
    Loop
    iOrbState = ORBST_ISINIT
    Exit Sub
InitRollback:
    On Error Resume Next
    Set oOrbLPnts(0).oSock = Nothing
    On Error GoTo 0
    Call mVBOrb.ErrLoad
    Call mVBOrb.ErrReraise("Orb.init")
ErrHandler:
    Call mVBOrb.ErrSave
    Resume InitRollback
End Sub

'id()
Public Function id() As String
    id = sOrbId
End Function

Public Function getLogFile() As String
    getLogFile = sLogFile
End Function

'ORBLL_ERR = 1, ORBLL_WARN = 2, ORBLL_INFO = 4, ORBLL_DEBUG = 8
Public Function getLogLevel() As Integer
    getLogLevel = iLogLevel
End Function

'Write an exception onto the ORB log and delete the exception
Public Sub logException(ByVal oEx As cOrbException)
    Call mVBOrb.logException(sLogFile, oEx)
End Sub

'If "On Error Resume Next" is on then
'write the Error onto the ORB log and delete the Error
Public Sub logErr(ByRef SourcePrefix As String)
    Call mVBOrb.logErr(sLogFile, SourcePrefix)
End Sub

'Write a message onto the ORB log defined by init(LogFile:="noname.log")
Public Sub logMsg(ByRef sMsg As String)
    Call mVBOrb.logMsg(sLogFile, sMsg)
End Sub

'IN:    id      The ID by which the initial reference will be known.
'IN:    ref     The initial reference itself.
Public Sub registerInitRefStr(ByVal id As String, ByVal ref As String)
    On Error GoTo ErrHandler
    Dim oRefStr As cCBStringValue
    If id = "" Then
        Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO, _
            "InitRefId is empty")
    End If
    If ref = "" Then
        Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO, _
            "InitRef is empty")
    End If
    If colInitRefObjs Is Nothing Then
        Set colInitRefObjs = New Collection
        Set colInitRefIds = New Collection
    ElseIf Not lookupInitialReference(id) Is Nothing Then
        Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO, _
            "Id " & id & " already registered")
    End If
    Set oRefStr = New cCBStringValue
    oRefStr.Value = ref
    Call colInitRefObjs.Add(oRefStr, id)
    Call colInitRefIds.Add(id, id)
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("registerInitRefStr")
End Sub

'IN:    id      The ID by which the initial reference will be known.
'IN:    obj     The initial reference itself.
'raises(InvalidName) if empty string id or already registered. Including defaults.
Public Sub registerInitialReference(ByVal id As String, ByVal Obj As cOrbObject)
    On Error GoTo ErrHandler
    Dim oInvalidNameEx As cOrbException
    If id = "" Then
        Set oInvalidNameEx = New cOrbInvalidName
        Call oInvalidNameEx.addInfos(PostDescr:="Id is empty")
        Call raiseUserEx(oInvalidNameEx)
    End If
    If Obj Is Nothing Then
        Call mVBOrb.VBOrb.raiseBADPARAM(24, mVBOrb.VBOrb.CompletedNO, _
            "Obj parameter is null")
    End If
    If colInitRefObjs Is Nothing Then
        Set colInitRefObjs = New Collection
        Set colInitRefIds = New Collection
    ElseIf Not lookupInitialReference(id) Is Nothing Then
        Set oInvalidNameEx = New cOrbInvalidName
        Call oInvalidNameEx.addInfos(PostDescr:="Id " & id & " already registered")
        Call raiseUserEx(oInvalidNameEx)
    End If
    Call colInitRefObjs.Add(Obj, id)
    Call colInitRefIds.Add(id, id)
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("Orb.registerInitialReference")
End Sub

Private Function lookupInitialReference(ByVal id As String) As Object
    Dim oRef As Object
    On Error Resume Next
    Set oRef = colInitRefObjs.Item(id)
    If Err.Number <> 0 Then Set oRef = Nothing
    On Error GoTo 0
    Set lookupInitialReference = oRef
End Function

'list_initial_services()
'Obtaining Initial Object References
'(First version was written by Craig Neuwirt)
Public Function listInitialServices() As c_StringSeq
    Dim oList As c_StringSeq
    Set oList = New c_StringSeq
    If Not colInitRefObjs Is Nothing Then
        Dim sId As Variant 'Variant is required by 'For Each' statement
        oList.Length = colInitRefObjs.Count
        Dim lCnt As Long
        lCnt = 0
        For Each sId In colInitRefIds
            oList.Item(lCnt) = sId
            lCnt = lCnt + 1
        Next sId
    End If
    Set listInitialServices = oList
End Function

'Obtaining Initial Object References
'RootPOA            PortableServer::POA
'POACurrent
'(First version was written by Craig Neuwirt)
Public Function resolveInitialReferences(ByVal id As String) As cOrbObject
    On Error GoTo ErrHandler
    Dim oRef As Object
    Dim oRefObj As cOrbObject
    Set oRef = lookupInitialReference(id)
    If TypeName(oRef) = "cOrbObject" Then
        Set oRefObj = oRef
    Else
        Dim sRefStr As String
        If oRef Is Nothing Then
            sRefStr = sDefaultInitRef & "/" & id
        Else
            Dim oRefStr As cCBStringValue
            Set oRefStr = oRef
            sRefStr = oRefStr.Value
        End If
        Select Case id
        Case "NameService"
            Set oRefObj = str2NmCtxRef(sRefStr)
        Case Else
            Set oRefObj = stringToObject(sRefStr)
        End Select
    End If
    Set resolveInitialReferences = oRefObj
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("Orb.resolveInitialReferences")
End Function

'Get the object reference of the naming context
Private Function str2NmCtxRef(ByVal sRefStr As String) As cOrbObjRef
    On Error GoTo ErrHandler
    Dim oNmCtxRef As cOrbObjRef
    Set oNmCtxRef = stringToObject(sRefStr)
    'ObjRef should be a reference to a NamingContext
    On Error Resume Next
    Call oNmCtxRef.narrow("IDL:omg.org/CosNaming/NamingContext:1.0", True, False)
    If Err.Number <> 0 Then
        Call mVBOrb.ErrSave
        'JDK 1.3.1 ORB gives OBJECT_NOT_EXIST
        If mVBOrb.ErrIsSystemEx() And _
            Err.Number = (mVBOrb.ITF_E_OBJECT_NOT_EXIST_NO Or vbObjectError) And _
            InStr(sRefStr, "corbaloc:") = 1 And _
            Right(sRefStr, 12) = "/NameService" Then
            Set oNmCtxRef = urlCorbaloc(Mid$(sRefStr, 10, Len(sRefStr) - 20) & "INIT")
            If Err.Number <> 0 Then GoTo ErrLoadRaise
            Call oNmCtxRef.setRebindMode(1)
            Set oNmCtxRef = sunInitGetNameService(oNmCtxRef)
            If Err.Number <> 0 Then GoTo ErrLoadRaise
            Call oNmCtxRef.narrow("IDL:omg.org/CosNaming/NamingContext:1.0", True, False)
            If Err.Number <> 0 Then GoTo ErrLoadRaise
        Else
            GoTo ErrLoadRaise 'Load and raise error
        End If
        Call mVBOrb.ErrLoad 'Load and ignore error
    End If
    On Error GoTo ErrHandler
    Set str2NmCtxRef = oNmCtxRef
    Exit Function
ErrHandler:
    Call mVBOrb.ErrSave
    Resume ErrLoadRaise
ErrLoadRaise:
    On Error GoTo 0
    Call mVBOrb.ErrLoad
    Call mVBOrb.ErrReraise("str2NmCtxRef")
End Function

'Create a primitive TypeCode
Public Function createPrimitiveTc(ByVal kind As Long) As cOrbTypeCode
    On Error GoTo ErrHandler
    Dim oTC As cOrbTypeCode
    Dim sChId As String
    sChId = "T#" & CStr(kind)
    Set oTC = getCachedTc(sChId, kind)
    If oTC Is Nothing Then
        Set oTC = New cOrbTypeCode
        Call oTC.init2PrimitiveTc(sChId, kind)
        Call colCachedTCs.Add(oTC, sChId)
    End If
    Set createPrimitiveTc = oTC
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("createPrimitiveTc")
End Function

'create_struct_tc()
Public Function createStructTc(ByVal id As String, ByVal name As String, _
    ByVal members As cCBStructMemberSeq) As cOrbTypeCode
    On Error GoTo ErrHandler
    Dim oTC As cOrbTypeCode
    Set oTC = createRecursiveTc(id)
    Call oTC.setRecTc2StructTc(name, members)
    Set createStructTc = oTC
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("createStructTc")
End Function

'create_union_tc()
Public Function createUnionTc(ByVal id As String, ByVal name As String, _
    ByVal discriminator_type As cOrbTypeCode, ByVal members As cCBUnionMemberSeq) _
    As cOrbTypeCode
    On Error GoTo ErrHandler
    Dim oTC As cOrbTypeCode
    Set oTC = createRecursiveTc(id)
    Call oTC.setRecTc2UnionTc(name, discriminator_type, members)
    Set createUnionTc = oTC
    Exit Function
ErrHandler:
    Set createUnionTc = Nothing
    Call mVBOrb.ErrReraise("createUnionTc")
End Function

'create_enum_tc()
Public Function createEnumTc(ByVal id As String, ByVal name As String, _
    ByVal members As c_StringSeq) As cOrbTypeCode
    On Error GoTo ErrHandler
    Dim oTC As cOrbTypeCode
    Set oTC = createRecursiveTc(id)
    Call oTC.setRecTc2EnumTc(name, members)
    Set createEnumTc = oTC
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("createEnumTc")
End Function

'create_alias_tc()
Public Function createAliasTc(ByVal id As String, ByVal name As String, _
    ByVal original_type As cOrbTypeCode) As cOrbTypeCode
    On Error GoTo ErrHandler
    Dim oTC As cOrbTypeCode
    Set oTC = createRecursiveTc(id)
    Call oTC.setRecTc2AliasTc(name, original_type)
    Set createAliasTc = oTC
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("createAliasTc")
End Function

'create_exception_tc()
Public Function createExceptionTc(ByVal id As String, ByVal name As String, _
    ByVal members As cCBStructMemberSeq) As cOrbTypeCode
    On Error GoTo ErrHandler
    Dim oTC As cOrbTypeCode
    Set oTC = createRecursiveTc(id)
    Call oTC.setRecTc2ExceptionTc(name, members)
    Set createExceptionTc = oTC
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("createExceptionTc")
End Function

'create_interface_tc()
Public Function createInterfaceTc(ByVal id As String, ByVal name As String) _
    As cOrbTypeCode
    On Error GoTo ErrHandler
    Dim oTC As cOrbTypeCode
    Set oTC = createRecursiveTc(id)
    Call oTC.setRecTc2InterfaceTc(name)
    Set createInterfaceTc = oTC
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("createInterfaceTc")
End Function

'create_string_tc()
Public Function createStringTc(ByVal bound As Long) As cOrbTypeCode
    On Error GoTo ErrHandler
    Dim oTC As cOrbTypeCode
    Dim sChId As String
    sChId = "string"
    If bound <> 0 Then sChId = sChId & "<" & CStr(bound) & ">"
    Set oTC = getCachedTc(sChId, mCB.tk_string)
    If oTC Is Nothing Then
        Set oTC = New cOrbTypeCode
        Call oTC.init2stringTc(sChId, bound)
        Call colCachedTCs.Add(oTC, sChId)
    End If
    Set createStringTc = oTC
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("createStringTc")
End Function

'create_wstring_tc()
Public Function createWstringTc(ByVal bound As Long) As cOrbTypeCode
    On Error GoTo ErrHandler
    Dim oTC As cOrbTypeCode
    Dim sChId As String
    sChId = "wstring"
    If bound <> 0 Then sChId = sChId & "<" & CStr(bound) & ">"
    Set oTC = getCachedTc(sChId, mCB.tk_wstring)
    If oTC Is Nothing Then
        Set oTC = New cOrbTypeCode
        Call oTC.init2WstringTC(sChId, bound)
        Call colCachedTCs.Add(oTC, sChId)
    End If
    Set createWstringTc = oTC
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("createWstringTc")
End Function

'create_fixed_tc()
Public Function createFixedTc(ByVal digits As Integer, ByVal scale_ As Integer) _
    As cOrbTypeCode
    On Error GoTo ErrHandler
    Dim oTC As cOrbTypeCode
    Dim sChId As String
    sChId = "fixed<" & CStr(digits) & "," & CStr(scale_) & ">"
    Set oTC = getCachedTc(sChId, mCB.tk_fixed)
    If oTC Is Nothing Then
        Set oTC = New cOrbTypeCode
        Call oTC.init2FixedTc(sChId, digits, scale_)
        Call colCachedTCs.Add(oTC, sChId)
    End If
    Set createFixedTc = oTC
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("createFixedTc")
End Function

'create_sequence_tc()
Public Function createSequenceTc(ByVal bound As Long, _
    ByVal element_type As cOrbTypeCode) As cOrbTypeCode
    On Error GoTo ErrHandler
    Dim oTC As cOrbTypeCode
    Dim sChId As String
    sChId = "sequence<" & element_type.getChId()
    If bound <> 0 Then sChId = sChId & "," & CStr(bound)
    sChId = sChId & ">"
    Set oTC = getCachedTc(sChId, mCB.tk_sequence)
    If oTC Is Nothing Then
        Set oTC = New cOrbTypeCode
        Call oTC.init2SequenceTc(sChId, bound, element_type)
        Call colCachedTCs.Add(oTC, sChId)
    End If
    Set createSequenceTc = oTC
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("createSequenceTc")
End Function

'create_recursive_sequence_tc()
Public Function createRecursiveSequenceTc(ByVal bound As Long, _
    ByVal offset As Long) As cOrbTypeCode
    On Error GoTo ErrHandler
    Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Exit Function
ErrHandler:
    Set createRecursiveSequenceTc = Nothing
    Call mVBOrb.ErrReraise("createRecursiveSequenceTc")
End Function

'create_array_tc()
Public Function createArrayTc(ByVal Length As Long, _
    ByVal element_type As cOrbTypeCode) As cOrbTypeCode
    On Error GoTo ErrHandler
    Dim oTC As cOrbTypeCode
    Dim sChId As String
    sChId = element_type.getChId() & "[" & CStr(Length) & "]"
    Set oTC = getCachedTc(sChId, mCB.tk_array)
    If oTC Is Nothing Then
        Set oTC = New cOrbTypeCode
        Call oTC.init2ArrayTc(sChId, Length, element_type)
        Call colCachedTCs.Add(oTC, sChId)
    End If
    Set createArrayTc = oTC
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("createArrayTc")
End Function

'create_value_tc()
Public Function createValueTc(ByVal id As String, ByVal name As String, _
    ByVal type_modifier As Integer, ByVal concrete_base As cOrbTypeCode, _
    ByVal members As cCBValueMemberSeq) As cOrbTypeCode
    On Error GoTo ErrHandler
    Dim oTC As cOrbTypeCode
    Set oTC = createRecursiveTc(id)
    Call oTC.setRecTc2ValueTc(name, type_modifier, concrete_base, members)
    Set createValueTc = oTC
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("createValueTc")
End Function

'create_value_box_tc()
Public Function createValueBoxTc(ByVal id As String, ByVal name As String, _
    ByVal boxed_type As cOrbTypeCode) As cOrbTypeCode
    On Error GoTo ErrHandler
    Dim oTC As cOrbTypeCode
    Set oTC = createRecursiveTc(id)
    Call oTC.setRecTc2ValueBoxTc(name, boxed_type)
    Set createValueBoxTc = oTC
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("createValueBoxTc")
End Function

'create_native_tc()
Public Function createNativeTc(ByVal id As String, ByVal name As String) _
    As cOrbTypeCode
    On Error GoTo ErrHandler
    Dim oTC As cOrbTypeCode
    Set oTC = createRecursiveTc(id)
    Call oTC.setRecTc2NativeTc(name)
    Set createNativeTc = oTC
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("createNativeTc")
End Function

'Get previously created TypeCode or Nothing if not exists
Public Function getCachedTc(ByVal ChId As String, ByVal kind As Long) _
    As cOrbTypeCode
    On Error GoTo ErrHandler
    Dim oTC As cOrbTypeCode
    If colCachedTCs Is Nothing Then
        Set colCachedTCs = New Collection
        Set oTC = Nothing
    Else
        On Error Resume Next
        Set oTC = colCachedTCs.Item(ChId)
        If Err.Number <> 0 Then Set oTC = Nothing
        On Error GoTo ErrHandler
        If Not oTC Is Nothing Then
            If Not oTC.isCompatible(kind) Then
                Call colCachedTCs.Remove(ChId)
                Call oTC.destroy
                Set oTC = Nothing
            End If
        End If
    End If
    Set getCachedTc = oTC
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("getCachedTc")
End Function

'Get previously created recursive TypeCode or Nothing if not exists
Public Function getRecursiveTc(ByVal id As String, ByVal kind As Long) _
    As cOrbTypeCode
    On Error GoTo ErrHandler
    Set getRecursiveTc = getCachedTc(id, kind)
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("getRecursiveTc")
End Function

'create_recursive_tc()
Public Function createRecursiveTc(ByVal id As String) As cOrbTypeCode
    On Error GoTo ErrHandler
    Dim oTC As cOrbTypeCode
    Set oTC = getRecursiveTc(id, -1)
    If oTC Is Nothing Then
        Set oTC = New cOrbTypeCode
        Call oTC.init2RecursiveTc(id)
        Call colCachedTCs.Add(oTC, id)
    End If
    Set createRecursiveTc = oTC
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("createRecursiveTc")
End Function

'create_abstract_interface_tc()
Public Function createAbstractInterfaceTc(ByVal id As String, _
    ByVal name As String) As cOrbTypeCode
    On Error GoTo ErrHandler
    Dim oTC As cOrbTypeCode
    Set oTC = createRecursiveTc(id)
    Call oTC.setRecTc2AbstractInterfaceTc(name)
    Set createAbstractInterfaceTc = oTC
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("createAbstractInterfaceTc")
End Function

'create_local_interface_tc()
Public Function createLocalInterfaceTc(ByVal id As String, ByVal name As String) _
    As cOrbTypeCode
    On Error GoTo ErrHandler
    Dim oTC As cOrbTypeCode
    Set oTC = createRecursiveTc(id)
    Call oTC.setRecTc2LocalInterfaceTc(name)
    Set createLocalInterfaceTc = oTC
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("createLocalInterfaceTc")
End Function

'create_component_tc()
Public Function createComponentTc(ByVal id As String, _
    ByVal name As String) As cOrbTypeCode
    On Error GoTo ErrHandler
    Dim oTC As cOrbTypeCode
    '???Set oTC = createRecursiveTc(id)
    '???Call oTC.setRecTc2ComponentTc(name)
    Set createComponentTc = oTC
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("createComponentTc")
End Function

'create_home_tc()
Public Function createHomeTc(ByVal id As String, _
    ByVal name As String) As cOrbTypeCode
    On Error GoTo ErrHandler
    Dim oTC As cOrbTypeCode
    '???Set oTC = createRecursiveTc(id)
    '???Call oTC.setRecTc2HomeTc(name)
    Set createHomeTc = oTC
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("createHomeTc")
End Function

'create_event_tc()
Public Function createEventTc(ByVal id As String, ByVal name As String, _
    ByVal type_modifier As Integer, ByVal concrete_base As cOrbTypeCode, _
    ByVal members As cCBValueMemberSeq) As cOrbTypeCode
    On Error GoTo ErrHandler
    Dim oTC As cOrbTypeCode
    '???Set oTC = createRecursiveTc(id)
    '???Call oTC.setRecTc2EventTc(name, type_modifier, concrete_base, members)
    Set createEventTc = oTC
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("createEventTc")
End Function

'Connect (activate) a servant object to the ORB (root POA)
Public Sub connect(ByVal newImpl As cOrbSkeleton, _
    Optional ByVal sKey As String = "")
    On Error GoTo ErrHandler
    If iOrbState = ORBST_NOTEXISTS Then
        Call mVBOrb.VBOrb.raiseOBJECTNOTEXIST(1, mVBOrb.VBOrb.CompletedNO)
    End If
    If sKey = "" Then
        sKey = collImpls.Count & "_" & mVBOrb.dllGetTickCount()
    Else
        Dim objKey() As Byte
        Call mVBOrb.string2ObjKey(sKey, objKey)
        sKey = mVBOrb.objKey2String(objKey)
    End If
    Dim oObjRef As cOrbObjRef
    Set oObjRef = New cOrbObjRef
    '"1.1@host:portno/key"
    Call oObjRef.initByURL(Me, _
        ":" & mVBOrb.GIOPVersion2Str(oOrbLPnts(0).iGIOPVersion) & "@" _
        & oOrbLPnts(0).sOAHost & ":" & oOrbLPnts(0).sOAPort & "/" & sKey, _
        oOrbLPnts(0).sOAPort, newImpl.TypeId(0), mVBOrb.ONCSC, mVBOrb.ONCSW)
    Set newImpl.ObjRef = oObjRef
    Call collImpls.Add(newImpl, sKey)
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("Orb.connect")
End Sub

'Disconnect a servant object
Public Sub disconnect(ByVal oldImpl As cOrbSkeleton)
    On Error GoTo ErrHandler
    Dim oObjRef As cOrbObjRef
    Dim sKey As String
    Dim oImpl As cOrbSkeleton
    Set oObjRef = oldImpl.ObjRef
    If oObjRef Is Nothing Then
        Call mVBOrb.VBOrb.raiseINVOBJREF(1, mVBOrb.VBOrb.CompletedNO)
    End If
    sKey = oObjRef.objectKey
    On Error Resume Next
    Set oImpl = collImpls.Item(sKey)
    If Err.Number <> 0 Then Set oImpl = Nothing
    On Error GoTo ErrHandler
    If oImpl Is Nothing Then
        Call mVBOrb.VBOrb.raiseINVOBJREF(1, mVBOrb.VBOrb.CompletedNO)
    End If
    Call collImpls.Remove(sKey)
    Call oObjRef.releaseMe
    Set oldImpl.ObjRef = Nothing
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("Orb.disconnect")
End Sub

'Run. After ORB Initialization an application should call either
'run or performWork on its main thread. Run will block until the ORB has
'completed the shutdown process, initiated when some thread calls shutdown.
Public Sub run()
    On Error GoTo ErrHandler
    Do While iOrbState <> ORBST_ISDOWN
        Call performWork(10)
        DoEvents 'Prevent blocking other window processes
    Loop
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("Orb.run")
End Sub

'Indicates that the ORB needs the main thread to perform some work
Public Function workPending() As Boolean
    On Error GoTo ErrHandler
    If iOrbState = ORBST_NOTEXISTS Then
        Call mVBOrb.VBOrb.raiseOBJECTNOTEXIST(1, mVBOrb.VBOrb.CompletedNO)
    ElseIf iOrbState = ORBST_ISDOWN Then
        Call mVBOrb.VBOrb.raiseBADINVORDER(4, mVBOrb.VBOrb.CompletedNO)
    End If
    workPending = True
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("Orb.workPending")
End Function

'Called by run() with lWaitTime > 0 or called by a Timer if work pending
Public Sub performWork(Optional ByVal lWaitTime As Long = 0)
    Const sFuncName As String = "Orb.performWork"
    On Error GoTo ErrHandler
    lThreadsInUse = lThreadsInUse + 1
    If iOrbState = ORBST_NOTEXISTS Then
        'Assert that init was successfully called
        Call mVBOrb.VBOrb.raiseOBJECTNOTEXIST(1, mVBOrb.VBOrb.CompletedNO)
    ElseIf iOrbState = ORBST_ISDOWN Then
        Call mVBOrb.VBOrb.raiseBADINVORDER(4, mVBOrb.VBOrb.CompletedNO)
    ElseIf iOrbState = ORBST_ISINIT Then
        'Auto Activate if called first time
        'If Not oOrbLPnts(0).oSock.isListen Then
        Call oOrbLPnts(0).oSock.startListen
        If iLogLevel >= 4 Then
            Call logMsg("I ORB is listening on port " _
                & oOrbLPnts(0).sOAPort & " at " & oOrbLPnts(0).sOAHost)
        End If
        iOrbState = ORBST_ISACTIVE
    ElseIf iOrbState = ORBST_SHUTDOWN Then
        'Reduce timeout
        lTimeOutServUnused = 1000&
        lTimeOutConnReuse = 15000&
    End If
    
    Dim lLPnt As Long
    Dim slotCnt As Long
    Dim lFDCnt As Long
    Dim lOpenCnt As Long
    lOpenCnt = 0
    'Collect all file descriptors of open connections
    Dim lSelFDCallId As Long
    lSelFDUseId = lSelFDUseId + 1
    lSelFDCallId = lSelFDUseId
    lFDCnt = 0
    For lLPnt = 0 To lOrbLPntCnt - 1
        If Not oOrbLPnts(lLPnt).oSock Is Nothing Then
            lFDCnt = lFDCnt + 1
            lSelFDs(lFDCnt) = oOrbLPnts(lLPnt).oSock.socketFd
        End If
    Next lLPnt
    For slotCnt = 0 To lOrbConnCnt - 1
        If Not oOrbConns(slotCnt).oSock Is Nothing Then
            lFDCnt = lFDCnt + 1
            lSelFDs(lFDCnt) = oOrbConns(slotCnt).oSock.socketFd
        End If
    Next slotCnt
    If lFDCnt = 0 Then
        If iOrbState = ORBST_SHUTDOWN Then
            GoTo RestOfShutdown
        End If
        Call mVBOrb.VBOrb.raiseBADINVORDER(4, mVBOrb.VBOrb.CompletedNO)
    End If
    lSelFDs(0) = lFDCnt 'readFDs.fd_count
    lSelFDs(lOrbLPntCnt + lOrbConnCnt + 1) = 0 'writeFDs.fd_count
    lSelFDs((lOrbLPntCnt + lOrbConnCnt) * 2 + 2) = 0 'exceptFDs.fd_count
    
    'Check file descriptors, Possible to accept/read data or timeout?
    Dim timeOut As tTimeVal
    timeOut.tv_usec = (lWaitTime Mod 1000) * 1000
    timeOut.tv_sec = lWaitTime \ 1000
    If dllSelect(lOrbLPntCnt + lOrbConnCnt, lSelFDs(0), _
        lSelFDs(lOrbLPntCnt + lOrbConnCnt + 1), _
        lSelFDs((lOrbLPntCnt + lOrbConnCnt) * 2 + 2), timeOut) = -1 Then
        Call mVBOrb.VBOrb.raiseINTERNAL(1, mVBOrb.VBOrb.CompletedMAYBE, _
            "select() failed, " & CStr(Err.LastDllError))
    End If
    
    'Check all incoming sockets
    Dim lTime As Long
    lTime = mVBOrb.dllGetTickCount()  'or mvborb.getTime()
    Dim recvAgain As Boolean
    On Error Resume Next 'Is calling Err.Clear()
    For slotCnt = 0 To lOrbConnCnt - 1
        If lSelFDCallId <> lSelFDUseId Then GoTo SelFDsHasChanged
        If Not oOrbConns(slotCnt).oSock Is Nothing Then
            'Possible to read data or timeout?
            recvAgain = False
            For lFDCnt = 1 To lSelFDs(0)
                If oOrbConns(slotCnt).oSock.socketFd = lSelFDs(lFDCnt) Then
                    recvAgain = True
                    Exit For
                End If
            Next lFDCnt
            If Not recvAgain Then
                'TimeOut, Close socked if a long time unused
                If iTOCnt = 0 Then
                    If iOrbState = ORBST_SHUTDOWN And _
                        oOrbConns(slotCnt).oReqsOI Is Nothing Then
                        Call ConnSendCloseAndClose(slotCnt, 1100)
                    ElseIf ConnIsOpen(slotCnt, lTime) Then
                        lOpenCnt = lOpenCnt + 1
                    End If
                Else
                    lOpenCnt = lOpenCnt + 1
                End If
            Else 'recvAgain
                oOrbConns(slotCnt).lMsgTime = lTime
                Call ConnRecvMsg(slotCnt)
                If Err.Number <> 0 Then
                    Call logErr(sFuncName) 'Is calling Err.Clear()
                    'Close connection to get a new connection next time
                    Call ConnSendCloseAndClose(slotCnt, 1090)
                ElseIf Not oOrbConns(slotCnt).oSock Is Nothing Then
                    lOpenCnt = lOpenCnt + 1
                End If
            End If
        End If
        If Err.Number <> 0 Then
            Call logErr(sFuncName) 'Is calling Err.Clear()
        End If
    Next slotCnt
    On Error GoTo ErrHandler 'Switch off "Resume Next" mode
    'Check listener sockets
    Dim lFreeSlot As Long
    For lLPnt = 0 To lOrbLPntCnt - 1
        If lSelFDCallId <> lSelFDUseId Then GoTo SelFDsHasChanged
        If Not oOrbLPnts(lLPnt).oSock Is Nothing Then
            lOpenCnt = lOpenCnt + 1
            'Acceptable connection is pending or nothing to do?
            recvAgain = False
            For lFDCnt = 1 To lSelFDs(0)
                If oOrbLPnts(lLPnt).oSock.socketFd = lSelFDs(lFDCnt) Then
                    recvAgain = True
                    Exit For
                End If
            Next lFDCnt
            'Note: No time out here, listener can stoped by shutdown function
            If recvAgain Then
                oOrbLPnts(lLPnt).lMsgTime = lTime
                'Will accept, find a free slot
                For lFreeSlot = 0 To lOrbConnCnt - 1
                    If oOrbConns(lFreeSlot).lListenPointCnt = 0 Then
                        Exit For
                    End If
                Next lFreeSlot
                If lFreeSlot = lOrbConnCnt Then
                    Call ConnIncCnt
                End If
                'Accept
                oOrbConns(lFreeSlot).lOCIdCnt = 0
                oOrbConns(lFreeSlot).bSameProcess = False
                Set oOrbConns(lFreeSlot).oSock = New cOrbSocket
                Set oOrbConns(lFreeSlot).oReqsOI = Nothing
                oOrbConns(lFreeSlot).lMsgTime = lTime
                oOrbConns(lFreeSlot).iGIOPVersion = 0
                Call oOrbConns(lFreeSlot).oSock.initAccept(oOrbLPnts(lLPnt).oSock)
                oOrbConns(lFreeSlot).lListenPointCnt = -1
                If iLogLevel >= ORBLL_INFO Then
                    Call logMsg("I c" & CStr(lFreeSlot + 1) & _
                        " Accept new ORB connection from " & _
                        oOrbConns(lFreeSlot).oSock.socketHost & ":" & _
                        oOrbConns(lFreeSlot).oSock.socketPort)
                End If
            End If
        End If
    Next lLPnt
    'Increment timeout check counter
    iTOCnt = iTOCnt + 1
    If iTOCnt > 10 Then iTOCnt = 0
    'Do rest of shutdown
RestOfShutdown:
    If lOpenCnt <= 0 And iOrbState = ORBST_SHUTDOWN Then
        'Disconnect all objects
        Dim oImpl As cOrbSkeleton
        For Each oImpl In collImpls
            If Not oImpl.ObjRef Is Nothing Then
                Call oImpl.ObjRef.releaseMe
            End If
            Set oImpl.ObjRef = Nothing
        Next oImpl
        Dim lCnt As Long
        For lCnt = 1 To collImpls.Count
            Call collImpls.Remove(1)
        Next lCnt
        'Shutdown internal services if exists
        'Shutdown recursive TypeCode cache
        If Not colCachedTCs Is Nothing Then
            Dim oTC As cOrbTypeCode
            For Each oTC In colCachedTCs
                Call oTC.destroy
            Next oTC
            Set colCachedTCs = Nothing
        End If
        'Shutdown completed
        Call logMsg("ORB shutdown completed")
        iOrbState = ORBST_ISDOWN
    End If
SelFDsHasChanged:
    lThreadsInUse = lThreadsInUse - 1
    Exit Sub
ErrHandler:
    lThreadsInUse = lThreadsInUse - 1
    Call mVBOrb.ErrReraise(sFuncName)
End Sub

'Read an object key of a request or a locate request
Friend Function readReqObjKey(ByVal oIn As cOrbStream, ByRef sKey As String) _
    As cOrbSkeleton
    On Error GoTo ErrHandler
    Dim baObjKey() As Byte
    If oIn.getGIOPVersion <> &H102 Then
        'sequence <octet> object_key;
        Call oIn.readSeqOctet(baObjKey)
        sKey = mVBOrb.objKey2String(baObjKey)
    Else
        'TargetAddress target;
        Dim addrDisposition As Integer
        addrDisposition = oIn.readShort()
        Dim oObjRef As cOrbObjRef
        Select Case addrDisposition
        Case 0 'KeyAddr, sequence <octet> object_key;
            Call oIn.readSeqOctet(baObjKey)
            sKey = mVBOrb.objKey2String(baObjKey)
        Case 1 'ProfileAddr, IOP::TaggedProfile profile;
            Set oObjRef = New cOrbObjRef
            Call oObjRef.initByIOR(Me, oIn, True)
            sKey = oObjRef.objectKey
        Case 2 'ReferenceAddr, unsigned long selected_profile_index; IOP::IOR ior;
            Dim lSelectedProfile As Long
            lSelectedProfile = oIn.readUlong() 'Starting at 0 or 1???
            Set oObjRef = New cOrbObjRef
            Call oObjRef.initByIOR(Me, oIn)
            Call oObjRef.selectIIOPProfile(lSelectedProfile)
            sKey = oObjRef.objectKey
        Case Else
            Call mVBOrb.VBOrb.raiseMARSHAL(1, mVBOrb.VBOrb.CompletedNO, _
                "Unknown AddressingDisposition: " & CStr(addrDisposition))
        End Select
    End If
    On Error Resume Next
    Set readReqObjKey = collImpls.Item(sKey)
    If Err.Number <> 0 Then Set readReqObjKey = Nothing
    On Error GoTo ErrHandler
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("readReqObjKey")
End Function

'Execute a Client Locate Request (First version was written by Holger Beer)
Friend Sub replyLocateRequest(ByVal oIn As cOrbStream, ByRef oOut As cOrbStream)
    On Error GoTo ErrHandler
    
    'Read GIOP LocateRequest Header
    Dim ReqId As Long
    'unsigned long request_id
    ReqId = oIn.readUlong()
    'sequence <octet> object_key; or TargetAddress target;
    Dim sKey As String
    Dim oImpl As cOrbSkeleton
    Set oImpl = readReqObjKey(oIn, sKey)
    
    Dim locStatus As Long
    '0 = UNKNOWN_OBJECT, 1 = OBJECT_HERE, 2 = OBJECT_FORWARD,
    '3 = OBJECT_FORWARD_PERM, 4 = LOC_SYSTEM_EXCEPTION,
    '5 = LOC_NEEDS_ADDRESSING_MODE
    locStatus = IIf(oImpl Is Nothing, 0, 1)
    
    Call oOut.initStream(Me, oIn.getGIOPVersion)
    'Prepare GIOP Message Header
    Call oOut.writeGIOPHead
    'Write GIOP LocateReply Header
    Call writeLocateReplyHeader(oOut, ReqId, locStatus)
    
    'Do not need writing GIOP LocateReply body because locStatus is 0 or 1
    'writeLocateReqstBody():
    #If IIOP12a Then
        'Locate Reply Body alignment was erroneous added in CORBA 2.4/2.5
        'See also cOrbObjRef.readLocateReplyHeader()
        If oOut.getGIOPVersion = &H102 Then Call oOut.writeAlign(8)
    #End If
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("replyLocateRequest")
End Sub

'(First version was written by Holger Beer)
Private Sub writeLocateReplyHeader(ByVal oOut As cOrbStream, ByVal ReqId As Long, _
    ByVal locStatus As Long)
    On Error GoTo ErrHandler
    'unsigned long request_id;
    Call oOut.writeUlong(ReqId)
    'enum LocateStatusType;
    Call oOut.writeUlong(locStatus)
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("writeLocateReplyHeader")
End Sub

'4.12.4, Stops the processing of requests, completing pending requests if desired
Public Sub shutdown(ByVal sWait As Boolean)
    On Error GoTo ErrHandler
    If iOrbState = ORBST_ISACTIVE Then
        'Deactivate Object Adapters
        Dim lLPnt As Long
        For lLPnt = 0 To lOrbLPntCnt - 1
            If Not oOrbLPnts(lLPnt).oSock Is Nothing Then
                Call oOrbLPnts(lLPnt).oSock.termBind
                Set oOrbLPnts(lLPnt).oSock = Nothing
            End If
        Next lLPnt
        iOrbState = ORBST_SHUTDOWN 'Rest must be done by performWork() or run()
    ElseIf iOrbState = ORBST_ISINIT Then
        'oOrbLPnts(0).oSock.isListen = False, isOpen = True
        If Not oOrbLPnts(0).oSock Is Nothing Then
            Call oOrbLPnts(0).oSock.termBind
            Set oOrbLPnts(0).oSock = Nothing
        End If
        iOrbState = ORBST_SHUTDOWN 'Rest must be done by performWork() or run()
    End If
    If sWait Then 'Block until the shutdown is completed?
        If lThreadsInUse > 0 Then 'Blocking would result in a deadlock
                                  'if shutdown is called by a worker thread.
            Call mVBOrb.VBOrb.raiseBADINVORDER(3, mVBOrb.VBOrb.CompletedNO)
        End If
        Call run
    End If
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("Orb.shutdown")
End Sub

Friend Function isDown() As Boolean
    isDown = (iOrbState = ORBST_ISDOWN Or iOrbState = ORBST_NOTEXISTS)
End Function

'Destroys the ORB so that its resources can be reclaimed
Public Sub destroy()
    On Error GoTo ErrHandler
    If iOrbState <> ORBST_ISDOWN Then
        Call shutdown(True)
    End If
    Set colValFactories = Nothing
    Set colInitRefObjs = Nothing
    Set colInitRefIds = Nothing
    iOrbState = ORBST_NOTEXISTS
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("destroy")
End Sub

Public Property Get OnewayRebind() As Boolean
    OnewayRebind = bOnewayRebind
End Property

Public Property Let OnewayRebind(ByVal OnewayRebind As Boolean)
    bOnewayRebind = OnewayRebind
End Property

'Time out value in units of milli seconds
'(Multiply with 10000 to get the timeout value in units of 100 nanoseconds)
Public Property Get RelativeRoundtripTimeout() As Long
    RelativeRoundtripTimeout = lTimeOutReplyEnd
End Property

'Time out value in units of milli seconds
'RelativeRoundtripTimeout = ::TimeBase::TimeT / 10000
Public Property Let RelativeRoundtripTimeout(ByVal RelativeRoundtripTimeout As Long)
    lTimeOutReplyEnd = RelativeRoundtripTimeout
End Property

'Time out value in units of milli seconds
'(Multiply with 10000 to get the timeout value in units of 100 nanoseconds)
Public Property Get RelativeConnReuseTimeout() As Long
    RelativeConnReuseTimeout = lTimeOutConnReuse
End Property

'Time out value in units of milli seconds
'TimeOut of unused outgoing connections
Public Property Let RelativeConnReuseTimeout(ByVal RelativeConnReuseTimeout As Long)
    lTimeOutConnReuse = RelativeConnReuseTimeout
End Property

Public Property Get VisiWorkaround() As Boolean
    VisiWorkaround = bVisiWorkaround
End Property

'Value factory operations
'IN:    id      RepositoryId
'IN:    factory ValueFactory
'RET:   factory ???
Public Function registerValueFactory(ByRef RepositoryId As String, _
    ByVal ValueFactory As cOrbValueFactory) As cOrbValueFactory
    If colValFactories Is Nothing Then
        Set colValFactories = New Collection
    ElseIf Not lookupValueFactory(RepositoryId) Is Nothing Then
        Call colValFactories.Remove(RepositoryId)
    End If
    Call colValFactories.Add(ValueFactory, RepositoryId)
End Function

'IN:    id      RepositoryId
Public Sub unregisterValueFactory(ByRef RepositoryId As String)
    If colValFactories Is Nothing Then
        Exit Sub
    End If
    If Not lookupValueFactory(RepositoryId) Is Nothing Then
        Call colValFactories.Remove(RepositoryId)
    End If
End Sub

'IN:    id      RepositoryId
'RET:   factory ValueFactory or Nothing
Public Function lookupValueFactory(ByRef RepositoryId As String) As cOrbValueFactory
    Dim oValueFactory As cOrbValueFactory
    On Error Resume Next
    Set oValueFactory = colValFactories.Item(RepositoryId)
    If Err.Number <> 0 Then Set oValueFactory = Nothing
    On Error GoTo 0
    Set lookupValueFactory = oValueFactory
End Function

