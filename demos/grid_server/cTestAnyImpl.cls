VERSION 1.0 CLASS
BEGIN
  MultiUse = -1  'True
END
Attribute VB_Name = "cTestAnyImpl"
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

Option Explicit

Implements c_TestAny

'print()
Private Function c_TestAny_printFunc(ByVal what As cOrbAny) As cOrbAny
    On Error GoTo ErrHandler
    Set c_TestAny_printFunc = what
    Exit Function
ErrHandler:
    Set c_TestAny_printFunc = Nothing
    Call VBOrb.ErrReraise(Err, "printFunc")
End Function

