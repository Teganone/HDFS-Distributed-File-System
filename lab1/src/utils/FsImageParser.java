package utils;


import enums.CommandTypeEnum;
import impl.NameNodeImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//import org.dom4j.io.OutputFormat;
//import org.dom4j.io.XMLWriter;

public class FsImageParser {

    //test only
    static String fsImagePath = "resources/fsimage.xml"; // fsimage文件的路径
    //        String filePath = "/dir1/file1.txt"; // 要查找的文件路径
    static String filePath = "././dir1/../dir3/file3.txt";

    //debug
    public static void main(String[] args) {
        try {
            File fsImageFile = new File(fsImagePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fsImageFile);
            doc.getDocumentElement().normalize();

//            Element rootElement = doc.getDocumentElement();
            Node rootInode = findRootInode(doc);
            Element inodeNode = (Element)findInodeByPath((Element) rootInode, filePath,false,CommandTypeEnum.OPEN);

            if (inodeNode != null) {
                System.out.println("Found inode for file path: " + filePath);
                // 解析inode的其他属性...
                String textContent = inodeNode.getElementsByTagName("name").item(0).getTextContent();
                System.out.println(textContent);

            } else {
                System.out.println("Inode not found for file path: " + filePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isFilePathExist(String fsImagePath,String filePath){
        return !Objects.isNull(parserPath(fsImagePath,filePath,true,null));
    }
    public static Node findInodeByFilePath(String fsImagePath,String filePath, CommandTypeEnum commandTypeEnum){
        return parserPath(fsImagePath,filePath,false,commandTypeEnum);
    }
    public static Node parserPath(String fsImagePath,String filePath, boolean onlyExist,CommandTypeEnum commandTypeEnum){
        Element inodeNode = null;
        try {
            File fsImageFile = new File(fsImagePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fsImageFile);
            doc.getDocumentElement().normalize();

//            Element rootElement = doc.getDocumentElement();
            Node rootInode = findRootInode(doc);
            inodeNode = (Element)findInodeByPath((Element) rootInode, filePath, onlyExist,commandTypeEnum);

            if (inodeNode != null) {
                System.out.println("Found inode for file path: " + filePath);
                // 解析inode的其他属性...
            } else {
                System.out.println("Inode not found for file path: " + filePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return inodeNode;
    }

    public static Node findInodeByPath(Element element, String filePath, boolean onlyExist, CommandTypeEnum commandType) {
        if(!filePath.startsWith("/")){
            filePath = "/" + filePath;
        }
        String[] pathSegments = filePath.split("/");
        List<String> pathSegmentsList = new ArrayList<>();
        for (int i = 1; i < pathSegments.length; i++) {
            if(pathSegments[i].equals(".")){
                continue;
            } else if(pathSegments[i].equals("..")){
                if(pathSegmentsList.size()>0)
                    pathSegmentsList.remove(pathSegmentsList.size()-1);
            } else {
                pathSegmentsList.add(pathSegments[i]);
            }
        }
        System.out.println(pathSegmentsList);
        if (pathSegmentsList.size()==0){ //文件路径不对
            return null;
        }
        return findInodeByName(element,pathSegmentsList,0, onlyExist, commandType);
    }

    public static Node findInodeByName(Element element, List<String> pathSegmentsList,int level, boolean onlyExist, CommandTypeEnum commandType){
        if(level == pathSegmentsList.size()){
            return element;
        }
        String curSegment = pathSegmentsList.get(level);
        String type = isDirectory(level, pathSegmentsList) ? "directory" : "file";
        Element childrenNode = (Element) element.getElementsByTagName("children").item(0);
        if(Objects.isNull(childrenNode)){
            childrenNode = element.getOwnerDocument().createElement("children");
            element.appendChild(childrenNode);
        }
        NodeList inodeList = childrenNode.getElementsByTagName("inode");
        for (int i = 0; i < inodeList.getLength(); i++) {
            Element inode = (Element) inodeList.item(i);
            String inodeName = inode.getElementsByTagName("name").item(0).getTextContent();
            String inodeType = inode.getElementsByTagName("type").item(0).getTextContent();
//            String inodePermission = inode.getElementsByTagName("permission").item(0).getTextContent();
            if(type.equals(inodeType) && curSegment.equals(inodeName)){
                return findInodeByName(inode,pathSegmentsList,level+1, onlyExist,commandType);
            }
        }
        if(!onlyExist) {
            Element newInode = null;
            if(commandType==CommandTypeEnum.OPEN) {
                Inode inode = Inode.newInode(type, curSegment, 3,false);
                newInode = (Element) Inode.createInodeElement(inode);
                System.out.println("创建新节点：" + curSegment);
            } else if(commandType==CommandTypeEnum.CLOSE){
                Inode inode = Inode.newInode(type, curSegment, 3,true);
                newInode = (Element) Inode.createInodeElement(inode);
            }
            Element reportedNode = (Element) element.getOwnerDocument().importNode(newInode, true);
            childrenNode.appendChild(reportedNode);
//            Element newInode = (Element) createInode(element.getOwnerDocument(), Config.InodeId++, type, curSegment, 3);
//            childrenNode.appendChild(newInode);
            return findInodeByName(reportedNode, pathSegmentsList, level + 1, onlyExist,commandType);
        }
        return null;
    }

    public static Node findRootInode(Document doc){
        Node root = doc.getElementsByTagName("root").item(0);
        Node item = ((Element) root).getElementsByTagName("id").item(0);
//        System.out.println(item.getTextContent());
        String rootId = item.getTextContent();
        NodeList inodeList = doc.getElementsByTagName("inode");
        for (int i = 0; i < inodeList.getLength(); i++) {
            Node inode = inodeList.item(i);
            NodeList idList = ((Element) inode).getElementsByTagName("id");
            for (int j = 0; j < idList.getLength(); j++) {
                Node idNode = idList.item(j);
                if(idNode.getTextContent().equals(rootId)) {
//                    System.out.println(inode.getParentNode());
                    return idNode.getParentNode();
                }
            }
        }
        return null;
    }

    public static boolean isDirectory(int level, List<String> pathSegmentsList) {
        return level < pathSegmentsList.size() - 1;
    }

    public static Node createInode(Document document, long id, String type, String name,int permission){
        Element inodeElement = document.createElement("inode");

        Element idElement = document.createElement("id");
        idElement.setTextContent(String.valueOf(id));
        inodeElement.appendChild(idElement);

        Element typeElement = document.createElement("type");
        typeElement.setTextContent(type);
        inodeElement.appendChild(typeElement);

        Element nameElement = document.createElement("name");
        nameElement.setTextContent(name);
        inodeElement.appendChild(nameElement);

        Element permissionElement = document.createElement("permission");
        permissionElement.setTextContent(String.valueOf(permission));
        inodeElement.appendChild(permissionElement);


        Element sizeElement = document.createElement("size");
        sizeElement.setTextContent(String.valueOf(0));
        inodeElement.appendChild(sizeElement);


        String time = Config.getCNDateTimeStr();
        Element ctimeElement = document.createElement("ctime");
        ctimeElement.setTextContent(time);
        inodeElement.appendChild(ctimeElement);

        Element mtimeElement = document.createElement("mtime");
        mtimeElement.setTextContent(time);
        inodeElement.appendChild(mtimeElement);

        Element atimeElement = document.createElement("atime");
        atimeElement.setTextContent(time);
        inodeElement.appendChild(atimeElement);

        if(type.equals("file")){
            Element blocks = document.createElement("blocks");
            inodeElement.appendChild(blocks);
        }

//        printNode(inodeElement);
        return inodeElement;
    }

//    public static void saveXML(Document document, File file)
//    {
//        try
//        {
//            Writer writer = new FileWriter(file,true);
//            XMLWriter xmlWriter = new XMLWriter(writer, OutputFormat.createPrettyPrint());
//            xmlWriter.write(document);
//            xmlWriter.close();
//            writer.close();
//        }
//        catch (Exception e)
//        {
//        }
//    }

    public static void saveXMLByDom(Document document,String filePath){
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT,"yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new File(filePath));
            transformer.transform(source, result);
            System.out.println("更新文件"+filePath);
        } catch (Exception e){
        }
    }

    public static void printNode(Node node){
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = null;
        try {
            transformer = transformerFactory.newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(node), new StreamResult(writer));
            String xmlString = writer.toString();
            System.out.println(xmlString);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    public static long randomInodeId(String fsImagePath){
        long usedNum = Config.randomFd();
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(fsImagePath);
            doc.normalizeDocument();
            Element rootElement = doc.getDocumentElement();
            Element inodesElement = (Element) rootElement.getElementsByTagName("inodes").item(0);
            String usedNumStr = inodesElement.getElementsByTagName("usedNum").item(0).getTextContent();
            if (!Objects.isNull(usedNumStr) && !usedNumStr.equals("")) {
                usedNum = Integer.valueOf(usedNumStr);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return usedNum;
    }


    public static void renewInodeElememt(FileDesc fileDesc,Element originInode){
        int mode = fileDesc.getOpMode();
        Inode inode = fileDesc.getOwnInode();
        List<Block> blocks = inode.getblocks();
        if(Config.canModeWrite(mode)) {
            Node originSizeElement = originInode.getElementsByTagName("size").item(0);
            int originSize = Integer.valueOf(originSizeElement.getTextContent());
            originSizeElement.setTextContent(String.valueOf(Math.max(inode.getSize(), originSize)));
            Node originMtime = originInode.getElementsByTagName("mtime").item(0);
            originMtime.setTextContent(Config.laterDateStr(originMtime.getTextContent(), inode.getMtime()));
            Element originBlocksNode = (Element) originInode.getElementsByTagName("blocks").item(0);
            List<Block> originBlocks = Block.nodes2Blocks(originBlocksNode);
            Document originDoc = originInode.getOwnerDocument();
            if (!Objects.isNull(blocks)) {
                for (Block block : blocks) {
                    if (!isBlockExists(block, originBlocks)) {
                        Element blockElement = Block.createBlockElement(block);
                        Node importedBlockNode = originDoc.importNode(blockElement, true);
                        originBlocksNode.appendChild(importedBlockNode);
                    }
                }
            }
        }
        if(Config.canModeRead(mode)){
            Node originAtimeElement = originInode.getElementsByTagName("atime").item(0);
            originAtimeElement.setTextContent(Config.laterDateStr(originAtimeElement.getTextContent(),inode.getAtime()));
        }
//        Node perimission = originInode.getElementsByTagName("perimission").item(0);
//        perimission.setTextContent(in);
        Node originCtimeEle = originInode.getElementsByTagName("ctime").item(0);
        originCtimeEle.setTextContent(inode.getCtime());
    }

    private static boolean isBlockExists(Block block,List<Block> originBlocks){
        return originBlocks.parallelStream().anyMatch(b -> b.equals(block));
    }
}
