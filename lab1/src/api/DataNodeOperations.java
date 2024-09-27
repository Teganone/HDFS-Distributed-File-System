package api;


/**
* api/DataNodeOperations.java .
* 由IDL-to-Java 编译器 (可移植), 版本 "3.2"生成
* 从api.idl
* 2023年11月15日 星期三 下午05时00分22秒 CST
*/

public interface DataNodeOperations 
{
  byte[] read (int block_id);
  void append (int block_id, byte[] bytes);
  int randomBlockId ();
  int getUsedBlockNum ();
  int getBlocksCapacity ();
} // interface DataNodeOperations
