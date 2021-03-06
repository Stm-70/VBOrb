VERSION 1.0 CLASS
BEGIN
  MultiUse = -1  'True
END
Attribute VB_Name = "c_SpInfvalueExampleExample"
Attribute VB_GlobalNameSpace = False
Attribute VB_Creatable = True
Attribute VB_PredeclaredId = False
Attribute VB_Exposed = False
'Generated by IDL2VB v121. Copyright (c) 1999-2003 Martin.Both
'Source File Name: ../demos/obv/valuetype/valuetype.idl
'Target File Name: c_SpInfvalueExampleExample.cls

Option Explicit

'You can use this class as a value example:
' Rename class to c_SpInfvalueExample and save the file as c_SpInfvalueExample.cls.
' Finally write your own local value operations.

Implements cOrbAbstractBase
Implements cOrbValueBase
Implements c_SpInfMessage

'valuetype ::obv::support_interface::valueExample
Const sTypeId As String = "IDL:obv/support_interface/valueExample:1.0"

'State member(s)
Private nameState_ As String

'Helper to get different COM interface
Friend Function thisOrbAbstractBase() As cOrbAbstractBase
    Set thisOrbAbstractBase = Me
End Function

'Helper
Private Function cOrbAbstractBase_isObjRef() As Boolean
    cOrbAbstractBase_isObjRef = False
End Function

'Helper to get different COM interface
Friend Function thisOrbValueBase() As cOrbValueBase
    Set thisOrbValueBase = Me
End Function

Private Function cOrbValueBase_UniqueId() As Long
    cOrbValueBase_UniqueId = VBOrb.getNextUniqueId()
End Function

'Helper
Private Sub cOrbValueBase_initByRead(ByVal oIn As cOrbStream)
    On Error GoTo ErrHandler
    nameState_ = oIn.readString()
    Exit Sub
ErrHandler:
    Call VBOrb.ErrReraise(Err, "valueExample.read")
End Sub

'Helper
Private Sub cOrbValueBase_writeMe(ByVal oOut As cOrbStream)
    On Error GoTo ErrHandler
    Call oOut.writeString(nameState_)
    Exit Sub
ErrHandler:
    Call VBOrb.ErrReraise(Err, "valueExample.write")
End Sub

Private Function cOrbValueBase_getIds(ByVal Item As Integer) As String
    Select Case Item
    Case 0: cOrbValueBase_getIds = sTypeId
    End Select
End Function

Private Function cOrbValueBase_isCustom() As Boolean
    cOrbValueBase_isCustom = False
End Function

'State member: name_state
Public Property Get nameState() As String
    nameState = nameState_
End Property

Public Property Let nameState(ByVal name_stateNew As String)
    nameState_ = name_stateNew
End Property

'Helper to get different COM interface
Friend Function thisSpInfMessage() As c_SpInfMessage
    Set thisSpInfMessage = Me
End Function

'print()
Private Sub c_SpInfMessage_printSub()
    Call printSub
End Sub

'print()
Public Sub printSub()
    On Error GoTo ErrHandler
    'Please write your code here after copying this file
    'instead of throwing an exception
    'e.g. Call oDelegate.printSub
    Call VBOrb.raiseNOIMPLEMENT(0, VBOrb.CompletedNO)
    Exit Sub
ErrHandler:
    Call VBOrb.ErrReraise(Err, "printSub")
End Sub

'printName()
Public Sub printName()
    On Error GoTo ErrHandler
    'Please write your code here after copying this file
    'instead of throwing an exception
    'e.g. Call oDelegate.printName
    Call VBOrb.raiseNOIMPLEMENT(0, VBOrb.CompletedNO)
    Exit Sub
ErrHandler:
    Call VBOrb.ErrReraise(Err, "printName")
End Sub
