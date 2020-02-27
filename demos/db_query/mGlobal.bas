Attribute VB_Name = "mGlobal"
Option Explicit

Dim oOrb As cOrbImpl

Dim sOldManagerIOR As String
Dim oDBManager As c_DBManager
Dim oDBSession As c_DBSession

Public Sub initOrb()
    'Get an ORB
    Set oOrb = VBOrb.init(ORBId:="")
End Sub

Public Sub termOrb()
    If Not oOrb Is Nothing Then Call oOrb.shutdown(False)
End Sub

Public Sub initDBManager(ByVal sIOR As String)
    On Error GoTo ErrHandler
    If oDBManager Is Nothing Or sOldManagerIOR <> sIOR Then
        Dim oObjRef As cOrbObjRef
        Set oObjRef = oOrb.stringToObject(sIOR)
        Set oDBManager = m_DBManager.narrow(oObjRef)
        sOldManagerIOR = sIOR
    End If
    Exit Sub
ErrHandler:
    Set oDBManager = Nothing
    Call VBOrb.ErrReraise(Err, "initDBManager")
End Sub

Public Sub shutdownServer(ByVal sUser As String, ByVal sPassword As String)
    If oDBManager Is Nothing Then
        Call VBOrb.ErrRaise(1, _
            "No valid DBManager. Please fill up IOR field.")
    End If
    Call oDBManager.shutdownServer(sUser, sPassword)
End Sub

Public Sub openDBSession(ByRef oEx As VBOrb.cOrbException, _
    ByVal sURL As String, ByVal sUser As String, ByVal sPassword As String, _
    ByRef oDBWarns As c_DBMessageSeq)
    On Error GoTo ErrHandler
    If oDBManager Is Nothing Then
        Call VBOrb.ErrRaise(1, _
            "No valid DBManager. Please fill up IOR field.")
    End If
    Set oDBSession = oDBManager.getSession(sURL, sUser, _
        sPassword, oDBWarns)
    Exit Sub
ExHandler:
    Set oEx = VBOrb.getException()
    Exit Sub
ErrHandler:
    If VBOrb.ErrIsUserEx() Then Resume ExHandler
    Call VBOrb.ErrReraise(Err, "openDBSession")
End Sub

Public Sub showExeptionBox(ByVal oEx As VBOrb.cOrbException)
    Select Case TypeName(oEx)
    Case "c_DBErrMsgsEx"
        Dim oDBEx As c_DBErrMsgsEx
        Set oDBEx = oEx
        Dim oErrs As c_DBMessageSeq
        Set oErrs = oDBEx.errs
        Dim i As Integer
        Dim sErrMsg As String
        For i = 0 To oErrs.Length - 1
            sErrMsg = sErrMsg & "Error " & oErrs.Item(i).sqlcode _
                & ", " & oErrs.Item(i).sqlstate _
                & vbCrLf & oErrs.Item(i).sqlmessage
        Next i
        Call MsgBox(sErrMsg)
    Case Else
        Call MsgBox("Error " & oEx.Source & vbCrLf & oEx.Description)
    End Select
End Sub

Public Sub closeDBSession(ByRef oEx As VBOrb.cOrbException, _
    ByRef oDBWarns As c_DBMessageSeq)
    On Error GoTo ErrHandler
    Call oDBSession.closeSub(oDBWarns)
    Set oDBSession = Nothing
    Exit Sub
ExHandler:
    Set oEx = VBOrb.getException()
    Exit Sub
ErrHandler:
    If VBOrb.ErrIsUserEx() Then Resume ExHandler
    Call VBOrb.ErrReraise(Err, "closeDBSession")
End Sub

Public Property Get DBSession() As c_DBSession
    Set DBSession = oDBSession
End Property
