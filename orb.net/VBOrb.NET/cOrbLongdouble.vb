Option Strict Off
Option Explicit On
<System.Runtime.InteropServices.ProgId("cOrblongdouble_NET.cOrblongdouble")> Public Class cOrblongdouble
	'Copyright (c) 2002 Martin.Both
	
	'This library is free software; you can redistribute it and/or
	'modify it under the terms of the GNU Library General Public
	'License as published by the Free Software Foundation; either
	'version 2 of the License, or (at your option) any later version.
	
	'This library is distributed in the hope that it will be useful,
	'but WITHOUT ANY WARRANTY; without even the implied warranty of
	'MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
	'Library General Public License for more details.
	
	
	'WARNING: Long double type is not tested
	Private buffer(15) As Byte

	'RtlMoveMemory();
	'UPGRADE_ISSUE: Declaring a parameter 'As Any' is not supported. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="FAE78A8D-8978-4FD4-8208-5B7324A8F795"'
	'UPGRADE_ISSUE: Declaring a parameter 'As Any' is not supported. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="FAE78A8D-8978-4FD4-8208-5B7324A8F795"'
	' Private Declare Sub dllMoveMem Lib "kernel32"  Alias "RtlMoveMemory"(ByRef hpvDest As Any, ByRef hpvSource As Any, ByVal cbCopy As Integer)
	Declare Sub dllMoveMemory Lib "Kernel32.dll" _
	Alias "RtlMoveMemory" (ByVal dest As IntPtr, ByVal src As IntPtr, ByVal size As Integer)

	'Helper for readLongdouble()
	Public Sub getBytes(ByRef bytes() As Byte)
		Call dllMoveMemory(bytes(LBound(bytes)), buffer(0), 16)
	End Sub
	
	'Helper for writeLongdouble()
	Public Sub setBytes(ByRef bytes() As Byte)
		Call dllMoveMemory(buffer(0), bytes(LBound(bytes)), 16)
	End Sub
End Class