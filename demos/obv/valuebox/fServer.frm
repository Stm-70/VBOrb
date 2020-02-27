VERSION 5.00
Begin VB.Form fServer 
   Caption         =   "OBV Server"
   ClientHeight    =   4800
   ClientLeft      =   60
   ClientTop       =   345
   ClientWidth     =   5235
   LinkTopic       =   "Form1"
   ScaleHeight     =   4800
   ScaleWidth      =   5235
   StartUpPosition =   3  'Windows-Standard
   Begin VB.Frame Frame2 
      Caption         =   "Calling Client"
      Height          =   1455
      Left            =   0
      TabIndex        =   6
      Top             =   3240
      Width           =   5175
      Begin VB.CommandButton cmdValuebox 
         Caption         =   "send valuebox"
         Height          =   495
         Left            =   120
         TabIndex        =   8
         Top             =   840
         Width           =   1575
      End
      Begin VB.TextBox tbIOR 
         Height          =   375
         Left            =   120
         TabIndex        =   7
         Text            =   "IOR:?"
         Top             =   240
         Width           =   4935
      End
   End
   Begin VB.CommandButton cmdClearRes 
      Caption         =   "Clear Result"
      Height          =   495
      Left            =   3480
      TabIndex        =   5
      Top             =   720
      Width           =   975
   End
   Begin VB.Frame Frame1 
      Caption         =   "Start Server"
      Height          =   1215
      Left            =   0
      TabIndex        =   1
      Top             =   0
      Width           =   2535
      Begin VB.ComboBox tbOAPort 
         Height          =   315
         ItemData        =   "fServer.frx":0000
         Left            =   240
         List            =   "fServer.frx":0002
         Style           =   1  'Einfaches Kombinationsfeld
         TabIndex        =   3
         Text            =   "1900"
         Top             =   600
         Width           =   855
      End
      Begin VB.CommandButton cmdStartServer 
         Caption         =   "Start"
         Height          =   375
         Left            =   1320
         TabIndex        =   2
         Top             =   600
         Width           =   975
      End
      Begin VB.Label Label2 
         Caption         =   "OAPort:"
         Height          =   255
         Left            =   240
         TabIndex        =   4
         Top             =   360
         Width           =   615
      End
   End
   Begin VB.TextBox tbResult 
      Enabled         =   0   'False
      Height          =   1815
      Left            =   0
      MultiLine       =   -1  'True
      ScrollBars      =   2  'Vertikal
      TabIndex        =   0
      Top             =   1320
      Width           =   5175
   End
End
Attribute VB_Name = "fServer"
Attribute VB_GlobalNameSpace = False
Attribute VB_Creatable = False
Attribute VB_PredeclaredId = True
Attribute VB_Exposed = False
Option Explicit

Dim oOrb As cOrbImpl
Dim oValueExchangeImplTie As c_obvvalueboxIValueExchangeImplTie

Implements c_obvvalueboxIValueExchange

Private Sub ORBClientStart()
    On Error GoTo ErrHandler
    'Writing start message to logfile
    Dim sLogFile As String
    sLogFile = App.Path & "\OBVClient.log"
    Call VBOrb.logMsg(sLogFile, "Starting Client")
    'Get an ORB
    Set oOrb = VBOrb.init(ORBId:="", OAPort:=tbOAPort.Text, OAVersion:=&H102, _
        LogFile:=sLogFile)
    
    Exit Sub
ErrHandler:
    Call VBOrb.ErrMsgBox(Err, "ORBClientStart")
End Sub

Private Sub cmdStartServer_Click()
    On Error GoTo ErrHandler
    'Writing start message to logfile
    Dim sLogFile As String
    sLogFile = App.Path & "\OBVServer.log"
    Call VBOrb.logMsg(sLogFile, "Starting Server")
    'Get an ORB
    Set oOrb = VBOrb.init(ORBId:="", OAPort:=tbOAPort.Text, LogFile:=sLogFile)
    'Create an object and connect the object to the ORB.
    Set oValueExchangeImplTie = New c_obvvalueboxIValueExchangeImplTie
    Call oValueExchangeImplTie.setDelegate(Me)
    Call oOrb.Connect(oValueExchangeImplTie, "ValueExchange")

    cmdStartServer.Enabled = False
    tbOAPort.Enabled = False
    tbResult.Enabled = True
    
    tbIOR.Text = oOrb.objectToString(oValueExchangeImplTie.ObjRef)
    Call printIOR
    
    'If you like to show forms here before calling oOrb.run() you must call
    'Show 0 instead of Show 1 because Show 0 does not wait.
    
    'Following call blocks and keep the ORB running until oOrb.shutdown()
    'is called in Form_Unload(). Instead of calling OrbRunLoopOutsideOfDLL()
    'or oOrb.run() you can call oOrb.performWork() by a timer.
    Call OrbRunLoopOutsideOfDLL
    
    Exit Sub
ErrHandler:
    Call VBOrb.ErrMsgBox(Err, "ORBServerStart")
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

Public Sub printIOR()
     Call printLine(oOrb.objectToString(oValueExchangeImplTie.ObjRef))
End Sub

Private Sub cmdClearRes_Click()
    Call clearResult
End Sub

Private Sub c_obvvalueboxIValueExchange_sendLongBox(ByVal box As c_obvvalueboxlongBox)
    If box Is Nothing Then
        Call printLine("box is Nothing")
    Else
        Call printLine("box is " & CStr(box.Value))
    End If
End Sub

Private Sub c_obvvalueboxIValueExchange_sendLongSeqBox(ByVal box As c_obvvalueboxlongSeqBox)
    If box Is Nothing Then
        Call printLine("box is Nothing")
    Else
        Call printLine("box, length= " & CStr(box.Value.Length))
        Dim i As Long
        For i = 0 To box.Value.Length - 1
            Call printLine("box[" & CStr(i) & "] is " _
                & CStr(box.Value.Item(i)))
        Next i
    End If
End Sub

Public Sub clearResult()
    tbResult.Text = ""
End Sub

Public Sub printLine(ByVal sLine As String)
    tbResult.Text = tbResult.Text & sLine & vbCrLf
End Sub

Private Sub cmdValuebox_Click()
    On Error GoTo ErrHandler
    If oOrb Is Nothing Then Call ORBClientStart
    If oOrb Is Nothing Then Exit Sub
    Dim oValExchange As c_obvvalueboxIValueExchange
    Set oValExchange = m_obvvalueboxIValueExchange.narrow(oOrb.stringToObject(tbIOR.Text))
    Dim oBox As c_obvvalueboxlongBox
    Set oBox = New c_obvvalueboxlongBox
    oBox.Value = 79
    Call oValExchange.sendLongBox(Nothing)
    Call oValExchange.sendLongBox(oBox)
    Call oValExchange.sendLongSeqBox(Nothing)
    Dim oBoxSeq As c_obvvalueboxlongSeqBox
    Set oBoxSeq = New c_obvvalueboxlongSeqBox
    Set oBoxSeq.Value = New c_LongSeq
    oBoxSeq.Value.Length = 5
    oBoxSeq.Value.Item(3) = 3453
    Call oValExchange.sendLongSeqBox(oBoxSeq)
    
    Exit Sub
ErrHandler:
    Call VBOrb.ErrMsgBox(Err, "ORBClientStart")
End Sub
