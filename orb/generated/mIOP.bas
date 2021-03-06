Attribute VB_Name = "mIOP"
'Generated by IDL2VB v123. Copyright (c) 2000-2003 Martin.Both
'Source File Name: ../include/IOP.idl

Option Explicit

'module ::IOP

'Constants
Public Const TransactionService As Long = 0
Public Const CodeSets As Long = 1
Public Const ChainBypassCheck As Long = 2
Public Const ChainBypassInfo As Long = 3
Public Const LogicalThreadId As Long = 4
Public Const BI_DIR_IIOP As Long = 5
Public Const SendingContextRunTime As Long = 6
Public Const INVOCATION_POLICIES As Long = 7
Public Const FORWARDED_IDENTITY As Long = 8
Public Const UnknownExceptionInfo As Long = 9
Public Const RTCorbaPriority As Long = 10
Public Const RTCorbaPriorityRange As Long = 11
Public Const FT_GROUP_VERSION As Long = 12
Public Const FT_REQUEST As Long = 13
Public Const ExceptionDetailMessage As Long = 14
Public Const SecurityAttributeService As Long = 15
Public Const TAG_INTERNET_IOP As Long = 0
Public Const TAG_MULTIPLE_COMPONENTS As Long = 1
Public Const TAG_SCCP_IOP As Long = 2
Public Const TAG_ORB_TYPE As Long = 0
Public Const TAG_CODE_SETS As Long = 1
Public Const TAG_POLICIES As Long = 2
Public Const TAG_ALTERNATE_IIOP_ADDRESS As Long = 3
Public Const TAG_COMPLETE_OBJECT_KEY As Long = 5
Public Const TAG_ENDPOINT_ID_POSITION As Long = 6
Public Const TAG_LOCATION_POLICY As Long = 12
Public Const TAG_ASSOCIATION_OPTIONS As Long = 13
Public Const TAG_SEC_NAME As Long = 14
Public Const TAG_SPKM_1_SEC_MECH As Long = 15
Public Const TAG_SPKM_2_SEC_MECH As Long = 16
Public Const TAG_KerberosV5_SEC_MECH As Long = 17
Public Const TAG_CSI_ECMA_Secret_SEC_MECH As Long = 18
Public Const TAG_CSI_ECMA_Hybrid_SEC_MECH As Long = 19
Public Const TAG_SSL_SEC_TRANS As Long = 20
Public Const TAG_CSI_ECMA_Public_SEC_MECH As Long = 21
Public Const TAG_GENERIC_SEC_MECH As Long = 22
Public Const TAG_FIREWALL_TRANS As Long = 23
Public Const TAG_SCCP_CONTACT_INFO As Long = 24
Public Const TAG_JAVA_CODEBASE As Long = 25
Public Const TAG_TRANSACTION_POLICY As Long = 26
Public Const TAG_FT_GROUP As Long = 27
Public Const TAG_FT_PRIMARY As Long = 28
Public Const TAG_FT_HEARTBEAT_ENABLED As Long = 29
Public Const TAG_MESSAGE_ROUTERS As Long = 30
Public Const TAG_OTS_POLICY As Long = 31
Public Const TAG_INV_POLICY As Long = 32
Public Const TAG_CSI_SEC_MECH_LIST As Long = 33
Public Const TAG_NULL_TAG As Long = 34
Public Const TAG_SECIOP_SEC_TRANS As Long = 35
Public Const TAG_TLS_SEC_TRANS As Long = 36
Public Const TAG_DCE_STRING_BINDING As Long = 100
Public Const TAG_DCE_BINDING_NAME As Long = 101
Public Const TAG_DCE_NO_PIPES As Long = 102
Public Const TAG_DCE_SEC_MECH As Long = 103
Public Const TAG_INET_SEC_TRANS As Long = 123
Public Const LOCATE_NEVER As Byte = 0
Public Const LOCATE_OBJECT As Byte = 1
Public Const LOCATE_OPERATION As Byte = 2
Public Const LOCATE_ALWAYS As Byte = 3
Public Const ENCODING_CDR_ENCAPS As Integer = 0
