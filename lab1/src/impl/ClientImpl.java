package impl;
//TODO: your implementation
import api.*;
import enums.CommandModeEnum;
import launcher.DataNodeLauncher;
import launcher.NameNodeLauncher;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Object;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import utils.Block;
import utils.Config;
import utils.FileDesc;
import utils.Inode;

import javax.xml.soap.Node;
import java.nio.ByteBuffer;
import java.util.*;

public class ClientImpl implements Client{
    private NameNode nameNode;
    private DataNode[] dataNodes = new DataNode[Config.MAX_DATANODE_COUNT];
    private FileDesc[] fdTable = new FileDesc[Config.MAX_CLIENT_FD_NUM+1]; //文件描述符从1开始

    {
        Arrays.fill(fdTable,null);
        Arrays.fill(dataNodes, null);
    }

    public ClientImpl(){
        try {
//            Properties properties = new Properties();
//            properties.put("org.omg.ORBInitialHost","127.0.0.1");
//            properties.put("org.omg.CORBA.ORBInitialPort","1050");
//
//            ORB orb = ORB.init(args,properties);

            // for debug only
            String[] args = new String[4];
            args[0] = "-ORBInitialHost";
            args[1] = "127.0.0.1";
            args[2] = "-ORBInitialPort";
            args[3] = "1050";

            ORB orb = ORB.init(args, null);

            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();

            // create servant
            NameNodeImpl nameNodeServant = new NameNodeImpl();

            // 将服务实现对象交给 POA 管理，并向外暴露接口
            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(nameNodeServant);
            NameNode href = NameNodeHelper.narrow(ref);

            // 命名上下文为 NameService
            // get the root naming context
            // NameService invokes the name service
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            // Use NamingContextExt which is part of the Interoperable
            // Naming Service (INS) specification.
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            // bind the Object Reference in Naming
            NameComponent path[] = ncRef.to_name("NameNode");  // 绑定名字，可以区分不同的launcher
            ncRef.rebind(path, href);

            System.out.println("NameNode is ready and waiting ...");

            for (int id = 0; id < 2; id++) {


                DataNodeImpl dataNodeServant = new DataNodeImpl(id);

                ref = rootpoa.servant_to_reference(dataNodeServant);
                DataNode href2 = DataNodeHelper.narrow(ref);

                NameComponent[] path2 = ncRef.to_name("DataNode" + dataNodeServant.getId());
                ncRef.rebind(path2, href2);

                System.out.println("DataNode" + dataNodeServant.getId() + " is ready and start...");
                NameNode nameNode = NameNodeHelper.narrow(ncRef.resolve_str("NameNode"));
                System.out.println("Register to nameNode.");
                nameNode.registerDataNode((int)dataNodeServant.getId());
            }

            this.nameNode = NameNodeHelper.narrow(ncRef.resolve_str("NameNode"));
            System.out.println("NameNode is obtained.");

            boolean[] registeredDataNodes = nameNode.getRegisteredDataNodes();
            for (int dataNodeId = 0; dataNodeId < 2; dataNodeId++) {
                if(registeredDataNodes[dataNodeId]==true) {
                    this.dataNodes[dataNodeId] = DataNodeHelper.narrow(ncRef.resolve_str("DataNode" + dataNodeId));
                    System.out.println("DataNode" + dataNodeId + " is obtained.");
                }
            }

        } catch (Exception e){
            e.printStackTrace();
        }
    }


    public ClientImpl(String[] args){
        try {
//            Properties properties = new Properties();
//            properties.put("org.omg.ORBInitialHost","127.0.0.1");
//            properties.put("org.omg.CORBA.ORBInitialPort","1050");
//
//            ORB orb = ORB.init(args,properties);

            // for debug only
            args = new String[4];
            args[0] = "-ORBInitialHost";
            args[1] = "127.0.0.1";
            args[2] = "-ORBInitialPort";
            args[3] = "1050";

            ORB orb = ORB.init(args, null);

            Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            this.nameNode = NameNodeHelper.narrow(ncRef.resolve_str("NameNode"));
            System.out.println("NameNode is obtained.");

            boolean[] registeredDataNodes = nameNode.getRegisteredDataNodes();
            for (int dataNodeId = 0; dataNodeId < Config.MAX_DATANODE_COUNT; dataNodeId++) {
                if(registeredDataNodes[dataNodeId]==true) {
                    this.dataNodes[dataNodeId] = DataNodeHelper.narrow(ncRef.resolve_str("DataNode" + dataNodeId));
                    System.out.println("DataNode" + dataNodeId + " is obtained.");
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public int open(String filepath, int mode) {
        int fd = getEmptyFd();
        if(fd>0){
            FileDesc fileDesc = null;
            if(Objects.isNull(nameNode)){
                System.out.println(Config.respINFOPrefix() + "cannot connect with NameNode Server...");
                fd = 0;
            } else {
                fileDesc = FileDesc.fromString(this.nameNode.open(filepath, mode));
            }
            if(Objects.isNull(fileDesc)) {
                System.out.println(Config.respINFOPrefix() + "cannot find the file or permission denied.");
                fd = 0;
            } else if(fileDesc.getId()==-1){
                System.out.println(Config.respINFOPrefix() + ""+fileDesc.getResultMessage());
                fd = 0;
            } else {
                fdTable[fd] = fileDesc;
            }
        }
        return fd;
    }

    @Override
    public void append(int fd, byte[] bytes) {
        FileDesc fileDesc = fdTable[fd];
        if(Objects.isNull(fileDesc)){
            System.out.println(Config.respINFOPrefix() + "the fd doesn't exist, please open the file first.");
            return;
        }
        boolean canWrite = (fileDesc.getOpMode() & CommandModeEnum.WRITE_MODE.getModeCode()) != 0;
        if(!canWrite){
            System.out.println(Config.respINFOPrefix() + "Write permission denied");
            return;
        }

        //to append
        Inode inode = fileDesc.getOwnInode();
        List<Block> blocks = inode.getblocks();
        int remained = bytes.length;
        int originSize = (int)inode.getSize();
        int blockOffset = originSize % Config.DATA_BLOCK_SIZE;
        int blockNo = originSize / Config.DATA_BLOCK_SIZE;
        int needBlockNum = remained / Config.DATA_BLOCK_SIZE + 1;
        int bufLen = 0;
        int bufStart = 0;
        int bufEnd = 0;
        byte[] buf = null;
        Block block = null;
        int dataNodeId = 0;

        boolean isRemained = false;
        while (remained > 0) {
            bufLen = Math.min(remained, Config.DATA_BLOCK_SIZE - blockOffset);
            bufEnd += bufLen;
            buf = new byte[Config.DATA_BLOCK_SIZE];
            System.arraycopy(bytes, bufStart, buf, 0, bufLen);
            if(bufStart==0) { //最末尾的datanode
                if (Objects.isNull(blocks) || blocks.size() == 0 || bufLen == 0) {
                    if (Objects.isNull(blocks)) {
                        blocks = new ArrayList<>();
                        inode.setblocks(blocks);
                    }
                    dataNodeId = getAvailableDataNodeNo(needBlockNum);
                    block = new Block(dataNodeId,dataNodes[dataNodeId].randomBlockId());
//                    blockOffset = 0;
                    dataNodes[dataNodeId].append(block.getBlockNo(), buf);
                    blocks.add(block);
                } else {
                    block = blocks.get(blocks.size() - 1);
                    dataNodeId = block.getDataNodeNo();
                    isRemained = withinBlockMaxNumThreshold(dataNodes[dataNodeId], needBlockNum);
                    dataNodes[dataNodeId].append(block.getBlockNo(), buf);
                }
            } else {
//                blockOffset = 0;
                if(!isRemained) {
                    dataNodeId = getAvailableDataNodeNo(needBlockNum);
                }
                block = new Block(dataNodeId,dataNodes[dataNodeId].randomBlockId());
                dataNodes[dataNodeId].append(block.getBlockNo(), buf);
                blocks.add(block);
            }
            System.out.println(Config.respINFOPrefix() + "Append to DataNode"+dataNodeId+", block"+block.getBlockNo());
//            bufEnd += bufLen;
//            buf = new byte[Config.DATA_BLOCK_SIZE];
//            System.arraycopy(bytes, bufStart, buf, 0, bufLen);
//            dataNodes[dataNodeId].append(block.getBlockNo(), buf);
//            blocks.add(block);
            bufStart = bufEnd;
            remained -= bufLen;
        }
        inode.setSize(inode.getSize()+bytes.length);
        inode.setMtime(Config.getCNDateTimeStr());
    }

    @Override
    public byte[] read(int fd) {
        FileDesc fileDesc = fdTable[fd];
//        byte[] result = new byte[0];
        if(Objects.isNull(fileDesc)){
            System.out.println(Config.respINFOPrefix() + "the fd doesn't exist, please open the file first.");
            return new byte[0];
        }
        boolean canRead = (fileDesc.getOpMode() & CommandModeEnum.READ_MODE.getModeCode()) != 0;
        if(!canRead){
            System.out.println(Config.respINFOPrefix() + "Read permission denied");
            return new byte[0];
        }
        Inode inode = fileDesc.getOwnInode();
        List<Block> blocks = inode.getblocks();
        fileDesc.getOwnInode().setAtime(Config.getCNDateTimeStr());
        if(inode.getSize()==0 || Objects.isNull(blocks) || blocks.size()==0){
            return new byte[0];
        }
        ByteBuffer result = ByteBuffer.allocate((int)inode.getSize());
        byte[] tmp;
        byte[] buf;
        for(int i=0;i<blocks.size();i++){
            Block block = blocks.get(i);
            DataNode dataNode = dataNodes[block.getDataNodeNo()];
            int blockNo = block.getBlockNo();
            if (Objects.isNull(dataNode)) {
                System.out.println("DataNode" + block.getDataNodeNo() + " is not alive, some data lost.");
            } else {
                if(blockNo<dataNode.getBlocksCapacity()) {
                    System.out.println(Config.respINFOPrefix() + "read from dataNode"+block.getDataNodeNo()+" block"+blockNo);
                    if (i < blocks.size() - 1) {
                        buf = dataNode.read(blockNo);
                        result.put(buf);
                    } else {
                        //最后一个可能bufferoverflow
                        System.out.println(Config.respINFOPrefix() + "read from the last block");
                        int length = (int) inode.getSize() % Config.DATA_BLOCK_SIZE;
                        if (length == 0) {
                            length = Config.DATA_BLOCK_SIZE;
                        }
                        buf = new byte[length];
                        tmp = dataNode.read(blockNo);
                        System.arraycopy(tmp, 0, buf, 0, length);
                        result.put(buf);
                    }
                }
            }
        }
        return result.array();
    }

    @Override
    public void close(int fd) {
        FileDesc fileDesc = fdTable[fd];
        if(Objects.isNull(fileDesc)){
            System.out.println(Config.respINFOPrefix() + "The file is not opened.");
        } else if(Objects.isNull(nameNode)){
            System.out.println(Config.respINFOPrefix() + "nameNode is disconnected.");
        } else {
            nameNode.close(fileDesc.toString());
            System.out.println(Config.respINFOPrefix() + "fd "+fd + " closed.");
        }
        fdTable[fd] = null;
    }

    private int getEmptyFd(){
        for (int i = 1; i < fdTable.length; i++) {
            if(Objects.isNull(fdTable[i])) {
                return i;
            }
        }
        System.out.println(Config.respINFOPrefix() + "当前客户端已达到文件描述符数量, 操作失败");
        return 0;
    }


    private int getAvailableDataNodeNo(int threshold){
        for (int i = 0; i < dataNodes.length; i++) {
            if(!Objects.isNull(dataNodes[i]) && withinBlockMaxNumThreshold(dataNodes[i],threshold))
                return i;
        }
        return -1;
    }

    private boolean withinBlockMaxNumThreshold(DataNode dataNode,int threshold){
        if(threshold<0) threshold = 0;
        return dataNode.getUsedBlockNum() <= dataNode.getBlocksCapacity() - threshold;
    }

}
