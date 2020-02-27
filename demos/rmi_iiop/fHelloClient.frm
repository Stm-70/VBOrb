VERSION 5.00
Begin VB.Form fHelloClient 
   Caption         =   "Hello Client using RMI/IIOP"
   ClientHeight    =   3360
   ClientLeft      =   60
   ClientTop       =   345
   ClientWidth     =   6315
   LinkTopic       =   "Form1"
   ScaleHeight     =   3360
   ScaleWidth      =   6315
   StartUpPosition =   3  'Windows-Standard
   Begin VB.CommandButton cmdSetWchar 
      Caption         =   "Set Wide Char"
      Height          =   375
      Left            =   1680
      TabIndex        =   11
      Top             =   1080
      Width           =   1455
   End
   Begin VB.CommandButton cmdGetWchar 
      Caption         =   "Get WideChar"
      Height          =   375
      Left            =   1680
      TabIndex        =   10
      Top             =   600
      Width           =   1455
   End
   Begin VB.CommandButton cmdSetHashMap 
      Caption         =   "Set HashMap"
      Height          =   375
      Left            =   4800
      TabIndex        =   9
      Top             =   1080
      Width           =   1455
   End
   Begin VB.CommandButton cmdSetWstring 
      Caption         =   "Set WideString"
      Height          =   375
      Left            =   3240
      TabIndex        =   8
      Top             =   1080
      Width           =   1455
   End
   Begin VB.CommandButton cmdGetWstring 
      Caption         =   "Get WideString"
      Height          =   375
      Left            =   3240
      TabIndex        =   7
      Top             =   600
      Width           =   1455
   End
   Begin VB.CommandButton cmdSetInteger 
      Caption         =   "Set Integer"
      Height          =   375
      Left            =   120
      TabIndex        =   6
      Top             =   1080
      Width           =   1455
   End
   Begin VB.CommandButton cmdGetInteger 
      Caption         =   "Get Integer"
      Height          =   375
      Left            =   120
      TabIndex        =   5
      Top             =   600
      Width           =   1455
   End
   Begin VB.CommandButton cmdGetHashMap 
      Caption         =   "Get HashMap"
      Height          =   375
      Left            =   4800
      TabIndex        =   4
      Top             =   600
      Width           =   1455
   End
   Begin VB.TextBox tbResult 
      Height          =   1695
      Left            =   120
      MultiLine       =   -1  'True
      ScrollBars      =   2  'Vertikal
      TabIndex        =   2
      Top             =   1560
      Width           =   6135
   End
   Begin VB.CommandButton cmdSayHello 
      Caption         =   "Say hello"
      Height          =   375
      Left            =   4800
      TabIndex        =   1
      Top             =   120
      Width           =   1455
   End
   Begin VB.ComboBox tbURL 
      Height          =   315
      ItemData        =   "fHelloClient.frx":0000
      Left            =   720
      List            =   "fHelloClient.frx":0002
      Style           =   1  'Einfaches Kombinationsfeld
      TabIndex        =   0
      Text            =   "corbaname::localhost:2809#HelloService"
      Top             =   120
      Width           =   3855
   End
   Begin VB.Label Label2 
      Caption         =   "URL:"
      Height          =   255
      Left            =   120
      TabIndex        =   3
      Top             =   120
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

Private Sub Form_Load()
    On Error GoTo ErrHandler
    'Get an ORB
    Set oOrb = VBOrb.init()
    Call oOrb.registerValueFactory(m_javalangInteger.TypeId, _
        New c_javalangIntegerVF)
    Call oOrb.registerValueFactory("RMI:[B:0000000000000000", _
        New c_ByteSeqVF)
    '??? Missing a module containing the type id for WStringValue
    Call oOrb.registerValueFactory("IDL:omg.org/CORBA/WStringValue:1.0", _
        New cCBWStringValueVF)
    Call oOrb.registerValueFactory(m_javautilHashMap.TypeId, _
        New c_javautilHashMapVF)
    Call printLine("You will get an error message" & vbCrLf & _
        "if the hello server or the name service are not running.")
    Exit Sub
ErrHandler:
    Call VBOrb.ErrMsgBox(Err, "Form_Load")
End Sub

Private Sub cmdSayHello_Click()
    On Error GoTo ErrHandler
    Call clearResult
    Dim oHelloServer As c_HelloAppHelloInterface
    Set oHelloServer = m_HelloAppHelloInterface.narrow( _
        oOrb.stringToObject(tbURL.Text))
    Call oHelloServer.sayHello
    Call printLine("sayHello() is called successfully")
    Exit Sub
ErrHandler:
    Call VBOrb.ErrMsgBox(Err, "Form_Load")
End Sub

Private Sub cmdGetInteger_Click()
    On Error GoTo ErrHandler
    Call clearResult
    Dim oHelloServer As c_HelloAppHelloInterface
    Set oHelloServer = m_HelloAppHelloInterface.narrow( _
        oOrb.stringToObject(tbURL.Text))
    Dim oJavaInt As c_javalangInteger
    Set oJavaInt = oHelloServer.integerObj
    Call printLine("getInteger() is called")
    If oJavaInt Is Nothing Then
        Call printLine("value= null")
    Else
        Call printLine("value= " & CStr(oJavaInt.value))
    End If
    Exit Sub
ErrHandler:
    Call VBOrb.ErrMsgBox(Err, "GetInteger")
End Sub

Private Sub cmdSetInteger_Click()
    On Error GoTo ErrHandler
    Call clearResult
    Dim oHelloServer As c_HelloAppHelloInterface
    Set oHelloServer = m_HelloAppHelloInterface.narrow( _
        oOrb.stringToObject(tbURL.Text))
    Dim oJavaInt As c_javalangInteger
    Set oJavaInt = New c_javalangInteger
    oJavaInt.value = Int(1000 * Rnd + 1)
    Call printLine("value= " & CStr(oJavaInt.value))
    Set oHelloServer.integerObj = oJavaInt
    Call printLine("setInteger() is called successfully")
    Exit Sub
ErrHandler:
    Call VBOrb.ErrMsgBox(Err, "SetInteger")
End Sub

Private Sub cmdGetWchar_Click()
    On Error GoTo ErrHandler
    Call clearResult
    Dim oHelloServer As c_HelloAppHelloInterface
    Set oHelloServer = m_HelloAppHelloInterface.narrow( _
        oOrb.stringToObject(tbURL.Text))
    Dim iCh As Integer
    iCh = oHelloServer.char
    Call printLine("getChar() is called")
    Call printLine("value= " & Hex(iCh))
    Exit Sub
ErrHandler:
    Call VBOrb.ErrMsgBox(Err, "GetWchar")
End Sub

Private Sub cmdSetWchar_Click()
    On Error GoTo ErrHandler
    Call clearResult
    Dim oHelloServer As c_HelloAppHelloInterface
    Set oHelloServer = m_HelloAppHelloInterface.narrow( _
        oOrb.stringToObject(tbURL.Text))
    Dim iCh As Integer
    iCh = CInt("&h" & Hex(CLng(&HFFFF& * Rnd + 1)))
    Call printLine("value= " & Hex(iCh))
    oHelloServer.char = iCh
    Call printLine("setChar() is called successfully")
    Exit Sub
ErrHandler:
    Call VBOrb.ErrMsgBox(Err, "SetWchar")
End Sub

Private Sub cmdGetWstring_Click()
    On Error GoTo ErrHandler
    Call clearResult
    Dim oHelloServer As c_HelloAppHelloInterface
    Set oHelloServer = m_HelloAppHelloInterface.narrow( _
        oOrb.stringToObject(tbURL.Text))
    Dim oJavaStr As cCBWStringValue
    Set oJavaStr = oHelloServer.stringProp
    Call printLine("getWstring() is called")
    If oJavaStr Is Nothing Then
        Call printLine("value= null")
    Else
        Call printLine("value= """ & oJavaStr.value & """")
    End If
    Exit Sub
ErrHandler:
    Call VBOrb.ErrMsgBox(Err, "GetWstring")
End Sub

Private Sub cmdSetWstring_Click()
    On Error GoTo ErrHandler
    Call clearResult
    Dim oHelloServer As c_HelloAppHelloInterface
    Set oHelloServer = m_HelloAppHelloInterface.narrow( _
        oOrb.stringToObject(tbURL.Text))
    Dim oJavaStr As cCBWStringValue
    Set oJavaStr = New cCBWStringValue
    oJavaStr.value = "VisualBasic example string"
    If oJavaStr Is Nothing Then
        Call printLine("value= null")
    Else
        Call printLine("value= """ & oJavaStr.value & """")
    End If
    Set oHelloServer.stringProp = oJavaStr
    Call printLine("setWstring() is called successfully")
    Exit Sub
ErrHandler:
    Call VBOrb.ErrMsgBox(Err, "SetWstring")
End Sub

Private Sub cmdGetHashMap_Click()
    On Error GoTo ErrHandler
    Call clearResult
    Dim oHelloServer As c_HelloAppHelloInterface
    Set oHelloServer = m_HelloAppHelloInterface.narrow( _
        oOrb.stringToObject(tbURL.Text))
    Dim oHM As c_javautilHashMap
    Dim i As Integer
    For i = 0 To 5
        Set oHM = oHelloServer.getHashMap(i)
        Call printLine("getHashMap(" & CStr(i) & ") is called")
        If oHM Is Nothing Then
            Call printLine("hashMap= null")
        Else
            Call printLine("hashMap.size= " & CStr(oHM.lSize))
        End If
    Next i
    Exit Sub
ErrHandler:
    Call VBOrb.ErrMsgBox(Err, "GetHashMap")
End Sub

Private Sub cmdSetHashMap_Click()
    On Error GoTo ErrHandler
    Call clearResult
    Dim oHelloServer As c_HelloAppHelloInterface
    Set oHelloServer = m_HelloAppHelloInterface.narrow( _
        oOrb.stringToObject(tbURL.Text))
    Dim oHM As c_javautilHashMap
    Set oHM = New c_javautilHashMap
    Call oHM.initToEmpty
    Call printLine("hashMap.size= " & CStr(oHM.lSize))
    Call oHelloServer.setHashMap(oHM)
    Call printLine("setHashMap() is called successfully")
    Exit Sub
ErrHandler:
    Call VBOrb.ErrMsgBox(Err, "SetHashMap")
End Sub

Public Sub clearResult()
    tbResult.Text = ""
End Sub

Public Sub printLine(ByVal sLine As String)
    tbResult.Text = tbResult.Text & sLine & vbCrLf
End Sub


