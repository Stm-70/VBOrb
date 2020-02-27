VERSION 1.0 CLASS
BEGIN
  MultiUse = -1  'True
END
Attribute VB_Name = "cOrblongdouble"
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

'WARNING: Long double type is not tested
Private buffer(0 To 15) As Byte

'RtlMoveMemory();
Private Declare Sub dllMoveMem Lib "kernel32" Alias "RtlMoveMemory" _
    (ByRef hpvDest As Any, ByRef hpvSource As Any, ByVal cbCopy As Long)

'Helper for readLongdouble()
Public Sub getBytes(ByRef bytes() As Byte)
    Call dllMoveMem(bytes(LBound(bytes)), buffer(0&), 16&)
End Sub

'Helper for writeLongdouble()
Public Sub setBytes(ByRef bytes() As Byte)
    Call dllMoveMem(buffer(0&), bytes(LBound(bytes)), 16&)
End Sub
