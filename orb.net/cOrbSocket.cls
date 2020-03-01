VERSION 1.0 CLASS
BEGIN
  MultiUse = -1  'True
END
Attribute VB_Name = "cOrbSocket"
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

'Set DebugMode = 0 to deactivate debug code in this class
#Const DebugMode = 0

#If DebugMode Then
    Private lClassDebugID As Long
#End If

Private bTermAll As Boolean
Private sockFd As Long
Private sockAddr As tInetAddr
'Origin of socket
Private bIsOutgoing As Boolean 'connect()
Private bIsListen As Boolean   'bind() and listen()
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
Private Type typWSAData
    wVersion As Integer
    wHighVersion As Integer
    szDescription As String * 257
    szSystemStatus As String * 129
    iMaxSockets As Integer
    iMaxUdpDg As Integer
    lpVendorInfo As Long
End Type

'struct  hostent {
'        char    FAR * h_name;           /* official name of host */
'        char    FAR * FAR * h_aliases;  /* alias list */
'        short   h_addrtype;             /* host address type */
'        short   h_length;               /* length of address */
'        char    FAR * FAR * h_addr_list; /* list of addresses */
'#define h_addr  h_addr_list[0]          /* address, for backward compat */
'};
Private Type tHostEnt
    h_name As Long
    h_aliases As Long
    h_addrtype As Integer
    h_length As Integer
    h_addr_list As Long
End Type

Private Const AF_INET As Integer = 2
Private Const SOCK_STREAM As Integer = 1 'stream socket
Private Const INADDR_ANY As Long = 0
Private Const SO_REUSEADDR As Long = 4    'allow local address reuse
Private Const SOL_SOCKET As Long = &HFFFF&  'options for socket level
                            '&HFFFF& <> &HFFFF (Thanks to Holger Beer)

'Windows Sockets definitions of regular Microsoft error constants
Private Const WSAEINTR           As Long = 10004 'WSA_OPERATION_ABORTED
Private Const WSAEBADF           As Long = 10009
Private Const WSAEACCES          As Long = 10013
Private Const WSAEFAULT          As Long = 10014
Private Const WSAEINVAL          As Long = 10022 'WSA_INVALID_PARAMETER
Private Const WSAEMFILE          As Long = 10024

'Windows Sockets definitions of regular Berkeley error constants
Private Const WSAEWOULDBLOCK     As Long = 10035 'WSA_IO_PENDING, WSA_IO_INCOMPLETE
Private Const WSAEINPROGRESS     As Long = 10036
Private Const WSAEALREADY        As Long = 10037
Private Const WSAENOTSOCK        As Long = 10038 'WSA_INVALID_HANDLE
Private Const WSAEDESTADDRREQ    As Long = 10039
Private Const WSAEMSGSIZE        As Long = 10040
Private Const WSAEPROTOTYPE      As Long = 10041
Private Const WSAENOPROTOOPT     As Long = 10042
Private Const WSAEPROTONOSUPPORT As Long = 10043
Private Const WSAESOCKTNOSUPPORT As Long = 10044
Private Const WSAEOPNOTSUPP      As Long = 10045
Private Const WSAEPFNOSUPPORT    As Long = 10046
Private Const WSAEAFNOSUPPORT    As Long = 10047
Private Const WSAEADDRINUSE      As Long = 10048
Private Const WSAEADDRNOTAVAIL   As Long = 10049
Private Const WSAENETDOWN        As Long = 10050
Private Const WSAENETUNREACH     As Long = 10051
Private Const WSAENETRESET       As Long = 10052
Private Const WSAECONNABORTED    As Long = 10053
Private Const WSAECONNRESET      As Long = 10054
Private Const WSAENOBUFS         As Long = 10055 'WSA_NOT_ENOUGH_MEMORY
Private Const WSAEISCONN         As Long = 10056
Private Const WSAENOTCONN        As Long = 10057
Private Const WSAESHUTDOWN       As Long = 10058
Private Const WSAETOOMANYREFS    As Long = 10059
Private Const WSAETIMEDOUT       As Long = 10060
Private Const WSAECONNREFUSED    As Long = 10061
Private Const WSAELOOP           As Long = 10062
Private Const WSAENAMETOOLONG    As Long = 10063
Private Const WSAEHOSTDOWN       As Long = 10064
Private Const WSAEHOSTUNREACH    As Long = 10065
Private Const WSAENOTEMPTY       As Long = 10066
Private Const WSAEPROCLIM        As Long = 10067
Private Const WSAEUSERS          As Long = 10068
Private Const WSAEDQUOT          As Long = 10069
Private Const WSAESTALE          As Long = 10070
Private Const WSAEREMOTE         As Long = 10071

'Extended Windows Sockets error constant definitions
Private Const WSASYSNOTREADY     As Long = 10091
Private Const WSAVERNOTSUPPORTED As Long = 10092
Private Const WSANOTINITIALISED  As Long = 10093
Private Const WSAEDISCON         As Long = 10101
Private Const WSAENOMORE         As Long = 10102
Private Const WSAECANCELLED      As Long = 10103
Private Const WSAEINVALIDPROCTABLE   As Long = 10104
Private Const WSAEINVALIDPROVIDER    As Long = 10105
Private Const WSAEPROVIDERFAILEDINIT As Long = 10106
Private Const WSASYSCALLFAILURE  As Long = 10107
Private Const WSASERVICE_NOT_FOUND   As Long = 10108
Private Const WSATYPE_NOT_FOUND  As Long = 10109
Private Const WSA_E_NO_MORE      As Long = 10110
Private Const WSA_E_CANCELLED    As Long = 10111
Private Const WSAEREFUSED        As Long = 10112

'Error return codes from gethostbyname() and gethostbyaddr()
'(when using the resolver). Note that these errors are
'retrieved via WSAGetLastError()

'Authoritative Answer: Host not found
Private Const WSAHOST_NOT_FOUND  As Long = 11001

'Non-Authoritative: Host not found, or SERVERFAIL
Private Const WSATRY_AGAIN       As Long = 11002

'Non-recoverable errors, FORMERR, REFUSED, NOTIMP
Private Const WSANO_RECOVERY     As Long = 11003

'Valid name, no data record of requested type
Private Const WSANO_DATA         As Long = 11004

'no address, look for MX record
Private Const WSANO_ADDRESS      As Long = 11004

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
Private Type tInetAddr
    sin_family As Integer
    sin_port As Integer
    sin_addr As Long
    sin_zero0 As Long
    sin_zero1 As Long
End Type

'Structure used in select() call
'struct timeval {
'    long    tv_sec;         /* seconds */
'    long    tv_usec;        /* and microseconds */
'};
Private Type tTimeVal
    tv_sec As Long
    tv_usec As Long
End Type

'typedef struct fd_set {
'        u_int   fd_count;               /* how many are SET? */
'        SOCKET  fd_array[FD_SETSIZE];   /* an array of SOCKETs */
'} fd_set;
Private Type tFDSet
    fd_count As Long
    fd_array As Long
End Type

'lstrcpyA();
Private Declare Function lstrcpy Lib "kernel32" Alias "lstrcpyA" _
    (ByVal lpString1 As String, ByVal lpString2 As Long) As Long

'CopyRect();
Private Declare Function dllCopy16B Lib "user32" Alias "CopyRect" _
    (ByRef lpDestRect As Any, ByVal lpSourceRect As Long) As Long

'RtlMoveMemory();
Private Declare Sub dllMoveMem Lib "kernel32" Alias "RtlMoveMemory" _
    (ByRef hpvDest As Any, ByVal hpvSource As Long, ByVal cbCopy As Long)

'int PASCAL FAR WSAStartup(WORD wVersionRequired, LPWSADATA lpWSAData);
Private Declare Function WSAStartup Lib "Ws2_32.dll" _
    (ByVal wVersionRequired As Integer, lpWSAData As typWSAData) As Long

'int WSACleanup(void);
Private Declare Function WSACleanup Lib "Ws2_32.dll" () As Long

'int PASCAL FAR gethostname(char FAR * name, int namelen);
Private Declare Function dllHostName Lib "Ws2_32.dll" Alias "gethostname" _
    (ByVal name As String, ByVal nameLen As Long) As Long
'Declare Function GetComputerName Lib "kernel32" Alias "GetComputerNameA" _
'   (ByVal lpBuffer As String, ByRef nSize As Long) As Long

'unsigned long PASCAL FAR inet_addr (const char FAR * cp);
Private Declare Function inet_addr Lib "Ws2_32.dll" _
    (ByVal cp As String) As Long

'char FAR * PASCAL FAR inet_ntoa (struct in_addr in);
Private Declare Function inet_ntoa Lib "Ws2_32.dll" _
    (ByVal inaddr As Long) As Long

'struct hostent FAR * PASCAL FAR gethostbyname(const char FAR * name);
Private Declare Function gethostbyname Lib "Ws2_32.dll" _
    (ByVal name As String) As Long

'SOCKET PASCAL FAR socket (int af, int type, int protocol);
Private Declare Function dllSocket Lib "Ws2_32.dll" Alias "socket" _
    (ByVal af As Long, ByVal stype As Long, ByVal protocol As Long) As Long

'int PASCAL FAR closesocket (SOCKET s);
Private Declare Function dllCloseSocket Lib "Ws2_32.dll" _
    Alias "closesocket" (ByVal sockFd As Long) As Long

'u_short PASCAL FAR htons (u_short hostshort);
Private Declare Function DLLhtons Lib "Ws2_32.dll" Alias "htons" _
    (ByVal iPort As Integer) As Integer

'u_short PASCAL FAR ntohs (u_short netshort);
Private Declare Function DLLntohs Lib "Ws2_32.dll" Alias "ntohs" _
    (ByVal iPort As Integer) As Integer

'u_long PASCAL FAR htonl (u_long hostlong);
'Private Declare Function htonl Lib "Ws2_32.dll" _
'    (ByVal lhost As Long) As Long

'u_long PASCAL FAR ntohl (u_long netlong);
'Private Declare Function ntohl Lib "Ws2_32.dll" _
'    (ByVal lnet As Long) As Long

'int PASCAL FAR connect (SOCKET s, const struct sockaddr FAR *name, int namelen);
Private Declare Function DLLconnect Lib "Ws2_32.dll" Alias "connect" _
    (ByVal sockFd As Long, ByRef sockAddr As tInetAddr, ByVal saddrlen As Long) _
    As Long

'int PASCAL FAR ioctlsocket (SOCKET s, long cmd, u_long FAR *argp);
Private Declare Function DLLioctlLong Lib "Ws2_32.dll" Alias "ioctlsocket" _
    (ByVal sockFd As Long, ByVal cmd As Long, ByRef arg As Long) As Long

'int PASCAL FAR recv (SOCKET s, char FAR * buf, int len, int flags);
Private Declare Function DLLrecv Lib "Ws2_32.dll" Alias "recv" _
    (ByVal sockFd As Long, ByRef buf As Any, _
    ByVal bufLen As Long, ByVal flags As Long) As Long

'int PASCAL FAR send (SOCKET s, const char FAR * buf, int len, int flags);
Private Declare Function DLLsend Lib "Ws2_32.dll" Alias "send" _
    (ByVal sockFd As Long, ByRef buf As Any, _
    ByVal bufLen As Long, ByVal flags As Long) As Long
Private Declare Function DLLsendstr Lib "Ws2_32.dll" Alias "send" _
    (ByVal sockFd As Long, ByVal buf As String, _
    ByVal bufLen As Long, ByVal flags As Long) As Long

'int PASCAL FAR getsockopt (SOCKET s, int level, int optname,
'   char FAR * optval, int FAR *optlen);
'Private Declare Function dllGetSockOpt Lib "Ws2_32.dll" Alias "getsockopt" _
'    (ByVal sockFd As Long, ByVal level As Long, ByVal optName As Long, _
'    ByRef optVal As Long, ByRef optLen As Long) As Long
'int PASCAL FAR setsockopt (SOCKET s, int level, int optname,
'   const char FAR * optval, int optlen);
'value = 65536 by Holger Beer
'ret = dllSetSockOpt(sockfd, SOL_SOCKET, &H1001&, value, 4)
Private Declare Function dllSetSockOpt Lib "Ws2_32.dll" Alias "setsockopt" _
    (ByVal sockFd As Long, ByVal level As Long, ByVal optName As Long, _
    ByRef optVal As Long, ByVal optLen As Long) As Long

'int PASCAL FAR bind (SOCKET s, const struct sockaddr FAR *addr, int namelen);
Private Declare Function dllBind Lib "Ws2_32.dll" Alias "bind" _
    (ByVal sockFd As Long, ByRef sockAddr As tInetAddr, ByVal nameLen As Long) _
    As Long
    
'int PASCAL FAR getsockname (SOCKET s, struct sockaddr FAR *name,
'    int FAR * namelen);
Private Declare Function dllGetSockName Lib "Ws2_32.dll" Alias "getsockname" _
    (ByVal sockFd As Long, ByRef sockAddr As tInetAddr, ByRef addrLen As Long) _
    As Long

'int PASCAL FAR listen (SOCKET s, int backlog);
Private Declare Function dllListen Lib "Ws2_32.dll" Alias "listen" _
    (ByVal sockFd As Long, ByVal backlog As Long) As Long

'SOCKET PASCAL FAR accept (SOCKET s, struct sockaddr FAR *addr, int FAR *addrlen);
Private Declare Function dllAccept Lib "Ws2_32.dll" Alias "accept" _
    (ByVal sockFd As Long, ByRef sockAddr As tInetAddr, ByRef addrLen As Long) _
    As Long

'int PASCAL FAR select (int nfds, fd_set FAR *readfds, fd_set FAR *writefds,
'   fd_set FAR *exceptfds, const struct timeval FAR *timeout);
Private Declare Function dllSelect Lib "Ws2_32.dll" Alias "select" _
    (ByVal nfds As Long, ByRef readFDs As tFDSet, ByRef writeFDs As tFDSet, _
    ByRef exceptFDs As tFDSet, ByRef timeOut As tTimeVal) As Long

Private Sub Class_Initialize()
    #If DebugMode Then
        lClassDebugID = mVBOrb.getNextClassDebugID()
        Debug.Print "'" & TypeName(Me) & "' " & lClassDebugID & " initialized"
    #End If
    Call initWSA
    bTermAll = False
    sockFd = -1
End Sub

Private Sub Class_Terminate()
    'Release something which VB cannot know if required
    If sockFd <> -1 Then
        Call closeSocket
    End If
    Call termWSA(bTermAll)
    #If DebugMode Then
        Debug.Print "'" & TypeName(Me) & "' " & CStr(lClassDebugID) & " terminated"
    #End If
End Sub

#If DebugMode Then
    Friend Property Get ClassDebugID() As Long
        ClassDebugID = lClassDebugID
    End Property
#End If

Public Sub initTermAll(ByVal bNewTermAll As Boolean)
    bTermAll = bNewTermAll
End Sub

Private Sub initWSA()
    Dim wsaData As typWSAData
    Dim ret As Long
    
    'Version 2.0
    ret = WSAStartup(2, wsaData)
    If ret <> 0 Then
        Err.Raise Err.LastDllError, Description:="WSAStartup(2.0)"
    End If
End Sub

Private Sub termWSA(bTermAll As Boolean)
    If bTermAll Then
        While (WSACleanup() = 0)
        Wend
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
Private Function leftstr(ByRef str As String) As String
    leftstr = Left$(str, InStr(str, vbNullChar) - 1)
End Function

'Get my host name or IP address
Public Function getHostName(ByVal dotStyle As Boolean) As String
    On Error GoTo ErrHandler
    Dim sHostName As String * 256
    'Dim llen As Long
    'llen = 256
    'Call GetComputerName(sHostName, llen)
    'getHostName= Left$(sHostName, llen)
    If dllHostName(sHostName, 256) <> 0 Then
        Call mVBOrb.ErrRaise(Err.LastDllError, "gethostname() failed")
    End If
    If dotStyle Then
        Dim addr As Long
        addr = inet_a2n(leftstr(sHostName))
        getHostName = inet_n2a(addr)
    Else
        getHostName = leftstr(sHostName)
    End If
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("getHostName")
End Function

' Convert Portnumber
' IN:   sPort  e.g: "80"
' RET:           Portnumber
Private Function inet_p2n(ByVal sPort As String) As Integer
    On Error GoTo ErrHandler
    If Len(sPort) = 0 Then
        Call mVBOrb.ErrRaise(1, "Portnumber is missing")
    End If
    Dim lPort As Long
    On Error Resume Next
    lPort = CLng(sPort)
    If Err.Number <> 0 Then
        On Error GoTo ErrHandler 'Is calling Err.Clear()
        Call mVBOrb.ErrRaise(6, "Portnumber " & sPort & " is invalid")
    End If
    On Error GoTo ErrHandler
    If lPort < 0 Or lPort >= &H10000 Then
        Call mVBOrb.ErrRaise(6, "Portnumber " & sPort & " is out of range")
    End If
    inet_p2n = IIf(lPort <= &H7FFF&, lPort, lPort - &H10000)
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("inet_p2n")
End Function

'Convert dot notation or host name to INET address
'IN:    host    Example: "127.0.0.1" or "localhost"
'RET:           Address or -1
Private Function inet_a2n(ByVal Host As String) As Long
    On Error GoTo ErrHandler
    Dim lPntr As Long
    
    If Host = "" Then
        Host = "localhost"  '127.0.0.1
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
            Call mVBOrb.VBOrb.raiseTRANSIENT(99, mVBOrb.VBOrb.CompletedNO, _
                "Unknown host " & Host)
        Else
            Call mVBOrb.ErrRaise(Err.LastDllError, _
                "gethostbyname(" & Host & ")")
        End If
    End If
    
    Dim hent As tHostEnt
    Call dllCopy16B(hent.h_name, lPntr)
    If hent.h_addrtype <> AF_INET Or hent.h_length <> 4 Then
        Call mVBOrb.ErrRaise(1, _
            "Address '" & Host & "' is not an INET address")
    End If
    Call dllMoveMem(lPntr, hent.h_addr_list, 4&)
    Call dllMoveMem(lPntr, lPntr, 4&)
    inet_a2n = lPntr
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("inet_a2n")
End Function

'Convert INET address to dot notation
'IN:            Address
'RET:           e.g: "127.0.0.1"
Private Function inet_n2a(ByVal n As Long) As String
    Dim strAddr As String * 256
    Call lstrcpy(strAddr, inet_ntoa(n))
    inet_n2a = leftstr(strAddr)
End Function

'Open an internet stream socket
'IN:    HostPort    hostname:port
'IN:    DefaultPort default port
Public Sub openSocket(ByRef HostPort As String, _
    Optional ByVal DefaultPort As String = "")
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
    Dim iPort As Integer
    Dim pos As Long
    pos = InStr(HostPort, ":")
    If pos = 0 Then
        sHost = HostPort
        If DefaultPort = "" Then
            Call mVBOrb.ErrRaise(1, HostPort & ", Port missing")
        End If
        iPort = inet_p2n(DefaultPort)
    Else
        sHost = Left$(HostPort, pos - 1)
        iPort = inet_p2n(Mid$(HostPort, pos + 1))
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
    Call mVBOrb.ErrSave
    Resume ErrClose
ErrClose:
    On Error Resume Next
    If sockFd <> -1 Then Call closeSocket
    On Error GoTo 0
    Call mVBOrb.ErrLoad
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

Public Property Get socketFd() As Long
    socketFd = sockFd
End Property

Public Property Get socketHost() As String
    socketHost = inet_n2a(sockAddr.sin_addr)
End Property

Public Property Get socketPort() As String
    Dim lPortNo As Long
    lPortNo = DLLntohs(sockAddr.sin_port)
    If lPortNo < 0 Then
        lPortNo = lPortNo + &H10000
    End If
    socketPort = lPortNo
End Property

Public Sub connectSocket()
    On Error GoTo ErrHandler
    If DLLconnect(sockFd, sockAddr, 16) < 0 Then
        If Err.LastDllError = WSAECONNREFUSED Then '10061, no port listener
            Call mVBOrb.VBOrb.raiseTRANSIENT(0, mVBOrb.VBOrb.CompletedNO, _
                "Connection refused")
        ElseIf Err.LastDllError = WSAEHOSTUNREACH Then '10065
            Call mVBOrb.VBOrb.raiseTRANSIENT(0, mVBOrb.VBOrb.CompletedNO, _
                "Host unreached")
        ElseIf Err.LastDllError = WSAETIMEDOUT Then '1060, listener overloaded
            Call mVBOrb.VBOrb.raiseTRANSIENT(0, mVBOrb.VBOrb.CompletedNO, _
                "Connection timed out")
        Else
            Call mVBOrb.VBOrb.raiseTRANSIENT(0, mVBOrb.VBOrb.CompletedNO, _
                "Connection failed (" & CStr(Err.LastDllError) & ")")
        End If
    End If
    bIsOutgoing = True
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("Socket.connect", socketHost() & ":" & socketPort())
End Sub

Public Sub initConnect(ByRef HostPort As String, _
    Optional ByVal DefaultPort As String = "")
    On Error Resume Next
    Call openSocket(HostPort, DefaultPort)
    If Err.Number <> 0 Then GoTo ErrClose
    Call connectSocket
    If Err.Number <> 0 Then GoTo ErrClose
    Exit Sub
ErrClose:
    Call mVBOrb.ErrSave
    If sockFd <> -1 Then Call closeSocket
    On Error GoTo 0
    Call mVBOrb.ErrLoad
    Call mVBOrb.ErrReraise("Socket.initConnect")
End Sub

Public Function isOutgoing() As Boolean
    isOutgoing = bIsOutgoing
End Function

Public Sub termConnect()
    On Error GoTo ErrHandler
    Call closeSocket
    bIsOutgoing = False
    Exit Sub
ErrHandler:
    bIsOutgoing = False
    Call mVBOrb.ErrReraise("termConnect")
End Sub

Public Sub sendBuffer(ByRef buffer() As Byte, ByVal bufLen As Long)
    On Error GoTo ErrHandler
    If DLLsend(sockFd, buffer(0), bufLen, 0) <> bufLen Then
        'Error WSAENOBUFS (10055) bei mehr als 20 MByte??? Holger Beer
        'Beim Empfang soll die Verbindung abbrechen bei mehr als 20MByte???
        If Err.LastDllError = WSAECONNABORTED Then
            'Server has been closed
            Call mVBOrb.VBOrb.raiseTRANSIENT(0, mVBOrb.VBOrb.CompletedNO, _
                "Connection has been aborted")
        ElseIf Err.LastDllError = WSAECONNRESET Then
            'Connection was already closed by Server
            Call mVBOrb.VBOrb.raiseTRANSIENT(0, mVBOrb.VBOrb.CompletedNO, _
                "Connection has been closed")
        Else
            Call mVBOrb.ErrRaise(Err.LastDllError, "send() failed")
        End If
    End If
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("sendBuffer")
End Sub

Public Sub sendString(ByRef str As String)
    On Error GoTo ErrHandler
    If DLLsendstr(sockFd, str, Len(str), 0) <> Len(str) Then
        Call mVBOrb.ErrRaise(Err.LastDllError, "sendstr() failed")
    End If
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("sendString")
End Sub

Public Sub recvBuffer(ByRef buffer() As Byte, ByVal bufOff As Long, _
    ByVal bufLen As Long)
    On Error GoTo ErrHandler
    If bufOff + bufLen > UBound(buffer) + 1 Then
        Call mVBOrb.ErrRaise(1, "buffer to small")
    End If
    Dim recvLen As Long
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
                Call mVBOrb.ErrRaise(Err.LastDllError, _
                    "Connection closed by Server")
            ElseIf Err.LastDllError = WSAECONNRESET Then
                'Server ends during accept
                Call mVBOrb.ErrRaise(Err.LastDllError, _
                    "Connection not accepted by Server")
            Else
                Call mVBOrb.ErrRaise(Err.LastDllError, _
                    "recv(" & CStr(bufLen) & ") failed")
            End If
        End If
        bufOff = bufOff + recvLen
        bufLen = bufLen - recvLen
    Wend
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrReraise("recvBuffer")
End Sub

Public Function recvCurrentLen() As Long
    On Error GoTo ErrHandler
    Dim FIONREAD As Long
    FIONREAD = &H40040000 + CLng(Asc("f")) * &H100 + 127
    If DLLioctlLong(sockFd, FIONREAD, recvCurrentLen) = -1 Then
        Call mVBOrb.ErrRaise(Err.LastDllError, "ioctl() failed")
    End If
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("recvCurrentLen")
End Function

Public Function recvBufall(ByRef buffer() As Byte) As Long
    On Error GoTo ErrHandler
    Dim recvLen As Long
    
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
        ReDim buffer(0 To recvLen - 1)
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
    Dim lSize As Long
    
    ReDim buffer(0 To 0)
    lSize = recvBufall(buffer)
    'recv_string = leftstr(StrConv(buffer, vbUnicode))
    If lSize = 0 Then
        recvString = ""
    Else
        recvString = StrConv(buffer, vbUnicode)
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
    
    If Left$(Port, 1) = "+" Then
        Dim lOpt As Long
        lOpt = 1
        If dllSetSockOpt(sockFd, SOL_SOCKET, SO_REUSEADDR, lOpt, LenB(lOpt)) <> 0 Then
            Call mVBOrb.ErrRaise(Err.LastDllError, _
                "bind(" & Port & ") failed")
        End If
    End If
    
    'Assign an address to an unbound socket. If sin_port is zero,
    'the system assigns an unused port number automatically.
    If dllBind(sockFd, sockAddr, LenB(sockAddr)) <> 0 Then
        If Err.LastDllError = WSAEADDRINUSE Then
            Call mVBOrb.ErrRaise(Err.LastDllError, "address in use: " & Port)
        Else 'WSAENOTSOCK, 10038 Socket not opened
            Call mVBOrb.ErrRaise(Err.LastDllError, "bind(" & Port & ") failed")
        End If
    End If
    'If sin_port was 0, we get the system assigned port number here
    Dim addrLen As Long
    addrLen = LenB(sockAddr)
    If dllGetSockName(sockFd, sockAddr, addrLen) <> 0 _
        Or addrLen <> LenB(sockAddr) Then
        Call mVBOrb.ErrRaise(Err.LastDllError, "getsockname() failed")
    End If
    Exit Sub
ErrHandler:
    Call mVBOrb.ErrSave
    Resume ErrClose
ErrClose:
    On Error Resume Next
    If sockFd <> -1 Then Call closeSocket
    On Error GoTo 0
    Call mVBOrb.ErrLoad
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
    Call mVBOrb.ErrSave
    Resume ErrClose
ErrClose:
    On Error Resume Next
    If sockFd <> -1 Then Call closeSocket
    On Error GoTo 0
    Call mVBOrb.ErrLoad
    Call mVBOrb.ErrReraise("startListen")
End Sub

Public Function isListen() As Boolean
    isListen = bIsListen
End Function

Public Sub termBind()
    On Error GoTo ErrHandler
    Call closeSocket
    bIsListen = False
    Exit Sub
ErrHandler:
    bIsListen = False
    Call mVBOrb.ErrReraise("termBind")
End Sub

Public Sub initAccept(ByVal oSocket As cOrbSocket)
    On Error GoTo ErrHandler
    Dim addrLen As Long
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
    Call mVBOrb.ErrSave
    Resume ErrClose
ErrClose:
    On Error Resume Next
    If sockFd <> -1 Then Call closeSocket
    On Error GoTo 0
    Call mVBOrb.ErrLoad
    Call mVBOrb.ErrReraise("Socket.Accept")
End Sub

Public Function isIncoming() As Boolean
    isIncoming = bIsIncoming
End Function

Public Sub termAccept()
    On Error GoTo ErrHandler
    Call closeSocket
    bIsIncoming = False
    Exit Sub
ErrHandler:
    bIsIncoming = False
    Call mVBOrb.ErrReraise("termAccept")
End Sub

Public Function recvWait(ByVal lMilliSec As Long) As Boolean
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
        Call mVBOrb.VBOrb.raiseINTERNAL(1, mVBOrb.VBOrb.CompletedMAYBE, _
            "select() failed, " & CStr(Err.LastDllError))
    End If
    If exceptFDs.fd_count <> 0 Then
        Call mVBOrb.VBOrb.raiseINTERNAL(1, mVBOrb.VBOrb.CompletedMAYBE, _
            "select() exception, " & CStr(Err.LastDllError))
    End If
    'Read (True) or TimeOut (False)
    recvWait = (readFDs.fd_count <> 0)
    Exit Function
ErrHandler:
    Call mVBOrb.ErrReraise("recvWait")
End Function

