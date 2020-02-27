VERSION 1.0 CLASS
BEGIN
  MultiUse = -1  'True
END
Attribute VB_Name = "cOrbRequest"
Attribute VB_GlobalNameSpace = False
Attribute VB_Creatable = True
Attribute VB_PredeclaredId = False
Attribute VB_Exposed = True
'Copyright (c) 1999 Martin.Both

'This library is free software; you can redistribute it and/or
'modify it under the terms of the GNU Library General Public
'License as published by the Free Software Foundation; either
'version 2 of the License, or (at your option) any later version.

'This library is distributed in the hope that it will be useful,
'but WITHOUT ANY WARRANTY; without even the implied warranty of
'MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
'Library General Public License for more details.

Option Explicit

'Set DebugMode = 0 to deactivate debug code in this class
#Const DebugMode = 0

#If DebugMode Then
    Private lClassDebugID As Long
#End If

'GIOP Request
Private iReqState As Integer
Const REQST_NOTEXISTS As Integer = 0 'Is not initialized?
Const REQST_INCOMING As Integer = 1
Const REQST_OUTGOING As Integer = 2

Private oOrb As cOrbImpl
Private iLogLevel As Integer

Private oObjImpl As cOrbSkeleton
Private sObjKey As String
Private oObjRef As cOrbObjRef
Private sOperation As String

Private lReqId As Long
Private bResponseExpected As Boolean
Private bResponseFlags As Byte

'Outgoing or incoming request stream
Private oReqsStream As cOrbStream
Private lReqstBodyPos As Long
'Incoming or outgoing reply stream
Private oReplStream As cOrbStream
Private iReplMsgType As Integer

'Use to handle more than one Request by the ORB
Public NextRequest As cOrbRequest

Private Sub Class_Initialize()
    #If DebugMode Then
        lClassDebugID = mVBOrb.getNextClassDebugID()
        Debug.Print "'" & TypeName(Me) & "' " & lClassDebugID & " initialized"
    #End If
End Sub

Private Sub Class_Terminate()
    'Release something which VB cannot know if required
    #If DebugMode Then
        Debug.Print "'" & TypeName(Me) & "' " & CStr(lClassDebugID) & " terminated"
    #End If
End Sub

#If DebugMode Then
    Friend Property Get ClassDebugID() As Long
        ClassDebugID = lClassDebugID
    End Property
#End If

'Initialization
'IN:    Orb
'IN:    ReqId               Request Id
'IN:    ReqsStream          GIOP Request Message, maybe incomplete
Public Function initInRequest(ByVal Orb As cOrbImpl, ByVal ReqId As Long, _
    ByVal ReqsStream As cOrbStream) As Boolean
    On Error GoTo ErrHandler
    If iReqState <> REQST_NOTEXISTS Then
        Call mVBOrb.VBOrb.raiseBADINVORDER(1, mVBOrb.VBOrb.CompletedNO)
    End If
    Set oOrb = Orb
    iLogLevel = oOrb.getLogLevel()
    'request_id;
    lReqId = ReqId
    iReqState = REQST_INCOMING
    Set oReqsStream = ReqsStream
    lReqstBodyPos = -1

    If oReqsStream.isComplete() Then
        Call readReqstHead(oReqsStream)
        initInRequest = True
    Else
        initInRequest = False
    End If
    Exit Function
ErrHandler:
    Set oOrb = Nothing
    Call mVBOrb.ErrReraise("initInRequest")
End Function

'Read rest of GIOP Request Header
Private Sub readReqstHead(ByVal oIn As cOrbStream)
    On Error GoTo ErrHandler
    'Read GIOP Request Header
    If oReqsStream.getGIOPVersion <> &H102 Then
        'IOP::ServiceContextList service_context;
        Call readReqstServiceContext(oReqsStream)
    End If
    'request_id;
    lReqId = oReqsStream.readUlong()
    'ResponseFlags: &H0 = oneway, &H1 = SyncScope.WITH_SERVER, &H3 = SyncScope.WITH_TARGET
    If oIn.getGIOPVersion <> &H102 Then
        'response_expected;
        bResponseFlags = IIf(oIn.readBoolean(), &H3, &H0)
    Else
        'response_flags;
        bResponseFlags = oIn.readOctet()
    End If
    bResponseExpected = bResponseFlags <> &H0
    
    If oIn.getGIOPVersion <> &H100 Then
        'octet reserved[3];
        Call oIn.readOctet
        Call oIn.readOctet
        Call oIn.readOctet
    End If
    'sequence <octet> object_key; or TargetAddress target;
    Set oObjImpl = oOrb.readReqObjKey(oIn, sObjKey)
    'operation;
    sOperation = oIn.readString()
    If oIn.getGIOPVersion <> &H102 Then
        'Principal (not in GIOP 1.2)
        Dim pcpLen As Long
        pcpLen = oIn.readUlong()
        Call oIn.readSkip(pcpLen)
    Else
        'IOP::ServiceContextList service_context;
        Call readReqstServiceContext(oIn)
        'In GIOP version 1.2, the Request Body is always aligned on an 8-octet
        'boundary. See also cOrbObjRef.writeReqstHeadFw()
        Call oIn.readAlign(8)
    End If
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("readReqstHead")
End Sub

'
Private Sub readReqstServiceContext(ByVal oIn As cOrbStream)
    On Error GoTo ErrHandler
    If (iLogLevel And &H20) <> 0 Then
        Call oOrb.logMsg("D readReqstServiceContext")
    End If
    Dim seqSC As Long
    Dim i1 As Long
    Dim lContextId As Long
    Dim lContextLen As Long
    'IOP::ServiceContextList service_context;
    seqSC = oIn.readUlong()
    For i1 = 1 To seqSC
        lContextId = oIn.readUlong()
        lContextLen = oIn.readUlong()
        Select Case lContextId
        Case 1 'Transmission code sets
            Call oIn.readEncapOpen(lContextLen)
            Dim lTCSC As Long
            Dim lTCSW As Long
            lTCSC = oIn.readUlong()
            lTCSW = oIn.readUlong()
            Call oIn.readEncapClose
            If (iLogLevel And &H20) <> 0 Then
                Call oOrb.logMsg("D TCS-C " & Hex(lTCSC) _
                    & " -> SNCS-C " & Hex(mVBOrb.ONCSC))
                Call oOrb.logMsg("D TCS-W " & Hex(lTCSW) _
                    & " -> SNCS-W " & Hex(mVBOrb.ONCSW))
            End If
        Case Else
            'Skipping unknown service context
            Call oIn.readSkip(lContextLen)
        End Select
    Next i1
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("readReqstServiceContext")
End Sub

'Execute a Client Request
Public Function replyRequest() As cOrbStream
    Const sFuncName As String = "replyRequest"
    On Error GoTo ErrHandler
    
    If iReqState = REQST_OUTGOING Then
        'Set oObjImpl and sObjKey
        Call readReqstHead(oReqsStream)
    End If
    'Prepare GIOP Message Header
    'Discard oOut later if oneway
    Dim oOut As cOrbStream
    Set oOut = New cOrbStream
    Call oOut.initStream(oOrb, oReqsStream.getGIOPVersion)
    Call oOut.writeGIOPHead

    'Write GIOP Reply Header
    Dim repStatusPos As Long
    repStatusPos = writeReplyHeader(oOut, ReqId, 0) '0 = NO_EXCEPTION

    'Call logMsg("Request operation: " & sOperation)
    'Execute request: oImpl.sOperation()
    'Attribute accessors have operation names as follows:
    '• Attribute selector: "_get_<attribute>"
    '• Attribute mutator: "_set_<attribute>"
    'CORBA::Object pseudo-operations have operation names as follows:
    '• InterfaceDef get_interface: "_interface"
    '• get_implementation: "_implementation"
    '• : "_get_domain_managers"
    Dim repStatus As Long
    If Not oObjImpl Is Nothing Then
        Select Case sOperation 'Name of the CORBA operation being invoked
        Case "_is_a" 'CORBA::Object pseudo-operation
            Dim sTypeId As String
            sTypeId = oReqsStream.readString()
            Dim i2 As Integer
            i2 = 0
            Do
                Select Case oObjImpl.TypeId(i2)
                Case sTypeId: Call oOut.writeBoolean(True): Exit Do
                Case "": Call oOut.writeBoolean(False): Exit Do
                Case Else: i2 = i2 + 1
                End Select
            Loop
            repStatus = 0 'NO_EXCEPTION
        Case "_non_existent" 'CORBA::Object pseudo-operation
            Call oOut.writeBoolean(False)
            repStatus = 0 'NO_EXCEPTION
        Case Else
            On Error Resume Next
            repStatus = oObjImpl.execute(sOperation, oReqsStream, oOut)
            If Err.Number <> 0 Then
                Call mVBOrb.ErrSave
                Call oOrb.logErr(sFuncName)
                GoTo ErrLoadSendSystemEx
            End If
            On Error GoTo ErrHandler
        End Select
    Else
        If sOperation = "_non_existent" Then 'CORBA::Object pseudo-operation
            Call oOut.writeBoolean(True)
            repStatus = 0 'NO_EXCEPTION
        Else
            On Error Resume Next
            Call mVBOrb.VBOrb.raiseOBJECTNOTEXIST(1, mVBOrb.VBOrb.CompletedNO, _
                "Object key '" & sObjKey & "' not found to execute '" & sOperation & "()'")
            Call mVBOrb.ErrSave
            Call oOrb.logErr(sFuncName)
            GoTo ErrLoadSendSystemEx
        End If
    End If
    'ResponseFlags: &H0 = oneway, &H1 = SyncScope.WITH_SERVER, &H3 = SyncScope.WITH_TARGET
    If ResponseFlags = &H3 Then 'SyncScope.WITH_TARGET
        'send UserException?
        If repStatus <> 0 Then
            Call oOut.setPos(repStatusPos)
            'ReplyStatusType reply_status;
            Call oOut.writeUlong(repStatus)
        End If
    ElseIf ResponseFlags = &H1 Then 'SyncScope.WITH_SERVER
        Call oOut.destroy
        'Init oOut again
        Call oOut.initStream(oOrb, oReqsStream.getGIOPVersion)
        Call oOut.writeGIOPHead
        'Write GIOP Reply Header
        Call writeReplyHeader(oOut, ReqId, 0) '0 = NO_EXCEPTION
    Else 'oneway
        Call oOut.destroy
        Set oOut = Nothing
    End If
    Set replyRequest = oOut
    Exit Function
ErrLoadSendSystemEx: 'after ResponseFlags
    On Error GoTo ErrHandler
    Call oOut.destroy
    If ResponseFlags = &H3 Then 'SyncScope.WITH_TARGET
        'Init oOut again
        Call oOut.initStream(oOrb, oReqsStream.getGIOPVersion)
        Call oOut.writeGIOPHead
        'Write GIOP Reply Header
        Call writeReplyHeader(oOut, ReqId, 2) '2 = SYSTEM_EXCEPTION
        Call mVBOrb.ErrLoad
        Call mVBOrb.ErrWriteSystemEx(oOut)
    ElseIf ResponseFlags = &H1 Then 'SyncScope.WITH_SERVER
        'Init oOut again
        Call oOut.initStream(oOrb, oReqsStream.getGIOPVersion)
        Call oOut.writeGIOPHead
        'Write GIOP Reply Header
        Call writeReplyHeader(oOut, ReqId, 0) '0 = NO_EXCEPTION
        Call mVBOrb.ErrLoad
        Call Err.Clear
    Else 'oneway
        Set oOut = Nothing
        Call mVBOrb.ErrLoad
        Call Err.Clear
    End If
    Set replyRequest = oOut
    Exit Function
ErrLoad1:
    'On Error GoTo ErrLog
    On Error GoTo 0
    Call mVBOrb.ErrLoad
ErrHandler:
    Call mVBOrb.ErrReraise(sFuncName)
ErrLog:
    Call oOrb.logErr(sFuncName)
    Resume Next
End Function

Private Function writeReplyHeader(ByVal oOut As cOrbStream, _
    ByVal ReqId As Long, ByVal repStatus As Long) As Long
    On Error GoTo ErrHandler
    If oOut.getGIOPVersion <> &H102 Then
        'IOP::ServiceContextList service_context;
        Call writeReplyServiceContext(oOut)
        'unsigned long request_id;
        Call oOut.writeUlong(ReqId)
        'ReplyStatusType reply_status;
        writeReplyHeader = oOut.getPos
        Call oOut.writeUlong(repStatus) '0 = NO_EXCEPTION
    Else
        'unsigned long request_id;
        Call oOut.writeUlong(ReqId)
        'ReplyStatusType reply_status;
        writeReplyHeader = oOut.getPos
        Call oOut.writeUlong(repStatus) '0 = NO_EXCEPTION
        'IOP::ServiceContextList service_context;
        Call writeReplyServiceContext(oOut)
        'In GIOP version 1.2, the Reply Body is always aligned on an 8-octet
        'boundary. See also cOrbObjRef.readReplyHeader()
        Call oOut.writeAlign(8)
    End If
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("writeReplyHeader")
End Function

'
Private Sub writeReplyServiceContext(ByVal oOut As cOrbStream)
    On Error GoTo ErrHandler
    'IOP::ServiceContextList service_context;
    Call oOut.writeUlong(0)
    '  ExceptionDetailMessage
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("writeReplyServiceContext")
End Sub

'Initialization
'IN:    Orb
'IN:    ObjRef              Target object
'IN:    Operation           Name of the operation
'IN:    ResponseExpected    Is not a oneway operation?
'IN:    ReqId               Request Id
'IN:    InArg               Or Nothing
Public Sub initOutRequest(ByVal Orb As cOrbImpl, ByVal ObjRef As cOrbObjRef, _
    ByRef Operation As String, ByVal ResponseExpected As Boolean, _
    ByVal ReqId As Long, ByVal InArg As cOrbStream)
    On Error GoTo ErrHandler
    If iReqState <> REQST_NOTEXISTS Then
        Call mVBOrb.VBOrb.raiseBADINVORDER(1, mVBOrb.VBOrb.CompletedNO)
    End If
    Set oOrb = Orb
    iLogLevel = oOrb.getLogLevel()
    sOperation = Operation
    bResponseExpected = ResponseExpected
    'ResponseFlags: &H0 = oneway, &H1 = SyncScope.WITH_SERVER, &H3 = SyncScope.WITH_TARGET
    bResponseFlags = IIf(bResponseExpected, &H3, &H0)
    lReqId = ReqId
    Set oObjRef = ObjRef
    lReqstBodyPos = -1
    Set oReqsStream = InArg
    iReqState = REQST_OUTGOING
    Exit Sub
ErrHandler:
    Set oObjRef = Nothing
    Call mVBOrb.ErrReraise("initOutRequest")
End Sub

'Is initialized?
Public Function isInitialized() As Boolean
    isInitialized = iReqState <> REQST_NOTEXISTS
End Function

'Is incoming?
Public Function isIncoming() As Boolean
    isIncoming = (iReqState = REQST_INCOMING)
End Function

'Is outgoing?
Public Function isOutgoing() As Boolean
    isOutgoing = (iReqState = REQST_OUTGOING)
End Function

'Close OrbRequest to reinit it next time
Public Sub delete()
    If iReqState <> REQST_NOTEXISTS Then
        If Not oReqsStream Is Nothing Then
            Call oReqsStream.destroy
            Set oReqsStream = Nothing
        End If
        If Not oReplStream Is Nothing Then
            Call oReplStream.destroy
            Set oReplStream = Nothing
        End If
        Call oObjRef.releaseMe
        Set oObjRef = Nothing
        Set oOrb = Nothing
        iReqState = REQST_NOTEXISTS
    End If
End Sub

Public Function getTarget() As cOrbObjRef
    Set getTarget = oObjRef
End Function

Public Property Get Operation() As String
    Operation = sOperation
End Property

Public Property Get ResponseExpected() As Boolean
    ResponseExpected = bResponseExpected 'ResponseFlags() <> &H0
End Property

'ResponseFlags: &H0 = oneway, &H1 = SyncScope.WITH_SERVER, &H3 = SyncScope.WITH_TARGET
Public Property Get ResponseFlags() As Byte
    ResponseFlags = bResponseFlags
End Property

Public Property Get ReqId() As Long
    ReqId = lReqId
End Property

'RET:           Get request stream to write or read the input arguments
Public Property Get InArg() As cOrbStream
    If iReqState = REQST_OUTGOING Then
        If lReqstBodyPos = -1 Then
            lReqstBodyPos = oReqsStream.getPos()
        End If
    End If
    Set InArg = oReqsStream
End Property

Public Property Get ReqstBodyPos() As Long
    ReqstBodyPos = lReqstBodyPos
End Property

'IN:    HasUserExceptions   Operation has UserExceptions?
'RET:                       UserException occured or LW required?
Public Function invokeReqst(ByVal HasUserExceptions As Boolean) As Boolean
    iReplMsgType = -1
    Set oReplStream = Nothing
    If Len(sOperation) = 0 Then
        invokeReqst = oObjRef.invokeLocateReqst(Me)
    Else
        invokeReqst = oObjRef.invokeReqst(Me)
    End If
    If invokeReqst And Not HasUserExceptions Then
        Dim sTypeId As String
        sTypeId = Me.OutRes.readString()
        Call mVBOrb.VBOrb.raiseUNKNOWN(0, mVBOrb.VBOrb.CompletedMAYBE, _
            "Unknown USER_EXCEPTION [" & sTypeId & "] received")
    End If
    Set oObjRef = Nothing
    iReqState = REQST_NOTEXISTS 'Calling OutRes is still allowed
End Function

'Set the result
'IN:    msgType     See cOrbImpl.ConnReqWait(), cOrbImpl.ConnClose(),
'                   cOrbImpl.ConnRecvMsg(), Me.OutRes()
'                   1, 2, 4, 1090, 1100
'IN.    oIn         Result or Nothing
Public Sub setRes(ByVal msgType As Integer, ByVal oIn As cOrbStream)
    'First fragment
    iReplMsgType = msgType
    Set oReplStream = oIn
End Sub

'Add next fragment
Friend Function addFragment(ByVal oIn As cOrbStream) As Boolean
    Dim bIsComplete As Boolean
    If iReqState = REQST_INCOMING Then
        bIsComplete = oReqsStream.addFragment(oIn)
        If bIsComplete Then
            Call readReqstHead(oReqsStream)
        End If
    Else
        bIsComplete = oReplStream.addFragment(oIn)
    End If
    addFragment = bIsComplete
End Function

Public Function isRes() As Boolean
    If iReplMsgType < 0 Then
        isRes = False
    ElseIf oReplStream Is Nothing Then
        isRes = True
    Else
        isRes = oReplStream.isComplete()
    End If
End Function

'RET:   5 If CloseConnection received and we should reopen connection and send again
Public Function getResType() As Integer
    getResType = iReplMsgType
End Function

'Get the result stream
'Is called to read the reply header and is called to read the reply body
'RET:           Stream for Results and UserExceptions
Public Property Get OutRes() As cOrbStream
    If oReplStream Is Nothing Then
        Select Case iReplMsgType
        'Case 0 '= GIOP Request received
        'Case 1 '= GIOP Reply received
        'Case 2 '= CancelRequest received
        'Case 3 '= Locate Request received
        'Case 4 '= GIOP LocateReply received
        Case 5 '5 = GIOP CloseConnection received
            Call mVBOrb.VBOrb.raiseCOMMFAILURE(1, mVBOrb.VBOrb.CompletedMAYBE, _
                "CloseConnection received")
        Case 6 '= MessageError received
            Call mVBOrb.VBOrb.raiseCOMMFAILURE(1, mVBOrb.VBOrb.CompletedMAYBE, _
                "MessageError received")
        'case 7'= Fragment received
        'See cOrbImpl.ConnClose():
        Case 1006
            Call mVBOrb.VBOrb.raiseMARSHAL(0, mVBOrb.VBOrb.CompletedMAYBE, _
                "MsgErr")
        Case 1007
            Call mVBOrb.VBOrb.raiseCOMMFAILURE(0, mVBOrb.VBOrb.CompletedMAYBE, _
                "Unsupported GIOP fragment message")
        Case 1100
            Call mVBOrb.VBOrb.raiseTIMEOUT(0, mVBOrb.VBOrb.CompletedMAYBE, _
                "ReplyEndTime")
        Case Else
            Call mVBOrb.VBOrb.raiseCOMMFAILURE(1, mVBOrb.VBOrb.CompletedMAYBE, _
                "No Results")
        End Select
    End If
    Set OutRes = oReplStream
End Property

