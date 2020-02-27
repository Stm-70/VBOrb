VERSION 5.00
Begin VB.Form frmIOR 
   Caption         =   "Object Reference"
   ClientHeight    =   4815
   ClientLeft      =   60
   ClientTop       =   345
   ClientWidth     =   9135
   LinkTopic       =   "Form1"
   ScaleHeight     =   4815
   ScaleWidth      =   9135
   StartUpPosition =   3  'Windows-Standard
   Begin VB.TextBox tbObjKey 
      Height          =   645
      Left            =   1320
      MultiLine       =   -1  'True
      TabIndex        =   8
      Top             =   3360
      Width           =   7695
   End
   Begin VB.TextBox tbIIOPAddr 
      Height          =   285
      Left            =   1320
      TabIndex        =   6
      Top             =   2880
      Width           =   7695
   End
   Begin VB.TextBox tbTypeId 
      Height          =   285
      Left            =   1320
      TabIndex        =   4
      Top             =   2400
      Width           =   7695
   End
   Begin VB.TextBox tbIOR 
      Height          =   1575
      Left            =   240
      MultiLine       =   -1  'True
      TabIndex        =   1
      Top             =   600
      Width           =   8775
   End
   Begin VB.CommandButton cmdOK 
      Caption         =   "OK"
      Default         =   -1  'True
      Height          =   495
      Left            =   3480
      TabIndex        =   0
      Top             =   4200
      Width           =   1695
   End
   Begin VB.Label Label4 
      Caption         =   "ObjKey:"
      Height          =   255
      Left            =   240
      TabIndex        =   7
      Top             =   3360
      Width           =   855
   End
   Begin VB.Label Label3 
      Caption         =   "IIOP Address:"
      Height          =   255
      Left            =   240
      TabIndex        =   5
      Top             =   2880
      Width           =   1095
   End
   Begin VB.Label Label2 
      Caption         =   "TypeId:"
      Height          =   255
      Left            =   240
      TabIndex        =   3
      Top             =   2400
      Width           =   615
   End
   Begin VB.Label Label1 
      Caption         =   "ORB.object_to_string()"
      Height          =   255
      Left            =   240
      TabIndex        =   2
      Top             =   240
      Width           =   1695
   End
End
Attribute VB_Name = "frmIOR"
Attribute VB_GlobalNameSpace = False
Attribute VB_Creatable = False
Attribute VB_PredeclaredId = True
Attribute VB_Exposed = False
Public Sub setObject(ByVal oOrb As cOrbImpl, ByVal oObj As cOrbObject)
    On Error GoTo ErrHandler
    Dim sIOR As String
    Dim oObjRef As cOrbObjRef
    Dim sIIOPAddr As String
    Dim sTypeId As String
    sIOR = oOrb.objectToString(oObj)
    tbIOR.Text = sIOR
    
    sTypeId = oObj.getId()
    tbTypeId.Text = sTypeId
    
    Set oObjRef = oObj.getObjRef()
    sIIOPAddr = oObjRef.IIOPAddressFw()
    tbIIOPAddr.Text = sIIOPAddr
    
    tbObjKey.Text = oObjRef.objectKeyFw()
    
    Exit Sub
ErrHandler:
    Call VBOrb.ErrReraise(Err, "setObject")
End Sub

Private Sub cmdOK_Click()
    Call Me.Hide
    Unload Me
End Sub

