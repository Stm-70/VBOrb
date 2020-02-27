VERSION 5.00
Begin VB.Form fGridClient 
   Caption         =   "Grid Example Client"
   ClientHeight    =   4935
   ClientLeft      =   60
   ClientTop       =   345
   ClientWidth     =   5805
   LinkTopic       =   "Form1"
   ScaleHeight     =   4935
   ScaleWidth      =   5805
   StartUpPosition =   3  'Windows-Standard
   Begin VB.Frame Frame1 
      Caption         =   "Call MyServer"
      Height          =   1815
      Left            =   120
      TabIndex        =   8
      Top             =   1320
      Width           =   5535
      Begin VB.CommandButton cmdArrTst 
         Caption         =   "ArrayTest"
         Height          =   375
         Left            =   1560
         TabIndex        =   15
         Top             =   720
         Width           =   975
      End
      Begin VB.CommandButton cmdAnyTest 
         Caption         =   "AnyTest"
         Height          =   375
         Left            =   120
         TabIndex        =   14
         Top             =   1320
         Width           =   1335
      End
      Begin VB.CommandButton cmdOpWithEx 
         Caption         =   "opWithException()"
         Height          =   375
         Left            =   3720
         TabIndex        =   13
         Top             =   240
         Width           =   1695
      End
      Begin VB.CommandButton cmdNonExistent 
         Caption         =   "_non_existent()"
         Height          =   375
         Left            =   120
         TabIndex        =   12
         Top             =   240
         Width           =   1335
      End
      Begin VB.CommandButton cmdWidth 
         Caption         =   "Width()"
         Height          =   375
         Left            =   2640
         TabIndex        =   11
         Top             =   240
         Width           =   975
      End
      Begin VB.CommandButton cmdHeight 
         Caption         =   "Height()"
         Height          =   375
         Left            =   1560
         TabIndex        =   10
         Top             =   240
         Width           =   975
      End
      Begin VB.CommandButton cmdShutdownServer 
         Caption         =   "shutdownServer()"
         Height          =   375
         Left            =   3720
         TabIndex        =   9
         Top             =   1320
         Width           =   1695
      End
   End
   Begin VB.Frame frLogin 
      Caption         =   "URL of MyServer"
      Height          =   1095
      Left            =   120
      TabIndex        =   1
      Top             =   120
      Width           =   5535
      Begin VB.CommandButton cmdVersion 
         Caption         =   "Version"
         Height          =   375
         Left            =   1200
         TabIndex        =   7
         Top             =   600
         Width           =   975
      End
      Begin VB.CommandButton cmdAddress 
         Caption         =   "Address"
         Height          =   375
         Left            =   2280
         TabIndex        =   6
         Top             =   600
         Width           =   975
      End
      Begin VB.CommandButton cmdObjKey 
         Caption         =   "ObjectKey"
         Height          =   375
         Left            =   3360
         TabIndex        =   5
         Top             =   600
         Width           =   975
      End
      Begin VB.CommandButton cmdTypeId 
         Caption         =   "TypeId"
         Height          =   375
         Left            =   4440
         TabIndex        =   4
         Top             =   600
         Width           =   975
      End
      Begin VB.CommandButton cmdIOR 
         Caption         =   "IOR"
         Height          =   375
         Left            =   120
         TabIndex        =   3
         Top             =   600
         Width           =   855
      End
      Begin VB.TextBox tbLogin 
         Height          =   285
         Left            =   120
         TabIndex        =   2
         Text            =   "corbaloc::1.1@localhost:1900/MyServer"
         Top             =   240
         Width           =   5295
      End
   End
   Begin VB.TextBox txtOutput 
      Height          =   1575
      Left            =   120
      MultiLine       =   -1  'True
      ScrollBars      =   3  'Beides
      TabIndex        =   0
      Top             =   3240
      Width           =   5535
   End
End
Attribute VB_Name = "fGridClient"
Attribute VB_GlobalNameSpace = False
Attribute VB_Creatable = False
Attribute VB_PredeclaredId = True
Attribute VB_Exposed = False
'Copyright (c) 2002 Martin.Both

'This library is free software; you can redistribute it and/or
'modify it under the terms of the GNU Library General Public
'License as published by the Free Software Foundation; either
'version 2 of the License, or (at your option) any later version.

'This library is distributed in the hope that it will be useful,
'but WITHOUT ANY WARRANTY; without even the implied warranty of
'MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
'Library General Public License for more details.

Option Explicit

Private oOrb As cOrbImpl
Private oURLObject As cOrbObject
Private oMyServer As c_gridMyServer
Private oTestAny As c_TestAny
Private lineCnt As Integer

Private Sub Form_Load()
    On Error GoTo ErrHandler
    
    'Writing start message to logfile
    Dim sLogFile As String
    sLogFile = App.Path & "\GridClient.log"
    Call VBOrb.logMsg(sLogFile, "Starting Client")
    
    'Get an ORB with logging
    Set oOrb = VBOrb.init(LogFile:=sLogFile, loglevel:=&HFF)
    'Set oOrb = VBOrb.init(LogFile:=sLogFile)
    oOrb.RelativeRoundtripTimeout = 10000
    
    lineCnt = 0
    Call initURLObject(tbLogin.Text)
    Exit Sub
ErrHandler:
    Call MsgBox("Error " & CStr(Err.Number) & vbCrLf & Err.Source & vbCrLf _
        & Err.Description)
End Sub

Private Sub tbLogin_LostFocus()
    On Error GoTo ErrHandler
    Call initURLObject(tbLogin.Text)
    Exit Sub
ErrHandler:
    Call MsgBox("Error " & CStr(Err.Number) & vbCrLf & Err.Source & vbCrLf _
        & Err.Description)
End Sub

'Initialies the start object by a String
Private Sub initURLObject(ByVal sURL As String)
    On Error GoTo ErrHandler
    Set oURLObject = oOrb.stringToObject(sURL)
    Exit Sub
ErrHandler:
    Set oURLObject = Nothing
    Set oMyServer = Nothing
    Set oTestAny = Nothing
    Call VBOrb.ErrReraise(Err, "initURLObject")
End Sub

'Initialies the start object (oMyServer)
Private Sub initMyServer()
    On Error GoTo ErrHandler
    If oMyServer Is Nothing Then
        Set oMyServer = m_gridMyServer.narrow(oURLObject)
    End If
    Exit Sub
ErrHandler:
    Set oMyServer = Nothing
    Call VBOrb.ErrReraise(Err, "initMyServer")
End Sub

'Initialies the start object (oMyServer)
Private Sub initTestAny()
    On Error GoTo ErrHandler
    If oTestAny Is Nothing Then
        'oObjRef.TypeId = "IDL:TestAny:1.0"
        Set oTestAny = m_TestAny.uncheckedNarrow(oURLObject)
    End If
    Exit Sub
ErrHandler:
    Set oTestAny = Nothing
    Call VBOrb.ErrReraise(Err, "initTestAny")
End Sub

'Display a message
Private Sub printLine(ByVal sLine As String)
    If lineCnt >= 6 Then
        lineCnt = 0
        txtOutput.Text = ""
    End If
    lineCnt = lineCnt + 1
    txtOutput.Text = txtOutput.Text & sLine & vbCrLf
End Sub

'Build an IOR string
Private Sub cmdIOR_Click()
    On Error GoTo ErrHandler
    If oURLObject Is Nothing Then
        Call MsgBox("No valid object IOR")
        Exit Sub
    End If
    Dim sIOR As String
    sIOR = oOrb.objectToString(oURLObject)
    Call printLine(sIOR)
    Exit Sub
ErrHandler:
    Call MsgBox("Error " & CStr(Err.Number) & vbCrLf & Err.Source & vbCrLf _
        & Err.Description)
End Sub

'Extract the IIOP version
Private Sub cmdVersion_Click()
    On Error GoTo ErrHandler
    If oURLObject Is Nothing Then
        Call MsgBox("No valid object IOR")
        Exit Sub
    End If
    Call printLine("IIOP-Version= " & Hex$(oURLObject.getObjRef.IIOPVersion))
    Exit Sub
ErrHandler:
    Call MsgBox("Error " & CStr(Err.Number) & vbCrLf & Err.Source & vbCrLf _
        & Err.Description)
End Sub

'Extract the IP address (host and portno)
Private Sub cmdAddress_Click()
    On Error GoTo ErrHandler
    If oURLObject Is Nothing Then
        Call MsgBox("No valid object IOR")
        Exit Sub
    End If
    Call printLine("IIOP-Address= """ & oURLObject.getObjRef.IIOPAddress & """")
    Exit Sub
ErrHandler:
    Call MsgBox("Error " & CStr(Err.Number) & vbCrLf & Err.Source & vbCrLf _
        & Err.Description)
End Sub

'Extract the object key
Private Sub cmdObjKey_Click()
    On Error GoTo ErrHandler
    If oURLObject Is Nothing Then
        Call MsgBox("No valid object IOR")
        Exit Sub
    End If
    Call printLine("ObjectKey= """ & oURLObject.getObjRef.objectKey & """")
    Exit Sub
ErrHandler:
    Call MsgBox("Error " & CStr(Err.Number) & vbCrLf & Err.Source & vbCrLf _
        & Err.Description)
End Sub

'Extract the type ID
Private Sub cmdTypeId_Click()
    On Error GoTo ErrHandler
    If oURLObject Is Nothing Then
        Call MsgBox("No valid object IOR")
        Exit Sub
    End If
    Call printLine("TypeId= """ & oURLObject.getObjRef.TypeId & """")
    Exit Sub
ErrHandler:
    Call MsgBox("Error " & CStr(Err.Number) & vbCrLf & Err.Source & vbCrLf _
        & Err.Description)
End Sub

'Call NonExistent method of IOR
Private Sub cmdNonExistent_Click()
    On Error GoTo ErrHandler
    If oURLObject Is Nothing Then
        Call MsgBox("No valid object IOR")
        Exit Sub
    End If
    Dim result As Boolean
    result = oURLObject.getObjRef.nonExistent
    Call printLine(result)
    Exit Sub
ErrHandler:
    Call MsgBox("Error " & CStr(Err.Number) & vbCrLf & Err.Source & vbCrLf _
        & Err.Description)
End Sub

'Call a remote method to get the height of the grid
Private Sub cmdHeight_Click()
    On Error GoTo ErrHandler
    If oURLObject Is Nothing Then
        Call MsgBox("No valid object IOR")
        Exit Sub
    End If
    Call initMyServer
    Dim result As String
    result = "Height: " & oMyServer.height()
    Call printLine(result)
    Exit Sub
ErrHandler:
    Call MsgBox("Error " & CStr(Err.Number) & vbCrLf & Err.Source & vbCrLf _
        & Err.Description)
End Sub

'Call a remote method to get the width of the grid
Private Sub cmdWidth_Click()
    On Error GoTo ErrHandler
    If oURLObject Is Nothing Then
        Call MsgBox("No valid object IOR")
        Exit Sub
    End If
    Call initMyServer
    Dim result As String
    result = "Width: " & oMyServer.width()
    Call printLine(result)
    Exit Sub
ErrHandler:
    Call MsgBox("Error " & CStr(Err.Number) & vbCrLf & Err.Source & vbCrLf _
        & Err.Description)
End Sub

'Call a remote method to get a user exception
Private Sub cmdOpWithEx_Click()
    On Error GoTo ErrHandler
    If oURLObject Is Nothing Then
        Call MsgBox("No valid object IOR")
        Exit Sub
    End If
    Call initMyServer
    'Call a method returning a user exception
    Call oMyServer.opWithException
    Exit Sub
ExHandler:
    Dim oUserEx As VBOrb.cOrbException
    Set oUserEx = VBOrb.getException()
    Select Case TypeName(oUserEx)
    Case "c_gridMyServerMyException"
        Dim oMyServEx As c_gridMyServerMyException
        Set oMyServEx = oUserEx
        Call printLine(oUserEx.Description & ": " & oMyServEx.why)
    Case Else
        Call MsgBox("Error " & oUserEx.Source & vbCrLf & _
            oUserEx.Description)
        Exit Sub
    End Select
    Exit Sub
ErrHandler:
    If VBOrb.ErrIsUserEx() Then Resume ExHandler
    Call MsgBox("Error " & CStr(Err.Number) & vbCrLf & Err.Source & vbCrLf _
        & Err.Description)
End Sub

'Call a remote methods to test arrays
Private Sub cmdArrTst_Click()
    On Error GoTo ErrHandler
    If oURLObject Is Nothing Then
        Call MsgBox("No valid object IOR")
        Exit Sub
    End If
    Call initMyServer
    Dim oEx As New cOrbException
    Dim arr5 As New c_IntegerArr4Arr5
    Dim arr4 As c_IntegerArr4
    Dim i As Long
    For i = 0 To arr5.Length - 1
        Set arr4 = New c_IntegerArr4
        Dim j As Long
        For j = 0 To arr4.Length - 1
            arr4.Item(j) = i * 100 + j
        Next j
        Set arr5.Item(i) = arr4
    Next i
    Set oMyServer.test2() = arr5
    Set arr5 = oMyServer.test2()
    For i = 0 To arr5.Length - 1
        Set arr4 = arr5.Item(i)
        Dim result As String
        result = ""
        For j = 0 To arr4.Length - 1
            result = result & arr4.Item(j) & " "
        Next j
        Call printLine(result)
    Next i
    Exit Sub
ErrHandler:
    Call MsgBox("Error " & CStr(Err.Number) & vbCrLf & Err.Source & vbCrLf _
        & Err.Description)
End Sub

'Call a remote oneway method to shutdown the server
Private Sub cmdShutdownServer_Click()
    On Error GoTo ErrHandler
    If oURLObject Is Nothing Then
        Call MsgBox("No valid object IOR")
        Exit Sub
    End If
    Call initMyServer
    Call oMyServer.shutdownServer
    Exit Sub
ErrHandler:
    Call MsgBox("Error " & CStr(Err.Number) & vbCrLf & Err.Source & vbCrLf _
        & Err.Description)
End Sub

'Call a remote method to send and receive any types
Private Sub cmdAnyTest_Click()
    On Error GoTo ErrHandler
    If oURLObject Is Nothing Then
        Call MsgBox("No valid object IOR")
        Exit Sub
    End If
    Call initTestAny
    
    'Create any
    'To create the TypeCode you can use m_TestAnyStruct.TypeCode()
    'instead of the following.
    Dim oStructMemSeq As cCBStructMemberSeq
    Set oStructMemSeq = New cCBStructMemberSeq
    oStructMemSeq.Length = 2
    Set oStructMemSeq.Item(0) = New cCBStructMember
    oStructMemSeq.Item(0).Name = "m1"
    Set oStructMemSeq.Item(0).p_type = oOrb.createStringTc(0)
    Set oStructMemSeq.Item(1) = New cCBStructMember
    oStructMemSeq.Item(1).Name = "m2"
    Set oStructMemSeq.Item(1).p_type = oOrb.createPrimitiveTc(mCB.tk_long)
    '
    Dim oAny1 As cOrbAny
    Set oAny1 = New cOrbAny
    Call oAny1.initByDefaultValue(oOrb.createStructTc( _
        "IDL:TestStruct:1.0", "TestStruct", _
        oStructMemSeq))
    Call oAny1.insertString("Hallo")
    Call oAny1.nextPos
    Call oAny1.insertLong(123)
    
    'Call function
    Dim oAny2 As cOrbAny
    Set oAny2 = oTestAny.printFunc(oAny1)
    If oAny2.getOrigType.kind = mCB.tk_struct Then
        Call printLine("Any is a struct " & oAny2.getOrigType().id() & _
            ", " & oAny2.componentCount())
        Dim lcnt As Long
        For lcnt = 0 To oAny2.componentCount - 1
            Dim oMem As cOrbAny
            Set oMem = oAny2.currentComponent()
            If oMem.isString() Then
                Call printLine("Member " & oAny2.getOrigType().memberName(lcnt) & _
                    "= " & oMem.getString())
            Else
                Call printLine("Member " & oAny2.getOrigType().memberName(lcnt) & _
                    "= " & CStr(oMem.getLong()))
            End If
            Call oAny2.nextPos
        'Loop While oAny2.nextPos()
        Next lcnt
    Else
        Call printLine("Any is not a struct, " & oAny2.getOrigType().id)
    End If
    Exit Sub
ErrHandler:
    Call MsgBox("Error " & CStr(Err.Number) & vbCrLf & Err.Source & vbCrLf _
        & Err.Description)
End Sub

