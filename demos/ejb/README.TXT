Steps to access EJBs written in Java.

Please see
http://www.iona.com/whitepapers/CORBA-EJB-Interoperability-WP-V00-02.pdf
also.

1. Generate the IDL file by calling a Java to IDL tool of your
   Java application server or Java ORB. I put an example of an
   EJB IDL file into this distribution. See `ejbHome.idl'.
   * Visibroker:
      `java2idl -VBJclasspath ejb.jar my.ejbHome > my.idl'
   * Visibroker (Simplified IDL) (It is not possible to compile all
     kind of EJBs by ejb2sidl, for example an EJB witch is using
     java.util.TimeZone):
      `vbj com.inprise.ejb.sidl.ejb2sidl my.ejbHome'
   * OpenORB:
      `java org.openorb.rmi.compiler.JavaToIdl my.ejbHome'
   * Sybase Jaguar CTS Release 3.6 (EAS)
       EAS is using a kind of simplified IDL without valuetypes
       and wide strings. They are not supporting valuetypes and
       therefore are not using the standard Java to IDL mapping.

2. Try to compile the IDL file by calling IDL2VB compiler.
   Because of bugs in some Java2IDL tools you have to edit the
   IDL file until the IDL2VB compiler does not complain about errors.
   Following errors of Visibroker (IAS v4.1, BAS v4.5) are known:
   a. Edit `#pragma prefix' to `#pragma prefix ""'.
   b. Unescaped IDL keywords, for example:
      Edit `Exception' to `_Exception'.
      Edit `SHORT' to `_SHORT'.
   c. Replace `__long long' in operation names to `__long_long'.
   d. Add missing custom keywords.
   e. Missing `#pragma ID' for java.lang.Class doesn't matter
   f. Definitions of negativ octet constants are not allowed.
      For example replace -1 with 255.

3. Shorten the VisualBasic names by using #pragma vbname
   Please see at the end of example file `ejbHome.idl'.
   (You don't need shorten boxed JavaRMI/IDL array definitions.
   They are mapped by the IDL2VB compiler into the sequence class)

4. Remove old generated files and make a final call to IDL2VB.
   You should use IDL2VB option `--simplevalues' if you don't write
   your own value operations in VB. Add Option `--novalfacts' if you
   don't need to register a value factory.

5. Add all VB files to your VB project. The best way to do this is
   using drag and drop from the windows explorer to your VB project.
   In the windows explorer you can mark more than one file.
   
6. Create real valuetypes for the valuetype example files.
   (Rename example and save as...)

7. If you are using IAS 4.1 or other faulty Visibroker stuff, do not
   forget to set the VisiWorkaround flag in your Visual Basic application
   e.g: Set oOrb = VBOrb.init(ORBId:="", VisiWorkaround:=True)
   Old versions of Visibroker have some bugs in IIOP transfering of
   wstrings and valuetypes. (Wide strings are always transfered in
   IIOP 1.1 style even if using IIOP 1.2 for example,
   valuetypes are often transfered without typeinfo.)
   Version 4.5 has less bugs then 4.1.1;-)

8. If your EJBs are using custom valuetypes write your own marshalling
   and unmarshalling routines otherwise you will get errors.

9. Connect to the EJB by getting the IOR of the home interface.
   You could write a Java client to write the IOR of the home
   interface into a file.
   If using IAS or BAS you can using corbaloc URL. Then you need
   to know the port number of the home interface (not the port number of
   the osagent!) and the objectkey. The objectkey is an encapsulation
   of some const strings. The port number can be set by using the
   vbroker property `vbroker.se.iiop_tp.scm.iiop_tp.listener.port'.
   Case a) Your EJB Container runs in a separate Java VM
      Configure the EJB Container to use the desired port number.
      (Notice: The port number of the IAS instance must be different.)
      Run up the console, selecting the IAS instance, select the EJB
      Container, right button->Edit properties and adding the Java
      Run Time parameter:
      -Dvbroker.se.iiop_tp.scm.iiop_tp.listener.port=12345
   Case b) Your EJB Container runs in the same process as IAS
      Configure the IAS instance to use the desired port number.
      Run up the console, selecting the IAS instance,
      right button->Edit properties and adding vbroker property:
      vbroker.se.iiop_tp.scm.iiop_tp.listener.port (property name)
      11111 (example of property value, IAS instance port number)
      The nameservice of Visibroker is also using that port.
      It is accessable by corbaloc::hostname:11111/NameService

