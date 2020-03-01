Option Strict Off
Option Explicit On
<System.Runtime.InteropServices.ProgId("cOrbObjRef_NET.cOrbObjRef")> Public Class cOrbObjRef
	Implements _cOrbAbstractBase
	Implements _cOrbObject
	'Copyright (c) 1999 Martin.Both
	
	'This library is free software; you can redistribute it and/or
	'modify it under the terms of the GNU Library General Public
	'License as published by the Free Software Foundation; either
	'version 2 of the License, or (at your option) any later version.
	
	'This library is distributed in the hope that it will be useful,
	'but WITHOUT ANY WARRANTY; without even the implied warranty of
	'MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
	'Library General Public License for more details.
	
	'History:
	'First LW implemetation was written by Mikael Gjörloff, Sweden, 2000-09-04
	'First CodeSetComponentInfo is written by Iwanture, 2001-05-01
	
	
	
	'Set DebugMode = 0 to deactivate debug code in this class
#Const DebugMode = 0
	
#If DebugMode Then
	'UPGRADE_NOTE: #If #EndIf block was not upgraded because the expression DebugMode did not evaluate to True or was not evaluated. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="27EE2C3C-05AF-4C04-B2AF-657B4FB6B5FC"'
	Private lClassDebugID As Long
#End If
	
	Private oOrb As cOrbImpl
	Private iLogLevel As Short
	
	'Id of a host connection slot or -1
	Private lOrbConnId As Short
	
	'Interoperable Object Reference (IOR)
	'An IOR is a sequence of object-specific protocol profiles, plus a type ID.
	
	'------ TypeId ---------
	'When a reference to a base Object is encoded, there are two allowed encodings
	'for the Repository ID: either "IDL:omg.org/CORBA/Object:1.0" or "" may be used.
	Private sTypeId As String
	'"Null" type ID is a string which contains only a single terminating character.
	'Type IDs may only be "Null" in any message, requiring the client to use existing
	'knowledge or to consult the object, to determine interface types supported.
	Const sNullTypeId As String = ""
	
	Private bIsLocal As Boolean
	
	'------ Profiles -------
	'To support the Full-IOR conformance it is required to preserve all the
	'semantic content of any IOR (including the ordering of each profile and its
	'components). Only transformations which preserve semantics (e.g.,
	'changing Byte order for encapsulation) are allowed.
	
	'Standard Profile Tags
	Const TAG_INTERNET_IOP As Integer = 0 'IIOP IOR Profile
	Const TAG_MULTIPLE_COMPONENTS As Integer = 1
	Const TAG_SCCP_IOP As Integer = 2 'See CORBA/IN Interworking specification
	
	Private Structure tTProf 'TaggedProfile
		Dim ptag As Integer 'ProfileId, unsigned long tag
		Dim pdatlen As Integer 'Profile data length
		Dim pdata() As Byte 'Profile data, sequence <octet>
		Dim bPIsInitByURL As Boolean
	End Structure
	'sequence <TaggedProfile> profiles;
	Private lTProfSeqLen As Integer
	Private TProfs() As tTProf
	
	'------ Used IIOP profile or -1
	Private lSelectedProfile As Integer
	
	'------ Standard Component Tags
	'Components of TAG_INTERNET_IOP profile
	Const TAG_ALTERNATE_IIOP_ADDRESS As Integer = 3
	Const TAG_CODE_SETS As Integer = 1
	
	'Components of TAG_MULTIPLE_COMPONENTS profile
	Const TAG_COMPLETE_OBJECT_KEY As Integer = 5
	
	'IIOP Version of the selected IIOP profile
	Private iIIOPVersion As Short
	
	'Server Native Code Set (or 0)
	Private lSNCS_C As Integer
	Private lSNCS_W As Integer
	
	'Object key, example:    0x3A 3A 00 0F 2E 48 22 3C 01 3C 1F
	Private baObjKey() As Byte
	
	'Number of IIOP addresses, at least one
	Private iAddrCnt As Short
	Private bIsInitByURL As Boolean
	
	'Host, example:       "164.25.33.38"
	Private sHosts(4) As String
	
	'Port, example:       3821
	Private iPorts(4) As Short
	
	'If lSelectedProfile >= 0 Then: Index of last used IP address or -1
	Private iAddrSel As Short
	
	'AddressingDisposition, 0 = KeyAddr, 1 = ProfileAddr, 2 = ReferenceAddr
	Private iAddrDisp As Short
	
	'------ Components -----
	'------ Components??? --
	'Object references are immutable. That is at the time that they are
	'created their policies are set in stone, and cannot be changed.
	
	'0 = LW not tested, >0 = LW tested and required if not oLcFwObjRef is nothing
	Private iLcFwCnt As Short
	'Mikael Gjörloff: Quiz: Any risk of a loop? Should there be a counter and an exit
	'if run more than, say, twenty times and still is recieving LOCATION_FORWARD?
	'It should never happen, but in my experience, a lot of things has happened
	'that was not meant to... ;-)
	Const LOCATION_FORWARD_LIMIT As Short = 20
	Private oLcFwObjRef As cOrbObjRef
	
	'TRANSPARENT = 0; NO_REBIND = 1; NO_RECONNECT = 2;
	Private iRebindMode As Short
	
	'UPGRADE_NOTE: Class_Initialize was upgraded to Class_Initialize_Renamed. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="A9E4979A-37FA-4718-9994-97DD76ED70A7"'
	Private Sub Class_Initialize_Renamed()
#If DebugMode Then
		'UPGRADE_NOTE: #If #EndIf block was not upgraded because the expression DebugMode did not evaluate to True or was not evaluated. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="27EE2C3C-05AF-4C04-B2AF-657B4FB6B5FC"'
		lClassDebugID = mVBOrb.getNextClassDebugID()
		Debug.Print "'" & TypeName(Me) & "' " & lClassDebugID & " initialized"
#End If
		'Set oOrb = Nothing
		'sTypeId = sNullTypeId
		'lTProfSeqLen = 0
		lSelectedProfile = -1
		'iAddrCnt = 0
		lOrbConnId = -1
	End Sub
	Public Sub New()
		MyBase.New()
		Class_Initialize_Renamed()
	End Sub
	
	'(Do not overwrite a global Err object here.)
	'UPGRADE_NOTE: Class_Terminate was upgraded to Class_Terminate_Renamed. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="A9E4979A-37FA-4718-9994-97DD76ED70A7"'
	Private Sub Class_Terminate_Renamed()
		'Do not call a function here which is using the Err object otherwise
		'you will get an Error 0 if an Error is raised before Class_Terminate()
		'is called implicitly.
		If Not oOrb Is Nothing Then
			'Release something which VB cannot know if required
			If lOrbConnId >= 0 Then
				'MarkOnly = True because of above explanation
				Call oOrb.ConnOCIdFree(lOrbConnId, True)
				lOrbConnId = -1
			End If
			'UPGRADE_NOTE: Object oOrb may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
			oOrb = Nothing
			iLogLevel = 0
		End If
#If DebugMode Then
		'UPGRADE_NOTE: #If #EndIf block was not upgraded because the expression DebugMode did not evaluate to True or was not evaluated. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="27EE2C3C-05AF-4C04-B2AF-657B4FB6B5FC"'
		Debug.Print "'" & TypeName(Me) & "' " & CStr(lClassDebugID) & " terminated"
#End If
	End Sub
	Protected Overrides Sub Finalize()
		Class_Terminate_Renamed()
		MyBase.Finalize()
	End Sub
	
#If DebugMode Then
	'UPGRADE_NOTE: #If #EndIf block was not upgraded because the expression DebugMode did not evaluate to True or was not evaluated. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="27EE2C3C-05AF-4C04-B2AF-657B4FB6B5FC"'
	Friend Property Get ClassDebugID() As Long
	ClassDebugID = lClassDebugID
	End Property
#End If
	
	'Release me (ObjRef)
	Public Sub releaseMe()
		Call unselectProfile()
		lTProfSeqLen = 0
		Erase TProfs
		bIsLocal = False
		sTypeId = sNullTypeId
		Call Class_Terminate_Renamed()
	End Sub
	
	'Initialies me (ObjRef and profiles) by iiop: URL, rir: URL is not allowed here
	'IN:    sURL    IIOP URL, e.g. "iiop:1.1@host:portno/key"
	'IN:    SNCS_C  Server Native Code Set, 0 = unknown
	Friend Sub initByURL(ByVal Orb As cOrbImpl, ByVal sURL As String, Optional ByVal sDefPort As String = "2809", Optional ByVal TypeId As String = sNullTypeId, Optional ByVal SNCS_C As Integer = 0, Optional ByVal SNCS_W As Integer = 0)
		On Error GoTo ErrHandler
		
		oOrb = Orb
		iLogLevel = oOrb.getLogLevel()
		'string type_id;
		sTypeId = TypeId
		
		'Init IOR IIOP profile(s)
		'sAddrList/sKeyString
		Dim lStartPos As Integer
		Dim lNextPos As Integer
		Dim lEndPos As Integer
		Dim sKeyString As String
		
		lStartPos = 1
		lEndPos = Len(sURL)
		
		lNextPos = InStr(lStartPos, sURL, "/")
		If lNextPos >= lStartPos And lNextPos <= lEndPos Then
			sKeyString = Mid(sURL, lNextPos + 1, lEndPos - lNextPos)
			lEndPos = lNextPos - 1
		Else
			sKeyString = ""
		End If
		
		Dim iIIOPVerss() As Short
		ReDim iIIOPVerss(UBound(sHosts))
		iAddrCnt = 0
		Dim iPrevVers As Short
		iPrevVers = &H0
		lTProfSeqLen = 0
		Dim lSplitPos As Integer
		Dim sIIOPVersion As String
		Dim lPort As Integer
		Do 
			'lNextPos = InStr(lStartPos & ",", sURL, ",")
			lNextPos = InStr(lStartPos, sURL, ",")
			If lNextPos < lStartPos Or lNextPos > lEndPos Then
				lNextPos = lEndPos + 1
			End If
			
			If InStr(lStartPos, sURL, "iiop:") = lStartPos Then
				lStartPos = lStartPos + 4
			End If
			If InStr(lStartPos, sURL, ":") <> lStartPos Then
				Call mVBOrb.VBOrb.raiseBADPARAM(9, mVBOrb.VBOrb.CompletedNO, "Invalid start of IIOP address '" & Mid(sURL, lStartPos, lNextPos - lStartPos) & "' in '" & sURL & "'")
			End If
			lStartPos = lStartPos + 1
			'{octet major; octet minor} iiop_version;
			lSplitPos = InStr(lStartPos, sURL, "@")
			If lSplitPos >= lStartPos And lSplitPos < lNextPos Then
				sIIOPVersion = Mid(sURL, lStartPos, lSplitPos - lStartPos)
				If Len(sIIOPVersion) <> 3 Or Mid(sIIOPVersion, 2, 1) <> "." Then
					Call mVBOrb.VBOrb.raiseBADPARAM(9, mVBOrb.VBOrb.CompletedNO, "IIOP version tag '" & sIIOPVersion & "' is invalid")
				End If
				iIIOPVersion = (Asc(Mid(sIIOPVersion, 1, 1)) - Asc("0")) 'IIOPVerMajor
				iIIOPVersion = iIIOPVersion * &H100 + Asc(Mid(sIIOPVersion, 3, 1)) - Asc("0") 'IIOPVerMinor
				If iIIOPVersion < &H100 Or iIIOPVersion > &H102 Then
					Call mVBOrb.VBOrb.raiseBADPARAM(10, mVBOrb.VBOrb.CompletedNO, "IIOP version '" & sIIOPVersion & "' is unsupported")
				End If
				iIIOPVerss(iAddrCnt) = iIIOPVersion
				lStartPos = lSplitPos + 1
			Else
				iIIOPVerss(iAddrCnt) = &H100
			End If
			'Do we need one more IIOP Profile to store the IIOP address?
			If iIIOPVerss(iAddrCnt) <> iPrevVers Or iIIOPVerss(iAddrCnt) = &H100 Then
				iPrevVers = iIIOPVerss(iAddrCnt)
				lTProfSeqLen = lTProfSeqLen + 1
			End If
			
			'??? It would be better to use inet_p2n of cOrbSocket here, or move to a module ???
			'string host; unsigned short port;
			lSplitPos = InStr(lStartPos, sURL, ":")
			If lSplitPos >= lStartPos And lSplitPos < lNextPos Then
				sHosts(iAddrCnt) = Mid(sURL, lStartPos, lSplitPos - lStartPos)
				On Error Resume Next
				lPort = CInt(Mid(sURL, lSplitPos + 1, lNextPos - lSplitPos - 1))
				If Err.Number <> 0 Then
					On Error GoTo ErrHandler 'Is calling Err.Clear()
					Call mVBOrb.VBOrb.raiseBADPARAM(10, mVBOrb.VBOrb.CompletedNO, "Portnumber " & Mid(sURL, lSplitPos + 1, lNextPos - lSplitPos - 1) & " is invalid")
				End If
				On Error GoTo ErrHandler
			Else
				sHosts(iAddrCnt) = Mid(sURL, lStartPos, lNextPos - lStartPos)
				On Error Resume Next
				lPort = CInt(sDefPort)
				If Err.Number <> 0 Then
					On Error GoTo ErrHandler 'Is calling Err.Clear()
					Call mVBOrb.VBOrb.raiseBADPARAM(10, mVBOrb.VBOrb.CompletedNO, "Portnumber " & sDefPort & " is invalid")
				End If
				On Error GoTo ErrHandler
			End If
			If lPort < 0 Or lPort >= &H10000 Then
				Call mVBOrb.VBOrb.raiseBADPARAM(10, mVBOrb.VBOrb.CompletedNO, "Portnumber " & CStr(lPort) & " is out of range")
			End If
			iPorts(iAddrCnt) = IIf(lPort <= &H7FFF, lPort, lPort - &H10000)
			If sHosts(iAddrCnt) = "" Or sHosts(iAddrCnt) = "localhost" Then
				sHosts(iAddrCnt) = oOrb.localHost
			End If
			
			iAddrCnt = iAddrCnt + 1
			lStartPos = lNextPos + 1 'Skip address separator ","
		Loop While lStartPos <= lEndPos
		
		'sequence <octet> object_key;
		Call mVBOrb.string2ObjKey(sKeyString, baObjKey)
		
		lSelectedProfile = -1
		'Null object reference, is_nil()?
		If sTypeId = sNullTypeId And lTProfSeqLen = 0 Then
			Exit Sub
		End If
		
		ReDim TProfs(lTProfSeqLen - 1)
		Dim iAddrIx As Integer
		iAddrIx = 0
		Dim iPNo As Integer
		iPNo = 0
		Dim oPOut As cOrbStream
		Dim iAddrNext As Integer
		Dim TCLen As Integer
		Do While iPNo < lTProfSeqLen
			TProfs(iPNo).ptag = TAG_INTERNET_IOP 'ProfileId
			oPOut = New cOrbStream
			Call oPOut.initStream(Orb, &H100)
			'sequence <octet> profile_data
			Call oPOut.writeEncapOpen(False)
			'{octet major; octet minor} iiop_version;
			iIIOPVersion = iIIOPVerss(iAddrIx)
			Call oPOut.writeOctet(iIIOPVersion \ &H100) 'IIOPVerMajor
			Call oPOut.writeOctet(iIIOPVersion And &HFF) 'IIOPVerMinor
			'string host; unsigned short port;
			Call oPOut.writeString(sHosts(iAddrIx))
			Call oPOut.writeUshort(iPorts(iAddrIx))
			iAddrIx = iAddrIx + 1
			'sequence <octet> object_key;
			Call oPOut.writeSeqOctet(baObjKey, UBound(baObjKey) - LBound(baObjKey) + 1)
			'Optional
			If iIIOPVersion = &H101 Or iIIOPVersion = &H102 Then
				'sequence <IOP::TaggedComponent> components;
				iAddrNext = iAddrIx
				Do While iAddrNext < iAddrCnt
					If iIIOPVerss(iAddrNext) <> iIIOPVersion Then
						Exit Do
					End If
					iAddrNext = iAddrNext + 1
				Loop 
				TCLen = iAddrNext - iAddrIx 'x * TAG_ALTERNATE_IIOP_ADDRESS
				If SNCS_C <> 0 Or SNCS_W <> 0 Then
					TCLen = TCLen + 1 'TAG_CODE_SETS
				End If
				Call oPOut.writeUlong(TCLen)
				Do While iAddrIx < iAddrNext
					'ComponentId tag
					Call oPOut.writeUlong(TAG_ALTERNATE_IIOP_ADDRESS)
					'sequence <octet> component_data;
					Call oPOut.writeEncapOpen(False)
					'string HostID; short Port;
					Call oPOut.writeString(sHosts(iAddrIx))
					Call oPOut.writeShort(iPorts(iAddrIx))
					Call oPOut.writeEncapClose()
					iAddrIx = iAddrIx + 1
				Loop 
				If SNCS_C <> 0 Or SNCS_W <> 0 Then
					'CodeSetComponent of IOR Multi-Component Profile is written by Iwanture
					'see CORBA v2.3 (page 13-33), CORBA v2.4.2 (page 13-37)
					Call oPOut.writeUlong(TAG_CODE_SETS)
					'sequence <octet> component_data;
					Call oPOut.writeEncapOpen(False) 'Encapsulation of CodeSetComponentInfo
					' CodeSetComponent for char type
					lSNCS_C = SNCS_C
					Call oPOut.writeUlong(lSNCS_C) 'CodeSetId native_code_set;
					Call oPOut.writeUlong(0) 'sequence<CodeSetId> conversion_code_sets;
					' CodeSetComponent for wchar type
					lSNCS_W = SNCS_W
					Call oPOut.writeUlong(lSNCS_W) 'CodeSetId native_code_set;
					Call oPOut.writeUlong(0) 'sequence<CodeSetId> conversion_code_sets;
					Call oPOut.writeEncapClose()
				End If
			End If
			Call oPOut.writeEncapClose()
			Call oPOut.setPos(0) 'Similar to sendGIOPToReadAgain()
			TProfs(iPNo).pdatlen = oPOut.readSeqOctet(TProfs(iPNo).pdata)
			Call oPOut.destroy()
			TProfs(iPNo).bPIsInitByURL = True
			iPNo = iPNo + 1
		Loop 
		'Select first IIOP profile
		Call selectNextIIOPProfile()
		
		Exit Sub
ErrHandler: 
		Call mVBOrb.ErrReraise("initByURL")
	End Sub
	
	'Initialies me (ObjRef) by IOR stream or by one TaggedProfile
	'IN:    bProfileOnly    Is used by "Read an object key of a request"
	'RET:   True if reading a null object reference
	Public Function initByIOR(ByVal Orb As cOrbImpl, ByVal oIn As cOrbStream, Optional ByVal bProfileOnly As Boolean = False) As Boolean
		On Error GoTo ErrHandler
		oOrb = Orb
		iLogLevel = oOrb.getLogLevel()
		If bProfileOnly Then
			sTypeId = sNullTypeId
			lTProfSeqLen = 1
		Else
			'string type_id; Example: IDL:TArbIdl/TConnFactory:1.0
			sTypeId = oIn.readString()
			'sequence <TaggedProfile> profiles;
			lTProfSeqLen = oIn.readUlong()
		End If
		
		lSelectedProfile = -1
		'Null object reference, is_nil()?
		If sTypeId = sNullTypeId And lTProfSeqLen = 0 Then
			initByIOR = True
			Exit Function
		End If
		
		'Read IOR profiles
		ReDim TProfs(lTProfSeqLen - 1) 'lTProfSeqLen must be > 0
		Dim iPNo As Short
		For iPNo = 0 To lTProfSeqLen - 1
			'ProfileId tag
			TProfs(iPNo).ptag = oIn.readUlong()
			'sequence <octet> profile_data
			TProfs(iPNo).pdatlen = oIn.readSeqOctet(TProfs(iPNo).pdata)
			TProfs(iPNo).bPIsInitByURL = False
		Next iPNo
		'Select first IIOP profile of IOR
		Call selectNextIIOPProfile()
		Exit Function
ErrHandler: 
		Call mVBOrb.ErrReraise("initByIOR")
	End Function
	
	'Writing the IOR of me
	Public Sub writeMe(ByVal oOut As cOrbStream, ByRef TypeId As String)
		On Error GoTo ErrHandler
		If bIsLocal Then
			Call mVBOrb.VBOrb.raiseMARSHAL(4, mVBOrb.VBOrb.CompletedNO, "Attempting to marshal a LocalObject")
		End If
		If sTypeId = sNullTypeId Then
			If lTProfSeqLen = 0 Then 'Null object reference, is_nil()?
				'Null object references cannot write this way
				Call mVBOrb.VBOrb.raiseBADPARAM(99, mVBOrb.VBOrb.CompletedNO, "Object has to be nothing")
			End If
			'Objects with no IDL type_id are allowed
		End If
		
		'string type_id; example: IDL:foo/bar:1.0
		Call oOut.writeString(TypeId)
		
		'sequence <TaggedProfile> profiles;
		Call oOut.writeUlong(lTProfSeqLen)
		Dim iPNo As Short
		For iPNo = 0 To lTProfSeqLen - 1
			'ProfileId tag
			Call oOut.writeUlong(TProfs(iPNo).ptag)
			'sequence <octet> profile_data
			Call oOut.writeUlong(TProfs(iPNo).pdatlen)
			Call oOut.writeOctets(TProfs(iPNo).pdata, TProfs(iPNo).pdatlen)
		Next iPNo
		Exit Sub
ErrHandler: 
		Call mVBOrb.ErrReraise("writeMe")
	End Sub
	
	Public ReadOnly Property Orb() As cOrbImpl
		Get
			Orb = oOrb
		End Get
	End Property
	
	Public ReadOnly Property TypeId() As String
		Get
			TypeId = sTypeId
		End Get
	End Property
	
	Public ReadOnly Property TypeIdFw() As String
		Get
			TypeIdFw = Me.ObjRefFw.TypeId
		End Get
	End Property
	
	Public ReadOnly Property ObjRefFw() As cOrbObjRef
		Get
			If oLcFwObjRef Is Nothing Then
				ObjRefFw = Me
			Else 'If LW tested and LW required
				ObjRefFw = oLcFwObjRef
			End If
		End Get
	End Property
	
	'IIOP object key of the selected IOR profile
	Public ReadOnly Property objectKey() As String
		Get
			'Note: IIf(lSelectedProfile >= 0,,) evaluates every expression before decision!
			If lSelectedProfile >= 0 Then
				objectKey = mVBOrb.objKey2String(baObjKey)
			Else
				objectKey = ""
			End If
		End Get
	End Property
	
	Public ReadOnly Property objectKeyFw() As String
		Get
			objectKeyFw = Me.ObjRefFw.objectKey
		End Get
	End Property
	
	'If no profile is selected get all IIOP addresses of all IIOP profiles.
	'If no address is selected get all IIOP addresses of the selected IIOP profile.
	'Otherwise get selected IIOP address of the selected IIOP profile.
	'RET:   "" if there is no IIOP profile.
	Public ReadOnly Property IIOPAddress() As String
		Get
			IIOPAddress = ""
			Dim iVer As Short
			If lSelectedProfile >= 0 Then 'Profile selected?
				iVer = iIIOPVersion
				If iAddrSel >= 0 Then 'Address selected?
					IIOPAddress = IIOPAddress & ":"
					If iVer <> &H100 Then
						IIOPAddress = IIOPAddress & mVBOrb.GIOPVersion2Str(iVer) & "@"
					End If
					IIOPAddress = IIOPAddress & getHostPort(iAddrSel)
				Else
					iAddrSel = 0
					Do 
						IIOPAddress = IIOPAddress & Me.IIOPAddress
						iAddrSel = iAddrSel + 1
						If iAddrSel >= iAddrCnt Then
							Exit Do
						End If
						IIOPAddress = IIOPAddress & ","
					Loop 
					iAddrSel = -1
				End If
			Else
				Do While selectNextIIOPProfile()
					If IIOPAddress <> "" Then
						IIOPAddress = IIOPAddress & ","
					End If
					IIOPAddress = IIOPAddress & Me.IIOPAddress
				Loop 
			End If
		End Get
	End Property
	
	Public ReadOnly Property IIOPAddressFw() As String
		Get
			IIOPAddressFw = Me.ObjRefFw.IIOPAddress
		End Get
	End Property
	
	'IIOPVersion of the selected IOR profile
	'RET:   &H0 if no IOR profile is selected or if the profile is not an IIOP profile
	Public ReadOnly Property IIOPVersion() As Short
		Get
			IIOPVersion = IIf(lSelectedProfile >= 0, iIIOPVersion, &H0)
		End Get
	End Property
	
	'RET:   Use mVBOrb.GIOPVersion2Str(Me.IIOPVersionFw) if you like a string result
	Public ReadOnly Property IIOPVersionFw() As Short
		Get
			IIOPVersionFw = Me.ObjRefFw.IIOPVersion
		End Get
	End Property
	
	'Server Native Code Set, characters
	Public ReadOnly Property SNCSC() As Integer
		Get
			SNCSC = lSNCS_C
		End Get
	End Property
	
	Public ReadOnly Property SNCSCFw() As Integer
		Get
			SNCSCFw = Me.ObjRefFw.SNCSC
		End Get
	End Property
	
	'Server Native Code Set, wide characters
	Public ReadOnly Property SNCSW() As Integer
		Get
			SNCSW = lSNCS_W
		End Get
	End Property
	
	Public ReadOnly Property SNCSWFw() As Integer
		Get
			SNCSWFw = Me.ObjRefFw.SNCSW
		End Get
	End Property
	
	'Transmission Code Set for characters
	Friend ReadOnly Property TCSC() As Integer
		Get
			If mVBOrb.ONCSC = SNCSC Then 'CNCS-C = SNCS-C
				TCSC = mVBOrb.ONCSC 'no conversion required
			ElseIf SNCSC <> 0 Then 
				TCSC = SNCSC
			Else
				TCSC = mVBOrb.ONCSC 'fallback
			End If
		End Get
	End Property
	
	Public ReadOnly Property TCSCFw() As Integer
		Get
			TCSCFw = Me.ObjRefFw.TCSC
		End Get
	End Property
	
	'Transmission Code Set for wide characters
	Friend ReadOnly Property TCSW() As Integer
		Get
			If mVBOrb.ONCSW = SNCSW Then 'CNSC-W = SNCS-W
				TCSW = mVBOrb.ONCSW 'no conversion required
			ElseIf SNCSW <> 0 Then 
				TCSW = SNCSW
			Else
				TCSW = mVBOrb.ONCSW 'fallback
			End If
		End Get
	End Property
	
	Public ReadOnly Property TCSWFw() As Integer
		Get
			TCSWFw = Me.ObjRefFw.TCSW
		End Get
	End Property
	
	'Select an IIOP profile
	'IN:    ProfNo      0 = First profile
	Public Sub selectIIOPProfile(ByVal ProfNo As Integer)
		On Error GoTo ErrHandler
		If (iLogLevel And &H20) <> 0 Then
			Call oOrb.logMsg("D selectIIOPProfile " & CStr(ProfNo))
		End If
		'Nothing to do?
		If ProfNo = lSelectedProfile Then
			Exit Sub
		End If
		'Unselect old profile
		If lSelectedProfile >= 0 Then
			Call unselectProfile()
		End If
		'Select new profile
		If ProfNo < 0 Or ProfNo >= lTProfSeqLen Then
			Call mVBOrb.VBOrb.raiseBADPARAM(0, mVBOrb.VBOrb.CompletedNO, "Out of bounds 0 <= " & CStr(ProfNo) & " < " & CStr(lTProfSeqLen))
		End If
		If TProfs(ProfNo).ptag <> TAG_INTERNET_IOP Then
			Call mVBOrb.VBOrb.raiseINTERNAL(0, mVBOrb.VBOrb.CompletedNO, "Profile " & CStr(ProfNo) & " is not an IIOP profile")
		End If
		iLcFwCnt = 0
		iAddrCnt = 0
		Dim oPIn As cOrbStream
		oPIn = New cOrbStream
		'Call oPIn.initByOctets(TProfs(ProfNo).pdata, TProfs(ProfNo).pdatlen, oOrb)
		Call oPIn.initStream(oOrb, &H100, TProfs(ProfNo).pdatlen)
		Call oPIn.writeOctets(TProfs(ProfNo).pdata, TProfs(ProfNo).pdatlen)
		Call oPIn.setPos(0) 'Similar to sendGIOPToReadAgain()
		'sequence <octet> profile_data
		Call oPIn.readEncapOpen(TProfs(ProfNo).pdatlen)
		'{octet major; octet minor} iiop_version;
		iIIOPVersion = CShort(oPIn.readOctet()) * &H100 'IIOPVerMajor
		iIIOPVersion = iIIOPVersion + CShort(oPIn.readOctet()) 'IIOPVerMinor
		If iIIOPVersion >= &H100 And iIIOPVersion <= &H103 Then
			'string host; unsigned short port;
			sHosts(iAddrCnt) = oPIn.readString(&H1000000)
			iPorts(iAddrCnt) = oPIn.readUshort()
			'A target that supports only protected IIOP invocations (SSL)
			'will have a port number of 0 (zero) here.
			iAddrCnt = iAddrCnt + 1
			'sequence <octet> object_key;
			Call oPIn.readSeqOctet(baObjKey)
		Else
			Call mVBOrb.VBOrb.raiseBADPARAM(8, mVBOrb.VBOrb.CompletedNO, "IIOP version " & Hex(iIIOPVersion) & " is unsupported")
		End If
		'Optional
		Dim iC, TCLen As Integer
		Dim tagC As Integer
		Dim CDLen As Integer
		Dim lCCSi, lCCSLen As Integer
		Dim lSCCS_C, lSCCS_W As Integer
		If iIIOPVersion >= &H101 And iIIOPVersion <= &H103 Then
			'sequence <IOP::TaggedComponent> components;
			TCLen = oPIn.readUlong()
			For iC = 0 To TCLen - 1
				'ComponentId tag
				tagC = oPIn.readUlong()
				'sequence <octet> component_data;
				CDLen = oPIn.readUlong()
				If tagC = TAG_ALTERNATE_IIOP_ADDRESS Then
					Call oPIn.readEncapOpen(CDLen)
					'string HostID; unsigned short Port;
					sHosts(iAddrCnt) = oPIn.readString(&H1000000)
					iPorts(iAddrCnt) = oPIn.readUshort()
					iAddrCnt = iAddrCnt + 1
					Call oPIn.readEncapClose()
				ElseIf tagC = TAG_CODE_SETS Then 
					Call oPIn.readEncapOpen(CDLen) 'Encapsulation of CodeSetComponentInfo
					'CodeSetComponent for char type
					lSNCS_C = oPIn.readUlong() 'CodeSetId native_code_set;
					lCCSLen = oPIn.readUlong() 'sequence<CodeSetId> c_c_sets;
					For lCCSi = 1 To lCCSLen
						lSCCS_C = oPIn.readUlong() 'CodeSetId conversion_code_set;
						If (iLogLevel And &H20) <> 0 Then
							Call oOrb.logMsg("D SCCS-C= " & Hex(lSCCS_C))
						End If
					Next lCCSi
					'CodeSetComponent for wchar type
					lSNCS_W = oPIn.readUlong() 'CodeSetId native_code_set;
					lCCSLen = oPIn.readUlong() 'sequence<CodeSetId> c_c_sets;
					For lCCSi = 1 To lCCSLen
						lSCCS_W = oPIn.readUlong() 'CodeSetId conversion_code_set;
						If (iLogLevel And &H20) <> 0 Then
							Call oOrb.logMsg("D SCCS-W= " & Hex(lSCCS_W))
						End If
					Next lCCSi
					Call oPIn.readEncapClose()
					If (iLogLevel And &H20) <> 0 Then
						Call oOrb.logMsg("D SNCS-C= " & Hex(lSNCS_C) & ", SNCS-W= " & Hex(lSNCS_W))
					End If
				Else
					'Skipping unknown component tag
					Call oPIn.readSkip(CDLen)
				End If
			Next iC
		End If
		Call oPIn.readEncapClose()
		Call oPIn.destroy()
		bIsInitByURL = TProfs(ProfNo).bPIsInitByURL
		
		lSelectedProfile = ProfNo
		iAddrSel = -1
		Exit Sub
ErrHandler: 
		Call mVBOrb.ErrReraise("selectIIOPProfile(" & CStr(ProfNo) & ")")
	End Sub
	
	Public Sub unselectProfile()
		On Error GoTo ErrHandler
		If (iLogLevel And &H20) <> 0 Then
			Call oOrb.logMsg("D unselectProfile")
		End If
		'Nothing to do?
		If lSelectedProfile < 0 Then
			Exit Sub
		End If
		
		iLcFwCnt = 0
		If Not oLcFwObjRef Is Nothing Then
			Call oLcFwObjRef.releaseMe()
			'UPGRADE_NOTE: Object oLcFwObjRef may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
			oLcFwObjRef = Nothing
		End If
		Erase baObjKey
		iAddrCnt = 0
		bIsInitByURL = False
		lSelectedProfile = -1
		
		Exit Sub
ErrHandler: 
		Call mVBOrb.ErrReraise("unselectProfile")
	End Sub
	
	'Select next IIOP profile or unselect if end of profiles reached
	'GI/O:  lSelectedProfile        -1 = Not selected, 0 = First profile
	Private Function selectNextIIOPProfile() As Boolean
		Dim iProfNo As Short
		iProfNo = lSelectedProfile + 1
		Do While iProfNo < lTProfSeqLen
			If TProfs(iProfNo).ptag = TAG_INTERNET_IOP Then
				Call selectIIOPProfile(iProfNo)
				selectNextIIOPProfile = True
				Exit Function
			End If
			iProfNo = iProfNo + 1
		Loop 
		'Unselect
		If lSelectedProfile >= 0 Then
			Call unselectProfile()
		End If
		selectNextIIOPProfile = False
	End Function
	
	'is_nil(), Null object references are indicated by an empty set of profiles,
	'and by a NullTypeID
	Public Function isNil() As Boolean
		isNil = (sTypeId = sNullTypeId And lTProfSeqLen = 0)
	End Function
	
	'is_a(), Equivalence Checking Operation
	Public Function isA(ByRef sRepId As String) As Boolean
		On Error GoTo ErrHandler
		If sTypeId = sRepId Then
			isA = True
			Exit Function
		End If
		Dim oRequest As cOrbRequest
		oRequest = Request("_is_a", False)
		Dim oOut As cOrbStream
		oOut = oRequest.InArg
		Call oOut.writeString(sRepId)
		Dim oIn As cOrbStream
		Call oRequest.invokeReqst(False)
		oIn = oRequest.OutRes
		isA = oIn.readBoolean()
		Exit Function
ErrHandler: 
		Call mVBOrb.ErrReraise("isA")
	End Function
	
	'interface helper
	Public Sub narrow(ByRef RepId As String, ByVal Check As Boolean, ByVal isLocal As Boolean)
		'On Error GoTo ErrHandler
		If Check Then
			If Not isA(RepId) Then
				Call mVBOrb.VBOrb.raiseUNKNOWN(0, mVBOrb.VBOrb.CompletedNO, "Cannot narrow " & IIf(sTypeId = sNullTypeId, "Object", "[" & sTypeId & "]") & " to [" & RepId & "]")
			End If
		End If
		If sTypeId = sNullTypeId Then
			sTypeId = RepId
			bIsLocal = isLocal
		End If
		'    Exit Sub
		'ErrHandler:
		'    Call mVBOrb.ErrReraise("narrow")
	End Sub
	
	'non_existent(), Probing for Object Non-Existence
	Public Function nonExistent() As Boolean
		On Error GoTo ErrHandler
		Dim oRequest As cOrbRequest
		oRequest = Request("_non_existent", False)
		Dim oOut As cOrbStream
		oOut = oRequest.InArg
		Dim oIn As cOrbStream
		Call oRequest.invokeReqst(False)
		oIn = oRequest.OutRes
		nonExistent = oIn.readBoolean()
		Exit Function
ObjNotExist: 'If location forward request fails
		nonExistent = True
		Exit Function
ErrHandler: 
		If mVBOrb.ErrIsSystemEx() And Err.Number = (mVBOrb.ITF_E_OBJECT_NOT_EXIST_NO Or vbObjectError) Then
			Resume ObjNotExist
		End If
		Call mVBOrb.ErrReraise("nonExistent")
	End Function
	
	Public Sub setRebindMode(ByVal RebindMode As Short)
		iRebindMode = RebindMode
	End Sub
	
	'Get, Bind or ReBind to a profile.
	'Function is not private to allow calling with oLcFwObjRef
	'I/O:   AutoProfBind    IN: Bind or ReBind if unbound / OUT: New binding?
	Friend Sub bindProfile(ByRef AutoProfBind As Boolean)
		On Error GoTo ErrHandler
		Dim AutoConnect As Boolean
		Dim SameProcess As Boolean
		Dim oSock As cOrbSocket
		'Try last profile
		Dim lErrProfile As Integer
		lErrProfile = -1
		If lSelectedProfile >= 0 Then
			On Error Resume Next
			AutoConnect = True
			oSock = getProfileConn(AutoConnect, SameProcess)
			If Err.Number = 0 Then
				On Error GoTo ErrHandler
				AutoProfBind = False
				Exit Sub 'Return with last used profile
			End If
			Call mVBOrb.ErrSave()
			lErrProfile = lSelectedProfile
			On Error GoTo ErrLog
			Call unselectProfile()
			On Error GoTo ErrHandler
		End If
		If Not AutoProfBind Then
			If lErrProfile >= 0 Then GoTo ErrLoadRaise 'Propagate first error
			Call mVBOrb.VBOrb.raiseREBIND(0, mVBOrb.VBOrb.CompletedNO, "REBIND required")
		End If
		'Select the first IIOP profile of current object reference
		If Not selectNextIIOPProfile() Then
			Call mVBOrb.VBOrb.raiseIMPLIMIT(1, mVBOrb.VBOrb.CompletedNO, "Unable to use any profile in IOR.")
		End If
		Do  'Try all IIOP profile(s) of current object reference except lErrProfile
			If lSelectedProfile <> lErrProfile Then
				On Error Resume Next
				AutoConnect = True
				oSock = getProfileConn(AutoConnect, SameProcess)
				If Err.Number = 0 Then
					On Error GoTo ErrHandler
					Exit Do 'Usefull IIOP profile found
				End If
				If lErrProfile < 0 Then
					Call mVBOrb.ErrSave()
					lErrProfile = lSelectedProfile
				End If
			End If
			If Not selectNextIIOPProfile() Then
				GoTo ErrLoadRaise 'No usefull IIOP profile found
			End If
		Loop 
		If lErrProfile >= 0 Then
			Call mVBOrb.ErrLoad() 'Load and ignore error
		End If
		AutoProfBind = True
		Exit Sub
ErrLog: 
		Call oOrb.logErr("cOrbObjRef.bindProfile")
		Resume Next
ErrHandler: 
		If lErrProfile >= 0 Then
			Call mVBOrb.ErrReplace()
		Else
			Call mVBOrb.ErrSave()
		End If
		Resume ErrLoadRaise
ErrLoadRaise: 
		On Error GoTo 0
		Call mVBOrb.ErrLoad()
		Call mVBOrb.ErrReraise("bindProfile")
	End Sub
	
	'Get, Bind or ReBind to a profile.
	'Function is not private to allow calling with oLcFwObjRef
	'I/O:   AutoProfBind    IN: Bind or ReBind if unbound / OUT: New binding?
	Private Sub bindProfileFw(ByRef AutoProfBind As Boolean, ByVal OneWay As Boolean)
		On Error GoTo ErrHandler
		If Not AutoProfBind Then
			If oLcFwObjRef Is Nothing Then
				Call bindProfile(AutoProfBind)
			Else
				Call oLcFwObjRef.bindProfile(AutoProfBind)
			End If
			Exit Sub
		End If
		If Not oLcFwObjRef Is Nothing Then
			On Error Resume Next
			Call oLcFwObjRef.bindProfile(AutoProfBind)
			If Err.Number = 0 Then
				On Error GoTo ErrHandler
				Exit Sub
			End If
			'If not Permanent???
			iLcFwCnt = 0
			Call oLcFwObjRef.releaseMe()
			'UPGRADE_NOTE: Object oLcFwObjRef may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
			oLcFwObjRef = Nothing
			On Error GoTo ErrHandler
		End If
		Call bindProfile(AutoProfBind)
		'If not LW tested and Me.IIOPVersion < &h102 then LW test here
		'because otherwise Request Body remarshalling would be required
		Dim oOut As cOrbStream
		Dim lwRequest As cOrbRequest
		Dim lwRequired As Boolean
		Dim oIn As cOrbStream
		Dim newObjRef As cOrbObjRef
		If iLcFwCnt = 0 And iRebindMode = 0 Then
			If (OneWay Or iIIOPVersion < &H102 Or bIsInitByURL) And (Not OneWay Or oOrb.OnewayRebind) Then
				'Locate Request
				oOut = New cOrbStream
				Do 
					If iLcFwCnt > LOCATION_FORWARD_LIMIT Then
						Call mVBOrb.VBOrb.raiseIMPLIMIT(99, mVBOrb.VBOrb.CompletedNO, "LOCATION_FORWARD_LIMIT")
						'Exit Do 'Break endless loop
					End If
					lwRequest = New cOrbRequest
					Call lwRequest.initOutRequest(oOrb, Me, "", True, oOrb.getNextReqId(), oOut)
					Call writeLocateReqstFw(oOut, lwRequest)
					'Call oOut.destroy 'in invokeLocateReqst()
					lwRequired = lwRequest.invokeReqst(True)
					iLcFwCnt = iLcFwCnt + 1
					If Not lwRequired Then
						Exit Do
					End If
					'readLocateReqstBody()
					oIn = lwRequest.OutRes
					newObjRef = oIn.readObject()
					'Call newObjRef.narrow(sTypeId)
					If Not oLcFwObjRef Is Nothing Then Call oLcFwObjRef.releaseMe()
					oLcFwObjRef = newObjRef
					AutoProfBind = True
					Call oLcFwObjRef.bindProfile(AutoProfBind)
				Loop 
			End If
		End If
		Exit Sub
ErrHandler: 
		Call mVBOrb.ErrReraise("bindProfileFw")
	End Sub
	
	'Create request object and write request header
	'(Do not call with oLcFwObjRef)
	'IN:    Operation   Name of the operation
	'IN:    OneWay      Is a oneway operation? (no response expected?)
	'RET:               Request object
	Public Function Request(ByRef Operation As String, ByVal OneWay As Boolean) As cOrbRequest
		On Error GoTo ErrHandler
		
		Dim AutoProfBind As Boolean
		AutoProfBind = True
		Call bindProfileFw(AutoProfBind, OneWay)
		
		Dim oOut As cOrbStream
		oOut = New cOrbStream
		
		'Prepare GIOP Message Header
		Request = New cOrbRequest
		Call Request.initOutRequest(oOrb, Me, Operation, Not OneWay, oOrb.getNextReqId(), oOut)
		
		Call writeReqstHeadFw(oOut, Request)
		Exit Function
ErrHandler: 
		Call mVBOrb.ErrReraise("request")
	End Function
	
	'(Do not call with oLcFwObjRef)
	'IN:    oOut        New cOrbStream
	'IN:    oRequest
	Private Sub writeLocateReqstFw(ByVal oOut As cOrbStream, ByVal oRequest As cOrbRequest)
		On Error GoTo ErrHandler
		
		'GIOPVersion may be lower than IIOPVersion
		Call oOut.initStream(oOrb, Me.IIOPVersionFw)
		Call oOut.writeGIOPHead()
		
		'Write GIOP LocateRequest Header
		'unsigned long request_id
		Call oOut.writeUlong(oRequest.ReqId)
		'Write an object key of a request or a locate request
		If oLcFwObjRef Is Nothing Then
			Call writeTargetAdrOfMe(oOut)
		Else
			Call oLcFwObjRef.writeTargetAdrOfMe(oOut)
		End If
		Exit Sub
ErrHandler: 
		Call mVBOrb.ErrReraise("writeLocateReqstFw")
	End Sub
	
	'(Do not call with oLcFwObjRef)
	'IN:    oOut        New cOrbStream
	Private Sub writeReqstHeadFw(ByVal oOut As cOrbStream, ByVal oRequest As cOrbRequest)
		On Error GoTo ErrHandler
		
		'GIOPVersion may be lower than IIOPVersion
		Call oOut.initStream(oOrb, Me.IIOPVersionFw, TCS_C:=Me.TCSCFw, TCS_W:=Me.TCSWFw)
		Call oOut.writeGIOPHead()
		
		'Write GIOP Request Header
		If oOut.getGIOPVersion <> &H102 Then
			'IOP::ServiceContextList service_context;
			Call writeReqstServiceContextFw(oOut)
		End If
		'request_id;
		Call oOut.writeUlong(oRequest.ReqId)
		'ResponseFlags: &H0 = oneway, &H1 = SyncScope.WITH_SERVER, &H3 = SyncScope.WITH_TARGET
		If oOut.getGIOPVersion <> &H102 Then
			'response_expected;
			Call oOut.writeBoolean(oRequest.ResponseFlags <> &H0)
		Else
			'response_flags;
			Call oOut.writeOctet(oRequest.ResponseFlags)
		End If
		If oOut.getGIOPVersion <> &H100 Then
			'octet reserved[3];
			Call oOut.writeOctet(0)
			Call oOut.writeOctet(0)
			Call oOut.writeOctet(0)
		End If
		'Write an object key of a request or a locate request
		If oLcFwObjRef Is Nothing Then
			Call writeTargetAdrOfMe(oOut)
		Else
			Call oLcFwObjRef.writeTargetAdrOfMe(oOut)
		End If
		'operation;
		Call oOut.writeString((oRequest.Operation))
		If oOut.getGIOPVersion() <> &H102 Then
			'Principal (not in GIOP 1.2)
			Call oOut.writeUlong(0)
		Else
			'IOP::ServiceContextList service_context;
			Call writeReqstServiceContextFw(oOut)
			'In GIOP version 1.2, the Request Body is always aligned on an 8-octet
			'boundary. The fact that GIOP specifies the maximum alignment for any
			'primitive type is 8 guarantees that the Request Body will not require
			'remarshalling if the Message or Request header are modified. The data
			'for the request body includes the following items:
			'• All in and inout parameters
			'• An optional Context pseudo object
			Call oOut.writeAlign(8)
		End If
		Exit Sub
ErrHandler: 
		Call mVBOrb.ErrReraise("writeReqstHeadFw")
	End Sub
	
	'Write an object key of a request or a locate request
	'Function is not private to allow calling with oLcFwObjRef
	'IN:    oOut        cOrbStream
	'GIN:   iAddrDisp
	Friend Sub writeTargetAdrOfMe(ByVal oOut As cOrbStream)
		On Error GoTo ErrHandler
		If oOut.getGIOPVersion() <> &H102 Then
			'sequence <octet> object_key;
			Call oOut.writeSeqOctet(baObjKey, UBound(baObjKey) + 1)
		Else
			'TargetAddress target;
			Call oOut.writeShort(iAddrDisp)
			Select Case iAddrDisp
				Case 0 'KeyAddr, sequence <octet> object_key;
					Call oOut.writeSeqOctet(baObjKey, UBound(baObjKey) + 1)
				Case 1 'ProfileAddr, IOP::TaggedProfile profile;
					'ProfileId tag
					Call oOut.writeUlong(TProfs(lSelectedProfile).ptag)
					'sequence <octet> profile_data
					Call oOut.writeUlong(TProfs(lSelectedProfile).pdatlen)
					Call oOut.writeOctets(TProfs(lSelectedProfile).pdata, TProfs(lSelectedProfile).pdatlen)
				Case 2 'ReferenceAddr, unsigned long selected_profile_index; IOP::IOR ior;
					Call oOut.writeUlong(lSelectedProfile) 'Starting by 0 or 1???
					Call writeMe(oOut, sTypeId)
				Case Else
					Call mVBOrb.VBOrb.raiseMARSHAL(1, mVBOrb.VBOrb.CompletedNO, "Unknown AddressingDisposition: " & CStr(iAddrDisp))
			End Select
		End If
		Exit Sub
ErrHandler: 
		Call mVBOrb.ErrReraise("writeTargetAdrOfMe")
	End Sub
	
	Private Function getHostPort(ByVal n As Short) As String
		Dim lPort As Integer
		lPort = iPorts(n)
		If lPort < 0 Then
			lPort = lPort + &H10000
		End If
		getHostPort = sHosts(n) & ":" & lPort
	End Function
	
	'Get, Open or ReOpen a connection of the selected profile.
	'Function is not private to allow calling with oLcFwObjRef
	'I/O:   AutoConnect IN: Connect or ReConnect if disconnected / OUT: New connection?
	'I/O:   SameProcess
	'RET:               Socket object or Nothing e.g. if same process
	Friend Function getProfileConn(ByRef AutoConnect As Boolean, ByRef SameProcess As Boolean) As cOrbSocket
		On Error GoTo ErrHandler
		Dim AutoOpen As Boolean
		Dim oSock As cOrbSocket
		Dim iErrAddr As Short
		iErrAddr = -1
		'Try last connection
		If lOrbConnId >= 0 Then
			On Error Resume Next
			AutoOpen = AutoConnect
			oSock = oOrb.ConnGet(SameProcess, lOrbConnId, AutoOpen)
			If Err.Number = 0 Then
				On Error GoTo ErrHandler
				AutoConnect = AutoOpen
				getProfileConn = oSock
				Exit Function
			End If
			Call mVBOrb.ErrSave()
			iErrAddr = iAddrSel
			If iLogLevel >= 4 Then
				Call oOrb.logMsg("I " & Err.Description)
			End If
			On Error GoTo ErrLog
			Call oOrb.ConnOCIdFree(lOrbConnId, False)
			lOrbConnId = -1
			On Error GoTo ErrHandler
		End If
		If Not AutoConnect Then
			If iErrAddr >= 0 Then GoTo ErrLoadRaise 'Propagate first error
			Call mVBOrb.VBOrb.raiseREBIND(0, mVBOrb.VBOrb.CompletedNO, "RECONNECT required")
		End If
		'Try (other) addresses of current profile to make a new connection
		If iAddrCnt = 0 Then
			Call mVBOrb.VBOrb.raiseREBIND(99, mVBOrb.VBOrb.CompletedNO, "Profile has no IIOP address")
		End If
		iAddrSel = 0
		Do 
			If iAddrSel <> iErrAddr Then
				lOrbConnId = oOrb.ConnOCIdAlloc(getHostPort(iAddrSel))
				On Error Resume Next
				AutoOpen = True
				oSock = oOrb.ConnGet(SameProcess, lOrbConnId, AutoOpen)
				If Err.Number = 0 Then
					On Error GoTo ErrHandler
					Exit Do
				End If
				If iErrAddr < 0 Then
					Call mVBOrb.ErrSave()
					iErrAddr = iAddrSel
				End If
				If iLogLevel >= 4 Then
					Call oOrb.logMsg("I " & Err.Description)
				End If
				On Error GoTo ErrLog
				Call oOrb.ConnOCIdFree(lOrbConnId, False)
				lOrbConnId = -1
				On Error GoTo ErrHandler
			End If
			iAddrSel = iAddrSel + 1
			If iAddrSel >= iAddrCnt Then
				iAddrSel = -1
				GoTo ErrLoadRaise
			End If
		Loop 
		If iErrAddr >= 0 Then
			Call mVBOrb.ErrLoad()
		End If
		AutoConnect = True
		getProfileConn = oSock
		Exit Function
ErrLog: 
		Call oOrb.logErr("cOrbObjRef.getProfileConn")
		Resume Next
ErrHandler: 
		If iErrAddr >= 0 Then
			Call mVBOrb.ErrReplace()
		Else
			Call mVBOrb.ErrSave()
		End If
		Resume ErrLoadRaise
ErrLoadRaise: 
		On Error GoTo 0
		Call mVBOrb.ErrLoad()
		Call mVBOrb.ErrReraise("getProfileConn")
	End Function
	
	'Send a request or a locate request to address of ObjRef
	'Function is not private to allow calling with oLcFwObjRef
	'IN:    msgType     0 = Request, 2 = CancelRequest, 3 = LocateRequest
	'I/O:   bReConnect  IN: Allow ReConnect / OUT: ReConnected
	Friend Function sendReqst(ByVal Request As cOrbRequest, ByVal msgType As Byte, ByRef bReConnect As Boolean) As cOrbSocket
		On Error GoTo ErrHandler
		Dim bSameProcess As Boolean
		Dim oSock As cOrbSocket
		oSock = getProfileConn(bReConnect, bSameProcess)
		If bSameProcess Then
			'same process (co-location)
			Call colocationMsg(Request, msgType)
			'UPGRADE_NOTE: Object sendReqst may not be destroyed until it is garbage collected. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="6E35BFF6-CD74-4B09-9689-3E1A43DF8969"'
			sendReqst = Nothing
			Exit Function
		End If
		On Error Resume Next
		Dim oOut As cOrbStream
		oOut = Request.InArg
		Call oOut.sendGIOPToSocket(msgType, oSock)
		If Err.Number <> 0 Then
			Call mVBOrb.ErrSave()
			'WSAECONNABORTED or WSAECONNRESET
			If Err.Number = vbObjectError + 10053 Or Err.Number = vbObjectError + 10054 Then
				On Error GoTo ErrLog
				'If you send oneway only, then "Connection aborted" maybe raised.
				'To raise "connection refused" next time:
				Call oOrb.ConnOCIdFree(lOrbConnId, False)
				lOrbConnId = -1
			End If
			'On Error GoTo 0 : Call mvborb.ErrLoad
			GoTo ErrLoadRaise
		End If
		sendReqst = oSock
		Exit Function
ErrLog: 
		Call oOrb.logErr("cOrbObjRef.sendReqst")
		Resume Next
ErrHandler: 
		Call mVBOrb.ErrSave()
		Resume ErrLoadRaise
ErrLoadRaise: 
		On Error GoTo 0
		Call mVBOrb.ErrLoad()
		Call mVBOrb.ErrReraise("sendReqst")
	End Function
	
	'IN:    msgType      0 = Request, 2 = CancelRequest, 3 = LocateRequest
	Private Sub colocationMsg(ByVal Request As cOrbRequest, ByVal msgType As Byte)
		On Error GoTo ErrHandler
		'Receive GIOP Message Header
		Dim oIn As cOrbStream
		oIn = Request.InArg
		Call oIn.sendGIOPToReadAgain()
		Dim oOut As cOrbStream
		Select Case msgType
			Case 0 '= GIOP Request received
				oOut = Request.replyRequest()
				If Not oOut Is Nothing Then
					'1 = Return Reply
					Call oOut.sendGIOPToReadAgain()
					Call Request.setRes(1, oOut)
				End If
				'Case 1 '= GIOP Reply received
				'Case 2 '= CancelRequest received
			Case 3 '= Locate Request received
				oOut = New cOrbStream
				Call oOrb.replyLocateRequest(oIn, oOut)
				If Not oOut Is Nothing Then
					'4 = Send LocateReply
					Call oOut.sendGIOPToReadAgain()
					Call Request.setRes(4, oOut)
				End If
				'Case 4 '= GIOP LocateReply received
				'Case 5 '5 = GIOP CloseConnection received
				'Case 6 '= MessageError received
				'case 7'= Fragment received
			Case Else
				Call mVBOrb.ErrRaise(1, "Unexpected GIOP msgType: " & msgType)
		End Select
		Exit Sub
ErrHandler: 
		Call mVBOrb.ErrReraise("colocationMsg")
	End Sub
	
	'Receive a Reply or a LocateReply from target address of ObjRef
	'Function is not private to allow calling with oLcFwObjRef
	'IN:    tryLastConn If connection is down return false
	'RET:   Connection was open, Reply received
	Friend Function recvReply(ByVal Request As cOrbRequest, ByVal tryLastConn As Boolean) As Boolean
		If Request.isRes Then 'e.g. if in same process
			recvReply = True
			Exit Function
		End If
		On Error Resume Next
		Call oOrb.ConnReqWait(lOrbConnId, Request) 'is calling Request.setRes()
		If Err.Number <> 0 Then
			Call mVBOrb.ErrSave()
			'Close connection to get a new connection next time
			If Err.Number = vbObjectError + 10058 And tryLastConn Then 'WSAESHUTDOWN
				On Error GoTo ErrLog
				'Connection was closed by Server
				Call oOrb.ConnOCIdFree(lOrbConnId, False)
				lOrbConnId = -1
				Call mVBOrb.ErrLoad() 'Ignore old Error
				recvReply = False
				Exit Function
			End If
			'On Error GoTo 0 : Call mvborb.ErrLoad
			GoTo ErrLoadRaise
		End If
		If tryLastConn And Request.getResType() = 5 Then
			recvReply = False
			Call Request.setRes(-1, Nothing)
		Else
			recvReply = True
		End If
		Exit Function
ErrLog: 
		Call oOrb.logErr("cOrbObjRef.recvReply")
		Resume Next
ErrLoadRaise: 
		On Error GoTo 0
		Call mVBOrb.ErrLoad()
		Call mVBOrb.ErrReraise("recvReply")
	End Function
	
	'Is called by Request.invokeReqst(), (Do not call with oLcFwObjRef)
	'OUT:   Request.OutRes  Reply or Nothing
	'RET:                   New IOR to read?
	Public Function invokeLocateReqst(ByVal Request As cOrbRequest) As Boolean
		On Error GoTo ErrHandler
		Dim bReConnect As Boolean
		Dim tryToRecv As Boolean
		Dim oSock As cOrbSocket
tryAgain: 
		bReConnect = True
		'If LW tested and LW required then do it
		If oLcFwObjRef Is Nothing Then
			oSock = sendReqst(Request, 3, bReConnect) '3 = LocateRequest
			tryToRecv = Not bReConnect
			If Not recvReply(Request, tryToRecv) Then
				tryToRecv = False
				GoTo tryAgain
			End If
		Else
			oSock = oLcFwObjRef.sendReqst(Request, 3, bReConnect) '3 = LocateRequest
			tryToRecv = Not bReConnect
			If Not oLcFwObjRef.recvReply(Request, tryToRecv) Then
				tryToRecv = False
				GoTo tryAgain
			End If
		End If
		
		'Receive GIOP Message Header
		Dim ReqId, locStatus As Integer
		Dim oIn As cOrbStream
		oIn = Request.OutRes
		'readLocateReplyHeader():
		'unsigned long request_id;
		ReqId = oIn.readUlong()
		'enum LocateStatusType;
		locStatus = oIn.readUlong()
#If IIOP12a Then
		'UPGRADE_NOTE: #If #EndIf block was not upgraded because the expression IIOP12a did not evaluate to True or was not evaluated. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="27EE2C3C-05AF-4C04-B2AF-657B4FB6B5FC"'
		'Locate Reply Body alignment was erroneous added in CORBA 2.4/2.5
		'See also cOrbImpl.writeLocateReqstBody()
		If oIn.getGIOPVersion = &H102 Then Call oIn.readAlign(8)
#End If
		
		If ReqId <> Request.ReqId Then
			Call mVBOrb.VBOrb.raiseCOMMFAILURE(1, mVBOrb.VBOrb.CompletedNO, "Unexpected RequestId: " & ReqId & " <> " & CStr(Request.ReqId))
		End If
		
		Select Case locStatus
			Case 0 '= UNKNOWN_OBJECT
				If oSock Is Nothing Then
					Call mVBOrb.VBOrb.raiseOBJECTNOTEXIST(1, mVBOrb.VBOrb.CompletedNO, "UNKNOWN_OBJECT")
				Else
					Call mVBOrb.VBOrb.raiseOBJECTNOTEXIST(1, mVBOrb.VBOrb.CompletedNO, "UNKNOWN_OBJECT at " & oSock.socketHost & ":" & oSock.socketPort)
				End If
			Case 1 '= OBJECT_HERE
				invokeLocateReqst = False
			Case 2 '= OBJECT_FORWARD
				invokeLocateReqst = True
			Case 3 '= OBJECT_FORWARD_PERM then Me.initByIOR???
				invokeLocateReqst = True
			Case 4 '= LOC_SYSTEM_EXCEPTION
				If oSock Is Nothing Then
					Call mVBOrb.readRaiseSystemEx(oIn, "ReqId= " & CStr(ReqId))
				Else
					Call mVBOrb.readRaiseSystemEx(oIn, "Received from " & oSock.socketHost & ":" & oSock.socketPort & ", ReqId= " & CStr(ReqId))
				End If
				'Case 5 '=LOC_NEEDS_ADDRESSING_MODE
				'???iAddrDisp=
			Case Else
				Call mVBOrb.VBOrb.raiseMARSHAL(1, mVBOrb.VBOrb.CompletedNO, "Unknown locStatus: " & CStr(locStatus))
		End Select
		Call Request.InArg.destroy()
		Exit Function
ErrLog: 
		Call oOrb.logErr("cOrbObjRef.invokeLocateReqst")
		Resume Next
ErrHandler: 
		Call mVBOrb.ErrSave()
		If iLogLevel >= 4 Then
			Call oOrb.logMsg("I " & Err.Description)
		End If
		Resume ErrCleanLoadRaise
ErrCleanLoadRaise: 
		On Error GoTo ErrLog
		Call Request.InArg.destroy()
		On Error GoTo 0
		Call mVBOrb.ErrLoad()
		Call mVBOrb.ErrReraise("invokeLocateReqst")
	End Function
	
	'Is called by Request.invokeReqst(), (Do not call with oLcFwObjRef)
	'The method name "invoke" is not allowed in VB
	'IN:    Request.InArg   Request
	'OUT:   Request.OutRes  Reply or Nothing
	'RET:                   CORBA User Exception occured? (repl_status = 1)
	Public Function invokeReqst(ByVal Request As cOrbRequest) As Boolean
		On Error GoTo ErrHandler
		'locate requests if iLcFwCnt = 0 and message.size > xxx???
		Dim bReConnect As Boolean
		Dim tryToRecv As Boolean
		Dim oSock As cOrbSocket
tryAgain: 
		bReConnect = True
		'If LW tested and LW required then do it
		If oLcFwObjRef Is Nothing Then
			oSock = sendReqst(Request, 0, bReConnect) '0 = Request
			'oneway?
			If Request.ResponseFlags = &H0 Then
				invokeReqst = False
				Exit Function
			End If
			tryToRecv = Not bReConnect
			If Not recvReply(Request, tryToRecv) Then
				tryToRecv = False
				GoTo tryAgain
			End If
		Else
			oSock = oLcFwObjRef.sendReqst(Request, 0, bReConnect) '0 = Request
			'oneway?
			If Request.ResponseFlags = &H0 Then
				invokeReqst = False
				Exit Function
			End If
			tryToRecv = Not bReConnect
			If Not oLcFwObjRef.recvReply(Request, tryToRecv) Then
				tryToRecv = False
				GoTo tryAgain
			End If
		End If
		'Receive GIOP Message Header
		Dim ReqId As Integer
		Dim repStatus As Integer
		Dim oIn As cOrbStream
		oIn = Request.OutRes
		'readReplyHeader():
		If oIn.getGIOPVersion < &H102 Then
			'IOP::ServiceContextList service_context;
			Call readReplyServiceContextFw(oIn)
			'unsigned long request_id;
			ReqId = oIn.readUlong()
			'ReplyStatusType reply_status;
			repStatus = oIn.readUlong()
		Else
			'unsigned long request_id;
			ReqId = oIn.readUlong()
			'ReplyStatusType reply_status;
			repStatus = oIn.readUlong()
			'IOP::ServiceContextList service_context;
			Call readReplyServiceContextFw(oIn)
			'In GIOP version 1.2, the Reply Body is always aligned on an 8-octet
			'boundary. See also cOrbImpl.writeReplyHeader()
			Call oIn.readAlign(8)
		End If
		
		If ReqId <> Request.ReqId Then
			Call mVBOrb.ErrRaise(1, "Unexpected RequestId: " & ReqId & " <> " & Request.ReqId)
		End If
		
		Dim newObjRef As cOrbObjRef
		Dim newRequest As cOrbRequest
		Dim oOut As cOrbStream
		Dim newOut As cOrbStream
		If repStatus = 0 Then
			'NO_EXCEPTION
			invokeReqst = False
		ElseIf repStatus = 1 Then 
			'USER_EXCEPTION
			invokeReqst = True
		ElseIf repStatus = 2 Then 
			'SYSTEM_EXCEPTION
			If oSock Is Nothing Then
				Call mVBOrb.readRaiseSystemEx(oIn, "Received, ReqId= " & CStr(ReqId))
			Else
				Call mVBOrb.readRaiseSystemEx(oIn, "Received from " & oSock.socketHost & ":" & oSock.socketPort & ", ReqId= " & CStr(ReqId))
			End If
		ElseIf repStatus = 3 Or repStatus = 4 Then 
			'LOCATION_FORWARD, LOCATION_FORWARD_PERM
			If iLcFwCnt > LOCATION_FORWARD_LIMIT Then
				Call mVBOrb.VBOrb.raiseIMPLIMIT(99, mVBOrb.VBOrb.CompletedNO, "LOCATION_FORWARD_LIMIT")
			End If
			iLcFwCnt = iLcFwCnt + 1
			'Get the new complete IOR
			'if LOCATION_FORWARD_PERM then Me.initByIOR???
			newObjRef = oIn.readObject()
			'Call newObjRef.narrow(sTypeId)
			If Not oLcFwObjRef Is Nothing Then Call oLcFwObjRef.releaseMe()
			oLcFwObjRef = newObjRef
			'If we get a LOCATION_FORWARD, we need to re-send the request and
			'to get hold of what was being sent in the original request
			'Get previous request parameters and create new request
			newRequest = Me.Request((Request.Operation), Request.ResponseFlags = &H0)
			oOut = Request.InArg
			Call oOut.setPos(Request.ReqstBodyPos)
			newOut = newRequest.InArg
			Call newOut.writeStream(oOut, oOut.Available)
			'The new request is baked and ready, so try again!
			invokeReqst = Me.invokeReqst(newRequest)
			'Start again with new info...
			'if LOCATION_FORWARD_PERM then GoTo tryAgain
			'ElseIf repStatus = 5 Then
			'NEEDS_ADDRESSING_MODE
			'???iAddrDisp=
		Else
			Call mVBOrb.VBOrb.raiseMARSHAL(1, mVBOrb.VBOrb.CompletedMAYBE, "Unknown repStatus: " & CStr(repStatus))
		End If
		Call Request.InArg.destroy()
		Exit Function
ErrLog: 
		Call oOrb.logErr("cOrbObjRef.invokeReqst")
		Resume Next
ErrHandler: 
		Call mVBOrb.ErrSave()
		If iLogLevel >= 4 Then
			Call oOrb.logMsg("I " & Err.Description)
		End If
		Resume ErrCleanLoadRaise
ErrCleanLoadRaise: 
		On Error GoTo ErrLog
		Call Request.InArg.destroy()
		On Error GoTo 0
		Call mVBOrb.ErrLoad()
		Call mVBOrb.ErrReraise("invokeReqst")
	End Function
	
	'(Do not call with oLcFwObjRef)
	Private Sub writeReqstServiceContextFw(ByVal oOut As cOrbStream)
		On Error GoTo ErrHandler
		If (iLogLevel And &H20) <> 0 Then
			Call oOrb.logMsg("D writeReqstServiceContextFw")
			Call oOrb.logMsg("D CNCS-C " & Hex(mVBOrb.ONCSC) & " -> TCS-C " & Hex(oOut.getTCSC()) & " -> SNCS-C " & Hex(SNCSCFw))
			Call oOrb.logMsg("D CNCS-W " & Hex(mVBOrb.ONCSW) & " -> TCS-W " & Hex(oOut.getTCSW()) & " -> SNCS-W " & Hex(SNCSWFw))
		End If
		'IOP::ServiceContextList service_context;
		Dim lSeqSC As Integer
		lSeqSC = 1
		Call oOut.writeUlong(lSeqSC)
		Call oOut.writeUlong(1) 'Transmission code sets
		Call oOut.writeEncapOpen(False)
		Call oOut.writeUlong(oOut.getTCSC())
		Call oOut.writeUlong(oOut.getTCSW())
		Call oOut.writeEncapClose()
		Exit Sub
ErrHandler: 
		Call mVBOrb.ErrReraise("writeReqstServiceContextFw")
	End Sub
	
	'(Do not call with oLcFwObjRef)
	Private Sub readReplyServiceContextFw(ByVal oIn As cOrbStream)
		On Error GoTo ErrHandler
		Dim seqSC As Integer
		Dim i1 As Integer
		Dim lContextId As Integer
		Dim lContextLen As Integer
		'IOP::ServiceContextList service_context;
		seqSC = oIn.readUlong()
		Dim sExDetailMsg As String
		For i1 = 1 To seqSC
			lContextId = oIn.readUlong()
			lContextLen = oIn.readUlong()
			Select Case lContextId
				'Case 1 'Transmission code sets
				'Replies are always using the same codesets as in the request???
				'JDK 1.4 ORB does not give code sets here
				Case 9 'UnknownExceptionInfo
					Call oIn.readEncapOpen(lContextLen)
					'Call readJavaExceptionValue()
					Call oIn.readEncapClose()
				Case 14 'ExceptionDetailMessage
					Call oIn.readEncapOpen(lContextLen)
					sExDetailMsg = oIn.readWString()
					Call oIn.readEncapClose()
				Case Else
					'Skipping unknown service context
					Call oIn.readSkip(lContextLen)
			End Select
		Next i1
		Exit Sub
ErrHandler: 
		Call mVBOrb.ErrReraise("readReplyServiceContextFw")
	End Sub
	
	Private Function cOrbAbstractBase_isObjRef() As Boolean Implements _cOrbAbstractBase.isObjRef
		cOrbAbstractBase_isObjRef = True
	End Function
	
	Private Function cOrbObject_setObjRef(ByVal ObjRef As cOrbObjRef, ByVal Check As Boolean) As Boolean Implements _cOrbObject.setObjRef
		On Error GoTo ErrHandler
		If ObjRef Is Nothing Then
			cOrbObject_setObjRef = True
		Else
			cOrbObject_setObjRef = False
			'Set Me = ObjRef???
			Call mVBOrb.VBOrb.raiseNOIMPLEMENT(0, mVBOrb.VBOrb.CompletedNO)
		End If
		Exit Function
ErrHandler:
		Call VBOrb.ErrReraise(Err, "setObjRef")
	End Function
	
	Private Function cOrbObject_getObjRef() As cOrbObjRef Implements _cOrbObject.getObjRef
		cOrbObject_getObjRef = Me
	End Function
	
	Private Function cOrbObject_getId() As String Implements _cOrbObject.getId
		cOrbObject_getId = sTypeId
	End Function
End Class