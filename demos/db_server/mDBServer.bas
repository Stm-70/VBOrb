Attribute VB_Name = "mDBServer"
Option Explicit

Public oTableHead As c_DBColumnDescriptorSeq
Public oTableRows As c_DBColumnDataSeqSeq

Public Sub Main()
    'To keep error handling working please switch following radio
    'button on in your Microsoft Visual Basic environment.
    'Menu: Tools|Options...|General|Break on Unhandled Errors
    'Or in german: Extras|Optionen...|Allgemein|Bei nicht verarbeiteten Fehlern
    On Error GoTo ErrHandler
    
    'Writing start message to logfile
    Dim sLogFile As String
    sLogFile = App.Path & "\DBServer.log"
    Call VBOrb.logMsg(sLogFile, "Starting Server")
    
    'Load demo database
    Call loadTable
    
    'Get an ORB with logging
    Dim oOrb As cOrbImpl
    Set oOrb = VBOrb.init(ORBId:="", OAPort:="1900", LogFile:=sLogFile)
    
    'Connect an object to the ORB. That object implements an interface.
    'That interface is defined in database.idl file
    Dim oImpl As c_DBManagerImpl
    Set oImpl = New c_DBManagerImpl
    'corbaloc://1.1@HostName:1900/DBManager
    Call oOrb.Connect(oImpl, "DBManager") 'ObjectKey = "DBManager"

    'If you like to show forms here before calling oOrb.run() you must call
    'Show 0 instead of Show 1 because Show 0 does not wait.
    
    'Following call blocks and keep the ORB running until oOrb.shutdown()
    'is called in Form_Unload(). Instead of calling OrbRunLoopOutsideOfDLL()
    'or oOrb.run() you can call oOrb.performWork() by a timer periodically.
    
    'In this example any client can shutdown the server by calling
    'shutdownServer method of DBManager.
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

'Demo database function
'Create and load table
Public Sub loadTable()
    Set oTableHead = New c_DBColumnDescriptorSeq
    oTableHead.Length = 3
    Set oTableHead.Item(0) = New c_DBColumnDescriptor
    oTableHead.Item(0).name = "Id"
    Set oTableHead.Item(1) = New c_DBColumnDescriptor
    oTableHead.Item(1).name = "Name"
    Set oTableHead.Item(2) = New c_DBColumnDescriptor
    oTableHead.Item(2).name = "Ort"

    Set oTableRows = New c_DBColumnDataSeqSeq
    Call insertRow(100, "Martin", "Berlin")
    Call insertRow(101, "Anke", "München")
    Call insertRow(102, "Sabine", "Hamburg")
    Call insertRow(103, "Manfred", "Hannover")
End Sub

'Demo database function
'Insert one row into demo table
Public Sub insertRow(ByVal lId As Long, ByVal sName As String, ByVal sOrt As String)
    Dim oRow As c_DBColumnDataSeq
    Set oRow = New c_DBColumnDataSeq
    oRow.Length = 3
    Set oRow.Item(0) = New c_DBColumnData
    Call oRow.Item(0).set_integerValue(lId)
    Set oRow.Item(1) = New c_DBColumnData
    Call oRow.Item(1).set_stringValue(sName)
    Set oRow.Item(2) = New c_DBColumnData
    Call oRow.Item(2).set_stringValue(sOrt)
    
    oTableRows.Length = oTableRows.Length + 1
    Set oTableRows.Item(oTableRows.Length - 1) = oRow
End Sub

'Demo database function
'Update column Ort to sOrt where column Name is equal sName
Public Function updateOrtByName(ByVal sOrt As String, ByVal sName As String) _
    As Long
    Dim oRow As c_DBColumnDataSeq
    Dim rowcnt As Long
    For rowcnt = 0 To oTableRows.Length - 1
        Set oRow = oTableRows.Item(rowcnt)
        If oRow.Item(1).get_stringValue = sName Then
            Call oRow.Item(2).set_stringValue(sOrt)
            updateOrtByName = updateOrtByName + 1
        End If
    Next rowcnt
End Function

'Demo database function
'Delete rows where column Name is equal sName
Public Function deleteName(ByVal sName As String) As Long
    Dim oRow As c_DBColumnDataSeq
    Dim rowcnt As Long
    rowcnt = 0
    Do While rowcnt < oTableRows.Length
        Set oRow = oTableRows.Item(rowcnt)
        If oRow.Item(1).get_stringValue = sName Then
            Dim mvCnt As Long
            mvCnt = rowcnt + 1
            Do While mvCnt < oTableRows.Length
                Set oTableRows.Item(mvCnt - 1) = oTableRows.Item(mvCnt)
                mvCnt = mvCnt + 1
            Loop
            Set oTableRows.Item(oTableRows.Length - 1) = Nothing
            deleteName = deleteName + 1
            oTableRows.Length = oTableRows.Length - 1
        Else
            rowcnt = rowcnt + 1
        End If
    Loop
End Function

