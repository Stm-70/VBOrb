VERSION 1.0 CLASS
BEGIN
  MultiUse = -1  'True
END
Attribute VB_Name = "c_javalangNumberVF"
Attribute VB_GlobalNameSpace = False
Attribute VB_Creatable = True
Attribute VB_PredeclaredId = False
Attribute VB_Exposed = False
'Generated by IDL2VB v121. Copyright (c) 1999-2003 Martin.Both
'Source File Name: jdk1.4/idl/java/lang/Number.idl
'Target File Name: c_javalangNumberVF.cls

Option Explicit

Implements cOrbValueFactory

'Helper to get different COM interface
Friend Function thisOrbValueFactory() As cOrbValueFactory
    Set thisOrbValueFactory = Me
End Function

Private Function cOrbValueFactory_newUninitValue() As cOrbValueBase
    Set cOrbValueFactory_newUninitValue = New c_javalangNumber
End Function
