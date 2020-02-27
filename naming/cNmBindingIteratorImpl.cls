VERSION 1.0 CLASS
BEGIN
  MultiUse = -1  'True
END
Attribute VB_Name = "cNmBindingIteratorImpl"
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

Dim oOrb As cOrbImpl
Dim oNmBindingIteratorImplTie As c_NmBindingIteratorImplTie

Dim oColBindRecs As Collection

Dim formNC As fNameContext

Implements c_NmBindingIterator

Public Function initMe(ByVal Orb As cOrbImpl, ByVal Start As Long, _
    ByVal ColBindRecs As Collection, ByVal fNC As fNameContext) _
    As c_NmBindingIterator
    Set formNC = fNC
    Call formNC.printLine("new BindingIterator(" & CStr(Start) & ")")
    Set oOrb = Orb
    Set oColBindRecs = New Collection
    Do While Start <= ColBindRecs.Count
        Call oColBindRecs.Add(ColBindRecs(Start))
        Start = Start + 1
    Loop
    'Create an object and connect the object to the ORB.
    Set oNmBindingIteratorImplTie = New c_NmBindingIteratorImplTie
    Call oNmBindingIteratorImplTie.setDelegate(Me)
    fNameServ.iRandom = fNameServ.iRandom + 1
    Call oOrb.Connect(oNmBindingIteratorImplTie, CStr(fNameServ.iRandom) & _
        "ALongNameToAvoidABugInJDKORB" & CStr(fNameServ.iRandom))
    Set initMe = oNmBindingIteratorImplTie.This
End Function

Public Sub termMe()
    If oOrb Is Nothing Then
        Exit Sub
    End If
    Call oOrb.disconnect(oNmBindingIteratorImplTie)
    Set oNmBindingIteratorImplTie = Nothing
    Set oOrb = Nothing
    Set formNC = Nothing
End Sub

Private Sub c_NmBindingIterator_destroy()
    Call formNC.printLine("BindingIterator.destroy()")
    Call termMe
End Sub

Private Function c_NmBindingIterator_nextOne(ByRef b As c_NmBinding) As Boolean
    On Error GoTo ErrHandler
    If oColBindRecs.Count > 0 Then
        Set b = getNextNmBinding()
        c_NmBindingIterator_nextOne = True
    Else
        Set b = New c_NmBinding
        Set b.binding_name = New c_NmNameComponentSeq
        c_NmBindingIterator_nextOne = False
    End If
    Exit Function
ErrHandler:
    Call VBOrb.ErrReraise(Err, "nextOne")
End Function

Private Function getNextNmBinding() As c_NmBinding
    Dim oBindRec As cBindRecord
    Set oBindRec = oColBindRecs(1)
    Call oColBindRecs.Remove(1)
    Dim oB As c_NmBinding
    Set oB = New c_NmBinding
    With oB
        .binding_type = oBindRec.lType
        Set .binding_name = New c_NmNameComponentSeq
        .binding_name.Length = 1
        Set .binding_name.Item(0) = New c_NmNameComponent
        .binding_name.Item(0).id = oBindRec.oNameComponent.id
        .binding_name.Item(0).kind = oBindRec.oNameComponent.kind
    End With
    Set getNextNmBinding = oB
End Function

Private Function c_NmBindingIterator_nextN(ByVal how_many As Long, _
    ByRef bl As c_NmBindingSeq) As Boolean
    On Error GoTo ErrHandler
    If how_many < 0 Then  'check for bad (unsigned long) how_many
        Call VBOrb.raiseBADPARAM(0, VBOrb.CompletedNO, _
            "how_many = " & CStr(how_many) & " <= 0")
    End If
    
    Dim lActual As Long
    lActual = oColBindRecs.Count
    
    Set bl = New c_NmBindingSeq
    If how_many < lActual Then
        bl.Length = how_many
    Else
        bl.Length = lActual
    End If
    
    Dim lIndex As Long
    For lIndex = 0 To bl.Length - 1
        Set bl.Item(lIndex) = getNextNmBinding()
    Next lIndex
    'true if at least one binding exists
    c_NmBindingIterator_nextN = lActual > 0
    Exit Function
ErrHandler:
    Call VBOrb.ErrReraise(Err, "nextN")
End Function

