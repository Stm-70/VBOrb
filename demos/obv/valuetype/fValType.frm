VERSION 5.00
Begin VB.Form frmValType 
   Caption         =   "Form1"
   ClientHeight    =   5445
   ClientLeft      =   60
   ClientTop       =   345
   ClientWidth     =   6420
   LinkTopic       =   "Form1"
   ScaleHeight     =   5445
   ScaleWidth      =   6420
   StartUpPosition =   3  'Windows-Standard
   Begin VB.CommandButton cmdTakeIOR4 
      Caption         =   "Take IOR of InfExc"
      Height          =   375
      Left            =   4560
      TabIndex        =   14
      Top             =   720
      Width           =   1695
   End
   Begin VB.CommandButton cmdTakeIOR3 
      Caption         =   "Take IOR of AbsExc"
      Height          =   375
      Left            =   4560
      TabIndex        =   13
      Top             =   240
      Width           =   1695
   End
   Begin VB.CommandButton cmdTakeIOR2 
      Caption         =   "Take IOR of ValExc"
      Height          =   375
      Left            =   2760
      TabIndex        =   12
      Top             =   720
      Width           =   1695
   End
   Begin VB.CommandButton cmdTakeIOR1 
      Caption         =   "Take IOR of Bank"
      Height          =   375
      Left            =   2760
      TabIndex        =   11
      Top             =   240
      Width           =   1695
   End
   Begin VB.TextBox tbResult 
      Enabled         =   0   'False
      Height          =   1815
      Left            =   0
      MultiLine       =   -1  'True
      ScrollBars      =   2  'Vertikal
      TabIndex        =   8
      Top             =   1320
      Width           =   6255
   End
   Begin VB.Frame Frame1 
      Caption         =   "Start Server"
      Height          =   1215
      Left            =   0
      TabIndex        =   4
      Top             =   0
      Width           =   2535
      Begin VB.CommandButton cmdStartServer 
         Caption         =   "Start"
         Height          =   375
         Left            =   1320
         TabIndex        =   6
         Top             =   600
         Width           =   975
      End
      Begin VB.ComboBox tbOAPort 
         Height          =   315
         ItemData        =   "fValType.frx":0000
         Left            =   240
         List            =   "fValType.frx":0002
         Style           =   1  'Einfaches Kombinationsfeld
         TabIndex        =   5
         Text            =   "1900"
         Top             =   600
         Width           =   855
      End
      Begin VB.Label Label2 
         Caption         =   "OAPort:"
         Height          =   255
         Left            =   240
         TabIndex        =   7
         Top             =   360
         Width           =   615
      End
   End
   Begin VB.CommandButton cmdClearRes 
      Caption         =   "Clear Results"
      Height          =   495
      Left            =   4800
      TabIndex        =   3
      Top             =   3240
      Width           =   1455
   End
   Begin VB.Frame Frame2 
      Caption         =   "Client calls"
      Height          =   1455
      Left            =   0
      TabIndex        =   0
      Top             =   3840
      Width           =   6255
      Begin VB.CommandButton cmdGetMessage 
         Caption         =   "getMessage"
         Height          =   495
         Left            =   4800
         TabIndex        =   15
         Top             =   840
         Width           =   1335
      End
      Begin VB.CommandButton cmdGetInterface 
         Caption         =   "getInterface"
         Height          =   495
         Left            =   3240
         TabIndex        =   10
         Top             =   840
         Width           =   1335
      End
      Begin VB.CommandButton cmdSendCs 
         Caption         =   "sendCustom"
         Height          =   495
         Left            =   1680
         TabIndex        =   9
         Top             =   840
         Width           =   1335
      End
      Begin VB.TextBox tbIOR 
         Height          =   375
         Left            =   120
         TabIndex        =   2
         Text            =   "IOR:?"
         Top             =   240
         Width           =   6015
      End
      Begin VB.CommandButton cmdCreateAcc 
         Caption         =   "create_account"
         Height          =   495
         Left            =   120
         TabIndex        =   1
         Top             =   840
         Width           =   1335
      End
   End
End
Attribute VB_Name = "frmValType"
Attribute VB_GlobalNameSpace = False
Attribute VB_Creatable = False
Attribute VB_PredeclaredId = True
Attribute VB_Exposed = False
Option Explicit

Dim oOrb As cOrbImpl
Dim oValTypeIBankImplTie As c_ValTypeIBankImplTie
Dim oValExchangeImplTie As c_CsValueExchangeImplTie
Dim oAbsValExchangeImplTie As c_SpAbsValueExchangeImplTie
Dim oAbsExampleImplTie As c_SpAbsExampleImplTie
Dim oInfValueExchangeImplTie As c_SpInfValueExchangeImplTie
Dim oSpInfMessageImplTie As c_SpInfMessageImplTie
Dim oInfvalueExample As c_SpInfvalueExample

Implements c_ValTypeIBank
Implements c_CsValueExchange
Implements c_SpAbsValueExchange
Implements c_SpAbsExample
Implements c_SpInfValueExchange

Private Sub cmdStartServer_Click()
    Call ORBStart
End Sub

Private Sub ORBStart()
    On Error GoTo ErrHandler
    'Writing start message to logfile
    Dim sLogFile As String
    sLogFile = App.Path & "\ValType.log"
    Call VBOrb.logMsg(sLogFile, "Starting Server")
    'Get an ORB
    Set oOrb = VBOrb.init(ORBId:="", OAPort:=tbOAPort.Text, LogFile:=sLogFile)
    'Create an object and connect the object to the ORB.
    Set oValTypeIBankImplTie = New c_ValTypeIBankImplTie
    Call oValTypeIBankImplTie.setDelegate(Me)
    Call oOrb.Connect(oValTypeIBankImplTie, "ValIBank")

    Set oValExchangeImplTie = New c_CsValueExchangeImplTie
    Call oValExchangeImplTie.setDelegate(Me)
    Call oOrb.Connect(oValExchangeImplTie, "ValExchange")

    Set oAbsValExchangeImplTie = New c_SpAbsValueExchangeImplTie
    Call oAbsValExchangeImplTie.setDelegate(Me)
    Call oOrb.Connect(oAbsValExchangeImplTie, "AbsValExchange")
    Set oAbsExampleImplTie = New c_SpAbsExampleImplTie
    Call oAbsExampleImplTie.setDelegate(Me)
    Call oOrb.Connect(oAbsExampleImplTie)

    Call oOrb.registerValueFactory(m_SpAbsvalueExample.TypeId, _
        New c_SpAbsvalueExampleVFactory)

    Set oInfValueExchangeImplTie = New c_SpInfValueExchangeImplTie
    Call oInfValueExchangeImplTie.setDelegate(Me)
    Call oOrb.Connect(oInfValueExchangeImplTie, "InfValExchange")
    Set oInfvalueExample = New c_SpInfvalueExample
    oInfvalueExample.name_state = "Hello"
    Set oSpInfMessageImplTie = New c_SpInfMessageImplTie
    Call oSpInfMessageImplTie.setDelegate(oInfvalueExample)
    Call oOrb.Connect(oSpInfMessageImplTie)

    cmdStartServer.Enabled = False
    tbOAPort.Enabled = False
    tbResult.Enabled = True
    
    'Call printLine(oOrb.objectToString(oValTypeIBankImpl.ObjRef))
    'Call printLine(oOrb.objectToString(oValExchangeImpl.ObjRef))
    'Call printLine(oOrb.objectToString(oAbsValExchangeImpl.ObjRef))
    
    'If you like to show forms here before calling oOrb.run() you must call
    'Show 0 instead of Show 1 because Show 0 does not wait.
    
    'Following call blocks and keep the ORB running until oOrb.shutdown()
    'is called in Form_Unload(). Instead of calling OrbRunLoopOutsideOfDLL()
    'or oOrb.run() you can call oOrb.performWork() by a timer.
    Call OrbRunLoopOutsideOfDLL
    
    Exit Sub
ErrHandler:
    Call VBOrb.ErrMsgBox(Err, "ORBStart")
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
        And Err.number = (VBOrb.ITF_E_BAD_INV_ORDER_NO Or vbObjectError) Then
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

Private Sub cmdTakeIOR1_Click()
    On Error GoTo ErrHandler
    If oOrb Is Nothing Then
        Call printLine("First call Start")
    Else
        tbIOR.Text = oOrb.objectToString(oValTypeIBankImplTie.ObjRef)
    End If
    Exit Sub
ErrHandler:
    Call printLine("Error: " & Err.Description)
End Sub

Private Sub cmdTakeIOR2_Click()
    On Error GoTo ErrHandler
    If oOrb Is Nothing Then
        Call printLine("First call Start")
    Else
        tbIOR.Text = oOrb.objectToString(oValExchangeImplTie.ObjRef)
    End If
    Exit Sub
ErrHandler:
    Call printLine("Error: " & Err.Description)
End Sub

Private Sub cmdTakeIOR3_Click()
    On Error GoTo ErrHandler
    If oOrb Is Nothing Then
        Call printLine("First call Start")
    Else
        tbIOR.Text = oOrb.objectToString(oAbsValExchangeImplTie.ObjRef)
    End If
    Exit Sub
ErrHandler:
    Call printLine("Error: " & Err.Description)
End Sub

Private Sub cmdTakeIOR4_Click()
    On Error GoTo ErrHandler
    If oOrb Is Nothing Then
        Call printLine("First call Start")
    Else
        tbIOR.Text = oOrb.objectToString(oInfValueExchangeImplTie.ObjRef)
    End If
    Exit Sub
ErrHandler:
    Call printLine("Error: " & Err.Description)
End Sub

Private Sub cmdClearRes_Click()
    Call clearResult
End Sub

Public Sub clearResult()
    tbResult.Text = ""
End Sub

Public Sub printLine(ByVal sLine As String)
    tbResult.Text = tbResult.Text & sLine & vbCrLf
End Sub

Private Function c_ValTypeIBank_createAccount(ByVal name As String, ByVal address As String, ByVal balance As Single) As c_ValTypeAccount
    Dim val As c_ValTypeAccount
    Set val = New c_ValTypeAccount
    Call val.init(name, address, balance)
    Call printLine("Account value created, balance = " & CStr(balance))
    Set c_ValTypeIBank_createAccount = val
End Function

Private Sub cmdCreateAcc_Click()
    On Error GoTo ErrHandler
    If oOrb Is Nothing Then
        Call printLine("First call Start")
        Exit Sub
    End If
    Dim oIBank As c_ValTypeIBank
    Set oIBank = m_ValTypeIBank.narrow(oOrb.stringToObject(tbIOR.Text))
    Dim oValAccount As c_ValTypeAccount
    Set oValAccount = oIBank.createAccount("Martin", "Str. 1, 33429 Minden", 3.14)
    Call printLine(oValAccount.address)
    Call oValAccount.credit(100#)
    Exit Sub
ErrHandler:
    Call VBOrb.ErrMsgBox(Err, "ValType")
End Sub

Private Sub c_CsValueExchange_sendValueExample(ByVal value As c_CsvalueExample)
    Call printLine(value.name_state)
    Call printLine("I received a value")
    Call printLine("")
    Call printLine("number = " & value.number())
    Call printLine("")
    Call value.printSub
End Sub

Private Sub cmdSendCs_Click()
    On Error GoTo ErrHandler
    If oOrb Is Nothing Then
        Call printLine("First call Start")
        Exit Sub
    End If
    Dim oValExchange As c_CsValueExchange
    Set oValExchange = m_CsValueExchange.narrow(oOrb.stringToObject(tbIOR.Text))
    
    Dim valueType As c_CsvalueExample
    Set valueType = New c_CsvalueExample
    valueType.name_state = "Example of a name_state hello"
    valueType.number_state = 100
    Call oValExchange.sendValueExample(valueType)
    
    Exit Sub
ErrHandler:
    Call VBOrb.ErrMsgBox(Err, "ValType")
End Sub

Private Function c_SpAbsValueExchange_getInterface(ByVal byValue As Boolean) As c_SpAbsAnAbstractInterface
    Set c_SpAbsValueExchange_getInterface = Nothing
    If byValue Then
        Dim oValue As c_SpAbsvalueExample
        Set oValue = New c_SpAbsvalueExample
        oValue.name_state = "Hello"
        'oValue implements c_SpAbsValueExchange
        Set c_SpAbsValueExchange_getInterface = oValue
    Else
        Dim oObjRef As c_SpAbsAnAbstractInterface
        Set oObjRef = m_SpAbsAnAbstractInterface.narrow(oAbsExampleImplTie.ObjRef)
        'Write ObjRef and TypeId of SpAbsExample
        Set c_SpAbsValueExchange_getInterface = oObjRef
    End If
End Function

Private Sub c_SpAbsExample_printSub()
    Call printLine(". I'm a remote operation of abstract example")
End Sub

Private Sub cmdGetInterface_Click()
    On Error GoTo ErrHandler
    If oOrb Is Nothing Then
        Call printLine("First call Start")
        Exit Sub
    End If
    Dim oAbsValExchange As c_SpAbsValueExchange
    Set oAbsValExchange = m_SpAbsValueExchange.narrow(oOrb.stringToObject(tbIOR.Text))
    
    Dim oAbsVal As c_SpAbsAnAbstractInterface
    Call printLine(". Ask for a object by reference")
    Set oAbsVal = oAbsValExchange.getInterface(False)
    Call oAbsVal.printSub
    Call printLine(". Ask for a object by value")
    Set oAbsVal = oAbsValExchange.getInterface(True)
    Call oAbsVal.printSub
    
    Exit Sub
ErrHandler:
    Call VBOrb.ErrMsgBox(Err, "ValType")
End Sub

Private Function c_SpInfValueExchange_getValueSupportedInterface() As c_SpInfMessage
    Set c_SpInfValueExchange_getValueSupportedInterface = oSpInfMessageImplTie.This
End Function

Private Function c_SpInfValueExchange_getValueExample() As c_SpInfvalueExample
    Set c_SpInfValueExchange_getValueExample = oInfvalueExample
End Function

Private Sub cmdGetMessage_Click()
    On Error GoTo ErrHandler
    If oOrb Is Nothing Then
        Call printLine("First call Start")
        Exit Sub
    End If
    Dim oInfValueExchange As c_SpInfValueExchange
    Set oInfValueExchange = m_SpInfValueExchange.narrow(oOrb.stringToObject(tbIOR.Text))
    
    Dim oInfMsg As c_SpInfMessage
    Call printLine(". Ask for a remote interface")
    Set oInfMsg = oInfValueExchange.getValueSupportedInterface()
    Call oInfMsg.printSub
    
    Call printLine(". Ask for a object by value supporting an interface")
    Set oInfMsg = oInfValueExchange.getValueExample()
    Call oInfMsg.printSub
    Dim oInfValue As c_SpInfvalueExample
    Set oInfValue = oInfMsg
    Call oInfValue.printName
    Exit Sub
ErrHandler:
    Call VBOrb.ErrMsgBox(Err, "ValType")
End Sub

