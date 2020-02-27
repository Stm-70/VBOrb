VERSION 5.00
Begin VB.Form fNameServ 
   Caption         =   "NameServ"
   ClientHeight    =   3375
   ClientLeft      =   60
   ClientTop       =   345
   ClientWidth     =   6120
   LinkTopic       =   "NameServ"
   ScaleHeight     =   3375
   ScaleWidth      =   6120
   StartUpPosition =   3  'Windows-Standard
   Begin VB.TextBox tbResult 
      Enabled         =   0   'False
      Height          =   1695
      Left            =   0
      MultiLine       =   -1  'True
      ScrollBars      =   2  'Vertikal
      TabIndex        =   4
      Top             =   1440
      Width           =   5895
   End
   Begin VB.Frame Frame1 
      Caption         =   "Start Server"
      Height          =   1215
      Left            =   0
      TabIndex        =   0
      Top             =   120
      Width           =   2535
      Begin VB.ComboBox tbOAPort 
         Height          =   315
         ItemData        =   "fNameServ.frx":0000
         Left            =   240
         List            =   "fNameServ.frx":0002
         Style           =   1  'Einfaches Kombinationsfeld
         TabIndex        =   2
         Text            =   "2809"
         Top             =   600
         Width           =   855
      End
      Begin VB.CommandButton cmdStartServer 
         Caption         =   "Start"
         Height          =   375
         Left            =   1320
         TabIndex        =   1
         Top             =   600
         Width           =   975
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
Attribute VB_Name = "fNameServ"
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

'First version was written by Craig Neuwirt

Option Explicit

Dim oOrb As cOrbImpl
Dim oSunInitRefImplTie As c_SunInitialReferencesImplTie

Public iRandom As Integer

Dim fNameContextRoot As fNameContext

Implements c_SunInitialReferences

Private Sub cmdStartServer_Click()
    On Error GoTo ErrHandler
    'Writing start message to logfile
    Dim sLogFile As String
    sLogFile = App.Path & "\NameServ.log"
    Call VBOrb.logMsg(sLogFile, "Starting Server")
    'Get an ORB
    Set oOrb = VBOrb.init(ORBId:="", OAPort:=tbOAPort.Text, LogFile:=sLogFile)
    'Create an object and connect the object to the ORB.
    Set oSunInitRefImplTie = New c_SunInitialReferencesImplTie
    Call oSunInitRefImplTie.setDelegate(Me)
    Call oOrb.Connect(oSunInitRefImplTie, "INIT")

    cmdStartServer.Enabled = False
    tbOAPort.Enabled = False
    tbResult.Enabled = True
    
    Call printLine("NameService is started")
    
    Set fNameContextRoot = New fNameContext
    Call fNameContextRoot.initMe(oOrb, True)
    Call fNameContextRoot.Show(0)
    
    Call printLine("Root Context is created")
    Call printLine("corbaloc:" & fNameContextRoot.getAddress())
    
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
    If Not fNameContextRoot Is Nothing Then
        If MsgBox("Exit NameService?", vbOKCancel) <> vbOK Then
            Cancel = True
            Exit Sub
        End If
        Call fNameContextRoot.Hide
        Call fNameContextRoot.termMe
        Unload fNameContextRoot
        Set fNameContextRoot = Nothing
    End If
    'Avoid unvisible background process:
    'If oOrb.run() or OrbRunLoopOutsideOfDLL() is called and the user close all
    'visible forms the application process do not stop. The ORB is still waiting
    'for incoming requests until oOrb.shutdown() is called. To avoid this kind
    'of background process call oOrb.shutdown() if the user close the last form.
    If Not oOrb Is Nothing Then Call oOrb.shutdown(False)
End Sub

Private Function c_SunInitialReferences_getFunc(ByVal id As String) As VBOrb.cOrbObject
    If id = "NameService" Then
        Set c_SunInitialReferences_getFunc = fNameContextRoot.getThis()
    End If
End Function

Private Function c_SunInitialReferences_list() As c_StringSeq
    Dim oStrSeq As c_StringSeq
    Set oStrSeq = New c_StringSeq
    oStrSeq.Length = 1
    oStrSeq.Item(0) = "NameService"
    Set c_SunInitialReferences_list = oStrSeq
End Function

Public Sub clearResult()
    tbResult.Text = ""
End Sub

Public Sub printLine(ByVal sLine As String)
    tbResult.Text = tbResult.Text & sLine & vbCrLf
End Sub


