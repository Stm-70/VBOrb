Option Strict Off
Option Explicit On
Public Interface _cOrbObject
	Function setObjRef(ByVal ObjRef As cOrbObjRef, ByVal Check As Boolean) As Boolean
	Function getObjRef() As cOrbObjRef
	Function getId() As String
End Interface
<System.Runtime.InteropServices.ProgId("cOrbObject_NET.cOrbObject")> Public Class cOrbObject
	Implements _cOrbObject
	'Copyright (c) 2001 Martin.Both
	
	'This library is free software; you can redistribute it and/or
	'modify it under the terms of the GNU Library General Public
	'License as published by the Free Software Foundation; either
	'version 2 of the License, or (at your option) any later version.
	
	'This library is distributed in the hope that it will be useful,
	'but WITHOUT ANY WARRANTY; without even the implied warranty of
	'MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
	'Library General Public License for more details.
	
	
	'Note: Public members with '_' are not allowed here because of
	'problems using 'implements cOrbObject'
	
	'All classes implementing this have also implement:
	'Implements cOrbAbstractBase
	
	'UPGRADE_NOTE: Class_Initialize was upgraded to Class_Initialize_Renamed. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="A9E4979A-37FA-4718-9994-97DD76ED70A7"'
	Private Sub Class_Initialize_Renamed()
		Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO, "cOrbObject is just an interface")
	End Sub
	Public Sub New()
		MyBase.New()
		Class_Initialize_Renamed()
	End Sub
	
	'Used by Helper.narrow(), Helper.uncheckedNarrow(), cOrbStream.readObject()
	'IN:    ObjRef          New ObjRef or Nothing
	'IN:    Check           Check the type?
	'RET:                   ObjRef is Nothing?
	Public Function setObjRef(ByVal ObjRef As cOrbObjRef, ByVal Check As Boolean) As Boolean Implements _cOrbObject.setObjRef
	End Function
	
	'Used by Helper.narrow(), Helper.uncheckedNarrow(), cOrbStream.writeObject()
	Public Function getObjRef() As cOrbObjRef Implements _cOrbObject.getObjRef
	End Function
	
	'Used by cOrbStream.writeObject()
	Public Function getId() As String Implements _cOrbObject.getId
	End Function
End Class