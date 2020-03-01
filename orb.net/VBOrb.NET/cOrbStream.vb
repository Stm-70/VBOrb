Option Strict Off
Option Explicit On
Imports System.Runtime.InteropServices

<System.Runtime.InteropServices.ProgId("cOrbStream_NET.cOrbStream")> Public Class cOrbStream
    'Copyright (c) 1999 Martin.Both

    'This library is free software; you can redistribute it and/or
    'modify it under the terms of the GNU Library General Public
    'License as published by the Free Software Foundation; either
    'version 2 of the License, or (at your option) any later version.

    'This library is distributed in the hope that it will be useful,
    'but WITHOUT ANY WARRANTY; without even the implied warranty of
    'MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
    'Library General Public License for more details.


    'Set DebugMode = 0 to deactivate debug code in this class
#Const DebugMode = 0

#If DebugMode Then
	'UPGRADE_NOTE: #If #EndIf block was not upgraded because the expression DebugMode did not evaluate to True or was not evaluated. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="27EE2C3C-05AF-4C04-B2AF-657B4FB6B5FC"'
	Private lClassDebugID As Long
#End If

    'Is initialized?
    Private oOrb As cOrbImpl
    Private iLogLevel As Short

    'Transmission Code Set
    Private lTCS_C As Integer
    Private lTCS_W As Integer

    'Common Data Representation (CDR)
    Private buffer() As Byte
    Private bufOff As Integer 'Offset (= bufStart), alignment position
    Private bufEof As Integer '(= bufEnd + 1), (bufLen = bufEof - bufStart)
    Private bufPos As Integer 'Next Position (buffer(bufPos))
    Private bLittleEndian As Boolean

    'Encapsulation Stack
    Private Structure tEncap
        Dim bufOff As Integer
        Dim bufEof As Integer
        Dim bufPos As Integer
        Dim bLittleEndian As Boolean
    End Structure
    Private encapCnt As Integer
    Private encaps() As tEncap

    'GIOP Version may be lower than IIOP Version
    Private iGIOPVersion As Short 'Example: &H0, &H100, &H101, &H102
    Private bMoreFrags As Boolean
    Private bIsComplete As Boolean

    Const tk_recursive As Integer = &HFFFFFFFF
    Private colTypeCodes As Collection

    Private colValues As Collection
    Private colValRepIds As Collection
    Private lChunkValNest As Integer
    Private lChunkBufEof As Integer
    Private lChunkBufPos As Integer

    'CopyRect();
    'Private Declare Function dllCopy16B Lib "user32" Alias "CopyRect" _
    ''    (ByRef lpDestRect As Any, ByRef lpSourceRect As Any) As Long

    'RtlMoveMemory();
    'UPGRADE_ISSUE: Declaring a parameter 'As Any' is not supported. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="FAE78A8D-8978-4FD4-8208-5B7324A8F795"'
    'UPGRADE_ISSUE: Declaring a parameter 'As Any' is not supported. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="FAE78A8D-8978-4FD4-8208-5B7324A8F795"'
    'Private Declare Sub dllMoveMem Lib "kernel32" Alias "RtlMoveMemory" (ByRef hpvDest As Any, ByRef hpvSource As Any, ByVal cbCopy As Integer)
    Private Declare Sub dllMoveMemory Lib "kernel32" Alias "RtlMoveMemory" (ByVal hpvDest As Integer, ByVal hpvSource As Integer, ByVal cbCopy As Integer)
    Private Declare Auto Sub dllMoveMemory Lib "Kernel32.dll" _
    Alias "RtlMoveMemory" (ByVal dest As IntPtr, ByVal src As IntPtr, ByVal size As Integer)

    'lstrcpyA();
    'Private Declare Function dllStrCpy Lib "kernel32" Alias "lstrcpyA" _
    ''    (ByRef lpString1 As Any, ByVal lpString2 As String) As Long

    'UPGRADE_NOTE: Class_Initialize was upgraded to Class_Initialize_Renamed. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="A9E4979A-37FA-4718-9994-97DD76ED70A7"'
    Private Sub Class_Initialize_Renamed()
#If DebugMode Then
		'UPGRADE_NOTE: #If #EndIf block was not upgraded because the expression DebugMode did not evaluate to True or was not evaluated. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="27EE2C3C-05AF-4C04-B2AF-657B4FB6B5FC"'
		lClassDebugID = mVBOrb.getNextClassDebugID()
		Debug.Print "'" & TypeName(Me) & "' " & lClassDebugID & " initialized"
#End If
    End Sub
    Public Sub New()
        MyBase.New()
        Class_Initialize_Renamed()
    End Sub

    'UPGRADE_NOTE: Class_Terminate was upgraded to Class_Terminate_Renamed. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="A9E4979A-37FA-4718-9994-97DD76ED70A7"'
    Private Sub Class_Terminate_Renamed()
        'Release something which VB cannot know if required
#If DebugMode Then
		'UPGRADE_NOTE: #If #EndIf block was not upgraded because the expression DebugMode did not evaluate to True or was not evaluated. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="27EE2C3C-05AF-4C04-B2AF-657B4FB6B5FC"'
		Debug.Print "'" & TypeName(Me) & "' " & CStr(lClassDebugID) & " terminated"
#End If
    End Sub
    Protected Overrides Sub Finalize()
        Class_Terminate_Renamed()
        MyBase.Finalize()
    End Sub

    Public Function VarPtr(ByVal e As Object) As Integer
        Dim GC As GCHandle = GCHandle.Alloc(e, GCHandleType.Pinned)
        Dim GC2 As Integer = GC.AddrOfPinnedObject.ToInt32
        GC.Free()
        Return GC2
    End Function


#If DebugMode Then
	'UPGRADE_NOTE: #If #EndIf block was not upgraded because the expression DebugMode did not evaluate to True or was not evaluated. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="27EE2C3C-05AF-4C04-B2AF-657B4FB6B5FC"'
	Friend Property Get ClassDebugID() As Long
	ClassDebugID = lClassDebugID
	End Property
#End If

    'Initialization
    'To init by octets (initByOctets) do following:
    ' - Call oIn.initStream(oOrb, &H100, seqLen)
    ' - Call oIn.writeOctets(seqArr, seqLen)
    ' - Call oIn.setPos(0)
    'IN:    TCS     Transmission Code Set, 0 = unknown (results in
    '               BAD_PARAM if reading wstring, INV_OBJREF if writing wstring)
    Public Sub initStream(ByVal Orb As cOrbImpl, ByVal GIOPVersion As Short, Optional ByVal lSize As Integer = 1024, Optional ByVal TCS_C As Integer = 0, Optional ByVal TCS_W As Integer = 0)
        On Error GoTo ErrHandler
        If isInitialized() Then
            Call mVBOrb.VBOrb.raiseBADINVORDER(1, mVBOrb.VBOrb.CompletedNO)
        End If
        oOrb = Orb
        iLogLevel = oOrb.getLogLevel()
        If lSize <= 0 Then lSize = 1
        ReDim buffer(lSize - 1)
        lTCS_C = TCS_C
        lTCS_W = TCS_W
        bufOff = 0
        bufEof = bufOff 'Buffer is empty
        bufPos = 0
        bLittleEndian = False
        encapCnt = 0
        ReDim encaps(4)
        iGIOPVersion = GIOPVersion
        bMoreFrags = False
        bIsComplete = True
        Exit Sub
ErrHandler:
        'UPGRADE_NOTE: Object oOrb may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
        oOrb = Nothing
        Call mVBOrb.ErrReraise("initStream")
    End Sub

    'Is initialized?
    Public Function isInitialized() As Boolean
        isInitialized = Not oOrb Is Nothing
    End Function

    'Close OrbStream to reinit it next time
    Public Sub destroy()
        If isInitialized() Then
            lChunkValNest = 0
            'UPGRADE_NOTE: Object colValRepIds may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
            colValRepIds = Nothing
            'UPGRADE_NOTE: Object colValues may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
            colValues = Nothing
            'UPGRADE_NOTE: Object colTypeCodes may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
            colTypeCodes = Nothing
            Erase buffer
            Erase encaps
            'UPGRADE_NOTE: Object oOrb may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
            oOrb = Nothing
            iLogLevel = 0
        End If
    End Sub

    'Get the transmission code sets
    Public Function getTCSC() As Integer
        getTCSC = lTCS_C
    End Function

    Public Function getTCSW() As Integer
        getTCSW = lTCS_W
    End Function

    'Used by colocation
    Public Sub sendGIOPToReadAgain()
        'Allow new Value tracking
        'UPGRADE_NOTE: Object colValRepIds may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
        colValRepIds = Nothing
        'UPGRADE_NOTE: Object colValues may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
        colValues = Nothing
        'UPGRADE_NOTE: Object colTypeCodes may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
        colTypeCodes = Nothing

        Call setPos(12) 'Skip GIOP header
    End Sub

    'Prepare outgoing GIOP message
    Public Sub writeGIOPHead()
        On Error GoTo ErrHandler
        Call writeEnsure(12)
        buffer(bufPos) = Asc("G") : bufPos = bufPos + 1
        buffer(bufPos) = Asc("I") : bufPos = bufPos + 1
        buffer(bufPos) = Asc("O") : bufPos = bufPos + 1
        buffer(bufPos) = Asc("P") : bufPos = bufPos + 1
        Call writeOctet(iGIOPVersion \ &H100) 'GIOPVerMajor
        Call writeOctet(iGIOPVersion And &HFF) 'GIOPVerMinor
        Call writeSkip(6)
        Exit Sub
ErrHandler:
        Call mVBOrb.ErrReraise("writeGIOPHead")
    End Sub

    'Send outgoing GIOP message
    Public Sub sendGIOPToSocket(ByVal msgType As Byte, ByVal oSock As cOrbSocket)
        On Error GoTo ErrHandler
        Call setPos(6)
        Dim bb As Byte
        If iGIOPVersion = &H100 Then
            Call writeBoolean(bLittleEndian)
        Else
            bb = 0
            If bLittleEndian Then
                bb = bb + 1
            End If
            If bMoreFrags Then
                bb = bb + 2
            End If
            Call writeOctet(bb)
        End If
        Call writeOctet(msgType)
        Call writeUlong(bufEof - 12)
        If (iLogLevel And &H50) <> 0 Then
            Call oOrb.logMsg("D Sending GIOP message to " & oSock.socketHost & ":" & oSock.socketPort)
            If (iLogLevel And &H40) <> 0 Then
                Call dumpBuffer(oOrb.getLogFile())
            End If
        End If
        Call oSock.sendBuffer(buffer, bufEof)
        Exit Sub
ErrLog:
        Call oOrb.logErr("sendGIOP")
        Resume Next
ErrHandler:
        Call mVBOrb.ErrSave()
        If iLogLevel >= 4 Then
            Call oOrb.logMsg("I " & Err.Description)
        End If
        Resume ErrClose
ErrClose:
        On Error GoTo ErrLog
        'Close connection to get a new connection next time
        Call oSock.closeSocket()
        On Error GoTo 0
        Call mVBOrb.ErrLoad()
        Call mVBOrb.ErrReraise("sendGIOP")
    End Sub

    'Convert contents of an OrbStream in an IOR string
    Public Sub sendToIOR(ByRef sIOR As String)
        On Error GoTo ErrHandler
        sIOR = "IOR:"
        Call setPos(0)
        For bufPos = 0 To bufEof - 1
            If buffer(bufPos) <= &HF Then
                sIOR = sIOR & "0"
            End If
            sIOR = sIOR & Hex(buffer(bufPos))
        Next bufPos
        Exit Sub
ErrHandler:
        Call mVBOrb.ErrReraise("sendIOR")
    End Sub

    'Convert an IOR string in an OrbStream
    Public Sub recvFromIOR(ByRef sIOR As String)
        On Error GoTo ErrHandler
        bufOff = 0
        bufEof = 0
        bufPos = 0
        Call writeEnsure((Len(sIOR) - 3) \ 2)
        'Convert string into sequence of octets
        For bufPos = 0 To bufEof - 1
            buffer(bufPos) = Val("&H" & Mid(sIOR, bufPos * 2 + 5, 2))
        Next bufPos
        bufPos = 0
        Exit Sub
ErrHandler:
        Call mVBOrb.ErrReraise("recvIOR")
    End Sub

    'Receive incoming GIOP message, read the header and receive the first fragment
    'RET:    0 = GIOP Request, 3 = Locate Request, ...
    Public Function recvGIOPFromSocket(ByVal oSock As cOrbSocket) As Short
        On Error GoTo ErrHandler
        If (iLogLevel And &H50) <> 0 Then
            Call oOrb.logMsg("D Receiving GIOP message from " & oSock.socketHost & ":" & oSock.socketPort)
        End If
        Dim recvLen As Integer
        recvLen = oSock.recvCurrentLen()
        If recvLen = 0 Then
            recvGIOPFromSocket = 2005 'received len = 0 = closed
            Exit Function
        End If

        bufOff = 0
        bufEof = 0
        bufPos = 0
        Call writeEnsure(12)
        '??? waiting endless, timeout required
        Call oSock.recvBuffer(buffer, bufPos, bufEof)

        If readOctet() <> Asc("G") Or readOctet() <> Asc("I") Or readOctet() <> Asc("O") Or readOctet() <> Asc("P") Then
            Call mVBOrb.VBOrb.raiseMARSHAL(1, mVBOrb.VBOrb.CompletedNO, "GIOP magic is wrong")
        End If
        iGIOPVersion = CShort(readOctet()) * &H100 'GIOPVerMajor
        iGIOPVersion = iGIOPVersion + CShort(readOctet()) 'GIOPVerMinor

        Dim bb As Byte
        If iGIOPVersion = &H100 Then
            bLittleEndian = readBoolean()
            bMoreFrags = False
        ElseIf iGIOPVersion = &H101 Or iGIOPVersion = &H102 Then
            bb = readOctet()
            bLittleEndian = (bb And &H1) <> 0
            bMoreFrags = (bb And &H2) <> 0
        Else
            Call mVBOrb.VBOrb.raiseMARSHAL(1, mVBOrb.VBOrb.CompletedNO, "GIOP version " & Hex(iGIOPVersion) & " is unsupported")
        End If
        bIsComplete = Not bMoreFrags
        recvGIOPFromSocket = CShort(readOctet())

        Dim msgSize As Integer
        msgSize = readUlong()
        'Add first fragment here to the file
        Call writeEnsure(msgSize)
        '??? waiting endless, timeout required
        Call oSock.recvBuffer(buffer, bufPos, bufEof - bufPos)
        If (iLogLevel And &H40) <> 0 Then
            Call dumpBuffer(oOrb.getLogFile())
        End If
        Exit Function
ErrHandler:
        Call mVBOrb.ErrReraise("recvGIOP")
    End Function

    'Incoming request or reply is complete (or last fragment)?
    Public Function isComplete() As Boolean
        isComplete = bIsComplete
    End Function

    'Add a fragment at the end of the file
    Public Function addFragment(ByVal oIn As cOrbStream) As Boolean
        On Error GoTo ErrHandler
        If bLittleEndian <> oIn.littleEndian Then
            Call mVBOrb.VBOrb.raiseMARSHAL(0, mVBOrb.VBOrb.CompletedNO, "Byte order mismatch")
        End If
        Dim lFragLen As Integer
        lFragLen = oIn.Available
        Dim aOcts() As Byte
        Dim lOldPos As Integer
        If lFragLen > 0 Then
            'Read the fragment
            ReDim aOcts(lFragLen - 1)
            Call oIn.readOctets(aOcts, lFragLen)
            'Add the fragment at the end of the file
            lOldPos = bufPos
            bufPos = bufEof 'Ensure relative to the old end
            Call writeEnsure(lFragLen)
            'Copy fragment behind the old end
            'UPGRADE_ISSUE: VarPtr function is not supported. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="367764E5-F3F8-4E43-AC3E-7FE0B5E074E2"'
            Call dllMoveMemory(VarPtr(buffer(bufPos)), VarPtr(aOcts(0)), lFragLen)
            bufPos = lOldPos
        End If
        If oIn.isComplete() Then
            bIsComplete = True
        End If
        addFragment = bIsComplete
        Exit Function
ErrHandler:
        Call mVBOrb.ErrReraise("addFragment")
    End Function

    Public Function getGIOPVersion() As Short
        getGIOPVersion = iGIOPVersion
    End Function

    Public ReadOnly Property littleEndian() As Boolean
        Get
            littleEndian = bLittleEndian
        End Get
    End Property

    Public ReadOnly Property Available() As Integer
        Get
            Available = bufEof - bufPos
            If Available < 0 Then Available = 0
        End Get
    End Property

    Public Function getPos() As Integer
        getPos = bufPos
    End Function

    Public Sub setPos(ByVal newBufPos As Integer)
        On Error GoTo ErrHandler
        If newBufPos < bufOff Or newBufPos > bufEof Then
            Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO, "Invalid newBufPos: " & CStr(newBufPos))
        End If
        bufPos = newBufPos
        Exit Sub
ErrHandler:
        Call mVBOrb.ErrReraise("setPos")
    End Sub

    Public Sub readSkip(ByVal skiplen As Integer)
        On Error GoTo ErrHandler
        If skiplen < 0 Or bufPos + skiplen > bufEof Then
            Call mVBOrb.VBOrb.raiseMARSHAL(0, mVBOrb.VBOrb.CompletedNO, "Unexpected skiplen: " & CStr(skiplen))
        End If
        bufPos = bufPos + skiplen
        Exit Sub
ErrHandler:
        Call mVBOrb.ErrReraise("readSkip")
    End Sub

    'To read an encapsulation
    'Read sequence length first, seqLen= oIn.readUlong()
    'Call readEncapOpen/readEncapClose or call oIn.readSkip(seqLen)
    Public Sub readEncapOpen(ByVal seqLen As Integer)
        On Error GoTo ErrHandler
        If seqLen < 0 Or bufPos + seqLen > bufEof Then
            Call mVBOrb.VBOrb.raiseMARSHAL(1, mVBOrb.VBOrb.CompletedNO, "Unexpected seqlen: " & CStr(seqLen))
        End If
        If encapCnt > UBound(encaps) Then
            ReDim Preserve encaps(encapCnt + 10)
        End If
        encaps(encapCnt).bufEof = bufEof
        encaps(encapCnt).bufOff = bufOff
        encaps(encapCnt).bufPos = bufPos + seqLen
        encaps(encapCnt).bLittleEndian = bLittleEndian
        encapCnt = encapCnt + 1
        bufOff = bufPos
        bufEof = bufOff + seqLen
        bLittleEndian = readBoolean()
        Exit Sub
ErrHandler:
        Call mVBOrb.ErrReraise("readEncapOpen")
    End Sub

    'Skip rest of encapsulation and close it
    Public Sub readEncapClose()
        On Error GoTo ErrHandler
        If encapCnt = 0 Then
            Call mVBOrb.VBOrb.raiseMARSHAL(1, mVBOrb.VBOrb.CompletedNO, "Unexpected EncapClose")
        End If
        encapCnt = encapCnt - 1
        bufEof = encaps(encapCnt).bufEof
        bufOff = encaps(encapCnt).bufOff
        bufPos = encaps(encapCnt).bufPos
        bLittleEndian = encaps(encapCnt).bLittleEndian
        Exit Sub
ErrHandler:
        Call mVBOrb.ErrReraise("readEncapClose")
    End Sub

    Public Function readOctet() As Byte
        On Error GoTo ErrHandler
        readOctet = buffer(bufPos)
        bufPos = bufPos + 1
        Exit Function
ErrHandler:
        Call mVBOrb.ErrReraise("readOctet")
    End Function

    'Used by sequence <octet> reader
    'First ReDim seqArr!
    Public Sub readOctets(ByRef seqArr() As Byte, ByVal seqLen As Integer)
        On Error GoTo ErrHandler
        If seqLen < 0 Or bufPos + seqLen > bufEof Then
            Call mVBOrb.VBOrb.raiseMARSHAL(1, mVBOrb.VBOrb.CompletedNO, "Unexpected seqLen: " & CStr(seqLen))
        End If
        If seqLen = 0 Then
            'Dim seqCnt As Long, seqEnd As Long
            'seqEnd = LBound(seqArr) + seqLen - 1
            'For seqCnt = LBound(seqArr) To seqEnd
            '    seqArr(seqCnt) = buffer(bufPos)
            '    bufPos = bufPos + 1
            'Next seqCnt
        Else
            If UBound(seqArr) - LBound(seqArr) + 1 < seqLen Then
                Call mVBOrb.VBOrb.raiseMARSHAL(1, mVBOrb.VBOrb.CompletedNO, "seqArr too small, need: " & CStr(seqLen))
            End If
            Call dllMoveMemory(seqArr(LBound(seqArr)), buffer(bufPos), seqLen)
            bufPos = bufPos + seqLen
        End If
        Exit Sub
ErrHandler:
        Call mVBOrb.ErrReraise("readOctets")
    End Sub

    'sequence <octet>
    Public Function readSeqOctet(ByRef seqArr() As Byte) As Integer
        On Error GoTo ErrHandler
        Dim seqLen As Integer
        seqLen = readUlong()
        If seqLen <= 0 Then
            If seqLen < 0 Then
                Call mVBOrb.VBOrb.raiseMARSHAL(1, mVBOrb.VBOrb.CompletedNO, "Unexpected seqLen: " & CStr(seqLen))
            End If
            'Second argument of MidB is always 1 for the first byte
            'UPGRADE_ISSUE: MidB function is not supported. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="367764E5-F3F8-4E43-AC3E-7FE0B5E074E2"'
            'UPGRADE_TODO: Code was upgraded to use System.Text.UnicodeEncoding.Unicode.GetBytes() which may not have the same behavior. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="93DD716C-10E3-41BE-A4A8-3BA40157905B"'
            Debug.Assert(True) ' need to fix mid
            Dim mystr As String '= Mid(buffer, 1 + bufPos, seqLen))
            seqArr = System.Text.UnicodeEncoding.Unicode.GetBytes(mystr)
            'bufPos = bufPos + seqLen
        Else
            ReDim seqArr(seqLen - 1)
            Call readOctets(seqArr, seqLen)
        End If
        readSeqOctet = seqLen
        Exit Function
ErrHandler:
        Call mVBOrb.ErrReraise("readSeqOctet")
    End Function

    'read_any()
    Public Function readAny() As cOrbAny
        On Error GoTo ErrHandler
        Dim oTC As cOrbTypeCode
        oTC = readTypeCode()
        Dim oAny As cOrbAny
        oAny = New cOrbAny
        Call oAny.initByReadValue(oTC, Me)
        readAny = oAny
        Exit Function
ErrHandler:
        Call mVBOrb.ErrReraise("readAny")
    End Function

    Public Function readBoolean() As Boolean
        On Error GoTo ErrHandler
        Dim bb As Byte
        bb = buffer(bufPos)
        bufPos = bufPos + 1

        If bb = 1 Then
            readBoolean = True
        ElseIf bb = 0 Then
            readBoolean = False
        Else
            Call mVBOrb.VBOrb.raiseMARSHAL(1, mVBOrb.VBOrb.CompletedNO, "Unexpected value: " & CStr(bb) & ", bufPos= " & CStr(bufPos))
        End If
        Exit Function
ErrHandler:
        Call mVBOrb.ErrReraise("readBoolean")
    End Function

    Public Function readChar() As Byte
        On Error GoTo ErrHandler
        readChar = buffer(bufPos)
        bufPos = bufPos + 1
        Exit Function
ErrHandler:
        Call mVBOrb.ErrReraise("readChar")
    End Function

    'Used by sequence <char> reader
    'First ReDim seqArr!
    Public Sub readChars(ByRef seqArr() As Byte, ByVal seqLen As Integer)
        On Error GoTo ErrHandler
        If seqLen < 0 Or bufPos + seqLen > bufEof Then
            Call mVBOrb.VBOrb.raiseMARSHAL(1, mVBOrb.VBOrb.CompletedNO, "Unexpected seqLen: " & CStr(seqLen))
        End If
        If seqLen = 0 Then
            'Dim seqCnt As Long, seqEnd As Long
            'seqEnd = LBound(seqArr) + seqLen - 1
            'For seqCnt = LBound(seqArr) To seqEnd
            '    seqArr(seqCnt) = buffer(bufPos)
            '    bufPos = bufPos + 1
            'Next seqCnt
        Else
            If UBound(seqArr) - LBound(seqArr) + 1 < seqLen Then
                Call mVBOrb.VBOrb.raiseMARSHAL(1, mVBOrb.VBOrb.CompletedNO, "seqArr too small, need: " & CStr(seqLen))
            End If
            Call dllMoveMemory(seqArr(LBound(seqArr)), buffer(bufPos), seqLen)
            bufPos = bufPos + seqLen
        End If
        Exit Sub
ErrHandler:
        Call mVBOrb.ErrReraise("readChars")
    End Sub

    Public Function readWchar() As Short
        If iGIOPVersion < &H102 Then
            readWchar = readShort()
            Exit Function
        End If
        Dim bLen As Byte
        bLen = buffer(bufPos)
        bufPos = bufPos + 1
        Dim bWcharLittleEndian As Boolean
        bWcharLittleEndian = False
        Dim b0 As Byte
        Dim b1 As Byte
        If bLen >= 2 Then
            b0 = buffer(bufPos)
            b1 = buffer(bufPos + 1)
            'BOM (byte order marker)?
            If b0 = &HFE And b1 = &HFF Then
                bufPos = bufPos + 2
                bLen = bLen - 2
            ElseIf b0 = &HFF And b1 = &HFE Then
                bWcharLittleEndian = True
                bufPos = bufPos + 2
                bLen = bLen - 2
            End If
        End If
        If bWcharLittleEndian Then
            b0 = buffer(bufPos)
            If bLen > 1 Then
                b1 = buffer(bufPos + 1)
            Else
                b1 = 0
            End If
        Else
            b1 = buffer(bufPos)
            If bLen > 1 Then
                b0 = buffer(bufPos + 1)
            Else
                b0 = 0
            End If
        End If
        bufPos = bufPos + bLen
        If b1 >= &H80 Then
            readWchar = (CShort(b1 Xor &HFF) * &H100 + CShort(b0)) Xor &HFF00
        Else
            readWchar = CShort(b1) * &H100 + CShort(b0)
        End If
    End Function

    Public Function readShort() As Short
        On Error GoTo ErrHandler
        'Alignment
        If (bufPos - bufOff) Mod 2 > 0 Then
            bufPos = bufPos + 1
        End If

        If bLittleEndian Then
            If buffer(bufPos + 1) >= &H80 Then
                readShort = (CShort(buffer(bufPos + 1) Xor &HFF) * &H100 + CShort(buffer(bufPos))) Xor &HFF00
            Else
                readShort = CShort(buffer(bufPos + 1)) * &H100 + CShort(buffer(bufPos))
            End If
        Else
            If buffer(bufPos) >= &H80 Then
                readShort = (CShort(buffer(bufPos) Xor &HFF) * &H100 + CShort(buffer(bufPos + 1))) Xor &HFF00
            Else
                readShort = CShort(buffer(bufPos)) * &H100 + CShort(buffer(bufPos + 1))
            End If
        End If
        bufPos = bufPos + 2
        Exit Function
ErrHandler:
        Call mVBOrb.ErrReraise("readShort")
    End Function

    Public Function readUshort() As Short
        readUshort = readShort()
    End Function

    Public Function readLong() As Integer
        On Error GoTo ErrHandler
        'Alignment
        If (bufPos - bufOff) Mod 4 > 0 Then
            bufPos = bufPos + 4 - ((bufPos - bufOff) Mod 4)
        End If

        If bLittleEndian Then
            'Call dllMoveMemory(VarPtr(lVal), VarPtr(buffer(bufPos)), 4)
            If buffer(bufPos + 3) >= &H80 Then
                readLong = ((((CInt(buffer(bufPos + 3) Xor &HFF) * &H100 + CInt(buffer(bufPos + 2))) * &H100 + CInt(buffer(bufPos + 1))) * &H100 + CInt(buffer(bufPos)))) Xor &HFF000000
            Else
                readLong = (((CInt(buffer(bufPos + 3)) * &H100 + CInt(buffer(bufPos + 2))) * &H100 + CInt(buffer(bufPos + 1))) * &H100 + CInt(buffer(bufPos)))
            End If
        Else
            If buffer(bufPos) >= &H80 Then
                readLong = ((((CInt(buffer(bufPos) Xor &HFF) * &H100 + CInt(buffer(bufPos + 1))) * &H100 + CInt(buffer(bufPos + 2))) * &H100 + CInt(buffer(bufPos + 3)))) Xor &HFF000000
            Else
                readLong = (((CInt(buffer(bufPos)) * &H100 + CInt(buffer(bufPos + 1))) * &H100 + CInt(buffer(bufPos + 2))) * &H100 + CInt(buffer(bufPos + 3)))
            End If
        End If
        bufPos = bufPos + 4
        Exit Function
ErrHandler:
        Call mVBOrb.ErrReraise("readLong")
    End Function

    Public Function readUlong() As Integer
        readUlong = readLong()
    End Function

    Public Function readLonglong() As Object
        'readLonglong = readUlonglong()
        'If readLonglong >= CDec("9223372036854775808") Then
        '    readLonglong = readLonglong - CDec("18446744073709551616")
        'End If
        'Exit Function
        'Alignment
        If (bufPos - bufOff) Mod 8 > 0 Then
            bufPos = bufPos + 8 - ((bufPos - bufOff) Mod 8)
        End If
        Dim l2p32 As Object
        'UPGRADE_WARNING: Couldn't resolve default property of object l2p32. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6A50421D-15FE-4896-8A1B-2EC21E9037B2"'
        l2p32 = CDec("4294967296")
        Dim l0 As Integer
        Dim l1 As Integer
        l0 = readLong()
        l1 = readLong()
        If bLittleEndian Then
            If l1 < 0 Then
                'UPGRADE_WARNING: Couldn't resolve default property of object l2p32. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6A50421D-15FE-4896-8A1B-2EC21E9037B2"'
                'UPGRADE_WARNING: Couldn't resolve default property of object readLonglong. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6A50421D-15FE-4896-8A1B-2EC21E9037B2"'
                readLonglong = CDec(l1 + 1) * l2p32 + IIf(l0 < 0, CDec(l0), CDec(l0) - l2p32)
            Else
                'UPGRADE_WARNING: Couldn't resolve default property of object l2p32. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6A50421D-15FE-4896-8A1B-2EC21E9037B2"'
                'UPGRADE_WARNING: Couldn't resolve default property of object readLonglong. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6A50421D-15FE-4896-8A1B-2EC21E9037B2"'
                readLonglong = CDec(l1) * l2p32 + IIf(l0 < 0, CDec(l0) + l2p32, CDec(l0))
            End If
        Else
            If l0 < 0 Then
                'UPGRADE_WARNING: Couldn't resolve default property of object l2p32. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6A50421D-15FE-4896-8A1B-2EC21E9037B2"'
                'UPGRADE_WARNING: Couldn't resolve default property of object readLonglong. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6A50421D-15FE-4896-8A1B-2EC21E9037B2"'
                readLonglong = CDec(l0 + 1) * l2p32 + IIf(l1 < 0, CDec(l1), CDec(l1) - l2p32)
            Else
                'UPGRADE_WARNING: Couldn't resolve default property of object l2p32. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6A50421D-15FE-4896-8A1B-2EC21E9037B2"'
                'UPGRADE_WARNING: Couldn't resolve default property of object readLonglong. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6A50421D-15FE-4896-8A1B-2EC21E9037B2"'
                readLonglong = CDec(l0) * l2p32 + IIf(l1 < 0, CDec(l1) + l2p32, CDec(l1))
            End If
        End If
    End Function

    Public Function readUlonglong() As Object
        'Alignment
        If (bufPos - bufOff) Mod 8 > 0 Then
            bufPos = bufPos + 8 - ((bufPos - bufOff) Mod 8)
        End If
        Dim l2p32 As Object
        'UPGRADE_WARNING: Couldn't resolve default property of object l2p32. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6A50421D-15FE-4896-8A1B-2EC21E9037B2"'
        l2p32 = CDec("4294967296")
        Dim l0 As Integer
        Dim l1 As Integer
        l0 = readLong()
        l1 = readLong()
        If bLittleEndian Then
            'UPGRADE_WARNING: Couldn't resolve default property of object l2p32. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6A50421D-15FE-4896-8A1B-2EC21E9037B2"'
            'UPGRADE_WARNING: Couldn't resolve default property of object readUlonglong. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6A50421D-15FE-4896-8A1B-2EC21E9037B2"'
            readUlonglong = IIf(l1 < 0, CDec(l1) + l2p32, CDec(l1)) * l2p32 + IIf(l0 < 0, CDec(l0) + l2p32, CDec(l0))
        Else
            'UPGRADE_WARNING: Couldn't resolve default property of object l2p32. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6A50421D-15FE-4896-8A1B-2EC21E9037B2"'
            'UPGRADE_WARNING: Couldn't resolve default property of object readUlonglong. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6A50421D-15FE-4896-8A1B-2EC21E9037B2"'
            readUlonglong = IIf(l0 < 0, CDec(l0) + l2p32, CDec(l0)) * l2p32 + IIf(l1 < 0, CDec(l1) + l2p32, CDec(l1))
        End If
    End Function

    'First version was written by Holger Beer
    Public Function readFloat() As Single
        On Error GoTo ErrHandler
        Dim i As Integer
        Dim bytes(3) As Byte

        'Alignment
        If (bufPos - bufOff) Mod 4 > 0 Then
            bufPos = bufPos + 4 - ((bufPos - bufOff) Mod 4)
        End If

        If bLittleEndian Then
            For i = 0 To 3
                bytes(i) = buffer(bufPos + i)
            Next
        Else
            For i = 0 To 3
                bytes(i) = buffer(bufPos + 3 - i)
            Next
        End If
        Dim mysingle As Single = readFloat()
        Debug.Assert(True) ' need to check address logic )
        Call dllMoveMemory(VarPtr(mysingle), bytes(0), 4)
        bufPos = bufPos + 4
        Exit Function
ErrHandler:
        Call mVBOrb.ErrReraise("readFloat")
    End Function

    'First version was written by Holger Beer
    Public Function readDouble() As Double
        On Error GoTo ErrHandler
        Dim i As Integer
        Dim bytes(7) As Byte

        'Alignment
        If (bufPos - bufOff) Mod 8 > 0 Then
            bufPos = bufPos + 8 - ((bufPos - bufOff) Mod 8)
        End If

        If bLittleEndian Then
            For i = 0 To 7
                bytes(i) = buffer(bufPos + i)
            Next
        Else
            For i = 0 To 7
                bytes(i) = buffer(bufPos + 7 - i)
            Next
        End If
        Dim mydouble As Double = readDouble

        Call dllMoveMemory(VarPtr(readDouble), bytes(0), 8)
        bufPos = bufPos + 8
        Exit Function
ErrHandler:
        Call mVBOrb.ErrReraise("readDouble")
    End Function

    'read_longdouble()
    Public Function readLongdouble() As cOrblongdouble
        On Error GoTo ErrHandler
        Dim i As Integer
        Dim bytes(15) As Byte

        'Alignment
        If (bufPos - bufOff) Mod 8 > 0 Then
            bufPos = bufPos + 8 - ((bufPos - bufOff) Mod 8)
        End If

        If bLittleEndian Then
            For i = 0 To 15
                bytes(i) = buffer(bufPos + i)
            Next
        Else
            For i = 0 To 15
                bytes(i) = buffer(bufPos + 15 - i)
            Next
        End If
        readLongdouble = New cOrblongdouble
        Call readLongdouble.setBytes(bytes)
        bufPos = bufPos + 16
        Exit Function
ErrHandler:
        'UPGRADE_NOTE: Object readLongdouble may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
        readLongdouble = Nothing
        Call mVBOrb.ErrReraise("readLongdouble")
    End Function

    '???(VBOrb does not convert code set info in lTCS_C to native code set mVBOrb.ONCSC)
    'If a server’s native char code set is not specified in the IOR multi-component profile,
    'then it is considered to be ISO 8859-1 for backward compatibility.
    Public Function readString(Optional ByVal maxLen As Integer = 0) As String
        On Error GoTo ErrHandler
        Dim lStrLen As Integer
        lStrLen = readUlong()
        If lStrLen <= 0 Then
            Call mVBOrb.VBOrb.raiseMARSHAL(0, mVBOrb.VBOrb.CompletedNO, "Illegal string length: " & CStr(lStrLen))
        End If
        lStrLen = lStrLen - 1 'Real string length
        If lStrLen >= bufEof - bufPos Then
            Call mVBOrb.VBOrb.raiseMARSHAL(0, mVBOrb.VBOrb.CompletedNO, "Illegal string length: " & CStr(lStrLen) & " >= " & CStr(Available))
        End If
        If maxLen <> 0 And lStrLen > maxLen Then
            Call mVBOrb.VBOrb.raiseMARSHAL(0, mVBOrb.VBOrb.CompletedNO, "Out of bounds: " & CStr(lStrLen) & " > " & CStr(maxLen))
        End If
        'buffer(bufpos + lStrLen) is always equals Chr$(0)
        Dim baStr() As Byte
        If lStrLen = 0 Then
            readString = ""
        Else
#If firstReadString Then
			'UPGRADE_NOTE: #If #EndIf block was not upgraded because the expression firstReadString did not evaluate to True or was not evaluated. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="27EE2C3C-05AF-4C04-B2AF-657B4FB6B5FC"'
			'Second argument of MidB is always 1 for the first byte
			readString = StrConv(MidB(buffer, 1 + bufPos, lStrLen), vbUnicode)
#ElseIf secondReadString Then 'by Henny van Esch:
			'UPGRADE_NOTE: #If #EndIf block was not upgraded because the expression secondReadString did not evaluate to True or was not evaluated. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="27EE2C3C-05AF-4C04-B2AF-657B4FB6B5FC"'
			Dim bi As Long
			Dim buf As String
			buf = "" 'Slow for long and fast for short strings:
			For bi = bufPos To bufPos + lStrLen - 1
			buf = buf & Chr$(buffer(bi))
			Next bi
			readString = buf
#Else 'Very fast for all string lengths:
            ReDim baStr(lStrLen - 1)
            'UPGRADE_ISSUE: VarPtr function is not supported. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="367764E5-F3F8-4E43-AC3E-7FE0B5E074E2"'
            Call dllMoveMemory(VarPtr(baStr(0)), VarPtr(buffer(bufPos)), lStrLen)
            'UPGRADE_ISSUE: Constant vbUnicode was not upgraded. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="55B59875-9A95-4B71-9D6A-7C294BF7139D"'
            readString = (System.Text.UnicodeEncoding.Unicode.GetString(baStr))
            Erase baStr
#End If
        End If
        bufPos = bufPos + lStrLen + 1
        Exit Function
ErrHandler:
        Call mVBOrb.ErrReraise("readString")
    End Function

    '???(VBOrb does not convert code set info in lTCS_W to native code set mVBOrb.ONCSW)
    'If no char transmission code set is specified in the code set service context,
    'then the server-side ORB raises exception BAD_PARAM.
    'First version was written by Kalinine Iwan (ICQ: Iwanture)
    Public Function readWString(Optional ByVal maxLen As Integer = 0) As String
        On Error GoTo ErrHandler
        'For GIOP version < 1.2 length of string sended in characters plus null terminator
        Dim oldGIOP As Boolean
        oldGIOP = iGIOPVersion < &H102
        If oOrb.VisiWorkaround Then
            oldGIOP = True
            'ElseIf lTCS_W = 0 Then???
            '    Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO, _
            ''        "Missing transmission code set in service context")
        End If
        'Read number of octets of wstring
        Dim lNOcts As Integer
        lNOcts = readUlong()
        If oldGIOP Then lNOcts = (lNOcts - 1) * 2
        If lNOcts < 0 Or (lNOcts And 1) = 1 Then
            Call mVBOrb.VBOrb.raiseMARSHAL(1, mVBOrb.VBOrb.CompletedNO, "Unexpected wstring value: lNOcts= " & CStr(lNOcts))
        ElseIf bufPos + lNOcts > bufEof Then
            Call mVBOrb.VBOrb.raiseMARSHAL(1, mVBOrb.VBOrb.CompletedNO, "Illegal wstring length: " & CStr(lNOcts) & " > " & CStr(Available))
        End If

        'BOM (byte order marker)?
        Dim bWcharLittleEndian As Boolean
        bWcharLittleEndian = False
        Dim b0 As Byte
        Dim b1 As Byte
        If lNOcts >= 2 Then
            b0 = buffer(bufPos)
            b1 = buffer(bufPos + 1)
            If b0 = &HFE And b1 = &HFF Then
                bufPos = bufPos + 2
                lNOcts = lNOcts - 2
            ElseIf b0 = &HFF And b1 = &HFE Then
                bWcharLittleEndian = True
                bufPos = bufPos + 2
                lNOcts = lNOcts - 2
            End If
        End If

        Dim sStr As String
        'Unicode string, len of wchar is 2
        sStr = Space(lNOcts \ 2) 'Preserve space
        Dim i As Integer
        Dim baStr() As Byte
        If lNOcts > 0 Then 'baStr(1) does not work if lNOcts = 0!
            If bWcharLittleEndian Then
                'LOW byte comes first
                'UPGRADE_ISSUE: StrPtr function is not supported. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="367764E5-F3F8-4E43-AC3E-7FE0B5E074E2"'
                Call dllMoveMemory(VarPtr(sStr), buffer(bufPos), lNOcts)
            Else
                ReDim baStr(lNOcts)
                'HI byte comes first - so we must swap bytes
                'Copy wstring to baStr(1 To lNOcts)
                Call dllMoveMemory(baStr(1), buffer(bufPos), lNOcts)
                'Swap bytes to baStr(0 To lNOcts - 1)
                For i = 0 To lNOcts - 2 Step 2
                    baStr(i) = baStr(i + 2)
                Next i
                'Copy baStr(0 To lNOcts - 1)
                'UPGRADE_ISSUE: StrPtr function is not supported. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="367764E5-F3F8-4E43-AC3E-7FE0B5E074E2"'
                Debug.Assert(True) ' need to cj3ck vb.net fuctinality
                Call dllMoveMemory(VarPtr(sStr), baStr(0), lNOcts)
                Erase baStr
            End If
        End If
        readWString = sStr
        bufPos = bufPos + lNOcts
        If oldGIOP Then
            If readShort() <> 0 Then
                Call mVBOrb.VBOrb.raiseMARSHAL(0, mVBOrb.VBOrb.CompletedNO, "Terminating null character missing")
            End If
        End If
        Exit Function
ErrHandler:
        Call mVBOrb.ErrReraise("readWString")
    End Function

    'read_Object()
    Public Function readObject() As _cOrbObject
        On Error GoTo ErrHandler
        Dim oObjRef As cOrbObjRef
        oObjRef = New cOrbObjRef
        If oObjRef.initByIOR(oOrb, Me) Then
            'UPGRADE_NOTE: Object oObjRef may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
            oObjRef = Nothing
        End If
        readObject = oObjRef
        Exit Function
ErrHandler:
        Call mVBOrb.ErrReraise("readObject")
    End Function

    'read_Value()
    'IN:    oValueBase  An uninitialized value or Nothing (Nothing means:
    '                   the ORB must use a previously registered ValueFactory)
    'RET:   oValue      Null value (= Nothing) or an initialized value
    Public Function readValue(ByVal oValueBase As _cOrbValueBase) As _cOrbValueBase
        On Error GoTo ErrHandler
        Dim lValPos As Integer
        Dim lIndirection As Integer
        Dim lVal2Pos As Integer
        '<value>
        Dim lValueTag As Integer
        Dim nextPos As Integer
        If lChunkValNest > 0 Then
            'Alignment
            If (bufPos - bufOff) Mod 4 > 0 Then
                nextPos = bufPos + 4 - ((bufPos - bufOff) Mod 4)
            Else
                nextPos = bufPos
            End If
            If nextPos + 4 > bufEof Then
                Call readChunkEnd() 'End of chunk if no previous value
            End If
        End If
        lValueTag = readLong()
#If DebugMode Then
		'UPGRADE_NOTE: #If #EndIf block was not upgraded because the expression DebugMode did not evaluate to True or was not evaluated. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="27EE2C3C-05AF-4C04-B2AF-657B4FB6B5FC"'
		Call oOrb.logMsg(CStr(lChunkValNest) & ": Read <value_tag>: " & Hex(lValueTag))
#End If
        Dim lIndTag As Integer
        Dim oValFactory As _cOrbValueFactory
        Dim sRepId As String
        Dim sExpectedValueId As String
        Dim lRepIDCount As Integer
        Dim lRepIDNo As Integer
        Dim nextTag As Integer 'single [ <type_info> ]
        If (lValueTag And &HFFFFFF00) = &H7FFFFF00 Then '<value_tag>
            lValPos = bufPos - 4
            If (lValueTag And &H1) = &H1 Then '[ <codebase_URL> ]
                'The <codebase_URL> is a blank-separated list of one or more URLs
                lIndTag = readLong()
                If lIndTag = &HFFFFFFFF Then
                    Call readLong() 'Ignore codebase URL
                Else
                    bufPos = bufPos - 4 'Unread <indirection_tag>
                    Call readString() 'Ignore codebase URL
                End If
            End If
            If (lValueTag And &H6) = &H2 Then
                '<repository_id>
                sRepId = readValRepId()
                If oValueBase Is Nothing Then
                    oValFactory = oOrb.lookupValueFactory(sRepId)
                    If oValFactory Is Nothing Then
                        Call mVBOrb.VBOrb.raiseMARSHAL(1, mVBOrb.VBOrb.CompletedNO, "Need a ValueFactory for " & sRepId)
                    End If
                    oValueBase = oValFactory.newUninitValue()
                Else
                    sExpectedValueId = oValueBase.getIds(0)
                    'Check if we must read an extended value instance
                    If Not mVBOrb.TypeIdEquals(sRepId, sExpectedValueId, False) Then
                        oValFactory = oOrb.lookupValueFactory(sRepId)
                        If oValFactory Is Nothing Then
                            Call mVBOrb.VBOrb.raiseMARSHAL(1, mVBOrb.VBOrb.CompletedNO, "Need a ValueFactory for " & sRepId & ". It must be an instance of " & sExpectedValueId)
                        End If
                        oValueBase = oValFactory.newUninitValue()
                        If Not mVBOrb.TypeIdIsInVal(sExpectedValueId, oValueBase, False) Then
                            Call mVBOrb.VBOrb.raiseBADPARAM(1, mVBOrb.VBOrb.CompletedNO, "Cannot assign received " & sRepId & " to expected " & sExpectedValueId)
                        End If
                    End If
                End If
            ElseIf (lValueTag And &H6) = &H6 Then  '[ <type_info> ]
                lRepIDCount = readLong()
                If lRepIDCount = &HFFFFFFFF Then
                    lIndirection = readLong() 'Ignore???
                Else
                    For lRepIDNo = 1 To lRepIDCount
                        '<repository_id>
                        Call readValRepId() 'Ignore???
                    Next lRepIDNo
                End If
            End If
            If oValueBase Is Nothing Then
                'If the ORB is unable to locate and use the appropriate ValueFactory,
                'then a MARSHAL exception with standard minor code 1 is raised.
                'Dim oFactory As cOrbValueFactory
                'Set oFactory = oOrb.lookupValueFactory(sRepositoryId)
                'Set oValueBase = oFactory.newUninitValue()
                Call mVBOrb.VBOrb.raiseMARSHAL(1, mVBOrb.VBOrb.CompletedNO, IIf((lValueTag And &H6) = &H0, "Missing valuetype info", "No appropriate ValueFactory"))
            End If
            If (lValueTag And &H8) = &H8 Then
                'For example custom valuetypes
                lChunkValNest = lChunkValNest + 1
                nextTag = readLong() 'Len of chunk or end of value tag
                If nextTag < 0 Then
                    bufPos = bufPos - 4
                    Call readChunkStart(0)
                Else
                    Call readChunkStart(nextTag)
                End If
            End If
#If DebugMode Then
			'UPGRADE_NOTE: #If #EndIf block was not upgraded because the expression DebugMode did not evaluate to True or was not evaluated. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="27EE2C3C-05AF-4C04-B2AF-657B4FB6B5FC"'
			Call oOrb.logMsg(CStr(lChunkValNest) & ": Start value state...")
#End If
            'Read value state
            Call oValueBase.initByRead(Me)
#If DebugMode Then
			'UPGRADE_NOTE: #If #EndIf block was not upgraded because the expression DebugMode did not evaluate to True or was not evaluated. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="27EE2C3C-05AF-4C04-B2AF-657B4FB6B5FC"'
			Call oOrb.logMsg(CStr(lChunkValNest) & ": ...value state end")
#End If
            If colValues Is Nothing Then colValues = New Collection
            Call colValues.Add(oValueBase, CStr(lValPos))
            If (lValueTag And &H8) = &H8 Then
                Call readChunkEnd()
                nextTag = readLong() 'Len of next chunk, next value tag or end of value
                If nextTag >= &H7FFFFF00 Then 'Next value tag
                    Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO, "Truncation is unsupported")
                ElseIf nextTag >= 0 Then  'Len of next chunk or use of reserved 0 tag
                    Call mVBOrb.VBOrb.raiseMARSHAL(0, mVBOrb.VBOrb.CompletedNO, "Forgotten to read next chunk or end of value is missing")
                End If
                If nextTag < -lChunkValNest Then
                    Call mVBOrb.VBOrb.raiseMARSHAL(0, mVBOrb.VBOrb.CompletedNO, "Invalid end of value nest " & CStr(-lChunkValNest) & " > " & CStr(nextTag))
                ElseIf nextTag > -lChunkValNest Then
                    bufPos = bufPos - 4 'Value is implicitly ended
                End If
                lChunkValNest = lChunkValNest - 1
            End If
            If lChunkValNest > 0 Then
                nextTag = readLong() 'Len of next chunk or next value tag
                If nextTag > 0 And nextTag < &H7FFFFF00 Then
                    Call readChunkStart(nextTag)
                Else
                    bufPos = bufPos - 4
                End If
            End If
        ElseIf lValueTag = &HFFFFFFFF Then  '<indirection_tag>
            lIndirection = readLong()
            If lIndirection >= -4 Then
                Call mVBOrb.VBOrb.raiseMARSHAL(0, mVBOrb.VBOrb.CompletedNO, "Invalid <indirection> = " & CStr(lIndirection))
            End If
            lVal2Pos = bufPos - 4 + lIndirection
            If (lVal2Pos - bufOff) Mod 4 > 0 Then 'align(4)
                lVal2Pos = lVal2Pos + 4 - ((lVal2Pos - bufOff) Mod 4)
            End If
            On Error Resume Next
            oValueBase = colValues.Item(CStr(lVal2Pos))
            'UPGRADE_NOTE: Object oValueBase may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
            If Err.Number <> 0 Then oValueBase = Nothing
            On Error GoTo ErrHandler
            If oValueBase Is Nothing Then
                Call mVBOrb.VBOrb.raiseMARSHAL(0, mVBOrb.VBOrb.CompletedNO, "Invalid <indirection> nothing found at " & CStr(lVal2Pos))
            End If
            'Call colValues.Add(oValueBase, CStr(bufPos - 8&))'lValPos = bufPos - 8&
        ElseIf lValueTag = 0 Then  '<null_tag>
            'UPGRADE_NOTE: Object oValueBase may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
            oValueBase = Nothing
        Else
            Call mVBOrb.VBOrb.raiseMARSHAL(0, mVBOrb.VBOrb.CompletedNO, "Invalid <value_tag> = 0x" & Hex(lValueTag))
        End If
        readValue = oValueBase
        Exit Function
ErrHandler:
        Call mVBOrb.ErrReraise("readValue")
    End Function

    'Read <repository_id> of value <type_info>
    Private Function readValRepId() As String
        On Error GoTo ErrHandler
        Dim lValIdPos As Integer
        Dim lIndTag As Integer
        lIndTag = readLong() '<indirection_tag>
        Dim lIndirection As Integer
        If lIndTag = &HFFFFFFFF Then
            lIndirection = readLong()
            If lIndirection >= -4 Then
                Call mVBOrb.VBOrb.raiseMARSHAL(0, mVBOrb.VBOrb.CompletedNO, "Invalid RepId <indirection> = " & CStr(lIndirection))
            End If
            lValIdPos = bufPos - 4 + lIndirection
            If (lValIdPos - bufOff) Mod 4 > 0 Then 'align(4)
                lValIdPos = lValIdPos + 4 - ((lValIdPos - bufOff) Mod 4)
            End If
            On Error Resume Next
            'UPGRADE_WARNING: Couldn't resolve default property of object colValRepIds.Item(). Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6A50421D-15FE-4896-8A1B-2EC21E9037B2"'
            readValRepId = colValRepIds.Item(CStr(lValIdPos))
            If Err.Number <> 0 Then readValRepId = "nnn"
            On Error GoTo ErrHandler
            If readValRepId = "nnn" Then
                Call mVBOrb.VBOrb.raiseMARSHAL(0, mVBOrb.VBOrb.CompletedNO, "Invalid RepId <indirection> nothing found at " & CStr(lValIdPos))
            End If
            'Call colValRepIds.Add(readValRepId, CStr(bufPos - 8&))
        Else
            lValIdPos = bufPos - 4
            bufPos = bufPos - 4 'Unread <indirection_tag>
            readValRepId = readString()
            If colValRepIds Is Nothing Then colValRepIds = New Collection
            Call colValRepIds.Add(readValRepId, CStr(lValIdPos))
        End If
#If DebugMode Then
		'UPGRADE_NOTE: #If #EndIf block was not upgraded because the expression DebugMode did not evaluate to True or was not evaluated. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="27EE2C3C-05AF-4C04-B2AF-657B4FB6B5FC"'
		Call oOrb.logMsg(CStr(lChunkValNest) & ": Read <repository_id>: " _
		            & readValRepId)
#End If
        Exit Function
ErrHandler:
        Call mVBOrb.ErrReraise("readValRepId")
    End Function

    Private Sub readChunkStart(ByVal chunkLen As Integer)
#If DebugMode Then
		'UPGRADE_NOTE: #If #EndIf block was not upgraded because the expression DebugMode did not evaluate to True or was not evaluated. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="27EE2C3C-05AF-4C04-B2AF-657B4FB6B5FC"'
		Call oOrb.logMsg(CStr(lChunkValNest) & ": Chunk start: from " _
		            & CStr(bufPos) & " to " & CStr(bufPos + chunkLen) _
		            & " len= " & CStr(chunkLen))
#End If
        lChunkBufEof = bufEof
        lChunkBufPos = bufPos + chunkLen
        bufEof = lChunkBufPos
    End Sub

    Private Sub readChunkEnd()
        'On Error GoTo ErrHandler
        If bufPos > bufEof Then
            Call mVBOrb.VBOrb.raiseMARSHAL(0, mVBOrb.VBOrb.CompletedNO, "Read " & CStr(bufPos - bufEof) & " bytes over end of chunk")
        End If
        If lChunkBufEof > 0 Then
#If DebugMode Then
			'UPGRADE_NOTE: #If #EndIf block was not upgraded because the expression DebugMode did not evaluate to True or was not evaluated. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="27EE2C3C-05AF-4C04-B2AF-657B4FB6B5FC"'
			Call oOrb.logMsg(CStr(lChunkValNest) & ": Chunk ends: pos= " _
			                & CStr(lChunkBufPos) & " to " & CStr(lChunkBufEof))
#End If
            bufEof = lChunkBufEof
            bufPos = lChunkBufPos
        End If
        lChunkBufEof = 0
        '    Exit Sub
        'ErrHandler:
        '    Call mVBOrb.ErrReraise("readChunkEnd")
    End Sub

    'Abstract interfaces are encoded as a union with a boolean discriminator.
    'RET:   oAbstract   cOrbObjRef or cOrbValueBase
    Public Function readAbstract(ByVal oValueBase As _cOrbValueBase) As _cOrbAbstractBase
        On Error GoTo ErrHandler
        Dim bObjRef As Boolean
        If readBoolean() Then
            readAbstract = readObject()
        Else
            readAbstract = readValue(oValueBase)
        End If
        Exit Function
ErrHandler:
        Call mVBOrb.ErrReraise("readAbstract")
    End Function

    'read_TypeCode()
    Public Function readTypeCode() As cOrbTypeCode
        On Error GoTo ErrHandler
        Dim oTC As cOrbTypeCode
        Dim lTcPos As Integer
        Dim lTcKind As Integer
        Dim sTcId As String
        Dim sTcName As String
        Dim lTcLength As Integer
        Dim oContentType As cOrbTypeCode
        lTcKind = readLong()
        lTcPos = bufPos - 4
        Dim iDigits As Short
        Dim iScale As Short
        Dim lIndirection As Integer
        Dim lTc2Pos As Integer
        Select Case lTcKind
            Case mCB.tk_null, mCB.tk_void, mCB.tk_short, mCB.tk_long, mCB.tk_ushort, mCB.tk_ulong, mCB.tk_float, mCB.tk_double, mCB.tk_boolean, mCB.tk_char, mCB.tk_octet, mCB.tk_any, mCB.tk_TypeCode, mCB.tk_Principal
                oTC = oOrb.createPrimitiveTc(lTcKind)
            Case mCB.tk_objref
                Call readEncapOpen(readUlong()) 'complex type
                sTcId = readString()
                sTcName = readString()
                Call readEncapClose()
                oTC = oOrb.createInterfaceTc(sTcId, sTcName)
            Case mCB.tk_struct, mCB.tk_union, mCB.tk_enum
                Call readEncapOpen(readUlong()) 'complex type
                oTC = New cOrbTypeCode
                Call oTC.initByRead(lTcKind, Me)
                Call readEncapClose()
            Case mCB.tk_string
                lTcLength = readUlong()
                oTC = oOrb.createStringTc(lTcLength)
            Case mCB.tk_sequence
                Call readEncapOpen(readUlong()) 'complex type
                oContentType = readTypeCode()
                lTcLength = readUlong()
                Call readEncapClose()
                oTC = oOrb.createSequenceTc(lTcLength, oContentType)
            Case mCB.tk_array
                Call readEncapOpen(readUlong()) 'complex type
                oContentType = readTypeCode()
                lTcLength = readUlong()
                Call readEncapClose()
                oTC = oOrb.createArrayTc(lTcLength, oContentType)
            Case mCB.tk_alias
                Call readEncapOpen(readUlong()) 'complex type
                sTcId = readString()
                sTcName = readString()
                oContentType = readTypeCode()
                Call readEncapClose()
                oTC = oOrb.createAliasTc(sTcId, sTcName, oContentType)
            Case mCB.tk_except
                Call readEncapOpen(readUlong()) 'complex type
                oTC = New cOrbTypeCode
                Call oTC.initByRead(lTcKind, Me)
                Call readEncapClose()
            Case mCB.tk_longlong, mCB.tk_ulonglong, mCB.tk_longdouble, mCB.tk_wchar
                oTC = oOrb.createPrimitiveTc(lTcKind)
            Case mCB.tk_wstring
                lTcLength = readUlong()
                oTC = oOrb.createWstringTc(lTcLength)
            Case mCB.tk_fixed
                iDigits = readUshort()
                iScale = readShort()
                oTC = oOrb.createFixedTc(iDigits, iScale)
            Case mCB.tk_value
                Call readEncapOpen(readUlong()) 'complex type
                oTC = New cOrbTypeCode
                Call oTC.initByRead(lTcKind, Me)
                Call readEncapClose()
            Case mCB.tk_value_box
                Call readEncapOpen(readUlong()) 'complex type
                sTcId = readString()
                sTcName = readString()
                oContentType = readTypeCode()
                Call readEncapClose()
                oTC = oOrb.createValueBoxTc(sTcId, sTcName, oContentType)
            Case mCB.tk_native
                Call readEncapOpen(readUlong()) 'complex type
                sTcId = readString()
                sTcName = readString()
                Call readEncapClose()
                oTC = oOrb.createNativeTc(sTcId, sTcName)
            Case mCB.tk_abstract_interface
                Call readEncapOpen(readUlong()) 'complex type
                sTcId = readString()
                sTcName = readString()
                Call readEncapClose()
                oTC = oOrb.createAbstractInterfaceTc(sTcId, sTcName)
            Case mCB.tk_local_interface
                Call readEncapOpen(readUlong()) 'complex type
                sTcId = readString()
                sTcName = readString()
                Call readEncapClose()
                oTC = oOrb.createLocalInterfaceTc(sTcId, sTcName)
            Case tk_recursive 'Indirection
                lIndirection = readLong()
                If lIndirection >= -4 Then
                    Call mVBOrb.VBOrb.raiseMARSHAL(0, mVBOrb.VBOrb.CompletedNO, "Invalid <indirection> = " & CStr(lIndirection))
                End If
                lTc2Pos = bufPos - 4 + lIndirection
                If (lTc2Pos - bufOff) Mod 4 > 0 Then 'align(4)
                    lTc2Pos = lTc2Pos + 4 - ((lTc2Pos - bufOff) Mod 4)
                End If
                On Error Resume Next
                oTC = colTypeCodes.Item(CStr(lTc2Pos))
                'UPGRADE_NOTE: Object oTC may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
                If Err.Number <> 0 Then oTC = Nothing
                On Error GoTo ErrHandler
                If oTC Is Nothing Then
                    Call mVBOrb.VBOrb.raiseMARSHAL(0, mVBOrb.VBOrb.CompletedNO, "Invalid <indirection> nothing found at " & CStr(lTc2Pos))
                End If
            Case Else
                Call mVBOrb.VBOrb.raiseMARSHAL(0, mVBOrb.VBOrb.CompletedNO, "lTCKind = " & CStr(lTcKind))
        End Select
        If colTypeCodes Is Nothing Then colTypeCodes = New Collection
        Call colTypeCodes.Add(oTC, CStr(lTcPos))
        readTypeCode = oTC
        Exit Function
ErrHandler:
        Call mVBOrb.ErrReraise("readTypeCode")
    End Function

    'Alignment only, don't checking end of stream condition
    Public Sub readAlign(ByVal algnVal As Integer)
        Dim lRemainder As Integer
        lRemainder = (bufPos - bufOff) Mod algnVal
        If lRemainder > 0 Then
            bufPos = bufPos + (algnVal - lRemainder)
        End If
    End Sub

    'Alignment only
    Public Sub writeAlign(ByVal algnVal As Integer)
        Dim lRemainder As Integer
        lRemainder = (bufPos - bufOff) Mod algnVal
        If lRemainder > 0 Then
            Call writeSkip(algnVal - lRemainder)
        End If
    End Sub

    'Ensure file and buffer size from the current position
    Public Sub writeEnsure(ByVal minSize As Integer)
        Dim lNewEof As Integer 'New file size
        lNewEof = bufPos + minSize
        If lNewEof <= bufEof Then 'Inside the file?
            Exit Sub
        End If
        Dim lBufSize As Integer
        lBufSize = UBound(buffer) + 1
        If lNewEof <= lBufSize Then 'Inside the buffer?
            bufEof = lNewEof
            Exit Sub
        End If
        'Calc new buffer size
        If lBufSize < 128 Then
            lBufSize = 256
        ElseIf lBufSize > 65536 Then
            lBufSize = lBufSize + (lBufSize \ 2)
        Else
            lBufSize = lBufSize + lBufSize
        End If
        If lBufSize < lNewEof Then
            lBufSize = lNewEof
        End If
        If bufEof = 0 Then
            ReDim buffer(lBufSize - 1)
        Else
            ReDim Preserve buffer(lBufSize - 1)
        End If
        bufEof = lNewEof
    End Sub

    Public Sub writeSkip(ByVal skiplen As Integer)
        Call writeEnsure(skiplen)
        bufPos = bufPos + skiplen
    End Sub

    Public Sub writeEncapOpen(ByVal newLittleEndian As Boolean)
        On Error GoTo ErrHandler
        Call writeLong(0) 'Will be overwritten by writeEncapClose()
        If encapCnt > UBound(encaps) Then
            ReDim Preserve encaps(encapCnt + 10)
        End If
        encaps(encapCnt).bufEof = bufEof
        encaps(encapCnt).bufOff = bufOff
        encaps(encapCnt).bufPos = bufPos
        encaps(encapCnt).bLittleEndian = bLittleEndian
        encapCnt = encapCnt + 1
        bufOff = bufPos
        bufEof = bufOff
        bLittleEndian = newLittleEndian
        Call writeBoolean(bLittleEndian)
        Exit Sub
ErrHandler:
        Call mVBOrb.ErrReraise("writeEncapOpen")
    End Sub

    Public Sub writeEncapClose()
        On Error GoTo ErrHandler
        If encapCnt = 0 Then
            Call mVBOrb.VBOrb.raiseMARSHAL(1, mVBOrb.VBOrb.CompletedNO, "Unexpected EncapClose")
        End If
        encapCnt = encapCnt - 1
        Dim seqLen As Integer
        seqLen = bufEof - bufOff 'bufOff = encaps(encapCnt).bufPos
        'bufEof = encaps(encapCnt).bufEof + seqLen
        bufOff = encaps(encapCnt).bufOff
        bufPos = encaps(encapCnt).bufPos - 4
        bLittleEndian = encaps(encapCnt).bLittleEndian
        Call writeLong(seqLen)
        Call writeSkip(seqLen)
        Exit Sub
ErrHandler:
        Call mVBOrb.ErrReraise("writeEncapClose")
    End Sub

    Public Sub writeOctet(ByVal wVal As Byte)
        On Error GoTo ErrHandler
        Call writeEnsure(1)
        buffer(bufPos) = wVal
        bufPos = bufPos + 1
        Exit Sub
ErrHandler:
        Call mVBOrb.ErrReraise("writeOctet")
    End Sub

    'Used by sequence <octet> writer
    Public Sub writeOctets(ByRef seqArr() As Byte, ByVal seqLen As Integer)
        On Error GoTo ErrHandler
        Call writeEnsure(seqLen)
        If seqLen <= 0 Then
            If seqLen < 0 Then
                Call mVBOrb.VBOrb.raiseMARSHAL(1, mVBOrb.VBOrb.CompletedNO, "Unexpected seqLen: " & CStr(seqLen))
            End If
            'Dim seqCnt As Long, seqEnd As Long
            'seqEnd = LBound(seqArr) + seqLen - 1
            'For seqCnt = LBound(seqArr) To seqEnd
            '    buffer(bufPos) = seqArr(seqCnt)
            '    bufPos = bufPos + 1
            'Next seqCnt
        Else
            If UBound(seqArr) - LBound(seqArr) + 1 < seqLen Then
                Call mVBOrb.VBOrb.raiseMARSHAL(1, mVBOrb.VBOrb.CompletedNO, "seqArr too small, need: " & CStr(seqLen))
            End If
            Call dllMoveMemory(buffer(bufPos), seqArr(LBound(seqArr)), seqLen)
            bufPos = bufPos + seqLen
        End If
        Exit Sub
ErrHandler:
        Call mVBOrb.ErrReraise("writeOctets")
    End Sub

    'sequence <octet>
    Public Sub writeSeqOctet(ByRef seqArr() As Byte, ByVal seqLen As Integer)
        On Error GoTo ErrHandler
        Call writeUlong(seqLen)
        Call writeOctets(seqArr, seqLen)
        Exit Sub
ErrHandler:
        Call mVBOrb.ErrReraise("writeSeqOctet")
    End Sub

    Public Sub writeAny(ByVal wVal As cOrbAny)
        On Error GoTo ErrHandler
        Call writeTypeCode(wVal.getType_Renamed())
        Call wVal.writeValue(Me)
        Exit Sub
ErrHandler:
        Call mVBOrb.ErrReraise("writeAny")
    End Sub

    Public Sub writeBoolean(ByVal wVal As Boolean)
        On Error GoTo ErrHandler
        Call writeEnsure(1)
        If wVal Then
            buffer(bufPos) = 1
        Else
            buffer(bufPos) = 0
        End If
        bufPos = bufPos + 1
        Exit Sub
ErrHandler:
        Call mVBOrb.ErrReraise("writeBoolean")
    End Sub

    Public Sub writeChar(ByVal wVal As Byte)
        On Error GoTo ErrHandler
        Call writeEnsure(1)
        buffer(bufPos) = wVal
        bufPos = bufPos + 1
        Exit Sub
ErrHandler:
        Call mVBOrb.ErrReraise("writeChar")
    End Sub

    'Used by sequence <char> writer
    Public Sub writeChars(ByRef seqArr() As Byte, ByVal seqLen As Integer)
        On Error GoTo ErrHandler
        Call writeEnsure(seqLen)
        If seqLen <= 0 Then
            If seqLen < 0 Then
                Call mVBOrb.VBOrb.raiseMARSHAL(1, mVBOrb.VBOrb.CompletedNO, "Unexpected seqLen: " & CStr(seqLen))
            End If
            'Dim seqCnt As Long, seqEnd As Long
            'seqEnd = LBound(seqArr) + seqLen - 1
            'For seqCnt = LBound(seqArr) To seqEnd
            '    buffer(bufPos) = seqArr(seqCnt)
            '    bufPos = bufPos + 1
            'Next seqCnt
        Else
            If UBound(seqArr) - LBound(seqArr) + 1 < seqLen Then
                Call mVBOrb.VBOrb.raiseMARSHAL(1, mVBOrb.VBOrb.CompletedNO, "seqArr too small, need: " & CStr(seqLen))
            End If
            Call dllMoveMemory(buffer(bufPos), seqArr(LBound(seqArr)), seqLen)
            bufPos = bufPos + seqLen
        End If
        Exit Sub
ErrHandler:
        Call mVBOrb.ErrReraise("writeChars")
    End Sub

    Public Sub writeWchar(ByVal wVal As Short)
        If iGIOPVersion < &H102 Then
            Call writeShort(wVal)
            Exit Sub
        End If
        Call writeEnsure(3)
        buffer(bufPos) = 2
        'Big Endian
        bufPos = bufPos + 1
        buffer(bufPos + 1) = wVal And &HFF 'Mod &H100
        If wVal >= 0 Then
            buffer(bufPos) = wVal \ &H100 'And &HFF not necessary
            'Not using And is also a workaround for a 'division by zero' compiler bug
        Else
            wVal = ((wVal Xor &HFFFF) \ &H100) Xor &HFF
            buffer(bufPos) = wVal And &HFF 'Mod &H100
        End If
        bufPos = bufPos + 2
    End Sub

    Public Sub writeShort(ByVal wVal As Short)
        On Error GoTo ErrHandler
        'Alignment
        If (bufPos - bufOff) Mod 2 > 0 Then
            bufPos = bufPos + 1
        End If
        Call writeEnsure(2)

        If wVal >= 0 Then
            If bLittleEndian Then
                buffer(bufPos) = wVal And &HFF 'Mod &H100
                buffer(bufPos + 1) = wVal \ &H100 'And &HFF not necessary
            Else
                buffer(bufPos + 1) = wVal And &HFF 'Mod &H100
                buffer(bufPos) = wVal \ &H100 'And &HFF not necessary
                'Not using And is also a workaround for a 'division by zero' compiler bug
            End If
        Else
            If bLittleEndian Then
                buffer(bufPos) = wVal And &HFF 'Mod &H100
                wVal = ((wVal Xor &HFFFF) \ &H100) Xor &HFF
                buffer(bufPos + 1) = wVal And &HFF 'Mod &H100
            Else
                buffer(bufPos + 1) = wVal And &HFF 'Mod &H100
                wVal = ((wVal Xor &HFFFF) \ &H100) Xor &HFF
                buffer(bufPos) = wVal And &HFF 'Mod &H100
            End If
        End If
        bufPos = bufPos + 2
        Exit Sub
ErrHandler:
        Call mVBOrb.ErrReraise("writeShort")
    End Sub

    Public Sub writeUshort(ByVal wVal As Short)
        Call writeShort(wVal)
    End Sub

    Public Sub writeLong(ByVal wVal As Integer)
        On Error GoTo ErrHandler
        'Alignment
        If (bufPos - bufOff) Mod 4 > 0 Then
            bufPos = bufPos + 4 - ((bufPos - bufOff) Mod 4)
        End If
        Call writeEnsure(4)

        If wVal >= 0 Then
            If bLittleEndian Then
                buffer(bufPos) = wVal And &HFF 'Mod &H100
                buffer(bufPos + 1) = (wVal \ &H100) And &HFF 'Mod &H100
                buffer(bufPos + 2) = (wVal \ &H10000) And &HFF 'Mod &H100
                buffer(bufPos + 3) = wVal \ &H1000000 'And &HFF not necessary
            Else
                buffer(bufPos + 3) = wVal And &HFF 'Mod &H100
                buffer(bufPos + 2) = (wVal \ &H100) And &HFF 'Mod &H100
                buffer(bufPos + 1) = (wVal \ &H10000) And &HFF 'Mod &H100
                buffer(bufPos) = wVal \ &H1000000 'And &HFF not necessary
            End If
        Else
            If bLittleEndian Then
                buffer(bufPos) = wVal And &HFF 'Mod &H100
                wVal = ((wVal Xor &HFFFFFFFF) \ &H100) Xor &HFFFFFF
                buffer(bufPos + 1) = wVal And &HFF 'Mod &H100
                buffer(bufPos + 2) = (wVal \ &H100) And &HFF 'Mod &H100
                buffer(bufPos + 3) = (wVal \ &H10000) And &HFF 'Mod &H100
            Else
                buffer(bufPos + 3) = wVal And &HFF 'Mod &H100
                wVal = ((wVal Xor &HFFFFFFFF) \ &H100) Xor &HFFFFFF
                buffer(bufPos + 2) = wVal And &HFF 'Mod &H100
                buffer(bufPos + 1) = (wVal \ &H100) And &HFF 'Mod &H100
                buffer(bufPos) = (wVal \ &H10000) And &HFF 'Mod &H100
            End If
        End If
        bufPos = bufPos + 4
        Exit Sub
ErrHandler:
        Call mVBOrb.ErrReraise("writeLong")
    End Sub

    Public Sub writeUlong(ByVal wVal As Integer)
        Call writeLong(wVal)
    End Sub

    Public Sub writeLonglong(ByVal wVal As Object)
        If wVal < CDec(0) Then
            'UPGRADE_WARNING: Couldn't resolve default property of object wVal. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6A50421D-15FE-4896-8A1B-2EC21E9037B2"'
            Call writeUlonglong(wVal + CDec("18446744073709551616"))
        Else
            Call writeUlonglong(wVal)
        End If
    End Sub

    Public Sub writeUlonglong(ByVal wVal As Object)
        'Alignment
        If (bufPos - bufOff) Mod 8 > 0 Then
            bufPos = bufPos + 8 - ((bufPos - bufOff) Mod 8)
        End If
        Call writeEnsure(8)
        Dim l2p32 As Object
        'UPGRADE_WARNING: Couldn't resolve default property of object l2p32. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6A50421D-15FE-4896-8A1B-2EC21E9037B2"'
        l2p32 = CDec("4294967296")
        Dim l2p31 As Object
        'UPGRADE_WARNING: Couldn't resolve default property of object l2p31. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6A50421D-15FE-4896-8A1B-2EC21E9037B2"'
        l2p31 = CDec("2147483648")
        Dim l0 As Object
        Dim l1 As Object
        'UPGRADE_WARNING: Couldn't resolve default property of object l2p32. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6A50421D-15FE-4896-8A1B-2EC21E9037B2"'
        'UPGRADE_WARNING: Couldn't resolve default property of object wVal. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6A50421D-15FE-4896-8A1B-2EC21E9037B2"'
        'UPGRADE_WARNING: Couldn't resolve default property of object l1. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6A50421D-15FE-4896-8A1B-2EC21E9037B2"'
        l1 = Fix(wVal / l2p32)
        'UPGRADE_WARNING: Couldn't resolve default property of object l2p32. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6A50421D-15FE-4896-8A1B-2EC21E9037B2"'
        'UPGRADE_WARNING: Couldn't resolve default property of object l1. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6A50421D-15FE-4896-8A1B-2EC21E9037B2"'
        'UPGRADE_WARNING: Couldn't resolve default property of object wVal. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6A50421D-15FE-4896-8A1B-2EC21E9037B2"'
        'UPGRADE_WARNING: Couldn't resolve default property of object l0. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6A50421D-15FE-4896-8A1B-2EC21E9037B2"'
        l0 = wVal - (l1 * l2p32)
        'UPGRADE_WARNING: Couldn't resolve default property of object l2p32. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6A50421D-15FE-4896-8A1B-2EC21E9037B2"'
        'UPGRADE_WARNING: Couldn't resolve default property of object l0. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6A50421D-15FE-4896-8A1B-2EC21E9037B2"'
        If l0 >= l2p32 Then
            'UPGRADE_WARNING: Couldn't resolve default property of object l2p32. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6A50421D-15FE-4896-8A1B-2EC21E9037B2"'
            'UPGRADE_WARNING: Couldn't resolve default property of object l0. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6A50421D-15FE-4896-8A1B-2EC21E9037B2"'
            l0 = l0 - l2p32
            'UPGRADE_WARNING: Couldn't resolve default property of object l1. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6A50421D-15FE-4896-8A1B-2EC21E9037B2"'
            l1 = l1 + CDec(1)
        ElseIf l0 < CDec(0) Then
            'UPGRADE_WARNING: Couldn't resolve default property of object l2p32. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6A50421D-15FE-4896-8A1B-2EC21E9037B2"'
            'UPGRADE_WARNING: Couldn't resolve default property of object l0. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6A50421D-15FE-4896-8A1B-2EC21E9037B2"'
            l0 = l0 + l2p32
            'UPGRADE_WARNING: Couldn't resolve default property of object l1. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6A50421D-15FE-4896-8A1B-2EC21E9037B2"'
            l1 = l1 - CDec(1)
        End If
        'UPGRADE_WARNING: Couldn't resolve default property of object l2p31. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6A50421D-15FE-4896-8A1B-2EC21E9037B2"'
        'UPGRADE_WARNING: Couldn't resolve default property of object l0. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6A50421D-15FE-4896-8A1B-2EC21E9037B2"'
        'UPGRADE_WARNING: Couldn't resolve default property of object l2p32. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6A50421D-15FE-4896-8A1B-2EC21E9037B2"'
        'UPGRADE_WARNING: Couldn't resolve default property of object l0. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6A50421D-15FE-4896-8A1B-2EC21E9037B2"'
        If l0 >= l2p31 Then l0 = l0 - l2p32
        'UPGRADE_WARNING: Couldn't resolve default property of object l2p31. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6A50421D-15FE-4896-8A1B-2EC21E9037B2"'
        'UPGRADE_WARNING: Couldn't resolve default property of object l1. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6A50421D-15FE-4896-8A1B-2EC21E9037B2"'
        'UPGRADE_WARNING: Couldn't resolve default property of object l2p32. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6A50421D-15FE-4896-8A1B-2EC21E9037B2"'
        'UPGRADE_WARNING: Couldn't resolve default property of object l1. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6A50421D-15FE-4896-8A1B-2EC21E9037B2"'
        If l1 >= l2p31 Then l1 = l1 - l2p32
        If bLittleEndian Then
            'UPGRADE_WARNING: Couldn't resolve default property of object l0. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6A50421D-15FE-4896-8A1B-2EC21E9037B2"'
            Call writeLong(CInt(l0))
            'UPGRADE_WARNING: Couldn't resolve default property of object l1. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6A50421D-15FE-4896-8A1B-2EC21E9037B2"'
            Call writeLong(CInt(l1))
        Else
            'UPGRADE_WARNING: Couldn't resolve default property of object l1. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6A50421D-15FE-4896-8A1B-2EC21E9037B2"'
            Call writeLong(CInt(l1))
            'UPGRADE_WARNING: Couldn't resolve default property of object l0. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6A50421D-15FE-4896-8A1B-2EC21E9037B2"'
            Call writeLong(CInt(l0))
        End If
    End Sub

    'First version was written by Holger Beer
    Public Sub writeFloat(ByVal wVal As Single)
        On Error GoTo ErrHandler
        Dim i As Integer
        Dim bytes(3) As Byte

        'Alignment
        If (bufPos - bufOff) Mod 4 > 0 Then
            bufPos = bufPos + 4 - ((bufPos - bufOff) Mod 4)
        End If
        Call writeEnsure(4)

        Call dllMoveMemory(VarPtr(bytes(0)), VarPtr(wVal), 4)
        If bLittleEndian Then
            For i = 0 To 3
                buffer(bufPos + i) = bytes(i)
            Next
        Else
            For i = 0 To 3
                buffer(bufPos + 3 - i) = bytes(i)
            Next
        End If

        bufPos = bufPos + 4
        Exit Sub
ErrHandler:
        Call mVBOrb.ErrReraise("writeFloat")
    End Sub

    'First version was written by Holger Beer
    Public Sub writeDouble(ByVal wVal As Double)
        On Error GoTo ErrHandler
        Dim i As Integer
        Dim bytes(7) As Byte

        'Alignment
        If (bufPos - bufOff) Mod 8 > 0 Then
            bufPos = bufPos + 8 - ((bufPos - bufOff) Mod 8)
        End If
        Call writeEnsure(8)

        Call dllMoveMemory(VarPtr(bytes(0)), VarPtr(wVal), 8)
        If bLittleEndian Then
            For i = 0 To 7
                buffer(bufPos + i) = bytes(i)
            Next
        Else
            For i = 0 To 7
                buffer(bufPos + 7 - i) = bytes(i)
            Next
        End If

        bufPos = bufPos + 8
        Exit Sub
ErrHandler:
        Call mVBOrb.ErrReraise("writeDouble")
    End Sub

    'write_longdouble()
    Public Sub writeLongdouble(ByVal wVal As cOrblongdouble)
        On Error GoTo ErrHandler
        Dim i As Integer
        Dim bytes(15) As Byte

        'Alignment
        If (bufPos - bufOff) Mod 8 > 0 Then
            bufPos = bufPos + 8 - ((bufPos - bufOff) Mod 8)
        End If
        Call writeEnsure(16)

        Call wVal.getBytes(bytes)
        If bLittleEndian Then
            For i = 0 To 15
                buffer(bufPos + i) = bytes(i)
            Next
        Else
            For i = 0 To 15
                buffer(bufPos + 15 - i) = bytes(i)
            Next
        End If

        bufPos = bufPos + 16
        Exit Sub
ErrHandler:
        Call mVBOrb.ErrReraise("writeLongdouble")
    End Sub

    '??? (VBOrb ignores code set info)
    'If a server’s native char code set is not specified in the IOR multi-component profile,
    'then it is considered to be ISO 8859-1 for backward compatibility.
    Public Sub writeString(ByRef wVal As String)
        On Error GoTo ErrHandler
        Dim strLen As Integer
        strLen = Len(wVal)
        Call writeUlong(strLen + 1)
        Call writeEnsure(strLen + 1)
        'Some users like to transfer '\0' characters in strings.
        'Thanks to Cristiano Duarte (System Analyst & Web Developer, Brazil)
        Call dllMoveMemory(VarPtr(buffer(bufPos)), VarPtr(wVal), strLen)
        'Old:
        '   Call dllStrCpy(buffer(bufPos), wVal)
        'Old:
        '   Dim i1 As Long
        '   For i1 = 0 To strlen - 1
        '       buffer(bufPos + i1) = Asc(Mid$(wVal, i1 + 1, 1))
        '   Next i1
        buffer(bufPos + strLen) = 0
        bufPos = bufPos + strLen + 1
        Exit Sub
ErrHandler:
        Call mVBOrb.ErrReraise("writeString")
    End Sub

    '??? (VBOrb ignores code set info)
    'A server that supports interfaces that use wide character data is required
    'to specify its native wchar code set in the IOR multi-component profile;
    'if one is not specified, then the client-side ORB raises exception INV_OBJREF.
    'First version was written by Kalinine Iwan (ICQ: Iwanture)
    Public Function writeWString(ByRef wVal As String) As Object
        On Error GoTo ErrHandler
        'For GIOP version < 1.2 length of string sended in characters plus null terminator
        Dim oldGIOP As Boolean
        oldGIOP = getGIOPVersion() < &H102
        If oOrb.VisiWorkaround Then
            oldGIOP = True
            '??? ElseIf lTCS_W = 0 Then
            '    Call mVBOrb.VBOrb.raiseINVOBJREF(0, mVBOrb.VBOrb.CompletedNO, _
            ''        "Missing server native wchar code set in IOR")
        End If
        'Length of Unicode string
        Dim lNOcts As Integer
        lNOcts = Len(wVal) * 2
        Call writeUlong(IIf(oldGIOP, (lNOcts \ 2) + 1, lNOcts))
        Dim baStr() As Byte
        Dim i As Integer
        If lNOcts > 0 Then 'baStr(1) does not work if lNOcts = 0!
            Call writeEnsure(lNOcts)
            ReDim baStr(lNOcts)
            'UPGRADE_ISSUE: StrPtr function is not supported. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="367764E5-F3F8-4E43-AC3E-7FE0B5E074E2"'

            Debug.Assert(True)
            Call dllMoveMemory(baStr(1), VarPtr(wVal), lNOcts)
            For i = 0 To lNOcts - 2 Step 2
                baStr(i) = baStr(i + 2)
            Next i
            Call dllMoveMemory(buffer(bufPos), baStr(0), lNOcts)
            Erase baStr
            bufPos = bufPos + lNOcts
        End If
        If oldGIOP Then Call writeShort(0)
        Exit Function
ErrHandler:
        Call mVBOrb.ErrReraise("writeWString")
    End Function

    Public Sub writeObject(ByVal Obj As _cOrbObject)
        On Error GoTo ErrHandler
        Dim oObjRef As cOrbObjRef
        If Obj Is Nothing Then
            'UPGRADE_NOTE: Object oObjRef may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
            oObjRef = Nothing
        Else
            oObjRef = Obj.getObjRef()
        End If
        If oObjRef Is Nothing Then
            'string type_id;
            Call writeString("")
            'sequence <TaggedProfile> profiles;
            Call writeUlong(0)
        Else
            Call oObjRef.writeMe(Me, Obj.getId())
        End If
        Exit Sub
ErrHandler:
        Call mVBOrb.ErrReraise("writeObject")
    End Sub

    Public Sub writeValue(ByVal oValueBase As _cOrbValueBase)
        On Error GoTo ErrHandler
        Dim sUniqueId As String
        Dim lookupPos As Integer
        Dim lValueTag As Integer
        If oValueBase Is Nothing Then
#If DebugMode Then
			'UPGRADE_NOTE: #If #EndIf block was not upgraded because the expression DebugMode did not evaluate to True or was not evaluated. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="27EE2C3C-05AF-4C04-B2AF-657B4FB6B5FC"'
			Call oOrb.logMsg("Write <value_tag>: 00000000")
#End If
            Call writeLong(0)
        Else
            sUniqueId = CStr(oValueBase.UniqueId)
            If colValues Is Nothing Then colValues = New Collection
            On Error Resume Next
            'UPGRADE_WARNING: Couldn't resolve default property of object colValues.Item(). Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6A50421D-15FE-4896-8A1B-2EC21E9037B2"'
            lookupPos = colValues.Item(sUniqueId)
            If Err.Number <> 0 Then lookupPos = 0
            On Error GoTo ErrHandler
            If lookupPos = 0 Then
                Call colValues.Add(bufPos, sUniqueId)
                lValueTag = &H7FFFFF00
                If oValueBase.getIds(1) <> "" Then
                    lValueTag = lValueTag Or &H2 '&H6&???
                End If
                If oValueBase.getIds(0) <> "" Then
                    lValueTag = lValueTag Or &H2
                End If
                If oValueBase.isCustom() Then 'or lChunkValNest > 0???
                    lValueTag = lValueTag Or &H8
                End If
                If lChunkValNest > 0 Then
                    Call writeChunkEnd() 'Complete chunk of last value if len > 0
                End If
#If DebugMode Then
				'UPGRADE_NOTE: #If #EndIf block was not upgraded because the expression DebugMode did not evaluate to True or was not evaluated. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="27EE2C3C-05AF-4C04-B2AF-657B4FB6B5FC"'
				Call oOrb.logMsg("Write <value_tag>: " & Hex(lValueTag))
#End If
                Call writeLong(lValueTag) 'Next tag: Start of value
                If (lValueTag And &H6) = &H2 Then
                    Call writeValRepId(oValueBase.getIds(0))
                End If
                If (lValueTag And &H8) = &H8 Then
                    lChunkValNest = lChunkValNest + 1
                    Call writeChunkStart() 'Next tag: Maybe a chunk follows
                End If
                Call oValueBase.writeMe(Me)
                If (lValueTag And &H8) = &H8 Then
                    Call writeChunkEnd() 'Complete chunk if len > 0
                    Call writeLong(-lChunkValNest) 'Next tag: End of value
                    lChunkValNest = lChunkValNest - 1
                End If
                If lChunkValNest > 0 Then
                    Call writeChunkStart() 'Next tag: Maybe a chunk follows
                End If
            Else
#If DebugMode Then
				'UPGRADE_NOTE: #If #EndIf block was not upgraded because the expression DebugMode did not evaluate to True or was not evaluated. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="27EE2C3C-05AF-4C04-B2AF-657B4FB6B5FC"'
				Call oOrb.logMsg("Write <value_tag>: FFFFFFFF")
#End If
                Call writeLong(&HFFFFFFFF) '<indirection_tag>
                Call writeLong(lookupPos - bufPos)
            End If
        End If
        Exit Sub
ErrHandler:
        Call mVBOrb.ErrReraise("writeValue")
    End Sub

    'Write <repository_id> of value <type_info>
    Private Sub writeValRepId(ByRef RepId As String)
        On Error GoTo ErrHandler
        If colValRepIds Is Nothing Then colValRepIds = New Collection
        Dim lookupPos As Integer
        On Error Resume Next
        'UPGRADE_WARNING: Couldn't resolve default property of object colValRepIds.Item(). Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6A50421D-15FE-4896-8A1B-2EC21E9037B2"'
        lookupPos = colValRepIds.Item(RepId)
        If Err.Number <> 0 Then lookupPos = 0
        On Error GoTo ErrHandler
#If DebugMode Then
		'UPGRADE_NOTE: #If #EndIf block was not upgraded because the expression DebugMode did not evaluate to True or was not evaluated. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="27EE2C3C-05AF-4C04-B2AF-657B4FB6B5FC"'
		'If lookupPos <> 0 Then
		'    Call colValRepIds.Remove(RepId)
		'    lookupPos = 0
		'End If
		Call oOrb.logMsg("Write <repository_id>: " & RepId)
#End If
        If lookupPos = 0 Then
            Call colValRepIds.Add(bufPos, RepId)
            Call writeString(RepId)
        Else
            Call writeLong(&HFFFFFFFF) 'RepId <indirection_tag>
            Call writeLong(lookupPos - bufPos)
        End If
        Exit Sub
ErrHandler:
        Call mVBOrb.ErrReraise("writeValRepId")
    End Sub

    Private Sub writeChunkStart()
        On Error GoTo ErrHandler
        Call writeLong(0) 'Will be overwritten by writeChunkEnd()
        lChunkBufPos = bufPos
        Exit Sub
ErrHandler:
        Call mVBOrb.ErrReraise("writeChunkStart")
    End Sub

    Private Sub writeChunkEnd()
        On Error GoTo ErrHandler
        Dim chunkLen As Integer
        chunkLen = bufEof - lChunkBufPos
        bufPos = lChunkBufPos - 4
        If chunkLen > 0 Then
            Call writeLong(chunkLen)
            Call writeSkip(chunkLen)
        End If
        Exit Sub
ErrHandler:
        Call mVBOrb.ErrReraise("writeChunkEnd")
    End Sub

    'Abstract interfaces are encoded as a union with a boolean discriminator.
    'IN:    oAbstract   cOrbObjRef or cOrbValueBase
    Public Sub writeAbstract(ByVal oAbstract As _cOrbAbstractBase)
        On Error GoTo ErrHandler
        If oAbstract Is Nothing Then
            Call writeBoolean(False)
            Call writeValue(Nothing)
        ElseIf oAbstract.isObjRef() Then
            Call writeBoolean(True)
            'UPGRADE_WARNING: Couldn't resolve default property of object oAbstract. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6A50421D-15FE-4896-8A1B-2EC21E9037B2"'
            Call writeObject(oAbstract)
        Else
            Call writeBoolean(False)
            'UPGRADE_WARNING: Couldn't resolve default property of object oAbstract. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6A50421D-15FE-4896-8A1B-2EC21E9037B2"'
            Call writeValue(oAbstract)
        End If
        Exit Sub
ErrHandler:
        Call mVBOrb.ErrReraise("writeAbstract")
    End Sub

    Public Sub writeTypeCode(ByVal wVal As cOrbTypeCode)
        On Error GoTo ErrHandler
        Dim bTopLevel As Boolean
        Dim bHasComplexPars As Boolean
        If colTypeCodes Is Nothing Then
            bTopLevel = True
            colTypeCodes = New Collection
        End If
        bHasComplexPars = wVal.hasComplexPars
        Dim sUniqueId As String
        Dim lookupPos As Integer
        If bHasComplexPars Then
            sUniqueId = CStr(wVal.getChId())
            On Error Resume Next
            'UPGRADE_WARNING: Couldn't resolve default property of object colTypeCodes.Item(). Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6A50421D-15FE-4896-8A1B-2EC21E9037B2"'
            lookupPos = colTypeCodes.Item(sUniqueId)
            If Err.Number <> 0 Then lookupPos = 0
            On Error GoTo ErrHandler
            If lookupPos = 0 Then
                Call colTypeCodes.Add(bufPos, sUniqueId)
            Else
                Call writeLong(tk_recursive) 'Indirection
                Call writeLong(lookupPos - bufPos)
                Exit Sub
            End If
            Call writeLong(wVal.kind)
            Call writeEncapOpen(False)
        Else
            Call writeLong(wVal.kind)
        End If
        Call wVal.writeMe(Me)
        If bHasComplexPars Then
            Call writeEncapClose()
        End If
        If bTopLevel Then
            'UPGRADE_NOTE: Object colTypeCodes may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
            colTypeCodes = Nothing
        End If
        Exit Sub
ErrHandler:
        Call mVBOrb.ErrReraise("writeTypeCode")
    End Sub

    'SeqLen = oIn.Available
    Public Sub writeStream(ByVal oIn As cOrbStream, ByVal seqLen As Integer)
        On Error GoTo ErrHandler
        Dim seqArr() As Byte
        If seqLen > 0 Then
            ReDim seqArr(seqLen)
            Call oIn.readOctets(seqArr, seqLen)
            Call Me.writeOctets(seqArr, seqLen)
            Erase seqArr
        End If
        Exit Sub
ErrHandler:
        Call mVBOrb.ErrReraise("writeStream")
    End Sub

    '#If DebugMode Then
    'First version was written by Mikael Gjörloff, Sweden, 2000-08-29
    'Small debugging tool to have a look in the buffer of 'Me'.
    'Usage:
    '   Call MyOrbStream.dumpbuffer(App.Path & "\" & "buffsniff" & ".log")
    'Creates it if it does not exists
    'All of this may very well be removed... ;-)
    Public Sub dumpBuffer(ByVal FileName As String)
        Dim bufPosOld As Integer
        bufPosOld = bufPos
        Dim iFileNo As Short
        iFileNo = FreeFile()
        FileOpen(iFileNo, FileName, OpenMode.Append)
        PrintLine(iFileNo, mVBOrb.getDateTimeStr() & " D Start, bufEof= " & CStr(bufEof))

        If bufEof > UBound(buffer) + 1 Then
            bufEof = UBound(buffer) + 1
        End If
        Dim sLine1 As String
        Dim sLine2 As String
        bufPos = 0
        Dim iValCnt As Short
        Dim sVal As String
        Dim iCh As Short
        Dim lVal As Integer
        While bufPos < bufEof
            sLine1 = VB6.Format(bufPos, "000000000")
            sLine2 = " "
            For iValCnt = 0 To 3
                If bufPos + 3 > UBound(buffer) Then
                    bufPos = bufPos + 4
                    sVal = Space(8)
                Else
                    For iCh = 0 To 3
                        If buffer(bufPos + iCh) < 32 Or buffer(bufPos + iCh) > 126 Then
                            sLine2 = sLine2 & "."
                        Else
                            sLine2 = sLine2 & Chr(buffer(bufPos + iCh))
                        End If
                    Next iCh
                    'LittleEndian:
                    'Call dllMoveMemory(VarPtr(lVal), VarPtr(buffer(bufPos)), 4)
                    'BigEndian:
                    If buffer(bufPos) >= &H80 Then
                        lVal = ((((CInt(buffer(bufPos) Xor &HFF) * &H100 + CInt(buffer(bufPos + 1))) * &H100 + CInt(buffer(bufPos + 2))) * &H100 + CInt(buffer(bufPos + 3)))) Xor &HFF000000
                    Else
                        lVal = (((CInt(buffer(bufPos)) * &H100 + CInt(buffer(bufPos + 1))) * &H100 + CInt(buffer(bufPos + 2))) * &H100 + CInt(buffer(bufPos + 3)))
                    End If
                    bufPos = bufPos + 4

                    sVal = Hex(lVal)
                    sVal = New String("0", 8 - Len(sVal)) & sVal
                End If
                sLine1 = sLine1 & " " & sVal
            Next iValCnt
            PrintLine(iFileNo, sLine1 & sLine2)
        End While

        PrintLine(iFileNo, mVBOrb.getDateTimeStr() & " D End of buffer")
        FileClose(iFileNo)
        bufPos = bufPosOld
    End Sub
    '#End If
End Class