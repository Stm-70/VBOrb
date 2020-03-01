VERSION 1.0 CLASS
BEGIN
  MultiUse = -1  'True
END
Attribute VB_Name = "cCBServiceDetail"
Attribute VB_GlobalNameSpace = False
Attribute VB_Creatable = True
Attribute VB_PredeclaredId = False
Attribute VB_Exposed = True
'Generated by IDL2VB v123. Copyright (c) 1999-2003 Martin.Both
'Source File Name: ../include/CORBA.idl
'Target File Name: cCBServiceDetail.cls

Option Explicit

'struct ::CORBA::ServiceDetail
Const sTypeId As String = "IDL:omg.org/CORBA/ServiceDetail:1.0"

'Member(s)
Private service_detail_type_ As Long
Private service_detail_ As c_ByteSeq

'Member: service_detail_type
Public Property Get service_detail_type() As Long
    service_detail_type = service_detail_type_
End Property

Public Property Let service_detail_type(ByVal service_detail_typeNew As Long)
    service_detail_type_ = service_detail_typeNew
End Property

'Member: service_detail
Public Property Get service_detail() As c_ByteSeq
    Set service_detail = service_detail_
End Property

Public Property Set service_detail(ByVal service_detailNew As c_ByteSeq)
    Set service_detail_ = service_detailNew
End Property

'Helper
Public Sub initByRead(ByVal oIn As cOrbStream)
    On Error GoTo ErrHandler
    service_detail_type_ = oIn.readUlong()
    Set service_detail_ = New c_ByteSeq
    Call service_detail_.initByRead(oIn, 0)
    Exit Sub
ErrHandler:
    Call mVBOrb.VBOrb.ErrReraise(Err, "ServiceDetail.read")
End Sub

'Helper
Public Sub writeMe(ByVal oOut As cOrbStream)
    On Error GoTo ErrHandler
    Call oOut.writeUlong(service_detail_type_)
    Call service_detail_.writeMe(oOut, 0)
    Exit Sub
ErrHandler:
    Call mVBOrb.VBOrb.ErrReraise(Err, "ServiceDetail.write")
End Sub

'Helper
Public Sub initByAny(ByVal oAny As cOrbAny)
    On Error GoTo ErrHandler
    service_detail_type_ = oAny.getUlong()
    Call oAny.nextPos
    Set service_detail_ = New c_ByteSeq
    Call service_detail_.initByAny(oAny.currentComponent())
    Call oAny.nextPos
    Call oAny.rewind
    Exit Sub
ErrHandler:
    Call mVBOrb.VBOrb.ErrReraise(Err, "initByAny")
End Sub

'Helper
Public Sub insertIntoAny(ByVal oAny As cOrbAny)
    On Error GoTo ErrHandler
    Call oAny.insertUlong(service_detail_type_)
    Call oAny.nextPos
    Call service_detail_.insertIntoAny(oAny.currentComponent())
    Call oAny.nextPos
    Call oAny.rewind
    Exit Sub
ErrHandler:
    Call mVBOrb.VBOrb.ErrReraise(Err, "insertIntoAny")
End Sub