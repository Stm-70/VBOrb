Attribute VB_Name = "m_javalangNumber"
'Generated by IDL2VB v121. Copyright (c) 2000-2003 Martin.Both
'Source File Name: jdk1.4/idl/java/lang/Number.idl

Option Explicit

'valuetype ::java::lang::Number
Public Const TypeId As String = "RMI:java.lang.Number:071DA8BE7F971128:86AC951D0B94E08B"

'Helper
Public Function TypeCode() As cOrbTypeCode
    On Error GoTo ErrHandler
    Dim oOrb As cOrbImpl
    Set oOrb = VBOrb.defaultOrb()
    'Get previously created recursive or concrete TypeCode
    Dim oRecTC As cOrbTypeCode
    Set oRecTC = oOrb.getRecursiveTC(TypeId, 29) 'mCB.tk_value
    If oRecTC Is Nothing Then
        'Create place holder for TypeCode to avoid endless recursion
        Set oRecTC = oOrb.createRecursiveTc(TypeId)
        On Error GoTo ErrRollback
        'Describe value members
        Dim oMemSeq As cCBValueMemberSeq
        Set oMemSeq = New cCBValueMemberSeq
        oMemSeq.Length = 0
        'Overwrite place holder
        Dim iTypeModifier As Integer
        iTypeModifier = 0 'VM_NONE
        Dim oConcreteBase As cOrbTypeCode
        Set oConcreteBase = Nothing
        Call oRecTC.setRecTc2ValueTc("Number", iTypeModifier, oConcreteBase, oMemSeq)
    End If
    Set TypeCode = oRecTC
    Exit Function
ErrRollback:
    Call oRecTC.destroy
ErrHandler:
    Call VBOrb.ErrReraise(Err, "TypeCode")
End Function
