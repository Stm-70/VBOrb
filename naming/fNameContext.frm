VERSION 5.00
Begin VB.Form fNameContext 
   Caption         =   "NameContext"
   ClientHeight    =   3555
   ClientLeft      =   60
   ClientTop       =   345
   ClientWidth     =   6345
   LinkTopic       =   "Form1"
   ScaleHeight     =   3555
   ScaleWidth      =   6345
   StartUpPosition =   3  'Windows-Standard
   Begin VB.TextBox tbResult 
      Height          =   3495
      Left            =   0
      MultiLine       =   -1  'True
      ScrollBars      =   2  'Vertikal
      TabIndex        =   0
      Top             =   0
      Width           =   6255
   End
End
Attribute VB_Name = "fNameContext"
Attribute VB_GlobalNameSpace = False
Attribute VB_Creatable = False
Attribute VB_PredeclaredId = True
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
Dim bIsRoot As Boolean
Dim oNmCtxtExtRootImplTie As c_NmContextExtImplTie
Dim oNmContextExtImplTie As c_NmContextExtImplTie

Dim oColBindRecs As Collection

Implements c_NmContextExt

Public Sub initMe(ByVal Orb As cOrbImpl, ByVal IsRoot As Boolean)
    Set oOrb = Orb
    bIsRoot = IsRoot
    'Create an object and connect the object to the ORB.
    Set oNmContextExtImplTie = New c_NmContextExtImplTie
    Call oNmContextExtImplTie.setDelegate(Me)
    fNameServ.iRandom = fNameServ.iRandom + 1
    Call oOrb.Connect(oNmContextExtImplTie, CStr(fNameServ.iRandom) & _
        "ALongNameToAvoidABugInJDKORB" & CStr(fNameServ.iRandom))
    If bIsRoot Then
        Me.Caption = fNameServ.Caption & " - Root" & Me.Caption
        Set oNmCtxtExtRootImplTie = New c_NmContextExtImplTie
        Call oNmCtxtExtRootImplTie.setDelegate(Me)
        Call oOrb.Connect(oNmCtxtExtRootImplTie, "NameService")
    Else
        Me.Caption = fNameServ.Caption & " - " & Me.Caption
    End If
    Call printLine(oOrb.objectToString(oNmContextExtImplTie.This))
End Sub

Public Function getAddress() As String
    If bIsRoot Then
        getAddress = oNmCtxtExtRootImplTie.ObjRef.IIOPAddressFw & "/" _
            & oNmCtxtExtRootImplTie.ObjRef.objectKey
    Else
        getAddress = oNmContextExtImplTie.ObjRef.IIOPAddressFw & "/" _
            & oNmContextExtImplTie.ObjRef.objectKey
    End If
End Function

Public Function getThis() As cOrbObject
    Set getThis = oNmContextExtImplTie.This
End Function

Public Sub termMe()
    If oOrb Is Nothing Then
        Exit Sub
    End If
    If bIsRoot Then
        Call oOrb.disconnect(oNmCtxtExtRootImplTie)
        Set oNmCtxtExtRootImplTie = Nothing
    End If
    Call oOrb.disconnect(oNmContextExtImplTie)
    Set oNmContextExtImplTie = Nothing
    Set oOrb = Nothing
    Unload Me
End Sub

Public Sub bindObject(ByVal oNmComponent As c_NmNameComponent, _
    ByVal oObject As cOrbObject, ByVal lType As Long, ByVal bRebind As Boolean)
    On Error GoTo ErrHandler
    Dim sName As String
    sName = nameFld2nameStr(oNmComponent.id)
    If nameFld2nameStr(oNmComponent.kind) <> "" Then
        sName = sName & "." & nameFld2nameStr(oNmComponent.kind)
    End If
    Dim oBindRec As cBindRecord
    Set oBindRec = New cBindRecord
    oBindRec.sName = sName
    Set oBindRec.oNameComponent = oNmComponent
    oBindRec.lType = lType
    Set oBindRec.oObject = oObject
    
    If oColBindRecs Is Nothing Then
        Set oColBindRecs = New Collection
    ElseIf Not resolveObject(oNmComponent) Is Nothing Then
        If Not bRebind Then
            Dim oABEx As c_NmAlreadyBoundEx
            Set oABEx = New c_NmAlreadyBoundEx
            Call VBOrb.raiseUserEx(oABEx)
        End If
        Call oColBindRecs.Remove(sName)
    End If
    Call oColBindRecs.Add(oBindRec, sName)
    Exit Sub
ErrHandler:
    Call VBOrb.ErrReraise(Err, "bindObject")
End Sub

Public Sub unbindObject(ByVal oNmComponent As c_NmNameComponent)
    On Error GoTo ErrHandler
    Dim sName As String
    sName = nameFld2nameStr(oNmComponent.id)
    If nameFld2nameStr(oNmComponent.kind) <> "" Then
        sName = sName & "." & nameFld2nameStr(oNmComponent.kind)
    End If
    If resolveObject(oNmComponent) Is Nothing Then
        Dim oNFEx As c_NmNotFoundEx
        Set oNFEx = New c_NmNotFoundEx
        oNFEx.why = m_NmContext.not_object
        Set oNFEx.rest_of_name = New c_NmNameComponentSeq
        Call VBOrb.raiseUserEx(oNFEx)
    End If
    Call oColBindRecs.Remove(sName)
    Exit Sub
ErrHandler:
    Call VBOrb.ErrReraise(Err, "unbindObject")
End Sub

'RET:   Nothing if Not found
Public Function resolveObject(ByVal oNmComponent As c_NmNameComponent) As cBindRecord
    On Error GoTo ErrHandler
    If oColBindRecs Is Nothing Then
        Exit Function
    End If
    Dim oBindRec As cBindRecord
    Dim sName As String
    sName = nameFld2nameStr(oNmComponent.id)
    If nameFld2nameStr(oNmComponent.kind) <> "" Then
        sName = sName & "." & nameFld2nameStr(oNmComponent.kind)
    End If
    On Error Resume Next
    Set oBindRec = oColBindRecs.Item(sName)
    If Err.Number <> 0 Then Set oBindRec = Nothing
    On Error GoTo 0
    Set resolveObject = oBindRec
    Exit Function
ErrHandler:
    Call VBOrb.ErrReraise(Err, "resolveObject")
End Function

Public Function resolveNextContext(ByVal n As c_NmNameComponentSeq) As c_NmContext
    On Error GoTo ErrHandler
    Dim oNFEx As c_NmNotFoundEx
    If n.Length = 0 Then
        Dim oINEx As c_NmInvalidNameEx
        Set oINEx = New c_NmInvalidNameEx
        Call VBOrb.raiseUserEx(oINEx)
    End If
    
    Dim sName As String
    sName = nameFld2nameStr(n.Item(0).id)
    If nameFld2nameStr(n.Item(0).kind) <> "" Then
        sName = sName & "." & nameFld2nameStr(n.Item(0).kind)
    End If
    Call printLine("resolveNextContext(" & sName & ")")
    
    Dim oFirstNameComponent As c_NmNameComponent
    Set oFirstNameComponent = n.Item(0)
    Dim iNc As Integer
    For iNc = 1 To n.Length - 1
        Set n.Item(iNc - 1) = n.Item(iNc)
    Next iNc
    n.Length = n.Length - 1
    
    Dim oBindRec As cBindRecord
    Set oBindRec = resolveObject(oFirstNameComponent)
    If oBindRec Is Nothing Then
        Set oNFEx = New c_NmNotFoundEx
        oNFEx.why = m_NmContext.missing_node
        Set oNFEx.rest_of_name = n
        Call VBOrb.raiseUserEx(oNFEx)
    End If
    If oBindRec.oObject Is Nothing Then
        Set oNFEx = New c_NmNotFoundEx
        oNFEx.why = m_NmContext.not_context
        Set oNFEx.rest_of_name = n
        Call VBOrb.raiseUserEx(oNFEx)
    End If
    Dim oNmContext As c_NmContext
    On Error Resume Next
    Set oNmContext = m_NmContext.narrow(oBindRec.oObject)
    If Err.Number <> 0 Then
        Set oNmContext = Nothing
    End If
    On Error GoTo ErrHandler
    If oNmContext Is Nothing Then
        Set oNFEx = New c_NmNotFoundEx
        oNFEx.why = m_NmContext.not_context
        Set oNFEx.rest_of_name = n
        Call VBOrb.raiseUserEx(oNFEx)
    End If
    Set resolveNextContext = oNmContext
    Exit Function
ErrHandler:
    Call VBOrb.ErrReraise(Err, "resolveNextContext")
End Function

Private Function c_NmContextExt_resolve(ByVal n As c_NmNameComponentSeq) As VBOrb.cOrbObject
    If n.Length = 0 Then
        Dim oINEx As c_NmInvalidNameEx
        Set oINEx = New c_NmInvalidNameEx
        Call VBOrb.raiseUserEx(oINEx)
    End If
    If n.Length > 1 Then
        Dim oNmContext As c_NmContext
        Set oNmContext = resolveNextContext(n)
        Set c_NmContextExt_resolve = oNmContext.resolve(n)
        Exit Function
    End If
    
    Dim sName As String
    sName = nameFld2nameStr(n.Item(0).id)
    If nameFld2nameStr(n.Item(0).kind) <> "" Then
        sName = sName & "." & nameFld2nameStr(n.Item(0).kind)
    End If
    Call printLine("resolve(" & sName & ")")
    'Show 0
    
    Dim oBindRec As cBindRecord
    Set oBindRec = resolveObject(n.Item(0))
    If oBindRec Is Nothing Then
        Dim oNFEx As c_NmNotFoundEx
        Set oNFEx = New c_NmNotFoundEx
        oNFEx.why = m_NmContext.not_object
        Set oNFEx.rest_of_name = New c_NmNameComponentSeq
        Call VBOrb.raiseUserEx(oNFEx)
    End If
    Set c_NmContextExt_resolve = oBindRec.oObject
End Function

Private Function c_NmContextExt_resolveStr(ByVal sn As String) As VBOrb.cOrbObject
    Call printLine("resolveStr(" & sn & ")")
    'Show 0
End Function

Private Sub c_NmContextExt_bind(ByVal n As c_NmNameComponentSeq, ByVal Obj As VBOrb.cOrbObject)
    If n.Length = 0 Then
        Dim oINEx As c_NmInvalidNameEx
        Set oINEx = New c_NmInvalidNameEx
        Call VBOrb.raiseUserEx(oINEx)
    End If
    If n.Length > 1 Then
        Dim oNmContext As c_NmContext
        Set oNmContext = resolveNextContext(n)
        Call oNmContext.bind(n, Obj)
        Exit Sub
    End If
    
    Dim sName As String
    sName = nameFld2nameStr(n.Item(0).id)
    If nameFld2nameStr(n.Item(0).kind) <> "" Then
        sName = sName & "." & nameFld2nameStr(n.Item(0).kind)
    End If
    Call printLine("bind(" & sName & ")")
    'Show 0
    
    Call bindObject(n.Item(0), Obj, m_Nm.nobject, False)
End Sub

Private Sub c_NmContextExt_bindContext(ByVal n As c_NmNameComponentSeq, ByVal nc As c_NmContext)
    If n.Length = 0 Then
        Dim oINEx As c_NmInvalidNameEx
        Set oINEx = New c_NmInvalidNameEx
        Call VBOrb.raiseUserEx(oINEx)
    End If
    If n.Length > 1 Then
        Dim oNmContext As c_NmContext
        Set oNmContext = resolveNextContext(n)
        Call oNmContext.bindContext(n, nc)
        Exit Sub
    End If
    
    Dim sName As String
    sName = nameFld2nameStr(n.Item(0).id)
    If nameFld2nameStr(n.Item(0).kind) <> "" Then
        sName = sName & "." & nameFld2nameStr(n.Item(0).kind)
    End If
    Call printLine("bindContext(" & sName & ")")
    'Show 0
    
    Call bindObject(n.Item(0), nc, m_Nm.ncontext, False)
End Sub

Private Function c_NmContextExt_bindNewContext(ByVal n As c_NmNameComponentSeq) As c_NmContext
    If n.Length = 0 Then
        Dim oINEx As c_NmInvalidNameEx
        Set oINEx = New c_NmInvalidNameEx
        Call VBOrb.raiseUserEx(oINEx)
    End If
    If n.Length > 1 Then
        Dim oNmContext As c_NmContext
        Set oNmContext = resolveNextContext(n)
        Call oNmContext.bindNewContext(n)
        Exit Function
    End If
    
    Dim sName As String
    sName = nameFld2nameStr(n.Item(0).id)
    If nameFld2nameStr(n.Item(0).kind) <> "" Then
        sName = sName & "." & nameFld2nameStr(n.Item(0).kind)
    End If
    Call printLine("bindNewContext(" & sName & ")")
    'Show 0
    
    Dim fNewNameContext As fNameContext
    Set fNewNameContext = New fNameContext
    Call fNewNameContext.initMe(oOrb, False)
    Call fNewNameContext.Show(0)
    On Error GoTo ErrHandler2
    Dim oNC As c_NmContext
    Set oNC = m_NmContext.narrow(fNewNameContext.getThis())
    Call bindObject(n.Item(0), oNC, m_Nm.ncontext, False)
    Exit Function
ErrHandler2:
    Call fNewNameContext.termMe
    Call VBOrb.ErrReraise(Err, "bindNewContext")
End Function

Private Sub c_NmContextExt_rebind(ByVal n As c_NmNameComponentSeq, ByVal Obj As VBOrb.cOrbObject)
    If n.Length = 0 Then
        Dim oINEx As c_NmInvalidNameEx
        Set oINEx = New c_NmInvalidNameEx
        Call VBOrb.raiseUserEx(oINEx)
    End If
    If n.Length > 1 Then
        Dim oNmContext As c_NmContext
        Set oNmContext = resolveNextContext(n)
        Call oNmContext.rebind(n, Obj)
        Exit Sub
    End If
    
    Dim sName As String
    sName = nameFld2nameStr(n.Item(0).id)
    If nameFld2nameStr(n.Item(0).kind) <> "" Then
        sName = sName & "." & nameFld2nameStr(n.Item(0).kind)
    End If
    Call printLine("rebind(" & sName & ")")
    'Show 0
    
    Call bindObject(n.Item(0), Obj, m_Nm.nobject, True)
End Sub

Private Sub c_NmContextExt_rebindContext(ByVal n As c_NmNameComponentSeq, ByVal nc As c_NmContext)
    If n.Length = 0 Then
        Dim oINEx As c_NmInvalidNameEx
        Set oINEx = New c_NmInvalidNameEx
        Call VBOrb.raiseUserEx(oINEx)
    End If
    If n.Length > 1 Then
        Dim oNmContext As c_NmContext
        Set oNmContext = resolveNextContext(n)
        Call oNmContext.rebindContext(n, nc)
        Exit Sub
    End If
    
    Dim sName As String
    sName = nameFld2nameStr(n.Item(0).id)
    If nameFld2nameStr(n.Item(0).kind) <> "" Then
        sName = sName & "." & nameFld2nameStr(n.Item(0).kind)
    End If
    Call printLine("rebindContext(" & sName & ")")
    'Show 0
    
    Call bindObject(n.Item(0), nc, m_Nm.ncontext, True)
End Sub

Private Sub c_NmContextExt_unbind(ByVal n As c_NmNameComponentSeq)
    If n.Length = 0 Then
        Dim oINEx As c_NmInvalidNameEx
        Set oINEx = New c_NmInvalidNameEx
        Call VBOrb.raiseUserEx(oINEx)
    End If
    If n.Length > 1 Then
        Dim oNmContext As c_NmContext
        Set oNmContext = resolveNextContext(n)
        Call oNmContext.unbind(n)
        Exit Sub
    End If
    
    Dim sName As String
    sName = nameFld2nameStr(n.Item(0).id)
    If nameFld2nameStr(n.Item(0).kind) <> "" Then
        sName = sName & "." & nameFld2nameStr(n.Item(0).kind)
    End If
    Call printLine("unbind(" & sName & ")")
    'Show 0
    
    Call unbindObject(n.Item(0))
End Sub

Private Sub c_NmContextExt_list(ByVal how_many As Long, _
    ByRef bl As c_NmBindingSeq, _
    ByRef bi As c_NmBindingIterator)
    On Error GoTo ErrHandler
    
    Call printLine("list")

    If how_many < 0 Then  'check for bad (unsigned long) how_many
        Call VBOrb.raiseBADPARAM(0, VBOrb.CompletedNO, _
            "how_many = " & CStr(how_many) & " <= 0")
    End If
    
    Dim lActual As Long
    If oColBindRecs Is Nothing Then
        lActual = 0
    Else
        lActual = oColBindRecs.Count
    End If
    
    Set bl = New c_NmBindingSeq
    If how_many < lActual Then
        bl.Length = how_many
    Else
        bl.Length = lActual
    End If
    
    Dim lIndex As Long
    For lIndex = 0 To bl.Length - 1
        Dim oBindRec As cBindRecord
        Set oBindRec = oColBindRecs(lIndex + 1)
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
        Set bl.Item(lIndex) = oB
    Next lIndex
    If bl.Length < lActual Then
        Dim oBIImpl As cNmBindingIteratorImpl
        Set oBIImpl = New cNmBindingIteratorImpl
        Set bi = oBIImpl.initMe(oOrb, bl.Length + 1, oColBindRecs, Me)
    End If
    'Show 0
    Exit Sub
ErrHandler:
    Call VBOrb.ErrReraise(Err, "list")
End Sub

Private Function c_NmContextExt_toName(ByVal sn As String) As c_NmNameComponentSeq
    Call printLine("toName(" & sn & ")")
    Dim oNameComps As c_NmNameComponentSeq
    Dim seqLen As Long
    Dim posStart As Long
    Dim posNext As Long
    'Write seqLen NameComponents
    Dim nameId As String
    Dim nameKind As String
    Dim posEnd1 As Long
    Set oNameComps = New c_NmNameComponentSeq
    seqLen = 0
    posStart = 1
    Do
        posNext = posStart
        Do
            posNext = InStr(posNext, sn, "/")
            If posNext <= 1 Then Exit Do
            If Mid(sn, posNext - 1, 1) <> "\" Then Exit Do
            posNext = posNext + 1
        Loop
        posEnd1 = IIf(posNext > 0, posNext, Len(sn) + 1)
        nameId = Mid$(sn, posStart, posEnd1 - posStart)
        posEnd1 = 1
        Do
            posEnd1 = InStr(posEnd1, nameId, ".")
            If posEnd1 <= 1 Then Exit Do
            If Mid(nameId, posEnd1 - 1, 1) <> "\" Then Exit Do
            posEnd1 = posEnd1 + 1
        Loop
        If posEnd1 = 0 Then
            nameKind = ""
        Else
            nameKind = Mid$(nameId, posEnd1 + 1)
            nameId = Left$(nameId, posEnd1 - 1)
        End If
        oNameComps.Length = seqLen + 1
        Set oNameComps.Item(seqLen) = New c_NmNameComponent
        oNameComps.Item(seqLen).id = nameStr2nameFld(nameId)
        oNameComps.Item(seqLen).kind = nameStr2nameFld(nameKind)
        seqLen = seqLen + 1
        If posNext = 0 Then
            Exit Do
        End If
        posStart = posNext + 1
    Loop
    Set c_NmContextExt_toName = oNameComps
End Function

Private Function c_NmContextExt_toString(ByVal n As c_NmNameComponentSeq) As String
    Dim name As String
    Dim nameId As String
    Dim nameKind As String
    name = ""
    Dim seqCnt As Long
    For seqCnt = 0 To n.Length - 1
        nameId = nameFld2nameStr(n.Item(seqCnt).id)
        nameKind = nameFld2nameStr(n.Item(seqCnt).kind)
        name = name & nameId
        If nameKind <> "" Then
            name = name & "." & nameKind
        End If
        If seqCnt < n.Length - 1 Then
            name = name & "/"
        End If
    Next seqCnt
    Call printLine("toString()= " & name)
    c_NmContextExt_toString = name
End Function

Private Function c_NmContextExt_toUrl(ByVal addr As String, ByVal sn As String) As String
    Call printLine("toUrl(" & addr & ", " & sn & ")")
    c_NmContextExt_toUrl = "corbaname:" & addr & "#" & nameStr2nameUrl(sn)
End Function

Private Sub c_NmContextExt_destroy()
    Call printLine("destroy")
    Call termMe
End Sub

Private Function c_NmContextExt_newContext() As c_NmContext
    Dim fNewNameContext As fNameContext
    Set fNewNameContext = New fNameContext
    Call fNewNameContext.initMe(oOrb, False)
    Call fNewNameContext.Show(0)
    Set c_NmContextExt_newContext = m_NmContext.narrow(fNewNameContext.getThis())
    Call printLine("newContext, corbaloc:" & fNewNameContext.getAddress())
    'Show 0
End Function

Public Sub clearResult()
    tbResult.Text = ""
End Sub

Public Sub printLine(ByVal sLine As String)
    tbResult.Text = tbResult.Text & sLine & vbCrLf
End Sub

Private Sub Form_Unload(Cancel As Integer)
    If Not oOrb Is Nothing And bIsRoot Then
        Call Me.Hide
        Cancel = True
    End If
End Sub

'US-ASCII alphanumeric characters plus the followings are not escaped.
'corbaloc  ;/:?@&=+$,-_!~*’()
'corbaname ;/:?@&=+$,-_.!~*’()
Private Function bytes2String(ByRef bytes() As Byte, ByRef notEscaped As String) _
    As String
    bytes2String = ""
    Dim iKey As Integer
    Dim bChr As Byte
    For iKey = LBound(bytes) To UBound(bytes)
        bChr = bytes(iKey)
        If (bChr >= Asc("0") And bChr <= Asc("9")) _
            Or (bChr >= Asc("A") And bChr <= Asc("Z")) _
            Or (bChr >= Asc("a") And bChr <= Asc("z")) Then
            bytes2String = bytes2String & Chr$(bChr)
        ElseIf bChr > 32 And bChr < 127 Then
            If InStr(1, notEscaped, Chr$(bChr)) > 0 Then
                bytes2String = bytes2String & Chr$(bChr)
            Else
                bytes2String = bytes2String & "%" & Hex$(bChr)
            End If
        ElseIf bChr <= &HF Then
            bytes2String = bytes2String & "%0" & Hex$(bChr)
        Else
            bytes2String = bytes2String & "%" & Hex$(bChr)
        End If
    Next iKey
End Function

'US-ASCII alphanumeric characters plus the followings are not escaped.
' ;/:?@&=+$,-_.!~*’()
'IN:    nameStr     Stringified name
'RET:   escStr      A stringified Name with URL escapes
Public Function nameStr2nameUrl(ByRef nameStr As String) As String
    Dim baName() As Byte
    If Len(nameStr) > 0 Then
        Dim strPos As Long
        ReDim baName(0 To Len(nameStr) - 1)
        For strPos = 1 To Len(nameStr)
            baName(strPos - 1) = Asc(Mid$(nameStr, strPos))
        Next
    Else
        'UBound(baName) = -1
        baName = MidB(nameStr, 1, 0)
    End If
    nameStr2nameUrl = bytes2String(baName, ";/:?@&=+$,-_.!~*'()")
End Function

'IN:    escStr      A stringified Name with URL escapes
'RET:   nameStr     Stringified name. If a ‘%’ is not followed by two hex digits,
'                   the stringified name is syntactically invalid.
Public Function nameUrl2NameStr(ByRef escStr As String) As String
    Dim strPos As Long
    Dim nameStr As String
    nameStr = ""
    strPos = 1
    Do
        If Mid$(escStr, strPos, 1) = "%" Then
            nameStr = nameStr & Chr$(Val("&H" & Mid$(escStr, strPos + 1, 2)))
            strPos = strPos + 3
        Else
            nameStr = nameStr & Mid$(escStr, strPos, 1)
            strPos = strPos + 1
        End If
    Loop While strPos <= Len(escStr)
    nameUrl2NameStr = nameStr
End Function

'Escape /.\ in name fields
'IN:    nameFld     Name field
'RET:   nameStr     Stringified name
Public Function nameFld2nameStr(ByRef nameFld As String) As String
    Dim strPos As Long
    Dim nameStr As String
    nameStr = ""
    For strPos = 1 To Len(nameFld)
        Dim sCh As String
        sCh = Mid$(nameFld, strPos, 1)
        If sCh = "/" Or sCh = "." Or sCh = "\" Then
            nameStr = nameStr & "\"
        End If
        nameStr = nameStr & sCh
    Next strPos
    nameFld2nameStr = nameStr
End Function

'Unescape /.\ in stringified names
'IN:    nameStr     Stringified name
'RET:   nameFld     Name field
Public Function nameStr2nameFld(ByRef nameStr As String) As String
    Dim strPos As Long
    Dim nameFld As String
    nameFld = ""
    strPos = 1
    Do
        If Mid$(nameStr, strPos, 1) = "\" Then
            nameFld = nameFld & Mid$(nameStr, strPos + 1, 1)
            strPos = strPos + 2
        Else
            nameFld = nameFld & Mid$(nameStr, strPos, 1)
            strPos = strPos + 1
        End If
    Loop While strPos <= Len(nameStr)
    nameStr2nameFld = nameFld
End Function


