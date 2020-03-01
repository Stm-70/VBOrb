Option Strict Off
Option Explicit On
Public Interface _cOrbValueBase
	Function UniqueId() As Integer
	Sub initByRead(ByVal oIn As cOrbStream)
	Sub writeMe(ByVal oOut As cOrbStream)
	Function getIds(ByVal Item As Short) As String
	Function isCustom() As Boolean
End Interface
<System.Runtime.InteropServices.ProgId("cOrbValueBase_NET.cOrbValueBase")> Public Class cOrbValueBase
	Implements _cOrbValueBase
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
	'problems using 'implements cOrbValueBase'
	
	'All classes implementing this have also implement:
	'Implements cOrbAbstractBase
	
	'UPGRADE_NOTE: Class_Initialize was upgraded to Class_Initialize_Renamed. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="A9E4979A-37FA-4718-9994-97DD76ED70A7"'
	Private Sub Class_Initialize_Renamed()
		Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO, "cOrbValueBase is just an interface")
	End Sub
	Public Sub New()
		MyBase.New()
		Class_Initialize_Renamed()
	End Sub
	
	'Used by oOrbStream.readValue()
	Public Function UniqueId() As Integer Implements _cOrbValueBase.UniqueId
	End Function
	
	'Used by oOrbStream.readValue()
	Public Sub initByRead(ByVal oIn As cOrbStream) Implements _cOrbValueBase.initByRead
	End Sub
	
	'Used by oOrbStream.writeValue()
	Public Sub writeMe(ByVal oOut As cOrbStream) Implements _cOrbValueBase.writeMe
	End Sub
	
	'Provides truncatable repository ids
	'Used by cOrbStream.writeObject()
	Public Function getIds(ByVal Item As Short) As String Implements _cOrbValueBase.getIds
	End Function
	
	'Used by oOrbStream.writeValue()
	Public Function isCustom() As Boolean Implements _cOrbValueBase.isCustom
	End Function
End Class