VERSION 1.0 CLASS
BEGIN
  MultiUse = -1  'True
END
Attribute VB_Name = "c_NmNotFoundEx"
Attribute VB_GlobalNameSpace = False
Attribute VB_Creatable = True
Attribute VB_PredeclaredId = False
Attribute VB_Exposed = False
'Generated by IDL2VB v121. Copyright (c) 1999-2003 Martin.Both
'Source File Name: ../naming/CosNaming.idl
'Target File Name: c_NmNotFoundEx.cls

Option Explicit

Implements cOrbException

'exception ::CosNaming::NamingContext::NotFound
Const sTypeId As String = "IDL:omg.org/CosNaming/NamingContext/NotFound:1.0"
Private sSource As String
Private sPostDescr As String

'Member(s)
Private why_ As Long
Private rest_of_name_ As c_NmNameComponentSeq

'Helper to get different COM interface
Public Function thisOrbException() As cOrbException
    Set thisOrbException = Me
End Function

'Member: why
Public Property Get why() As Long
    why = why_
End Property

Public Property Let why(ByVal whyNew As Long)
    why_ = whyNew
End Property

'Member: rest_of_name
Public Property Get rest_of_name() As c_NmNameComponentSeq
    Set rest_of_name = rest_of_name_
End Property

Public Property Set rest_of_name(ByVal rest_of_nameNew As c_NmNameComponentSeq)
    Set rest_of_name_ = rest_of_nameNew
End Property

Private Property Get cOrbException_TypeId() As String
    cOrbException_TypeId = sTypeId
End Property

Private Property Get cOrbException_Source() As String
    cOrbException_Source = sSource
End Property

Private Property Get cOrbException_Description() As String
    cOrbException_Description = "CORBA User Exception: [" & sTypeId & "], " & _
        TypeName(Me) & sPostDescr
End Property

Private Sub cOrbException_addInfos(Optional ByRef SourcePrefix As String = "", _
    Optional ByRef PostDescr As String = "")
    If sSource = "" Then
        sSource = SourcePrefix
    ElseIf SourcePrefix <> "" Then
        sSource = SourcePrefix & ": " & sSource
    End If
    If PostDescr <> "" Then
        sPostDescr = sPostDescr & ", " & PostDescr
    End If
End Sub

'Helper
Private Sub cOrbException_initByRead(ByVal oIn As cOrbStream)
    On Error GoTo ErrHandler
    why_ = oIn.readUlong()
    Set rest_of_name_ = New c_NmNameComponentSeq
    Call rest_of_name_.initByRead(oIn, 0)
    Exit Sub
ErrHandler:
    Call VBOrb.ErrReraise(Err, "NotFound.read")
End Sub

'Helper
Private Sub cOrbException_writeMe(ByVal oOut As cOrbStream)
    On Error GoTo ErrHandler
    Call oOut.writeString(sTypeId)
    Call oOut.writeUlong(why_)
    Call rest_of_name_.writeMe(oOut, 0)
    Exit Sub
ErrHandler:
    Call VBOrb.ErrReraise(Err, "NotFound.write")
End Sub

'Helper
Public Sub initByAny(ByVal oAny As cOrbAny)
    On Error GoTo ErrHandler
    why_ = oAny.getUlong()
    Call oAny.nextPos
    Set rest_of_name_ = New c_NmNameComponentSeq
    Call rest_of_name_.initByAny(oAny.currentComponent())
    Call oAny.nextPos
    Exit Sub
ErrHandler:
    Call VBOrb.ErrReraise(Err, "initByAny")
End Sub

'Helper
Public Sub insertIntoAny(ByVal oAny As cOrbAny)
    On Error GoTo ErrHandler
    Call oAny.insertUlong(why_)
    Call oAny.nextPos
    Call rest_of_name_.insertIntoAny(oAny.currentComponent())
    Call oAny.nextPos
    Exit Sub
ErrHandler:
    Call VBOrb.ErrReraise(Err, "insertIntoAny")
End Sub