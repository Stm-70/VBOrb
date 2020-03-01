VERSION 1.0 CLASS
BEGIN
  MultiUse = -1  'True
END
Attribute VB_Name = "cOrbValueBase"
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
'problems using 'implements cOrbValueBase'

'All classes implementing this have also implement:
'Implements cOrbAbstractBase

Private Sub Class_Initialize()
    Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO, _
        "cOrbValueBase is just an interface")
End Sub

'Used by oOrbStream.readValue()
Public Function UniqueId() As Long
End Function

'Used by oOrbStream.readValue()
Public Sub initByRead(ByVal oIn As cOrbStream)
End Sub

'Used by oOrbStream.writeValue()
Public Sub writeMe(ByVal oOut As cOrbStream)
End Sub

'Provides truncatable repository ids
'Used by cOrbStream.writeObject()
Public Function getIds(ByVal Item As Integer) As String
End Function

'Used by oOrbStream.writeValue()
Public Function isCustom() As Boolean
End Function

