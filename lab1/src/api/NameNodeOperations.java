package api;


/**
* api/NameNodeOperations.java .
* 由IDL-to-Java 编译器 (可移植), 版本 "3.2"生成
* 从api.idl
* 2023年11月15日 星期三 下午05时00分22秒 CST
*/

public interface NameNodeOperations 
{

  //TODO: complete the interface design
  String open (String filepath, int mode);
  void close (String filepath);
  void registerDataNode (int id);
  boolean[] getRegisteredDataNodes ();
} // interface NameNodeOperations
