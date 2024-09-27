package conn;


/**
* conn/ConnHelper.java .
* 由IDL-to-Java 编译器 (可移植), 版本 "3.2"生成
* 从Conn.idl
* 2023年11月10日 星期五 下午04时06分46秒 CST
*/

abstract public class ConnHelper
{
  private static String  _id = "IDL:conn/Conn:1.0";

  public static void insert (org.omg.CORBA.Any a, conn.Conn that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static conn.Conn extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      __typeCode = org.omg.CORBA.ORB.init ().create_interface_tc (conn.ConnHelper.id (), "Conn");
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static conn.Conn read (org.omg.CORBA.portable.InputStream istream)
  {
    return narrow (istream.read_Object (_ConnStub.class));
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, conn.Conn value)
  {
    ostream.write_Object ((org.omg.CORBA.Object) value);
  }

  public static conn.Conn narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof conn.Conn)
      return (conn.Conn)obj;
    else if (!obj._is_a (id ()))
      throw new org.omg.CORBA.BAD_PARAM ();
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      conn._ConnStub stub = new conn._ConnStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

  public static conn.Conn unchecked_narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof conn.Conn)
      return (conn.Conn)obj;
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      conn._ConnStub stub = new conn._ConnStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

}
