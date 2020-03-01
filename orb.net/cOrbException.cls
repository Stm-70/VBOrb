VERSION 1.0 CLASS
BEGIN
  MultiUse = -1  'True
END
Attribute VB_Name = "cOrbException"
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
'problems using 'implements cOrbException'

Private sSource As String
Private sDescription As String

Public Property Get TypeId() As String
    Dim lStart As Long
    Dim lEnd As Long
    lStart = InStr(sDescription, "[")
    If lStart > 0 Then lEnd = InStr(lStart, sDescription, "]")
    If lStart = 0 Or lEnd <= lStart Then
        Call mVBOrb.ErrRaise(1, "Empty or invalid description" _
            & ", Descr= " & sDescription _
            & ", Source= " & sSource, "TypeId")
    End If
    TypeId = Mid$(sDescription, lStart + 1, lEnd - 1)
End Property

Public Property Get Source() As String
    Source = sSource
End Property

Public Property Get Description() As String
    Description = sDescription
End Property

Public Sub addInfos(Optional ByRef SourcePrefix As String = "", _
    Optional ByRef PostDescr As String = "")
    If sSource = "" Then
        sSource = SourcePrefix
    ElseIf SourcePrefix <> "" Then
        sSource = SourcePrefix & ":" & sSource
    End If
    If sDescription = "" Then
        sDescription = PostDescr
    ElseIf PostDescr <> "" Then
        sDescription = sDescription & ", " & PostDescr
    End If
End Sub

'Helper
Public Sub initByRead(ByVal oIn As cOrbStream)
'    On Error GoTo ErrHandler
'    Exit Sub
'ErrHandler:
'    Call mvborb.ErrReraise("Exception.read")
End Sub

'Helper
Public Sub writeMe(ByVal oOut As cOrbStream)
    On Error GoTo ErrHandler
    Call oOut.writeString(TypeId)
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("Exception.write")
End Sub

