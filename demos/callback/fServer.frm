VERSION 5.00
Begin VB.Form fServer 
   Caption         =   "CountServer"
   ClientHeight    =   3270
   ClientLeft      =   60
   ClientTop       =   345
   ClientWidth     =   5370
   LinkTopic       =   "Form1"
   ScaleHeight     =   3270
   ScaleWidth      =   5370
   StartUpPosition =   3  'Windows-Standard
   Begin VB.TextBox tbResult 
      Enabled         =   0   'False
      Height          =   1815
      Left            =   0
      MultiLine       =   -1  'True
      ScrollBars      =   2  'Vertikal
      TabIndex        =   6
      Top             =   1440
      Width           =   5175
   End
   Begin VB.CommandButton cmdStop 
      Caption         =   "Stop"
      Enabled         =   0   'False
      Height          =   495
      Left            =   4080
      TabIndex        =   1
      Top             =   600
      Width           =   975
   End
   Begin VB.CommandButton cmdStart 
      Caption         =   "Start"
      Enabled         =   0   'False
      Height          =   495
      Left            =   3000
      TabIndex        =   0
      Top             =   600
      Width           =   975
   End
   Begin VB.Frame Frame1 
      Caption         =   "Start Server"
      Height          =   1215
      Left            =   0
      TabIndex        =   2
      Top             =   120
      Width           =   2535
      Begin VB.CommandButton cmdStartServer 
         Caption         =   "Start"
         Height          =   375
         Left            =   1320
         TabIndex        =   5
         Top             =   600
         Width           =   975
      End
      Begin VB.ComboBox tbOAPort 
         Height          =   315
         ItemData        =   "fServer.frx":0000
         Left            =   240
         List            =   "fServer.frx":0002
         Style           =   1  'Einfaches Kombinationsfeld
         TabIndex        =   4
         Text            =   "1900"
         Top             =   600
         Width           =   855
      End
      Begin VB.Label Label2 
         Caption         =   "OAPort:"
         Height          =   255
         Left            =   240
         TabIndex        =   3
         Top             =   360
         Width           =   615
      End
   End
End
Attribute VB_Name = "fServer"
Attribute VB_GlobalNameSpace = False
Attribute VB_Creatable = False
Attribute VB_PredeclaredId = True
Attribute VB_Exposed = False
Option Explicit

Dim oOrb As cOrbImpl
Dim oCounterImplTie As c_ServerCounterImplTie
Dim oCoordinatorImplTie As c_ServerCoordinatorImplTie

Dim lSum As Long
Dim lClientId As Long
Dim oClients As New Collection

Implements c_ServerCoordinator
Implements c_ServerCounter

Private Sub cmdStartServer_Click()
    On Error GoTo ErrHandler
    'Writing start message to logfile
    Dim sLogFile As String
    sLogFile = App.Path & "\CBServer.log"
    Call VBOrb.logMsg(sLogFile, "Starting Server")
    'Get an ORB
    Set oOrb = VBOrb.init(ORBId:="", OAPort:=tbOAPort.Text, OAVersion:=&H101, _
        LogFile:=sLogFile, loglevel:=4)
    'Create an object and connect the object to the ORB.
    Set oCounterImplTie = New c_ServerCounterImplTie
    Call oCounterImplTie.setDelegate(Me)
    Call oOrb.Connect(oCounterImplTie)
    'Create an object and connect the object to the ORB.
    Set oCoordinatorImplTie = New c_ServerCoordinatorImplTie
    Call oCoordinatorImplTie.setDelegate(Me)
    Call oOrb.Connect(oCoordinatorImplTie, "Coordinator")

    cmdStartServer.Enabled = False
    tbOAPort.Enabled = False
    cmdStart.Enabled = True
    cmdStop.Enabled = True
    tbResult.Enabled = True
    
    'If you like to show forms here before calling oOrb.run() you must call
    'Show 0 instead of Show 1 because Show 0 does not wait.
    
    'Following call blocks and keep the ORB running until oOrb.shutdown()
    'is called in Form_Unload(). Instead of calling OrbRunLoopOutsideOfDLL()
    'or oOrb.run() you can call oOrb.performWork() by a timer.
    Call OrbRunLoopOutsideOfDLL
    
    Exit Sub
ErrHandler:
    Call VBOrb.ErrMsgBox(Err, "CountServer")
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

Private Sub Form_Unload(Cancel As Integer)
    'Avoid unvisible background process:
    'If oOrb.run() or OrbRunLoopOutsideOfDLL() is called and the user close all
    'visible forms the application process do not stop. The ORB is still waiting
    'for incoming requests until oOrb.shutdown() is called. To avoid this kind
    'of background process call oOrb.shutdown() if the user close the last form.
    If Not oOrb Is Nothing Then Call oOrb.shutdown(False)
End Sub

Private Function c_ServerCoordinator_register(ByVal clientObjRef As c_ClientControl) _
    As c_ServerCounter
    lClientId = lClientId + 1
    Call oClients.Add(clientObjRef, CStr(lClientId))
    Dim oCounter As c_ServerCounter
    Set oCounter = oCounterImplTie.This
    Set c_ServerCoordinator_register = oCounter
    Call printLine("Client " & CStr(lClientId) & " registered")
    'Callback to client
    Call clientObjRef.setId(lClientId)
    Call clientObjRef.sendWStringToClient("Hallo Client")
End Function

Private Sub c_ServerCoordinator_unregister(ByVal clientObjRef As c_ClientControl)
    Dim lClId As Long
    lClId = clientObjRef.getId()
    Call oClients.Remove(CStr(lClId))
    Call printLine("Client " & CStr(lClId) & " unregistered")
End Sub

Private Sub cmdStart_Click()
    Call clearResult
    Dim oClient As c_ClientControl
    For Each oClient In oClients
        On Error Resume Next
        Call oClient.start
        If Err.Number <> 0 Then
            Call printLine("Client Error: " & Err.Description)
        End If
        On Error GoTo 0
    Next
End Sub

Private Sub cmdStop_Click()
    Call clearResult
    Dim oClient As c_ClientControl
    For Each oClient In oClients
        On Error Resume Next
        Dim sCount As String
        sCount = oClient.stopFunc
        If Err.Number <> 0 Then
            Call printLine("Client Error: " & Err.Description)
        Else
            Call printLine("Client= " & sCount)
        End If
        On Error GoTo 0
    Next
    'Co-location to counter
    Dim oCounter As c_ServerCounter
    Set oCounter = oCounterImplTie.This
    Call printLine("Sum= " & CStr(oCounter.sum))
End Sub

Private Function c_ServerCounter_increment() As Long
    lSum = lSum + 1
    c_ServerCounter_sum = lSum
End Function

Private Property Let c_ServerCounter_sum(ByVal RHS As Long)
    lSum = c_ServerCounter_sum
End Property

Private Property Get c_ServerCounter_sum() As Long
    c_ServerCounter_sum = lSum
End Property

Public Sub clearResult()
    tbResult.Text = ""
End Sub

Public Sub printLine(ByVal sLine As String)
    tbResult.Text = tbResult.Text & sLine & vbCrLf
End Sub

