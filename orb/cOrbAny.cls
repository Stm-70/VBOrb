VERSION 1.0 CLASS
BEGIN
  MultiUse = -1  'True
END
Attribute VB_Name = "cOrbAny"
Attribute VB_GlobalNameSpace = False
Attribute VB_Creatable = True
Attribute VB_PredeclaredId = False
Attribute VB_Exposed = True
'Copyright (c) 2000 Martin.Both

'This library is free software; you can redistribute it and/or
'modify it under the terms of the GNU Library General Public
'License as published by the Free Software Foundation; either
'version 2 of the License, or (at your option) any later version.

'This library is distributed in the hope that it will be useful,
'but WITHOUT ANY WARRANTY; without even the implied warranty of
'MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
'Library General Public License for more details.

Option Explicit

Private oType As cOrbTypeCode
Private oOrigType As cOrbTypeCode
Private lOrigTcKind As Long 'Cache for oType.getOrigType().kind()
Private lCompCnt As Long 'Cache for component count

Private lCurPos As Long 'Selected component or -1

Private bVal As Boolean
Private cVal As Byte
Private iVal As Integer
Private lVal As Long
Private fVal As Single
Private dVal As Double
Private sVal As String
Private oVal As Object
Private vVal As Variant
Private oMemberVals() As cOrbAny

'Helper to read an any value using a TypeCode
Public Sub initByReadValue(ByVal oTC As cOrbTypeCode, ByVal oIn As cOrbStream)
    On Error GoTo ErrHandler
    Dim lCnt As Long
    Set oOrigType = oTC.getOrigType()
    lOrigTcKind = oOrigType.kind()
    lCompCnt = 0
    lCurPos = -1
    Select Case lOrigTcKind
    Case mCB.tk_null, mCB.tk_void
    Case mCB.tk_short
        iVal = oIn.readShort()
    Case mCB.tk_long
        lVal = oIn.readLong()
    Case mCB.tk_ushort
        iVal = oIn.readUshort()
    Case mCB.tk_ulong
        lVal = oIn.readUlong()
    Case mCB.tk_float
        fVal = oIn.readFloat()
    Case mCB.tk_double
        dVal = oIn.readDouble()
    Case mCB.tk_boolean
        bVal = oIn.readBoolean()
    Case mCB.tk_char
        cVal = oIn.readChar()
    Case mCB.tk_octet
        cVal = oIn.readOctet()
    Case mCB.tk_any
        Set oVal = oIn.readAny()
    Case mCB.tk_TypeCode
        Set oVal = oIn.readTypeCode()
    Case mCB.tk_Principal
        Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Case mCB.tk_objref
        Set oVal = oIn.readObject()
    Case mCB.tk_struct, mCB.tk_except
        lCompCnt = oOrigType.memberCount()
        If lCompCnt > 0 Then
            ReDim oMemberVals(0 To lCompCnt - 1)
            lCurPos = 0
        End If
        For lCnt = 0 To lCompCnt - 1
            Set oMemberVals(lCnt) = New cOrbAny
            Call oMemberVals(lCnt).initByReadValue(oOrigType.memberType(lCnt), oIn)
        Next lCnt
    Case mCB.tk_union
        ReDim oMemberVals(0 To 2 - 1)
        lCompCnt = 1 '1 or 2
        lCurPos = 0 '0 or 1
        Set oMemberVals(0) = New cOrbAny
        Call oMemberVals(0).initByReadValue(oOrigType.discriminatorType, oIn)
        'if has active member
        'lCompCnt= 2
        'lcurpos=1
        'Set oMemberVals(1) = New cOrbAny
        'Call oMemberVals(1).initByReadValue(oOrigType.memberType(???), oIn)
        '???
        Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Case mCB.tk_enum
        lVal = oIn.readUlong()
    Case mCB.tk_string
        sVal = oIn.readString(oOrigType.Length())
    Case mCB.tk_sequence
        lCompCnt = oIn.readUlong()
        If lCompCnt > 0 Then
            ReDim oMemberVals(0 To lCompCnt - 1)
            lCurPos = 0
        End If
        For lCnt = 0 To lCompCnt - 1
            Set oMemberVals(lCnt) = New cOrbAny
            Call oMemberVals(lCnt).initByReadValue(oOrigType.memberType(lCnt), oIn)
        Next lCnt
    Case mCB.tk_array
        lCompCnt = oOrigType.Length()
        ReDim oMemberVals(0 To lCompCnt - 1)
        lCurPos = 0
        For lCnt = 0 To lCompCnt - 1
            Set oMemberVals(lCnt) = New cOrbAny
            Call oMemberVals(lCnt).initByReadValue(oOrigType.memberType(lCnt), oIn)
        Next lCnt
    'Case mCB.tk_alias cannot occur
    Case mCB.tk_longlong
        vVal = oIn.readLonglong()
    Case mCB.tk_ulonglong
        vVal = oIn.readUlonglong()
    Case mCB.tk_longdouble
        Set oVal = oIn.readLongdouble()
    Case mCB.tk_wchar
        iVal = oIn.readWchar()
    Case mCB.tk_wstring
        sVal = oIn.readWString(oOrigType.Length())
    Case mCB.tk_fixed
        Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Case mCB.tk_value
        Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Case mCB.tk_value_box
        Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Case mCB.tk_native
        Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Case mCB.tk_abstract_interface, mCB.tk_local_interface
        Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Case Else
        Call mVBOrb.VBOrb.raiseMARSHAL(0, mVBOrb.VBOrb.CompletedNO, _
            "TC.kind = " & CStr(lOrigTcKind))
    End Select
    Set oType = oTC
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("initByReadValue")
End Sub

'Helper to init an any value using a TypeCode
'For default values see: CORBA v2.6: Creating a DynAny Object
'Note DynAnyFactory.createDynAnyFromTypeCode(in CORBA::TypeCode type)
Public Sub initByDefaultValue(ByVal oTC As cOrbTypeCode)
    On Error GoTo ErrHandler
    Dim lCnt As Long
    Set oOrigType = oTC.getOrigType()
    lOrigTcKind = oOrigType.kind()
    lCompCnt = 0
    lCurPos = -1
    Select Case lOrigTcKind
    Case mCB.tk_null, mCB.tk_void
        'Is legal and results in a DynAny without a value and with zero components
    Case mCB.tk_short
        iVal = 0
    Case mCB.tk_long
        lVal = 0&
    Case mCB.tk_ushort
        iVal = 0
    Case mCB.tk_ulong
        lVal = 0&
    Case mCB.tk_float
        fVal = 0!
    Case mCB.tk_double
        dVal = 0#
    Case mCB.tk_boolean
        bVal = False
    Case mCB.tk_char
        cVal = 0
    Case mCB.tk_octet
        cVal = 0
    Case mCB.tk_any
        Dim oAny As cOrbAny
        Set oAny = New cOrbAny
        Call oAny.initByDefaultValue(mVBOrb.VBOrb.TCNull())
        Set oVal = oAny
    Case mCB.tk_TypeCode
        Set oVal = mVBOrb.VBOrb.TCNull()
    Case mCB.tk_Principal
        Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Case mCB.tk_objref
        Set oVal = Nothing
    Case mCB.tk_struct, mCB.tk_except
        'Sets the current position to -1 for empty exceptions and to zero
        'for all other TypeCodes. The members (if any) are (recursively)
        'initialized to their default values.
        lCompCnt = oOrigType.memberCount()
        If lCompCnt > 0 Then
            ReDim oMemberVals(0 To lCompCnt - 1)
            lCurPos = 0
        End If
        For lCnt = 0 To lCompCnt - 1
            Set oMemberVals(lCnt) = New cOrbAny
            Call oMemberVals(lCnt).initByDefaultValue(oOrigType.memberType(lCnt))
        Next lCnt
    Case mCB.tk_union
        'Sets the current position to zero. The discriminator value is set to
        'a value consistent with the first named member of the union. That
        'member is activated and (recursively) initialized to its default value.
        ReDim oMemberVals(0 To 2 - 1)
        lCompCnt = 1
        lCurPos = 0
        Set oMemberVals(0) = New cOrbAny
        Call oMemberVals(0).initByDefaultValue(oOrigType.discriminatorType())
        'oOrigType.defaultIndex()
        Set oMemberVals(1) = New cOrbAny
        Call oMemberVals(1).initByDefaultValue(oOrigType.memberType(1))
        '???
        Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Case mCB.tk_enum
        'Sets the value of the enumerator to the first enumerator value
        'indicated by the TypeCode
        lVal = 0 'Always 0
    Case mCB.tk_string
        sVal = ""
    Case mCB.tk_sequence
        'Sets the current position to -1 and creates an empty sequence
        'Here is nothing to do! Use seqSetLength() to set the length.
    Case mCB.tk_array
        'Sets the current position to zero and (recursively)
        'initializes elements to their default value.
        lCompCnt = oOrigType.Length()
        ReDim oMemberVals(0 To lCompCnt - 1)
        lCurPos = 0
        For lCnt = 0 To lCompCnt - 1
            Set oMemberVals(lCnt) = New cOrbAny
            Call oMemberVals(lCnt).initByDefaultValue(oOrigType.memberType(lCnt))
        Next lCnt
    'Case mCB.tk_alias cannot occur
    Case mCB.tk_longlong, mCB.tk_ulonglong
        Set vVal = CDec(0)
    Case mCB.tk_longdouble
        'Set oVal = New cOrblongdouble
        Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Case mCB.tk_wchar
        iVal = 0
    Case mCB.tk_wstring
        sVal = ""
    Case mCB.tk_fixed
        'Set the current position to -1 and sets the value zero
        Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Case mCB.tk_value, mCB.tk_value_box
        Set oVal = Nothing
    Case mCB.tk_native
        Set oVal = Nothing
    Case mCB.tk_abstract_interface, mCB.tk_local_interface
        Set oVal = Nothing
    Case Else
        Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO, _
            "TC.kind = " & CStr(lOrigTcKind))
    End Select
    Set oType = oTC
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("initByDefaultValue")
End Sub

'Helper to init an any value using a TypeCode
'Note DynAnyFactory.createDynAny(in any value)
Public Sub initByAnyValue(ByVal oAny As cOrbAny)
    On Error GoTo ErrHandler
    Set oOrigType = oAny.getType().getOrigType()
    lOrigTcKind = oOrigType.kind()
    lCompCnt = 0 '???oAny.componentCount()
    lCurPos = -1 '??? 0
    Select Case lOrigTcKind
        
    Case Else
        Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO, _
            "TC.kind = " & CStr(lOrigTcKind))
    End Select
    Set oType = oAny.getType()
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("initByAnyValue")
End Sub

'Helper to write an any value using a TypeCode
Public Sub writeValue(ByVal oOut As cOrbStream)
    On Error GoTo ErrHandler
    Dim lCnt As Long
    Select Case lOrigTcKind
    Case mCB.tk_null, mCB.tk_void
    Case mCB.tk_short
        Call oOut.writeShort(iVal)
    Case mCB.tk_long
        Call oOut.writeLong(lVal)
    Case mCB.tk_ushort
        Call oOut.writeUshort(iVal)
    Case mCB.tk_ulong
        Call oOut.writeUlong(lVal)
    Case mCB.tk_float
        Call oOut.writeFloat(fVal)
    Case mCB.tk_double
        Call oOut.writeDouble(dVal)
    Case mCB.tk_boolean
        Call oOut.writeBoolean(bVal)
    Case mCB.tk_char
        Call oOut.writeChar(cVal)
    Case mCB.tk_octet
        Call oOut.writeOctet(cVal)
    Case mCB.tk_any
        Call oOut.writeAny(oVal)
    Case mCB.tk_TypeCode
        Call oOut.writeTypeCode(oVal)
    Case mCB.tk_Principal
        Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Case mCB.tk_objref
        Call oOut.writeObject(oVal)
    Case mCB.tk_struct, mCB.tk_except
        For lCnt = 0 To lCompCnt - 1
            Call oMemberVals(lCnt).writeValue(oOut)
        Next lCnt
    Case mCB.tk_union
        For lCnt = 0 To lCompCnt - 1
            Call oMemberVals(lCnt).writeValue(oOut)
        Next lCnt
    Case mCB.tk_enum
        Call oOut.writeUlong(lVal)
    Case mCB.tk_string
        Call oOut.writeString(sVal)
    Case mCB.tk_sequence
        Call oOut.writeUlong(lCompCnt)
        For lCnt = 0 To lCompCnt - 1
            Call oMemberVals(lCnt).writeValue(oOut)
        Next lCnt
    Case mCB.tk_array
        For lCnt = 0 To lCompCnt - 1
            Call oMemberVals(lCnt).writeValue(oOut)
        Next lCnt
    'Case mCB.tk_alias cannot occur
    Case mCB.tk_longlong
        Call oOut.writeLonglong(vVal)
    Case mCB.tk_ulonglong
        Call oOut.writeUlonglong(vVal)
    Case mCB.tk_longdouble
        Call oOut.writeLongdouble(oVal)
    Case mCB.tk_wchar
        Call oOut.writeWchar(iVal)
    Case mCB.tk_wstring
        Call oOut.writeWString(sVal)
    Case mCB.tk_fixed
        Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Case mCB.tk_value
        Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Case mCB.tk_value_box
        Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Case mCB.tk_native
        Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Case mCB.tk_abstract_interface, mCB.tk_local_interface
        Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Case Else
        Call mVBOrb.VBOrb.raiseMARSHAL(0, mVBOrb.VBOrb.CompletedNO, _
            "TC.kind = " & CStr(lOrigTcKind))
    End Select
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("writeValue")
End Sub

'Get the TypeCode
'RET:   CORBA::TypeCode
Public Function getType() As cOrbTypeCode
    Set getType = oType
End Function

'Get the original TypeCode (without alias)
'RET:   CORBA::TypeCode
Public Function getOrigType() As cOrbTypeCode
    Set getOrigType = oOrigType
End Function

Public Function setPos(ByVal index As Long) As Boolean
    If index < 0 Or index >= lCompCnt Then
        lCurPos = -1
        setPos = False
    Else
        lCurPos = index
        setPos = True
    End If
End Function

Public Sub rewind()
    Call setPos(0)
End Sub

Public Function nextPos() As Boolean
    nextPos = setPos(lCurPos + 1)
End Function

Public Function componentCount() As Long
    componentCount = lCompCnt
End Function

'DynAny current_component() raises(TypeMismatch);
'The returned any can be used to get/set the value of the current component.
Public Function currentComponent() As cOrbAny
    On Error GoTo ErrHandler
    If lCompCnt <= 0 And lOrigTcKind <> mCB.tk_sequence Then
        'TypeMismatch
        Call mVBOrb.VBOrb.raiseMARSHAL(0, mVBOrb.VBOrb.CompletedNO, _
            "TC.kind = " & CStr(lOrigTcKind))
    End If
    If lCurPos < 0 Or lCurPos >= lCompCnt Then
        Set currentComponent = Nothing
    Else
        Set currentComponent = oMemberVals(lCurPos)
    End If
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("currentComponent")
End Function

'Creates a new Any object whose value is a deep copy
Public Function copy() As cOrbAny
    Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
End Function

Public Sub insertBoolean(ByVal val As Boolean)
    If lCurPos < 0 Then
        If lOrigTcKind <> mCB.tk_boolean Then
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
        End If
        bVal = val
    Else 'currentComponent()
        Call oMemberVals(lCurPos).insertBoolean(val)
    End If
End Sub

Public Sub insertOctet(ByVal val As Byte)
    If lCurPos < 0 Then
        If lOrigTcKind <> mCB.tk_octet Then
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
        End If
        cVal = val
    Else 'currentComponent()
        Call oMemberVals(lCurPos).insertOctet(val)
    End If
End Sub

Public Sub insertChar(ByVal val As Byte)
    If lCurPos < 0 Then
        If lOrigTcKind <> mCB.tk_char Then
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
        End If
        cVal = val
    Else 'currentComponent()
        Call oMemberVals(lCurPos).insertChar(val)
    End If
End Sub

Public Sub insertShort(ByVal val As Integer)
    If lCurPos < 0 Then
        If lOrigTcKind <> mCB.tk_short Then
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
        End If
        iVal = val
    Else 'currentComponent()
        Call oMemberVals(lCurPos).insertShort(val)
    End If
End Sub

Public Sub insertUshort(ByVal val As Integer)
    If lCurPos < 0 Then
        If lOrigTcKind <> mCB.tk_ushort Then
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
        End If
        iVal = val
    Else 'currentComponent()
        Call oMemberVals(lCurPos).insertUshort(val)
    End If
End Sub

Public Sub insertLong(ByVal val As Long)
    If lCurPos < 0 Then
        If lOrigTcKind <> mCB.tk_long Then
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
        End If
        lVal = val
    Else 'currentComponent()
        Call oMemberVals(lCurPos).insertLong(val)
    End If
End Sub

Public Sub insertUlong(ByVal val As Long)
    If lCurPos < 0 Then
        If lOrigTcKind <> mCB.tk_ulong Then
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
        End If
        lVal = val
    Else 'currentComponent()
        Call oMemberVals(lCurPos).insertUlong(val)
    End If
End Sub

Public Sub insertFloat(ByVal val As Single)
    If lCurPos < 0 Then
        If lOrigTcKind <> mCB.tk_float Then
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
        End If
        fVal = val
    Else 'currentComponent()
        Call oMemberVals(lCurPos).insertFloat(val)
    End If
End Sub

Public Sub insertDouble(ByVal val As Double)
    If lCurPos < 0 Then
        If lOrigTcKind <> mCB.tk_double Then
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
        End If
        dVal = val
    Else 'currentComponent()
        Call oMemberVals(lCurPos).insertDouble(val)
    End If
End Sub

Public Sub insertString(ByVal val As String)
    If lCurPos < 0 Then
        If lCompCnt > 0 Or lOrigTcKind <> mCB.tk_string Then
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
        End If
        Dim maxLen As Long
        maxLen = oOrigType.Length
        If maxLen > 0 And Len(val) > maxLen Then
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
        End If
        sVal = val
    Else
        Call oMemberVals(lCurPos).insertString(val)
    End If
End Sub

Public Sub insertReference(ByVal val As cOrbObject)
    If lCurPos < 0 Then
        If lCompCnt > 0 Or lOrigTcKind <> mCB.tk_objref Then
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
        End If
        Set oVal = val
    Else
        Call oMemberVals(lCurPos).insertReference(val)
    End If
End Sub

Public Sub insertTypecode(ByVal val As cOrbTypeCode)
    If lCurPos < 0 Then
        If lCompCnt > 0 Or lOrigTcKind <> mCB.tk_TypeCode Then
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
        End If
        Set oVal = val
    Else
        Call oMemberVals(lCurPos).insertTypecode(val)
    End If
End Sub

Public Sub insertLonglong(ByVal val As Variant)
    If lCurPos < 0 Then
        If lCompCnt > 0 Or lOrigTcKind <> mCB.tk_longlong Then
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
        End If
        vVal = val
    Else
        Call oMemberVals(lCurPos).insertLonglong(val)
    End If
End Sub

Public Sub insertUlonglong(ByVal val As Variant)
    If lCurPos < 0 Then
        If lCompCnt > 0 Or lOrigTcKind <> mCB.tk_ulonglong Then
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
        End If
        vVal = val
    Else
        Call oMemberVals(lCurPos).insertUlonglong(val)
    End If
End Sub

Public Sub insertLongdouble(ByVal val As cOrblongdouble)
    If lCurPos < 0 Then
        If lCompCnt > 0 Or lOrigTcKind <> mCB.tk_longdouble Then
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
        End If
        Set oVal = val
    Else
        Call oMemberVals(lCurPos).insertLongdouble(val)
    End If
End Sub

Public Sub insertWchar(ByVal val As Integer)
    If lCurPos < 0 Then
        If lCompCnt > 0 Or lOrigTcKind <> mCB.tk_wchar Then
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
        End If
        iVal = val
    Else
        Call oMemberVals(lCurPos).insertWchar(val)
    End If
End Sub

Public Sub insertWstring(ByVal val As String)
    If lCurPos < 0 Then
        If lCompCnt > 0 Or lOrigTcKind <> mCB.tk_wstring Then
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
        End If
        Dim maxLen As Long
        maxLen = oOrigType.Length
        If maxLen > 0 And Len(val) > maxLen Then
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
        End If
        sVal = val
    Else
        Call oMemberVals(lCurPos).insertWstring(val)
    End If
End Sub

Public Sub insertAny(ByVal val As cOrbAny)
    If lCurPos < 0 Then
        If lCompCnt > 0 Or lOrigTcKind <> mCB.tk_any Then
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
        End If
        Set oVal = val
    Else
        Call oMemberVals(lCurPos).insertAny(val)
    End If
End Sub

'Public Sub insertDynAny(ByVal val As cOrbAny)
'End Sub

Public Sub insertVal(ByVal val As cOrbValueBase)
    If lCurPos < 0 Then
        If lCompCnt > 0 Or (lOrigTcKind <> mCB.tk_value _
            And lOrigTcKind <> mCB.tk_value_box) Then
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
        End If
        Set oVal = val
    Else
        Call oMemberVals(lCurPos).insertVal(val)
    End If
End Sub

Public Sub insertAbstract(ByVal val As cOrbAbstractBase)
    If lCurPos < 0 Then
        If lCompCnt > 0 Or lOrigTcKind <> mCB.tk_abstract_interface Then
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
        End If
        Set oVal = val
    Else
        Call oMemberVals(lCurPos).insertAbstract(val)
    End If
End Sub

Public Function getBoolean() As Boolean
    If lCurPos < 0 Then
        If lOrigTcKind <> mCB.tk_boolean Then
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
        End If
        getBoolean = bVal
    Else
        getBoolean = oMemberVals(lCurPos).getBoolean()
    End If
End Function

Public Function getOctet() As Byte
    If lCurPos < 0 Then
        If lOrigTcKind <> mCB.tk_octet Then
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
        End If
        getOctet = cVal
    Else
        getOctet = oMemberVals(lCurPos).getOctet()
    End If
End Function

Public Function getChar() As Byte
    If lCurPos < 0 Then
        If lOrigTcKind <> mCB.tk_char Then
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
        End If
        getChar = cVal
    Else
        getChar = oMemberVals(lCurPos).getChar()
    End If
End Function

Public Function getShort() As Integer
    If lCurPos < 0 Then
        If lOrigTcKind <> mCB.tk_short Then
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
        End If
        getShort = iVal
    Else
        getShort = oMemberVals(lCurPos).getShort()
    End If
End Function

Public Function getUshort() As Integer
    If lCurPos < 0 Then
        If lOrigTcKind <> mCB.tk_ushort Then
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
        End If
        getUshort = iVal
    Else
        getUshort = oMemberVals(lCurPos).getUshort()
    End If
End Function

Public Function getLong() As Long
    If lCurPos < 0 Then
        If lOrigTcKind <> mCB.tk_long Then
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
        End If
        getLong = lVal
    Else
        getLong = oMemberVals(lCurPos).getLong()
    End If
End Function

Public Function getUlong() As Long
    If lCurPos < 0 Then
        If lOrigTcKind <> mCB.tk_ulong Then
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
        End If
        getUlong = lVal
    Else
        getUlong = oMemberVals(lCurPos).getUlong()
    End If
End Function

Public Function getFloat() As Single
    If lCurPos < 0 Then
        If lOrigTcKind <> mCB.tk_float Then
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
        End If
        getFloat = fVal
    Else
        getFloat = oMemberVals(lCurPos).getFloat()
    End If
End Function

Public Function getDouble() As Double
    If lCurPos < 0 Then
        If lOrigTcKind <> mCB.tk_double Then
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
        End If
        getDouble = dVal
    Else
        getDouble = oMemberVals(lCurPos).getDouble()
    End If
End Function

Public Function isString() As Boolean
    If lCurPos < 0 Then
        If lCompCnt > 0 Or lOrigTcKind <> mCB.tk_string Then
            isString = False
        Else
            isString = True
        End If
    Else
        isString = oMemberVals(lCurPos).isString()
    End If
End Function

Public Function getString() As String
    If lCurPos < 0 Then
        If lCompCnt > 0 Or lOrigTcKind <> mCB.tk_string Then
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
        End If
        getString = sVal
    Else
        getString = oMemberVals(lCurPos).getString()
    End If
End Function

Public Function getReference() As cOrbObject
    If lCurPos < 0 Then
        If lCompCnt > 0 Or lOrigTcKind <> mCB.tk_objref Then
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
        End If
        Set getReference = oVal
    Else
        Set getReference = oMemberVals(lCurPos).getReference()
    End If
End Function

Public Function getTypecode() As cOrbTypeCode
    If lCurPos < 0 Then
        If lCompCnt > 0 Or lOrigTcKind <> mCB.tk_TypeCode Then
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
        End If
        Set getTypecode = oVal
    Else
        Set getTypecode = oMemberVals(lCurPos).getTypecode()
    End If
End Function

Public Function getLonglong() As Variant
    If lCurPos < 0 Then
        If lCompCnt > 0 Or lOrigTcKind <> mCB.tk_longlong Then
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
        End If
        getLonglong = vVal
    Else
        getLonglong = oMemberVals(lCurPos).getLonglong()
    End If
End Function

Public Function getUlonglong() As Variant
    If lCurPos < 0 Then
        If lCompCnt > 0 Or lOrigTcKind <> mCB.tk_ulonglong Then
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
        End If
        getUlonglong = vVal
    Else
        getUlonglong = oMemberVals(lCurPos).getUlonglong()
    End If
End Function

Public Function getLongdouble() As cOrblongdouble
    If lCurPos < 0 Then
        If lCompCnt > 0 Or lOrigTcKind <> mCB.tk_longdouble Then
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
        End If
        Set getLongdouble = oVal
    Else
        Set getLongdouble = oMemberVals(lCurPos).getLongdouble()
    End If
End Function

Public Function getWchar() As Integer
    If lCurPos < 0 Then
        If lOrigTcKind <> mCB.tk_wchar Then
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
        End If
        getWchar = iVal
    Else
        getWchar = oMemberVals(lCurPos).getWchar()
    End If
End Function

Public Function getWstring() As String
    If lCurPos < 0 Then
        If lCompCnt > 0 Or lOrigTcKind <> mCB.tk_wstring Then
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
        End If
        getWstring = sVal
    Else
        getWstring = oMemberVals(lCurPos).getWstring()
    End If
End Function

Public Function getAny() As cOrbAny
    If lCurPos < 0 Then
        If lCompCnt > 0 Or lOrigTcKind <> mCB.tk_any Then
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
        End If
        Set getAny = oVal
    Else
        Set getAny = oMemberVals(lCurPos).getAny()
    End If
End Function

Public Function getVal() As cOrbValueBase
    If lCurPos < 0 Then
        If lCompCnt > 0 Or (lOrigTcKind <> mCB.tk_value _
            And lOrigTcKind <> mCB.tk_value_box) Then
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
        End If
        Set getVal = oVal
    Else
        Set getVal = oMemberVals(lCurPos).getVal()
    End If
End Function

Public Function getAbstract() As cOrbAbstractBase
    If lCurPos < 0 Then
        If lCompCnt > 0 Or lOrigTcKind <> mCB.tk_abstract_interface Then
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
        End If
        Set getAbstract = oVal
    Else
        Set getAbstract = oMemberVals(lCurPos).getAbstract()
    End If
End Function

'string get_as_string();
Public Function enumGetAsString() As String
    If lCurPos < 0 Then
        If lOrigTcKind <> mCB.tk_enum Then
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
        End If
        enumGetAsString = oOrigType.memberName(lVal)
    Else
        enumGetAsString = oMemberVals(lCurPos).enumGetAsString()
    End If
End Function

'void set_as_string(in string val) raises(InvalidValue);
Public Sub enumSetAsString(ByVal val As String)
    If lCurPos < 0 Then
        If lOrigTcKind <> mCB.tk_enum Then
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
        End If
        Dim lULVal As Long
        For lULVal = 0 To oOrigType.memberCount - 1
            If oOrigType.memberName(lULVal) = val Then
                Exit For
            End If
        Next lULVal
        If lULVal >= oOrigType.memberCount Then
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO, _
                "Invalid Value")
        End If
        lVal = lULVal
    Else 'currentComponent()
        Call oMemberVals(lCurPos).enumSetAsString(val)
    End If
End Sub

'unsigned long get_as_ulong();
Public Function enumGetAsUlong()
    If lCurPos < 0 Then
        If lOrigTcKind <> mCB.tk_enum Then
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
        End If
        enumGetAsUlong = lVal
    Else
        enumGetAsUlong = oMemberVals(lCurPos).enumGetAsUlong()
    End If
End Function

'void set_as_ulong(in unsigned long val) raises(InvalidValue);
Public Sub enumSetAsUlong(ByVal val As Long)
    If lCurPos < 0 Then
        If lOrigTcKind <> mCB.tk_enum Then
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
        End If
        If val < 0 Or val >= oOrigType.memberCount Then
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO, _
                "Invalid Value")
        End If
        lVal = val
    Else 'currentComponent()
        Call oMemberVals(lCurPos).enumSetAsUlong(val)
    End If
End Sub

'unsigned long get_length();
Public Function seqGetLength() As Long
    On Error GoTo ErrHandler
    If lOrigTcKind <> mCB.tk_sequence Then
        Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
    End If
    seqGetLength = lCompCnt
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("seqGetLength")
End Function

'void set_length(in unsigned long len) raises(InvalidValue);
Public Sub seqSetLength(ByVal newLen As Long)
    On Error GoTo ErrHandler
    Dim oEx As cOrbException
    If lOrigTcKind <> mCB.tk_sequence Then
        Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
    End If
    If newLen > lCompCnt Then
        Dim lCnt As Long
        If oOrigType.Length() > 0 And newLen > oOrigType.Length() Then
            'Set oEx = New cDADynAnyInvalidValue???
            'Call oEx.addInfos(PostDescr:="newLen = " & CStr(newLen))
            'Call mVBOrb.raiseUserEx(oEx)
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO, _
                "Out of sequence bounds " & newLen & " > " & oOrigType.Length())
        End If
        ReDim Preserve oMemberVals(0 To newLen - 1)
        For lCnt = lCompCnt To newLen - 1
            Set oMemberVals(lCnt) = New cOrbAny
            Call oMemberVals(lCnt).initByDefaultValue(oOrigType.contentType())
        Next lCnt
        If lCurPos < 0 Then
            lCurPos = lCompCnt
        End If
        lCompCnt = newLen
    ElseIf newLen < lCompCnt Then
        If newLen <= 0 Then
            Erase oMemberVals
            lCompCnt = 0
        Else
            ReDim Preserve oMemberVals(0 To newLen - 1)
            lCompCnt = newLen
        End If
        If lCurPos >= lCompCnt Then
            lCurPos = -1
        End If
    End If
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("seqSetLength")
End Sub

'DynAny get_discriminator();
Public Function unionGetDiscriminator() As cOrbAny
    On Error GoTo ErrHandler
    If lOrigTcKind <> mCB.tk_union Then
        Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
    End If
    Set unionGetDiscriminator = oMemberVals(0)
    '???
    Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("unionGetDiscriminator")
End Function

'void set_discriminator(in DynAny d) raises(TypeMismatch);
Public Sub unionSetDiscriminator(ByVal disc As cOrbAny)
    On Error GoTo ErrHandler
    Dim oEx As cOrbException
    If lOrigTcKind <> mCB.tk_union Then
        Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
    End If
    Set oMemberVals(0) = disc
    '???
    Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("unionSetDiscriminator")
End Sub

'void set_to_default_member() raises(TypeMismatch);
Public Sub unionSetToDefaultMember()
    On Error GoTo ErrHandler
    Dim oEx As cOrbException
    If lOrigTcKind <> mCB.tk_union Then
        Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
    End If
    lCurPos = 0
    lCompCnt = 2
    '???
    Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("unionSetToDefaultMember")
End Sub

'void set_to_no_active_member() raises(TypeMismatch);
Public Sub unionSetToNoActiveMember()
    On Error GoTo ErrHandler
    Dim oEx As cOrbException
    If lOrigTcKind <> mCB.tk_union Then
        Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
    End If
    lCurPos = 0
    lCompCnt = 1
    '???
    Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("unionSetToNoActiveMember")
End Sub

'boolean has_no_active_member();
Public Function unionHasNoActiveMember() As Boolean
    On Error GoTo ErrHandler
    If lOrigTcKind <> mCB.tk_union Then
        Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
    End If
    unionHasNoActiveMember = lCurPos = 0
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("unionHasNoActiveMember")
End Function

'DynAny member() raises(InvalidValue);
Public Function unionMember() As cOrbAny
    On Error GoTo ErrHandler
    Dim oEx As cOrbException
    If lOrigTcKind <> mCB.tk_union Then
        Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO)
    End If
    Set unionMember = oMemberVals(1)
    '???
    Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("unionMember")
End Function

