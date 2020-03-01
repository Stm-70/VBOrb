Option Strict Off
Option Explicit On
<System.Runtime.InteropServices.ProgId("cOrbSocket_NET.cOrbSocket")> Public Class cOrbSocket
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
	
	Private bTermAll As Boolean
	Private sockFd As Integer
	Private sockAddr As tInetAddr
	'Origin of socket
	Private bIsOutgoing As Boolean 'connect()
	Private bIsListen As Boolean 'bind() and listen()
	Private bIsIncoming As Boolean 'accept()
	
	' Siehe auch Hilfethema: Zugreifen auf DLLs und das Windows-API
	'
	
	'typedef struct WSAData {
	'        WORD                    wVersion;
	'        WORD                    wHighVersion;
	'        char                    szDescription[WSADESCRIPTION_LEN+1];
	'        char                    szSystemStatus[WSASYS_STATUS_LEN+1];
	'        unsigned short          iMaxSockets;
	'        unsigned short          iMaxUdpDg;
	'        char FAR *              lpVendorInfo;
	'} WSADATA, FAR * LPWSADATA;
	Private Structure typWSAData
		Dim wVersion As Short
		Dim wHighVersion As Short
		'UPGRADE_WARNING: Fixed-length string size must fit in the buffer. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="3C1E4426-0B80-443E-B943-0627CD55D48B"'
		<VBFixedString(257),System.Runtime.InteropServices.MarshalAs(System.Runtime.InteropServices.UnmanagedType.ByValArray,SizeConst:=257)> Public szDescription() As Char
		'UPGRADE_WARNING: Fixed-length string size must fit in the buffer. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="3C1E4426-0B80-443E-B943-0627CD55D48B"'
		<VBFixedString(129),System.Runtime.InteropServices.MarshalAs(System.Runtime.InteropServices.UnmanagedType.ByValArray,SizeConst:=129)> Public szSystemStatus() As Char
		Dim iMaxSockets As Short
		Dim iMaxUdpDg As Short
		Dim lpVendorInfo As Integer
	End Structure
	
	'struct  hostent {
	'        char    FAR * h_name;           /* official name of host */
	'        char    FAR * FAR * h_aliases;  /* alias list */
	'        short   h_addrtype;             /* host address type */
	'        short   h_length;               /* length of address */
	'        char    FAR * FAR * h_addr_list; /* list of addresses */
	'#define h_addr  h_addr_list[0]          /* address, for backward compat */
	'};
	Private Structure tHostEnt
		Dim h_name As Integer
		Dim h_aliases As Integer
		Dim h_addrtype As Short
		Dim h_length As Short
		Dim h_addr_list As Integer
	End Structure
	
	Private Const AF_INET As Short = 2
	Private Const SOCK_STREAM As Short = 1 'stream socket
	Private Const INADDR_ANY As Integer = 0
	Private Const SO_REUSEADDR As Integer = 4 'allow local address reuse
	Private Const SOL_SOCKET As Integer = &HFFFF 'options for socket level
	'&HFFFF& <> &HFFFF (Thanks to Holger Beer)
	
	'Windows Sockets definitions of regular Microsoft error constants
	Private Const WSAEINTR As Integer = 10004 'WSA_OPERATION_ABORTED
	Private Const WSAEBADF As Integer = 10009
	Private Const WSAEACCES As Integer = 10013
	Private Const WSAEFAULT As Integer = 10014
	Private Const WSAEINVAL As Integer = 10022 'WSA_INVALID_PARAMETER
	Private Const WSAEMFILE As Integer = 10024
	
	'Windows Sockets definitions of regular Berkeley error constants
	Private Const WSAEWOULDBLOCK As Integer = 10035 'WSA_IO_PENDING, WSA_IO_INCOMPLETE
	Private Const WSAEINPROGRESS As Integer = 10036
	Private Const WSAEALREADY As Integer = 10037
	Private Const WSAENOTSOCK As Integer = 10038 'WSA_INVALID_HANDLE
	Private Const WSAEDESTADDRREQ As Integer = 10039
	Private Const WSAEMSGSIZE As Integer = 10040
	Private Const WSAEPROTOTYPE As Integer = 10041
	Private Const WSAENOPROTOOPT As Integer = 10042
	Private Const WSAEPROTONOSUPPORT As Integer = 10043
	Private Const WSAESOCKTNOSUPPORT As Integer = 10044
	Private Const WSAEOPNOTSUPP As Integer = 10045
	Private Const WSAEPFNOSUPPORT As Integer = 10046
	Private Const WSAEAFNOSUPPORT As Integer = 10047
	Private Const WSAEADDRINUSE As Integer = 10048
	Private Const WSAEADDRNOTAVAIL As Integer = 10049
	Private Const WSAENETDOWN As Integer = 10050
	Private Const WSAENETUNREACH As Integer = 10051
	Private Const WSAENETRESET As Integer = 10052
	Private Const WSAECONNABORTED As Integer = 10053
	Private Const WSAECONNRESET As Integer = 10054
	Private Const WSAENOBUFS As Integer = 10055 'WSA_NOT_ENOUGH_MEMORY
	Private Const WSAEISCONN As Integer = 10056
	Private Const WSAENOTCONN As Integer = 10057
	Private Const WSAESHUTDOWN As Integer = 10058
	Private Const WSAETOOMANYREFS As Integer = 10059
	Private Const WSAETIMEDOUT As Integer = 10060
	Private Const WSAECONNREFUSED As Integer = 10061
	Private Const WSAELOOP As Integer = 10062
	Private Const WSAENAMETOOLONG As Integer = 10063
	Private Const WSAEHOSTDOWN As Integer = 10064
	Private Const WSAEHOSTUNREACH As Integer = 10065
	Private Const WSAENOTEMPTY As Integer = 10066
	Private Const WSAEPROCLIM As Integer = 10067
	Private Const WSAEUSERS As Integer = 10068
	Private Const WSAEDQUOT As Integer = 10069
	Private Const WSAESTALE As Integer = 10070
	Private Const WSAEREMOTE As Integer = 10071
	
	'Extended Windows Sockets error constant definitions
	Private Const WSASYSNOTREADY As Integer = 10091
	Private Const WSAVERNOTSUPPORTED As Integer = 10092
	Private Const WSANOTINITIALISED As Integer = 10093
	Private Const WSAEDISCON As Integer = 10101
	Private Const WSAENOMORE As Integer = 10102
	Private Const WSAECANCELLED As Integer = 10103
	Private Const WSAEINVALIDPROCTABLE As Integer = 10104
	Private Const WSAEINVALIDPROVIDER As Integer = 10105
	Private Const WSAEPROVIDERFAILEDINIT As Integer = 10106
	Private Const WSASYSCALLFAILURE As Integer = 10107
	Private Const WSASERVICE_NOT_FOUND As Integer = 10108
	Private Const WSATYPE_NOT_FOUND As Integer = 10109
	Private Const WSA_E_NO_MORE As Integer = 10110
	Private Const WSA_E_CANCELLED As Integer = 10111
	Private Const WSAEREFUSED As Integer = 10112
	
	'Error return codes from gethostbyname() and gethostbyaddr()
	'(when using the resolver). Note that these errors are
	'retrieved via WSAGetLastError()
	
	'Authoritative Answer: Host not found
	Private Const WSAHOST_NOT_FOUND As Integer = 11001
	
	'Non-Authoritative: Host not found, or SERVERFAIL
	Private Const WSATRY_AGAIN As Integer = 11002
	
	'Non-recoverable errors, FORMERR, REFUSED, NOTIMP
	Private Const WSANO_RECOVERY As Integer = 11003
	
	'Valid name, no data record of requested type
	Private Const WSANO_DATA As Integer = 11004
	
	'no address, look for MX record
	Private Const WSANO_ADDRESS As Integer = 11004
	
	'Private Const WSA_WAIT_FAILED     ((DWORD)-1L)
	'Private Const WSA_WAIT_EVENT_0    ((DWORD)0)
	'Private Const WSA_WAIT_TIMEOUT    ((DWORD)0x102L)
	'Private Const WSA_INFINITE        ((DWORD)-1L)
	
	' Socket address, internet style.
	'
	'struct sockaddr_in {
	'        short   sin_family;        /* address family = AF_INET */
	'        u_short sin_port;
	'        struct  in_addr sin_addr;
	'        char    sin_zero[8];
	'};
	Private Structure tInetAddr
		Dim sin_family As Short
		Dim sin_port As Short
		Dim sin_addr As Integer
		Dim sin_zero0 As Integer
		Dim sin_zero1 As Integer
	End Structure
	
	'Structure used in select() call
	'struct timeval {
	'    long    tv_sec;         /* seconds */
	'    long    tv_usec;        /* and microseconds */
	'};
	Private Structure tTimeVal
		Dim tv_sec As Integer
		Dim tv_usec As Integer
	End Structure
	
	'typedef struct fd_set {
	'        u_int   fd_count;               /* how many are SET? */
	'        SOCKET  fd_array[FD_SETSIZE];   /* an array of SOCKETs */
	'} fd_set;
	Private Structure tFDSet
		Dim fd_count As Integer
		Dim fd_array As Integer
	End Structure
	
	'lstrcpyA();
	Private Declare Function lstrcpy Lib "kernel32"  Alias "lstrcpyA"(ByVal lpString1 As String, ByVal lpString2 As Integer) As Integer
	
	'CopyRect();
	'UPGRADE_ISSUE: Declaring a parameter 'As Any' is not supported. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="FAE78A8D-8978-4FD4-8208-5B7324A8F795"'
	Private Declare Function dllCopy16B Lib "user32"  Alias "CopyRect"(ByRef lpDestRect As Any, ByVal lpSourceRect As Integer) As Integer
	
	'RtlMoveMemory();
	'UPGRADE_ISSUE: Declaring a parameter 'As Any' is not supported. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="FAE78A8D-8978-4FD4-8208-5B7324A8F795"'
	Private Declare Sub dllMoveMem Lib "kernel32"  Alias "RtlMoveMemory"(ByRef hpvDest As Any, ByVal hpvSource As Integer, ByVal cbCopy As Integer)
	
	'int PASCAL FAR WSAStartup(WORD wVersionRequired, LPWSADATA lpWSAData);
	'UPGRADE_WARNING: Structure typWSAData may require marshalling attributes to be passed as an argument in this Declare statement. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="C429C3A5-5D47-4CD9-8F51-74A1616405DC"'
	Private Declare Function WSAStartup Lib "Ws2_32.dll" (ByVal wVersionRequired As Short, ByRef lpWSAData As typWSAData) As Integer
	
	'int WSACleanup(void);
	Private Declare Function WSACleanup Lib "Ws2_32.dll" () As Integer
	
	'int PASCAL FAR gethostname(char FAR * name, int namelen);
	Private Declare Function dllHostName Lib "Ws2_32.dll"  Alias "gethostname"(ByVal name As String, ByVal nameLen As Integer) As Integer
	'Declare Function GetComputerName Lib "kernel32" Alias "GetComputerNameA" _
	''   (ByVal lpBuffer As String, ByRef nSize As Long) As Long
	
	'unsigned long PASCAL FAR inet_addr (const char FAR * cp);
	Private Declare Function inet_addr Lib "Ws2_32.dll" (ByVal cp As String) As Integer
	
	'char FAR * PASCAL FAR inet_ntoa (struct in_addr in);
	Private Declare Function inet_ntoa Lib "Ws2_32.dll" (ByVal inaddr As Integer) As Integer
	
	'struct hostent FAR * PASCAL FAR gethostbyname(const char FAR * name);
	Private Declare Function gethostbyname Lib "Ws2_32.dll" (ByVal name As String) As Integer
	
	'SOCKET PASCAL FAR socket (int af, int type, int protocol);
	Private Declare Function dllSocket Lib "Ws2_32.dll"  Alias "socket"(ByVal af As Integer, ByVal stype As Integer, ByVal protocol As Integer) As Integer
	
	'int PASCAL FAR closesocket (SOCKET s);
	Private Declare Function dllCloseSocket Lib "Ws2_32.dll"  Alias "closesocket"(ByVal sockFd As Integer) As Integer
	
	'u_short PASCAL FAR htons (u_short hostshort);
	Private Declare Function DLLhtons Lib "Ws2_32.dll"  Alias "htons"(ByVal iPort As Short) As Short
	
	'u_short PASCAL FAR ntohs (u_short netshort);
	Private Declare Function DLLntohs Lib "Ws2_32.dll"  Alias "ntohs"(ByVal iPort As Short) As Short
	
	'u_long PASCAL FAR htonl (u_long hostlong);
	'Private Declare Function htonl Lib "Ws2_32.dll" _
	''    (ByVal lhost As Long) As Long
	
	'u_long PASCAL FAR ntohl (u_long netlong);
	'Private Declare Function ntohl Lib "Ws2_32.dll" _
	''    (ByVal lnet As Long) As Long
	
	'int PASCAL FAR connect (SOCKET s, const struct sockaddr FAR *name, int namelen);
	'UPGRADE_WARNING: Structure tInetAddr may require marshalling attributes to be passed as an argument in this Declare statement. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="C429C3A5-5D47-4CD9-8F51-74A1616405DC"'
	Private Declare Function DLLconnect Lib "Ws2_32.dll"  Alias "connect"(ByVal sockFd As Integer, ByRef sockAddr As tInetAddr, ByVal saddrlen As Integer) As Integer
	
	'int PASCAL FAR ioctlsocket (SOCKET s, long cmd, u_long FAR *argp);
	Private Declare Function DLLioctlLong Lib "Ws2_32.dll"  Alias "ioctlsocket"(ByVal sockFd As Integer, ByVal cmd As Integer, ByRef arg As Integer) As Integer
	
	'int PASCAL FAR recv (SOCKET s, char FAR * buf, int len, int flags);
	'UPGRADE_ISSUE: Declaring a parameter 'As Any' is not supported. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="FAE78A8D-8978-4FD4-8208-5B7324A8F795"'
	Private Declare Function DLLrecv Lib "Ws2_32.dll"  Alias "recv"(ByVal sockFd As Integer, ByRef buf As Any, ByVal bufLen As Integer, ByVal flags As Integer) As Integer
	
	'int PASCAL FAR send (SOCKET s, const char FAR * buf, int len, int flags);
	'UPGRADE_ISSUE: Declaring a parameter 'As Any' is not supported. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="FAE78A8D-8978-4FD4-8208-5B7324A8F795"'
	Private Declare Function DLLsend Lib "Ws2_32.dll"  Alias "send"(ByVal sockFd As Integer, ByRef buf As Any, ByVal bufLen As Integer, ByVal flags As Integer) As Integer
	Private Declare Function DLLsendstr Lib "Ws2_32.dll"  Alias "send"(ByVal sockFd As Integer, ByVal buf As String, ByVal bufLen As Integer, ByVal flags As Integer) As Integer
	
	'int PASCAL FAR getsockopt (SOCKET s, int level, int optname,
	'   char FAR * optval, int FAR *optlen);
	'Private Declare Function dllGetSockOpt Lib "Ws2_32.dll" Alias "getsockopt" _
	''    (ByVal sockFd As Long, ByVal level As Long, ByVal optName As Long, _
	''    ByRef optVal As Long, ByRef optLen As Long) As Long
	'int PASCAL FAR setsockopt (SOCKET s, int level, int optname,
	'   const char FAR * optval, int optlen);
	'value = 65536 by Holger Beer
	'ret = dllSetSockOpt(sockfd, SOL_SOCKET, &H1001&, value, 4)
	Private Declare Function dllSetSockOpt Lib "Ws2_32.dll"  Alias "setsockopt"(ByVal sockFd As Integer, ByVal level As Integer, ByVal optName As Integer, ByRef optVal As Integer, ByVal optLen As Integer) As Integer
	
	'int PASCAL FAR bind (SOCKET s, const struct sockaddr FAR *addr, int namelen);
	'UPGRADE_WARNING: Structure tInetAddr may require marshalling attributes to be passed as an argument in this Declare statement. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="C429C3A5-5D47-4CD9-8F51-74A1616405DC"'
	Private Declare Function dllBind Lib "Ws2_32.dll"  Alias "bind"(ByVal sockFd As Integer, ByRef sockAddr As tInetAddr, ByVal nameLen As Integer) As Integer
	
	'int PASCAL FAR getsockname (SOCKET s, struct sockaddr FAR *name,
	'    int FAR * namelen);
	'UPGRADE_WARNING: Structure tInetAddr may require marshalling attributes to be passed as an argument in this Declare statement. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="C429C3A5-5D47-4CD9-8F51-74A1616405DC"'
	Private Declare Function dllGetSockName Lib "Ws2_32.dll"  Alias "getsockname"(ByVal sockFd As Integer, ByRef sockAddr As tInetAddr, ByRef addrLen As Integer) As Integer
	
	'int PASCAL FAR listen (SOCKET s, int backlog);
	Private Declare Function dllListen Lib "Ws2_32.dll"  Alias "listen"(ByVal sockFd As Integer, ByVal backlog As Integer) As Integer
	
	'SOCKET PASCAL FAR accept (SOCKET s, struct sockaddr FAR *addr, int FAR *addrlen);
	'UPGRADE_WARNING: Structure tInetAddr may require marshalling attributes to be passed as an argument in this Declare statement. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="C429C3A5-5D47-4CD9-8F51-74A1616405DC"'
	Private Declare Function dllAccept Lib "Ws2_32.dll"  Alias "accept"(ByVal sockFd As Integer, ByRef sockAddr As tInetAddr, ByRef addrLen As Integer) As Integer
	
	'int PASCAL FAR select (int nfds, fd_set FAR *readfds, fd_set FAR *writefds,
	'   fd_set FAR *exceptfds, const struct timeval FAR *timeout);
	'UPGRADE_WARNING: Structure tTimeVal may require marshalling attributes to be passed as an argument in this Declare statement. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="C429C3A5-5D47-4CD9-8F51-74A1616405DC"'
	'UPGRADE_WARNING: Structure tFDSet may require marshalling attributes to be passed as an argument in this Declare statement. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="C429C3A5-5D47-4CD9-8F51-74A1616405DC"'
	'UPGRADE_WARNING: Structure tFDSet may require marshalling attributes to be passed as an argument in this Declare statement. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="C429C3A5-5D47-4CD9-8F51-74A1616405DC"'
	'UPGRADE_WARNING: Structure tFDSet may require marshalling attributes to be passed as an argument in this Declare statement. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="C429C3A5-5D47-4CD9-8F51-74A1616405DC"'
	Private Declare Function dllSelect Lib "Ws2_32.dll"  Alias "select"(ByVal nfds As Integer, ByRef readFDs As tFDSet, ByRef writeFDs As tFDSet, ByRef exceptFDs As tFDSet, ByRef timeOut As tTimeVal) As Integer
	
	'UPGRADE_NOTE: Class_Initialize was upgraded to Class_Initialize_Renamed. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="A9E4979A-37FA-4718-9994-97DD76ED70A7"'
	Private Sub Class_Initialize_Renamed()
#If DebugMode Then
		'UPGRADE_NOTE: #If #EndIf block was not upgraded because the expression DebugMode did not evaluate to True or was not evaluated. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="27EE2C3C-05AF-4C04-B2AF-657B4FB6B5FC"'
		lClassDebugID = mVBOrb.getNextClassDebugID()
		Debug.Print "'" & TypeName(Me) & "' " & lClassDebugID & " initialized"
#End If
		Call initWSA()
		bTermAll = False
		sockFd = -1
	End Sub
	Public Sub New()
		MyBase.New()
		Class_Initialize_Renamed()
	End Sub
	
	'UPGRADE_NOTE: Class_Terminate was upgraded to Class_Terminate_Renamed. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="A9E4979A-37FA-4718-9994-97DD76ED70A7"'
	Private Sub Class_Terminate_Renamed()
		'Release something which VB cannot know if required
		If sockFd <> -1 Then
			Call closeSocket()
		End If
		Call termWSA(bTermAll)
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
	
	Public Sub initTermAll(ByVal bNewTermAll As Boolean)
		bTermAll = bNewTermAll
	End Sub
	
	Private Sub initWSA()
		Dim wsaData As typWSAData
		Dim ret As Integer
		
		'Version 2.0
		ret = WSAStartup(2, wsaData)
		If ret <> 0 Then
			Err.Raise(Err.LastDllError, Description:="WSAStartup(2.0)")
		End If
	End Sub
	
	Private Sub termWSA(ByRef bTermAll As Boolean)
		If bTermAll Then
			While (WSACleanup() = 0)
			End While
			If Err.LastDllError <> WSANOTINITIALISED Then
				Call mVBOrb.ErrRaise(Err.LastDllError, "WSACleanup()")
			End If
		Else
			If WSACleanup() <> 0 Then
				Call mVBOrb.ErrRaise(Err.LastDllError, "WSACleanup()")
			End If
		End If
	End Sub
	
	'Remove trailing '\0' characters
	'UPGRADE_NOTE: str was upgraded to str_Renamed. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="A9E4979A-37FA-4718-9994-97DD76ED70A7"'
	Private Function leftstr(ByRef str_Renamed As String) As String
		leftstr = Left(str_Renamed, InStr(str_Renamed, vbNullChar) - 1)
	End Function
	
	'Get my host name or IP address
	Public Function getHostName(ByVal dotStyle As Boolean) As String
		On Error GoTo ErrHandler
		Dim sHostName As New VB6.FixedLengthString(256)
		'Dim llen As Long
		'llen = 256
		'Call GetComputerName(sHostName, llen)
		'getHostName= Left$(sHostName, llen)
		If dllHostName(sHostName.Value, 256) <> 0 Then
			Call mVBOrb.ErrRaise(Err.LastDllError, "gethostname() failed")
		End If
		Dim addr As Integer
		If dotStyle Then
			addr = inet_a2n(leftstr(sHostName.Value))
			getHostName = inet_n2a(addr)
		Else
			getHostName = leftstr(sHostName.Value)
		End If
		Exit Function
ErrHandler: 
		Call mVBOrb.ErrReraise("getHostName")
	End Function
	
	' Convert Portnumber
	' IN:   sPort  e.g: "80"
	' RET:           Portnumber
	Private Function inet_p2n(ByVal sPort As String) As Short
		On Error GoTo ErrHandler
		If Len(sPort) = 0 Then
			Call mVBOrb.ErrRaise(1, "Portnumber is missing")
		End If
		Dim lPort As Integer
		On Error Resume Next
		lPort = CInt(sPort)
		If Err.Number <> 0 Then
			On Error GoTo ErrHandler 'Is calling Err.Clear()
			Call mVBOrb.ErrRaise(6, "Portnumber " & sPort & " is invalid")
		End If
		On Error GoTo ErrHandler
		If lPort < 0 Or lPort >= &H10000 Then
			Call mVBOrb.ErrRaise(6, "Portnumber " & sPort & " is out of range")
		End If
		inet_p2n = IIf(lPort <= &H7FFF, lPort, lPort - &H10000)
		Exit Function
ErrHandler: 
		Call mVBOrb.ErrReraise("inet_p2n")
	End Function
	
	'Convert dot notation or host name to INET address
	'IN:    host    Example: "127.0.0.1" or "localhost"
	'RET:           Address or -1
	Private Function inet_a2n(ByVal Host As String) As Integer
		On Error GoTo ErrHandler
		Dim lPntr As Integer
		
		If Host = "" Then
			Host = "localhost" '127.0.0.1
		End If
		
		'First try the dot notation (a.b.c.d, a.b.c, a.b, a)
		'All numbers supplied as parts in dot notation can be decimal, octal,
		'or hexadecimal, as specified in the C language (i.e., a leading 0x or
		'0X implies hexadecimal; a leading 0 implies octal; otherwise, the
		'number is interpreted as decimal).
		inet_a2n = inet_addr(Host)
		If inet_a2n <> -1 Then
			Exit Function
		End If
		
		'No dot notation. Try to get the network host entry by name.
		lPntr = gethostbyname(Host)
		If lPntr = 0 Then
			If Err.LastDllError = WSAHOST_NOT_FOUND Then
				Call mVBOrb.VBOrb.raiseTRANSIENT(99, mVBOrb.VBOrb.CompletedNO, "Unknown host " & Host)
			Else
				Call mVBOrb.ErrRaise(Err.LastDllError, "gethostbyname(" & Host & ")")
			End If
		End If
		
		Dim hent As tHostEnt
		Call dllCopy16B(hent.h_name, lPntr)
		If hent.h_addrtype <> AF_INET Or hent.h_length <> 4 Then
			Call mVBOrb.ErrRaise(1, "Address '" & Host & "' is not an INET address")
		End If
		Call dllMoveMem(lPntr, hent.h_addr_list, 4)
		Call dllMoveMem(lPntr, lPntr, 4)
		inet_a2n = lPntr
		Exit Function
ErrHandler: 
		Call mVBOrb.ErrReraise("inet_a2n")
	End Function
	
	'Convert INET address to dot notation
	'IN:            Address
	'RET:           e.g: "127.0.0.1"
	Private Function inet_n2a(ByVal n As Integer) As String
		Dim strAddr As New VB6.FixedLengthString(256)
		Call lstrcpy(strAddr.Value, inet_ntoa(n))
		inet_n2a = leftstr(strAddr.Value)
	End Function
	
	'Open an internet stream socket
	'IN:    HostPort    hostname:port
	'IN:    DefaultPort default port
	Public Sub openSocket(ByRef HostPort As String, Optional ByVal DefaultPort As String = "")
		On Error GoTo ErrHandler
		
		If sockFd <> -1 Then
			Call mVBOrb.ErrRaise(1, "Is already open")
		End If
		sockFd = dllSocket(AF_INET, SOCK_STREAM, 0)
		If sockFd < 0 Then
			If Err.LastDllError = WSANOTINITIALISED Then
				Call mVBOrb.ErrRaise(Err.LastDllError, "WSA not initialised")
			Else
				Call mVBOrb.ErrRaise(Err.LastDllError, "socket() failed")
			End If
		End If
		
		Dim sHost As String
		Dim iPort As Short
		Dim pos As Integer
		pos = InStr(HostPort, ":")
		If pos = 0 Then
			sHost = HostPort
			If DefaultPort = "" Then
				Call mVBOrb.ErrRaise(1, HostPort & ", Port missing")
			End If
			iPort = inet_p2n(DefaultPort)
		Else
			sHost = Left(HostPort, pos - 1)
			iPort = inet_p2n(Mid(HostPort, pos + 1))
		End If
		
		sockAddr.sin_family = AF_INET
		sockAddr.sin_addr = IIf(sHost = "", INADDR_ANY, inet_a2n(sHost))
		sockAddr.sin_port = DLLhtons(iPort)
		sockAddr.sin_zero0 = 0
		sockAddr.sin_zero1 = 0
		
		bIsOutgoing = False
		bIsListen = False
		bIsIncoming = False
		
		Exit Sub
ErrHandler: 
		Call mVBOrb.ErrSave()
		Resume ErrClose
ErrClose: 
		On Error Resume Next
		If sockFd <> -1 Then Call closeSocket()
		On Error GoTo 0
		Call mVBOrb.ErrLoad()
		Call mVBOrb.ErrReraise("openSocket")
	End Sub
	
	Public Function isOpen() As Boolean
		isOpen = (sockFd <> -1)
	End Function
	
	'Close the socket
	Public Sub closeSocket()
		On Error GoTo ErrHandler
		If sockFd = -1 Then
			Call mVBOrb.ErrRaise(1, "Is already closed")
		End If
		If dllCloseSocket(sockFd) <> 0 Then
			If Err.LastDllError = WSAENOTSOCK Then '10038
				Call mVBOrb.ErrRaise(Err.LastDllError, "fd is not a socket")
			Else
				Call mVBOrb.ErrRaise(Err.LastDllError, "closesocket() failed")
			End If
		End If
		sockFd = -1
		Exit Sub
ErrHandler: 
		sockFd = -1
		Call mVBOrb.ErrReraise("Socket.close")
	End Sub
	
	Public ReadOnly Property socketFd() As Integer
		Get
			socketFd = sockFd
		End Get
	End Property
	
	Public ReadOnly Property socketHost() As String
		Get
			socketHost = inet_n2a(sockAddr.sin_addr)
		End Get
	End Property
	
	Public ReadOnly Property socketPort() As String
		Get
			Dim lPortNo As Integer
			lPortNo = DLLntohs(sockAddr.sin_port)
			If lPortNo < 0 Then
				lPortNo = lPortNo + &H10000
			End If
			socketPort = CStr(lPortNo)
		End Get
	End Property
	
	Public Sub connectSocket()
		On Error GoTo ErrHandler
		If DLLconnect(sockFd, sockAddr, 16) < 0 Then
			If Err.LastDllError = WSAECONNREFUSED Then '10061, no port listener
				Call mVBOrb.VBOrb.raiseTRANSIENT(0, mVBOrb.VBOrb.CompletedNO, "Connection refused")
			ElseIf Err.LastDllError = WSAEHOSTUNREACH Then  '10065
				Call mVBOrb.VBOrb.raiseTRANSIENT(0, mVBOrb.VBOrb.CompletedNO, "Host unreached")
			ElseIf Err.LastDllError = WSAETIMEDOUT Then  '1060, listener overloaded
				Call mVBOrb.VBOrb.raiseTRANSIENT(0, mVBOrb.VBOrb.CompletedNO, "Connection timed out")
			Else
				Call mVBOrb.VBOrb.raiseTRANSIENT(0, mVBOrb.VBOrb.CompletedNO, "Connection failed (" & CStr(Err.LastDllError) & ")")
			End If
		End If
		bIsOutgoing = True
		Exit Sub
ErrHandler: 
		Call mVBOrb.ErrReraise("Socket.connect", socketHost() & ":" & socketPort())
	End Sub
	
	Public Sub initConnect(ByRef HostPort As String, Optional ByVal DefaultPort As String = "")
		On Error Resume Next
		Call openSocket(HostPort, DefaultPort)
		If Err.Number <> 0 Then GoTo ErrClose
		Call connectSocket()
		If Err.Number <> 0 Then GoTo ErrClose
		Exit Sub
ErrClose: 
		Call mVBOrb.ErrSave()
		If sockFd <> -1 Then Call closeSocket()
		On Error GoTo 0
		Call mVBOrb.ErrLoad()
		Call mVBOrb.ErrReraise("Socket.initConnect")
	End Sub
	
	Public Function isOutgoing() As Boolean
		isOutgoing = bIsOutgoing
	End Function
	
	Public Sub termConnect()
		On Error GoTo ErrHandler
		Call closeSocket()
		bIsOutgoing = False
		Exit Sub
ErrHandler: 
		bIsOutgoing = False
		Call mVBOrb.ErrReraise("termConnect")
	End Sub
	
	Public Sub sendBuffer(ByRef buffer() As Byte, ByVal bufLen As Integer)
		On Error GoTo ErrHandler
		If DLLsend(sockFd, buffer(0), bufLen, 0) <> bufLen Then
			'Error WSAENOBUFS (10055) bei mehr als 20 MByte??? Holger Beer
			'Beim Empfang soll die Verbindung abbrechen bei mehr als 20MByte???
			If Err.LastDllError = WSAECONNABORTED Then
				'Server has been closed
				Call mVBOrb.VBOrb.raiseTRANSIENT(0, mVBOrb.VBOrb.CompletedNO, "Connection has been aborted")
			ElseIf Err.LastDllError = WSAECONNRESET Then 
				'Connection was already closed by Server
				Call mVBOrb.VBOrb.raiseTRANSIENT(0, mVBOrb.VBOrb.CompletedNO, "Connection has been closed")
			Else
				Call mVBOrb.ErrRaise(Err.LastDllError, "send() failed")
			End If
		End If
		Exit Sub
ErrHandler: 
		Call mVBOrb.ErrReraise("sendBuffer")
	End Sub
	
	'UPGRADE_NOTE: str was upgraded to str_Renamed. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="A9E4979A-37FA-4718-9994-97DD76ED70A7"'
	Public Sub sendString(ByRef str_Renamed As String)
		On Error GoTo ErrHandler
		If DLLsendstr(sockFd, str_Renamed, Len(str_Renamed), 0) <> Len(str_Renamed) Then
			Call mVBOrb.ErrRaise(Err.LastDllError, "sendstr() failed")
		End If
		Exit Sub
ErrHandler: 
		Call mVBOrb.ErrReraise("sendString")
	End Sub
	
	Public Sub recvBuffer(ByRef buffer() As Byte, ByVal bufOff As Integer, ByVal bufLen As Integer)
		On Error GoTo ErrHandler
		If bufOff + bufLen > UBound(buffer) + 1 Then
			Call mVBOrb.ErrRaise(1, "buffer to small")
		End If
		Dim recvLen As Integer
		'Auf die Daten warten, wenn es sein muﬂ, ewig!
		'10000 Bytes werden nicht auf einmal empfangen.
		While bufLen > 0
			recvLen = DLLrecv(sockFd, buffer(bufOff), bufLen, 0)
			If recvLen <= 0 Or recvLen > bufLen Then
				If recvLen = 0 Then
					'Server ends after accept
					Call mVBOrb.ErrRaise(1, "Connection closed") 'by Client or Server
				ElseIf Err.LastDllError = WSAESHUTDOWN Then 
					'Accepted but unused
					Call mVBOrb.ErrRaise(Err.LastDllError, "Connection closed by Server")
				ElseIf Err.LastDllError = WSAECONNRESET Then 
					'Server ends during accept
					Call mVBOrb.ErrRaise(Err.LastDllError, "Connection not accepted by Server")
				Else
					Call mVBOrb.ErrRaise(Err.LastDllError, "recv(" & CStr(bufLen) & ") failed")
				End If
			End If
			bufOff = bufOff + recvLen
			bufLen = bufLen - recvLen
		End While
		Exit Sub
ErrHandler: 
		Call mVBOrb.ErrReraise("recvBuffer")
	End Sub
	
	Public Function recvCurrentLen() As Integer
		On Error GoTo ErrHandler
		Dim FIONREAD As Integer
		FIONREAD = &H40040000 + CInt(Asc("f")) * &H100 + 127
		If DLLioctlLong(sockFd, FIONREAD, recvCurrentLen) = -1 Then
			Call mVBOrb.ErrRaise(Err.LastDllError, "ioctl() failed")
		End If
		Exit Function
ErrHandler: 
		Call mVBOrb.ErrReraise("recvCurrentLen")
	End Function
	
	Public Function recvBufall(ByRef buffer() As Byte) As Integer
		On Error GoTo ErrHandler
		Dim recvLen As Integer
		
		'Auf die Daten warten, wenn es sein muﬂ, ewig
		recvLen = DLLrecv(sockFd, buffer(0), 0, 0)
		If recvLen < 0 Then
			Call mVBOrb.ErrRaise(Err.LastDllError, "recv(0) failed")
		End If
		
		'Wieviele sind es?
		recvLen = recvCurrentLen()
		
		If recvLen <= 0 Then
			recvBufall = 0
			Exit Function
		End If
		
		If recvLen > UBound(buffer) + 1 Then
			ReDim buffer(recvLen - 1)
		End If
		'Daten holen
		recvLen = DLLrecv(sockFd, buffer(0), recvLen, 0)
		If recvLen < 0 Then
			Call mVBOrb.ErrRaise(Err.LastDllError, "recv(" & recvLen & ") failed")
		End If
		recvBufall = recvLen
		
		Exit Function
ErrHandler: 
		Call mVBOrb.ErrReraise("recvBufall")
	End Function
	
	Public Function recvString() As String
		On Error GoTo ErrHandler
		Dim buffer() As Byte
		Dim lSize As Integer
		
		ReDim buffer(0)
		lSize = recvBufall(buffer)
		'recv_string = leftstr(StrConv(buffer, vbUnicode))
		If lSize = 0 Then
			recvString = ""
		Else
			'UPGRADE_ISSUE: Constant vbUnicode was not upgraded. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="55B59875-9A95-4B71-9D6A-7C294BF7139D"'
			recvString = StrConv(System.Text.UnicodeEncoding.Unicode.GetString(buffer), vbUnicode)
		End If
		Exit Function
ErrHandler: 
		Call mVBOrb.ErrReraise("recvString")
	End Function
	
	'Host can be "". Use socketHost to retrieve the actual host after bind.
	'Port can be "0". Use socketPort to retrieve the actual port after bind.
	Public Sub initBind(ByVal Host As String, ByVal Port As String)
		On Error GoTo ErrHandler
		
		Call openSocket(Host, Port)
		
		Dim lOpt As Integer
		If Left(Port, 1) = "+" Then
			lOpt = 1
			'UPGRADE_ISSUE: LenB function is not supported. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="367764E5-F3F8-4E43-AC3E-7FE0B5E074E2"'
			If dllSetSockOpt(sockFd, SOL_SOCKET, SO_REUSEADDR, lOpt, LenB(lOpt)) <> 0 Then
				Call mVBOrb.ErrRaise(Err.LastDllError, "bind(" & Port & ") failed")
			End If
		End If
		
		'Assign an address to an unbound socket. If sin_port is zero,
		'the system assigns an unused port number automatically.
		'UPGRADE_ISSUE: LenB function is not supported. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="367764E5-F3F8-4E43-AC3E-7FE0B5E074E2"'
		If dllBind(sockFd, sockAddr, LenB(sockAddr)) <> 0 Then
			If Err.LastDllError = WSAEADDRINUSE Then
				Call mVBOrb.ErrRaise(Err.LastDllError, "address in use: " & Port)
			Else 'WSAENOTSOCK, 10038 Socket not opened
				Call mVBOrb.ErrRaise(Err.LastDllError, "bind(" & Port & ") failed")
			End If
		End If
		'If sin_port was 0, we get the system assigned port number here
		Dim addrLen As Integer
		'UPGRADE_ISSUE: LenB function is not supported. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="367764E5-F3F8-4E43-AC3E-7FE0B5E074E2"'
		addrLen = LenB(sockAddr)
		'UPGRADE_ISSUE: LenB function is not supported. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="367764E5-F3F8-4E43-AC3E-7FE0B5E074E2"'
		If dllGetSockName(sockFd, sockAddr, addrLen) <> 0 Or addrLen <> LenB(sockAddr) Then
			Call mVBOrb.ErrRaise(Err.LastDllError, "getsockname() failed")
		End If
		Exit Sub
ErrHandler: 
		Call mVBOrb.ErrSave()
		Resume ErrClose
ErrClose: 
		On Error Resume Next
		If sockFd <> -1 Then Call closeSocket()
		On Error GoTo 0
		Call mVBOrb.ErrLoad()
		Call mVBOrb.ErrReraise("Socket.Bind")
	End Sub
	
	Public Sub startListen()
		On Error GoTo ErrHandler
		'Start listening for client connections
		'20 = queue length for pending connections (avoid error ETIMEDOUT)
		'     (Number of outstanding connection requests
		'     that can be queued at a single TCP/IP port.)
		If dllListen(sockFd, 20) <> 0 Then
			Call mVBOrb.ErrRaise(Err.LastDllError, "listen() failed")
		End If
		bIsListen = True
		Exit Sub
ErrHandler: 
		Call mVBOrb.ErrSave()
		Resume ErrClose
ErrClose: 
		On Error Resume Next
		If sockFd <> -1 Then Call closeSocket()
		On Error GoTo 0
		Call mVBOrb.ErrLoad()
		Call mVBOrb.ErrReraise("startListen")
	End Sub
	
	Public Function isListen() As Boolean
		isListen = bIsListen
	End Function
	
	Public Sub termBind()
		On Error GoTo ErrHandler
		Call closeSocket()
		bIsListen = False
		Exit Sub
ErrHandler: 
		bIsListen = False
		Call mVBOrb.ErrReraise("termBind")
	End Sub
	
	Public Sub initAccept(ByVal oSocket As cOrbSocket)
		On Error GoTo ErrHandler
		Dim addrLen As Integer
		'UPGRADE_ISSUE: LenB function is not supported. Click for more: 'ms-help://MS.VSCC.v90/dv_commoner/local/redirect.htm?keyword="367764E5-F3F8-4E43-AC3E-7FE0B5E074E2"'
		addrLen = LenB(sockAddr)
		sockFd = dllAccept(oSocket.socketFd, sockAddr, addrLen)
		If sockFd = -1 Then
			Call mVBOrb.ErrRaise(Err.LastDllError, "accept() failed")
		End If
		bIsOutgoing = False
		bIsListen = False
		bIsIncoming = True
		Exit Sub
ErrHandler: 
		Call mVBOrb.ErrSave()
		Resume ErrClose
ErrClose: 
		On Error Resume Next
		If sockFd <> -1 Then Call closeSocket()
		On Error GoTo 0
		Call mVBOrb.ErrLoad()
		Call mVBOrb.ErrReraise("Socket.Accept")
	End Sub
	
	Public Function isIncoming() As Boolean
		isIncoming = bIsIncoming
	End Function
	
	Public Sub termAccept()
		On Error GoTo ErrHandler
		Call closeSocket()
		bIsIncoming = False
		Exit Sub
ErrHandler: 
		bIsIncoming = False
		Call mVBOrb.ErrReraise("termAccept")
	End Sub
	
	Public Function recvWait(ByVal lMilliSec As Integer) As Boolean
		On Error GoTo ErrHandler
		Dim timeOut As tTimeVal
		timeOut.tv_usec = (lMilliSec Mod 1000) * 1000
		timeOut.tv_sec = lMilliSec \ 1000
		Dim readFDs As tFDSet
		Dim writeFDs As tFDSet
		Dim exceptFDs As tFDSet
		readFDs.fd_array = sockFd
		readFDs.fd_count = 1
		writeFDs.fd_count = 0
		exceptFDs.fd_array = sockFd
		exceptFDs.fd_count = 1
		If dllSelect(1, readFDs, writeFDs, exceptFDs, timeOut) = -1 Then
			Call mVBOrb.VBOrb.raiseINTERNAL(1, mVBOrb.VBOrb.CompletedMAYBE, "select() failed, " & CStr(Err.LastDllError))
		End If
		If exceptFDs.fd_count <> 0 Then
			Call mVBOrb.VBOrb.raiseINTERNAL(1, mVBOrb.VBOrb.CompletedMAYBE, "select() exception, " & CStr(Err.LastDllError))
		End If
		'Read (True) or TimeOut (False)
		recvWait = (readFDs.fd_count <> 0)
		Exit Function
ErrHandler: 
		Call mVBOrb.ErrReraise("recvWait")
	End Function
End Class