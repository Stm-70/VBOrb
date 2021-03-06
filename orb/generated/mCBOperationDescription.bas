Attribute VB_Name = "mCBOperationDescription"
'Generated by IDL2VB v123. Copyright (c) 2000-2003 Martin.Both
'Source File Name: ../include/CORBA.idl

Option Explicit

'struct ::CORBA::OperationDescription
Public Const TypeId As String = "IDL:omg.org/CORBA/OperationDescription:1.0"

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
        oMemSeq.Length = 9
        Set oMemSeq.Item(0) = New cCBStructMember
        oMemSeq.Item(0).name = "name"
        Set oMemSeq.Item(0).p_type = oOrb.createStringTc(0)
        Set oMemSeq.Item(0).p_type = oOrb.createAliasTc("IDL:omg.org/CORBA/Identifier:1.0", "Identifier", oMemSeq.Item(0).p_type)
        Set oMemSeq.Item(1) = New cCBStructMember
        oMemSeq.Item(1).name = "id"
        Set oMemSeq.Item(1).p_type = oOrb.createStringTc(0)
        Set oMemSeq.Item(1).p_type = oOrb.createAliasTc("IDL:omg.org/CORBA/RepositoryId:1.0", "RepositoryId", oMemSeq.Item(1).p_type)
        Set oMemSeq.Item(2) = New cCBStructMember
        oMemSeq.Item(2).name = "defined_in"
        Set oMemSeq.Item(2).p_type = oOrb.createStringTc(0)
        Set oMemSeq.Item(2).p_type = oOrb.createAliasTc("IDL:omg.org/CORBA/RepositoryId:1.0", "RepositoryId", oMemSeq.Item(2).p_type)
        Set oMemSeq.Item(3) = New cCBStructMember
        oMemSeq.Item(3).name = "version"
        Set oMemSeq.Item(3).p_type = oOrb.createStringTc(0)
        Set oMemSeq.Item(3).p_type = oOrb.createAliasTc("IDL:omg.org/CORBA/VersionSpec:1.0", "VersionSpec", oMemSeq.Item(3).p_type)
        Set oMemSeq.Item(4) = New cCBStructMember
        oMemSeq.Item(4).name = "result"
        Set oMemSeq.Item(4).p_type = oOrb.createPrimitiveTc(12) 'VBOrb.TCTypeCode
        Set oMemSeq.Item(5) = New cCBStructMember
        oMemSeq.Item(5).name = "mode"
        Set oMemSeq.Item(5).p_type = mCB.OperationMode_TypeCode()
        Set oMemSeq.Item(6) = New cCBStructMember
        oMemSeq.Item(6).name = "contexts"
        Set oMemSeq.Item(6).p_type = oOrb.createStringTc(0)
        Set oMemSeq.Item(6).p_type = oOrb.createAliasTc("IDL:omg.org/CORBA/Identifier:1.0", "Identifier", oMemSeq.Item(6).p_type)
        Set oMemSeq.Item(6).p_type = oOrb.createAliasTc("IDL:omg.org/CORBA/ContextIdentifier:1.0", "ContextIdentifier", oMemSeq.Item(6).p_type)
        Set oMemSeq.Item(6).p_type = oOrb.createSequenceTc(0, oMemSeq.Item(6).p_type)
        Set oMemSeq.Item(6).p_type = oOrb.createAliasTc("IDL:omg.org/CORBA/ContextIdSeq:1.0", "ContextIdSeq", oMemSeq.Item(6).p_type)
        Set oMemSeq.Item(7) = New cCBStructMember
        oMemSeq.Item(7).name = "parameters"
        Set oMemSeq.Item(7).p_type = mCBParameterDescription.TypeCode()
        Set oMemSeq.Item(7).p_type = oOrb.createSequenceTc(0, oMemSeq.Item(7).p_type)
        Set oMemSeq.Item(7).p_type = oOrb.createAliasTc("IDL:omg.org/CORBA/ParDescriptionSeq:1.0", "ParDescriptionSeq", oMemSeq.Item(7).p_type)
        Set oMemSeq.Item(8) = New cCBStructMember
        oMemSeq.Item(8).name = "exceptions"
        Set oMemSeq.Item(8).p_type = mCBExceptionDescription.TypeCode()
        Set oMemSeq.Item(8).p_type = oOrb.createSequenceTc(0, oMemSeq.Item(8).p_type)
        Set oMemSeq.Item(8).p_type = oOrb.createAliasTc("IDL:omg.org/CORBA/ExcDescriptionSeq:1.0", "ExcDescriptionSeq", oMemSeq.Item(8).p_type)
        'Overwrite place holder
        Call oRecTC.setRecTc2StructTc("OperationDescription", oMemSeq)
    End If
    Set TypeCode = oRecTC
    Exit Function
ErrRollback:
    Call oRecTC.destroy
ErrHandler:
    Call mVBOrb.VBOrb.ErrReraise(Err, "TypeCode")
End Function

'Helper, oAny.writeValue() -> struct.initByRead()
Public Function extractFromAny(ByVal oAny As cOrbAny) As cCBOperationDescription
    Dim oStruct As cCBOperationDescription
    Set oStruct = New cCBOperationDescription
    Call oStruct.initByAny(oAny)
    Set extractFromAny = oStruct
End Function

'Helper, struct.writeMe() -> oAny.initByReadValue()
Public Function cloneAsAny(ByVal oStruct As cCBOperationDescription) As cOrbAny
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
