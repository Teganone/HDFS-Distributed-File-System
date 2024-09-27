package utils;


import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


//TODO: According to your design, complete the FileDesc class, which wraps the information returned by NameNode open()
@XmlRootElement
@XmlType(propOrder = {"id","inode","opMode","writeRef","readRef","resultMessage"})
public class FileDesc {
    /* the id should be assigned uniquely during the lifetime of NameNode,
     * so that NameNode can know which client's open has over at close
     * e.g., on nameNode1
     * client1 opened file "Hello.txt" with mode 'w' , and retrieved a FileDesc with 0x889
     * client2 tries opening the same file "Hello.txt" with mode 'w' , and since the 0x889 is not closed yet, the return
     * value of open() is null.
     * after a while client1 call close() with the FileDesc of id 0x889.
     * client2 tries again and get a new FileDesc with a new id 0x88a
     */
    @XmlElement
    final long id;
    @XmlElement
    private Inode inode;
    private int opMode; // to read/write in the fd
    private int writeRef; //0 or 1
    private int readRef;  // current read num;
    private String resultMessage;  //回复信息

    public static void main(String[] args) {
//        Node node = addFileDescXml();
//        FileDesc fileDesc = new FileDesc(200, "test.txt", "file", 0b11);
//        List<Block> blockList = fileDesc.getOwnInode().getblocks();
//        blockList.add(new Block(1,1));
//        String string = fileDesc.toString();
//        System.out.println(string);
//        FileDesc fileDesc1 = FileDesc.fromString(string);
//        System.out.println("-----------");
//        System.out.println(fileDesc1.toString());
//        FileDesc fileDesc = new FileDesc();
//        fileDesc.setResultMessage("消息错误");
//        String string = fileDesc.toString();
//        System.out.println(string);
//        FileDesc fileDesc1 = FileDesc.fromString(string);
//        System.out.println(fileDesc1.toString());


    }

    public FileDesc(long id) {
        this.id = id;
    }

    public FileDesc(){this.id = -1;}
    public static FileDesc ofWrong(String result){
        FileDesc fileDesc = new FileDesc();
        fileDesc.setResultMessage(result);
        return fileDesc;
    }

    public void ofSuccess(){
        this.resultMessage = "OK";
    }
//    public FileDesc(long id, String name, String type, int opMode){
//        this.id = id;
//        this.inode = Inode.newInode(type,name,3);
//        this.opMode = opMode;
//        this.writeRef = 0;
//        this.readRef = 0;
//    }

    public FileDesc(long id, Element element, int opMode){
        Inode newInode = new Inode(element);
        this.id = id;
        this.inode = newInode;
        this.opMode = opMode;
//        newInode.setNodeId(Long.valueOf(element.getElementsByTagName("id").item(0).getTextContent()));
//        newInode.setType(element.getElementsByTagName("type").item(0).getTextContent());
//        newInode.setName(element.getElementsByTagName("name").item(0).getTextContent());
//        newInode.setPermission(Integer.valueOf(element.getElementsByTagName("permission").item(0).getTextContent()));
//        newInode.setSize(Long.valueOf(element.getElementsByTagName("size").item(0).getTextContent()));
//        newInode.setCtime(element.getElementsByTagName("ctime").item(0).getTextContent());
//        newInode.setMtime(element.getElementsByTagName("mtime").item(0).getTextContent());
//        newInode.setAtime(element.getElementsByTagName("atime").item(0).getTextContent());
//        newInode.setBlockList(Block.nodes2Blocks((Element) element.getElementsByTagName("blocks").item(0)));
    }

    public FileDesc(long id, Inode inode, int opMode, int writeRef, int readRef) {
        this.id = id;
        this.inode = inode;
        this.opMode = opMode;
        this.writeRef = writeRef;
        this.readRef = readRef;
    }

    public Node addFileDescXml(){
        try{
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbFactory.newDocumentBuilder();
            Document document = builder.newDocument();
            System.out.println(document);
            Element fileDescRoot = document.createElement("FileDesc");
            document.appendChild(fileDescRoot);
            Element fdId = document.createElement("fdId");
            fdId.setTextContent(String.valueOf(id));
            fileDescRoot.appendChild(fdId);
//            Inode.createInodeElement()
            return null;
        } catch (Exception e){

        }
        return null;

    }


    @Override
    public String toString() {
        try {
            // 创建一个对象
            // 创建JAXBContext实例
            JAXBContext jaxbContext = JAXBContext.newInstance(FileDesc.class);

            // 创建Marshaller实例
            Marshaller marshaller = jaxbContext.createMarshaller();

            // 设置输出格式为漂亮的格式
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            // 将对象转换为XML字符串
            StringWriter stringWriter = new StringWriter();

            // 将对象转换为XML字符串
            marshaller.marshal(this, stringWriter);

            // 获取XML字符串
            String xmlString = stringWriter.toString();
            return xmlString;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static FileDesc fromString(String str){
        FileDesc fileDesc = null;
        if(!Objects.isNull(str)) {
            try {
                JAXBContext jaxbContext = JAXBContext.newInstance(FileDesc.class);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                fileDesc = (FileDesc) unmarshaller.unmarshal(new StringReader(str));
            } catch (JAXBException e) {
                e.printStackTrace();
            }
        }
        return fileDesc;
    }


//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (!(o instanceof FileDesc)) return false;
//        FileDesc fileDesc = (FileDesc) o;
//        return this.id == fileDesc.getId()
//                && this.opMode == fileDesc.getOpMode()
//                && this.writeRef == fileDesc.getWriteRef()
//                && this.readRef == fileDesc.getReadRef()
//                && Objects.equals(inode, fileDesc.inode)
//                && Objects.equals(this.resultMessage, fileDesc.getResultMessage());
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileDesc)) return false;
        FileDesc fileDesc = (FileDesc) o;
        return this.id == fileDesc.getId();
//                && Objects.equals(inode, fileDesc.inode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), inode, getOpMode(), getWriteRef(), getReadRef(), getResultMessage());
    }


    public long getId() {
        return id;
    }


    public int getWriteRef() {
        return writeRef;
    }

    public void setWriteRef(int writeRef) {
        this.writeRef = writeRef;
    }

    public int getReadRef() {
        return readRef;
    }

    public void setReadRef(int readRef) {
        this.readRef = readRef;
    }

    public Inode getOwnInode() {
        return inode;
    }

    public void setInode(Inode inode) {
        this.inode = inode;
    }

    public int getOpMode() {
        return opMode;
    }

    public void setOpMode(int opMode) {
        this.opMode = opMode;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }
}
