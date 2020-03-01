VERSION 1.0 CLASS
BEGIN
  MultiUse = -1  'True
END
Attribute VB_Name = "cCBDataOutputStreamExample"
Attribute VB_GlobalNameSpace = False
Attribute VB_Creatable = True
Attribute VB_PredeclaredId = False
Attribute VB_Exposed = True
'Generated by IDL2VB v123. Copyright (c) 1999-2003 Martin.Both
'Source File Name: ../include/CORBA.idl
'Target File Name: cCBDataOutputStreamExample.cls

Option Explicit

'You can use this class as a value example:
' Rename class to cCBDataOutputStream and save the file as cCBDataOutputStream.cls.
' Finally write your own local value operations.

Implements cOrbAbstractBase
Implements cOrbValueBase

'valuetype ::CORBA::DataOutputStream
Const sTypeId As String = "IDL:omg.org/CORBA/DataOutputStream:1.0"

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
    cOrbValueBase_UniqueId = mVBOrb.VBOrb.getNextUniqueId()
End Function

'Helper
Private Sub cOrbValueBase_initByRead(ByVal oIn As cOrbStream)
    On Error GoTo ErrHandler
    Exit Sub
ErrHandler:
    Call mVBOrb.VBOrb.ErrReraise(Err, "DataOutputStream.read")
End Sub

'Helper
Private Sub cOrbValueBase_writeMe(ByVal oOut As cOrbStream)
    On Error GoTo ErrHandler
    Exit Sub
ErrHandler:
    Call mVBOrb.VBOrb.ErrReraise(Err, "DataOutputStream.write")
End Sub

Private Function cOrbValueBase_getIds(ByVal Item As Integer) As String
    Select Case Item
    Case 0: cOrbValueBase_getIds = sTypeId
    End Select
End Function

Private Function cOrbValueBase_isCustom() As Boolean
    cOrbValueBase_isCustom = False
End Function

'write_any()
Public Sub writeAny(ByVal value As cOrbAny)
    On Error GoTo ErrHandler
    'Please write your code here after copying this file
    'instead of throwing an exception
    'e.g. Call oDelegate.writeAny(value)
    Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Exit Sub
ErrHandler:
    Call mVBOrb.VBOrb.ErrReraise(Err, "writeAny")
End Sub

'write_boolean()
Public Sub writeBoolean(ByVal value As Boolean)
    On Error GoTo ErrHandler
    'Please write your code here after copying this file
    'instead of throwing an exception
    'e.g. Call oDelegate.writeBoolean(value)
    Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Exit Sub
ErrHandler:
    Call mVBOrb.VBOrb.ErrReraise(Err, "writeBoolean")
End Sub

'write_char()
Public Sub writeChar(ByVal value As Byte)
    On Error GoTo ErrHandler
    'Please write your code here after copying this file
    'instead of throwing an exception
    'e.g. Call oDelegate.writeChar(value)
    Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Exit Sub
ErrHandler:
    Call mVBOrb.VBOrb.ErrReraise(Err, "writeChar")
End Sub

'write_wchar()
Public Sub writeWchar(ByVal value As Integer)
    On Error GoTo ErrHandler
    'Please write your code here after copying this file
    'instead of throwing an exception
    'e.g. Call oDelegate.writeWchar(value)
    Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Exit Sub
ErrHandler:
    Call mVBOrb.VBOrb.ErrReraise(Err, "writeWchar")
End Sub

'write_octet()
Public Sub writeOctet(ByVal value As Byte)
    On Error GoTo ErrHandler
    'Please write your code here after copying this file
    'instead of throwing an exception
    'e.g. Call oDelegate.writeOctet(value)
    Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Exit Sub
ErrHandler:
    Call mVBOrb.VBOrb.ErrReraise(Err, "writeOctet")
End Sub

'write_short()
Public Sub writeShort(ByVal value As Integer)
    On Error GoTo ErrHandler
    'Please write your code here after copying this file
    'instead of throwing an exception
    'e.g. Call oDelegate.writeShort(value)
    Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Exit Sub
ErrHandler:
    Call mVBOrb.VBOrb.ErrReraise(Err, "writeShort")
End Sub

'write_ushort()
Public Sub writeUshort(ByVal value As Integer)
    On Error GoTo ErrHandler
    'Please write your code here after copying this file
    'instead of throwing an exception
    'e.g. Call oDelegate.writeUshort(value)
    Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Exit Sub
ErrHandler:
    Call mVBOrb.VBOrb.ErrReraise(Err, "writeUshort")
End Sub

'write_long()
Public Sub writeLong(ByVal value As Long)
    On Error GoTo ErrHandler
    'Please write your code here after copying this file
    'instead of throwing an exception
    'e.g. Call oDelegate.writeLong(value)
    Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Exit Sub
ErrHandler:
    Call mVBOrb.VBOrb.ErrReraise(Err, "writeLong")
End Sub

'write_ulong()
Public Sub writeUlong(ByVal value As Long)
    On Error GoTo ErrHandler
    'Please write your code here after copying this file
    'instead of throwing an exception
    'e.g. Call oDelegate.writeUlong(value)
    Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Exit Sub
ErrHandler:
    Call mVBOrb.VBOrb.ErrReraise(Err, "writeUlong")
End Sub

'write_longlong()
Public Sub writeLonglong(ByVal value As Variant)
    On Error GoTo ErrHandler
    'Please write your code here after copying this file
    'instead of throwing an exception
    'e.g. Call oDelegate.writeLonglong(value)
    Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Exit Sub
ErrHandler:
    Call mVBOrb.VBOrb.ErrReraise(Err, "writeLonglong")
End Sub

'write_ulonglong()
Public Sub writeUlonglong(ByVal value As Variant)
    On Error GoTo ErrHandler
    'Please write your code here after copying this file
    'instead of throwing an exception
    'e.g. Call oDelegate.writeUlonglong(value)
    Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Exit Sub
ErrHandler:
    Call mVBOrb.VBOrb.ErrReraise(Err, "writeUlonglong")
End Sub

'write_float()
Public Sub writeFloat(ByVal value As Single)
    On Error GoTo ErrHandler
    'Please write your code here after copying this file
    'instead of throwing an exception
    'e.g. Call oDelegate.writeFloat(value)
    Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Exit Sub
ErrHandler:
    Call mVBOrb.VBOrb.ErrReraise(Err, "writeFloat")
End Sub

'write_double()
Public Sub writeDouble(ByVal value As Double)
    On Error GoTo ErrHandler
    'Please write your code here after copying this file
    'instead of throwing an exception
    'e.g. Call oDelegate.writeDouble(value)
    Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Exit Sub
ErrHandler:
    Call mVBOrb.VBOrb.ErrReraise(Err, "writeDouble")
End Sub

'write_longdouble()
Public Sub writeLongdouble(ByVal value As cOrbLongdouble)
    On Error GoTo ErrHandler
    'Please write your code here after copying this file
    'instead of throwing an exception
    'e.g. Call oDelegate.writeLongdouble(value)
    Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Exit Sub
ErrHandler:
    Call mVBOrb.VBOrb.ErrReraise(Err, "writeLongdouble")
End Sub

'write_string()
Public Sub writeString(ByVal value As String)
    On Error GoTo ErrHandler
    'Please write your code here after copying this file
    'instead of throwing an exception
    'e.g. Call oDelegate.writeString(value)
    Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Exit Sub
ErrHandler:
    Call mVBOrb.VBOrb.ErrReraise(Err, "writeString")
End Sub

'write_wstring()
Public Sub writeWstring(ByVal value As String)
    On Error GoTo ErrHandler
    'Please write your code here after copying this file
    'instead of throwing an exception
    'e.g. Call oDelegate.writeWstring(value)
    Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Exit Sub
ErrHandler:
    Call mVBOrb.VBOrb.ErrReraise(Err, "writeWstring")
End Sub

'write_Object()
Public Sub writeObject(ByVal value As cOrbObject)
    On Error GoTo ErrHandler
    'Please write your code here after copying this file
    'instead of throwing an exception
    'e.g. Call oDelegate.writeObject(value)
    Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Exit Sub
ErrHandler:
    Call mVBOrb.VBOrb.ErrReraise(Err, "writeObject")
End Sub

'write_Abstract()
Public Sub writeAbstract(ByVal value As AbstractBase)
    On Error GoTo ErrHandler
    'Please write your code here after copying this file
    'instead of throwing an exception
    'e.g. Call oDelegate.writeAbstract(value)
    Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Exit Sub
ErrHandler:
    Call mVBOrb.VBOrb.ErrReraise(Err, "writeAbstract")
End Sub

'write_Value()
Public Sub writeValue(ByVal value As cOrbValueBase)
    On Error GoTo ErrHandler
    'Please write your code here after copying this file
    'instead of throwing an exception
    'e.g. Call oDelegate.writeValue(value)
    Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Exit Sub
ErrHandler:
    Call mVBOrb.VBOrb.ErrReraise(Err, "writeValue")
End Sub

'write_TypeCode()
Public Sub writeTypeCode(ByVal value As cOrbTypeCode)
    On Error GoTo ErrHandler
    'Please write your code here after copying this file
    'instead of throwing an exception
    'e.g. Call oDelegate.writeTypeCode(value)
    Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Exit Sub
ErrHandler:
    Call mVBOrb.VBOrb.ErrReraise(Err, "writeTypeCode")
End Sub

'write_any_array()
Public Sub writeAnyArray(ByVal seq As c_AnySeq, ByVal offset As Long, _
    ByVal length As Long)
    On Error GoTo ErrHandler
    'Please write your code here after copying this file
    'instead of throwing an exception
    'e.g. Call oDelegate.writeAnyArray(seq, offset, length)
    Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Exit Sub
ErrHandler:
    Call mVBOrb.VBOrb.ErrReraise(Err, "writeAnyArray")
End Sub

'write_boolean_array()
Public Sub writeBooleanArray(ByVal seq As c_BooleanSeq, ByVal offset As Long, _
    ByVal length As Long)
    On Error GoTo ErrHandler
    'Please write your code here after copying this file
    'instead of throwing an exception
    'e.g. Call oDelegate.writeBooleanArray(seq, offset, length)
    Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Exit Sub
ErrHandler:
    Call mVBOrb.VBOrb.ErrReraise(Err, "writeBooleanArray")
End Sub

'write_char_array()
Public Sub writeCharArray(ByVal seq As c_CharSeq, ByVal offset As Long, _
    ByVal length As Long)
    On Error GoTo ErrHandler
    'Please write your code here after copying this file
    'instead of throwing an exception
    'e.g. Call oDelegate.writeCharArray(seq, offset, length)
    Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Exit Sub
ErrHandler:
    Call mVBOrb.VBOrb.ErrReraise(Err, "writeCharArray")
End Sub

'write_wchar_array()
Public Sub writeWcharArray(ByVal seq As c_WcharSeq, ByVal offset As Long, _
    ByVal length As Long)
    On Error GoTo ErrHandler
    'Please write your code here after copying this file
    'instead of throwing an exception
    'e.g. Call oDelegate.writeWcharArray(seq, offset, length)
    Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Exit Sub
ErrHandler:
    Call mVBOrb.VBOrb.ErrReraise(Err, "writeWcharArray")
End Sub

'write_octet_array()
Public Sub writeOctetArray(ByVal seq As c_ByteSeq, ByVal offset As Long, _
    ByVal length As Long)
    On Error GoTo ErrHandler
    'Please write your code here after copying this file
    'instead of throwing an exception
    'e.g. Call oDelegate.writeOctetArray(seq, offset, length)
    Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Exit Sub
ErrHandler:
    Call mVBOrb.VBOrb.ErrReraise(Err, "writeOctetArray")
End Sub

'write_short_array()
Public Sub writeShortArray(ByVal seq As c_IntegerSeq, ByVal offset As Long, _
    ByVal length As Long)
    On Error GoTo ErrHandler
    'Please write your code here after copying this file
    'instead of throwing an exception
    'e.g. Call oDelegate.writeShortArray(seq, offset, length)
    Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Exit Sub
ErrHandler:
    Call mVBOrb.VBOrb.ErrReraise(Err, "writeShortArray")
End Sub

'write_ushort_array()
Public Sub writeUshortArray(ByVal seq As c_IntegerSeq, ByVal offset As Long, _
    ByVal length As Long)
    On Error GoTo ErrHandler
    'Please write your code here after copying this file
    'instead of throwing an exception
    'e.g. Call oDelegate.writeUshortArray(seq, offset, length)
    Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Exit Sub
ErrHandler:
    Call mVBOrb.VBOrb.ErrReraise(Err, "writeUshortArray")
End Sub

'write_long_array()
Public Sub writeLongArray(ByVal seq As c_LongSeq, ByVal offset As Long, _
    ByVal length As Long)
    On Error GoTo ErrHandler
    'Please write your code here after copying this file
    'instead of throwing an exception
    'e.g. Call oDelegate.writeLongArray(seq, offset, length)
    Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Exit Sub
ErrHandler:
    Call mVBOrb.VBOrb.ErrReraise(Err, "writeLongArray")
End Sub

'write_ulong_array()
Public Sub writeUlongArray(ByVal seq As c_LongSeq, ByVal offset As Long, _
    ByVal length As Long)
    On Error GoTo ErrHandler
    'Please write your code here after copying this file
    'instead of throwing an exception
    'e.g. Call oDelegate.writeUlongArray(seq, offset, length)
    Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Exit Sub
ErrHandler:
    Call mVBOrb.VBOrb.ErrReraise(Err, "writeUlongArray")
End Sub

'write_ulonglong_array()
Public Sub writeUlonglongArray(ByVal seq As c_VariantSeq, ByVal offset As Long, _
    ByVal length As Long)
    On Error GoTo ErrHandler
    'Please write your code here after copying this file
    'instead of throwing an exception
    'e.g. Call oDelegate.writeUlonglongArray(seq, offset, length)
    Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Exit Sub
ErrHandler:
    Call mVBOrb.VBOrb.ErrReraise(Err, "writeUlonglongArray")
End Sub

'write_longlong_array()
Public Sub writeLonglongArray(ByVal seq As c_VariantSeq, ByVal offset As Long, _
    ByVal length As Long)
    On Error GoTo ErrHandler
    'Please write your code here after copying this file
    'instead of throwing an exception
    'e.g. Call oDelegate.writeLonglongArray(seq, offset, length)
    Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Exit Sub
ErrHandler:
    Call mVBOrb.VBOrb.ErrReraise(Err, "writeLonglongArray")
End Sub

'write_float_array()
Public Sub writeFloatArray(ByVal seq As c_SingleSeq, ByVal offset As Long, _
    ByVal length As Long)
    On Error GoTo ErrHandler
    'Please write your code here after copying this file
    'instead of throwing an exception
    'e.g. Call oDelegate.writeFloatArray(seq, offset, length)
    Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Exit Sub
ErrHandler:
    Call mVBOrb.VBOrb.ErrReraise(Err, "writeFloatArray")
End Sub

'write_double_array()
Public Sub writeDoubleArray(ByVal seq As c_DoubleSeq, ByVal offset As Long, _
    ByVal length As Long)
    On Error GoTo ErrHandler
    'Please write your code here after copying this file
    'instead of throwing an exception
    'e.g. Call oDelegate.writeDoubleArray(seq, offset, length)
    Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Exit Sub
ErrHandler:
    Call mVBOrb.VBOrb.ErrReraise(Err, "writeDoubleArray")
End Sub

'write_long_double_array()
Public Sub writeLongDoubleArray(ByVal seq As c_LongdoubleSeq, _
    ByVal offset As Long, ByVal length As Long)
    On Error GoTo ErrHandler
    'Please write your code here after copying this file
    'instead of throwing an exception
    'e.g. Call oDelegate.writeLongDoubleArray(seq, offset, length)
    Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Exit Sub
ErrHandler:
    Call mVBOrb.VBOrb.ErrReraise(Err, "writeLongDoubleArray")
End Sub

'write_fixed() raises(cCBBadFixedValue)
Public Sub writeFixed(ByVal fixed_value As cOrbAny)
    On Error GoTo ErrHandler
    'Please write your code here after copying this file
    'instead of throwing an exception
    'e.g. Call oDelegate.writeFixed(fixed_value)
    Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Exit Sub
ErrHandler:
    Call mVBOrb.VBOrb.ErrReraise(Err, "writeFixed")
End Sub

'write_fixed_array() raises(cCBBadFixedValue)
Public Sub writeFixedArray(ByVal seq As c_AnySeq, ByVal offset As Long, _
    ByVal length As Long)
    On Error GoTo ErrHandler
    'Please write your code here after copying this file
    'instead of throwing an exception
    'e.g. Call oDelegate.writeFixedArray(seq, offset, length)
    Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Exit Sub
ErrHandler:
    Call mVBOrb.VBOrb.ErrReraise(Err, "writeFixedArray")
End Sub