Option Strict Off
Option Explicit On
Public Interface _cOrbValueFactory
	Function newUninitValue() As cOrbValueBase
End Interface
<System.Runtime.InteropServices.ProgId("cOrbValueFactory_NET.cOrbValueFactory")> Public Class cOrbValueFactory
	Implements _cOrbValueFactory
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
	'problems using 'implements cOrbValueFactory'
	
	'UPGRADE_NOTE: Class_Initialize was upgraded to Class_Initialize_Renamed. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="A9E4979A-37FA-4718-9994-97DD76ED70A7"'
	Private Sub Class_Initialize_Renamed()
		Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO, "cOrbValueFactory is just an interface")
	End Sub
	Public Sub New()
		MyBase.New()
		Class_Initialize_Renamed()
	End Sub

	'If the ORB needs to unmarshal a value the ORB first needs an
	'uninitialized value from the Factory in cOrbStream.readValue(Nothing)
	'Public Function newUninitValue() As _cOrbValueBase Implements _cOrbValueFactory.newUninitValue
	'End Function

	Private Function _cOrbValueFactory_newUninitValue() As cOrbValueBase Implements _cOrbValueFactory.newUninitValue
		Throw New NotImplementedException()
	End Function
End Class