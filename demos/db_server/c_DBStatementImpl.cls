VERSION 1.0 CLASS
BEGIN
  MultiUse = -1  'True
END
Attribute VB_Name = "c_DBStatementImpl"
Attribute VB_GlobalNameSpace = False
Attribute VB_Creatable = True
Attribute VB_PredeclaredId = False
Attribute VB_Exposed = False
Attribute VB_Ext_KEY = "SavedWithClassBuilder" ,"Yes"
Attribute VB_Ext_KEY = "Top_Level" ,"Yes"
'Generated by IDL2VB v092. Copyright (c) 1999 Martin.Both
'Source File Name: ../demos/db_query/database.idl

Option Explicit

Implements cOrbSkeleton
Implements c_DBStatement

'interface ::DB::Statement
Const sTypeId As String = "IDL:DB/Statement:1.0"
Private oObjRef As cOrbObjRef
Private oDelegate As c_DBStatement

Friend Property Get ObjRef() As cOrbObjRef
    Set ObjRef = oObjRef
End Property

Friend Property Get This() As c_DBStatement
    Dim oObj As cOrbObject
    Set oObj = New c_DBStatement
    If oObj.setObjRef(oObjRef, False) Then
        Set This = Nothing
    Else
        Set This = oObj
    End If
End Property

Private Property Get cOrbSkeleton_TypeId(ByVal Item As Integer) As String
    Select Case Item
    Case 0: cOrbSkeleton_TypeId = sTypeId
    Case 1: cOrbSkeleton_TypeId = "IDL:omg.org/CORBA/Object:1.0"
    End Select
End Property

Private Property Get cOrbSkeleton_ObjRef() As cOrbObjRef
    Set cOrbSkeleton_ObjRef = oObjRef
End Property

Private Property Set cOrbSkeleton_ObjRef(ByVal newObjRef As cOrbObjRef)
    Set oObjRef = newObjRef
End Property

'Helper
Private Function cOrbSkeleton_execute(ByVal sOperation As String, _
    ByVal oIn As cOrbStream, ByVal oOut As cOrbStream) As Long
    On Error GoTo ErrHandler
    Dim oImpl_ As c_DBStatement
    Set oImpl_ = IIf(oDelegate Is Nothing, Me, oDelegate)
    Select Case sOperation
    Case "describe"
        Dim a1_cols As c_DBColumnDescriptorSeq
        Dim a1_warns As c_DBMessageSeq
        Dim describe As Long
        describe = oImpl_.describe(a1_cols, a1_warns)
        Call oOut.writeLong(describe)
        Call a1_cols.writeMe(oOut, 0)
        Call a1_warns.writeMe(oOut, 0)
    Case "execute"
        Dim a2_using As c_DBColumnDataSeq
        Set a2_using = New c_DBColumnDataSeq
        Call a2_using.initByRead(oIn, 0)
        Dim a2_row As c_DBColumnDataSeq
        Dim a2_warns As c_DBMessageSeq
        Dim execute As Long
        execute = oImpl_.execute(a2_using, a2_row, a2_warns)
        Call oOut.writeLong(execute)
        Call a2_row.writeMe(oOut, 0)
        Call a2_warns.writeMe(oOut, 0)
    Case "declareCursor"
        Dim a3_hold As Boolean
        a3_hold = oIn.readBoolean()
        Dim a3_warns As c_DBMessageSeq
        Dim declareCursor As c_DBCursor
        Set declareCursor = oImpl_.declareCursor(a3_hold, a3_warns)
        Call oOut.writeObject(declareCursor)
        Call a3_warns.writeMe(oOut, 0)
    Case "free"
        Dim a4_warns As c_DBMessageSeq
        Call oImpl_.free(a4_warns)
        Call a4_warns.writeMe(oOut, 0)
    Case Else
        Call VBOrb.raiseBADOPERATION(1, VBOrb.CompletedNO, sOperation)
    End Select
    cOrbSkeleton_execute = 0 'NO_EXCEPTION
    Exit Function
UserExWriter:
    Call VBOrb.getException().writeMe(oOut)
    cOrbSkeleton_execute = 1 'USER_EXCEPTION
    Exit Function
ErrHandler:
    If VBOrb.ErrIsUserEx() Then Resume UserExWriter
    Call VBOrb.ErrReraise(Err, "Statement.execute(" & sOperation & ")")
End Function

'Implements c_DBStatement

'free() raises(c_DBErrMsgsEx)
Private Sub c_DBStatement_free(ByRef warns As c_DBMessageSeq)
    On Error GoTo ErrHandler
    Set warns = New c_DBMessageSeq
    Call oObjRef.Orb.disconnect(Me)
    Exit Sub
ErrHandler:
    Call VBOrb.ErrReraise(Err, "free")
End Sub

'describe() raises(c_DBErrMsgsEx)
Private Function c_DBStatement_describe(ByRef cols As c_DBColumnDescriptorSeq, _
    ByRef warns As c_DBMessageSeq) As Long
    On Error GoTo ErrHandler
    Set warns = New c_DBMessageSeq
    Set cols = mDBServer.oTableHead
    c_DBStatement_describe = 0
    Exit Function
ErrHandler:
    Call VBOrb.ErrReraise(Err, "describe")
End Function

'execute() raises(c_DBErrMsgsEx)
Private Function c_DBStatement_execute(ByVal using As c_DBColumnDataSeq, _
    ByRef row As c_DBColumnDataSeq, ByRef warns As c_DBMessageSeq) As Long
    On Error GoTo ErrHandler
    'do something
    Dim oDBEx As c_DBErrMsgsEx
    Dim oErrMsg As c_DBMessage
    Set oErrMsg = New c_DBMessage
    oErrMsg.sqlcode = -999
    oErrMsg.sqlstate = "EE999"
    oErrMsg.sqlmessage = "Statement.execute is not implemented"
    Set oDBEx = New c_DBErrMsgsEx
    Set oDBEx.errs = New c_DBMessageSeq
    oDBEx.errs.Length = 1
    Set oDBEx.errs.Item(0) = oErrMsg
    Call VBOrb.raiseUserEx(oDBEx)
    Exit Function
ErrHandler:
    Call VBOrb.ErrReraise(Err, "execute")
End Function

'declareCursor() raises(c_DBErrMsgsEx)
Private Function c_DBStatement_declareCursor(ByVal hold As Boolean, _
    ByRef warns As c_DBMessageSeq) As c_DBCursor
    On Error GoTo ErrHandler
    'Create a cursor
    Dim oDBCurImpl As c_DBCursorImpl
    Set oDBCurImpl = New c_DBCursorImpl
    Call oObjRef.Orb.Connect(oDBCurImpl)
    'Returning warnings if any
    Set warns = New c_DBMessageSeq
    'Returning a reference of the cursor object
    Set c_DBStatement_declareCursor = oDBCurImpl.This
    Exit Function
ErrHandler:
    Call VBOrb.ErrReraise(Err, "declareCursor")
End Function
