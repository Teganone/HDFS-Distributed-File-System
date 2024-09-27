package conn;

/**
* conn/ConnHolder.java .
* 由IDL-to-Java 编译器 (可移植), 版本 "3.2"生成
* 从Conn.idl
* 2023年11月10日 星期五 下午04时06分46秒 CST
*/

public final class ConnHolder implements org.omg.CORBA.portable.Streamable
{
  public conn.Conn value = null;

  public ConnHolder ()
  {
  }

  public ConnHolder (conn.Conn initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = conn.ConnHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    conn.ConnHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return conn.ConnHelper.type ();
  }

}
