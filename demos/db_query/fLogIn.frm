VERSION 5.00
Begin VB.Form fLogIn 
   Caption         =   "LogIn"
   ClientHeight    =   3555
   ClientLeft      =   60
   ClientTop       =   345
   ClientWidth     =   5805
   LinkTopic       =   "Form1"
   ScaleHeight     =   3555
   ScaleWidth      =   5805
   StartUpPosition =   3  'Windows-Standard
   Begin VB.ComboBox tbIOR 
      Height          =   315
      ItemData        =   "fLogIn.frx":0000
      Left            =   240
      List            =   "fLogIn.frx":000D
      TabIndex        =   10
      Text            =   "corbaloc::1.2@localhost:1900/DBManager"
      Top             =   360
      Width           =   5295
   End
   Begin VB.ComboBox tbDBUrl 
      Height          =   315
      ItemData        =   "fLogIn.frx":0085
      Left            =   240
      List            =   "fLogIn.frx":0092
      TabIndex        =   9
      Text            =   "Testdatenbank"
      Top             =   1200
      Width           =   5295
   End
   Begin VB.CommandButton cmdShutdownServer 
      Caption         =   "Server shutdown"
      Height          =   495
      Left            =   240
      TabIndex        =   8
      Top             =   2880
      Width           =   1575
   End
   Begin VB.TextBox tbPassword 
      BeginProperty Font 
         Name            =   "MS Sans Serif"
         Size            =   9.75
         Charset         =   0
         Weight          =   400
         Underline       =   0   'False
         Italic          =   0   'False
         Strikethrough   =   0   'False
      EndProperty
      Height          =   375
      IMEMode         =   3  'DISABLE
      Left            =   2880
      PasswordChar    =   "*"
      TabIndex        =   7
      Text            =   "geheim"
      Top             =   2040
      Width           =   2295
   End
   Begin VB.TextBox tbUser 
      BeginProperty Font 
         Name            =   "MS Sans Serif"
         Size            =   9.75
         Charset         =   0
         Weight          =   400
         Underline       =   0   'False
         Italic          =   0   'False
         Strikethrough   =   0   'False
      EndProperty
      Height          =   375
      Left            =   240
      TabIndex        =   6
      Text            =   "Petra"
      Top             =   2040
      Width           =   2295
   End
   Begin VB.CommandButton cmdExit 
      Caption         =   "Exit"
      Height          =   495
      Left            =   4200
      TabIndex        =   1
      Top             =   2880
      Width           =   1335
   End
   Begin VB.CommandButton cmdLogIn 
      Caption         =   "LogIn"
      Height          =   495
      Left            =   2640
      TabIndex        =   0
      Top             =   2880
      Width           =   1335
   End
   Begin VB.Label Label4 
      Caption         =   "Password"
      Height          =   255
      Left            =   2880
      TabIndex        =   5
      Top             =   1800
      Width           =   1095
   End
   Begin VB.Label Label3 
      Caption         =   "User"
      Height          =   255
      Left            =   240
      TabIndex        =   4
      Top             =   1800
      Width           =   1815
   End
   Begin VB.Label Label2 
      Caption         =   "Database"
      Height          =   255
      Left            =   240
      TabIndex        =   3
      Top             =   960
      Width           =   1215
   End
   Begin VB.Label Label1 
      Caption         =   "CORBA IOR of DBManager Object"
      Height          =   255
      Left            =   240
      TabIndex        =   2
      Top             =   120
      Width           =   3015
   End
End
Attribute VB_Name = "fLogIn"
Attribute VB_GlobalNameSpace = False
Attribute VB_Creatable = False
Attribute VB_PredeclaredId = True
Attribute VB_Exposed = False
Option Explicit

Private Sub Form_Load()
    On Error GoTo ErrHandler
    Call mGlobal.initOrb
    Exit Sub
ErrHandler:
    Call MsgBox("Error " & CStr(Err.Number) & vbCrLf & Err.Source & vbCrLf _
        & Err.Description)
End Sub

Private Sub Form_Unload(Cancel As Integer)
    Call mGlobal.termOrb
End Sub

Private Sub cmdExit_Click()
    End
End Sub

Private Sub cmdShutdownServer_Click()
    On Error GoTo ErrHandler
    Call mGlobal.initDBManager(tbIOR.Text)
    Call mGlobal.shutdownServer(tbUser.Text, tbPassword.Text)
    Exit Sub
ErrHandler:
    Call MsgBox("Error " & CStr(Err.Number) & vbCrLf & Err.Source & vbCrLf _
        & Err.Description)
End Sub

Private Sub cmdLogIn_Click()
    On Error GoTo ErrHandler
    Call mGlobal.initDBManager(tbIOR.Text)
    Dim oWarns As c_DBMessageSeq
    Dim oEx As VBOrb.cOrbException
    Call mGlobal.openDBSession(oEx, tbDBUrl.Text, tbUser.Text, _
        tbPassword.Text, oWarns)
    If Not oEx Is Nothing Then GoTo ExHandler
    
    Call fQuery.Show
    Call fQuery.clearResult
    Call fQuery.printWarnings(oWarns)
    
    Call Me.Hide
    'Unload Me 'Destroy field contents
    
    Exit Sub
ExHandler:
    Call mGlobal.showExeptionBox(oEx)
    Exit Sub
ErrHandler:
    If VBOrb.ErrIsUserEx() Then Resume ExHandler
    Call VBOrb.ErrMsgBox(Err, "LogIn.Click")
End Sub

