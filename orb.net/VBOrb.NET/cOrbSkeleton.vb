Option Strict Off
Option Explicit On
<System.Runtime.InteropServices.ProgId("cOrbSkeleton_NET.cOrbSkeleton")> Public Class cOrbSkeleton
	'Copyright (c) 1999 Martin.Both
	
	'This library is free software; you can redistribute it and/or
	'modify it under the terms of the GNU Library General Public
	'License as published by the Free Software Foundation; either
	'version 2 of the License, or (at your option) any later version.
	
	'This library is distributed in the hope that it will be useful,
	'but WITHOUT ANY WARRANTY; without even the implied warranty of
	'MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
	'Library General Public License for more details.
	
	
	'Note: Public members with '_' are not allowed here because of
	'problems using 'implements cOrbSkeleton'
	
	'UPGRADE_NOTE: Class_Initialize was upgraded to Class_Initialize_Renamed. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="A9E4979A-37FA-4718-9994-97DD76ED70A7"'
	Private Sub Class_Initialize_Renamed()
		Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO, "cOrbSkeleton is just an interface")
	End Sub
	Public Sub New()
		MyBase.New()
		Class_Initialize_Renamed()
	End Sub
	
	'Used by oOrb.connect() and is_a()
	Public ReadOnly Property TypeId(ByVal Item As Short) As String
		Get
		End Get
	End Property
	
	'Used by oOrb.disconnect()
	
	'Used by oOrb.connect()
	Public Property ObjRef() As cOrbObjRef
		Get
		End Get
		Set(ByVal Value As cOrbObjRef)
		End Set
	End Property
	
	'Used by oOrb to execute an object operation
	Public Function execute(ByVal sOperation As String, ByVal oIn As cOrbStream, ByVal oOut As cOrbStream) As Integer
	End Function
End Class