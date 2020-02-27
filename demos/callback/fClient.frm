VERSION 5.00
Begin VB.Form fClient 
   Caption         =   "CountClient"
   ClientHeight    =   1470
   ClientLeft      =   60
   ClientTop       =   345
   ClientWidth     =   5370
   LinkTopic       =   "Form1"
   ScaleHeight     =   1470
   ScaleWidth      =   5370
   StartUpPosition =   3  'Windows-Standard
   Begin VB.CheckBox cbLogFile 
      Caption         =   "Write Logfile"
      Height          =   255
      Left            =   120
      TabIndex        =   8
      Top             =   480
      Width           =   1335
   End
   Begin VB.CommandButton cmdStop 
      Caption         =   "Stop"
      Enabled         =   0   'False
      Height          =   495
      Left            =   4080
      TabIndex        =   5
      Top             =   840
      Width           =   975
   End
   Begin VB.CommandButton cmdStart 
      Caption         =   "Start"
      Enabled         =   0   'False
      Height          =   495
      Left            =   3000
      TabIndex        =   4
      Top             =   840
      Width           =   975
   End
   Begin VB.Timer tm1 
      Interval        =   5
      Left            =   4920
      Top             =   720
   End
   Begin VB.ComboBox tbIOR 
      Height          =   315
      ItemData        =   "fClient.frx":0000
      Left            =   0
      List            =   "fClient.frx":000A
      TabIndex        =   1
      Text            =   "corbaloc::1.1@localhost:1900/Coordinator"
      Top             =   120
      Width           =   5295
   End
   Begin VB.CommandButton cmdRegister 
      Caption         =   "Register"
      Height          =   495
      Left            =   120
      TabIndex        =   0
      Top             =   840
      Width           =   1335
   End
   Begin VB.Label lblSum 
      Caption         =   "?"
      Height          =   255
      Left            =   2040
      TabIndex        =   7
      Top             =   1200
      Width           =   1215
   End
   Begin VB.Label Label2 
      Caption         =   "Sum: "
      Height          =   255
      Left            =   1560
      TabIndex        =   6
      Top             =   1200
      Width           =   615
   End
   Begin VB.Label lbID 
      Caption         =   "?"
      Height          =   255
      Left            =   2280
      TabIndex        =   3
      Top             =   840
      Width           =   615
   End
   Begin VB.Label Label1 
      Caption         =   "Client ID:"
      Height          =   255
      Left            =   1560
      TabIndex        =   2
      Top             =   840
      Width           =   735
   End
End
Attribute VB_Name = "fClient"
Attribute VB_GlobalNameSpace = False
Attribute VB_Creatable = False
Attribute VB_PredeclaredId = True
Attribute VB_Exposed = False
Option Explicit

Dim oOrb As cOrbImpl
Dim oClientControlImplTie As c_ClientControlImplTie
Dim oCoordinator As c_ServerCoordinator
Dim oCounter As c_ServerCounter
Public lID As Long
Public bCounting As Boolean
Dim lSum As Long

Implements c_ClientControl

Private Sub cmdRegister_Click()
    On Error GoTo ErrHandler
    'Writing start message to logfile
    Dim sLogFile As String
    If cbLogFile.Value <> 0 Then
        sLogFile = App.Path & "\CBClient.log"
        Call VBOrb.logMsg(sLogFile, "Starting CBClient")
    Else
        sLogFile = ""
    End If
    'Get an ORB
    Set oOrb = VBOrb.init(ORBId:="", LogFile:=sLogFile, loglevel:=&H8)
    'Create an object and connect the object to the ORB.
    Set oClientControlImplTie = New c_ClientControlImplTie
    Call oClientControlImplTie.setDelegate(Me)
    Call oOrb.Connect(oClientControlImplTie)

    Dim oObjRef As cOrbObjRef
    Set oObjRef = oOrb.stringToObject(tbIOR.Text)
    Set oCoordinator = m_ServerCoordinator.narrow(oObjRef)
    Dim oClientControl As c_ClientControl
    Set oClientControl = oClientControlImplTie.This
    Set oCounter = oCoordinator.register(oClientControl)
    cmdRegister.Enabled = False
    cbLogFile.Enabled = False
    tbIOR.Enabled = False
    cmdStart.Enabled = True
    Exit Sub
ErrHandler:
    Call VBOrb.ErrMsgBox(Err, "CountClient")
End Sub

Private Sub c_ClientControl_setId(ByVal id As Long)
    lID = id
    lbID.Caption = CStr(lID)
End Sub

Private Function c_ClientControl_getId() As Long
    c_ClientControl_getId = lID
End Function

Private Sub c_ClientControl_start()
    lblSum.Caption = "?"
    lSum = 0
    bCounting = True
    cmdStart.Enabled = False
    cmdStop.Enabled = True
End Sub

Private Function c_ClientControl_stopFunc() As String
    bCounting = False
    cmdStart.Enabled = True
    cmdStop.Enabled = False
    lblSum.Caption = CStr(oCounter.sum) & ", " & CStr(lSum)
    c_ClientControl_stopFunc = lblSum.Caption
End Function

Private Sub c_ClientControl_sendWStringToClient(ByVal wstr As String)
    
End Sub

Private Sub c_ClientControl_sendBigArrayToClient(ByVal buffer As c_ByteArr4096)

End Sub

Private Sub cmdStart_Click()
    Dim oClientControl As c_ClientControl
    Set oClientControl = Me
    Call oClientControl.start
End Sub

Private Sub cmdStop_Click()
    Dim oClientControl As c_ClientControl
    Set oClientControl = Me
    Call oClientControl.stopFunc
End Sub

Private Function getIniFile() As String
    getIniFile = App.Path & "\CBClient.ini"
End Function

Private Sub Form_Load()
    Dim inVar As Variant
    Dim iFileNo As Integer
    iFileNo = FreeFile
    On Error GoTo skipIniFile
    Open getIniFile() For Input As #iFileNo
    Input #iFileNo, inVar: tbIOR.Text = inVar
skipIniFile:
    On Error Resume Next
    Close #iFileNo
    On Error GoTo 0
End Sub

Private Sub Form_Unload(Cancel As Integer)
    On Error Resume Next
    If Not oCoordinator Is Nothing Then
        Dim oClientControl As c_ClientControl
        Set oClientControl = oClientControlImplTie.This
        Call oCoordinator.unregister(oClientControl)
    End If
    
    Dim iFileNo As Integer
    iFileNo = FreeFile
    On Error GoTo skipIniFile
    Open getIniFile() For Output As #iFileNo
    Write #iFileNo, tbIOR.Text
skipIniFile:
    On Error Resume Next
    Close #iFileNo
End Sub

Private Sub tm1_Timer()
    If oOrb Is Nothing Then Exit Sub
    Call oOrb.performWork
    If bCounting Then
        On Error GoTo IncError
        Call oCounter.increment
        lSum = lSum + 1
    End If
    Exit Sub
IncError:
    Call VBOrb.ErrSave(Err)
    Resume EndOfIncrement
EndOfIncrement:
    On Error Resume Next
    Dim oClientControl As c_ClientControl
    Set oClientControl = Me
    Call oClientControl.stopFunc
    Call VBOrb.ErrLoad
    Call VBOrb.ErrMsgBox(Err, "oCounter.increment")
End Sub
