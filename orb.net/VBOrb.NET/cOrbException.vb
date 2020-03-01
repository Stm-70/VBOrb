Option Strict Off
Option Explicit On
Public Interface _cOrbException
	ReadOnly Property TypeId As String
	ReadOnly Property Source As String
	ReadOnly Property Description As String
	Sub addInfos(Optional ByRef SourcePrefix As String = "", Optional ByRef PostDescr As String = "")
	Sub initByRead(ByVal oIn As cOrbStream)
	Sub writeMe(ByVal oOut As cOrbStream)
End Interface
<System.Runtime.InteropServices.ProgId("cOrbException_NET.cOrbException")> Public Class cOrbException
	Implements _cOrbException
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
	'problems using 'implements cOrbException'
	
	Private sSource As String
	Private sDescription As String
	
	Public ReadOnly Property TypeId() As String Implements _cOrbException.TypeId
		Get
			Dim lStart As Integer
			Dim lEnd As Integer
			lStart = InStr(sDescription, "[")
			If lStart > 0 Then lEnd = InStr(lStart, sDescription, "]")
			If lStart = 0 Or lEnd <= lStart Then
				Call mVBOrb.ErrRaise(1, "Empty or invalid description" & ", Descr= " & sDescription & ", Source= " & sSource, "TypeId")
			End If
			TypeId = Mid(sDescription, lStart + 1, lEnd - 1)
		End Get
	End Property
	
	Public ReadOnly Property Source() As String Implements _cOrbException.Source
		Get
			Source = sSource
		End Get
	End Property
	
	Public ReadOnly Property Description() As String Implements _cOrbException.Description
		Get
			Description = sDescription
		End Get
	End Property
	
	Public Sub addInfos(Optional ByRef SourcePrefix As String = "", Optional ByRef PostDescr As String = "") Implements _cOrbException.addInfos
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
	Public Sub initByRead(ByVal oIn As cOrbStream) Implements _cOrbException.initByRead
		'    On Error GoTo ErrHandler
		'    Exit Sub
		'ErrHandler:
		'    Call mvborb.ErrReraise("Exception.read")
	End Sub
	
	'Helper
	Public Sub writeMe(ByVal oOut As cOrbStream) Implements _cOrbException.writeMe
		On Error GoTo ErrHandler
		Call oOut.writeString(TypeId)
		Exit Sub
ErrHandler: 
		Call mVBOrb.ErrReraise("Exception.write")
	End Sub
End Class