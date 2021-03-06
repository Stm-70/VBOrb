Attribute VB_Name = "m_NmBinding"
'Generated by IDL2VB v121. Copyright (c) 2000-2003 Martin.Both
'Source File Name: ../naming/CosNaming.idl

Option Explicit

'struct ::CosNaming::Binding
Public Const TypeId As String = "IDL:omg.org/CosNaming/Binding:1.0"

'Helper
Public Function TypeCode() As cOrbTypeCode
    On Error GoTo ErrHandler
    Dim oOrb As cOrbImpl
    Set oOrb = VBOrb.defaultOrb()
    'Get previously created recursive or concrete TypeCode
    Dim oRecTC As cOrbTypeCode
    Set oRecTC = oOrb.getRecursiveTC(TypeId, 15) 'mCB.tk_struct
    If oRecTC Is Nothing Then
        'Create place holder for TypeCode to avoid endless recursion
        Set oRecTC = oOrb.createRecursiveTc(TypeId)
        On Error GoTo ErrRollback
        'Describe struct members
        Dim oMemSeq As cCBStructMemberSeq
        Set oMemSeq = New cCBStructMemberSeq
        oMemSeq.Length = 2
        Set oMemSeq.Item(0) = New cCBStructMember
        oMemSeq.Item(0).name = "binding_name"
        Set oMemSeq.Item(0).p_type = m_NmNameComponent.TypeCode()
        Set oMemSeq.Item(0).p_type = oOrb.createSequenceTc(0, oMemSeq.Item(0).p_type)
        Set oMemSeq.Item(0).p_type = oOrb.createAliasTc("IDL:omg.org/CosNaming/Name:1.0", "Name", oMemSeq.Item(0).p_type)
        Set oMemSeq.Item(1) = New cCBStructMember
        oMemSeq.Item(1).name = "binding_type"
        Set oMemSeq.Item(1).p_type = m_Nm.BindingType_TypeCode()
        'Overwrite place holder
        Call oRecTC.setRecTc2StructTc("Binding", oMemSeq)
    End If
    Set TypeCode = oRecTC
    Exit Function
ErrRollback:
    Call oRecTC.destroy
ErrHandler:
    Call VBOrb.ErrReraise(Err, "TypeCode")
End Function

'Helper, oAny.writeValue() -> struct.initByRead()
Public Function extractFromAny(ByVal oAny As cOrbAny) As c_NmBinding
    Dim oStruct As c_NmBinding
    Set oStruct = New c_NmBinding
    Call oStruct.initByAny(oAny)
    Set extractFromAny = oStruct
End Function

'Helper, struct.writeMe() -> oAny.initByReadValue()
Public Function cloneAsAny(ByVal oStruct As c_NmBinding) As cOrbAny
    On Error GoTo ErrHandler
    Dim oAny As cOrbAny
    Set oAny = New cOrbAny
    Call oAny.initByDefaultValue(TypeCode())
    Call oStruct.insertIntoAny(oAny)
    Set cloneAsAny = oAny
    Exit Function
ErrHandler:
    Call VBOrb.ErrReraise(Err, "cloneAsAny")
End Function
