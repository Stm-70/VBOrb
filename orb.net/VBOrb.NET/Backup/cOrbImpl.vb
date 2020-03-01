Option Strict Off
Option Explicit On
<System.Runtime.InteropServices.ProgId("cOrbImpl_NET.cOrbImpl")> Public Class cOrbImpl
	'Copyright (c) 1999-2003 Martin.Both
	
	'This library is free software; you can redistribute it and/or
	'modify it under the terms of the GNU Library General Public
	'License as published by the Free Software Foundation; either
	'version 2 of the License, or (at your option) any later version.
	
	'This library is distributed in the hope that it will be useful,
	'but WITHOUT ANY WARRANTY; without even the implied warranty of
	'MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
	'Library General Public License for more details.
	
	
	Private sOrbId As String
	Private iOrbState As Short
	Const ORBST_NOTEXISTS As Short = 0
	Const ORBST_ISINIT As Short = 1
	Const ORBST_ISACTIVE As Short = 2
	Const ORBST_SHUTDOWN As Short = 3
	Const ORBST_ISDOWN As Short = 4
	
	Private sDefaultInitRef As String
	Private colInitRefObjs As Collection
	Private colInitRefIds As Collection
	
	Private colCachedTCs As Collection
	
	Private lThreadsInUse As Integer
	Private lTimeOutConnReuse As Integer 'TimeOut of unused outgoing connections
	Private lTimeOutReplyEnd As Integer
	Private lTimeOutServUnused As Integer
	Private bOnewayRebind As Boolean
	
	'Log file name or equal "" to suppress logging
	Private sLogFile As String
	Private iLogLevel As Short
	Const ORBLL_ERR As Short = 1
	Const ORBLL_WARN As Short = 2
	Const ORBLL_INFO As Short = 4
	Const ORBLL_DEBUG As Short = 8
	Private bVisiWorkaround As Boolean
	'TimeOut check counter
	Private iTOCnt As Short
	
	'ORB object adapter is listening on port "sOAPort" at "sOAHost"
	'The sListenPoint contains dot notation of IP address or host name and OAPort
	Private Structure tOrbLPnt
		Dim sOAHost As String 'Dot notation of IP address or host name or "" if free
		Dim sOAPort As String
		Dim oSock As cOrbSocket 'Socket or nothing if closed
		Dim lMsgTime As Integer 'Last access time if oSock is not nothing
		Dim iGIOPVersion As Short '&H100, &H101, &H102
	End Structure
	Private lOrbLPntCnt As Integer 'Number of listen points <= UBound(oOrbLPnts) + 1
	'oOrbLPnts(0...lOrbLPntCnt-1) are slots for listen points of this ORB
	Private oOrbLPnts() As tOrbLPnt
	
	'Object implementation map
	Private collImpls As New Collection
	
	Private Structure tOrbConn 'Incoming (served) and outgoing connections
		Dim lListenPointCnt As Integer 'Number of foreign listen points or 0 if free
		'or -1 if incoming unidirectional connection
		Dim sListenPoints() As String 'Listen points of a foreign ORB
		Dim lOCIdCnt As Integer 'How many object references are holding an Id of this slot
		Dim bSameProcess As Boolean 'co-location, short-circuit call
		Dim oSock As cOrbSocket 'Socket or nothing if closed or bSameProcess
		Dim iGIOPVersion As Short '&H0=not yet known, &H100, &H101, &H102
		Dim bBiDir As Boolean
		Dim oReqsOI As cOrbRequest 'Outgoing request(s) waiting for an
		'answer or fragment; this blocks timeout
		'Incoming request(s) waiting for more fragments
		'; also blocking timeout
		Dim lMsgTime As Integer 'Last access time if oSock is not nothing
	End Structure
	Private lOrbConnCnt As Integer ' <= UBound(oOrbConns) + 1
	'oOrbConns(0...lOrbConnCnt-1) are slots for connections of this ORB
	Private oOrbConns() As tOrbConn
	
	'Seed for Request IDs
	Private lReqId As Integer
	
	'Structure used in select() call
	'struct timeval {
	'    long    tv_sec;         /* seconds */
	'    long    tv_usec;        /* and microseconds */
	'};
	Private Structure tTimeVal
		Dim tv_sec As Integer
		Dim tv_usec As Integer
	End Structure
	
	'int PASCAL FAR select (int nfds, fd_set FAR *readfds, fd_set FAR *writefds,
	'   fd_set FAR *exceptfds, const struct timeval FAR *timeout);
	'UPGRADE_WARNING: Structure tTimeVal may require marshalling attributes to be passed as an argument in this Declare statement. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="C429C3A5-5D47-4CD9-8F51-74A1616405DC"'
	'UPGRADE_ISSUE: Declaring a parameter 'As Any' is not supported. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="FAE78A8D-8978-4FD4-8208-5B7324A8F795"'
	'UPGRADE_ISSUE: Declaring a parameter 'As Any' is not supported. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="FAE78A8D-8978-4FD4-8208-5B7324A8F795"'
	'UPGRADE_ISSUE: Declaring a parameter 'As Any' is not supported. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="FAE78A8D-8978-4FD4-8208-5B7324A8F795"'
	Private Declare Function dllSelect Lib "Ws2_32.dll"  Alias "select"(ByVal nfds As Integer, ByRef readFDs As Any, ByRef writeFDs As Any, ByRef exceptFDs As Any, ByRef timeOut As tTimeVal) As Integer
	'Array of socket file descriptors used by select() call
	Private lSelFDs() As Integer
	Private lSelFDUseId As Integer
	
	Private colValFactories As Collection
	
	'Produce a new connection originator even valued Request ID.
	'If using the other side of a bi-directionally connection add 1 if required.
	Public Function getNextReqId() As Integer
		lReqId = lReqId + 2 'Produce only even valued Request IDs
		getNextReqId = lReqId 'Later we add 1 if we are not the connection originator
	End Function
	
	'urlAny()
	'??? The object reference should know the original string to
	'do a better recovery if an error occurs.
	Public Function stringToObject(ByRef sURL As String) As _cOrbObject
		On Error GoTo ErrHandler
		'Assert that init was successfully called
		If iOrbState = ORBST_NOTEXISTS Then
			Call mVBOrb.VBOrb.raiseBADINVORDER(0, mVBOrb.VBOrb.CompletedNO)
		End If
		Dim oObjRef As cOrbObjRef
		If InStr(sURL, "IOR:") = 1 Then
			stringToObject = urlIor(sURL)
		ElseIf InStr(sURL, "file://") = 1 Then 
			stringToObject = urlFileToObject(sURL)
		ElseIf InStr(sURL, "http://") = 1 Then 
			stringToObject = urlHttpToObject(sURL)
		ElseIf InStr(sURL, "iioploc://") = 1 Then 
			oObjRef = New cOrbObjRef
			Call oObjRef.initByURL(Me, ":" & Mid(sURL, 11))
			stringToObject = oObjRef
		ElseIf InStr(sURL, "corbaloc:") = 1 Then 
			stringToObject = urlCorbaloc(Mid(sURL, 10))
		ElseIf InStr(sURL, "corbaname:") = 1 Then 
			stringToObject = urlCorbaname(Mid(sURL, 11))
		ElseIf InStr(sURL, "ias_ejb:") = 1 Then 
			stringToObject = urlIas_ejb(Mid(sURL, 9))
		Else
			Call mVBOrb.VBOrb.raiseBADPARAM(7, mVBOrb.VBOrb.CompletedNO, "Unknown URL schema name: " & sURL)
		End If
		Exit Function
ErrHandler: 
		'UPGRADE_NOTE: Object stringToObject may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
		stringToObject = Nothing
		Call mVBOrb.ErrReraise("stringToObject")
	End Function
	
	'sIOR contains "file://"
	Private Function urlFileToObject(ByVal sURL As String) As cOrbObjRef
		On Error GoTo ErrHandler
		Dim sFileName As String
		Dim iFileNo As Short
		Dim sLine As String
		
		sFileName = Mid(sURL, 8)
		iFileNo = FreeFile
		FileOpen(iFileNo, sFileName, OpenMode.Input)
		If Not EOF(iFileNo) Then
			sLine = LineInput(iFileNo)
		End If
		FileClose(iFileNo)
		
		Dim pos As Integer
		pos = InStr(sLine, "IOR:")
		If pos = 0 Then
			Call mVBOrb.VBOrb.raiseBADPARAM(10, mVBOrb.VBOrb.CompletedNO, "File doesn't contain an IOR. URL: " & sURL)
		End If
		urlFileToObject = urlIor(Mid(sLine, pos))
		Exit Function
ErrHandler: 
		'UPGRADE_NOTE: Object urlFileToObject may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
		urlFileToObject = Nothing
		Call mVBOrb.ErrReraise("urlFileToObject")
	End Function
	
	'sIOR contains "http://"
	Private Function urlHttpToObject(ByVal sURL As String) As cOrbObjRef
		On Error GoTo ErrHandler
		Dim pos As Integer
		Dim sHostPort As String
		Dim sPath As String
		
		pos = InStr(8, sURL & "/", "/")
		sHostPort = Mid(sURL, 8, pos - 8)
		sPath = Mid(sURL, pos)
		
		Dim oSock As cOrbSocket
		oSock = New cOrbSocket
		Call oSock.initConnect(sHostPort, "80")
		Call oSock.sendString("GET " & sPath & " HTTP/1.0" & vbCrLf & vbCrLf & "User-Agent: VB-IIOP VisualBasic 24-08-1999" & vbCrLf & vbCrLf & "Accept: text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2" & vbCrLf & vbCrLf & "Connection: keep -alive" & vbCrLf & vbCrLf & vbCrLf & vbCrLf)
		
		Dim webpage As String
		webpage = oSock.recvString()
		Call oSock.termConnect()
		
		pos = InStr(webpage, "IOR:")
		If pos = 0 Then
			Call mVBOrb.VBOrb.raiseBADPARAM(10, mVBOrb.VBOrb.CompletedNO, "Webpage doesn't contain an IOR. URL: " & sURL)
		End If
		urlHttpToObject = urlIor(Mid(webpage, pos))
		Exit Function
ErrHandler: 
		'UPGRADE_NOTE: Object urlHttpToObject may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
		urlHttpToObject = Nothing
		Call mVBOrb.ErrReraise("urlHttpToObject")
	End Function
	
	'sIOR contains "IOR:..."
	Private Function urlIor(ByRef sIOR As String) As _cOrbObject
		On Error GoTo ErrHandler
		Dim oObj As _cOrbObject
		Dim oIn As cOrbStream
		oIn = New cOrbStream
		Call oIn.initStream(Me, &H100, (Len(sIOR) - 3) \ 2)
		Call oIn.recvFromIOR(sIOR)
		Call oIn.readEncapOpen((Len(sIOR) - 3) \ 2)
		oObj = oIn.readObject()
		Call oIn.readEncapClose()
		Call oIn.destroy()
		urlIor = oObj
		Exit Function
InvalidIOR: 
		Call mVBOrb.VBOrb.raiseBADPARAM(10, mVBOrb.VBOrb.CompletedNO, "IOR unmarshalling")
ErrHandler: 
		If mVBOrb.ErrIsSystemEx() And Err.Number = (mVBOrb.ITF_E_MARSHAL_NO Or vbObjectError) Then
			Resume InvalidIOR
		End If
		Call mVBOrb.ErrReraise("urlIor")
	End Function
	
	'objectToUrl()
	Public Function objectToString(ByVal Obj As _cOrbObject) As String
		On Error GoTo ErrHandler
		Dim oOut As cOrbStream
		oOut = New cOrbStream
		
		'CDR stream
		Call oOut.initStream(Me, &H100)
		
		'Like oOut.writeEncapOpen() without len
		Call oOut.writeBoolean(oOut.littleEndian)
		Call oOut.writeObject(Obj)
		
		Call oOut.sendToIOR(objectToString)
		
		Call oOut.destroy()
		Exit Function
ErrHandler: 
		Call mVBOrb.ErrReraise("objectToString")
	End Function
	
	'IN:    sURL            AddrList [ '/' ObjKey ] [ '#' StringName ]
	'I/O:   sKeyString      Key after / character
	'I/O:   sStringName     Name after # sign
	'RET:   sAddrList       AddrList
	Private Function urlIiopAddrList(ByRef sURL As String, ByRef sKeyString As String, ByRef sStringName As String) As String
		On Error GoTo ErrHandler
		Dim lStartPos As Integer
		Dim lNextPos As Integer
		Dim lEndPos As Integer
		
		lStartPos = 1
		lEndPos = Len(sURL)
		
		lNextPos = InStr(lStartPos, sURL, "#")
		If lNextPos >= lStartPos And lNextPos <= lEndPos Then
			sStringName = Mid(sURL, lNextPos + 1, lEndPos - lNextPos)
			lEndPos = lNextPos - 1
		End If
		
		lNextPos = InStr(lStartPos, sURL, "/")
		If lNextPos >= lStartPos And lNextPos <= lEndPos Then
			sKeyString = Mid(sURL, lNextPos + 1, lEndPos - lNextPos)
			lEndPos = lNextPos - 1
		End If
		
		urlIiopAddrList = Mid(sURL, lStartPos, lEndPos - lStartPos + 1)
		
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
			Call mVBOrb.VBOrb.raiseBADPARAM(9, mVBOrb.VBOrb.CompletedNO, "Inadequate # character in 'corbaloc:" & sURL & "'")
		End If
		If Len(sAddrList) <= 0 Then
			Call mVBOrb.VBOrb.raiseBADPARAM(8, mVBOrb.VBOrb.CompletedNO, "Missing an address in 'corbaloc:" & sURL & "'")
		ElseIf InStr(1, sAddrList, ":") <= 0 Then 
			Call mVBOrb.VBOrb.raiseBADPARAM(9, mVBOrb.VBOrb.CompletedNO, "Missing a protocol identifier in address '" & sAddrList & "' of 'corbaloc:" & sURL & "'")
		ElseIf sAddrList = "rir:" Then 
			'There is no version or address information when rir is used.
			urlCorbaloc = resolveInitialReferences(sKeyString)
		Else
			urlCorbaloc = New cOrbObjRef
			Call urlCorbaloc.initByURL(Me, sAddrList & "/" & sKeyString)
		End If
		Exit Function
ErrHandler: 
		'UPGRADE_NOTE: Object urlCorbaloc may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
		urlCorbaloc = Nothing
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
		oNmCtxRef = str2NmCtxRef("corbaloc:" & sAddrList & "/" & sKeyString)
		'Call the resolve operation of the NamingContext
		Dim sName As String
		sName = mVBOrb.nameUrl2NameStr(sStringName)
		urlCorbaname = nameContextResolveName(oNmCtxRef, sName)
		Exit Function
ErrHandler: 
		Call mVBOrb.ErrSave()
		Resume ErrLoadRaise
ErrLoadRaise: 
		On Error GoTo 0
		Call mVBOrb.ErrLoad()
		Call mVBOrb.ErrReraise("urlCorbaname")
	End Function
	
	Private Function urlIas_ejb(ByRef sURL As String) As cOrbObjRef
		On Error GoTo ErrHandler
		Dim startPos As Integer
		Dim nextPos As Integer
		Dim sAddrList As String
		Dim sRmiId As String
		Dim sEjbId As String
		
		startPos = 1
		nextPos = InStr(startPos, sURL, "/RMI:")
		If nextPos <= 0 Then
			Call mVBOrb.ErrRaise(1, "Missing /RMI Id: " & sURL)
		End If
		sAddrList = Mid(sURL, startPos, nextPos - startPos)
		startPos = nextPos + 1
		nextPos = InStr(startPos, sURL, "/")
		If nextPos <= 0 Then
			Call mVBOrb.ErrRaise(1, "Missing /EJB Id: " & sURL)
		End If
		sRmiId = Mid(sURL, startPos, nextPos - startPos)
		sEjbId = Mid(sURL, nextPos + 1, Len(sURL) - nextPos)
		
		Dim oOutIn As cOrbStream
		oOutIn = New cOrbStream
		Call oOutIn.initStream(Me, &H100)
		Call oOutIn.writeGIOPHead()
		Call oOutIn.writeEncapOpen(False)
		Call oOutIn.writeOctet(Asc("P"))
		Call oOutIn.writeOctet(Asc("M"))
		Call oOutIn.writeOctet(Asc("C"))
		Call oOutIn.writeOctet(0)
		Call oOutIn.writeString(sRmiId)
		Call oOutIn.writeString(sEjbId)
		Call oOutIn.writeString("/persistent")
		Call oOutIn.writeEncapClose()
		Call oOutIn.sendGIOPToReadAgain()
		Dim baObjKey() As Byte
		Call oOutIn.readSeqOctet(baObjKey)
		Dim sKeyString As String
		sKeyString = mVBOrb.objKey2String(baObjKey)
		Call oOutIn.destroy()
		
		Dim oObjRef As cOrbObjRef
		oObjRef = New cOrbObjRef
		Call oObjRef.initByURL(Me, sAddrList & "/" & sKeyString)
		urlIas_ejb = oObjRef
		Exit Function
ErrHandler: 
		Call mVBOrb.ErrReraise("urlIas_ejb")
	End Function
	
	'One more connection slot please
	Private Sub ConnIncCnt()
		On Error GoTo ErrHandler
		If lOrbConnCnt >= 1000 Then
			'Max. Connection limit reached
			Call mVBOrb.VBOrb.raiseIMPLIMIT(99, mVBOrb.VBOrb.CompletedNO, "More than 1000 connections")
		End If
		If lOrbConnCnt > UBound(oOrbConns) Then
			ReDim Preserve oOrbConns(lOrbConnCnt + 2)
			lSelFDUseId = lSelFDUseId + 1
			ReDim lSelFDs((lOrbLPntCnt + lOrbConnCnt + 2) * 3 + 5)
		End If
		lOrbConnCnt = lOrbConnCnt + 1
		If iLogLevel >= 4 Then
			Call mVBOrb.logMsg("I One more ORB connection, now: " & lOrbConnCnt)
		End If
		Exit Sub
ErrHandler: 
		Call mVBOrb.ErrReraise("ConnIncCnt")
	End Sub
	
	'Allocate an outgoing connection slot
	'IN:    sHostPort   Dot notation or hostname and port
	'RET:   lOCId       Outgoing connection slot Id
	Public Function ConnOCIdAlloc(ByRef sHostPort As String) As Integer
		On Error GoTo ErrHandler
		If sHostPort = "" Then
			Call mVBOrb.VBOrb.raiseINVOBJREF(1, mVBOrb.VBOrb.CompletedNO)
		End If
		Dim lFirstFreeId As Integer
		Dim lFirstSuitId As Integer
		lFirstFreeId = lOrbConnCnt
		lFirstSuitId = lOrbConnCnt
		Dim lOCId As Integer
		Dim lLP As Integer
		'Find a suitable slot
		For lOCId = 0 To lOrbConnCnt - 1
			If oOrbConns(lOCId).lListenPointCnt > 0 Then 'An allocated outgoing slot
				For lLP = 0 To oOrbConns(lOCId).lListenPointCnt - 1
					If oOrbConns(lOCId).sListenPoints(lLP) = sHostPort Then
						'Found a suitable slot
						If Not oOrbConns(lOCId).oSock Is Nothing Or oOrbConns(lOCId).bSameProcess Then
							'Use immediately
							oOrbConns(lOCId).lOCIdCnt = oOrbConns(lOCId).lOCIdCnt + 1
							ConnOCIdAlloc = lOCId
							Exit Function
						End If
						If oOrbConns(lOCId).lListenPointCnt = 1 And lFirstSuitId > lOCId Then
							lFirstSuitId = lOCId
						End If
					End If
				Next lLP
			ElseIf oOrbConns(lOCId).lListenPointCnt = 0 Then  'A free slot
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
			Call ConnIncCnt()
		End If
		'Allocate free slot
		oOrbConns(lOCId).lListenPointCnt = 1
		ReDim oOrbConns(lOCId).sListenPoints(0)
		oOrbConns(lOCId).sListenPoints(0) = sHostPort
		oOrbConns(lOCId).bSameProcess = False
		'UPGRADE_NOTE: Object oOrbConns().oSock may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
		oOrbConns(lOCId).oSock = Nothing
		oOrbConns(lOCId).lOCIdCnt = 1
		ConnOCIdAlloc = lOCId
		Exit Function
ErrHandler: 
		Call mVBOrb.ErrReraise("ConnOCIdAlloc")
	End Function
	
	'Release an outgoing connection slot
	'IN:    ConnId      ORB connection slot Id
	'IN:    MarkOnly    Don't touch global Err object
	Public Sub ConnOCIdFree(ByVal ConnId As Integer, ByVal MarkOnly As Boolean)
		oOrbConns(ConnId).lOCIdCnt = oOrbConns(ConnId).lOCIdCnt - 1
		If Not MarkOnly And oOrbConns(ConnId).lOCIdCnt = 0 And Not oOrbConns(ConnId).bBiDir Then
			Call ConnSendCloseAndClose(ConnId, 1005)
		End If
	End Sub
	
	'Check timeout, Get an [open] socket connection
	'IN:    ConnId      ORB connection slot Id
	'I/O:   AutoOpen    IN: Open or reopen if closed / OUT: New connection?
	'OUT:   SameProcess co-location?
	'RET:               Socket object or Nothing e.g. if same process
	Public Function ConnGet(ByRef SameProcess As Boolean, ByVal ConnId As Integer, ByRef AutoOpen As Boolean) As cOrbSocket
		On Error GoTo ErrHandler
		'co-location?
		SameProcess = oOrbConns(ConnId).bSameProcess
		If SameProcess Then
			AutoOpen = False
			'UPGRADE_NOTE: Object ConnGet may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
			ConnGet = Nothing
			Exit Function
		End If
		Dim lTime As Integer
		lTime = mVBOrb.dllGetTickCount() 'or mvborb.getTime()
		'Connection is closed or timed out?
		Dim newSocket As cOrbSocket 'Don't use As New cOrbSocket
		If ConnIsOpen(ConnId, lTime) Then
			AutoOpen = False
		Else
			If Not oOrbConns(ConnId).oReqsOI Is Nothing Then
				Call mVBOrb.VBOrb.raiseINTERNAL(1, mVBOrb.VBOrb.CompletedNO, "bad lock")
			End If
			If Not AutoOpen Then
				Call mVBOrb.VBOrb.raiseREBIND(98, mVBOrb.VBOrb.CompletedNO, "Connection is closed, NO_RECONNECT")
			End If
			'Open or reopen a connection on the first listen point
			If oOrbConns(ConnId).lListenPointCnt <> 1 Then
				'Force rebind because this ORB don't remember the right listen point
				Call mVBOrb.VBOrb.raiseREBIND(99, mVBOrb.VBOrb.CompletedNO, "Please open a new connection")
			End If
			newSocket = New cOrbSocket
			Call newSocket.openSocket(oOrbConns(ConnId).sListenPoints(0), "683")
			If newSocket.socketHost = oOrbLPnts(0).sOAHost And newSocket.socketPort = oOrbLPnts(0).sOAPort Then
				If iLogLevel >= 4 Then
					Call mVBOrb.logMsg("I c" & CStr(ConnId + 1) & " Open ORB internal connection")
				End If
				'co-location, short-circuit call
				oOrbConns(ConnId).bSameProcess = True
				SameProcess = True
				'UPGRADE_NOTE: Object newSocket may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
				newSocket = Nothing
			Else
				If iLogLevel >= 4 Then
					Call mVBOrb.logMsg("I c" & CStr(ConnId + 1) & " Open ORB connection to " & newSocket.socketHost & ":" & newSocket.socketPort)
				End If
				Call newSocket.connectSocket()
			End If
			'Register new connection
			oOrbConns(ConnId).oSock = newSocket
			AutoOpen = True
		End If
		'Set access time
		oOrbConns(ConnId).lMsgTime = lTime
		ConnGet = oOrbConns(ConnId).oSock
		Exit Function
ErrHandler: 
		SameProcess = False
		'UPGRADE_NOTE: Object ConnGet may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
		ConnGet = Nothing
		Call mVBOrb.ErrReraise("Orb.ConnGet")
	End Function
	
	'Get the GIOPVersion of an open connection
	Private Function ConnGIOPVersion(ByVal ConnId As Integer) As Short
		ConnGIOPVersion = oOrbConns(ConnId).iGIOPVersion
		If ConnGIOPVersion = 0 Then
			ConnGIOPVersion = oOrbLPnts(0).iGIOPVersion
		End If
	End Function
	
	'Check connection if no timeout is happen
	'IN:    ConnId      Connection slot Id
	'IN:    lTime       GetTickCount() or mvborb.getTime()
	'RET:               Connection is still open and usable?
	Private Function ConnIsOpen(ByVal ConnId As Integer, ByVal lTime As Integer) As Boolean
		On Error GoTo ErrHandler
		Dim lAge As Integer
		If oOrbConns(ConnId).bSameProcess Then
			ConnIsOpen = True
		ElseIf oOrbConns(ConnId).oSock Is Nothing Then 
			ConnIsOpen = False
		ElseIf Not oOrbConns(ConnId).oSock.isOpen Then 
			'UPGRADE_NOTE: Object oOrbConns().oSock may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
			oOrbConns(ConnId).oSock = Nothing
			ConnIsOpen = False
		Else
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
			ElseIf oOrbConns(ConnId).oSock.isOutgoing() And Not oOrbConns(ConnId).bBiDir Then 
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
		Call mVBOrb.logErr("Orb.ConnIsOpen")
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
	Public Sub ConnClose(ByVal ConnId As Integer, ByVal ReqCancelType As Short)
		On Error GoTo ErrLog
		Dim thisReq As cOrbRequest
		Dim nextReq As cOrbRequest
		If Not oOrbConns(ConnId).oReqsOI Is Nothing Then
			thisReq = oOrbConns(ConnId).oReqsOI
			If thisReq.isOutgoing() Then
				'Cancel all pending requests
				Do 
					Call thisReq.setRes(ReqCancelType, Nothing)
					nextReq = thisReq.NextRequest
					If nextReq Is Nothing Then Exit Do
					thisReq = nextReq
				Loop 
			End If
			'UPGRADE_NOTE: Object oOrbConns().oReqsOI may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
			oOrbConns(ConnId).oReqsOI = Nothing
		End If
		If oOrbConns(ConnId).bSameProcess Then
			If iLogLevel >= 4 Then
				Call mVBOrb.logMsg("I c" & CStr(ConnId + 1) & " Close ORB internal connection")
			End If
			oOrbConns(ConnId).bSameProcess = False
		ElseIf Not oOrbConns(ConnId).oSock Is Nothing Then 
			If iLogLevel >= 4 Then
				Call mVBOrb.logMsg("I c" & CStr(ConnId + 1) & " Close ORB socket connection")
			End If
			If oOrbConns(ConnId).oSock.isOpen Then
				If oOrbConns(ConnId).oSock.isOutgoing Then
					Call oOrbConns(ConnId).oSock.termConnect()
				ElseIf oOrbConns(ConnId).oSock.isIncoming Then 
					Call oOrbConns(ConnId).oSock.termAccept()
				End If
			End If
		End If
		oOrbConns(ConnId).iGIOPVersion = 0
		'UPGRADE_NOTE: Object oOrbConns().oSock may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
		oOrbConns(ConnId).oSock = Nothing
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
		Call mVBOrb.logErr("ConnClose")
		Resume Next
	End Sub
	
	'Free a connection slot
	'IN:    ConnId       Connection slot Id
	Private Sub ConnSendCloseAndClose(ByVal ConnId As Integer, ByVal ReqCancelType As Short)
		On Error GoTo ErrLog
		Call ConnSendCloseConnection(ConnId)
		Call ConnClose(ConnId, ReqCancelType)
		Exit Sub
ErrLog: 
		Call mVBOrb.logErr("Orb.ConnSendCloseAndClose")
		Resume Next
	End Sub
	
	'Send MessageError if socket is open
	'IN:    ConnId      Connection slot Id
	Private Sub ConnSendMessageError(ByVal ConnId As Integer)
		Dim oOut As cOrbStream
		On Error GoTo ErrHandler
		If oOrbConns(ConnId).bSameProcess Or oOrbConns(ConnId).oSock Is Nothing Then
			Exit Sub
		End If
		On Error Resume Next
		If oOrbConns(ConnId).oSock.isOpen Then
			oOut = New cOrbStream
			Call oOut.initStream(Me, ConnGIOPVersion(ConnId))
			If Err.Number <> 0 Then Call mVBOrb.ErrSave() : GoTo ErrLoad1
			'Prepare GIOP Message Header
			Call oOut.writeGIOPHead()
			If Err.Number <> 0 Then Call mVBOrb.ErrSave() : GoTo ErrLoad2
			'6 = MessageError
			Call mVBOrb.logMsg("E c" & CStr(ConnId + 1) & " Send MessageError")
			Call oOut.sendGIOPToSocket(6, oOrbConns(ConnId).oSock)
			If Err.Number <> 0 Then Call mVBOrb.ErrSave() : GoTo ErrLoad2
			Call oOut.destroy()
			If Err.Number <> 0 Then Call mVBOrb.ErrSave() : GoTo ErrLoad1
			'UPGRADE_NOTE: Object oOut may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
			oOut = Nothing
		End If
		Exit Sub
ErrLoad2: 
		Call oOut.destroy()
ErrLoad1: 
		'UPGRADE_NOTE: Object oOut may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
		oOut = Nothing
		On Error GoTo 0
		Call mVBOrb.ErrLoad()
ErrHandler: 
		Call mVBOrb.ErrReraise("ConnSendMessageError")
	End Sub
	
	'Send CloseConnection if socket is open
	'IN:    ConnId      Connection slot Id
	Private Sub ConnSendCloseConnection(ByVal ConnId As Integer)
		Dim oOut As cOrbStream
		On Error GoTo ErrHandler
		If oOrbConns(ConnId).bSameProcess Or oOrbConns(ConnId).oSock Is Nothing Then
			Exit Sub
		End If
		On Error Resume Next
		Dim iGIOPVersion As Short
		If oOrbConns(ConnId).oSock.isOpen Then
			iGIOPVersion = ConnGIOPVersion(ConnId)
			If (oOrbConns(ConnId).oSock.isIncoming Or iGIOPVersion >= &H102) And Not VisiWorkaround Then 'fix for Visi5 by Andreas Roth
				'Use VisiWorkaround if Visi5 closes ALL connections
				'after receiving a CloseConnection message
				oOut = New cOrbStream
				Call oOut.initStream(Me, iGIOPVersion)
				If Err.Number <> 0 Then Call mVBOrb.ErrSave() : GoTo ErrLoad1
				'Prepare GIOP Message Header
				Call oOut.writeGIOPHead()
				If Err.Number <> 0 Then Call mVBOrb.ErrSave() : GoTo ErrLoad2
				'5 = CloseConnection
				If iLogLevel >= 4 Then
					Call mVBOrb.logMsg("I c" & CStr(ConnId + 1) & " Send CloseConnection")
				End If
				Call oOut.sendGIOPToSocket(5, oOrbConns(ConnId).oSock)
				If Err.Number <> 0 Then Call mVBOrb.ErrSave() : GoTo ErrLoad2
				Call oOut.destroy()
				If Err.Number <> 0 Then Call mVBOrb.ErrSave() : GoTo ErrLoad1
				'UPGRADE_NOTE: Object oOut may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
				oOut = Nothing
			End If
		End If
		Exit Sub
ErrLoad2: 
		Call oOut.destroy()
ErrLoad1: 
		'UPGRADE_NOTE: Object oOut may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
		oOut = Nothing
		On Error GoTo 0
		Call mVBOrb.ErrLoad()
ErrHandler: 
		Call mVBOrb.ErrReraise("ConnSendCloseConnection")
	End Sub
	
	'Receive GIOP message
	'IN:    ConnId      Connection slot Id
	Private Sub ConnRecvMsg(ByVal ConnId As Integer)
		On Error Resume Next
		Dim oIn As cOrbStream
		Dim oOut As cOrbStream
		oIn = New cOrbStream
		Call oIn.initStream(Me, &H100)
		If Err.Number <> 0 Then Call mVBOrb.ErrSave() : GoTo ErrLoad1
		Dim msgType As Short
		'Receive GIOP Message Header
		msgType = oIn.recvGIOPFromSocket(oOrbConns(ConnId).oSock)
		If Err.Number <> 0 Then
			Call mVBOrb.ErrSave()
			If Err.Number = mVBOrb.VBOrb.ITF_E_MARSHAL_NO Then
				On Error GoTo ErrLog
				Call ConnSendMessageError(ConnId)
				Call ConnClose(ConnId, 1006) 'Is deleting oOrbConns().oReqsOi
				On Error Resume Next
			End If
			GoTo ErrLoad1
		End If
		Dim iGIOPVersion As Short
		iGIOPVersion = oIn.getGIOPVersion()
		oOrbConns(ConnId).iGIOPVersion = iGIOPVersion
		Dim lHeadPos As Integer
		Dim lReqstId As Integer
		Dim oReqst As cOrbRequest
		Dim seqSC, i1 As Integer
		Select Case msgType
			Case 0 '= GIOP Request received
				lHeadPos = oIn.getPos()
				'Read GIOP Request Header
				If iGIOPVersion < &H102 Then
					'Unfortunately we must read over the IOP::ServiceContextList
					'just to get the following request id.
					seqSC = oIn.readUlong()
					For i1 = 1 To seqSC
						Call oIn.readUlong()
						Call oIn.readSkip(oIn.readUlong())
					Next i1
				End If
				'unsigned long request_id;
				lReqstId = oIn.readUlong()
				Call oIn.setPos(lHeadPos)
				oReqst = New cOrbRequest
				If oReqst.initInRequest(Me, lReqstId, oIn) Then 'If oIn.isComplete() Then
					If iLogLevel >= 8 Then
						Call mVBOrb.logMsg("D c" & CStr(ConnId + 1) & " Request '" & oReqst.Operation & "' received")
					End If
					oOut = oReqst.replyRequest()
					If Err.Number <> 0 Then Call mVBOrb.ErrSave() : GoTo ErrLoad2
					If Not oOut Is Nothing Then
						'1 = Send Reply
						Call oOut.sendGIOPToSocket(1, oOrbConns(ConnId).oSock)
						If Err.Number <> 0 Then Call mVBOrb.ErrSave() : GoTo ErrLoad2
						Call oOut.destroy()
						'UPGRADE_NOTE: Object oOut may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
						oOut = Nothing
						If Err.Number <> 0 Then Call mVBOrb.ErrSave() : GoTo ErrLoad1
					End If
				Else
					'Set wait lock
					oReqst.NextRequest = oOrbConns(ConnId).oReqsOI
					oOrbConns(ConnId).oReqsOI = oReqst
				End If
			Case 1 '= GIOP Reply received
				lHeadPos = oIn.getPos()
				'Read GIOP Reply Header
				If iGIOPVersion < &H102 Then
					'Unfortunately we must read over the IOP::ServiceContextList
					'just to get the following request id.
					seqSC = oIn.readUlong()
					For i1 = 1 To seqSC
						Call oIn.readUlong()
						Call oIn.readSkip(oIn.readUlong())
					Next i1
				End If
				'unsigned long request_id;
				lReqstId = oIn.readUlong()
				Call oIn.setPos(lHeadPos)
				'If incomplete Then First fragment
				oReqst = ConnReqCut(ConnId, lReqstId, oIn.isComplete()) 'Unset wait lock if final fragment
				If oReqst Is Nothing Then
					On Error GoTo ErrLog
					Call mVBOrb.ErrRaise(1, "Reply for unknown request " & CStr(lReqstId) & " received")
					Call oIn.destroy()
					On Error Resume Next
				Else
					Call oReqst.setRes(msgType, oIn)
					If iLogLevel >= 8 Then
						If oIn.isComplete() Then
							Call mVBOrb.logMsg("D c" & CStr(ConnId + 1) & " Reply '" & oReqst.Operation & "' received")
						End If
					End If
				End If
			Case 2 '= CancelRequest received
				'Read GIOP CancelRequestHeader
				'unsigned long request_id;
				lReqstId = oIn.readUlong()
				oReqst = ConnReqCut(ConnId, lReqstId, True) 'Unset wait lock
				If oReqst Is Nothing Then
					On Error GoTo ErrLog
					Call mVBOrb.ErrRaise(1, "CancelRequest for unknown request " & CStr(lReqstId) & " received")
					On Error Resume Next
				Else
					Call oReqst.setRes(msgType, Nothing)
				End If
				Call oIn.destroy()
			Case 3 '= Locate Request received
				oOut = New cOrbStream
				Call replyLocateRequest(oIn, oOut)
				If Err.Number <> 0 Then Call mVBOrb.ErrSave() : GoTo ErrLoad2
				If Not oOut Is Nothing Then
					'4 = Send LocateReply
					Call oOut.sendGIOPToSocket(4, oOrbConns(ConnId).oSock)
					If Err.Number <> 0 Then Call mVBOrb.ErrSave() : GoTo ErrLoad2
					Call oOut.destroy()
					'UPGRADE_NOTE: Object oOut may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
					oOut = Nothing
					If Err.Number <> 0 Then Call mVBOrb.ErrSave() : GoTo ErrLoad1
				End If
			Case 4 '= GIOP LocateReply received
				lHeadPos = oIn.getPos()
				'Read GIOP LocateReply Header
				'unsigned long request_id;
				lReqstId = oIn.readUlong()
				Call oIn.setPos(lHeadPos)
				oReqst = ConnReqCut(ConnId, lReqstId, True) 'Unset wait lock
				If oReqst Is Nothing Then
					Call mVBOrb.ErrRaise(1, "Unknown LocateReply received")
					Call mVBOrb.ErrSave() : GoTo ErrLoad1
				Else
					Call oReqst.setRes(msgType, oIn)
				End If
			Case 5 '5 = GIOP CloseConnection received
				If iLogLevel >= 4 Then
					Call mVBOrb.logMsg("I CloseConnection message received")
				End If
				Call ConnClose(ConnId, msgType) 'Is deleting oOrbConns().oReqsOI
				Call oIn.destroy()
			Case 6 '= MessageError received
				If iLogLevel >= 2 Then
					Call mVBOrb.logMsg("W MessageError message received")
				End If
				Call ConnClose(ConnId, msgType) 'Is deleting oOrbConns().oReqsOI
				Call oIn.destroy()
			Case 7 '= Fragment received
				If iGIOPVersion < &H102 Then
					Call mVBOrb.logMsg("E Unsupported GIOP " & mVBOrb.GIOPVersion2Str(iGIOPVersion) & " fragment message received")
					Call ConnClose(ConnId, 1007) 'Is deleting oOrbConns().oReqsOI
					Call oIn.destroy()
				Else
					'struct FragmentHeader_1_2 {unsigned long request_id;};
					lReqstId = oIn.readUlong()
					'If complete Then Final fragment
					oReqst = ConnReqCut(ConnId, lReqstId, oIn.isComplete()) 'Unset wait lock if final fragment
					If oReqst Is Nothing Then
						On Error GoTo ErrLog
						Call mVBOrb.ErrRaise(1, "Fragment for unknown request " & CStr(lReqstId) & " received")
						Call oIn.destroy()
						On Error Resume Next
					Else
						If oReqst.addFragment(oIn) Then 'If oIn.isComplete() Then
							If oReqst.isIncoming() Then
								If iLogLevel >= 8 Then
									Call mVBOrb.logMsg("D c" & CStr(ConnId + 1) & " Fragmented Request '" & oReqst.Operation & "' received")
								End If
								oOut = oReqst.replyRequest()
								If Err.Number <> 0 Then Call mVBOrb.ErrSave() : GoTo ErrLoad2
								If Not oOut Is Nothing Then
									'1 = Send Reply
									Call oOut.sendGIOPToSocket(1, oOrbConns(ConnId).oSock)
									If Err.Number <> 0 Then Call mVBOrb.ErrSave() : GoTo ErrLoad2
									Call oOut.destroy()
									'UPGRADE_NOTE: Object oOut may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
									oOut = Nothing
									If Err.Number <> 0 Then Call mVBOrb.ErrSave() : GoTo ErrLoad1
								End If
							Else
								If iLogLevel >= 8 Then
									Call mVBOrb.logMsg("D c" & CStr(ConnId + 1) & " Fragmented Reply '" & oReqst.Operation & "' received")
								End If
							End If
						End If
					End If
				End If
			Case 2005 'received len is 0
				If iLogLevel >= 2 Then
					Call mVBOrb.logMsg("W Empty message received")
				End If
				Call ConnClose(ConnId, msgType) 'Is deleting oOrbConns().oReqsOI
				Call oIn.destroy()
			Case Else
				Call mVBOrb.ErrRaise(1, "Unknown GIOP msgType: " & CStr(msgType))
				Call mVBOrb.ErrSave()
				On Error GoTo ErrLog
				Call ConnSendMessageError(ConnId)
				On Error Resume Next
				GoTo ErrLoad1
		End Select
		Exit Sub
ErrLoad2: 
		On Error GoTo ErrLog
		If Not oOut Is Nothing Then
			Call oOut.destroy()
			'UPGRADE_NOTE: Object oOut may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
			oOut = Nothing
		End If
ErrLoad1: 
		On Error GoTo ErrLog
		Call oIn.destroy()
		'UPGRADE_NOTE: Object oIn may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
		oIn = Nothing
		On Error GoTo 0
		Call mVBOrb.ErrLoad()
ErrHandler: 
		Call mVBOrb.ErrReraise("ConnRecvMsg")
ErrLog: 
		Call mVBOrb.logErr("ConnRecvMsg")
		Resume Next
	End Sub
	
	'Find (and cut) a waiting request
	Private Function ConnReqCut(ByVal lConnId As Integer, ByVal lReqId As Integer, ByVal bCut As Boolean) As cOrbRequest
		Dim oPrevReq, oReq As cOrbRequest
		oReq = oOrbConns(lConnId).oReqsOI
		Do Until oReq Is Nothing
			If oReq.ReqId = lReqId Then
				If bCut Then
					If oPrevReq Is Nothing Then
						oOrbConns(lConnId).oReqsOI = oReq.NextRequest
					Else
						oPrevReq.NextRequest = oReq.NextRequest
					End If
					'UPGRADE_NOTE: Object oReq.NextRequest may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
					oReq.NextRequest = Nothing
				End If
				Exit Do
			End If
			oPrevReq = oReq
			oReq = oReq.NextRequest
		Loop 
		ConnReqCut = oReq
	End Function
	
	'IN:    lOCId       Host connection Id
	Public Function ConnReqWait(ByVal lOCId As Integer, ByVal oReqOut As cOrbRequest) As Object
		On Error GoTo ErrHandler
		Dim lTime As Integer
		'Set wait lock
		oReqOut.NextRequest = oOrbConns(lOCId).oReqsOI
		oOrbConns(lOCId).oReqsOI = oReqOut
		
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
				Call mVBOrb.VBOrb.raiseTIMEOUT(0, mVBOrb.VBOrb.CompletedMAYBE, "ReplyEndTime " & CStr(lTimeOutReplyEnd) & "ms")
			End If
			Call performWork(10)
			If oOrbConns(lOCId).oReqsOI Is Nothing Then Exit Do
		Loop 
		
		'Call mvborb.ErrRaise(2, "Test") 'See Class_Terminate of cOrbObjRef
		Exit Function
ErrHandler: 
		Call mVBOrb.ErrSave()
		If iLogLevel >= 4 Then
			Call mVBOrb.logMsg("I " & Err.Description)
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
		Call mVBOrb.ErrLoad()
		Call mVBOrb.ErrReraise("ConnReqWait")
	End Function
	
	Public ReadOnly Property localHost() As String
		Get
			localHost = oOrbLPnts(0).sOAHost
		End Get
	End Property
	
	
	Public Property OnewayRebind() As Boolean
		Get
			OnewayRebind = bOnewayRebind
		End Get
		Set(ByVal Value As Boolean)
			bOnewayRebind = Value
		End Set
	End Property
	
	'Time out value in units of milli seconds
	'(Multiply with 10000 to get the timeout value in units of 100 nanoseconds)
	
	'Time out value in units of milli seconds
	'RelativeRoundtripTimeout = ::TimeBase::TimeT / 10000
	Public Property RelativeRoundtripTimeout() As Integer
		Get
			RelativeRoundtripTimeout = lTimeOutReplyEnd
		End Get
		Set(ByVal Value As Integer)
			lTimeOutReplyEnd = Value
		End Set
	End Property
	
	'Time out value in units of milli seconds
	'(Multiply with 10000 to get the timeout value in units of 100 nanoseconds)
	
	'Time out value in units of milli seconds
	'TimeOut of unused outgoing connections
	Public Property RelativeConnReuseTimeout() As Integer
		Get
			RelativeConnReuseTimeout = lTimeOutConnReuse
		End Get
		Set(ByVal Value As Integer)
			lTimeOutConnReuse = Value
		End Set
	End Property
	
	Public ReadOnly Property VisiWorkaround() As Boolean
		Get
			VisiWorkaround = bVisiWorkaround
		End Get
	End Property
	
	'ORB Initialization (is called by cVBOrb.init via mVBOrb.init, see there)
	'Note: Mosts applications need only one global instance of cOrbImpl.
	Public Sub init(ByVal ORBId As String, ByVal OAHost As String, ByVal OAPort As String, ByVal OAVersion As Short, ByVal ORBDefaultInitRef As String, ByVal ORBInitRef As String, ByVal LogFile As String, ByVal LogLevel As Short, ByVal VisiWorkaround As Boolean)
		On Error GoTo ErrHandler
		sOrbId = ORBId
		lThreadsInUse = 0
		'Setting socket connection time out values in units of milli seconds
		lTimeOutConnReuse = 20000
		lTimeOutServUnused = 600000
		lTimeOutReplyEnd = 7200000
		bOnewayRebind = True
		'Setting log file name
		sLogFile = LogFile
		iLogLevel = IIf(Len(sLogFile) = 0, 0, LogLevel)
		bVisiWorkaround = VisiWorkaround
		'Init connection arrays
		lOrbLPntCnt = 1
		ReDim oOrbLPnts(lOrbLPntCnt - 1)
		lOrbConnCnt = 2
		ReDim oOrbConns(lOrbConnCnt - 1)
		ReDim lSelFDs((lOrbLPntCnt + lOrbConnCnt) * 3 + 5)
		'Create and bind socket for listener
		oOrbLPnts(0).oSock = New cOrbSocket
		If OAHost = "" Then
			'Get real name of localhost
			OAHost = oOrbLPnts(0).oSock.getHostName(True)
		End If
		'Bind the server socket immediately to retrieve the actual port
		On Error Resume Next
		Call oOrbLPnts(0).oSock.initBind(OAHost, OAPort)
		If Err.Number <> 0 Then
			Call mVBOrb.ErrSave()
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
		Dim lNextPos As Integer
		Dim sInitRef As String
		Do While ORBInitRef <> ""
			lNextPos = InStr(1, ORBInitRef, " ")
			If lNextPos <= 0 Then
				sInitRef = ORBInitRef
				ORBInitRef = ""
			Else
				sInitRef = Mid(ORBInitRef, 1, lNextPos - 1)
				ORBInitRef = Mid(ORBInitRef, lNextPos + 1)
			End If
			lNextPos = InStr(1, sInitRef, "=")
			If lNextPos <= 0 Then
				Call mVBOrb.VBOrb.raiseBADPARAM(99, mVBOrb.VBOrb.CompletedNO, "Missing equal sign in ORBInitRef parameter")
			End If
			Call registerInitRefStr(Mid(sInitRef, 1, lNextPos - 1), Mid(sInitRef, lNextPos + 1))
		Loop 
		iOrbState = ORBST_ISINIT
		Exit Sub
InitRollback: 
		On Error Resume Next
		'UPGRADE_NOTE: Object oOrbLPnts().oSock may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
		oOrbLPnts(0).oSock = Nothing
		On Error GoTo 0
		Call mVBOrb.ErrLoad()
		Call mVBOrb.ErrReraise("Orb.init")
ErrHandler: 
		Call mVBOrb.ErrSave()
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
	Public Function getLogLevel() As Short
		getLogLevel = iLogLevel
	End Function
	
	'Write an exception onto the ORB log and delete the exception
	Public Sub logException(ByVal oEx As _cOrbException)
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
			Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO, "InitRefId is empty")
		End If
		If ref = "" Then
			Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO, "InitRef is empty")
		End If
		If colInitRefObjs Is Nothing Then
			colInitRefObjs = New Collection
			colInitRefIds = New Collection
		ElseIf Not lookupInitialReference(id) Is Nothing Then 
			Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO, "Id " & id & " already registered")
		End If
		oRefStr = New cCBStringValue
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
	Public Sub registerInitialReference(ByVal id As String, ByVal Obj As _cOrbObject)
		On Error GoTo ErrHandler
		Dim oInvalidNameEx As _cOrbException
		If id = "" Then
			oInvalidNameEx = New cOrbInvalidName
			Call oInvalidNameEx.addInfos(PostDescr:="Id is empty")
			Call mVBOrb.raiseUserEx(oInvalidNameEx)
		End If
		If Obj Is Nothing Then
			Call mVBOrb.VBOrb.raiseBADPARAM(24, mVBOrb.VBOrb.CompletedNO, "Obj parameter is null")
		End If
		If colInitRefObjs Is Nothing Then
			colInitRefObjs = New Collection
			colInitRefIds = New Collection
		ElseIf Not lookupInitialReference(id) Is Nothing Then 
			oInvalidNameEx = New cOrbInvalidName
			Call oInvalidNameEx.addInfos(PostDescr:="Id " & id & " already registered")
			Call mVBOrb.raiseUserEx(oInvalidNameEx)
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
		oRef = colInitRefObjs.Item(id)
		'UPGRADE_NOTE: Object oRef may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
		If Err.Number <> 0 Then oRef = Nothing
		On Error GoTo 0
		lookupInitialReference = oRef
	End Function
	
	'list_initial_services()
	'Obtaining Initial Object References
	'(First version was written by Craig Neuwirt)
	Public Function listInitialServices() As c_StringSeq
		Dim oList As c_StringSeq
		oList = New c_StringSeq
		Dim sId As Object
		Dim lCnt As Integer 'Variant is required by 'For Each' statement
		If Not colInitRefObjs Is Nothing Then
			oList.Length = colInitRefObjs.Count()
			lCnt = 0
			For	Each sId In colInitRefIds
				'UPGRADE_WARNING: Couldn't resolve default property of object sId. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6A50421D-15FE-4896-8A1B-2EC21E9037B2"'
				oList.Item(lCnt) = sId
				lCnt = lCnt + 1
			Next sId
		End If
		listInitialServices = oList
	End Function
	
	'Obtaining Initial Object References
	'RootPOA            PortableServer::POA
	'POACurrent
	'(First version was written by Craig Neuwirt)
	Public Function resolveInitialReferences(ByVal id As String) As _cOrbObject
		On Error GoTo ErrHandler
		Dim oRef As Object
		Dim oRefObj As _cOrbObject
		oRef = lookupInitialReference(id)
		'UPGRADE_WARNING: TypeName has a new behavior. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="9B7D5ADD-D8FE-4819-A36C-6DEDAF088CC7"'
		Dim sRefStr As String
		Dim oRefStr As cCBStringValue
		If TypeName(oRef) = "cOrbObject" Then
			oRefObj = oRef
		Else
			If oRef Is Nothing Then
				sRefStr = sDefaultInitRef & "/" & id
			Else
				oRefStr = oRef
				sRefStr = oRefStr.Value
			End If
			Select Case id
				Case "NameService"
					oRefObj = str2NmCtxRef(sRefStr)
				Case Else
					oRefObj = stringToObject(sRefStr)
			End Select
		End If
		resolveInitialReferences = oRefObj
		Exit Function
ErrHandler: 
		Call mVBOrb.ErrReraise("Orb.resolveInitialReferences")
	End Function
	
	'Get the object reference of the naming context
	Private Function str2NmCtxRef(ByVal sRefStr As String) As cOrbObjRef
		On Error GoTo ErrHandler
		Dim oNmCtxRef As cOrbObjRef
		oNmCtxRef = stringToObject(sRefStr)
		'ObjRef should be a reference to a NamingContext
		On Error Resume Next
		Call oNmCtxRef.narrow("IDL:omg.org/CosNaming/NamingContext:1.0", True, False)
		If Err.Number <> 0 Then
			Call mVBOrb.ErrSave()
			'JDK 1.3.1 ORB gives OBJECT_NOT_EXIST
			If mVBOrb.ErrIsSystemEx() And Err.Number = (mVBOrb.ITF_E_OBJECT_NOT_EXIST_NO Or vbObjectError) And InStr(sRefStr, "corbaloc:") = 1 And Right(sRefStr, 12) = "/NameService" Then
				oNmCtxRef = urlCorbaloc(Mid(sRefStr, 10, Len(sRefStr) - 20) & "INIT")
				If Err.Number <> 0 Then GoTo ErrLoadRaise
				Call oNmCtxRef.setRebindMode(1)
				oNmCtxRef = sunInitGetNameService(oNmCtxRef)
				If Err.Number <> 0 Then GoTo ErrLoadRaise
				Call oNmCtxRef.narrow("IDL:omg.org/CosNaming/NamingContext:1.0", True, False)
				If Err.Number <> 0 Then GoTo ErrLoadRaise
			Else
				GoTo ErrLoadRaise 'Load and raise error
			End If
			Call mVBOrb.ErrLoad() 'Load and ignore error
		End If
		On Error GoTo ErrHandler
		str2NmCtxRef = oNmCtxRef
		Exit Function
ErrHandler: 
		Call mVBOrb.ErrSave()
		Resume ErrLoadRaise
ErrLoadRaise: 
		On Error GoTo 0
		Call mVBOrb.ErrLoad()
		Call mVBOrb.ErrReraise("str2NmCtxRef")
	End Function
	
	'Create a primitive TypeCode
	Public Function createPrimitiveTc(ByVal kind As Integer) As cOrbTypeCode
		On Error GoTo ErrHandler
		Dim oTC As cOrbTypeCode
		Dim sChId As String
		sChId = "T#" & CStr(kind)
		oTC = getCachedTc(sChId, kind)
		If oTC Is Nothing Then
			oTC = New cOrbTypeCode
			Call oTC.init2PrimitiveTc(sChId, kind)
			Call colCachedTCs.Add(oTC, sChId)
		End If
		createPrimitiveTc = oTC
		Exit Function
ErrHandler: 
		Call mVBOrb.ErrReraise("createPrimitiveTc")
	End Function
	
	'create_struct_tc()
	Public Function createStructTc(ByVal id As String, ByVal name As String, ByVal members As cCBStructMemberSeq) As cOrbTypeCode
		On Error GoTo ErrHandler
		Dim oTC As cOrbTypeCode
		oTC = createRecursiveTc(id)
		Call oTC.setRecTc2StructTc(name, members)
		createStructTc = oTC
		Exit Function
ErrHandler: 
		Call mVBOrb.ErrReraise("createStructTc")
	End Function
	
	'create_union_tc()
	Public Function createUnionTc(ByVal id As String, ByVal name As String, ByVal discriminator_type As cOrbTypeCode, ByVal members As cCBUnionMemberSeq) As cOrbTypeCode
		On Error GoTo ErrHandler
		Dim oTC As cOrbTypeCode
		oTC = createRecursiveTc(id)
		Call oTC.setRecTc2UnionTc(name, discriminator_type, members)
		createUnionTc = oTC
		Exit Function
ErrHandler: 
		'UPGRADE_NOTE: Object createUnionTc may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
		createUnionTc = Nothing
		Call mVBOrb.ErrReraise("createUnionTc")
	End Function
	
	'create_enum_tc()
	Public Function createEnumTc(ByVal id As String, ByVal name As String, ByVal members As c_StringSeq) As cOrbTypeCode
		On Error GoTo ErrHandler
		Dim oTC As cOrbTypeCode
		oTC = createRecursiveTc(id)
		Call oTC.setRecTc2EnumTc(name, members)
		createEnumTc = oTC
		Exit Function
ErrHandler: 
		Call mVBOrb.ErrReraise("createEnumTc")
	End Function
	
	'create_alias_tc()
	Public Function createAliasTc(ByVal id As String, ByVal name As String, ByVal original_type As cOrbTypeCode) As cOrbTypeCode
		On Error GoTo ErrHandler
		Dim oTC As cOrbTypeCode
		oTC = createRecursiveTc(id)
		Call oTC.setRecTc2AliasTc(name, original_type)
		createAliasTc = oTC
		Exit Function
ErrHandler: 
		Call mVBOrb.ErrReraise("createAliasTc")
	End Function
	
	'create_exception_tc()
	Public Function createExceptionTc(ByVal id As String, ByVal name As String, ByVal members As cCBStructMemberSeq) As cOrbTypeCode
		On Error GoTo ErrHandler
		Dim oTC As cOrbTypeCode
		oTC = createRecursiveTc(id)
		Call oTC.setRecTc2ExceptionTc(name, members)
		createExceptionTc = oTC
		Exit Function
ErrHandler: 
		Call mVBOrb.ErrReraise("createExceptionTc")
	End Function
	
	'create_interface_tc()
	Public Function createInterfaceTc(ByVal id As String, ByVal name As String) As cOrbTypeCode
		On Error GoTo ErrHandler
		Dim oTC As cOrbTypeCode
		oTC = createRecursiveTc(id)
		Call oTC.setRecTc2InterfaceTc(name)
		createInterfaceTc = oTC
		Exit Function
ErrHandler: 
		Call mVBOrb.ErrReraise("createInterfaceTc")
	End Function
	
	'create_string_tc()
	Public Function createStringTc(ByVal bound As Integer) As cOrbTypeCode
		On Error GoTo ErrHandler
		Dim oTC As cOrbTypeCode
		Dim sChId As String
		sChId = "string"
		If bound <> 0 Then sChId = sChId & "<" & CStr(bound) & ">"
		oTC = getCachedTc(sChId, mCB.tk_string)
		If oTC Is Nothing Then
			oTC = New cOrbTypeCode
			Call oTC.init2stringTc(sChId, bound)
			Call colCachedTCs.Add(oTC, sChId)
		End If
		createStringTc = oTC
		Exit Function
ErrHandler: 
		Call mVBOrb.ErrReraise("createStringTc")
	End Function
	
	'create_wstring_tc()
	Public Function createWstringTc(ByVal bound As Integer) As cOrbTypeCode
		On Error GoTo ErrHandler
		Dim oTC As cOrbTypeCode
		Dim sChId As String
		sChId = "wstring"
		If bound <> 0 Then sChId = sChId & "<" & CStr(bound) & ">"
		oTC = getCachedTc(sChId, mCB.tk_wstring)
		If oTC Is Nothing Then
			oTC = New cOrbTypeCode
			Call oTC.init2WstringTC(sChId, bound)
			Call colCachedTCs.Add(oTC, sChId)
		End If
		createWstringTc = oTC
		Exit Function
ErrHandler: 
		Call mVBOrb.ErrReraise("createWstringTc")
	End Function
	
	'create_fixed_tc()
	Public Function createFixedTc(ByVal digits As Short, ByVal scale_ As Short) As cOrbTypeCode
		On Error GoTo ErrHandler
		Dim oTC As cOrbTypeCode
		Dim sChId As String
		sChId = "fixed<" & CStr(digits) & "," & CStr(scale_) & ">"
		oTC = getCachedTc(sChId, mCB.tk_fixed)
		If oTC Is Nothing Then
			oTC = New cOrbTypeCode
			Call oTC.init2FixedTc(sChId, digits, scale_)
			Call colCachedTCs.Add(oTC, sChId)
		End If
		createFixedTc = oTC
		Exit Function
ErrHandler: 
		Call mVBOrb.ErrReraise("createFixedTc")
	End Function
	
	'create_sequence_tc()
	Public Function createSequenceTc(ByVal bound As Integer, ByVal element_type As cOrbTypeCode) As cOrbTypeCode
		On Error GoTo ErrHandler
		Dim oTC As cOrbTypeCode
		Dim sChId As String
		sChId = "sequence<" & element_type.getChId()
		If bound <> 0 Then sChId = sChId & "," & CStr(bound)
		sChId = sChId & ">"
		oTC = getCachedTc(sChId, mCB.tk_sequence)
		If oTC Is Nothing Then
			oTC = New cOrbTypeCode
			Call oTC.init2SequenceTc(sChId, bound, element_type)
			Call colCachedTCs.Add(oTC, sChId)
		End If
		createSequenceTc = oTC
		Exit Function
ErrHandler: 
		Call mVBOrb.ErrReraise("createSequenceTc")
	End Function
	
	'create_recursive_sequence_tc()
	Public Function createRecursiveSequenceTc(ByVal bound As Integer, ByVal offset As Integer) As cOrbTypeCode
		On Error GoTo ErrHandler
		Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
		Exit Function
ErrHandler: 
		'UPGRADE_NOTE: Object createRecursiveSequenceTc may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
		createRecursiveSequenceTc = Nothing
		Call mVBOrb.ErrReraise("createRecursiveSequenceTc")
	End Function
	
	'create_array_tc()
	Public Function createArrayTc(ByVal Length As Integer, ByVal element_type As cOrbTypeCode) As cOrbTypeCode
		On Error GoTo ErrHandler
		Dim oTC As cOrbTypeCode
		Dim sChId As String
		sChId = element_type.getChId() & "[" & CStr(Length) & "]"
		oTC = getCachedTc(sChId, mCB.tk_array)
		If oTC Is Nothing Then
			oTC = New cOrbTypeCode
			Call oTC.init2ArrayTc(sChId, Length, element_type)
			Call colCachedTCs.Add(oTC, sChId)
		End If
		createArrayTc = oTC
		Exit Function
ErrHandler: 
		Call mVBOrb.ErrReraise("createArrayTc")
	End Function
	
	'create_value_tc()
	Public Function createValueTc(ByVal id As String, ByVal name As String, ByVal type_modifier As Short, ByVal concrete_base As cOrbTypeCode, ByVal members As cCBValueMemberSeq) As cOrbTypeCode
		On Error GoTo ErrHandler
		Dim oTC As cOrbTypeCode
		oTC = createRecursiveTc(id)
		Call oTC.setRecTc2ValueTc(name, type_modifier, concrete_base, members)
		createValueTc = oTC
		Exit Function
ErrHandler: 
		Call mVBOrb.ErrReraise("createValueTc")
	End Function
	
	'create_value_box_tc()
	Public Function createValueBoxTc(ByVal id As String, ByVal name As String, ByVal boxed_type As cOrbTypeCode) As cOrbTypeCode
		On Error GoTo ErrHandler
		Dim oTC As cOrbTypeCode
		oTC = createRecursiveTc(id)
		Call oTC.setRecTc2ValueBoxTc(name, boxed_type)
		createValueBoxTc = oTC
		Exit Function
ErrHandler: 
		Call mVBOrb.ErrReraise("createValueBoxTc")
	End Function
	
	'create_native_tc()
	Public Function createNativeTc(ByVal id As String, ByVal name As String) As cOrbTypeCode
		On Error GoTo ErrHandler
		Dim oTC As cOrbTypeCode
		oTC = createRecursiveTc(id)
		Call oTC.setRecTc2NativeTc(name)
		createNativeTc = oTC
		Exit Function
ErrHandler: 
		Call mVBOrb.ErrReraise("createNativeTc")
	End Function
	
	'Get previously created TypeCode or Nothing if not exists
	Public Function getCachedTc(ByVal ChId As String, ByVal kind As Integer) As cOrbTypeCode
		On Error GoTo ErrHandler
		Dim oTC As cOrbTypeCode
		If colCachedTCs Is Nothing Then
			colCachedTCs = New Collection
			'UPGRADE_NOTE: Object oTC may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
			oTC = Nothing
		Else
			On Error Resume Next
			oTC = colCachedTCs.Item(ChId)
			'UPGRADE_NOTE: Object oTC may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
			If Err.Number <> 0 Then oTC = Nothing
			On Error GoTo ErrHandler
			If Not oTC Is Nothing Then
				If Not oTC.isCompatible(kind) Then
					Call colCachedTCs.Remove(ChId)
					Call oTC.destroy()
					'UPGRADE_NOTE: Object oTC may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
					oTC = Nothing
				End If
			End If
		End If
		getCachedTc = oTC
		Exit Function
ErrHandler: 
		Call mVBOrb.ErrReraise("getCachedTc")
	End Function
	
	'Get previously created recursive TypeCode or Nothing if not exists
	Public Function getRecursiveTc(ByVal id As String, ByVal kind As Integer) As cOrbTypeCode
		On Error GoTo ErrHandler
		getRecursiveTc = getCachedTc(id, kind)
		Exit Function
ErrHandler: 
		Call mVBOrb.ErrReraise("getRecursiveTc")
	End Function
	
	'create_recursive_tc()
	Public Function createRecursiveTc(ByVal id As String) As cOrbTypeCode
		On Error GoTo ErrHandler
		Dim oTC As cOrbTypeCode
		oTC = getRecursiveTc(id, -1)
		If oTC Is Nothing Then
			oTC = New cOrbTypeCode
			Call oTC.init2RecursiveTc(id)
			Call colCachedTCs.Add(oTC, id)
		End If
		createRecursiveTc = oTC
		Exit Function
ErrHandler: 
		Call mVBOrb.ErrReraise("createRecursiveTc")
	End Function
	
	'create_abstract_interface_tc()
	Public Function createAbstractInterfaceTc(ByVal id As String, ByVal name As String) As cOrbTypeCode
		On Error GoTo ErrHandler
		Dim oTC As cOrbTypeCode
		oTC = createRecursiveTc(id)
		Call oTC.setRecTc2AbstractInterfaceTc(name)
		createAbstractInterfaceTc = oTC
		Exit Function
ErrHandler: 
		Call mVBOrb.ErrReraise("createAbstractInterfaceTc")
	End Function
	
	'create_local_interface_tc()
	Public Function createLocalInterfaceTc(ByVal id As String, ByVal name As String) As cOrbTypeCode
		On Error GoTo ErrHandler
		Dim oTC As cOrbTypeCode
		oTC = createRecursiveTc(id)
		Call oTC.setRecTc2LocalInterfaceTc(name)
		createLocalInterfaceTc = oTC
		Exit Function
ErrHandler: 
		Call mVBOrb.ErrReraise("createLocalInterfaceTc")
	End Function
	
	'create_component_tc()
	Public Function createComponentTc(ByVal id As String, ByVal name As String) As cOrbTypeCode
		On Error GoTo ErrHandler
		Dim oTC As cOrbTypeCode
		'???Set oTC = createRecursiveTc(id)
		'???Call oTC.setRecTc2ComponentTc(name)
		createComponentTc = oTC
		Exit Function
ErrHandler: 
		Call mVBOrb.ErrReraise("createComponentTc")
	End Function
	
	'create_home_tc()
	Public Function createHomeTc(ByVal id As String, ByVal name As String) As cOrbTypeCode
		On Error GoTo ErrHandler
		Dim oTC As cOrbTypeCode
		'???Set oTC = createRecursiveTc(id)
		'???Call oTC.setRecTc2HomeTc(name)
		createHomeTc = oTC
		Exit Function
ErrHandler: 
		Call mVBOrb.ErrReraise("createHomeTc")
	End Function
	
	'create_event_tc()
	Public Function createEventTc(ByVal id As String, ByVal name As String, ByVal type_modifier As Short, ByVal concrete_base As cOrbTypeCode, ByVal members As cCBValueMemberSeq) As cOrbTypeCode
		On Error GoTo ErrHandler
		Dim oTC As cOrbTypeCode
		'???Set oTC = createRecursiveTc(id)
		'???Call oTC.setRecTc2EventTc(name, type_modifier, concrete_base, members)
		createEventTc = oTC
		Exit Function
ErrHandler: 
		Call mVBOrb.ErrReraise("createEventTc")
	End Function
	
	'Connect (activate) a servant object to the ORB (root POA)
	Public Sub connect(ByVal newImpl As cOrbSkeleton, Optional ByVal sKey As String = "")
		On Error GoTo ErrHandler
		If iOrbState = ORBST_NOTEXISTS Then
			Call mVBOrb.VBOrb.raiseOBJECTNOTEXIST(1, mVBOrb.VBOrb.CompletedNO)
		End If
		Dim objKey() As Byte
		If sKey = "" Then
			sKey = collImpls.Count() & "_" & mVBOrb.dllGetTickCount()
		Else
			Call mVBOrb.string2ObjKey(sKey, objKey)
			sKey = mVBOrb.objKey2String(objKey)
		End If
		Dim oObjRef As cOrbObjRef
		oObjRef = New cOrbObjRef
		'"1.1@host:portno/key"
		Call oObjRef.initByURL(Me, ":" & mVBOrb.GIOPVersion2Str(oOrbLPnts(0).iGIOPVersion) & "@" & oOrbLPnts(0).sOAHost & ":" & oOrbLPnts(0).sOAPort & "/" & sKey, oOrbLPnts(0).sOAPort, newImpl.TypeId(0), (mVBOrb.ONCSC), (mVBOrb.ONCSW))
		newImpl.ObjRef = oObjRef
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
		oObjRef = oldImpl.ObjRef
		If oObjRef Is Nothing Then
			Call mVBOrb.VBOrb.raiseINVOBJREF(1, mVBOrb.VBOrb.CompletedNO)
		End If
		sKey = oObjRef.objectKey
		On Error Resume Next
		oImpl = collImpls.Item(sKey)
		'UPGRADE_NOTE: Object oImpl may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
		If Err.Number <> 0 Then oImpl = Nothing
		On Error GoTo ErrHandler
		If oImpl Is Nothing Then
			Call mVBOrb.VBOrb.raiseINVOBJREF(1, mVBOrb.VBOrb.CompletedNO)
		End If
		Call collImpls.Remove(sKey)
		Call oObjRef.releaseMe()
		'UPGRADE_NOTE: Object oldImpl.ObjRef may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
		oldImpl.ObjRef = Nothing
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
			System.Windows.Forms.Application.DoEvents() 'Prevent blocking other window processes
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
	Public Sub performWork(Optional ByVal lWaitTime As Integer = 0)
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
			Call oOrbLPnts(0).oSock.startListen()
			If iLogLevel >= 4 Then
				Call mVBOrb.logMsg("I ORB is listening on port " & oOrbLPnts(0).sOAPort & " at " & oOrbLPnts(0).sOAHost)
			End If
			iOrbState = ORBST_ISACTIVE
		ElseIf iOrbState = ORBST_SHUTDOWN Then 
			'Reduce timeout
			lTimeOutServUnused = 1000
			lTimeOutConnReuse = 15000
		End If
		
		Dim lLPnt As Integer
		Dim slotCnt As Integer
		Dim lFDCnt As Integer
		Dim lOpenCnt As Integer
		lOpenCnt = 0
		'Collect all file descriptors of open connections
		Dim lSelFDCallId As Integer
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
		If dllSelect(lOrbLPntCnt + lOrbConnCnt, lSelFDs(0), lSelFDs(lOrbLPntCnt + lOrbConnCnt + 1), lSelFDs((lOrbLPntCnt + lOrbConnCnt) * 2 + 2), timeOut) = -1 Then
			Call mVBOrb.VBOrb.raiseINTERNAL(1, mVBOrb.VBOrb.CompletedMAYBE, "select() failed, " & CStr(Err.LastDllError))
		End If
		
		'Check all incoming sockets
		Dim lTime As Integer
		lTime = mVBOrb.dllGetTickCount() 'or mvborb.getTime()
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
						If iOrbState = ORBST_SHUTDOWN And oOrbConns(slotCnt).oReqsOI Is Nothing Then
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
						Call mVBOrb.logErr(sFuncName) 'Is calling Err.Clear()
						'Close connection to get a new connection next time
						Call ConnSendCloseAndClose(slotCnt, 1090)
					ElseIf Not oOrbConns(slotCnt).oSock Is Nothing Then 
						lOpenCnt = lOpenCnt + 1
					End If
				End If
			End If
			If Err.Number <> 0 Then
				Call mVBOrb.logErr(sFuncName) 'Is calling Err.Clear()
			End If
		Next slotCnt
		On Error GoTo ErrHandler 'Switch off "Resume Next" mode
		'Check listener sockets
		Dim lFreeSlot As Integer
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
						Call ConnIncCnt()
					End If
					'Accept
					oOrbConns(lFreeSlot).lOCIdCnt = 0
					oOrbConns(lFreeSlot).bSameProcess = False
					oOrbConns(lFreeSlot).oSock = New cOrbSocket
					'UPGRADE_NOTE: Object oOrbConns().oReqsOI may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
					oOrbConns(lFreeSlot).oReqsOI = Nothing
					oOrbConns(lFreeSlot).lMsgTime = lTime
					oOrbConns(lFreeSlot).iGIOPVersion = 0
					Call oOrbConns(lFreeSlot).oSock.initAccept(oOrbLPnts(lLPnt).oSock)
					oOrbConns(lFreeSlot).lListenPointCnt = -1
					If iLogLevel >= ORBLL_INFO Then
						Call mVBOrb.logMsg("I c" & CStr(lFreeSlot + 1) & " Accept new ORB connection from " & oOrbConns(lFreeSlot).oSock.socketHost & ":" & oOrbConns(lFreeSlot).oSock.socketPort)
					End If
				End If
			End If
		Next lLPnt
		'Increment timeout check counter
		iTOCnt = iTOCnt + 1
		If iTOCnt > 10 Then iTOCnt = 0
		'Do rest of shutdown
RestOfShutdown: 
		Dim oImpl As cOrbSkeleton
		Dim lCnt As Integer
		Dim oTC As cOrbTypeCode
		If lOpenCnt <= 0 And iOrbState = ORBST_SHUTDOWN Then
			'Disconnect all objects
			For	Each oImpl In collImpls
				If Not oImpl.ObjRef Is Nothing Then
					Call oImpl.ObjRef.releaseMe()
				End If
				'UPGRADE_NOTE: Object oImpl.ObjRef may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
				oImpl.ObjRef = Nothing
			Next oImpl
			For lCnt = 1 To collImpls.Count()
				Call collImpls.Remove(1)
			Next lCnt
			'Shutdown internal services if exists
			'Shutdown recursive TypeCode cache
			If Not colCachedTCs Is Nothing Then
				For	Each oTC In colCachedTCs
					Call oTC.destroy()
				Next oTC
				'UPGRADE_NOTE: Object colCachedTCs may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
				colCachedTCs = Nothing
			End If
			'Shutdown completed
			Call mVBOrb.logMsg("ORB shutdown completed")
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
	Friend Function readReqObjKey(ByVal oIn As cOrbStream, ByRef sKey As String) As cOrbSkeleton
		On Error GoTo ErrHandler
		Dim baObjKey() As Byte
		Dim addrDisposition As Short
		Dim oObjRef As cOrbObjRef
		Dim lSelectedProfile As Integer 'KeyAddr, sequence <octet> object_key;
		If oIn.getGIOPVersion <> &H102 Then
			'sequence <octet> object_key;
			Call oIn.readSeqOctet(baObjKey)
			sKey = mVBOrb.objKey2String(baObjKey)
		Else
			'TargetAddress target;
			addrDisposition = oIn.readShort()
			Select Case addrDisposition
				Case 0
					Call oIn.readSeqOctet(baObjKey)
					sKey = mVBOrb.objKey2String(baObjKey)
				Case 1 'ProfileAddr, IOP::TaggedProfile profile;
					oObjRef = New cOrbObjRef
					Call oObjRef.initByIOR(Me, oIn, True)
					sKey = oObjRef.objectKey
				Case 2 'ReferenceAddr, unsigned long selected_profile_index; IOP::IOR ior;
					lSelectedProfile = oIn.readUlong() 'Starting at 0 or 1???
					oObjRef = New cOrbObjRef
					Call oObjRef.initByIOR(Me, oIn)
					Call oObjRef.selectIIOPProfile(lSelectedProfile)
					sKey = oObjRef.objectKey
				Case Else
					Call mVBOrb.VBOrb.raiseMARSHAL(1, mVBOrb.VBOrb.CompletedNO, "Unknown AddressingDisposition: " & CStr(addrDisposition))
			End Select
		End If
		On Error Resume Next
		readReqObjKey = collImpls.Item(sKey)
		'UPGRADE_NOTE: Object readReqObjKey may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
		If Err.Number <> 0 Then readReqObjKey = Nothing
		On Error GoTo ErrHandler
		Exit Function
ErrHandler: 
		Call mVBOrb.ErrReraise("readReqObjKey")
	End Function
	
	'Execute a Client Locate Request (First version was written by Holger Beer)
	Friend Sub replyLocateRequest(ByVal oIn As cOrbStream, ByRef oOut As cOrbStream)
		On Error GoTo ErrHandler
		
		'Read GIOP LocateRequest Header
		Dim ReqId As Integer
		'unsigned long request_id
		ReqId = oIn.readUlong()
		'sequence <octet> object_key; or TargetAddress target;
		Dim sKey As String
		Dim oImpl As cOrbSkeleton
		oImpl = readReqObjKey(oIn, sKey)
		
		Dim locStatus As Integer
		'0 = UNKNOWN_OBJECT, 1 = OBJECT_HERE, 2 = OBJECT_FORWARD,
		'3 = OBJECT_FORWARD_PERM, 4 = LOC_SYSTEM_EXCEPTION,
		'5 = LOC_NEEDS_ADDRESSING_MODE
		locStatus = IIf(oImpl Is Nothing, 0, 1)
		
		Call oOut.initStream(Me, oIn.getGIOPVersion)
		'Prepare GIOP Message Header
		Call oOut.writeGIOPHead()
		'Write GIOP LocateReply Header
		Call writeLocateReplyHeader(oOut, ReqId, locStatus)
		
		'Do not need writing GIOP LocateReply body because locStatus is 0 or 1
		'writeLocateReqstBody():
#If IIOP12a Then
		'UPGRADE_NOTE: #If #EndIf block was not upgraded because the expression IIOP12a did not evaluate to True or was not evaluated. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="27EE2C3C-05AF-4C04-B2AF-657B4FB6B5FC"'
		'Locate Reply Body alignment was erroneous added in CORBA 2.4/2.5
		'See also cOrbObjRef.readLocateReplyHeader()
		If oOut.getGIOPVersion = &H102 Then Call oOut.writeAlign(8)
#End If
		Exit Sub
ErrHandler: 
		Call mVBOrb.ErrReraise("replyLocateRequest")
	End Sub
	
	'(First version was written by Holger Beer)
	Private Sub writeLocateReplyHeader(ByVal oOut As cOrbStream, ByVal ReqId As Integer, ByVal locStatus As Integer)
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
		Dim lLPnt As Integer
		If iOrbState = ORBST_ISACTIVE Then
			'Deactivate Object Adapters
			For lLPnt = 0 To lOrbLPntCnt - 1
				If Not oOrbLPnts(lLPnt).oSock Is Nothing Then
					Call oOrbLPnts(lLPnt).oSock.termBind()
					'UPGRADE_NOTE: Object oOrbLPnts().oSock may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
					oOrbLPnts(lLPnt).oSock = Nothing
				End If
			Next lLPnt
			iOrbState = ORBST_SHUTDOWN 'Rest must be done by performWork() or run()
		ElseIf iOrbState = ORBST_ISINIT Then 
			'oOrbLPnts(0).oSock.isListen = False, isOpen = True
			If Not oOrbLPnts(0).oSock Is Nothing Then
				Call oOrbLPnts(0).oSock.termBind()
				'UPGRADE_NOTE: Object oOrbLPnts().oSock may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
				oOrbLPnts(0).oSock = Nothing
			End If
			iOrbState = ORBST_SHUTDOWN 'Rest must be done by performWork() or run()
		End If
		If sWait Then 'Block until the shutdown is completed?
			If lThreadsInUse > 0 Then 'Blocking would result in a deadlock
				'if shutdown is called by a worker thread.
				Call mVBOrb.VBOrb.raiseBADINVORDER(3, mVBOrb.VBOrb.CompletedNO)
			End If
			Call run()
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
		'UPGRADE_NOTE: Object colValFactories may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
		colValFactories = Nothing
		'UPGRADE_NOTE: Object colInitRefObjs may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
		colInitRefObjs = Nothing
		'UPGRADE_NOTE: Object colInitRefIds may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
		colInitRefIds = Nothing
		iOrbState = ORBST_NOTEXISTS
		Exit Sub
ErrHandler: 
		Call mVBOrb.ErrReraise("destroy")
	End Sub
	
	'Value factory operations
	'IN:    id      RepositoryId
	'IN:    factory ValueFactory
	'RET:   factory ???
	Public Function registerValueFactory(ByRef RepositoryId As String, ByVal ValueFactory As _cOrbValueFactory) As _cOrbValueFactory
		If colValFactories Is Nothing Then
			colValFactories = New Collection
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
	Public Function lookupValueFactory(ByRef RepositoryId As String) As _cOrbValueFactory
		Dim oValueFactory As _cOrbValueFactory
		On Error Resume Next
		oValueFactory = colValFactories.Item(RepositoryId)
		'UPGRADE_NOTE: Object oValueFactory may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
		If Err.Number <> 0 Then oValueFactory = Nothing
		On Error GoTo 0
		lookupValueFactory = oValueFactory
	End Function
End Class