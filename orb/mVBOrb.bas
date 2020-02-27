Attribute VB_Name = "mVBOrb"
'Copyright (c) 2000 Martin.Both

'This library is free software; you can redistribute it and/or
'modify it under the terms of the GNU Library General Public
'License as published by the Free Software Foundation; either
'version 2 of the License, or (at your option) any later version.

'This library is distributed in the hope that it will be useful,
'but WITHOUT ANY WARRANTY; without even the implied warranty of
'MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
'Library General Public License for more details.

Option Explicit

'Set DebugMode = 0 to deactivate debug code in this module
#Const DebugMode = 1

'Fallback code set for char data= "X/Open UTF-8; UCS Transformation Format 8 (UTF-8)"
'&H05010001

'ORB Native Code Set
'VB native_code_set ="ISO 8859-1:1987; Latin Alphabet No. 1"
Const lONCS_C As Long = &H10001
'VB BSTR native_code_set = "ISO/IEC 10646-1:1993; UTF-16, UCS Transformation Format 16-bit form"
Const lONCS_W As Long = &H10109
'JDK 1.3.1 HP-UX:  C= &H10020, W= &H10100

'ORB pool
Private oOrbs As cOrbImpl

'Global exception memory
Private oGlobalEx As cOrbException

'GetTickCount();
Public Declare Function dllGetTickCount Lib "kernel32" _
    Alias "GetTickCount" () As Long

'GetSystemTime();
Private Type SYSTEMTIME
    wYear As Integer
    wMonth As Integer
    wDayOfWeek As Integer
    wDay As Integer
    wHour As Integer
    wMinute As Integer
    wSecond As Integer
    wMilliseconds As Integer
End Type
Private Declare Sub dllGetSystemTime Lib "kernel32" _
    Alias "GetSystemTime" (ByRef lpSystemTime As SYSTEMTIME)

#If DebugMode Then
    Private lClassDebugIDPool As Long
#End If

Private lUniqueIDPool As Long

Private Type tErr
    lNumber As Long
    sDescription As String
    sSource As String
End Type
Private aErrs(0 To 9) As tErr
Private lErrIdx As Long

Public Const MinorOMGVMCID As Long = &H4F4D0000

Public Const CompletedYES As Long = 0&
Public Const CompletedNO As Long = 1&
Public Const CompletedMAYBE As Long = 2&

Public Const ITF_E_UNKNOWN_NO As Long = &H40200
Public Const ITF_E_BAD_PARAM_NO As Long = &H40201
Public Const ITF_E_NO_MEMORY_NO As Long = &H40202
Public Const ITF_E_IMP_LIMIT_NO As Long = &H40203
Public Const ITF_E_COMM_FAILURE_NO As Long = &H40204
Public Const ITF_E_INV_OBJREF_NO As Long = &H40205
Public Const ITF_E_NO_PERMISSION_NO As Long = &H40206
Public Const ITF_E_INTERNAL_NO As Long = &H40207
Public Const ITF_E_MARSHAL_NO As Long = &H40208
Public Const ITF_E_INITIALIZE_NO As Long = &H40209
Public Const ITF_E_NO_IMPLEMENT_NO As Long = &H4020A
Public Const ITF_E_BAD_TYPECODE_NO As Long = &H4020B
Public Const ITF_E_BAD_OPERATION_NO As Long = &H4020C
Public Const ITF_E_NO_RESOURCES_NO As Long = &H4020D
Public Const ITF_E_NO_RESPONSE_NO As Long = &H4020E
Public Const ITF_E_PERSIST_STORE_NO As Long = &H4020F
Public Const ITF_E_BAD_INV_ORDER_NO As Long = &H40210
Public Const ITF_E_TRANSIENT_NO As Long = &H40211
Public Const ITF_E_FREE_MEM_NO As Long = &H40212
Public Const ITF_E_INV_IDENT_NO As Long = &H40213
Public Const ITF_E_INV_FLAG_NO As Long = &H40214
Public Const ITF_E_INTF_REPOS_NO As Long = &H40215
Public Const ITF_E_BAD_CONTEXT_NO As Long = &H40216
Public Const ITF_E_OBJ_ADAPTER_NO As Long = &H40217
Public Const ITF_E_DATA_CONVERSION_NO As Long = &H40218
Public Const ITF_E_OBJECT_NOT_EXIST_NO As Long = &H40219
Public Const ITF_E_TRANSACTION_REQUIRED_NO As Long = &H40220
Public Const ITF_E_TRANSACTION_ROLLEDBACK_NO As Long = &H40221
Public Const ITF_E_INVALID_TRANSACTION_NO As Long = &H40222
Public Const ITF_E_INV_POLICY_NO As Long = &H40223
Public Const ITF_E_CODESET_INCOMPATIBLE_NO As Long = &H40224
Public Const ITF_E_REBIND_NO As Long = &H40225
Public Const ITF_E_TIMEOUT_NO As Long = &H40226
Public Const ITF_E_TRANSACTION_UNAVAILABLE_NO As Long = &H40227
Public Const ITF_E_TRANSACTION_MODE_NO As Long = &H40228
Public Const ITF_E_BAD_QOS_NO As Long = &H40229
Public Const ITF_E_USER_EX As Long = &H402FF

Private Const sExPrefix As String = "IDL:omg.org/CORBA/"
Private Const sExPostfix As String = ":1.0"

Private collITF As New Collection

'GlobalMultiUse Object
Private oVBOrb As New cVBOrb

#If DebugMode Then
    'Get a unique class ID
    Public Function getNextClassDebugID() As Long
        lClassDebugIDPool = lClassDebugIDPool + 1
        getNextClassDebugID = lClassDebugIDPool
    End Function
#End If

'ORB Native Code Set
Public Property Get ONCSC() As Long
    ONCSC = lONCS_C
End Property

Public Property Get ONCSW() As Long
    ONCSW = lONCS_W
End Property

Public Function getNextUniqueID() As Long
    lUniqueIDPool = lUniqueIDPool + 1
    getNextUniqueID = lUniqueIDPool
End Function

Public Function getLongDiff(ByVal l1 As Long, ByVal l2 As Long) As Long
    getLongDiff = IIf(l1 > l2, l1 - l2, l2 - l1)
End Function

Public Function getTime() As Long
    Dim lpSystemTime As SYSTEMTIME
    Call dllGetSystemTime(lpSystemTime) 'is about 50 times faster than "Now"
    getTime = lpSystemTime.wMonth * 31 + lpSystemTime.wDay
    getTime = getTime * 24 + lpSystemTime.wHour
    getTime = getTime * 60 + lpSystemTime.wMinute
    getTime = getTime * 60 + lpSystemTime.wSecond
End Function

'GIOPVersion may be lower than IIOPVersion
Public Function GIOPVersion2Str(ByVal GIOPVersion As Integer) As String
    Select Case GIOPVersion
    Case &H100
        GIOPVersion2Str = "1.0"
    Case &H101
        GIOPVersion2Str = "1.1"
    Case &H102
        GIOPVersion2Str = "1.2"
    Case Else
        'GIOPVersion2Str = CStr(iVer \ &H100) & "." & CStr(iVer Mod &H100)
        Call mVBOrb.VBOrb.raiseBADPARAM(8, VBOrb.CompletedNO, _
            "Invalid GIOP version " & CStr(GIOPVersion))
    End Select
End Function

'ORB Initialization (is called by VBOrb.init, see cVBOrb.init and cOrbImpl.init)
Public Function init(ByVal ORBId As String, _
    ByVal OAHost As String, ByVal OAPort As String, ByVal OAVersion As Integer, _
    ByVal ORBDefaultInitRef As String, ByVal ORBInitRef As String, _
    ByVal LogFile As String, ByVal LogLevel As Integer, ByVal VisiWorkaround As Boolean) _
    As cOrbImpl
    If ORBId <> "" Then
        Call mVBOrb.VBOrb.raiseNOIMPLEMENT(1, VBOrb.CompletedNO, _
            "Different ORBIds are not supported")
    End If
    If Not oOrbs Is Nothing Then
        If oOrbs.isDown() Then
            Set oOrbs = Nothing
        End If
    End If
    If oOrbs Is Nothing Then
        Dim oNewOrb As cOrbImpl
        Set oNewOrb = New cOrbImpl
        Call oNewOrb.init(ORBId, OAHost, OAPort, OAVersion, ORBDefaultInitRef, _
            ORBInitRef, LogFile, LogLevel, VisiWorkaround)
        Set oOrbs = oNewOrb
    End If
    Set init = oOrbs
End Function

'Get an ORB to create TypeCodes
Public Function defaultOrb() As cOrbImpl
    If Not oOrbs Is Nothing Then
        If oOrbs.isDown() Then
            Set oOrbs = Nothing
        End If
    End If
    If oOrbs Is Nothing Then
        Call mVBOrb.VBOrb.raiseBADINVORDER(0, VBOrb.CompletedNO, _
            "Initialize an ORB before using it")
    End If
    Set defaultOrb = oOrbs
End Function

'Write an exception onto a log and delete the exception
Public Sub logException(ByRef sLogFile As String, ByVal oUserEx As cOrbException)
    If Len(sLogFile) > 0 Then
        Call logMsg(sLogFile, "Exception: " & oUserEx.Source & ", " _
            & oUserEx.Description)
    End If
End Sub

'If "On Error Resume Next" is on then
'write the Error onto a log file and delete the Error
Public Sub logErr(ByRef sLogFile As String, ByRef SourcePrefix As String)
    If Len(sLogFile) > 0 Then
        Dim sNumberStr As String
        If (Err.Number And vbObjectError) = vbObjectError Then
            sNumberStr = "0x" & Hex$(Err.Number)
        Else
            sNumberStr = CStr(Err.Number)
        End If
        Call logMsg(sLogFile, "Error: " & sNumberStr & ", " _
            & IIf(Err.Source = "", SourcePrefix, SourcePrefix & ":" & Err.Source) _
            & ", " & Err.Description)
    End If
    Call Err.Clear
End Sub

'Write a message onto a log file
Public Sub logMsg(ByRef sLogFile As String, ByRef sMsg As String)
    If Len(sLogFile) > 0 Then
        Dim iFileNo As Integer
        iFileNo = FreeFile
        Open sLogFile For Append As #iFileNo
        Print #iFileNo, getDateTimeStr() & " " & sMsg
        Close #iFileNo
    End If
End Sub

'Format(Now, "yyyy-mm-dd HH:MM:SS")
Public Function getDateTimeStr() As String
    Dim lpSystemTime As SYSTEMTIME      'Is about 50 times faster than "Now"
    Call dllGetSystemTime(lpSystemTime) 'but different hours
    getDateTimeStr = CStr(lpSystemTime.wYear) & "-" _
        & format00(lpSystemTime.wMonth) & "-" _
        & format00(lpSystemTime.wDay) & " " _
        & format00(lpSystemTime.wHour) & ":" _
        & format00(lpSystemTime.wMinute) & ":" _
        & format00(lpSystemTime.wSecond)
End Function

'Is equal to Format(iVal, "00")
Private Function format00(ByVal iVal As Integer) As String
    format00 = CStr(iVal)
    If iVal < 10 Then format00 = "0" & format00
End Function

Public Sub ErrRaise(ByVal Number As Long, ByRef Description As String, _
    Optional ByVal Source As String = "")
    Call Err.Raise(vbObjectError Or Number, Source, Description, "", 0)
End Sub

Public Sub ErrReraise(ByRef SourcePrefix As String, _
    Optional ByRef PostDescr As String = "")
    If (Err.Number And vbObjectError) = vbObjectError Then
        Call Err.Raise(Err.Number, _
            IIf(Err.Source = "", SourcePrefix, SourcePrefix & ":" & Err.Source), _
            IIf(PostDescr = "", Err.Description, Err.Description & ", " & PostDescr), _
            "", 0)
    Else 'Overwrite Err.Source
        Call Err.Raise(vbObjectError Or Err.Number, SourcePrefix, _
            IIf(PostDescr = "", Err.Description, Err.Description & ", " & PostDescr), _
            "", 0)
    End If
End Sub

'Is not calling Err.Clear
Public Sub ErrSave()
    'If Number= 0 then Err.Raise 0 is invalid
    If Err.Number = 0 Then
        Call ErrRaise(1, "Err.Number = 0", "ErrSave")
    End If
    If lErrIdx > UBound(aErrs) Then
        lErrIdx = 0
        Call ErrRaise(1, "lErrIdx > " & CStr(UBound(aErrs)), "ErrSave")
    End If
    aErrs(lErrIdx).lNumber = Err.Number
    aErrs(lErrIdx).sDescription = Err.Description
    aErrs(lErrIdx).sSource = Err.Source
    lErrIdx = lErrIdx + 1
End Sub

Public Sub ErrReplace()
    lErrIdx = lErrIdx - 1
    If lErrIdx < 0 Then
        lErrIdx = 0
        Call ErrRaise(1, "lErrIdx < 0", "ErrReplace")
    End If
    Call ErrSave
End Sub

Public Sub ErrLoad()
    lErrIdx = lErrIdx - 1
    If lErrIdx < 0 Then
        lErrIdx = 0
        Call ErrRaise(1, "lErrIdx < 0", "ErrLoad")
    End If
    On Error Resume Next
    Call Err.Raise(aErrs(lErrIdx).lNumber, aErrs(lErrIdx).sSource, _
        aErrs(lErrIdx).sDescription)
End Sub

Private Function lookupITF(ByRef ExceptionId As String) As Long
    If collITF.Count = 0 Then
        Call collITF.Add(ITF_E_UNKNOWN_NO, "UNKNOWN")
        Call collITF.Add(ITF_E_BAD_PARAM_NO, "BAD_PARAM")
        Call collITF.Add(ITF_E_NO_MEMORY_NO, "NO_MEMORY")
        Call collITF.Add(ITF_E_IMP_LIMIT_NO, "IMP_LIMIT")
        Call collITF.Add(ITF_E_COMM_FAILURE_NO, "COMM_FAILURE")
        Call collITF.Add(ITF_E_INV_OBJREF_NO, "INV_OBJREF")
        Call collITF.Add(ITF_E_NO_PERMISSION_NO, "NO_PERMISSION")
        Call collITF.Add(ITF_E_INTERNAL_NO, "INTERNAL")
        Call collITF.Add(ITF_E_MARSHAL_NO, "MARSHAL")
        Call collITF.Add(ITF_E_INITIALIZE_NO, "INITIALIZE")
        Call collITF.Add(ITF_E_NO_IMPLEMENT_NO, "NO_IMPLEMENT")
        Call collITF.Add(ITF_E_BAD_TYPECODE_NO, "BAD_TYPECODE")
        Call collITF.Add(ITF_E_BAD_OPERATION_NO, "BAD_OPERATION")
        Call collITF.Add(ITF_E_NO_RESOURCES_NO, "NO_RESOURCES")
        Call collITF.Add(ITF_E_NO_RESPONSE_NO, "NO_RESPONSE")
        Call collITF.Add(ITF_E_PERSIST_STORE_NO, "PERSIST_STORE")
        Call collITF.Add(ITF_E_BAD_INV_ORDER_NO, "BAD_INV_ORDER")
        Call collITF.Add(ITF_E_TRANSIENT_NO, "TRANSIENT")
        Call collITF.Add(ITF_E_FREE_MEM_NO, "FREE_MEM")
        Call collITF.Add(ITF_E_INV_IDENT_NO, "INV_IDENT")
        Call collITF.Add(ITF_E_INV_FLAG_NO, "INV_FLAG")
        Call collITF.Add(ITF_E_INTF_REPOS_NO, "INTF_REPOS")
        Call collITF.Add(ITF_E_BAD_CONTEXT_NO, "BAD_CONTEXT")
        Call collITF.Add(ITF_E_OBJ_ADAPTER_NO, "OBJ_ADAPTER")
        Call collITF.Add(ITF_E_DATA_CONVERSION_NO, "DATA_CONVERSION")
        Call collITF.Add(ITF_E_OBJECT_NOT_EXIST_NO, "OBJECT_NOT_EXIST")
        Call collITF.Add(ITF_E_TRANSACTION_REQUIRED_NO, "TRANSACTION_REQUIRED")
        Call collITF.Add(ITF_E_TRANSACTION_ROLLEDBACK_NO, "TRANSACTION_ROLLEDBACK")
        Call collITF.Add(ITF_E_INVALID_TRANSACTION_NO, "INVALID_TRANSACTION")
        Call collITF.Add(ITF_E_INV_POLICY_NO, "INV_POLICY")
        Call collITF.Add(ITF_E_CODESET_INCOMPATIBLE_NO, "CODESET_INCOMPATIBLE")
        Call collITF.Add(ITF_E_REBIND_NO, "REBIND")
        Call collITF.Add(ITF_E_TIMEOUT_NO, "TIMEOUT")
        Call collITF.Add(ITF_E_TRANSACTION_UNAVAILABLE_NO, "TRANSACTION_UNAVAILABLE")
        Call collITF.Add(ITF_E_TRANSACTION_MODE_NO, "TRANSACTION_MODE")
        Call collITF.Add(ITF_E_BAD_QOS_NO, "BAD_QOS")
    End If
    Dim sId As String
    Dim lPos As Long
    Dim rPos As Long
    lPos = Len(sExPrefix) + 1
    rPos = InStr(ExceptionId, sExPostfix)
    If InStr(ExceptionId, sExPrefix) <> 1 Or rPos <= 0 Then
        lookupITF = 0
    Else
        sId = Mid$(ExceptionId, lPos, rPos - lPos)
        On Error Resume Next
        lookupITF = collITF.Item(sId)
        If Err.Number <> 0 Then lookupITF = 0
        On Error GoTo 0
    End If
End Function

'IN:    id          ExceptionId
'IN:    minor       MinorCodeValue
'IN:    completed   CompletionStatus
'IN:    source      <interface name>.<operation name>
Public Sub raiseSystemException(ByVal Number As Long, ByVal id As String, _
    ByVal minor As Long, ByVal completed As Long, _
    Optional ByRef PostDescr As String = "", _
    Optional ByVal Source As String = "")
    Dim Description As String
    Description = "CORBA System Exception: [" & id & "] minor code [" _
        & CStr(minor) & "]"
    Select Case completed
    Case CompletedYES
        Number = Number Or &H41000 Or vbObjectError
        Description = Description & "[YES]"
    Case CompletedNO
        Number = Number Or &H40000 Or vbObjectError
        Description = Description & "[NO]"
    Case Else
        Number = Number Or &H42000 Or vbObjectError
        Description = Description & "[MAYBE]"
    End Select
    Call mVBOrb.ErrRaise(Number, _
        IIf(PostDescr = "", Description, Description & ", " & PostDescr), _
        Source)
End Sub

'Helper
Public Sub readRaiseSystemEx(ByVal oIn As cOrbStream, _
    Optional ByRef PostDescr As String = "")
    On Error GoTo ErrHandler
    'GIOP::SystemExceptionReplyBody
    Dim ExceptionId As String 'string exception_id;
    Dim MinorCodeValue As Long 'unsigned long minor_code_value;
    Dim CompletionStatus As Long 'unsigned long completion_status;
    ExceptionId = oIn.readString()
    MinorCodeValue = oIn.readUlong()
    CompletionStatus = oIn.readUlong()
    Dim lITF As Long
    lITF = lookupITF(ExceptionId)
    If lITF <= 0 Then
        'Has to raise UNKNOWN with standard minor code set to 2
        Call VBOrb.raiseUNKNOWN(2, CompletionStatus, _
            "Undefined CORBA System Exception: [" & ExceptionId & "] minor code [" _
            & CStr(MinorCodeValue) & "]")
    Else
        Call raiseSystemException(lITF, ExceptionId, _
            MinorCodeValue, CompletionStatus, PostDescr)
    End If
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("readRaiseSystemEx")
End Sub

'Helper
Public Sub ErrWriteSystemEx(ByVal oOut As cOrbStream)
    Dim sDescription As String
    Dim lStart As Long
    Dim lEnd As Long
    Dim sExceptionId As String
    Dim lMinor As Long 'MinorCodeValue
    Dim lCompleted As Long 'CompletionStatus
    
    Call mVBOrb.ErrSave
    On Error GoTo ErrHandler
    Call mVBOrb.ErrLoad
    sDescription = Err.Description
    
    lStart = InStr(sDescription, "[")
    If lStart > 0 Then lEnd = InStr(lStart, sDescription, "]")
    If lStart = 0 Or lEnd <= lStart Then
        sExceptionId = sExPrefix & "UNKNOWN" & sExPostfix
        lMinor = 1
        lCompleted = CompletedMAYBE
        'Call mvborb.ErrRaise(1, "Empty or invalid description" _
        '    & ", Descr= " & sDescription _
        '    & ", Source= " & sSource, "ErrWriteSystemEx")
    Else
        lStart = lStart + 1
        sExceptionId = Mid$(sDescription, lStart, lEnd - lStart)
        lStart = InStr(lEnd, sDescription, "[")
        If lStart > 0 Then lEnd = InStr(lStart, sDescription, "]")
        If lStart = 0 Or lEnd <= lStart Then
            lMinor = 1
            lCompleted = CompletedMAYBE
        Else
            lStart = lStart + 1
            lMinor = CLng(Mid$(sDescription, lStart, lEnd - lStart))
            If InStr(lEnd, sDescription, "[YES]", vbTextCompare) > 0 Then
                lCompleted = CompletedYES
            ElseIf InStr(lEnd, sDescription, "[NO]", vbTextCompare) > 0 Then
                lCompleted = CompletedNO
            Else
                lCompleted = CompletedMAYBE
            End If
        End If
    End If
    Call oOut.writeString(sExceptionId)
    Call oOut.writeUlong(lMinor) 'MinorCodeValue
    Call oOut.writeUlong(lCompleted) 'CompletionStatus
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("writeSystemException")
End Sub

'CORBA system exceptions are raised using the VBOrb.DLL ErrObject
Public Function ErrIsSystemEx() As Boolean
    If (Err.Number And (&H40200 Or vbObjectError)) = _
        (&H40200 Or vbObjectError) Then
        ErrIsSystemEx = InStr(Err.Description, "CORBA System Exception:") <> 0
    Else
        ErrIsSystemEx = False
    End If
End Function

'CORBA user exceptions are raised using the VBOrb.DLL ErrObject
Public Function ErrIsUserEx() As Boolean
    If (Err.Number And (&H402F0 Or vbObjectError)) = _
        (&H402F0 Or vbObjectError) Then
        ErrIsUserEx = InStr(Err.Description, "CORBA User Exception:") <> 0
    Else
        ErrIsUserEx = False
    End If
End Function

'Raise a CORBA user exception
'IN:    oEx         User Exception
Public Sub raiseUserEx(ByVal oEx As cOrbException)
    Set oGlobalEx = oEx
    Call mVBOrb.ErrRaise(ITF_E_USER_EX Or vbObjectError, _
        oEx.Description, oEx.Source)
End Sub

'Get a CORBA user exception and delete the VBOrb.DLL ErrObject
Public Function getException() As cOrbException
    Set getException = oGlobalEx
    Set oGlobalEx = Nothing
    Call Err.Clear 'Set ErrIsUserEx() to False
End Function

'Get the GlobalMultiUse Object to access global used functions
Public Property Get VBOrb() As cVBOrb
    Set VBOrb = oVBOrb
End Property

'US-ASCII alphanumeric characters plus the followings are not escaped.
'corbaloc  ;/:?@&=+$,-_!~*’()
Public Function objKey2String(ByRef objKey() As Byte) As String
    objKey2String = bytes2String(objKey, ";/:?@&=+$,-_!~*'()")
End Function

Public Sub string2ObjKey(ByRef keyStr As String, ByRef objKey() As Byte)
    If Len(keyStr) > 0 Then
        Dim strPos As Long
        Dim keyLen As Long
        ReDim objKey(0 To Len(keyStr) - 1)
        strPos = 1
        keyLen = 0
        Do
            If Mid$(keyStr, strPos, 1) = "%" Then
                objKey(keyLen) = val("&H" & Mid$(keyStr, strPos + 1, 2))
                strPos = strPos + 3
            Else
                objKey(keyLen) = Asc(Mid$(keyStr, strPos))
                strPos = strPos + 1
            End If
            keyLen = keyLen + 1
        Loop While strPos <= Len(keyStr)
        ReDim Preserve objKey(0 To keyLen - 1)
    Else
        'UBound(objKey) = -1
        objKey = MidB(keyStr, 1, 0)
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

'IN:    escStr      A stringified Name with URL escapes. If a ‘%’ is not followed
'                   by two hex digits, the stringified name is syntactically invalid.
'RET:   nameStr     Stringified name
Public Function nameUrl2NameStr(ByRef escStr As String) As String
    Dim strPos As Long
    Dim nameStr As String
    nameStr = ""
    strPos = 1
    Do
        If Mid$(escStr, strPos, 1) = "%" Then
            nameStr = nameStr & Chr$(val("&H" & Mid$(escStr, strPos + 1, 2)))
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

'Call the "get" operation of the SunInitialReference
Public Function sunInitGetNameService(ByVal SunInitRef As cOrbObjRef) As cOrbObject
    On Error GoTo ErrHandler
    Dim oRequest As cOrbRequest
    Set oRequest = SunInitRef.Request("get", False)
    Dim oOut As cOrbStream
    Set oOut = oRequest.InArg
    Call oOut.writeString("NameService")
    Dim oIn As cOrbStream
    Call oRequest.invokeReqst(False)
    Set oIn = oRequest.OutRes
    Set sunInitGetNameService = oIn.readObject()
    Exit Function
ErrHandler:
    Set sunInitGetNameService = Nothing
    Call mVBOrb.ErrReraise("sunInitGetNameService")
End Function

'Call the "resolve" operation of the NamingContext
'Convert sStringName to a CosNaming::Name
'Pass CosNaming::Name to a resolve operation on the naming context
'Or use resolve_str of CosNamingExt
Public Function nameContextResolveName(ByVal NmCtxRef As cOrbObjRef, _
    ByRef name As String) As cOrbObject
    On Error GoTo ErrHandler
    'resolve() raises(c_NmNotFoundEx, c_NmCannotProceedEx, c_NmInvalidNameEx)
    Dim oRequest As cOrbRequest
    Set oRequest = NmCtxRef.Request("resolve", False)
    Dim oOut As cOrbStream
    Set oOut = oRequest.InArg
    Call nameServiceNameWrite(oOut, name)
    Dim oIn As cOrbStream
    If oRequest.invokeReqst(True) Then
        Set oIn = oRequest.OutRes
        Dim sTypeId As String
        sTypeId = oIn.readString()
        Dim sErrMsg As String
        Select Case sTypeId
        Case "IDL:omg.org/CosNaming/NamingContext/NotFound:1.0"
            sErrMsg = nameServiceExNotFoundRead(oIn)
        Case "IDL:omg.org/CosNaming/NamingContext/CannotProceed:1.0"
            sErrMsg = nameServiceExCannotProceedRead(oIn)
        Case "IDL:omg.org/CosNaming/NamingContext/InvalidName:1.0"
            sErrMsg = nameServiceExInvalidNameRead(oIn)
        Case Else
            Call VBOrb.raiseUNKNOWN(1, VBOrb.CompletedMAYBE, _
                "Undefined CORBA User Exception: [" & sTypeId & "]")
        End Select
        Call VBOrb.raiseBADPARAM(10, VBOrb.CompletedNO, sErrMsg)
    Else
        Set oIn = oRequest.OutRes
        Set nameContextResolveName = oIn.readObject()
    End If
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("nameContextResolveName")
End Function

'Write sequence<::CosNaming::NameComponent>
'IN:    name    Stringified name
Private Sub nameServiceNameWrite(ByVal oOut As cOrbStream, ByRef name As String)
    On Error GoTo ErrHandler
    Dim posStart As Long
    Dim posNext As Long
    Dim seqLen As Long
    'Calculate and write NameComponent sequence lenght
    posStart = 1
    seqLen = 0
    Do
        posNext = posStart
        Do
            posNext = InStr(posNext, name, "/")
            If posNext <= 1 Then Exit Do
            If Mid(name, posNext - 1, 1) <> "\" Then Exit Do
            posNext = posNext + 1
        Loop
        seqLen = seqLen + 1
        If posNext = 0 Then
            Exit Do
        End If
        posStart = posNext + 1
    Loop
    Call oOut.writeUlong(seqLen)
    'Write seqLen NameComponents
    Dim nameId As String
    Dim nameKind As String
    Dim posEnd1 As Long
    posStart = 1
    Do
        posNext = posStart
        Do
            posNext = InStr(posNext, name, "/")
            If posNext <= 1 Then Exit Do
            If Mid(name, posNext - 1, 1) <> "\" Then Exit Do
            posNext = posNext + 1
        Loop
        posEnd1 = IIf(posNext > 0, posNext, Len(name) + 1)
        nameId = Mid$(name, posStart, posEnd1 - posStart)
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
        Call oOut.writeString(nameStr2nameFld(nameId))
        Call oOut.writeString(nameStr2nameFld(nameKind))
        If posNext = 0 Then
            Exit Do
        End If
        posStart = posNext + 1
    Loop
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("nameServiceNameWrite")
End Sub

'Read sequence<::CosNaming::NameComponent>
'RET:   name    Stringified name
Private Function nameServiceNameRead(ByVal oIn As cOrbStream) As String
    On Error GoTo ErrHandler
    Dim seqLen As Long
    seqLen = oIn.readUlong()
    If seqLen < 0 And seqLen > 10000 Then
        Call VBOrb.raiseMARSHAL(1, VBOrb.CompletedNO, _
            "SeqLen is out of range: " & CStr(seqLen))
    End If
    Dim name As String
    Dim nameId As String
    Dim nameKind As String
    name = ""
    Dim seqCnt As Long
    For seqCnt = 1 To seqLen
        nameId = nameFld2nameStr(oIn.readString())
        nameKind = nameFld2nameStr(oIn.readString())
        name = name & nameId
        If nameKind <> "" Then
            name = name & "." & nameKind
        End If
        If seqCnt < seqLen Then
            name = name & "/"
        End If
    Next seqCnt
    nameServiceNameRead = name
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("nameServiceNameRead")
End Function

'Read exception ::CosNaming::NamingContext::NotFound
'RET:   sErrMsg     Error message
Private Function nameServiceExNotFoundRead(ByVal oIn As cOrbStream) As String
    On Error GoTo ErrHandler
    Dim sErrMsg As String
    sErrMsg = "NameNotFound,why="
    Dim lWhy As Long
    lWhy = oIn.readUlong()
    Select Case lWhy
    Case 0: 'missing_node
        sErrMsg = sErrMsg & "missing_node"
    Case 1: 'not_context
        sErrMsg = sErrMsg & "not_context"
    Case 2: 'not_object
        sErrMsg = sErrMsg & "not_object"
    Case Else
        sErrMsg = sErrMsg & CStr(lWhy)
    End Select
    sErrMsg = sErrMsg & ",rest_of_name="
    sErrMsg = sErrMsg & nameServiceNameRead(oIn)
    nameServiceExNotFoundRead = sErrMsg
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("nameServiceExNotFoundRead")
End Function

'Read exception ::CosNaming::NamingContext::NotFound
'RET:   sErrMsg     Error message
Private Function nameServiceExCannotProceedRead(ByVal oIn As cOrbStream) As String
    On Error GoTo ErrHandler
    Dim sErrMsg As String
    sErrMsg = "CannotProceed,cxt="
    'NamingContext cxt;
    Dim oCxtObjRef As cOrbObjRef
    Set oCxtObjRef = oIn.readObject()
    sErrMsg = sErrMsg & oCxtObjRef.IIOPAddress
    sErrMsg = sErrMsg & ",rest_of_name="
    sErrMsg = sErrMsg & nameServiceNameRead(oIn)
    nameServiceExCannotProceedRead = sErrMsg
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("nameServiceExCannotProceedRead")
End Function

'Read exception ::CosNaming::NamingContext::NotFound
'RET:   sErrMsg     Error message
Private Function nameServiceExInvalidNameRead(ByVal oIn As cOrbStream) As String
    On Error GoTo ErrHandler
    Dim sErrMsg As String
    sErrMsg = "InvalidName"
    nameServiceExInvalidNameRead = sErrMsg
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("nameServiceExInvalidNameRead")
End Function

Public Function TypeIdEquals(ByRef sTypeId1 As String, ByRef sTypeId2 As String, _
    ByVal bExact As Boolean) As Boolean
    If sTypeId1 = sTypeId2 Then
        TypeIdEquals = True
        Exit Function
    End If
    If bExact Then
        TypeIdEquals = False
        Exit Function
    End If
    If Left$(sTypeId1, 4) = "RMI:" And Left$(sTypeId2, 4) = "RMI:" Then
        Dim iEnd1 As Integer
        Dim iEnd2 As Integer
        iEnd1 = InStr(5, sTypeId1, ":")
        iEnd2 = InStr(5, sTypeId2, ":")
        If iEnd1 > 0 And iEnd2 > 0 And iEnd1 = iEnd2 Then
            If Left$(sTypeId1, iEnd1) = Left$(sTypeId2, iEnd2) Then
                TypeIdEquals = True
                Exit Function
            End If
        End If
    End If
    TypeIdEquals = False
End Function

Public Function TypeIdIsInVal(ByRef sTypeId As String, _
    ByVal oValueBase As cOrbValueBase, ByVal bExact As Boolean) As Boolean
    Dim iIdCnt As Integer
    Dim sIdStr As String
    'We do not test the first (0) item id
    iIdCnt = 1
    Do
        sIdStr = oValueBase.getIds(iIdCnt)
        If sIdStr = "" Then
            TypeIdIsInVal = False
            Exit Do
        End If
        If TypeIdEquals(sTypeId, sIdStr, bExact) Then
            TypeIdIsInVal = True
            Exit Do
        End If
        iIdCnt = iIdCnt + 1
    Loop
End Function

