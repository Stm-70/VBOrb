Option Strict Off
Option Explicit On
'UPGRADE_WARNING: Class instancing was changed to public. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="ED41034B-3890-49FC-8076-BD6FC2F42A85"'
<System.Runtime.InteropServices.ProgId("cVBOrb_NET.cVBOrb")> Public Class cVBOrb
	'Copyright (c) 2000 Martin.Both
	
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
	
	Private Const sExPrefix As String = "IDL:omg.org/CORBA/"
	Private Const sExPostfix As String = ":1.0"
	
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
	
	Public Function getNextUniqueID() As Integer
		getNextUniqueID = mVBOrb.getNextUniqueID()
	End Function
	
	'ORB Initialization (VBOrb.init)
	'The ORB_init is part of the CORBA module but not part of the ORB interface.
	'IN:    ORBId       Specifies the identifier of the ORB to be used by the application.
	'IN:    OAHost      Explicitly defines the hostname(s)and/or IP address(es)to be used
	'IN:    OAPort      Specifies the port number on which the Root POA Manager should listen
	'                   for new connections. If no port is specified, one random port will
	'                   be selected automatically by the server.
	'IN:    OAVersion   Specifies the GIOP version to be used in own object references.
	'IN:    ORBDefaultInitRef  Specifies a URL. If an application calls the ORB operation
	'                   resolveInitialReferences and the initial references is not explicitly
	'                   specified, the ORB appends a slash ‘/’ character and the ObjectId
	'                   to the specified URL to obtain the initial object reference.
	'IN:    ORBInitRef  <ObjectId>=<ObjectURL> <ObjectId>=<ObjectURL> ...
	'                   Explicitly specifies one or more initial references.
	'IN:    LogFile     Specifies the ORB logging file, if logging is desired
	'IN:    LogLevel    Specifies the ORB logging level (ERR= 1, WARN= 2, INFO= 4, DEBUG= 8)
	'IN:    VisiWorkaround  Use Visibroker 4.0 protocol instead of IIOP
	Public Function init(Optional ByVal ORBId As String = "", Optional ByVal OAHost As String = "", Optional ByVal OAPort As String = "0", Optional ByVal OAVersion As Short = &H102, Optional ByVal ORBDefaultInitRef As String = "", Optional ByVal ORBInitRef As String = "", Optional ByVal LogFile As String = "", Optional ByVal LogLevel As Short = 4, Optional ByVal VisiWorkaround As Boolean = False) As cOrbImpl
		init = mVBOrb.init(ORBId, OAHost, OAPort, OAVersion, ORBDefaultInitRef, ORBInitRef, LogFile, LogLevel, VisiWorkaround)
	End Function
	
	'Get an ORB to create TypeCodes
	Public Function defaultOrb() As cOrbImpl
		defaultOrb = mVBOrb.defaultOrb()
	End Function
	
	'Write an exception onto a log and delete the exception
	Public Sub logException(ByRef sLogFile As String, ByVal oUserEx As _cOrbException)
		Call mVBOrb.logException(sLogFile, oUserEx)
	End Sub
	
	'If "On Error Resume Next" is on then
	'write the Error onto a log file and delete the Error
	Public Sub logErr(ByRef sLogFile As String, ByRef ErrObj As ErrObject, ByRef SourcePrefix As String)
		Call ErrToDll(ErrObj)
		Call mVBOrb.logErr(sLogFile, SourcePrefix)
	End Sub
	
	'Write a message onto a log file
	Public Sub logMsg(ByRef sLogFile As String, ByRef sMsg As String)
		Call mVBOrb.logMsg(sLogFile, sMsg)
	End Sub
	
	Public Sub ErrRaise(ByVal Number As Integer, ByRef Description As String, Optional ByVal Source As String = "")
		Call Err.Raise(vbObjectError Or Number, Source, Description, "", 0)
	End Sub
	
	Public Sub ErrReraise(ByRef ErrObj As ErrObject, ByRef SourcePrefix As String, Optional ByRef PostDescr As String = "")
		If (Err.Number And vbObjectError) = vbObjectError Then
			Call Err.Raise(Err.Number, IIf(ErrObj.Source = "", SourcePrefix, SourcePrefix & ":" & ErrObj.Source), IIf(PostDescr = "", Err.Description, Err.Description & ", " & PostDescr), "", 0)
		Else 'Overwrite Err.Source
			Call Err.Raise(vbObjectError Or Err.Number, SourcePrefix, IIf(PostDescr = "", Err.Description, Err.Description & ", " & PostDescr), "", 0)
		End If
	End Sub
	
	'Is not calling Err.Clear, see also ErrToDll()
	Public Sub ErrSave(ByRef ErrObj As ErrObject)
		Call ErrToDll(ErrObj)
		Call mVBOrb.ErrSave()
	End Sub
	
	'Load the VBOrb.DLL ErrObject, see also ErrOfDll()
	Public Function ErrLoad() As ErrObject
		Call mVBOrb.ErrLoad()
		ErrLoad = ErrOfDll()
	End Function
	
	'If "On Error Resume Next" is on then show the Error and delete the Error
	Public Sub ErrMsgBox(ByRef ErrObj As ErrObject, ByRef SourcePrefix As String)
		Dim sNumberStr As String
		If (Err.Number And vbObjectError) = vbObjectError Then
			sNumberStr = "0x" & Hex(Err.Number)
		Else
			sNumberStr = CStr(Err.Number)
		End If
		Call MsgBox("Error: " & sNumberStr & vbCrLf & IIf(ErrObj.Source = "", SourcePrefix, SourcePrefix & ":" & ErrObj.Source) & vbCrLf & Err.Description)
		Call ErrObj.Clear()
	End Sub
	
	'Set the VBOrb.DLL ErrObject
	Public Sub ErrToDll(ByRef ErrObj As ErrObject)
		If Err.Number = 0 Then
			Call mVBOrb.ErrRaise(1, "ErrObj.Number = 0", "ErrToDll")
		End If
		Dim lNumber As Integer
		Dim sDescription As String
		Dim sSource As String
		lNumber = Err.Number
		sDescription = Err.Description
		sSource = ErrObj.Source
		On Error Resume Next
		Call Err.Raise(lNumber, sSource, sDescription)
	End Sub
	
	'Get the VBOrb.DLL ErrObject
	Public Function ErrOfDll() As ErrObject
		ErrOfDll = Err
	End Function
	
	Public ReadOnly Property MinorOMGVMCID() As Integer
		Get
			MinorOMGVMCID = mVBOrb.MinorOMGVMCID
		End Get
	End Property
	
	Public ReadOnly Property CompletedYES() As Integer
		Get
			CompletedYES = 0
		End Get
	End Property
	
	Public ReadOnly Property CompletedNO() As Integer
		Get
			CompletedNO = 1
		End Get
	End Property
	
	Public ReadOnly Property CompletedMAYBE() As Integer
		Get
			CompletedMAYBE = 2
		End Get
	End Property
	
	Public ReadOnly Property ITF_E_UNKNOWN_NO() As Integer
		Get
			ITF_E_UNKNOWN_NO = &H40200
		End Get
	End Property
	Public ReadOnly Property ITF_E_BAD_PARAM_NO() As Integer
		Get
			ITF_E_BAD_PARAM_NO = &H40201
		End Get
	End Property
	Public ReadOnly Property ITF_E_NO_MEMORY_NO() As Integer
		Get
			ITF_E_NO_MEMORY_NO = &H40202
		End Get
	End Property
	Public ReadOnly Property ITF_E_IMP_LIMIT_NO() As Integer
		Get
			ITF_E_IMP_LIMIT_NO = &H40203
		End Get
	End Property
	Public ReadOnly Property ITF_E_COMM_FAILURE_NO() As Integer
		Get
			ITF_E_COMM_FAILURE_NO = &H40204
		End Get
	End Property
	Public ReadOnly Property ITF_E_INV_OBJREF_NO() As Integer
		Get
			ITF_E_INV_OBJREF_NO = &H40205
		End Get
	End Property
	Public ReadOnly Property ITF_E_NO_PERMISSION_NO() As Integer
		Get
			ITF_E_NO_PERMISSION_NO = &H40206
		End Get
	End Property
	Public ReadOnly Property ITF_E_INTERNAL_NO() As Integer
		Get
			ITF_E_INTERNAL_NO = &H40207
		End Get
	End Property
	Public ReadOnly Property ITF_E_MARSHAL_NO() As Integer
		Get
			ITF_E_MARSHAL_NO = &H40208
		End Get
	End Property
	Public ReadOnly Property ITF_E_INITIALIZE_NO() As Integer
		Get
			ITF_E_INITIALIZE_NO = &H40209
		End Get
	End Property
	Public ReadOnly Property ITF_E_NO_IMPLEMENT_NO() As Integer
		Get
			ITF_E_NO_IMPLEMENT_NO = &H4020A
		End Get
	End Property
	Public ReadOnly Property ITF_E_BAD_TYPECODE_NO() As Integer
		Get
			ITF_E_BAD_TYPECODE_NO = &H4020B
		End Get
	End Property
	Public ReadOnly Property ITF_E_BAD_OPERATION_NO() As Integer
		Get
			ITF_E_BAD_OPERATION_NO = &H4020C
		End Get
	End Property
	Public ReadOnly Property ITF_E_NO_RESOURCES_NO() As Integer
		Get
			ITF_E_NO_RESOURCES_NO = &H4020D
		End Get
	End Property
	Public ReadOnly Property ITF_E_NO_RESPONSE_NO() As Integer
		Get
			ITF_E_NO_RESPONSE_NO = &H4020E
		End Get
	End Property
	Public ReadOnly Property ITF_E_PERSIST_STORE_NO() As Integer
		Get
			ITF_E_PERSIST_STORE_NO = &H4020F
		End Get
	End Property
	Public ReadOnly Property ITF_E_BAD_INV_ORDER_NO() As Integer
		Get
			ITF_E_BAD_INV_ORDER_NO = &H40210
		End Get
	End Property
	Public ReadOnly Property ITF_E_TRANSIENT_NO() As Integer
		Get
			ITF_E_TRANSIENT_NO = &H40211
		End Get
	End Property
	Public ReadOnly Property ITF_E_FREE_MEM_NO() As Integer
		Get
			ITF_E_FREE_MEM_NO = &H40212
		End Get
	End Property
	Public ReadOnly Property ITF_E_INV_IDENT_NO() As Integer
		Get
			ITF_E_INV_IDENT_NO = &H40213
		End Get
	End Property
	Public ReadOnly Property ITF_E_INV_FLAG_NO() As Integer
		Get
			ITF_E_INV_FLAG_NO = &H40214
		End Get
	End Property
	Public ReadOnly Property ITF_E_INTF_REPOS_NO() As Integer
		Get
			ITF_E_INTF_REPOS_NO = &H40215
		End Get
	End Property
	Public ReadOnly Property ITF_E_BAD_CONTEXT_NO() As Integer
		Get
			ITF_E_BAD_CONTEXT_NO = &H40216
		End Get
	End Property
	Public ReadOnly Property ITF_E_OBJ_ADAPTER_NO() As Integer
		Get
			ITF_E_OBJ_ADAPTER_NO = &H40217
		End Get
	End Property
	Public ReadOnly Property ITF_E_DATA_CONVERSION_NO() As Integer
		Get
			ITF_E_DATA_CONVERSION_NO = &H40218
		End Get
	End Property
	Public ReadOnly Property ITF_E_OBJECT_NOT_EXIST_NO() As Integer
		Get
			ITF_E_OBJECT_NOT_EXIST_NO = &H40219
		End Get
	End Property
	Public ReadOnly Property ITF_E_TRANSACTION_REQUIRED_NO() As Integer
		Get
			ITF_E_TRANSACTION_REQUIRED_NO = &H40220
		End Get
	End Property
	Public ReadOnly Property ITF_E_TRANSACTION_ROLLEDBACK_NO() As Integer
		Get
			ITF_E_TRANSACTION_ROLLEDBACK_NO = &H40221
		End Get
	End Property
	Public ReadOnly Property ITF_E_INVALID_TRANSACTION_NO() As Integer
		Get
			ITF_E_INVALID_TRANSACTION_NO = &H40222
		End Get
	End Property
	Public ReadOnly Property ITF_E_INV_POLICY_NO() As Integer
		Get
			ITF_E_INV_POLICY_NO = &H40223
		End Get
	End Property
	Public ReadOnly Property ITF_E_CODESET_INCOMPATIBLE_NO() As Integer
		Get
			ITF_E_CODESET_INCOMPATIBLE_NO = &H40224
		End Get
	End Property
	Public ReadOnly Property ITF_E_REBIND_NO() As Integer
		Get
			ITF_E_REBIND_NO = &H40225
		End Get
	End Property
	Public ReadOnly Property ITF_E_TIMEOUT_NO() As Integer
		Get
			ITF_E_TIMEOUT_NO = &H40226
		End Get
	End Property
	Public ReadOnly Property ITF_E_TRANSACTION_UNAVAILABLE_NO() As Integer
		Get
			ITF_E_TRANSACTION_UNAVAILABLE_NO = &H40227
		End Get
	End Property
	Public ReadOnly Property ITF_E_TRANSACTION_MODE_NO() As Integer
		Get
			ITF_E_TRANSACTION_MODE_NO = &H40228
		End Get
	End Property
	Public ReadOnly Property ITF_E_BAD_QOS_NO() As Integer
		Get
			ITF_E_BAD_QOS_NO = &H40229
		End Get
	End Property
	
	'The unknown exception
	Public Sub raiseUNKNOWN(ByVal minor As Integer, ByVal completed As Integer, Optional ByRef PostDescr As String = "")
		Call mVBOrb.raiseSystemException(ITF_E_UNKNOWN_NO, sExPrefix & "UNKNOWN" & sExPostfix, minor, completed, PostDescr)
	End Sub
	
	'An invalid parameter was passed
	Public Sub raiseBADPARAM(ByVal minor As Integer, ByVal completed As Integer, Optional ByRef PostDescr As String = "")
		Call mVBOrb.raiseSystemException(ITF_E_BAD_PARAM_NO, sExPrefix & "BAD_PARAM" & sExPostfix, minor, completed, PostDescr)
	End Sub
	
	'Dynamic memory allocation failure
	Public Sub raiseNOMEMORY(ByVal minor As Integer, ByVal completed As Integer, Optional ByRef PostDescr As String = "")
		Call mVBOrb.raiseSystemException(ITF_E_NO_MEMORY_NO, sExPrefix & "NO_MEMORY" & sExPostfix, minor, completed, PostDescr)
	End Sub
	
	'Violated implementation limit
	Public Sub raiseIMPLIMIT(ByVal minor As Integer, ByVal completed As Integer, Optional ByRef PostDescr As String = "")
		Call mVBOrb.raiseSystemException(ITF_E_IMP_LIMIT_NO, sExPrefix & "IMP_LIMIT" & sExPostfix, minor, completed, PostDescr)
	End Sub
	
	'Communication is lost while an operation is in progress
	Public Sub raiseCOMMFAILURE(ByVal minor As Integer, ByVal completed As Integer, Optional ByRef PostDescr As String = "")
		Call mVBOrb.raiseSystemException(ITF_E_COMM_FAILURE_NO, sExPrefix & "COMM_FAILURE" & sExPostfix, minor, completed, PostDescr)
	End Sub
	
	'Invalid object reference
	Public Sub raiseINVOBJREF(ByVal minor As Integer, ByVal completed As Integer, Optional ByRef PostDescr As String = "")
		Call mVBOrb.raiseSystemException(ITF_E_INV_OBJREF_NO, sExPrefix & "INV_OBJREF" & sExPostfix, minor, completed, PostDescr)
	End Sub
	
	'An invocation failed because the caller has insufficient privileges
	Public Sub raiseNOPERMISSION(ByVal minor As Integer, ByVal completed As Integer, Optional ByRef PostDescr As String = "")
		Call mVBOrb.raiseSystemException(ITF_E_NO_PERMISSION_NO, sExPrefix & "NO_PERMISSION" & sExPostfix, minor, completed, PostDescr)
	End Sub
	
	'ORB internal error (e.g. ORB has detected corruption of its internal data structures)
	Public Sub raiseINTERNAL(ByVal minor As Integer, ByVal completed As Integer, Optional ByRef PostDescr As String = "")
		Call mVBOrb.raiseSystemException(ITF_E_INTERNAL_NO, sExPrefix & "INTERNAL" & sExPostfix, minor, completed, PostDescr)
	End Sub
	
	'Error marshalling param/result
	Public Sub raiseMARSHAL(ByVal minor As Integer, ByVal completed As Integer, Optional ByRef PostDescr As String = "")
		Call mVBOrb.raiseSystemException(ITF_E_MARSHAL_NO, sExPrefix & "MARSHAL" & sExPostfix, minor, completed, PostDescr)
	End Sub
	
	'ORB initialization failure
	Public Sub raiseINITIALIZE(ByVal minor As Integer, ByVal completed As Integer, Optional ByRef PostDescr As String = "")
		Call mVBOrb.raiseSystemException(ITF_E_INITIALIZE_NO, sExPrefix & "INITIALIZE" & sExPostfix, minor, completed, PostDescr)
	End Sub
	
	'Operation implementation unavailable
	Public Sub raiseNOIMPLEMENT(ByVal minor As Integer, ByVal completed As Integer, Optional ByRef PostDescr As String = "")
		Call mVBOrb.raiseSystemException(ITF_E_NO_IMPLEMENT_NO, sExPrefix & "NO_IMPLEMENT" & sExPostfix, minor, completed, PostDescr)
	End Sub
	
	'The ORB has encountered a malformed type code
	Public Sub raiseBADTYPECODE(ByVal minor As Integer, ByVal completed As Integer, Optional ByRef PostDescr As String = "")
		Call mVBOrb.raiseSystemException(ITF_E_BAD_TYPECODE_NO, sExPrefix & "BAD_TYPECODE" & sExPostfix, minor, completed, PostDescr)
	End Sub
	
	'Object does not support the operation that was invoked
	Public Sub raiseBADOPERATION(ByVal minor As Integer, ByVal completed As Integer, Optional ByRef PostDescr As String = "")
		Call mVBOrb.raiseSystemException(ITF_E_BAD_OPERATION_NO, sExPrefix & "BAD_OPERATION" & sExPostfix, minor, completed, PostDescr)
	End Sub
	
	'Insufficient resources for req. (e.g. max. number of open connections)
	Public Sub raiseNORESOURCES(ByVal minor As Integer, ByVal completed As Integer, Optional ByRef PostDescr As String = "")
		Call mVBOrb.raiseSystemException(ITF_E_NO_RESOURCES_NO, sExPrefix & "NO_RESOURCES" & sExPostfix, minor, completed, PostDescr)
	End Sub
	
	'Response to request not yet available
	Public Sub raiseNORESPONSE(ByVal minor As Integer, ByVal completed As Integer, Optional ByRef PostDescr As String = "")
		Call mVBOrb.raiseSystemException(ITF_E_NO_RESPONSE_NO, sExPrefix & "NO_RESPONSE" & sExPostfix, minor, completed, PostDescr)
	End Sub
	
	'Persistent storage failure
	Public Sub raisePERSISTSTORE(ByVal minor As Integer, ByVal completed As Integer, Optional ByRef PostDescr As String = "")
		Call mVBOrb.raiseSystemException(ITF_E_PERSIST_STORE_NO, sExPrefix & "PERSIST_STORE" & sExPostfix, minor, completed, PostDescr)
	End Sub
	
	'Routine invocations out of order
	Public Sub raiseBADINVORDER(ByVal minor As Integer, ByVal completed As Integer, Optional ByRef PostDescr As String = "")
		Call mVBOrb.raiseSystemException(ITF_E_BAD_INV_ORDER_NO, sExPrefix & "BAD_INV_ORDER" & sExPostfix, minor, completed, PostDescr)
	End Sub
	
	'Transient failure - reissue request
	Public Sub raiseTRANSIENT(ByVal minor As Integer, ByVal completed As Integer, Optional ByRef PostDescr As String = "")
		Call mVBOrb.raiseSystemException(ITF_E_TRANSIENT_NO, sExPrefix & "TRANSIENT" & sExPostfix, minor, completed, PostDescr)
	End Sub
	
	'Cannot free memory
	Public Sub raiseFREEMEM(ByVal minor As Integer, ByVal completed As Integer, Optional ByRef PostDescr As String = "")
		Call mVBOrb.raiseSystemException(ITF_E_FREE_MEM_NO, sExPrefix & "FREE_MEM" & sExPostfix, minor, completed, PostDescr)
	End Sub
	
	'Invalid identifier syntax
	Public Sub raiseINVIDENT(ByVal minor As Integer, ByVal completed As Integer, Optional ByRef PostDescr As String = "")
		Call mVBOrb.raiseSystemException(ITF_E_INV_IDENT_NO, sExPrefix & "INV_IDENT" & sExPostfix, minor, completed, PostDescr)
	End Sub
	
	'An invalid flag was specified or passed to an operation
	Public Sub raiseINVFLAG(ByVal minor As Integer, ByVal completed As Integer, Optional ByRef PostDescr As String = "")
		Call mVBOrb.raiseSystemException(ITF_E_INV_FLAG_NO, sExPrefix & "INV_FLAG" & sExPostfix, minor, completed, PostDescr)
	End Sub
	
	'Error accessing interface repository
	Public Sub raiseINTFREPOS(ByVal minor As Integer, ByVal completed As Integer, Optional ByRef PostDescr As String = "")
		Call mVBOrb.raiseSystemException(ITF_E_INTF_REPOS_NO, sExPrefix & "INTF_REPOS" & sExPostfix, minor, completed, PostDescr)
	End Sub
	
	'Error processing context object
	Public Sub raiseBADCONTEXT(ByVal minor As Integer, ByVal completed As Integer, Optional ByRef PostDescr As String = "")
		Call mVBOrb.raiseSystemException(ITF_E_BAD_CONTEXT_NO, sExPrefix & "BAD_CONTEXT" & sExPostfix, minor, completed, PostDescr)
	End Sub
	
	'Failure detected by object adapter
	Public Sub raiseOBJADAPTER(ByVal minor As Integer, ByVal completed As Integer, Optional ByRef PostDescr As String = "")
		Call mVBOrb.raiseSystemException(ITF_E_OBJ_ADAPTER_NO, sExPrefix & "OBJ_ADAPTER" & sExPostfix, minor, completed, PostDescr)
	End Sub
	
	'Data conversion error
	Public Sub raiseDATACONVERSION(ByVal minor As Integer, ByVal completed As Integer, Optional ByRef PostDescr As String = "")
		Call mVBOrb.raiseSystemException(ITF_E_DATA_CONVERSION_NO, sExPrefix & "DATA_CONVERSION" & sExPostfix, minor, completed, PostDescr)
	End Sub
	
	'Non-existent object, delete reference
	Public Sub raiseOBJECTNOTEXIST(ByVal minor As Integer, ByVal completed As Integer, Optional ByRef PostDescr As String = "")
		Call mVBOrb.raiseSystemException(ITF_E_OBJECT_NOT_EXIST_NO, sExPrefix & "OBJECT_NOT_EXIST" & sExPostfix, minor, completed, PostDescr)
	End Sub
	
	'Transaction required
	Public Sub raiseTRANSACTIONREQUIRED(ByVal minor As Integer, ByVal completed As Integer, Optional ByRef PostDescr As String = "")
		Call mVBOrb.raiseSystemException(ITF_E_TRANSACTION_REQUIRED_NO, sExPrefix & "TRANSACTION_REQUIRED" & sExPostfix, minor, completed, PostDescr)
	End Sub
	
	'Transaction rolled back
	Public Sub raiseTRANSACTIONROLLEDBACK(ByVal minor As Integer, ByVal completed As Integer, Optional ByRef PostDescr As String = "")
		Call mVBOrb.raiseSystemException(ITF_E_TRANSACTION_ROLLEDBACK_NO, sExPrefix & "TRANSACTION_ROLLEDBACK" & sExPostfix, minor, completed, PostDescr)
	End Sub
	
	'Invalid transaction
	Public Sub raiseINVALIDTRANSACTION(ByVal minor As Integer, ByVal completed As Integer, Optional ByRef PostDescr As String = "")
		Call mVBOrb.raiseSystemException(ITF_E_INVALID_TRANSACTION_NO, sExPrefix & "INVALID_TRANSACTION" & sExPostfix, minor, completed, PostDescr)
	End Sub
	
	'Invalid policy
	Public Sub raiseINVPOLICY(ByVal minor As Integer, ByVal completed As Integer, Optional ByRef PostDescr As String = "")
		Call mVBOrb.raiseSystemException(ITF_E_INV_POLICY_NO, sExPrefix & "INV_POLICY" & sExPostfix, minor, completed, PostDescr)
	End Sub
	
	'Incompatible code set
	Public Sub raiseCODESETINCOMPATIBLE(ByVal minor As Integer, ByVal completed As Integer, Optional ByRef PostDescr As String = "")
		Call mVBOrb.raiseSystemException(ITF_E_CODESET_INCOMPATIBLE_NO, sExPrefix & "CODESET_INCOMPATIBLE" & sExPostfix, minor, completed, PostDescr)
	End Sub
	
	'Rebind needed
	Public Sub raiseREBIND(ByVal minor As Integer, ByVal completed As Integer, Optional ByRef PostDescr As String = "")
		Call mVBOrb.raiseSystemException(ITF_E_REBIND_NO, sExPrefix & "REBIND" & sExPostfix, minor, completed, PostDescr)
	End Sub
	
	'Operation timed out
	Public Sub raiseTIMEOUT(ByVal minor As Integer, ByVal completed As Integer, Optional ByRef PostDescr As String = "")
		Call mVBOrb.raiseSystemException(ITF_E_TIMEOUT_NO, sExPrefix & "TIMEOUT" & sExPostfix, minor, completed, PostDescr)
	End Sub
	
	'No transaction
	Public Sub raiseTRANSACTIONUNAVAILABLE(ByVal minor As Integer, ByVal completed As Integer, Optional ByRef PostDescr As String = "")
		Call mVBOrb.raiseSystemException(ITF_E_TRANSACTION_UNAVAILABLE_NO, sExPrefix & "TRANSACTION_UNAVAILABLE" & sExPostfix, minor, completed, PostDescr)
	End Sub
	
	'Invalid transaction mode
	Public Sub raiseTRANSACTIONMODE(ByVal minor As Integer, ByVal completed As Integer, Optional ByRef PostDescr As String = "")
		Call mVBOrb.raiseSystemException(ITF_E_TRANSACTION_MODE_NO, sExPrefix & "TRANSACTION_MODE" & sExPostfix, minor, completed, PostDescr)
	End Sub
	
	'Bad quality of service
	Public Sub raiseBADQOS(ByVal minor As Integer, ByVal completed As Integer, Optional ByRef PostDescr As String = "")
		Call mVBOrb.raiseSystemException(ITF_E_BAD_QOS_NO, sExPrefix & "BAD_QOS" & sExPostfix, minor, completed, PostDescr)
	End Sub
	
	'Raise a CORBA user exception
	Public Sub raiseUserEx(ByVal oEx As _cOrbException)
		Call mVBOrb.raiseUserEx(oEx)
	End Sub
	
	'Get a CORBA user exception and delete the VBOrb.DLL ErrObject
	Public Function getException() As _cOrbException
		getException = mVBOrb.getException()
	End Function
	
	'CORBA system exceptions are raised using the VBOrb.DLL ErrObject
	Public Function ErrIsSystemEx() As Boolean
		ErrIsSystemEx = mVBOrb.ErrIsSystemEx()
	End Function
	
	'CORBA user exceptions are raised using the VBOrb.DLL ErrObject
	Public Function ErrIsUserEx() As Boolean
		ErrIsUserEx = mVBOrb.ErrIsUserEx()
	End Function
	
	'Predefined TypeCode constants
	'An instance of the tk_null TypeCode
	Public Function TCNull() As Object
		TCNull = mVBOrb.VBOrb.defaultOrb().createPrimitiveTc(mCB.tk_null)
	End Function
	
	Public Function TCVoid() As Object
		TCVoid = mVBOrb.VBOrb.defaultOrb().createPrimitiveTc(mCB.tk_void)
	End Function
	
	Public Function TCShort() As Object
		TCShort = mVBOrb.VBOrb.defaultOrb().createPrimitiveTc(mCB.tk_short)
	End Function
	
	Public Function TCLong() As Object
		TCLong = mVBOrb.VBOrb.defaultOrb().createPrimitiveTc(mCB.tk_long)
	End Function
	
	Public Function TCLonglong() As Object
		TCLonglong = mVBOrb.VBOrb.defaultOrb().createPrimitiveTc(mCB.tk_longlong)
	End Function
	
	Public Function TCUshort() As Object
		TCUshort = mVBOrb.VBOrb.defaultOrb().createPrimitiveTc(mCB.tk_ushort)
	End Function
	
	Public Function TCUlong() As Object
		TCUlong = mVBOrb.VBOrb.defaultOrb().createPrimitiveTc(mCB.tk_ulong)
	End Function
	
	Public Function TCUlonglong() As Object
		TCUlonglong = mVBOrb.VBOrb.defaultOrb().createPrimitiveTc(mCB.tk_ulonglong)
	End Function
	
	Public Function TCFloat() As Object
		TCFloat = mVBOrb.VBOrb.defaultOrb().createPrimitiveTc(mCB.tk_float)
	End Function
	
	Public Function TCDouble() As Object
		TCDouble = mVBOrb.VBOrb.defaultOrb().createPrimitiveTc(mCB.tk_double)
	End Function
	
	Public Function TCLongdouble() As Object
		TCLongdouble = mVBOrb.VBOrb.defaultOrb().createPrimitiveTc(mCB.tk_longdouble)
	End Function
	
	Public Function TCBoolean() As Object
		TCBoolean = mVBOrb.VBOrb.defaultOrb().createPrimitiveTc(mCB.tk_boolean)
	End Function
	
	Public Function TCChar() As Object
		TCChar = mVBOrb.VBOrb.defaultOrb().createPrimitiveTc(mCB.tk_char)
	End Function
	
	Public Function TCWChar() As Object
		TCWChar = mVBOrb.VBOrb.defaultOrb().createPrimitiveTc(mCB.tk_wchar)
	End Function
	
	Public Function TCOctet() As Object
		TCOctet = mVBOrb.VBOrb.defaultOrb().createPrimitiveTc(mCB.tk_octet)
	End Function
	
	Public Function TCAny() As Object
		TCAny = mVBOrb.VBOrb.defaultOrb().createPrimitiveTc(mCB.tk_any)
	End Function
	
	Public Function TCTypeCode() As Object
		TCTypeCode = mVBOrb.VBOrb.defaultOrb().createPrimitiveTc(mCB.tk_TypeCode)
	End Function
	
	'Calling id returns "IDL:omg.org/CORBA/Object:1.0" and calling name returns "Object"
	Public Function TCObject() As Object
		TCObject = mVBOrb.VBOrb.defaultOrb().createPrimitiveTc(mCB.tk_objref)
	End Function
	
	'tk_string {0} // unbounded
	Public Function TCString() As Object
		TCString = mVBOrb.VBOrb.defaultOrb().createPrimitiveTc(mCB.tk_string)
	End Function
	
	'tk_wstring{0}/// unbounded
	Public Function TCWstring() As Object
		TCWstring = mVBOrb.VBOrb.defaultOrb().createPrimitiveTc(mCB.tk_wstring)
	End Function
	
	'Calling id returns "IDL:omg.org/CORBA/ValueBase:1.0", calling name returns "ValueBase",
	'calling member_count returns 0, calling type_modifier returns CORBA::VM_NONE,
	'and calling concrete_base_type returns a nil TypeCode.
	Public Function TCValueBase() As Object
		TCValueBase = mVBOrb.VBOrb.defaultOrb().createPrimitiveTc(mCB.tk_value)
	End Function
	
	'IDL:omg.org/Components/CCMObject:1.0
	Public Function TCComponent() As Object
		TCComponent = mVBOrb.VBOrb.defaultOrb().createPrimitiveTc(mCB.tk_component)
	End Function
	
	'IDL:omg.org/Components/CCMHome:1.0
	Public Function TCHome() As Object
		TCHome = mVBOrb.VBOrb.defaultOrb().createPrimitiveTc(mCB.tk_home)
	End Function
	
	'IDL:omg.org/Components/EventBase:1.0
	Public Function TCEventBase() As Object
		TCEventBase = mVBOrb.VBOrb.defaultOrb().createPrimitiveTc(mCB.tk_event)
	End Function
End Class