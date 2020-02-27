VERSION 5.00
Begin VB.Form fHelloClient 
   Caption         =   "Hello Client"
   ClientHeight    =   2955
   ClientLeft      =   60
   ClientTop       =   345
   ClientWidth     =   6255
   LinkTopic       =   "Form1"
   ScaleHeight     =   2955
   ScaleWidth      =   6255
   StartUpPosition =   3  'Windows-Standard
   Begin VB.ComboBox tbURL 
      Height          =   315
      ItemData        =   "fHelloClient.frx":0000
      Left            =   840
      List            =   "fHelloClient.frx":0002
      Style           =   1  'Einfaches Kombinationsfeld
      TabIndex        =   2
      Text            =   "corbaname::localhost:2809#Hello"
      Top             =   360
      Width           =   3495
   End
   Begin VB.CommandButton cmdSayHello 
      Caption         =   "Say hello"
      Height          =   615
      Left            =   4560
      TabIndex        =   1
      Top             =   240
      Width           =   1455
   End
   Begin VB.TextBox tbResult 
      Height          =   1695
      Left            =   120
      MultiLine       =   -1  'True
      ScrollBars      =   2  'Vertikal
      TabIndex        =   0
      Top             =   1080
      Width           =   5895
   End
   Begin VB.Label Label2 
      Caption         =   "URL:"
      Height          =   255
      Left            =   240
      TabIndex        =   3
      Top             =   360
      Width           =   495
   End
End
Attribute VB_Name = "fHelloClient"
Attribute VB_GlobalNameSpace = False
Attribute VB_Creatable = False
Attribute VB_PredeclaredId = True
Attribute VB_Exposed = False
Option Explicit

Dim oOrb As cOrbImpl

Private Function getIniFile() As String
    getIniFile = App.Path & "\HelloClient.ini"
End Function

Private Sub Form_Load()
    Dim inVar As Variant
    Dim iFileNo As Integer
    iFileNo = FreeFile
    On Error GoTo skipIniFile
    Open getIniFile() For Input As #iFileNo
    Input #iFileNo, inVar: tbURL.Text = inVar
skipIniFile:
    On Error Resume Next
    Close #iFileNo
    'On Error GoTo 0
    On Error GoTo ErrHandler
    
    Dim sLogFile As String
    sLogFile = App.Path & "\HelloClient.log"
    Call VBOrb.logMsg(sLogFile, "Starting HelloClient")
    'Get an ORB
    Set oOrb = VBOrb.init(LogFile:=sLogFile, loglevel:=&H28)
    Call printLine("You will get an error message" & vbCrLf & _
        "if the hello server or the name service are not running.")
    Exit Sub
ErrHandler:
    Call VBOrb.ErrMsgBox(Err, "Form_Load")
End Sub

Private Sub Form_Unload(Cancel As Integer)
    Dim iFileNo As Integer
    iFileNo = FreeFile
    On Error GoTo skipIniFile
    Open getIniFile() For Output As #iFileNo
    Write #iFileNo, tbURL.Text
skipIniFile:
    On Error Resume Next
    Close #iFileNo
End Sub

Private Sub cmdSayHello_Click()
    On Error GoTo ErrHandler
    Dim oHelloServer As c_HelloAppHello
    Set oHelloServer = m_HelloAppHello.narrow( _
        oOrb.stringToObject(tbURL.Text))
    Call printLine(oHelloServer.sayHello())
    Exit Sub
ErrHandler:
    Call VBOrb.ErrMsgBox(Err, "Form_Load")
End Sub

Public Sub clearResult()
    tbResult.Text = ""
End Sub

Public Sub printLine(ByVal sLine As String)
    tbResult.Text = tbResult.Text & sLine & vbCrLf
End Sub

