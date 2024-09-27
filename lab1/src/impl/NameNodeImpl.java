package impl;
//TODO: your implementation
import api.NameNodePOA;
import enums.CommandTypeEnum;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import utils.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.IntStream;

public class NameNodeImpl extends NameNodePOA {

    private static String fsImagePath = Config.NAMENODE_PATH + "fsImage.xml";
//    private static Map<String, FileDesc> FilePathAndFileDescMap;  // a buffer for files being openend
    private static Map<String, List<FileDesc>> FilePathAndFileDescMap;
    private boolean[] registeredDataNodes;
    private static boolean[] inodes;
    private static boolean lock;
//    private static
    public NameNodeImpl() {
    }

    static {
        FilePathAndFileDescMap = new HashMap<>();
        loadMetaDataFromDisk();
    }

    {
        registeredDataNodes = new boolean[Config.MAX_DATANODE_COUNT];
        Arrays.fill(registeredDataNodes,false);
//        loadMetaDataFromDisk();
//        FsImageParser.loadMetaDataFromDisk(fsImagePath);
    }
    @Override
    public String open(String filepath, int mode) {
        List<FileDesc> fileDescs = FilePathAndFileDescMap.get(filepath);
//        FileDesc fileDesc = (FileDesc) fileDescs
        String resultMessage = "";
        if (!Objects.isNull(fileDescs) && fileDescs.size()>0) {
            System.out.println("The file " + filepath + " already in buffer");
            int permission = fileDescs.get(0).getOwnInode().getPermission();
            if (!FsImageParser.isFilePathExist(fsImagePath,filepath)) {
                resultMessage = "The file is been created by other client.";
                return FileDesc.ofWrong(resultMessage).toString();
            } else if (Config.isModePermitted(permission, mode)==0) {
                resultMessage = "Permission denied";
                return FileDesc.ofWrong(resultMessage).toString();
            } else {
                boolean hasWriteClient = fileDescs.parallelStream().anyMatch(fileDesc ->
                        Config.canModeWrite(mode) && fileDesc.getWriteRef() > 0
                );
                if(hasWriteClient) {
                    resultMessage = "The file is been writen... please wait.";  //包含正在创建的文件
                    FileDesc fileDescResp = FileDesc.ofWrong(resultMessage);
                    return fileDescResp.toString();
                }
                //如果写者没有close，那么读者不能实时读取到最新数据,所以统一根据fsimage的最新inode进行创建。
//                else {
//                    new FileDesc(Config.fdId++,inode)
//                    fileDesc.ofSuccess();
//                    refreshRefCount(fileDesc,mode,CommandTypeEnum.OPEN);
//                    return fileDesc.toString();
//                }
            }
        }
        Node node = FsImageParser.findInodeByFilePath(fsImagePath, filepath,CommandTypeEnum.OPEN);
        if(Objects.isNull(node)){
            return FileDesc.ofWrong("The filepath doesn't exist...").toString();
        }
        FsImageParser.printNode(node);
        FileDesc fileDesc = new FileDesc(Config.fdId++, (Element) node, mode);
        refreshRefCount(fileDesc,mode,CommandTypeEnum.OPEN);
        fileDesc.ofSuccess();
        if(Objects.isNull(fileDescs)){
            fileDescs = new ArrayList<>();
            FilePathAndFileDescMap.put(filepath,fileDescs);
        }
        fileDescs.add(fileDesc);
        return fileDesc.toString();
    }

    @Override
    public void close(String fileInfo) {
        FileDesc fileDesc = FileDesc.fromString(fileInfo);
        if(Objects.isNull(fileDesc)) {
            return;
        }
//        Inode inode = fileDesc.getOwnInode();
//        Element inodeElement = Inode.createInodeElement(inode);
//        List<Block> blocks = inode.getblocks();
//        Node blocksNode = inodeElement.getElementsByTagName("blocks").item(0);
//        Document inodeDoc = inodeElement.getOwnerDocument();
//        for(Block block: blocks){
//            Element blockElement = Block.createBlockElement(block);
//            Node importedBlockNode = inodeDoc.importNode(blockElement, true);
//            blocksNode.appendChild(importedBlockNode);
//        }
        //write back to disk;
        String filePath = FilePathAndFileDescMap.keySet().parallelStream()
                .filter(key -> FilePathAndFileDescMap.get(key).stream()
                        .anyMatch(filedesc -> filedesc.getId() == fileDesc.getId()))
                .findFirst()
                .orElse(null);
        if(!Objects.isNull(filePath)) {
            try {
                File fsImageFile = new File(fsImagePath);
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(fsImageFile);
                doc.getDocumentElement().normalize();
                Node rootInode = FsImageParser.findRootInode(doc);
                Element originInode = (Element) FsImageParser.findInodeByPath((Element) rootInode, filePath, false,CommandTypeEnum.CLOSE);
//                Node parentNode = originInode.getParentNode();
//                Node importedNode = parentNode.getOwnerDocument().importNode(inodeElement, true);
//                parentNode.replaceChild(importedNode, originInode);
                Element originDocElement = doc.getDocumentElement();
                FsImageParser.renewInodeElememt(fileDesc,originInode);
                Node usedNum = originDocElement.getElementsByTagName("usedNum").item(0);
                usedNum.setTextContent(String.valueOf(getUsedInodeCount()));
                FsImageParser.saveXMLByDom(doc, fsImagePath);
                refreshRefCount(fileDesc, fileDesc.getOpMode(), CommandTypeEnum.CLOSE);
            } catch (Exception e) {
                e.printStackTrace();
            }
            List<FileDesc> fileDescs = FilePathAndFileDescMap.get(filePath);
            FileDesc theFileDesc = FilePathAndFileDescMap.keySet().parallelStream()
                    .flatMap(key -> FilePathAndFileDescMap.get(key).stream())
                    .filter(filedesc -> filedesc.equals(fileDesc))
                    .findFirst()
                    .orElse(null);
            if (fileDesc.getWriteRef() == 0 && fileDesc.getReadRef() == 0) { //remove out of the buffer
                fileDescs.remove(theFileDesc);  //删除原来的。
            }
        }
    }

    public void registerDataNode(int id) {
        registeredDataNodes[id] = true;
    }

    public boolean[] getRegisteredDataNodes() {
        return registeredDataNodes;
    }

    public void setRegisteredDataNodes(boolean[] registeredDataNodes) {
        this.registeredDataNodes = registeredDataNodes;
    }

    public static void loadMetaDataFromDisk(){
        initializeFsImage();
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(fsImagePath);
            doc.normalizeDocument();
            Element rootElement = doc.getDocumentElement();
            Element inodesElement = (Element)rootElement.getElementsByTagName("inodes").item(0);
            String capacityStr = inodesElement.getElementsByTagName("capacity").item(0).getTextContent();
            int capacity = Config.DEFAULT_MAX_INODE_NUM;
            if(!Objects.isNull(capacityStr) && !capacityStr.equals("")) {
                capacity = Integer.valueOf(capacityStr);
            }
            Node usedNum = inodesElement.getElementsByTagName("usedNum").item(0);
            inodes = new boolean[capacity+1];
            NodeList idNodes = inodesElement.getElementsByTagName("id");
            int count = 0;
            for (int i = 0; i < idNodes.getLength(); i++) {
                Integer id = Integer.valueOf(idNodes.item(i).getTextContent());
                inodes[id] = true;
                count++;
            }
            usedNum.setTextContent(String.valueOf(count));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static int getInodeId(){
//        if(Objects.isNull())
        for (int i = 1; i < inodes.length; i++) {
            if(inodes[i]==false) {
                inodes[i] = true;
                return i;
            }
        }
        return -1; //磁盘已满
    }

    public static void setInodes(int inodeId){
        inodes[inodeId] = true;
    }

    public static int getUsedInodeCount(){
        return (int) IntStream.range(1, inodes.length)
                .mapToObj(i -> inodes[i])
                .filter(inode -> inode)
                .count();
    }

    private static void initializeFsImage() {
        try {
            File file = new File(fsImagePath);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
                Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(Config.fsImageConfigPath);
                FsImageParser.saveXMLByDom(doc, fsImagePath);
                System.out.println("initializing");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private void refreshRefCount(FileDesc fileDesc,int mode,CommandTypeEnum op){
        if(Config.canModeRead(mode)){
            if(op == CommandTypeEnum.OPEN)
                fileDesc.setReadRef(fileDesc.getReadRef()+1);
            else if(op==CommandTypeEnum.CLOSE){
                fileDesc.setReadRef(fileDesc.getReadRef()-1);
            }
        }
        if(Config.canModeWrite(mode)){
            if(op == CommandTypeEnum.OPEN)
                fileDesc.setWriteRef(1);
            else if(op==CommandTypeEnum.CLOSE){
                fileDesc.setWriteRef(0);
            }
        }
    }

}
