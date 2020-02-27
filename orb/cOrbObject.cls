VERSION 1.0 CLASS
BEGIN
  MultiUse = -1  'True
END
Attribute VB_Name = "cOrbObject"
Attribute VB_GlobalNameSpace = False
Attribute VB_Creatable = True
Attribute VB_PredeclaredId = False
Attribute VB_Exposed = True
'Copyright (c) 2001 Martin.Both

'This library is free software; you can redistribute it and/or
'modify it under the terms of the GNU Library General Public
'License as published by the Free Software Foundation; either
'version 2 of the License, or (at your option) any later version.

'This library is distributed in the hope that it will be useful,
'but WITHOUT ANY WARRANTY; without even the implied warranty of
'MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
'Library General Public License for more details.

Option Explicit

'Note: Public members with '_' are not allowed here because of
'problems using 'implements cOrbObject'

'All classes implementing this have also implement:
'Implements cOrbAbstractBase

Private Sub Class_Initialize()
    Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO, _
        "cOrbObject is just an interface")
End Sub

'Used by Helper.narrow(), Helper.uncheckedNarrow(), cOrbStream.readObject()
'IN:    ObjRef          New ObjRef or Nothing
'IN:    Check           Check the type?
'RET:                   ObjRef is Nothing?
Public Function setObjRef(ByVal ObjRef As cOrbObjRef, ByVal Check As Boolean) _
    As Boolean
End Function

'Used by Helper.narrow(), Helper.uncheckedNarrow(), cOrbStream.writeObject()
Public Function getObjRef() As cOrbObjRef
End Function

'Used by cOrbStream.writeObject()
Public Function getId() As String
End Function

