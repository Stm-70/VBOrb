VERSION 1.0 CLASS
BEGIN
  MultiUse = -1  'True
END
Attribute VB_Name = "cOrbTypeCode"
Attribute VB_GlobalNameSpace = False
Attribute VB_Creatable = True
Attribute VB_PredeclaredId = False
Attribute VB_Exposed = True
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

'Set DebugMode = 0 to deactivate debug code in this class
#Const DebugMode = 0

#If DebugMode Then
    Private lClassDebugID As Long
#End If

'CORBA pseudo object TypeCode

'For all TypeCode kinds
Private lTcKind As Long

'For tk_objref, tk_struct, tk_union, tk_enum, tk_alias,
'    tk_value, tk_value_box, tk_native, tk_abstract_interface
'    tk_local_interface, tk_except, tk_component, tk_home, tk_event
Private sTcId As String
Private sTcName As String

'Length: For tk_string, tk_wstring, tk_sequence, and tk_array
'Count:  For tk_struct, tk_union, tk_enum, tk_value, tk_except, tk_event
Private lTcLength As Long

'For tk_fixed
Private iDigits As Integer
Private iScale As Integer

'For tk_sequence, tk_array, tk_value_box and tk_alias
'Discriminant type: tk_union
'Concrete base type: tk_value, tk_event
Private oContentType As cOrbTypeCode

'For tk_struct, tk_union, tk_enum, tk_value, tk_except, tk_event
Private sMemberNames() As String
Private oMemberTypes() As cOrbTypeCode

'For tk_union
Private lDefaultIndex As Long
Private oMemberLabels() As cOrbAny

'For tk_value, tk_event
Private iTypeModifier As Integer
Private iMemberVisibilities() As Integer

Private Sub Class_Initialize()
    lTcKind = -2
    #If DebugMode Then
        lClassDebugID = mVBOrb.getNextClassDebugID()
        Debug.Print "'" & TypeName(Me) & "' " & lClassDebugID & " initialized"
    #End If
End Sub

Private Sub Class_Terminate()
    'Release something which VB cannot know if required
    #If DebugMode Then
        Debug.Print "'" & TypeName(Me) & "' " & CStr(lClassDebugID) & " terminated"
    #End If
End Sub

#If DebugMode Then
    Friend Property Get ClassDebugID() As Long
        ClassDebugID = lClassDebugID
    End Property
#End If

Private Sub checkId(ByRef id As String)
    On Error GoTo ErrHandler
    If Len(id) = 0 Then Exit Sub
    Dim lPos As Long
    lPos = InStr(id, ":")
    If lPos <= 1 Or lPos = Len(id) Then
        Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO, _
            "Invalid ID '" & id & "'")
    End If
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("checkId")
End Sub

Private Sub checkName(ByRef name As String)
    On Error GoTo ErrHandler
    If Len(name) = 0 Then Exit Sub
    Dim lPos As Long
    Dim iChr As Integer
    lPos = 1
    iChr = Asc(Mid$(name, lPos))
    If (iChr < Asc("A") Or iChr > Asc("Z")) _
        And (iChr < Asc("a") Or iChr > Asc("z")) Then
        Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO, _
            "Invalid name '" & name & "'")
    End If
    For lPos = 2 To Len(name)
        If (iChr < Asc("0") Or iChr > Asc("9")) _
            And (iChr < Asc("A") Or iChr > Asc("Z")) _
            And (iChr < Asc("a") Or iChr > Asc("z")) Then
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO, _
                "Invalid name '" & name & "'")
        End If
    Next lPos
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("checkName")
End Sub

Private Sub checkType(ByVal tc As cOrbTypeCode)
    On Error GoTo ErrHandler
    Dim lKind As Long
    lKind = tc.getOrigType().kind
    If lKind = mCB.tk_null Or lKind = mCB.tk_void Or lKind = mCB.tk_except Then
        Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO, _
            "Invalid TypeCode " & CStr(lKind))
    End If
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("checkType")
End Sub

Friend Sub init2PrimitiveTc(ByVal TcId As String, ByVal kind As Long)
    On Error GoTo ErrHandler
    sTcId = TcId
    Select Case kind
    Case mCB.tk_null, mCB.tk_void, mCB.tk_short, mCB.tk_long
    Case mCB.tk_ushort, mCB.tk_ulong, mCB.tk_float, mCB.tk_double
    Case mCB.tk_boolean, mCB.tk_char, mCB.tk_octet, mCB.tk_any
    Case mCB.tk_TypeCode, mCB.tk_Principal
    Case mCB.tk_objref
        Call init2RecursiveTc("IDL:omg.org/CORBA/Object:1.0")
        Call setRecTc2InterfaceTc("Object")
    Case mCB.tk_string
    Case mCB.tk_longlong, mCB.tk_ulonglong, mCB.tk_longdouble
    Case mCB.tk_wchar, mCB.tk_wstring, mCB.tk_fixed
    Case mCB.tk_value
        Call init2RecursiveTc("IDL:omg.org/CORBA/ValueBase:1.0")
        Call setRecTc2ValueTc("ValueBase", mCB.VM_NONE, _
            Nothing, New cCBValueMemberSeq) 'VM_NONE: See TypeCode constants
    'case mcb.tk_component
    'IDL:omg.org/Components/CCMObject:1.0
    'mcb.tk_home???
    'IDL:omg.org/Components/CCMHome:1.0
    'case mcb.tk_event???
    'IDL:omg.org/Components/EventBase:1.0
    Case Else
        Dim oEx As cOrbException
        Set oEx = New cOrbTypeCodeBadKind
        Call oEx.addInfos(PostDescr:="lTCKind = " & CStr(kind))
        Call mVBOrb.raiseUserEx(oEx)
    End Select
    lTcKind = kind
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("init2PrimitiveTc")
End Sub

Friend Sub init2stringTc(ByVal ChId As String, ByVal bound As Long)
    On Error GoTo ErrHandler
    sTcId = ChId
    lTcLength = bound
    lTcKind = mCB.tk_string
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("init2stringTc")
End Sub

Friend Sub init2WstringTC(ByVal ChId As String, ByVal bound As Long)
    On Error GoTo ErrHandler
    sTcId = ChId
    lTcLength = bound
    lTcKind = mCB.tk_wstring
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("init2WstringTC")
End Sub

Friend Sub init2FixedTc(ByVal ChId As String, ByVal digits As Integer, _
    ByVal scale_ As Integer)
    On Error GoTo ErrHandler
    sTcId = ChId
    iDigits = digits
    iScale = scale_
    lTcKind = mCB.tk_fixed
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("init2FixedTc")
End Sub

Friend Sub init2SequenceTc(ByVal ChId As String, ByVal bound As Long, _
    ByVal element_type As cOrbTypeCode)
    On Error GoTo ErrHandler
    sTcId = ChId
    lTcLength = bound
    Call checkType(element_type)
    Set oContentType = element_type
    lTcKind = mCB.tk_sequence
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("init2SequenceTc")
End Sub

Friend Sub init2ArrayTc(ByVal ChId As String, ByVal Length As Long, _
    ByVal element_type As cOrbTypeCode)
    On Error GoTo ErrHandler
    sTcId = ChId
    lTcLength = Length
    Call checkType(element_type)
    Set oContentType = element_type
    lTcKind = mCB.tk_array
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("init2ArrayTc")
End Sub

Friend Sub init2RecursiveTc(ByVal id As String)
    Call checkId(id)
    sTcId = id
    lTcKind = -1
End Sub

'Is recursive and setable to kind
Friend Function isCompatible(ByVal kind As Long) As Boolean
    If lTcKind = kind Then
        isCompatible = True
    ElseIf lTcKind = -1 Then
        Select Case kind
        Case mCB.tk_objref, mCB.tk_struct, mCB.tk_union, mCB.tk_enum, _
            mCB.tk_alias, mCB.tk_value, mCB.tk_value_box, mCB.tk_native, _
            mCB.tk_abstract_interface, mCB.tk_local_interface, mCB.tk_except
            '???,  tk_component, tk_home, mCB.tk_event
            isCompatible = True
        End Select
    End If
End Function

Friend Function getChId() As String
    getChId = sTcId
End Function

'Break circular references
Public Sub destroy()
    On Error GoTo ErrHandler
    If lTcKind = -2 Then
        Exit Sub
    End If
    Select Case lTcKind
    Case -2, -1, mCB.tk_null, mCB.tk_void, mCB.tk_short, mCB.tk_long
    Case mCB.tk_ushort, mCB.tk_ulong, mCB.tk_float, mCB.tk_double
    Case mCB.tk_boolean, mCB.tk_char, mCB.tk_octet, mCB.tk_any
    Case mCB.tk_TypeCode, mCB.tk_Principal, mCB.tk_objref
    Case mCB.tk_struct
        Erase oMemberTypes
    Case mCB.tk_union
        Set oContentType = Nothing
        Erase oMemberTypes
        Erase oMemberLabels
    Case mCB.tk_enum
        Erase oMemberTypes
    Case mCB.tk_string
    Case mCB.tk_sequence
        Set oContentType = Nothing
    Case mCB.tk_array
        Set oContentType = Nothing
    Case mCB.tk_alias
        Set oContentType = Nothing
    Case mCB.tk_except
        Erase oMemberTypes
    Case mCB.tk_longlong
    Case mCB.tk_ulonglong, mCB.tk_longdouble, mCB.tk_wchar, mCB.tk_wstring
    Case mCB.tk_fixed
    Case mCB.tk_value
        Set oContentType = Nothing
        Erase oMemberTypes
    Case mCB.tk_value_box
        Set oContentType = Nothing
    Case mCB.tk_native
    Case mCB.tk_abstract_interface, mCB.tk_local_interface
    Case mCB.tk_component, mCB.tk_home
    Case mCB.tk_event
        Set oContentType = Nothing
        Erase oMemberTypes
    Case Else
        Call mVBOrb.VBOrb.raiseINTERNAL(0, mVBOrb.VBOrb.CompletedNO, _
            "lTCKind = " & CStr(lTcKind))
    End Select
    lTcKind = -2
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("destroy")
End Sub

Public Sub setRecTc2StructTc(ByVal name As String, _
    ByVal members As cCBStructMemberSeq)
    On Error GoTo ErrHandler
    lTcLength = members.Length
    If lTcLength > 0 Then
        ReDim sMemberNames(0 To lTcLength - 1)
        ReDim oMemberTypes(0 To lTcLength - 1)
    End If
    Dim oMem As cCBStructMember
    Dim li As Long
    For li = 0 To lTcLength - 1
        Set oMem = members.Item(li)
        Call checkName(oMem.name)
        sMemberNames(li) = oMem.name
        Call checkType(oMem.p_type)
        Set oMemberTypes(li) = oMem.p_type
    Next li
    Call checkName(name)
    sTcName = name
    lTcKind = mCB.tk_struct
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("setRecTc2StructTc")
End Sub

Public Sub setRecTc2UnionTc(ByVal name As String, _
    ByVal discriminator_type As cOrbTypeCode, _
    ByVal members As cCBUnionMemberSeq)
    On Error GoTo ErrHandler
    Dim lKind As Long
    lKind = discriminator_type.getOrigType().kind
    If lKind <> mCB.tk_short And lKind <> mCB.tk_long And _
        lKind <> mCB.tk_ushort And lKind <> mCB.tk_ulong And _
        lKind <> mCB.tk_boolean And lKind <> mCB.tk_char And _
        lKind <> mCB.tk_enum And lKind <> mCB.tk_longlong And _
        lKind <> mCB.tk_ulonglong And lKind <> mCB.tk_wchar Then
        Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO, _
            "Invalid TypeCode " & CStr(lKind))
    End If
    Set oContentType = discriminator_type
    lTcLength = members.Length
    If lTcLength > 0 Then
        ReDim sMemberNames(0 To lTcLength - 1)
        ReDim oMemberTypes(0 To lTcLength - 1)
        ReDim oMemberLabels(0 To lTcLength - 1)
    End If
    Dim oMem As cCBUnionMember
    Dim li As Long
    For li = 0 To lTcLength - 1
        Set oMem = members.Item(li)
        Call checkName(oMem.name)
        sMemberNames(li) = oMem.name
        Call checkType(oMem.p_type)
        Set oMemberTypes(li) = oMem.p_type
        Set oMemberLabels(li) = oMem.label
    Next li
    lDefaultIndex = -1
    For li = 0 To lTcLength
        If oMemberLabels(li).getType.kind() = mCB.tk_octet Then
            lDefaultIndex = li
            Exit For
        End If
    Next li
    Call checkName(name)
    sTcName = name
    lTcKind = mCB.tk_union
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("setRecTc2UnionTc")
End Sub

Public Sub setRecTc2EnumTc(ByVal name As String, _
    ByVal members As c_StringSeq)
    On Error GoTo ErrHandler
    lTcLength = members.Length
    If lTcLength > 0 Then
        ReDim sMemberNames(0 To lTcLength - 1)
    End If
    Dim sMemName As String
    Dim li As Long
    For li = 0 To lTcLength - 1
        sMemName = members.Item(li)
        Call checkName(sMemName)
        sMemberNames(li) = sMemName
    Next li
    Call checkName(name)
    sTcName = name
    lTcKind = mCB.tk_enum
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("setRecTc2EnumTc")
End Sub

Public Sub setRecTc2AliasTc(ByVal name As String, _
    ByVal original_type As cOrbTypeCode)
    On Error GoTo ErrHandler
    Call checkName(name)
    sTcName = name
    Call checkType(original_type)
    Set oContentType = original_type
    lTcKind = mCB.tk_alias
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("setRecTc2AliasTc")
End Sub

Public Sub setRecTc2ExceptionTc(ByVal name As String, _
    ByVal members As cCBStructMemberSeq)
    On Error GoTo ErrHandler
    lTcLength = members.Length
    If lTcLength > 0 Then
        ReDim sMemberNames(0 To lTcLength - 1)
        ReDim oMemberTypes(0 To lTcLength - 1)
    End If
    Dim oMem As cCBStructMember
    Dim li As Long
    For li = 0 To lTcLength - 1
        Set oMem = members.Item(li)
        Call checkName(oMem.name)
        sMemberNames(li) = oMem.name
        Call checkType(oMem.p_type)
        Set oMemberTypes(li) = oMem.p_type
    Next li
    Call checkName(name)
    sTcName = name
    lTcKind = mCB.tk_except
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("setRecTc2ExceptionTc")
End Sub

Public Sub setRecTc2InterfaceTc(ByVal name As String)
    On Error GoTo ErrHandler
    Call checkName(name)
    sTcName = name
    lTcKind = mCB.tk_objref
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("setRecTc2InterfaceTc")
End Sub

Public Sub setRecTc2ValueTc(ByVal name As String, _
    ByVal type_modifier As Integer, ByVal concrete_base As cOrbTypeCode, _
    ByVal members As cCBValueMemberSeq)
    On Error GoTo ErrHandler
    Call checkName(name)
    sTcName = name
    iTypeModifier = type_modifier
    If Not concrete_base Is Nothing Then
        Dim lKind As Long
        lKind = concrete_base.getOrigType().kind
        If lKind <> mCB.tk_value Then
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO, _
                "Invalid TypeCode " & CStr(lKind))
        End If
    End If
    Set oContentType = concrete_base
    lTcLength = members.Length
    If lTcLength > 0 Then
        ReDim sMemberNames(0 To lTcLength - 1)
        ReDim oMemberTypes(0 To lTcLength - 1)
        ReDim iMemberVisibilities(0 To lTcLength - 1)
    End If
    Dim oMem As cCBValueMember
    Dim li As Long
    For li = 0 To lTcLength - 1
        Set oMem = members.Item(li)
        Call checkName(oMem.name)
        sMemberNames(li) = oMem.name
        Call checkType(oMem.p_type)
        Set oMemberTypes(li) = oMem.p_type
        iMemberVisibilities(li) = oMem.access
    Next li
    lTcKind = mCB.tk_value
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("setRecTc2ValueTc")
End Sub

Public Sub setRecTc2ValueBoxTc(ByVal name As String, _
    ByVal boxed_type As cOrbTypeCode)
    On Error GoTo ErrHandler
    Call checkName(name)
    sTcName = name
    Call checkType(boxed_type)
    Set oContentType = boxed_type
    lTcKind = mCB.tk_value_box
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("setRecTc2ValueBoxTc")
End Sub

Public Sub setRecTc2NativeTc(ByVal name As String)
    On Error GoTo ErrHandler
    Call checkName(name)
    sTcName = name
    lTcKind = mCB.tk_native
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("setRecTc2NativeTc")
End Sub

Public Sub setRecTc2AbstractInterfaceTc(ByVal name As String)
    On Error GoTo ErrHandler
    Call checkName(name)
    sTcName = name
    lTcKind = mCB.tk_abstract_interface
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("setRecTc2AbstractInterfaceTc")
End Sub

Public Sub setRecTc2LocalInterfaceTc(ByVal name As String)
    On Error GoTo ErrHandler
    Call checkName(name)
    sTcName = name
    lTcKind = mCB.tk_local_interface
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("setRecTc2LocalInterfaceTc")
End Sub

'Init TypeCode by reading (used by cOrbStream.readTypeCode())
'IN:    kind        tk_struct, tk_union, tk_enum, tk_except, tk_value, tk_event
'IN:    oIn         Input stream
Friend Sub initByRead(ByVal kind As Long, ByVal oIn As cOrbStream)
    On Error GoTo ErrHandler
    Dim lCnt As Long
    sTcId = oIn.readString()
    sTcName = oIn.readString()
    Select Case kind
    Case mCB.tk_struct
        lTcLength = oIn.readUlong()
        ReDim sMemberNames(0 To lTcLength - 1)
        ReDim oMemberTypes(0 To lTcLength - 1)
        For lCnt = 0 To lTcLength - 1
            sMemberNames(lCnt) = oIn.readString()
            Set oMemberTypes(lCnt) = oIn.readTypeCode()
        Next lCnt
    Case mCB.tk_union
        Set oContentType = oIn.readTypeCode()
        lDefaultIndex = oIn.readLong()
        lTcLength = oIn.readUlong()
        ReDim oMemberLabels(0 To lTcLength - 1)
        ReDim sMemberNames(0 To lTcLength - 1)
        ReDim oMemberTypes(0 To lTcLength - 1)
        For lCnt = 0 To lTcLength - 1
            Set oMemberLabels(lCnt) = New cOrbAny
            Call oMemberLabels(lCnt).initByReadValue(oContentType, oIn)
            sMemberNames(lCnt) = oIn.readString()
            Set oMemberTypes(lCnt) = oIn.readTypeCode()
        Next lCnt
    Case mCB.tk_enum
        lTcLength = oIn.readUlong()
        ReDim sMemberNames(0 To lTcLength - 1)
        For lCnt = 0 To lTcLength - 1
            sMemberNames(lCnt) = oIn.readString()
        Next lCnt
    Case mCB.tk_except
        lTcLength = oIn.readUlong()
        ReDim sMemberNames(0 To lTcLength - 1)
        ReDim oMemberTypes(0 To lTcLength - 1)
        For lCnt = 0 To lTcLength - 1
            sMemberNames(lCnt) = oIn.readString()
            Set oMemberTypes(lCnt) = oIn.readTypeCode()
        Next lCnt
    Case mCB.tk_value, mCB.tk_event
        iTypeModifier = oIn.readShort()
        Set oContentType = oIn.readTypeCode()
        lTcLength = oIn.readUlong()
        ReDim sMemberNames(0 To lTcLength - 1)
        ReDim oMemberTypes(0 To lTcLength - 1)
        ReDim iMemberVisibilities(0 To lTcLength - 1)
        For lCnt = 0 To lTcLength - 1
            sMemberNames(lCnt) = oIn.readString()
            Set oMemberTypes(lCnt) = oIn.readTypeCode()
            iMemberVisibilities(lCnt) = oIn.readShort()
        Next lCnt
    Case Else
        Call mVBOrb.VBOrb.raiseINTERNAL(0, mVBOrb.VBOrb.CompletedNO, _
            "lTCKind = " & CStr(kind))
    End Select
    lTcKind = kind
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("initByRead")
End Sub

'Has a complex parameter list
Friend Function hasComplexPars() As Boolean
    Select Case kind
    Case mCB.tk_objref, mCB.tk_struct, mCB.tk_union, mCB.tk_enum, _
        mCB.tk_sequence, mCB.tk_array, mCB.tk_alias, mCB.tk_except, _
        mCB.tk_value, mCB.tk_value_box, mCB.tk_native, _
        mCB.tk_abstract_interface, mCB.tk_local_interface, _
        mCB.tk_component, mCB.tk_home, mCB.tk_event
        hasComplexPars = True
    End Select
End Function

Friend Sub writeMe(ByVal oOut As cOrbStream)
    On Error GoTo ErrHandler
    Dim lCnt As Long
    'Call oOut.writeLong(lTcKind) is done by writeTypeCode()
    Select Case lTcKind
    Case mCB.tk_null, mCB.tk_void, mCB.tk_short, mCB.tk_long, _
        mCB.tk_ushort, mCB.tk_ulong, mCB.tk_float, mCB.tk_double, _
        mCB.tk_boolean, mCB.tk_char, mCB.tk_octet, mCB.tk_any, _
        mCB.tk_TypeCode, mCB.tk_Principal
    Case mCB.tk_objref 'complex type
        Call oOut.writeString(sTcId)
        Call oOut.writeString(sTcName)
    Case mCB.tk_struct 'complex type
        Call oOut.writeString(sTcId)
        Call oOut.writeString(sTcName)
        Call oOut.writeUlong(lTcLength)
        For lCnt = 0 To lTcLength - 1
            Call oOut.writeString(sMemberNames(lCnt))
            Call oOut.writeTypeCode(oMemberTypes(lCnt))
        Next lCnt
    Case mCB.tk_union 'complex type
        Call oOut.writeString(sTcId)
        Call oOut.writeString(sTcName)
        Call oOut.writeTypeCode(oContentType)
        Call oOut.writeLong(lDefaultIndex)
        Call oOut.writeUlong(lTcLength)
        For lCnt = 0 To lTcLength - 1
            Call oOut.writeAny(oMemberLabels(lCnt))
            Call oOut.writeString(sMemberNames(lCnt))
            Call oOut.writeTypeCode(oMemberTypes(lCnt))
        Next lCnt
    Case mCB.tk_enum 'complex type
        Call oOut.writeString(sTcId)
        Call oOut.writeString(sTcName)
        Call oOut.writeUlong(lTcLength)
        For lCnt = 0 To lTcLength - 1
            Call oOut.writeString(sMemberNames(lCnt))
        Next lCnt
    Case mCB.tk_string
        Call oOut.writeUlong(lTcLength)
    Case mCB.tk_sequence 'complex type
        Call oOut.writeTypeCode(oContentType)
        Call oOut.writeUlong(lTcLength)
    Case mCB.tk_array 'complex type
        Call oOut.writeTypeCode(oContentType)
        Call oOut.writeUlong(lTcLength)
    Case mCB.tk_alias 'complex type
        Call oOut.writeString(sTcId)
        Call oOut.writeString(sTcName)
        Call oOut.writeTypeCode(oContentType)
    Case mCB.tk_except 'complex type
        Call oOut.writeString(sTcId)
        Call oOut.writeString(sTcName)
        Call oOut.writeUlong(lTcLength)
        For lCnt = 0 To lTcLength - 1
            Call oOut.writeString(sMemberNames(lCnt))
            Call oOut.writeTypeCode(oMemberTypes(lCnt))
        Next lCnt
    Case mCB.tk_longlong, mCB.tk_ulonglong, mCB.tk_longdouble, mCB.tk_wchar
    Case mCB.tk_wstring
        Call oOut.writeLong(lTcLength)
    Case mCB.tk_fixed
        Call oOut.writeUshort(iDigits)
        Call oOut.writeShort(iScale)
    Case mCB.tk_value 'complex type
        Call oOut.writeString(sTcId)
        Call oOut.writeString(sTcName)
        Call oOut.writeShort(iTypeModifier)
        Call oOut.writeTypeCode(oContentType)
        Call oOut.writeUlong(lTcLength)
        For lCnt = 0 To lTcLength - 1
            Call oOut.writeString(sMemberNames(lCnt))
            Call oOut.writeTypeCode(oMemberTypes(lCnt))
            Call oOut.writeShort(iMemberVisibilities(lCnt))
        Next lCnt
    Case mCB.tk_value_box 'complex type
        Call oOut.writeString(sTcId)
        Call oOut.writeString(sTcName)
        Call oOut.writeTypeCode(oContentType)
    Case mCB.tk_native 'complex type
        Call oOut.writeString(sTcId)
        Call oOut.writeString(sTcName)
    Case mCB.tk_abstract_interface, mCB.tk_local_interface 'complex type
        Call oOut.writeString(sTcId)
        Call oOut.writeString(sTcName)
    Case mCB.tk_component, mCB.tk_home 'complex type
        Call oOut.writeString(sTcId)
        Call oOut.writeString(sTcName)
    Case mCB.tk_event 'complex type
        Call oOut.writeString(sTcId)
        Call oOut.writeString(sTcName)
        Call oOut.writeShort(iTypeModifier)
        Call oOut.writeTypeCode(oContentType)
        Call oOut.writeUlong(lTcLength)
        For lCnt = 0 To lTcLength - 1
            Call oOut.writeString(sMemberNames(lCnt))
            Call oOut.writeTypeCode(oMemberTypes(lCnt))
            Call oOut.writeShort(iMemberVisibilities(lCnt))
        Next lCnt
Case Else
        Call mVBOrb.VBOrb.raiseINTERNAL(0, mVBOrb.VBOrb.CompletedNO, _
            "lTCKind = " & CStr(lTcKind))
    End Select
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("writeMe")
End Sub

'Get the original TypeCode (without alias)
'For all TypeCode kinds
Public Function getOrigType(Optional ByVal LoopCnt As Long = 0) As cOrbTypeCode
    On Error GoTo ErrHandler
    LoopCnt = LoopCnt + 1
    If LoopCnt > 20 Then
        Call mVBOrb.VBOrb.raiseIMPLIMIT(0, mVBOrb.VBOrb.CompletedNO, _
            "tk_alias loop = " & CStr(LoopCnt))
    End If
    If lTcKind = mCB.tk_alias Then
        Set getOrigType = oContentType.getOrigType(LoopCnt)
    Else
        Set getOrigType = Me
    End If
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("getOrigType")
End Function

'For all TypeCode kinds
'equal()
Public Function equal(ByVal tc As cOrbTypeCode) As Boolean
    On Error GoTo ErrHandler
    If tc Is Nothing Then
        equal = False
    ElseIf tc = Me Then
        equal = True
    ElseIf tc.kind() <> lTcKind Then
        equal = False
    Else
        'Please write your code here
        Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    End If
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("equal")
End Function

'For all TypeCode kinds
'equivalent()
Public Function equivalent(ByVal tc As cOrbTypeCode) As Boolean
    On Error GoTo ErrHandler
    'Please write your code here
    Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("equivalent")
End Function

'For all TypeCode kinds
'get_compact_typecode()
Public Function getCompactTypecode() As cOrbTypeCode
    On Error GoTo ErrHandler
    'Please write your code here
    Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
    Exit Function
ErrHandler:
    Set getCompactTypecode = Nothing
    Call mVBOrb.ErrReraise("getCompactTypecode")
End Function

'For all TypeCode kinds
'kind()
Public Function kind() As Long
    On Error GoTo ErrHandler
    kind = lTcKind
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("kind")
End Function

'For tk_objref, tk_struct, tk_union, tk_enum, tk_alias,
'    tk_value, tk_value_box, tk_native, tk_abstract_interface,
'    tk_local_interface, tk_except, tk_component, tk_home, tk_event, -1
'RepositoryId id() raises(cOrbTypeCodeBadKind)
Public Function id() As String
    On Error GoTo ErrHandler
    Dim oEx As cOrbException
    Select Case lTcKind
    Case -1, mCB.tk_objref, mCB.tk_struct, mCB.tk_union, mCB.tk_enum, _
        mCB.tk_alias, mCB.tk_value, mCB.tk_value_box, mCB.tk_native, _
        mCB.tk_abstract_interface, mCB.tk_local_interface, mCB.tk_except, _
        mCB.tk_component, mCB.tk_home, mCB.tk_event
        id = sTcId
    Case Else
        Set oEx = New cOrbTypeCodeBadKind
        Call oEx.addInfos(PostDescr:="lTCKind = " & CStr(lTcKind))
        Call mVBOrb.raiseUserEx(oEx)
    End Select
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("id")
End Function

'for tk_objref, tk_struct, tk_union, tk_enum, tk_alias,
'    tk_value, tk_value_box, tk_native, tk_abstract_interface
'    tk_local_interface, tk_except, tk_component, tk_home, tk_event
'name() raises(cOrbTypeCodeBadKind)
Public Function name() As String
    On Error GoTo ErrHandler
    Dim oEx As cOrbException
    Select Case lTcKind
    Case mCB.tk_objref, mCB.tk_struct, mCB.tk_union, mCB.tk_enum, _
        mCB.tk_alias, mCB.tk_value, mCB.tk_value_box, mCB.tk_native, _
        mCB.tk_abstract_interface, mCB.tk_local_interface, mCB.tk_except, _
        mCB.tk_component, mCB.tk_home, mCB.tk_event
    Case Else
        Set oEx = New cOrbTypeCodeBadKind
        Call oEx.addInfos(PostDescr:="lTCKind = " & CStr(lTcKind))
        Call mVBOrb.raiseUserEx(oEx)
    End Select
    name = sTcName
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("name")
End Function

'For tk_struct, tk_union, tk_enum, tk_value, tk_except, tk_event
'member_count() raises(cOrbTypeCodeBadKind)
Public Function memberCount() As Long
    On Error GoTo ErrHandler
    Dim oEx As cOrbException
    Select Case lTcKind
    Case mCB.tk_struct, mCB.tk_union, mCB.tk_enum, _
        mCB.tk_value, mCB.tk_except, mCB.tk_event
    Case Else
        Set oEx = New cOrbTypeCodeBadKind
        Call oEx.addInfos(PostDescr:="lTCKind = " & CStr(lTcKind))
        Call mVBOrb.raiseUserEx(oEx)
    End Select
    memberCount = lTcLength
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("memberCount")
End Function

'For tk_struct, tk_union, tk_enum, tk_value, tk_except, tk_event
'member_name() raises(cOrbTypeCodeBadKind, cOrbTypeCodeBounds)
Public Function memberName(ByVal index As Long) As String
    On Error GoTo ErrHandler
    Dim oEx As cOrbException
    If index < 0 Or index >= lTcLength Then
        Set oEx = New cOrbTypeCodeBounds
        Call oEx.addInfos(PostDescr:=CStr(index) & " >= " & CStr(lTcLength))
        Call mVBOrb.raiseUserEx(oEx)
    End If
    Select Case lTcKind
    Case mCB.tk_struct, mCB.tk_union, mCB.tk_enum, _
        mCB.tk_value, mCB.tk_except, mCB.tk_event
        memberName = sMemberNames(index)
    Case Else
        Set oEx = New cOrbTypeCodeBadKind
        Call oEx.addInfos(PostDescr:="lTCKind = " & CStr(lTcKind))
        Call mVBOrb.raiseUserEx(oEx)
    End Select
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("memberName")
End Function

'For tk_struct, tk_union, tk_value and tk_except
'member_type() raises(cOrbTypeCodeBadKind, cOrbTypeCodeBounds)
Public Function memberType(ByVal index As Long) As cOrbTypeCode
    On Error GoTo ErrHandler
    Dim oEx As cOrbException
    If index < 0 Or index >= lTcLength Then
        Set oEx = New cOrbTypeCodeBounds
        Call oEx.addInfos(PostDescr:=CStr(index) & " >= " & CStr(lTcLength))
        Call mVBOrb.raiseUserEx(oEx)
    End If
    Select Case lTcKind
    Case mCB.tk_struct, mCB.tk_union, mCB.tk_value, mCB.tk_except
        Set memberType = oMemberTypes(index)
    Case Else
        Set oEx = New cOrbTypeCodeBadKind
        Call oEx.addInfos(PostDescr:="lTCKind = " & CStr(lTcKind))
        Call mVBOrb.raiseUserEx(oEx)
    End Select
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("memberType")
End Function

'For tk_union
'member_label() raises(cOrbTypeCodeBadKind, cOrbTypeCodeBounds)
Public Function memberLabel(ByVal index As Long) As cOrbAny
    On Error GoTo ErrHandler
    Dim oEx As cOrbException
    If index < 0 Or index >= lTcLength Then
        Set oEx = New cOrbTypeCodeBounds
        Call oEx.addInfos(PostDescr:=CStr(index) & " >= " & CStr(lTcLength))
        Call mVBOrb.raiseUserEx(oEx)
    End If
    Select Case lTcKind
    Case mCB.tk_union
        Set memberLabel = oMemberLabels(index)
    Case Else
        Set oEx = New cOrbTypeCodeBadKind
        Call oEx.addInfos(PostDescr:="lTCKind = " & CStr(lTcKind))
        Call mVBOrb.raiseUserEx(oEx)
    End Select
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("memberLabel")
End Function

'For tk_union
'discriminator_type() raises(cOrbTypeCodeBadKind)
Public Function discriminatorType() As cOrbTypeCode
    On Error GoTo ErrHandler
    Dim oEx As cOrbException
    If lTcKind <> mCB.tk_union Then
        Set oEx = New cOrbTypeCodeBadKind
        Call oEx.addInfos(PostDescr:="lTCKind = " & CStr(lTcKind))
        Call mVBOrb.raiseUserEx(oEx)
    End If
    Set discriminatorType = oContentType
    Exit Function
ErrHandler:
    Set discriminatorType = Nothing
    Call mVBOrb.ErrReraise("discriminatorType")
End Function

'For tk_union
'default_index() raises(cOrbTypeCodeBadKind)
Public Function defaultIndex() As Long
    On Error GoTo ErrHandler
    Dim oEx As cOrbException
    If lTcKind <> mCB.tk_union Then
        Set oEx = New cOrbTypeCodeBadKind
        Call oEx.addInfos(PostDescr:="lTCKind = " & CStr(lTcKind))
        Call mVBOrb.raiseUserEx(oEx)
    End If
    defaultIndex = lDefaultIndex
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("defaultIndex")
End Function

'For tk_string, tk_wstring, tk_sequence, and tk_array
'length() raises(cOrbTypeCodeBadKind)
Public Function Length() As Long
    On Error GoTo ErrHandler
    Dim oEx As cOrbException
    Select Case lTcKind
    Case mCB.tk_string, mCB.tk_wstring, mCB.tk_sequence, mCB.tk_array
    Case Else
        Set oEx = New cOrbTypeCodeBadKind
        Call oEx.addInfos(PostDescr:="lTCKind = " & CStr(lTcKind))
        Call mVBOrb.raiseUserEx(oEx)
    End Select
    Length = lTcLength
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("length")
End Function

'For tk_sequence, tk_array, tk_value_box and tk_alias
'content_type() raises(cOrbTypeCodeBadKind)
Public Function contentType() As cOrbTypeCode
    On Error GoTo ErrHandler
    Dim oEx As cOrbException
    Select Case lTcKind
    Case mCB.tk_sequence, mCB.tk_array, mCB.tk_alias, mCB.tk_value_box
    Case Else
        Set oEx = New cOrbTypeCodeBadKind
        Call oEx.addInfos(PostDescr:="lTCKind = " & CStr(lTcKind))
        Call mVBOrb.raiseUserEx(oEx)
    End Select
    Set contentType = oContentType
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("contentType")
End Function

'For tk_fixed
'fixed_digits() raises(cOrbTypeCodeBadKind)
Public Function fixedDigits() As Integer
    On Error GoTo ErrHandler
    Dim oEx As cOrbException
    If lTcKind <> mCB.tk_fixed Then
        Set oEx = New cOrbTypeCodeBadKind
        Call oEx.addInfos(PostDescr:="lTCKind = " & CStr(lTcKind))
        Call mVBOrb.raiseUserEx(oEx)
    End If
    fixedDigits = iDigits
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("fixedDigits")
End Function

'For tk_fixed
'fixed_scale() raises(cOrbTypeCodeBadKind)
Public Function fixedScale() As Integer
    On Error GoTo ErrHandler
    Dim oEx As cOrbException
    If lTcKind <> mCB.tk_fixed Then
        Set oEx = New cOrbTypeCodeBadKind
        Call oEx.addInfos(PostDescr:="lTCKind = " & CStr(lTcKind))
        Call mVBOrb.raiseUserEx(oEx)
    End If
    fixedScale = iScale
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("fixedScale")
End Function

'For tk_value, tk_event
'member_visibility() raises(cOrbTypeCodeBadKind, cOrbTypeCodeBounds)
Public Function memberVisibility(ByVal index As Long) As Integer
    On Error GoTo ErrHandler
    Dim oEx As cOrbException
    If index < 0 Or index >= lTcLength Then
        Set oEx = New cOrbTypeCodeBounds
        Call oEx.addInfos(PostDescr:=CStr(index) & " >= " & CStr(lTcLength))
        Call mVBOrb.raiseUserEx(oEx)
    End If
    Select Case lTcKind
    Case mCB.tk_value, mCB.tk_event
        memberVisibility = iMemberVisibilities(index)
    Case Else
        Set oEx = New cOrbTypeCodeBadKind
        Call oEx.addInfos(PostDescr:="lTCKind = " & CStr(lTcKind))
        Call mVBOrb.raiseUserEx(oEx)
    End Select
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("memberVisibility")
End Function

'For tk_value, tk_event
'type_modifier() raises(cOrbTypeCodeBadKind)
Public Function typeModifier() As Integer
    On Error GoTo ErrHandler
    Dim oEx As cOrbException
    Select Case lTcKind
    Case mCB.tk_value, mCB.tk_event
        typeModifier = iTypeModifier
    Case Else
        Set oEx = New cOrbTypeCodeBadKind
        Call oEx.addInfos(PostDescr:="lTCKind = " & CStr(lTcKind))
        Call mVBOrb.raiseUserEx(oEx)
    End Select
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("typeModifier")
End Function

'For tk_value, tk_event
'concrete_base_type() raises(cOrbTypeCodeBadKind)
Public Function concreteBaseType() As cOrbTypeCode
    On Error GoTo ErrHandler
    Dim oEx As cOrbException
    Select Case lTcKind
    Case mCB.tk_value, mCB.tk_event
        Set concreteBaseType = oContentType
    Case Else
        Set oEx = New cOrbTypeCodeBadKind
        Call oEx.addInfos(PostDescr:="lTCKind = " & CStr(lTcKind))
        Call mVBOrb.raiseUserEx(oEx)
    End Select
    Exit Function
ErrHandler:
    Set concreteBaseType = Nothing
    Call mVBOrb.ErrReraise("concreteBaseType")
End Function

