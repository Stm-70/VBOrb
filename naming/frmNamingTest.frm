VERSION 5.00
Begin VB.Form frmNamingTest 
   Caption         =   "NameService Test"
   ClientHeight    =   9225
   ClientLeft      =   60
   ClientTop       =   345
   ClientWidth     =   8100
   LinkTopic       =   "Form1"
   ScaleHeight     =   9225
   ScaleWidth      =   8100
   StartUpPosition =   3  'Windows-Standard
   Begin VB.Frame Frame3 
      Caption         =   "ORB.string_to_object()"
      Height          =   1335
      Left            =   120
      TabIndex        =   38
      Top             =   7800
      Width           =   7815
      Begin VB.CommandButton cmdStrToObj 
         Caption         =   "assign test_obj"
         Height          =   375
         Left            =   5640
         TabIndex        =   40
         Top             =   840
         Width           =   1695
      End
      Begin VB.TextBox txtURL 
         Height          =   285
         Left            =   840
         TabIndex        =   39
         Text            =   "url"
         Top             =   360
         Width           =   6495
      End
      Begin VB.Label Label7 
         Caption         =   "URL:"
         Height          =   255
         Left            =   240
         TabIndex        =   43
         Top             =   360
         Width           =   495
      End
      Begin VB.Label lblObjType 
         Caption         =   "type"
         Height          =   255
         Left            =   840
         TabIndex        =   42
         Top             =   840
         Width           =   4695
      End
      Begin VB.Label Label8 
         Caption         =   "Type:"
         Height          =   255
         Left            =   240
         TabIndex        =   41
         Top             =   840
         Width           =   495
      End
   End
   Begin VB.Frame frNameComponents 
      Caption         =   "NameComponent(s)"
      Height          =   2655
      Left            =   240
      TabIndex        =   11
      Top             =   2040
      Width           =   7575
      Begin VB.CommandButton cmdBindContext 
         Caption         =   "bind_context(test_obj)"
         Height          =   375
         Left            =   840
         TabIndex        =   37
         Top             =   2160
         Width           =   1935
      End
      Begin VB.CommandButton cmdRebindContext 
         Caption         =   "rebind_context()"
         Height          =   375
         Left            =   2880
         TabIndex        =   36
         Top             =   2160
         Width           =   1695
      End
      Begin VB.CommandButton cmdBindNewContext 
         Caption         =   "bind_new_context()"
         Height          =   375
         Left            =   4680
         TabIndex        =   35
         Top             =   2160
         Width           =   1695
      End
      Begin VB.CommandButton cmdList 
         Caption         =   "list()?"
         Enabled         =   0   'False
         Height          =   375
         Left            =   4200
         TabIndex        =   33
         Top             =   1200
         Width           =   1575
      End
      Begin VB.CommandButton cmdBind 
         Caption         =   "bind(test_obj)"
         Height          =   375
         Left            =   840
         TabIndex        =   32
         Top             =   1680
         Width           =   1575
      End
      Begin VB.CommandButton cmdRebind 
         Caption         =   "rebind(test_obj)"
         Height          =   375
         Left            =   2520
         TabIndex        =   31
         Top             =   1680
         Width           =   1575
      End
      Begin VB.CommandButton cmdUnbind 
         Caption         =   "unbind()"
         Height          =   375
         Left            =   4200
         TabIndex        =   30
         Top             =   1680
         Width           =   1575
      End
      Begin VB.CommandButton cmdResolve 
         Caption         =   "resolve()"
         Enabled         =   0   'False
         Height          =   375
         Left            =   840
         TabIndex        =   23
         Top             =   1200
         Width           =   1575
      End
      Begin VB.CommandButton cmdToString 
         Caption         =   "to_string()"
         Enabled         =   0   'False
         Height          =   375
         Left            =   2520
         TabIndex        =   22
         Top             =   1200
         Width           =   1575
      End
      Begin VB.TextBox txtNC3kind 
         Height          =   285
         Left            =   5400
         TabIndex        =   19
         Top             =   720
         Width           =   1935
      End
      Begin VB.TextBox txtNC2kind 
         Height          =   285
         Left            =   3120
         TabIndex        =   18
         Top             =   720
         Width           =   1935
      End
      Begin VB.TextBox txtNC3id 
         Height          =   285
         Left            =   5400
         TabIndex        =   17
         Top             =   360
         Width           =   1935
      End
      Begin VB.TextBox txtNC2id 
         Height          =   285
         Left            =   3120
         TabIndex        =   16
         Top             =   360
         Width           =   1935
      End
      Begin VB.TextBox txtNC1kind 
         Height          =   285
         Left            =   840
         TabIndex        =   15
         Top             =   720
         Width           =   1935
      End
      Begin VB.TextBox txtNC1id 
         Height          =   285
         Left            =   840
         TabIndex        =   12
         Text            =   "Hello"
         Top             =   360
         Width           =   1935
      End
      Begin VB.Label Label9 
         Caption         =   "Query:"
         Height          =   375
         Left            =   240
         TabIndex        =   34
         Top             =   1200
         Width           =   615
      End
      Begin VB.Label Label5 
         Caption         =   "kind(s):"
         Height          =   255
         Left            =   240
         TabIndex        =   14
         Top             =   720
         Width           =   495
      End
      Begin VB.Label Label4 
         Caption         =   "id(s):"
         Height          =   255
         Left            =   240
         TabIndex        =   13
         Top             =   360
         Width           =   495
      End
   End
   Begin VB.Frame frNamingContext 
      Caption         =   "NamingContext"
      Height          =   855
      Left            =   240
      TabIndex        =   7
      Top             =   6480
      Width           =   7575
      Begin VB.CommandButton cmdNewContext 
         Caption         =   "new_context()"
         Height          =   375
         Left            =   840
         TabIndex        =   20
         Top             =   360
         Width           =   1575
      End
      Begin VB.CommandButton cmdDestroy 
         Caption         =   "destroy()"
         Height          =   375
         Left            =   2520
         TabIndex        =   8
         Top             =   360
         Width           =   1575
      End
   End
   Begin VB.Frame Frame1 
      Caption         =   "1. Where is the NameService running?"
      Height          =   1575
      Left            =   120
      TabIndex        =   0
      Top             =   120
      Width           =   7815
      Begin VB.TextBox txtNameServiceVersion 
         Height          =   285
         Left            =   6960
         TabIndex        =   10
         Text            =   "1.1"
         Top             =   480
         Width           =   495
      End
      Begin VB.CommandButton cmdApply 
         Caption         =   "Apply"
         Height          =   375
         Left            =   240
         TabIndex        =   6
         Top             =   960
         Width           =   1095
      End
      Begin VB.TextBox txtNameServicePort 
         Height          =   285
         Left            =   4560
         TabIndex        =   4
         Top             =   480
         Width           =   1095
      End
      Begin VB.TextBox txtNameServiceHost 
         Height          =   285
         Left            =   1080
         TabIndex        =   2
         Text            =   "localhost"
         Top             =   480
         Width           =   2895
      End
      Begin VB.Label Label3 
         Caption         =   "IIOP-Version:"
         Height          =   255
         Left            =   5880
         TabIndex        =   9
         Top             =   480
         Width           =   975
      End
      Begin VB.Label lblNameServiceLoc 
         Caption         =   "corbaloc::"
         Height          =   255
         Left            =   1440
         TabIndex        =   5
         Top             =   1080
         Width           =   6255
      End
      Begin VB.Label Label2 
         Caption         =   "Port:"
         Height          =   255
         Left            =   4080
         TabIndex        =   3
         Top             =   480
         Width           =   375
      End
      Begin VB.Label Label1 
         Caption         =   "Hostname:"
         Height          =   255
         Left            =   240
         TabIndex        =   1
         Top             =   480
         Width           =   855
      End
   End
   Begin VB.Frame frTestArea 
      Caption         =   "2. Test Area"
      Height          =   5895
      Left            =   120
      TabIndex        =   21
      Top             =   1800
      Width           =   7815
      Begin VB.Frame Frame4 
         Caption         =   "String"
         Height          =   1455
         Left            =   120
         TabIndex        =   24
         Top             =   3120
         Width           =   7575
         Begin VB.CommandButton cmdResolveStr 
            Caption         =   "resolve_str()"
            Enabled         =   0   'False
            Height          =   375
            Left            =   240
            TabIndex        =   29
            Top             =   840
            Width           =   1575
         End
         Begin VB.CommandButton cmdToUrl 
            Caption         =   "to_url()"
            Height          =   375
            Left            =   1920
            TabIndex        =   28
            Top             =   840
            Width           =   1575
         End
         Begin VB.CommandButton cmdToName 
            Caption         =   "to_name()"
            Height          =   375
            Left            =   3600
            TabIndex        =   27
            Top             =   840
            Width           =   1575
         End
         Begin VB.TextBox txtString 
            Height          =   285
            Left            =   840
            TabIndex        =   25
            Text            =   "string"
            Top             =   360
            Width           =   6495
         End
         Begin VB.Label Label6 
            Caption         =   "string:"
            Height          =   255
            Left            =   240
            TabIndex        =   26
            Top             =   360
            Width           =   615
         End
      End
   End
End
Attribute VB_Name = "frmNamingTest"
Attribute VB_GlobalNameSpace = False
Attribute VB_Creatable = False
Attribute VB_PredeclaredId = True
Attribute VB_Exposed = False
Option Explicit

Dim oOrb As cOrbImpl
Dim sNameServiceLocURL As String
Dim sSunInitLocURL As String
Dim oNmRootContext As c_NmContext
Dim oNmRootContextExt As c_NmContextExt
Dim oTestObj As cOrbObject

Private Function getIniFile() As String
    getIniFile = App.Path & "\NamingTest.ini"
End Function

Private Sub Form_Load()
    Dim inVar As Variant
    Dim iFileNo As Integer
    iFileNo = FreeFile
    On Error GoTo skipIniFile
    Open getIniFile() For Input As #iFileNo
    Input #iFileNo, inVar: txtNameServiceHost.Text = inVar
    Input #iFileNo, inVar: txtNameServicePort.Text = inVar
    Input #iFileNo, inVar: txtNameServiceVersion.Text = inVar
skipIniFile:
    On Error Resume Next
    Close #iFileNo
    On Error GoTo 0

    'Writing start message to logfile
    Dim sLogFile As String
    sLogFile = App.Path & "\NamingTest.log"
    Call VBOrb.logMsg(sLogFile, "Starting NamingTest")
    'Get an ORB
    Set oOrb = VBOrb.init(ORBId:="", LogFile:=sLogFile)
    oOrb.RelativeRoundtripTimeout = 20000
    
    frNameComponents.Enabled = False
End Sub

Private Sub Form_Unload(Cancel As Integer)
    Dim iFileNo As Integer
    iFileNo = FreeFile
    On Error GoTo skipIniFile
    Open getIniFile() For Output As #iFileNo
    Write #iFileNo, txtNameServiceHost.Text
    Write #iFileNo, txtNameServicePort.Text
    Write #iFileNo, txtNameServiceVersion.Text
skipIniFile:
    On Error Resume Next
    Close #iFileNo
End Sub

Private Sub cmdApply_Click()
    On Error GoTo ErrHandler
    Dim sAddr As String
    If txtNameServiceVersion.Text = "1.0" Then
        sAddr = ":" & txtNameServiceHost.Text
    Else
        sAddr = ":" & txtNameServiceVersion.Text & "@" & txtNameServiceHost.Text
    End If
    If txtNameServicePort.Text <> "" And txtNameServicePort.Text <> "2809" Then
        sAddr = sAddr & ":" & txtNameServicePort.Text
    End If
    sNameServiceLocURL = "corbaloc:" & sAddr & "/NameService"
    lblNameServiceLoc.Caption = sNameServiceLocURL
    sSunInitLocURL = "corbaloc:" & sAddr & "/INIT"

    Set oNmRootContext = Nothing
    Set oNmRootContextExt = Nothing
    
    Dim oNmContext As cOrbObject
    Set oNmContext = getNmRootContext()
    sAddr = oNmContext.getObjRef.IIOPAddressFw
    sNameServiceLocURL = "corbaloc:" & sAddr & "/" & oNmContext.getObjRef.objectKey
    lblNameServiceLoc.Caption = sNameServiceLocURL
    
    frNameComponents.Enabled = True
    Call setButtons
    
    'Set an example object reference for binding test
    'Call setTestObj(oOrb.stringToObject("corbaloc::testhost:9999/testkey"))
    Call setTestObj(oOrb.stringToObject("IOR:00000000000000124D7920737065636961" & _
        "6C206F626A65637400000000000001000000000000001F0001000000000009746" & _
        "57374686F73740000270F00000007746573746B6579"))
    Exit Sub
ErrHandler:
    Call VBOrb.ErrMsgBox(Err, "Apply")
End Sub

Private Sub setButtons()
    Dim isActive As Boolean
    isActive = Not getNmRootContext() Is Nothing
    
    cmdResolve.Enabled = isActive
    cmdToString.Enabled = isActive
    cmdList.Enabled = isActive
    
    cmdResolveStr.Enabled = isActive
End Sub

Sub setTestObj(ByVal TestObj As cOrbObject)
    Set oTestObj = TestObj
    If TestObj Is Nothing Then
        lblObjType.Caption = "Object is nothing"
    Else
        lblObjType.Caption = "Id= " & TestObj.getId
    End If
End Sub

Function getTestContext() As c_NmContext
    On Error GoTo ErrHandler
    If oTestObj Is Nothing Then
        Call VBOrb.ErrRaise(1, "Missing an object")
    End If
    Set getTestContext = m_NmContext.narrow(oTestObj)
    Exit Function
ErrHandler:
    Call VBOrb.ErrReraise(Err, "getTestContext")
End Function

Function getNmRootContext() As c_NmContext
    On Error GoTo ErrHandler
    If oNmRootContext Is Nothing Then
        Dim oObject As cOrbObject
        Set oObject = oOrb.stringToObject(sNameServiceLocURL)
        On Error Resume Next
        Set oNmRootContext = m_NmContext.narrow(oObject)
        If Err.Number <> 0 Then
            Call VBOrb.ErrSave(Err)
            If VBOrb.ErrIsSystemEx() And _
                Err.Number = (VBOrb.ITF_E_OBJECT_NOT_EXIST_NO Or vbObjectError) Then
                Set oObject = oOrb.stringToObject(sSunInitLocURL)
                If Err.Number <> 0 Then GoTo ErrLoadRaise
                Call oObject.getObjRef.setRebindMode(1)
                Dim oSunInitialReferences As c_SunInitialReferences
                Set oSunInitialReferences = m_SunInitialReferences.uncheckedNarrow(oObject)
                If Err.Number <> 0 Then GoTo ErrLoadRaise
                Set oObject = oSunInitialReferences.getFunc("NameService")
                If Err.Number <> 0 Then GoTo ErrLoadRaise
                Set oNmRootContext = m_NmContext.narrow(oObject)
                If Err.Number <> 0 Then GoTo ErrLoadRaise
            Else
                GoTo ErrLoadRaise
            End If
            Call VBOrb.ErrLoad
        End If
        On Error GoTo ErrHandler
    End If
    Set getNmRootContext = oNmRootContext
    Exit Function
ErrHandler:
    Call VBOrb.ErrSave(Err)
    Resume ErrLoadRaise
ErrLoadRaise:
    On Error GoTo 0
    Call VBOrb.ErrReraise(VBOrb.ErrLoad, "getNmRootContext")
End Function

Function getNmRootContextExt() As c_NmContextExt
    On Error GoTo ErrHandler
    If oNmRootContextExt Is Nothing Then
        Dim oObject As cOrbObject
        Set oObject = oOrb.stringToObject(sNameServiceLocURL)
        Set oNmRootContextExt = m_NmContextExt.narrow(oObject)
    End If
    Set getNmRootContextExt = oNmRootContextExt
    Exit Function
ErrHandler:
    Call VBOrb.ErrReraise(Err, "getNmRootContext")
End Function

Function getNCs() As c_NmNameComponentSeq
    On Error GoTo ErrHandler
    Dim NCs As c_NmNameComponentSeq
    Set NCs = New c_NmNameComponentSeq
    Dim ncLen As Integer
    ncLen = 3
    If txtNC3id.Text = "" And txtNC3kind.Text = "" Then
        ncLen = 2
    End If
    If txtNC2id.Text = "" And txtNC2kind.Text = "" Then
        ncLen = 1
    End If
    If txtNC1id.Text = "" And txtNC1kind.Text = "" Then
        ncLen = 0
    End If
    NCs.Length = ncLen
    If ncLen >= 3 Then
        Set NCs.Item(2) = New c_NmNameComponent
        NCs.Item(2).id = txtNC3id.Text
        NCs.Item(2).kind = txtNC3kind.Text
    End If
    If ncLen >= 2 Then
        Set NCs.Item(1) = New c_NmNameComponent
        NCs.Item(1).id = txtNC2id.Text
        NCs.Item(1).kind = txtNC2kind.Text
    End If
    If ncLen >= 1 Then
        Set NCs.Item(0) = New c_NmNameComponent
        NCs.Item(0).id = txtNC1id.Text
        NCs.Item(0).kind = txtNC1kind.Text
    End If
    Set getNCs = NCs
    Exit Function
ErrHandler:
    Call VBOrb.ErrReraise(Err, "getNCs")
End Function

Sub setNCs(ByVal NCs As c_NmNameComponentSeq)
    On Error GoTo ErrHandler
    If NCs.Length >= 3 Then
        txtNC3id.Text = NCs.Item(2).id: txtNC3kind.Text = NCs.Item(2).kind
    Else
        txtNC3id.Text = "": txtNC3kind.Text = ""
    End If
    If NCs.Length >= 2 Then
        txtNC2id.Text = NCs.Item(1).id: txtNC2kind.Text = NCs.Item(1).kind
    Else
        txtNC2id.Text = "": txtNC2kind.Text = ""
    End If
    If NCs.Length >= 1 Then
        txtNC1id.Text = NCs.Item(0).id: txtNC1kind.Text = NCs.Item(0).kind
    Else
        txtNC1id.Text = "": txtNC1kind.Text = ""
    End If
    Exit Sub
ErrHandler:
    Call VBOrb.ErrReraise(Err, "setNCs")
End Sub

Private Sub cmdResolve_Click()
    On Error GoTo ErrHandler
    Dim oNmContext As c_NmContext
    Set oNmContext = getNmRootContext()
    Dim oObject As cOrbObject
    Set oObject = oNmContext.resolve(getNCs())
    If oObject Is Nothing Then
        Call MsgBox("Object is nothing")
    Else
        Dim oFrmIOR As frmIOR
        Set oFrmIOR = New frmIOR
        Call oFrmIOR.setObject(oOrb, oObject)
        Load oFrmIOR
        Call oFrmIOR.Show(1)
    End If
    Exit Sub
ErrHandler:
    Call VBOrb.ErrMsgBox(Err, "cmdResolve")
End Sub

Private Sub cmdList_Click()
    On Error GoTo ErrHandler
    Dim oNmRContext As c_NmContext
    Set oNmRContext = getNmRootContext()
    Dim oBndList As c_NmBindingSeq
    Dim iList As Long
    Dim oBnd As c_NmBinding
    Dim oBndIter As c_NmBindingIterator
    Call oNmRContext.list(3, oBndList, oBndIter)
    Dim sList As String
    sList = "List"
    For iList = 0 To oBndList.Length - 1
        Set oBnd = oBndList.Item(iList)
        sList = sList & vbCrLf & bndToStr(iList, oBnd)
    Next iList
    Dim a As ErrObject
    If Not oBndIter Is Nothing Then
        Do While oBndIter.nextOne(oBnd)
            sList = sList & vbCrLf & bndToStr(iList, oBnd)
            iList = iList + 1
        Loop
        Call oBndIter.destroy
    End If
    Call MsgBox(sList)
    Exit Sub
ErrHandler:
    Call VBOrb.ErrMsgBox(Err, "cmdList")
End Sub

Private Function bndToStr(ByVal iList As Long, _
    ByVal oBnd As c_NmBinding) As String
    Dim sList As String
    sList = CStr(iList) & ": " _
        & IIf(oBnd.binding_type = m_Nm.ncontext, "Ctx", "Obj") & " " _
        & oBnd.binding_name.Item(0).id & "." _
        & oBnd.binding_name.Item(0).kind
    bndToStr = sList
End Function

Private Sub cmdBind_Click()
    On Error GoTo ErrHandler
    Dim oNmRContext As c_NmContext
    Set oNmRContext = getNmRootContext()
    Call oNmRContext.bind(getNCs(), oTestObj)
    Exit Sub
ErrHandler:
    Call VBOrb.ErrMsgBox(Err, "cmdBind")
End Sub

Private Sub cmdRebind_Click()
    On Error GoTo ErrHandler
    Dim oNmRContext As c_NmContext
    Set oNmRContext = getNmRootContext()
    Call oNmRContext.rebind(getNCs(), oTestObj)
    Exit Sub
ErrHandler:
    Call VBOrb.ErrMsgBox(Err, "cmdRebind")
End Sub

Private Sub cmdUnbind_Click()
    On Error GoTo ErrHandler
    Dim oNmRContext As c_NmContext
    Set oNmRContext = getNmRootContext()
    Call oNmRContext.unbind(getNCs())
    Exit Sub
ErrHandler:
    Call VBOrb.ErrMsgBox(Err, "cmdUnbind")
End Sub

Private Sub cmdToString_Click()
    On Error GoTo ErrHandler
    Dim oNmRContextExt As c_NmContextExt
    Set oNmRContextExt = getNmRootContextExt()
    txtString.Text = oNmRContextExt.toString(getNCs())
    Exit Sub
ErrHandler:
    Call VBOrb.ErrMsgBox(Err, "cmdToString")
End Sub

Private Sub cmdResolveStr_Click()
    On Error GoTo ErrHandler
    Dim oNmRContextExt As c_NmContextExt
    Set oNmRContextExt = getNmRootContextExt()
    Dim oObject As cOrbObject
    Set oObject = oNmRContextExt.resolveStr(txtString.Text)
    If oObject Is Nothing Then
        Call MsgBox("Object is nothing")
    Else
        Dim oFrmIOR As frmIOR
        Set oFrmIOR = New frmIOR
        Call oFrmIOR.setObject(oOrb, oObject)
        Load oFrmIOR
        Call oFrmIOR.Show(1)
    End If
    Exit Sub
ErrHandler:
    Call VBOrb.ErrMsgBox(Err, "cmdResolveStr")
End Sub

Private Sub cmdToUrl_Click()
    On Error GoTo ErrHandler
    Dim oNmRContextExt As c_NmContextExt
    Set oNmRContextExt = getNmRootContextExt()
    txtURL.Text = oNmRContextExt.toUrl(":" & txtNameServiceHost.Text & ":" _
        & txtNameServicePort.Text, txtString.Text)
    Exit Sub
ErrHandler:
    Call VBOrb.ErrMsgBox(Err, "cmdToUrl")
End Sub

Private Sub cmdToName_Click()
    On Error GoTo ErrHandler
    Dim oNmRContextExt As c_NmContextExt
    Set oNmRContextExt = getNmRootContextExt()
    Call setNCs(oNmRContextExt.toName(txtString.Text))
    Exit Sub
ErrHandler:
    Call VBOrb.ErrMsgBox(Err, "cmdToName")
End Sub

Private Sub cmdBindContext_Click()
    On Error GoTo ErrHandler
    Dim oNmRContext As c_NmContext
    Set oNmRContext = getNmRootContext()
    Dim oNmTContext As c_NmContext
    Set oNmTContext = getTestContext()
    Call oNmRContext.bindContext(getNCs(), oNmTContext)
    Exit Sub
ErrHandler:
    Call VBOrb.ErrMsgBox(Err, "cmdBindContext")
End Sub

Private Sub cmdBindNewContext_Click()
    On Error GoTo ErrHandler
    Dim oNmRContext As c_NmContext
    Set oNmRContext = getNmRootContext()
    Call oNmRContext.bindNewContext(getNCs())
    Exit Sub
ErrHandler:
    Call VBOrb.ErrMsgBox(Err, "cmdBindNewContext")
End Sub

Private Sub cmdRebindContext_Click()
    On Error GoTo ErrHandler
    Dim oNmRContext As c_NmContext
    Set oNmRContext = getNmRootContext()
    Dim oNmTContext As c_NmContext
    Set oNmTContext = getTestContext()
    Call oNmRContext.rebindContext(getNCs(), oNmTContext)
    Exit Sub
ErrHandler:
    Call VBOrb.ErrMsgBox(Err, "cmdRebindContext")
End Sub

Private Sub cmdNewContext_Click()
    On Error GoTo ErrHandler
    Dim oNmRContext As c_NmContext
    Set oNmRContext = getNmRootContext()
    Call setTestObj(oNmRContext.newContext())
    Exit Sub
ErrHandler:
    Call VBOrb.ErrMsgBox(Err, "cmdNewContext")
End Sub

Private Sub cmdDestroy_Click()
    On Error GoTo ErrHandler
    Dim oNmTContext As c_NmContext
    Set oNmTContext = getTestContext()
    Call oNmTContext.destroy
    Exit Sub
ErrHandler:
    Call VBOrb.ErrMsgBox(Err, "cmdDestroy")
End Sub

Private Sub cmdStrToObj_Click()
    On Error GoTo ErrHandler
    lblObjType.Caption = ""
    Dim oObject As cOrbObject
    Set oObject = oOrb.stringToObject(txtURL.Text)
    Call setTestObj(oObject)
    If Not oObject Is Nothing Then
        Dim oFrmIOR As frmIOR
        Set oFrmIOR = New frmIOR
        Call oFrmIOR.setObject(oOrb, oObject)
        Load oFrmIOR
        Call oFrmIOR.Show(1)
    End If
    Exit Sub
ErrHandler:
    Call VBOrb.ErrMsgBox(Err, "cmdStrToObj")
End Sub

