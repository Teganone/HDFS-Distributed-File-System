package conn;


/**
* conn/ConnPOA.java .
* 由IDL-to-Java 编译器 (可移植), 版本 "3.2"生成
* 从Conn.idl
* 2023年11月10日 星期五 下午04时06分46秒 CST
*/

public abstract class ConnPOA extends org.omg.PortableServer.Servant
 implements conn.ConnOperations, org.omg.CORBA.portable.InvokeHandler
{

  // Constructors

  private static java.util.Hashtable _methods = new java.util.Hashtable ();
  static
  {
    _methods.put ("connect", new java.lang.Integer (0));
    _methods.put ("sendMessage", new java.lang.Integer (1));
    _methods.put ("receiveMessage", new java.lang.Integer (2));
    _methods.put ("createChatRoom", new java.lang.Integer (3));
    _methods.put ("listChatRooms", new java.lang.Integer (4));
    _methods.put ("joinChatRoom", new java.lang.Integer (5));
    _methods.put ("leaveChatRoom", new java.lang.Integer (6));
    _methods.put ("changeName", new java.lang.Integer (7));
  }

  public org.omg.CORBA.portable.OutputStream _invoke (String $method,
                                org.omg.CORBA.portable.InputStream in,
                                org.omg.CORBA.portable.ResponseHandler $rh)
  {
    org.omg.CORBA.portable.OutputStream out = null;
    java.lang.Integer __method = (java.lang.Integer)_methods.get ($method);
    if (__method == null)
      throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);

    switch (__method.intValue ())
    {
       case 0:  // conn/Conn/connect
       {
         String $result = null;
         $result = this.connect ();
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 1:  // conn/Conn/sendMessage
       {
         String token = in.read_string ();
         String message = in.read_string ();
         this.sendMessage (token, message);
         out = $rh.createReply();
         break;
       }

       case 2:  // conn/Conn/receiveMessage
       {
         String token = in.read_string ();
         String $result = null;
         $result = this.receiveMessage (token);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 3:  // conn/Conn/createChatRoom
       {
         String token = in.read_string ();
         String name = in.read_string ();
         this.createChatRoom (token, name);
         out = $rh.createReply();
         break;
       }

       case 4:  // conn/Conn/listChatRooms
       {
         String token = in.read_string ();
         this.listChatRooms (token);
         out = $rh.createReply();
         break;
       }

       case 5:  // conn/Conn/joinChatRoom
       {
         String token = in.read_string ();
         String name = in.read_string ();
         this.joinChatRoom (token, name);
         out = $rh.createReply();
         break;
       }

       case 6:  // conn/Conn/leaveChatRoom
       {
         String token = in.read_string ();
         this.leaveChatRoom (token);
         out = $rh.createReply();
         break;
       }

       case 7:  // conn/Conn/changeName
       {
         String token = in.read_string ();
         String name = in.read_string ();
         this.changeName (token, name);
         out = $rh.createReply();
         break;
       }

       default:
         throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
    }

    return out;
  } // _invoke

  // Type-specific CORBA::Object operations
  private static String[] __ids = {
    "IDL:conn/Conn:1.0"};

  public String[] _all_interfaces (org.omg.PortableServer.POA poa, byte[] objectId)
  {
    return (String[])__ids.clone ();
  }

  public Conn _this() 
  {
    return ConnHelper.narrow(
    super._this_object());
  }

  public Conn _this(org.omg.CORBA.ORB orb) 
  {
    return ConnHelper.narrow(
    super._this_object(orb));
  }


} // class ConnPOA
