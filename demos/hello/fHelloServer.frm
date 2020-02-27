VERSION 5.00
Begin VB.Form fHelloServer 
   Caption         =   "HelloServer"
   ClientHeight    =   3960
   ClientLeft      =   60
   ClientTop       =   345
   ClientWidth     =   5415
   LinkTopic       =   "Form1"
   ScaleHeight     =   3960
   ScaleWidth      =   5415
   StartUpPosition =   3  'Windows-Standard
   Begin VB.TextBox tbResult 
      Enabled         =   0   'False
      Height          =   1815
      Left            =   120
      MultiLine       =   -1  'True
      ScrollBars      =   2  'Vertikal
      TabIndex        =   5
      Top             =   2040
      Width           =   5175
   End
   Begin VB.Frame Frame1 
      Caption         =   "Start Server"
      Height          =   1815
      Left            =   120
      TabIndex        =   0
      Top             =   120
      Width           =   5175
      Begin VB.ComboBox tbNameServiceURL 
         Height          =   315
         ItemData        =   "fHelloServer.frx":0000
         Left            =   240
         List            =   "fHelloServer.frx":0002
         Style           =   1  'Einfaches Kombinationsfeld
         TabIndex        =   6
         Text            =   "corbaloc::localhost:2809/NameService"
         Top             =   600
         Width           =   4815
      End
      Begin VB.CommandButton cmdStopServer 
         Caption         =   "Stop"
         Enabled         =   0   'False
         Height          =   375
         Left            =   3840
         TabIndex        =   4
         Top             =   1320
         Width           =   975
      End
      Begin VB.ComboBox tbOAPort 
         Height          =   315
         ItemData        =   "fHelloServer.frx":0004
         Left            =   240
         List            =   "fHelloServer.frx":0006
         Style           =   1  'Einfaches Kombinationsfeld
         TabIndex        =   2
         Text            =   "1900"
         Top             =   1320
         Width           =   855
      End
      Begin VB.CommandButton cmdStartServer 
         Caption         =   "Start"
         Height          =   375
         Left            =   2640
         TabIndex        =   1
         Top             =   1320
         Width           =   975
      End
      Begin VB.Label Label1 
         Caption         =   "NameServiceLocation:"
         Height          =   255
         Left            =   240
         TabIndex        =   7
         Top             =   360
         Width           =   2295
      End
      Begin VB.Label Label2 
         Caption         =   "ObjectAdapterPortNumber:"
         Height          =   255
         Left            =   240
         TabIndex        =   3
         Top             =   1080
         Width           =   2295
      End
   End
End
Attribute VB_Name = "fHelloServer"
Attribute VB_GlobalNameSpace = False
Attribute VB_Creatable = False
Attribute VB_PredeclaredId = True
Attribute VB_Exposed = False
Option Explicit

Dim sLogFile As String
Dim oOrb As cOrbImpl
Dim oHelloImplTie As c_HelloAppHelloImplTie

Implements c_HelloAppHello

Private Sub cmdStartServer_Click()
    On Error GoTo ErrHandler
    Call clearResult
    cmdStartServer.Enabled = False
    tbNameServiceURL.Enabled = False
    tbOAPort.Enabled = False
    
    'Writing start message to logfile
    sLogFile = App.Path & "\HelloServer.log"
    Call VBOrb.logMsg(sLogFile, "Starting Server")
    'Get an ORB
    Set oOrb = VBOrb.init(ORBId:="", _
        ORBInitRef:="NameService=" & tbNameServiceURL.Text, _
        OAPort:=tbOAPort.Text, _
        OAVersion:=&H101, _
        LogFile:=sLogFile, loglevel:=&H28)
    Call printLine("Writing Log to: " & sLogFile)
    'Create an object and connect the object to the ORB.
    Set oHelloImplTie = New c_HelloAppHelloImplTie
    Call oHelloImplTie.setDelegate(Me)
    Call oOrb.Connect(oHelloImplTie, _
        "ExtraLongNameToAvoidJavaOrbBugInJDK_1.1,1.2,1.3")

    'Bind server object to NameService
    Dim oObject As cOrbObject
    Set oObject = oOrb.resolveInitialReferences("NameService")
    Dim oNmRootContext As c_NmContext
    Set oNmRootContext = m_NmContext.narrow(oObject)
    Dim oNmPath As c_NmNameComponentSeq
    'Set oNmPath = oNmRootContextExt.toName("Hello")
    Set oNmPath = New c_NmNameComponentSeq
    oNmPath.Length = 1
    Set oNmPath.Item(0) = New c_NmNameComponent
    oNmPath.Item(0).id = "Hello"
    oNmPath.Item(0).kind = ""
    Call oNmRootContext.rebind(oNmPath, oHelloImplTie.This)
    
    'Enable Stop button
    tbResult.Enabled = True
    cmdStopServer.Enabled = True
    
    'If you like to show forms here before calling oOrb.run() you must call
    'Show 0 instead of Show 1 because Show 0 does not wait.
    
    'Following call blocks and keep the ORB running until oOrb.shutdown()
    'is called in Form_Unload(). Instead of calling OrbRunLoopOutsideOfDLL()
    'or oOrb.run() you can call oOrb.performWork() by a timer.
    Call OrbRunLoopOutsideOfDLL
    
    Exit Sub
StartRollback:
    On Error Resume Next
    Call oOrb.destroy
    Set oOrb = Nothing
    cmdStartServer.Enabled = True
    tbNameServiceURL.Enabled = True
    tbOAPort.Enabled = True
    On Error GoTo 0
    Call VBOrb.ErrLoad
    Call VBOrb.ErrMsgBox(Err, "HelloServer")
    Exit Sub
ErrHandler:
    Call VBOrb.ErrSave(Err)
    Resume StartRollback
End Sub

'For debugging purposes:
'If you call oOrb.run() and you stop the application during debugging without
'calling oOrb.shutdown() (e.g. in Form.Unload) the VB environment is hanging
'inside the VBOrb-DLL still waiting for requests. To avoid that behavior
'during debugging please call OrbLoopOutsideOfDLL() instead of oOrb.run().
Public Sub OrbRunLoopOutsideOfDLL()
    On Error GoTo ErrHandler
    Do
        Call oOrb.performWork(10)
        DoEvents 'Prevent blocking other window processes
    Loop
ORBisDown:
    Set oOrb = Nothing
    Exit Sub
ErrHandler:
    If VBOrb.ErrIsSystemEx() _
        And Err.Number = (VBOrb.ITF_E_BAD_INV_ORDER_NO Or vbObjectError) Then
        Resume ORBisDown
    End If
    Call VBOrb.ErrReraise(Err, "OrbRunLoopOutsideOfDLL")
End Sub

Private Sub cmdStopServer_Click()
    cmdStopServer.Enabled = False
    tbResult.Enabled = False
    If Not oOrb Is Nothing Then
        Call oOrb.shutdown(True)
    End If
    tbNameServiceURL.Enabled = True
    tbOAPort.Enabled = True
    cmdStartServer.Enabled = True
End Sub

Private Sub Form_Unload(Cancel As Integer)
    'Avoid unvisible background process:
    'If oOrb.run() or OrbRunLoopOutsideOfDLL() is called and the user close all
    'visible forms the application process do not stop. The ORB is still waiting
    'for incoming requests until oOrb.shutdown() is called. To avoid this kind
    'of background process call oOrb.shutdown() if the user close the last form.
    If Not oOrb Is Nothing Then Call oOrb.shutdown(False)
End Sub

Private Function c_HelloAppHello_sayHello() As String
    Dim sLogMessage As String
    sLogMessage = "Client has called sayHello()"
    
    c_HelloAppHello_sayHello = "HELLO"
    
    'Log to dialog and file
    Call printLine(sLogMessage)
    Call VBOrb.logMsg(sLogFile, sLogMessage)
End Function

Public Sub clearResult()
    tbResult.Text = ""
End Sub

Public Sub printLine(ByVal sLine As String)
    tbResult.Text = tbResult.Text & sLine & vbCrLf
End Sub

