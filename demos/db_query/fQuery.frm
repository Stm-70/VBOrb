VERSION 5.00
Begin VB.Form fQuery 
   Caption         =   "Database Query"
   ClientHeight    =   5565
   ClientLeft      =   60
   ClientTop       =   345
   ClientWidth     =   6780
   LinkTopic       =   "Form1"
   ScaleHeight     =   5565
   ScaleWidth      =   6780
   StartUpPosition =   3  'Windows-Standard
   Begin VB.CommandButton cmdDelete 
      Caption         =   "DELETE"
      Height          =   375
      Left            =   4200
      TabIndex        =   9
      Top             =   120
      Width           =   975
   End
   Begin VB.CommandButton cmdInsert 
      Caption         =   "INSERT"
      Height          =   375
      Left            =   2040
      TabIndex        =   8
      Top             =   120
      Width           =   975
   End
   Begin VB.CommandButton cmdUpdate 
      Caption         =   "UPDATE"
      Height          =   375
      Left            =   3120
      TabIndex        =   7
      Top             =   120
      Width           =   975
   End
   Begin VB.CommandButton cmdSelect 
      Caption         =   "SELECT"
      Height          =   375
      Left            =   960
      TabIndex        =   6
      Top             =   120
      Width           =   975
   End
   Begin VB.CommandButton cmdExec 
      Caption         =   "Execute SQL"
      Height          =   495
      Left            =   5040
      TabIndex        =   5
      Top             =   720
      Width           =   1575
   End
   Begin VB.TextBox tbInput 
      BeginProperty Font 
         Name            =   "Courier New"
         Size            =   8.25
         Charset         =   0
         Weight          =   400
         Underline       =   0   'False
         Italic          =   0   'False
         Strikethrough   =   0   'False
      EndProperty
      Height          =   1575
      Left            =   120
      MultiLine       =   -1  'True
      ScrollBars      =   3  'Beides
      TabIndex        =   3
      Text            =   "fQuery.frx":0000
      Top             =   600
      Width           =   4695
   End
   Begin VB.TextBox tbResult 
      BeginProperty Font 
         Name            =   "Courier New"
         Size            =   8.25
         Charset         =   0
         Weight          =   400
         Underline       =   0   'False
         Italic          =   0   'False
         Strikethrough   =   0   'False
      EndProperty
      Height          =   2895
      Left            =   120
      MultiLine       =   -1  'True
      ScrollBars      =   3  'Beides
      TabIndex        =   1
      Top             =   2520
      Width           =   6255
   End
   Begin VB.CommandButton cmdClose 
      Caption         =   "Close"
      Height          =   495
      Left            =   5040
      TabIndex        =   0
      Top             =   1560
      Width           =   1575
   End
   Begin VB.Label Label2 
      Caption         =   "SQL Input"
      Height          =   255
      Left            =   120
      TabIndex        =   4
      Top             =   360
      Width           =   1095
   End
   Begin VB.Label Label1 
      Caption         =   "Result(s)"
      Height          =   255
      Left            =   120
      TabIndex        =   2
      Top             =   2280
      Width           =   1575
   End
End
Attribute VB_Name = "fQuery"
Attribute VB_GlobalNameSpace = False
Attribute VB_Creatable = False
Attribute VB_PredeclaredId = True
Attribute VB_Exposed = False
Option Explicit

Private Sub cmdClose_Click()
    Dim oWarns As c_DBMessageSeq
    Dim oEx As VBOrb.cOrbException
    On Error Resume Next
    Call mGlobal.closeDBSession(oEx, oWarns)
    If Not oEx Is Nothing Then GoTo ExHandler
    
    Call fLogIn.Show
    Unload Me
    
    Exit Sub
ExHandler:
    Call mGlobal.showExeptionBox(oEx)
End Sub

Public Sub clearResult()
    tbResult.Text = ""
End Sub

Public Sub printLine(ByVal sLine As String)
    tbResult.Text = tbResult.Text & sLine & vbCrLf
End Sub

Public Sub printWarnings(ByVal oWarns As c_DBMessageSeq)
    Dim i As Integer
    For i = 0 To oWarns.Length - 1
        Call printLine("Warning: sqlcode= " & oWarns.Item(i).sqlcode _
            & ", sqlstate= " & oWarns.Item(i).sqlstate)
        Call printLine(oWarns.Item(i).sqlmessage)
    Next i
End Sub

Public Sub printExeption(ByVal oEx As VBOrb.cOrbException)
    Select Case TypeName(oEx)
    Case "c_DBErrMsgsEx"
        Dim oDBEx As c_DBErrMsgsEx
        Set oDBEx = oEx
        Dim oErrs As c_DBMessageSeq
        Set oErrs = oDBEx.errs
        Dim i As Integer
        For i = 0 To oErrs.Length - 1
            Call printLine("Error: sqlcode= " & oErrs.Item(i).sqlcode _
                & ", sqlstate= " & oErrs.Item(i).sqlstate _
                & vbCrLf & oErrs.Item(i).sqlmessage)
        Next i
    Case Else
        Call printLine("Error " & oEx.Source & vbCrLf & oEx.Description)
    End Select
End Sub

Private Sub cmdExec_Click()
    On Error GoTo ErrHandler
    Dim rowcnt As Long
    Dim oWarns As c_DBMessageSeq
    
    Call clearResult
    
    Select Case StrConv(Left$(tbInput.Text, 6), vbLowerCase)
    Case "insert"
        rowcnt = mGlobal.DBSession.executeStatement(tbInput.Text, oWarns)
        Call printLine("Row Counter= " & rowcnt)
        Call printWarnings(oWarns)
        Exit Sub
    Case "update"
        rowcnt = mGlobal.DBSession.executeStatement(tbInput.Text, oWarns)
        Call printLine("Row Counter= " & rowcnt)
        Call printWarnings(oWarns)
        Exit Sub
    Case "delete"
        rowcnt = mGlobal.DBSession.executeStatement(tbInput.Text, oWarns)
        Call printLine("Row Counter= " & rowcnt)
        Call printWarnings(oWarns)
        Exit Sub
    End Select
    
    'SQL-Anweisung vorbereiten
    Dim oStatement As c_DBStatement
    Set oStatement = mGlobal.DBSession.prepareStatement(tbInput.Text, oWarns)
    Call printWarnings(oWarns)
    
    'Cursor für SQL-Anweisung besorgen
    Dim oCursor As c_DBCursor
    Set oCursor = oStatement.declareCursor(False, oWarns)
    Call printWarnings(oWarns)
    
    'SQL-Anweisung über den Cursor ausführen
    Dim oRowIn As c_DBColumnDataSeq
    Set oRowIn = New c_DBColumnDataSeq 'Leere Zeile
    Call oCursor.openSub(oRowIn, oWarns)
    Call printWarnings(oWarns)
    
    'SQL-Anweisung untersuchen und Spaltenbeschreibungen holen
    Dim stmType As Long
    Dim oCols As c_DBColumnDescriptorSeq
    stmType = oStatement.describe(oCols, oWarns)
    Call printWarnings(oWarns)
    Call printLine("Statement type= " & stmType)
    Call printLine("")
    Dim colHeadCnt As Long
    Dim sHeadLine As String
    sHeadLine = ""
    For colHeadCnt = 0 To oCols.Length - 1
        sHeadLine = sHeadLine & oCols.Item(colHeadCnt).name & vbTab
    Next colHeadCnt
    Call printLine(sHeadLine)
    sHeadLine = ""
    For colHeadCnt = 0 To oCols.Length - 1
        sHeadLine = sHeadLine & "------" & vbTab
    Next colHeadCnt
    Call printLine(sHeadLine)
    
    'Zeile für Zeile einlesen
    Dim oRowOut As c_DBColumnDataSeq
    Dim curState As Long
    Do
        curState = oCursor.fetch(0, oRowOut, oWarns)
        If curState <> 0 Then
            Exit Do
        End If
        Call printWarnings(oWarns)
        Dim colDataCnt As Long
        Dim sDataLine As String
        sDataLine = ""
        For colDataCnt = 0 To oCols.Length - 1
            If oRowOut.Item(colDataCnt).is_integerValue() Then
                sDataLine = sDataLine & _
                    oRowOut.Item(colDataCnt).get_integerValue() & vbTab
            Else
                sDataLine = sDataLine & _
                    oRowOut.Item(colDataCnt).get_stringValue() & vbTab
            End If
        Next colDataCnt
        Call printLine(sDataLine)
    Loop
    Call printLine("")
    Call printWarnings(oWarns)
    
    'Cursor schließen
    Call oCursor.closeSub(oWarns)
    Call printWarnings(oWarns)
    
    'SQL-Statement freigeben
    Call oStatement.free(oWarns)
    Call printWarnings(oWarns)
    
    Exit Sub
ExHandler:
    Dim oEx As VBOrb.cOrbException
    Set oEx = VBOrb.getException()
    Call printExeption(oEx)
    Exit Sub
ErrHandler:
    If VBOrb.ErrIsUserEx() Then Resume ExHandler
    Call VBOrb.ErrMsgBox(Err, "cmdExec")
End Sub

Private Sub cmdSelect_Click()
    tbInput.Text = "SELECT * FROM tabelle"
End Sub

Private Sub cmdInsert_Click()
    tbInput.Text = "INSERT INTO tabelle" & vbCrLf _
        & "VALUES(729, ""Joeline"", ""Sydney"")"
End Sub

Private Sub cmdUpdate_Click()
    tbInput.Text = "UPDATE tabelle" & vbCrLf _
        & "SET ort = ""Minden""" & vbCrLf _
        & "WHERE name = ""Joeline"""
End Sub

Private Sub cmdDelete_Click()
    tbInput.Text = "DELETE FROM tabelle" & vbCrLf _
        & "WHERE name = ""Joeline"""
End Sub

