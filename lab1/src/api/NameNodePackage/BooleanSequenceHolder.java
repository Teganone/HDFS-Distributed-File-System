package api.NameNodePackage;


/**
* api/NameNodePackage/BooleanSequenceHolder.java .
* 由IDL-to-Java 编译器 (可移植), 版本 "3.2"生成
* 从api.idl
* 2023年11月15日 星期三 下午05时00分22秒 CST
*/

public final class BooleanSequenceHolder implements org.omg.CORBA.portable.Streamable
{
  public boolean value[] = null;

  public BooleanSequenceHolder ()
  {
  }

  public BooleanSequenceHolder (boolean[] initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = api.NameNodePackage.BooleanSequenceHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    api.NameNodePackage.BooleanSequenceHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return api.NameNodePackage.BooleanSequenceHelper.type ();
  }

}
