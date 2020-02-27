Attribute VB_Name = "mGridServer"
'Copyright (c) 2002 Martin.Both

'This library is free software; you can redistribute it and/or
'modify it under the terms of the GNU Library General Public
'License as published by the Free Software Foundation; either
'version 2 of the License, or (at your option) any later version.

'This library is distributed in the hope that it will be useful,
'but WITHOUT ANY WARRANTY; without even the implied warranty of
'MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
'Library General Public License for more details.

Option Explicit

Public Sub Main()
    'To keep error handling working please switch following radio
    'button on in your Microsoft Visual Basic environment.
    'Menu: Tools|Options...|General|Break on Unhandled Errors
    'Or in german: Extras|Optionen...|Allgemein|Bei nicht verarbeiteten Fehlern
    On Error GoTo ErrHandler
    
    'Writing start message to logfile
    Dim sLogFile As String
    sLogFile = App.Path & "\GridServer.log"
    Call VBOrb.logMsg(sLogFile, "Starting Server")
    
    'Get an ORB with logging
    Dim oOrb As cOrbImpl
    Set oOrb = VBOrb.init(ORBId:="", OAPort:="1900", LogFile:=sLogFile)
    
    'Connect an object to the ORB. That object implements an interface.
    'That interface is defined in grid.idl file
    Dim oImpl As New c_gridMyServerImpl
    'corbaloc://1.1@HostName:1900/MyServer
    Call oOrb.Connect(oImpl, "MyServer") 'ObjectKey = "MyServer"

    Dim oTestAnyImplTie As c_TestAnyImplTie
    Set oTestAnyImplTie = New c_TestAnyImplTie
    Call oTestAnyImplTie.setDelegate(New cTestAnyImpl)
    Call oOrb.Connect(oTestAnyImplTie, "AnyServer")

    'If you like to show forms here before calling oOrb.run() you must call
    'Show 0 instead of Show 1 because Show 0 does not wait.
    
    'Following call blocks and keep the ORB running until oOrb.shutdown()
    'is called in Form_Unload(). Instead of calling OrbRunLoopOutsideOfDLL()
    'or oOrb.run() you can call oOrb.performWork() by a timer periodically.
    
    'In this example any client can shutdown the server by calling
    'shutdownServer method of MyServer.
    Call OrbRunLoopOutsideOfDLL(oOrb)
    
EndOfServer:
    Call VBOrb.logMsg(sLogFile, "End of Server")
    End
ErrHandler:
    Call VBOrb.logErr(sLogFile, Err, "Main")
    Resume EndOfServer
End Sub

'For debugging purposes:
'If you call oOrb.run() and you stop the application during debugging without
'calling oOrb.shutdown() (e.g. in Form.Unload) the VB environment is hanging
'inside the VBOrb-DLL still waiting for requests. To avoid that behavior
'during debugging please call OrbLoopOutsideOfDLL() instead of oOrb.run().
Public Sub OrbRunLoopOutsideOfDLL(ByVal oOrb As cOrbImpl)
    On Error GoTo ErrHandler
    Do
        Call oOrb.performWork(10)
        DoEvents 'Prevent blocking other window processes
    Loop
ORBisDown:
    Set oOrb = Nothing
    Exit Sub
ErrHandler:
    If VBOrb.ErrIsSystemEx() _
        And Err.Number = (VBOrb.ITF_E_BAD_INV_ORDER_NO Or vbObjectError) Then
        Resume ORBisDown
    End If
    Call VBOrb.ErrReraise(Err, "OrbRunLoopOutsideOfDLL")
End Sub

