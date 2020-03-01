Option Strict Off
Option Explicit On
<System.Runtime.InteropServices.ProgId("cOrbTypeCode_NET.cOrbTypeCode")> Public Class cOrbTypeCode
	'Copyright (c) 2002 Martin.Both
	
	'This library is free software; you can redistribute it and/or
	'modify it under the terms of the GNU Library General Public
	'License as published by the Free Software Foundation; either
	'version 2 of the License, or (at your option) any later version.
	
	'This library is distributed in the hope that it will be useful,
	'but WITHOUT ANY WARRANTY; without even the implied warranty of
	'MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
	'Library General Public License for more details.
	
	
	'Set DebugMode = 0 to deactivate debug code in this class
#Const DebugMode = 0
	
#If DebugMode Then
	'UPGRADE_NOTE: #If #EndIf block was not upgraded because the expression DebugMode did not evaluate to True or was not evaluated. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="27EE2C3C-05AF-4C04-B2AF-657B4FB6B5FC"'
	Private lClassDebugID As Long
#End If
	
	'CORBA pseudo object TypeCode
	
	'For all TypeCode kinds
	Private lTcKind As Integer
	
	'For tk_objref, tk_struct, tk_union, tk_enum, tk_alias,
	'    tk_value, tk_value_box, tk_native, tk_abstract_interface
	'    tk_local_interface, tk_except, tk_component, tk_home, tk_event
	Private sTcId As String
	Private sTcName As String
	
	'Length: For tk_string, tk_wstring, tk_sequence, and tk_array
	'Count:  For tk_struct, tk_union, tk_enum, tk_value, tk_except, tk_event
	Private lTcLength As Integer
	
	'For tk_fixed
	Private iDigits As Short
	Private iScale As Short
	
	'For tk_sequence, tk_array, tk_value_box and tk_alias
	'Discriminant type: tk_union
	'Concrete base type: tk_value, tk_event
	Private oContentType As cOrbTypeCode
	
	'For tk_struct, tk_union, tk_enum, tk_value, tk_except, tk_event
	Private sMemberNames() As String
	Private oMemberTypes() As cOrbTypeCode
	
	'For tk_union
	Private lDefaultIndex As Integer
	Private oMemberLabels() As cOrbAny
	
	'For tk_value, tk_event
	Private iTypeModifier As Short
	Private iMemberVisibilities() As Short
	
	'UPGRADE_NOTE: Class_Initialize was upgraded to Class_Initialize_Renamed. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="A9E4979A-37FA-4718-9994-97DD76ED70A7"'
	Private Sub Class_Initialize_Renamed()
		lTcKind = -2
#If DebugMode Then
		'UPGRADE_NOTE: #If #EndIf block was not upgraded because the expression DebugMode did not evaluate to True or was not evaluated. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="27EE2C3C-05AF-4C04-B2AF-657B4FB6B5FC"'
		lClassDebugID = mVBOrb.getNextClassDebugID()
		Debug.Print "'" & TypeName(Me) & "' " & lClassDebugID & " initialized"
#End If
	End Sub
	Public Sub New()
		MyBase.New()
		Class_Initialize_Renamed()
	End Sub
	
	'UPGRADE_NOTE: Class_Terminate was upgraded to Class_Terminate_Renamed. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="A9E4979A-37FA-4718-9994-97DD76ED70A7"'
	Private Sub Class_Terminate_Renamed()
		'Release something which VB cannot know if required
#If DebugMode Then
		'UPGRADE_NOTE: #If #EndIf block was not upgraded because the expression DebugMode did not evaluate to True or was not evaluated. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="27EE2C3C-05AF-4C04-B2AF-657B4FB6B5FC"'
		Debug.Print "'" & TypeName(Me) & "' " & CStr(lClassDebugID) & " terminated"
#End If
	End Sub
	Protected Overrides Sub Finalize()
		Class_Terminate_Renamed()
		MyBase.Finalize()
	End Sub
	
#If DebugMode Then
	'UPGRADE_NOTE: #If #EndIf block was not upgraded because the expression DebugMode did not evaluate to True or was not evaluated. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="27EE2C3C-05AF-4C04-B2AF-657B4FB6B5FC"'
	Friend Property Get ClassDebugID() As Long
	ClassDebugID = lClassDebugID
	End Property
#End If
	
	Private Sub checkId(ByRef id As String)
		On Error GoTo ErrHandler
		If Len(id) = 0 Then Exit Sub
		Dim lPos As Integer
		lPos = InStr(id, ":")
		If lPos <= 1 Or lPos = Len(id) Then
			Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO, "Invalid ID '" & id & "'")
		End If
		Exit Sub
ErrHandler: 
		Call mVBOrb.ErrReraise("checkId")
	End Sub
	
	Private Sub checkName(ByRef name As String)
		On Error GoTo ErrHandler
		If Len(name) = 0 Then Exit Sub
		Dim lPos As Integer
		Dim iChr As Short
		lPos = 1
		iChr = Asc(Mid(name, lPos))
		If (iChr < Asc("A") Or iChr > Asc("Z")) And (iChr < Asc("a") Or iChr > Asc("z")) Then
			Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO, "Invalid name '" & name & "'")
		End If
		For lPos = 2 To Len(name)
			If (iChr < Asc("0") Or iChr > Asc("9")) And (iChr < Asc("A") Or iChr > Asc("Z")) And (iChr < Asc("a") Or iChr > Asc("z")) Then
				Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO, "Invalid name '" & name & "'")
			End If
		Next lPos
		Exit Sub
ErrHandler: 
		Call mVBOrb.ErrReraise("checkName")
	End Sub
	
	Private Sub checkType(ByVal tc As cOrbTypeCode)
		On Error GoTo ErrHandler
		Dim lKind As Integer
		lKind = tc.getOrigType().kind
		If lKind = mCB.tk_null Or lKind = mCB.tk_void Or lKind = mCB.tk_except Then
			Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO, "Invalid TypeCode " & CStr(lKind))
		End If
		Exit Sub
ErrHandler: 
		Call mVBOrb.ErrReraise("checkType")
	End Sub
	
	Friend Sub init2PrimitiveTc(ByVal TcId As String, ByVal kind As Integer)
		On Error GoTo ErrHandler
		sTcId = TcId
		Dim oEx As _cOrbException
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
				Call setRecTc2ValueTc("ValueBase", mCB.VM_NONE, Nothing, New cCBValueMemberSeq) 'VM_NONE: See TypeCode constants
				'case mcb.tk_component
				'IDL:omg.org/Components/CCMObject:1.0
				'mcb.tk_home???
				'IDL:omg.org/Components/CCMHome:1.0
				'case mcb.tk_event???
				'IDL:omg.org/Components/EventBase:1.0
			Case Else
				oEx = New cOrbTypeCodeBadKind
				Call oEx.addInfos(PostDescr:="lTCKind = " & CStr(kind))
				Call mVBOrb.raiseUserEx(oEx)
		End Select
		lTcKind = kind
		Exit Sub
ErrHandler: 
		Call mVBOrb.ErrReraise("init2PrimitiveTc")
	End Sub
	
	Friend Sub init2stringTc(ByVal ChId As String, ByVal bound As Integer)
		On Error GoTo ErrHandler
		sTcId = ChId
		lTcLength = bound
		lTcKind = mCB.tk_string
		Exit Sub
ErrHandler: 
		Call mVBOrb.ErrReraise("init2stringTc")
	End Sub
	
	Friend Sub init2WstringTC(ByVal ChId As String, ByVal bound As Integer)
		On Error GoTo ErrHandler
		sTcId = ChId
		lTcLength = bound
		lTcKind = mCB.tk_wstring
		Exit Sub
ErrHandler: 
		Call mVBOrb.ErrReraise("init2WstringTC")
	End Sub
	
	Friend Sub init2FixedTc(ByVal ChId As String, ByVal digits As Short, ByVal scale_ As Short)
		On Error GoTo ErrHandler
		sTcId = ChId
		iDigits = digits
		iScale = scale_
		lTcKind = mCB.tk_fixed
		Exit Sub
ErrHandler: 
		Call mVBOrb.ErrReraise("init2FixedTc")
	End Sub
	
	Friend Sub init2SequenceTc(ByVal ChId As String, ByVal bound As Integer, ByVal element_type As cOrbTypeCode)
		On Error GoTo ErrHandler
		sTcId = ChId
		lTcLength = bound
		Call checkType(element_type)
		oContentType = element_type
		lTcKind = mCB.tk_sequence
		Exit Sub
ErrHandler: 
		Call mVBOrb.ErrReraise("init2SequenceTc")
	End Sub
	
	Friend Sub init2ArrayTc(ByVal ChId As String, ByVal Length As Integer, ByVal element_type As cOrbTypeCode)
		On Error GoTo ErrHandler
		sTcId = ChId
		lTcLength = Length
		Call checkType(element_type)
		oContentType = element_type
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
	Friend Function isCompatible(ByVal kind As Integer) As Boolean
		If lTcKind = kind Then
			isCompatible = True
		ElseIf lTcKind = -1 Then 
			Select Case kind
				Case mCB.tk_objref, mCB.tk_struct, mCB.tk_union, mCB.tk_enum, mCB.tk_alias, mCB.tk_value, mCB.tk_value_box, mCB.tk_native, mCB.tk_abstract_interface, mCB.tk_local_interface, mCB.tk_except
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
				'UPGRADE_NOTE: Object oContentType may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
				oContentType = Nothing
				Erase oMemberTypes
				Erase oMemberLabels
			Case mCB.tk_enum
				Erase oMemberTypes
			Case mCB.tk_string
			Case mCB.tk_sequence
				'UPGRADE_NOTE: Object oContentType may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
				oContentType = Nothing
			Case mCB.tk_array
				'UPGRADE_NOTE: Object oContentType may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
				oContentType = Nothing
			Case mCB.tk_alias
				'UPGRADE_NOTE: Object oContentType may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
				oContentType = Nothing
			Case mCB.tk_except
				Erase oMemberTypes
			Case mCB.tk_longlong
			Case mCB.tk_ulonglong, mCB.tk_longdouble, mCB.tk_wchar, mCB.tk_wstring
			Case mCB.tk_fixed
			Case mCB.tk_value
				'UPGRADE_NOTE: Object oContentType may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
				oContentType = Nothing
				Erase oMemberTypes
			Case mCB.tk_value_box
				'UPGRADE_NOTE: Object oContentType may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
				oContentType = Nothing
			Case mCB.tk_native
			Case mCB.tk_abstract_interface, mCB.tk_local_interface
			Case mCB.tk_component, mCB.tk_home
			Case mCB.tk_event
				'UPGRADE_NOTE: Object oContentType may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
				oContentType = Nothing
				Erase oMemberTypes
			Case Else
				Call mVBOrb.VBOrb.raiseINTERNAL(0, mVBOrb.VBOrb.CompletedNO, "lTCKind = " & CStr(lTcKind))
		End Select
		lTcKind = -2
		Exit Sub
ErrHandler: 
		Call mVBOrb.ErrReraise("destroy")
	End Sub
	
	Public Sub setRecTc2StructTc(ByVal name As String, ByVal members As cCBStructMemberSeq)
		On Error GoTo ErrHandler
		lTcLength = members.Length
		If lTcLength > 0 Then
			ReDim sMemberNames(lTcLength - 1)
			ReDim oMemberTypes(lTcLength - 1)
		End If
		Dim oMem As cCBStructMember
		Dim li As Integer
		For li = 0 To lTcLength - 1
			oMem = members.Item(li)
			Call checkName((oMem.name))
			sMemberNames(li) = oMem.name
			Call checkType(oMem.p_type)
			oMemberTypes(li) = oMem.p_type
		Next li
		Call checkName(name)
		sTcName = name
		lTcKind = mCB.tk_struct
		Exit Sub
ErrHandler: 
		Call mVBOrb.ErrReraise("setRecTc2StructTc")
	End Sub
	
	Public Sub setRecTc2UnionTc(ByVal name As String, ByVal discriminator_type As cOrbTypeCode, ByVal members As cCBUnionMemberSeq)
		On Error GoTo ErrHandler
		Dim lKind As Integer
		lKind = discriminator_type.getOrigType().kind
		If lKind <> mCB.tk_short And lKind <> mCB.tk_long And lKind <> mCB.tk_ushort And lKind <> mCB.tk_ulong And lKind <> mCB.tk_boolean And lKind <> mCB.tk_char And lKind <> mCB.tk_enum And lKind <> mCB.tk_longlong And lKind <> mCB.tk_ulonglong And lKind <> mCB.tk_wchar Then
			Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO, "Invalid TypeCode " & CStr(lKind))
		End If
		oContentType = discriminator_type
		lTcLength = members.Length
		If lTcLength > 0 Then
			ReDim sMemberNames(lTcLength - 1)
			ReDim oMemberTypes(lTcLength - 1)
			ReDim oMemberLabels(lTcLength - 1)
		End If
		Dim oMem As cCBUnionMember
		Dim li As Integer
		For li = 0 To lTcLength - 1
			oMem = members.Item(li)
			Call checkName((oMem.name))
			sMemberNames(li) = oMem.name
			Call checkType(oMem.p_type)
			oMemberTypes(li) = oMem.p_type
			oMemberLabels(li) = oMem.label
		Next li
		lDefaultIndex = -1
		For li = 0 To lTcLength
			If oMemberLabels(li).getType_Renamed().kind() = mCB.tk_octet Then
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
	
	Public Sub setRecTc2EnumTc(ByVal name As String, ByVal members As c_StringSeq)
		On Error GoTo ErrHandler
		lTcLength = members.Length
		If lTcLength > 0 Then
			ReDim sMemberNames(lTcLength - 1)
		End If
		Dim sMemName As String
		Dim li As Integer
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
	
	Public Sub setRecTc2AliasTc(ByVal name As String, ByVal original_type As cOrbTypeCode)
		On Error GoTo ErrHandler
		Call checkName(name)
		sTcName = name
		Call checkType(original_type)
		oContentType = original_type
		lTcKind = mCB.tk_alias
		Exit Sub
ErrHandler: 
		Call mVBOrb.ErrReraise("setRecTc2AliasTc")
	End Sub
	
	Public Sub setRecTc2ExceptionTc(ByVal name As String, ByVal members As cCBStructMemberSeq)
		On Error GoTo ErrHandler
		lTcLength = members.Length
		If lTcLength > 0 Then
			ReDim sMemberNames(lTcLength - 1)
			ReDim oMemberTypes(lTcLength - 1)
		End If
		Dim oMem As cCBStructMember
		Dim li As Integer
		For li = 0 To lTcLength - 1
			oMem = members.Item(li)
			Call checkName((oMem.name))
			sMemberNames(li) = oMem.name
			Call checkType(oMem.p_type)
			oMemberTypes(li) = oMem.p_type
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
	
	Public Sub setRecTc2ValueTc(ByVal name As String, ByVal type_modifier As Short, ByVal concrete_base As cOrbTypeCode, ByVal members As cCBValueMemberSeq)
		On Error GoTo ErrHandler
		Call checkName(name)
		sTcName = name
		iTypeModifier = type_modifier
		Dim lKind As Integer
		If Not concrete_base Is Nothing Then
			lKind = concrete_base.getOrigType().kind
			If lKind <> mCB.tk_value Then
				Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO, "Invalid TypeCode " & CStr(lKind))
			End If
		End If
		oContentType = concrete_base
		lTcLength = members.Length
		If lTcLength > 0 Then
			ReDim sMemberNames(lTcLength - 1)
			ReDim oMemberTypes(lTcLength - 1)
			ReDim iMemberVisibilities(lTcLength - 1)
		End If
		Dim oMem As cCBValueMember
		Dim li As Integer
		For li = 0 To lTcLength - 1
			oMem = members.Item(li)
			Call checkName((oMem.name))
			sMemberNames(li) = oMem.name
			Call checkType(oMem.p_type)
			oMemberTypes(li) = oMem.p_type
			iMemberVisibilities(li) = oMem.access
		Next li
		lTcKind = mCB.tk_value
		Exit Sub
ErrHandler: 
		Call mVBOrb.ErrReraise("setRecTc2ValueTc")
	End Sub
	
	Public Sub setRecTc2ValueBoxTc(ByVal name As String, ByVal boxed_type As cOrbTypeCode)
		On Error GoTo ErrHandler
		Call checkName(name)
		sTcName = name
		Call checkType(boxed_type)
		oContentType = boxed_type
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
	Friend Sub initByRead(ByVal kind As Integer, ByVal oIn As cOrbStream)
		On Error GoTo ErrHandler
		Dim lCnt As Integer
		sTcId = oIn.readString()
		sTcName = oIn.readString()
		Select Case kind
			Case mCB.tk_struct
				lTcLength = oIn.readUlong()
				ReDim sMemberNames(lTcLength - 1)
				ReDim oMemberTypes(lTcLength - 1)
				For lCnt = 0 To lTcLength - 1
					sMemberNames(lCnt) = oIn.readString()
					oMemberTypes(lCnt) = oIn.readTypeCode()
				Next lCnt
			Case mCB.tk_union
				oContentType = oIn.readTypeCode()
				lDefaultIndex = oIn.readLong()
				lTcLength = oIn.readUlong()
				ReDim oMemberLabels(lTcLength - 1)
				ReDim sMemberNames(lTcLength - 1)
				ReDim oMemberTypes(lTcLength - 1)
				For lCnt = 0 To lTcLength - 1
					oMemberLabels(lCnt) = New cOrbAny
					Call oMemberLabels(lCnt).initByReadValue(oContentType, oIn)
					sMemberNames(lCnt) = oIn.readString()
					oMemberTypes(lCnt) = oIn.readTypeCode()
				Next lCnt
			Case mCB.tk_enum
				lTcLength = oIn.readUlong()
				ReDim sMemberNames(lTcLength - 1)
				For lCnt = 0 To lTcLength - 1
					sMemberNames(lCnt) = oIn.readString()
				Next lCnt
			Case mCB.tk_except
				lTcLength = oIn.readUlong()
				ReDim sMemberNames(lTcLength - 1)
				ReDim oMemberTypes(lTcLength - 1)
				For lCnt = 0 To lTcLength - 1
					sMemberNames(lCnt) = oIn.readString()
					oMemberTypes(lCnt) = oIn.readTypeCode()
				Next lCnt
			Case mCB.tk_value, mCB.tk_event
				iTypeModifier = oIn.readShort()
				oContentType = oIn.readTypeCode()
				lTcLength = oIn.readUlong()
				ReDim sMemberNames(lTcLength - 1)
				ReDim oMemberTypes(lTcLength - 1)
				ReDim iMemberVisibilities(lTcLength - 1)
				For lCnt = 0 To lTcLength - 1
					sMemberNames(lCnt) = oIn.readString()
					oMemberTypes(lCnt) = oIn.readTypeCode()
					iMemberVisibilities(lCnt) = oIn.readShort()
				Next lCnt
			Case Else
				Call mVBOrb.VBOrb.raiseINTERNAL(0, mVBOrb.VBOrb.CompletedNO, "lTCKind = " & CStr(kind))
		End Select
		lTcKind = kind
		Exit Sub
ErrHandler: 
		Call mVBOrb.ErrReraise("initByRead")
	End Sub
	
	'Has a complex parameter list
	Friend Function hasComplexPars() As Boolean
		Select Case kind
			Case mCB.tk_objref, mCB.tk_struct, mCB.tk_union, mCB.tk_enum, mCB.tk_sequence, mCB.tk_array, mCB.tk_alias, mCB.tk_except, mCB.tk_value, mCB.tk_value_box, mCB.tk_native, mCB.tk_abstract_interface, mCB.tk_local_interface, mCB.tk_component, mCB.tk_home, mCB.tk_event
				hasComplexPars = True
		End Select
	End Function
	
	Friend Sub writeMe(ByVal oOut As cOrbStream)
		On Error GoTo ErrHandler
		Dim lCnt As Integer
		'Call oOut.writeLong(lTcKind) is done by writeTypeCode()
		Select Case lTcKind
			Case mCB.tk_null, mCB.tk_void, mCB.tk_short, mCB.tk_long, mCB.tk_ushort, mCB.tk_ulong, mCB.tk_float, mCB.tk_double, mCB.tk_boolean, mCB.tk_char, mCB.tk_octet, mCB.tk_any, mCB.tk_TypeCode, mCB.tk_Principal
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
				Call mVBOrb.VBOrb.raiseINTERNAL(0, mVBOrb.VBOrb.CompletedNO, "lTCKind = " & CStr(lTcKind))
		End Select
		Exit Sub
ErrHandler: 
		Call mVBOrb.ErrReraise("writeMe")
	End Sub
	
	'Get the original TypeCode (without alias)
	'For all TypeCode kinds
	Public Function getOrigType(Optional ByVal LoopCnt As Integer = 0) As cOrbTypeCode
		On Error GoTo ErrHandler
		LoopCnt = LoopCnt + 1
		If LoopCnt > 20 Then
			Call mVBOrb.VBOrb.raiseIMPLIMIT(0, mVBOrb.VBOrb.CompletedNO, "tk_alias loop = " & CStr(LoopCnt))
		End If
		If lTcKind = mCB.tk_alias Then
			getOrigType = oContentType.getOrigType(LoopCnt)
		Else
			getOrigType = Me
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
		ElseIf tc.equals(Me) Then 
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
		'UPGRADE_NOTE: Object getCompactTypecode may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
		getCompactTypecode = Nothing
		Call mVBOrb.ErrReraise("getCompactTypecode")
	End Function
	
	'For all TypeCode kinds
	'kind()
	Public Function kind() As Integer
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
		Dim oEx As _cOrbException
		Select Case lTcKind
			Case -1, mCB.tk_objref, mCB.tk_struct, mCB.tk_union, mCB.tk_enum, mCB.tk_alias, mCB.tk_value, mCB.tk_value_box, mCB.tk_native, mCB.tk_abstract_interface, mCB.tk_local_interface, mCB.tk_except, mCB.tk_component, mCB.tk_home, mCB.tk_event
				id = sTcId
			Case Else
				oEx = New cOrbTypeCodeBadKind
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
		Dim oEx As _cOrbException
		Select Case lTcKind
			Case mCB.tk_objref, mCB.tk_struct, mCB.tk_union, mCB.tk_enum, mCB.tk_alias, mCB.tk_value, mCB.tk_value_box, mCB.tk_native, mCB.tk_abstract_interface, mCB.tk_local_interface, mCB.tk_except, mCB.tk_component, mCB.tk_home, mCB.tk_event
			Case Else
				oEx = New cOrbTypeCodeBadKind
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
	Public Function memberCount() As Integer
		On Error GoTo ErrHandler
		Dim oEx As _cOrbException
		Select Case lTcKind
			Case mCB.tk_struct, mCB.tk_union, mCB.tk_enum, mCB.tk_value, mCB.tk_except, mCB.tk_event
			Case Else
				oEx = New cOrbTypeCodeBadKind
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
	Public Function memberName(ByVal index As Integer) As String
		On Error GoTo ErrHandler
		Dim oEx As _cOrbException
		If index < 0 Or index >= lTcLength Then
			oEx = New cOrbTypeCodeBounds
			Call oEx.addInfos(PostDescr:=CStr(index) & " >= " & CStr(lTcLength))
			Call mVBOrb.raiseUserEx(oEx)
		End If
		Select Case lTcKind
			Case mCB.tk_struct, mCB.tk_union, mCB.tk_enum, mCB.tk_value, mCB.tk_except, mCB.tk_event
				memberName = sMemberNames(index)
			Case Else
				oEx = New cOrbTypeCodeBadKind
				Call oEx.addInfos(PostDescr:="lTCKind = " & CStr(lTcKind))
				Call mVBOrb.raiseUserEx(oEx)
		End Select
		Exit Function
ErrHandler: 
		Call mVBOrb.ErrReraise("memberName")
	End Function
	
	'For tk_struct, tk_union, tk_value and tk_except
	'member_type() raises(cOrbTypeCodeBadKind, cOrbTypeCodeBounds)
	Public Function memberType(ByVal index As Integer) As cOrbTypeCode
		On Error GoTo ErrHandler
		Dim oEx As _cOrbException
		If index < 0 Or index >= lTcLength Then
			oEx = New cOrbTypeCodeBounds
			Call oEx.addInfos(PostDescr:=CStr(index) & " >= " & CStr(lTcLength))
			Call mVBOrb.raiseUserEx(oEx)
		End If
		Select Case lTcKind
			Case mCB.tk_struct, mCB.tk_union, mCB.tk_value, mCB.tk_except
				memberType = oMemberTypes(index)
			Case Else
				oEx = New cOrbTypeCodeBadKind
				Call oEx.addInfos(PostDescr:="lTCKind = " & CStr(lTcKind))
				Call mVBOrb.raiseUserEx(oEx)
		End Select
		Exit Function
ErrHandler: 
		Call mVBOrb.ErrReraise("memberType")
	End Function
	
	'For tk_union
	'member_label() raises(cOrbTypeCodeBadKind, cOrbTypeCodeBounds)
	Public Function memberLabel(ByVal index As Integer) As cOrbAny
		On Error GoTo ErrHandler
		Dim oEx As _cOrbException
		If index < 0 Or index >= lTcLength Then
			oEx = New cOrbTypeCodeBounds
			Call oEx.addInfos(PostDescr:=CStr(index) & " >= " & CStr(lTcLength))
			Call mVBOrb.raiseUserEx(oEx)
		End If
		Select Case lTcKind
			Case mCB.tk_union
				memberLabel = oMemberLabels(index)
			Case Else
				oEx = New cOrbTypeCodeBadKind
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
		Dim oEx As _cOrbException
		If lTcKind <> mCB.tk_union Then
			oEx = New cOrbTypeCodeBadKind
			Call oEx.addInfos(PostDescr:="lTCKind = " & CStr(lTcKind))
			Call mVBOrb.raiseUserEx(oEx)
		End If
		discriminatorType = oContentType
		Exit Function
ErrHandler: 
		'UPGRADE_NOTE: Object discriminatorType may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
		discriminatorType = Nothing
		Call mVBOrb.ErrReraise("discriminatorType")
	End Function
	
	'For tk_union
	'default_index() raises(cOrbTypeCodeBadKind)
	Public Function defaultIndex() As Integer
		On Error GoTo ErrHandler
		Dim oEx As _cOrbException
		If lTcKind <> mCB.tk_union Then
			oEx = New cOrbTypeCodeBadKind
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
	Public Function Length() As Integer
		On Error GoTo ErrHandler
		Dim oEx As _cOrbException
		Select Case lTcKind
			Case mCB.tk_string, mCB.tk_wstring, mCB.tk_sequence, mCB.tk_array
			Case Else
				oEx = New cOrbTypeCodeBadKind
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
		Dim oEx As _cOrbException
		Select Case lTcKind
			Case mCB.tk_sequence, mCB.tk_array, mCB.tk_alias, mCB.tk_value_box
			Case Else
				oEx = New cOrbTypeCodeBadKind
				Call oEx.addInfos(PostDescr:="lTCKind = " & CStr(lTcKind))
				Call mVBOrb.raiseUserEx(oEx)
		End Select
		contentType = oContentType
		Exit Function
ErrHandler: 
		Call mVBOrb.ErrReraise("contentType")
	End Function
	
	'For tk_fixed
	'fixed_digits() raises(cOrbTypeCodeBadKind)
	Public Function fixedDigits() As Short
		On Error GoTo ErrHandler
		Dim oEx As _cOrbException
		If lTcKind <> mCB.tk_fixed Then
			oEx = New cOrbTypeCodeBadKind
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
	Public Function fixedScale() As Short
		On Error GoTo ErrHandler
		Dim oEx As _cOrbException
		If lTcKind <> mCB.tk_fixed Then
			oEx = New cOrbTypeCodeBadKind
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
	Public Function memberVisibility(ByVal index As Integer) As Short
		On Error GoTo ErrHandler
		Dim oEx As _cOrbException
		If index < 0 Or index >= lTcLength Then
			oEx = New cOrbTypeCodeBounds
			Call oEx.addInfos(PostDescr:=CStr(index) & " >= " & CStr(lTcLength))
			Call mVBOrb.raiseUserEx(oEx)
		End If
		Select Case lTcKind
			Case mCB.tk_value, mCB.tk_event
				memberVisibility = iMemberVisibilities(index)
			Case Else
				oEx = New cOrbTypeCodeBadKind
				Call oEx.addInfos(PostDescr:="lTCKind = " & CStr(lTcKind))
				Call mVBOrb.raiseUserEx(oEx)
		End Select
		Exit Function
ErrHandler: 
		Call mVBOrb.ErrReraise("memberVisibility")
	End Function
	
	'For tk_value, tk_event
	'type_modifier() raises(cOrbTypeCodeBadKind)
	Public Function typeModifier() As Short
		On Error GoTo ErrHandler
		Dim oEx As _cOrbException
		Select Case lTcKind
			Case mCB.tk_value, mCB.tk_event
				typeModifier = iTypeModifier
			Case Else
				oEx = New cOrbTypeCodeBadKind
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
		Dim oEx As _cOrbException
		Select Case lTcKind
			Case mCB.tk_value, mCB.tk_event
				concreteBaseType = oContentType
			Case Else
				oEx = New cOrbTypeCodeBadKind
				Call oEx.addInfos(PostDescr:="lTCKind = " & CStr(lTcKind))
				Call mVBOrb.raiseUserEx(oEx)
		End Select
		Exit Function
ErrHandler: 
		'UPGRADE_NOTE: Object concreteBaseType may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
		concreteBaseType = Nothing
		Call mVBOrb.ErrReraise("concreteBaseType")
	End Function
End Class