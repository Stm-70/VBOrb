Attribute VB_Name = "mCBValueDescription"
'Generated by IDL2VB v123. Copyright (c) 2000-2003 Martin.Both
'Source File Name: ../include/CORBA.idl

Option Explicit

'struct ::CORBA::ValueDescription
Public Const TypeId As String = "IDL:omg.org/CORBA/ValueDescription:1.0"

'Helper
Public Function TypeCode() As cOrbTypeCode
    On Error GoTo ErrHandler
    Dim oOrb As cOrbImpl
    Set oOrb = mVBOrb.VBOrb.defaultOrb()
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
        oMemSeq.Length = 10
        Set oMemSeq.Item(0) = New cCBStructMember
        oMemSeq.Item(0).name = "name"
        Set oMemSeq.Item(0).p_type = oOrb.createStringTc(0)
        Set oMemSeq.Item(0).p_type = oOrb.createAliasTc("IDL:omg.org/CORBA/Identifier:1.0", "Identifier", oMemSeq.Item(0).p_type)
        Set oMemSeq.Item(1) = New cCBStructMember
        oMemSeq.Item(1).name = "id"
        Set oMemSeq.Item(1).p_type = oOrb.createStringTc(0)
        Set oMemSeq.Item(1).p_type = oOrb.createAliasTc("IDL:omg.org/CORBA/RepositoryId:1.0", "RepositoryId", oMemSeq.Item(1).p_type)
        Set oMemSeq.Item(2) = New cCBStructMember
        oMemSeq.Item(2).name = "is_abstract"
        Set oMemSeq.Item(2).p_type = oOrb.createPrimitiveTc(8) 'VBOrb.TCBoolean
        Set oMemSeq.Item(3) = New cCBStructMember
        oMemSeq.Item(3).name = "is_custom"
        Set oMemSeq.Item(3).p_type = oOrb.createPrimitiveTc(8) 'VBOrb.TCBoolean
        Set oMemSeq.Item(4) = New cCBStructMember
        oMemSeq.Item(4).name = "defined_in"
        Set oMemSeq.Item(4).p_type = oOrb.createStringTc(0)
        Set oMemSeq.Item(4).p_type = oOrb.createAliasTc("IDL:omg.org/CORBA/RepositoryId:1.0", "RepositoryId", oMemSeq.Item(4).p_type)
        Set oMemSeq.Item(5) = New cCBStructMember
        oMemSeq.Item(5).name = "version"
        Set oMemSeq.Item(5).p_type = oOrb.createStringTc(0)
        Set oMemSeq.Item(5).p_type = oOrb.createAliasTc("IDL:omg.org/CORBA/VersionSpec:1.0", "VersionSpec", oMemSeq.Item(5).p_type)
        Set oMemSeq.Item(6) = New cCBStructMember
        oMemSeq.Item(6).name = "supported_interfaces"
        Set oMemSeq.Item(6).p_type = oOrb.createStringTc(0)
        Set oMemSeq.Item(6).p_type = oOrb.createAliasTc("IDL:omg.org/CORBA/RepositoryId:1.0", "RepositoryId", oMemSeq.Item(6).p_type)
        Set oMemSeq.Item(6).p_type = oOrb.createSequenceTc(0, oMemSeq.Item(6).p_type)
        Set oMemSeq.Item(6).p_type = oOrb.createAliasTc("IDL:omg.org/CORBA/RepositoryIdSeq:1.0", "RepositoryIdSeq", oMemSeq.Item(6).p_type)
        Set oMemSeq.Item(7) = New cCBStructMember
        oMemSeq.Item(7).name = "abstract_base_values"
        Set oMemSeq.Item(7).p_type = oOrb.createStringTc(0)
        Set oMemSeq.Item(7).p_type = oOrb.createAliasTc("IDL:omg.org/CORBA/RepositoryId:1.0", "RepositoryId", oMemSeq.Item(7).p_type)
        Set oMemSeq.Item(7).p_type = oOrb.createSequenceTc(0, oMemSeq.Item(7).p_type)
        Set oMemSeq.Item(7).p_type = oOrb.createAliasTc("IDL:omg.org/CORBA/RepositoryIdSeq:1.0", "RepositoryIdSeq", oMemSeq.Item(7).p_type)
        Set oMemSeq.Item(8) = New cCBStructMember
        oMemSeq.Item(8).name = "is_truncatable"
        Set oMemSeq.Item(8).p_type = oOrb.createPrimitiveTc(8) 'VBOrb.TCBoolean
        Set oMemSeq.Item(9) = New cCBStructMember
        oMemSeq.Item(9).name = "base_value"
        Set oMemSeq.Item(9).p_type = oOrb.createStringTc(0)
        Set oMemSeq.Item(9).p_type = oOrb.createAliasTc("IDL:omg.org/CORBA/RepositoryId:1.0", "RepositoryId", oMemSeq.Item(9).p_type)
        'Overwrite place holder
        Call oRecTC.setRecTc2StructTc("ValueDescription", oMemSeq)
    End If
    Set TypeCode = oRecTC
    Exit Function
ErrRollback:
    Call oRecTC.destroy
ErrHandler:
    Call mVBOrb.VBOrb.ErrReraise(Err, "TypeCode")
End Function

'Helper, oAny.writeValue() -> struct.initByRead()
Public Function extractFromAny(ByVal oAny As cOrbAny) As cCBValueDescription
    Dim oStruct As cCBValueDescription
    Set oStruct = New cCBValueDescription
    Call oStruct.initByAny(oAny)
    Set extractFromAny = oStruct
End Function

'Helper, struct.writeMe() -> oAny.initByReadValue()
Public Function cloneAsAny(ByVal oStruct As cCBValueDescription) As cOrbAny
    On Error GoTo ErrHandler
    Dim oAny As cOrbAny
    Set oAny = New cOrbAny
    Call oAny.initByDefaultValue(TypeCode())
    Call oStruct.insertIntoAny(oAny)
    Set cloneAsAny = oAny
    Exit Function
ErrHandler:
    Call mVBOrb.VBOrb.ErrReraise(Err, "cloneAsAny")
End Function