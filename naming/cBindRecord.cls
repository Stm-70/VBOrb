VERSION 1.0 CLASS
BEGIN
  MultiUse = -1  'True
END
Attribute VB_Name = "cBindRecord"
Attribute VB_GlobalNameSpace = False
Attribute VB_Creatable = True
Attribute VB_PredeclaredId = False
Attribute VB_Exposed = False
'Copyright (c) 2002 Martin.Both

'This library is free software; you can redistribute it and/or
'modify it under the terms of the GNU Library General Public
'License as published by the Free Software Foundation; either
'version 2 of the License, or (at your option) any later version.

'This library is distributed in the hope that it will be useful,
'but WITHOUT ANY WARRANTY; without even the implied warranty of
'MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
'Library General Public License for more details.

'First version was written by Craig Neuwirt

Option Explicit

Public sName As String
Public oNameComponent As c_NmNameComponent
Public lType As Long
Public oObject As cOrbObject

Public Function getBinding() As c_NmBinding
    Dim oBinding As c_NmBinding
    Set oBinding = New c_NmBinding
    oBinding.binding_name = New c_NmNameComponentSeq
    oBinding.binding_name.Length = 1
    Set oBinding.binding_name.Item(0) = oNameComponent
    oBinding.binding_type = lType
End Function
