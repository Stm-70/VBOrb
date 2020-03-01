VERSION 1.0 CLASS
BEGIN
  MultiUse = -1  'True
END
Attribute VB_Name = "cOrbSkeleton"
Attribute VB_GlobalNameSpace = False
Attribute VB_Creatable = True
Attribute VB_PredeclaredId = False
Attribute VB_Exposed = True
'Copyright (c) 1999 Martin.Both

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
'problems using 'implements cOrbSkeleton'

Private Sub Class_Initialize()
    Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO, _
        "cOrbSkeleton is just an interface")
End Sub

'Used by oOrb.connect() and is_a()
Public Property Get TypeId(ByVal Item As Integer) As String
End Property

'Used by oOrb.disconnect()
Public Property Get ObjRef() As cOrbObjRef
End Property

'Used by oOrb.connect()
Public Property Set ObjRef(ByVal newObjRef As cOrbObjRef)
End Property

'Used by oOrb to execute an object operation
Public Function execute(ByVal sOperation As String, _
    ByVal oIn As cOrbStream, ByVal oOut As cOrbStream) As Long
End Function
