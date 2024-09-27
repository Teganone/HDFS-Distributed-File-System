package utils;

import impl.NameNodeImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.bind.annotation.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@XmlRootElement
@XmlType(propOrder = {"id","type","name","size","permission","ctime","mtime","atime","blocks"})
public class Inode {

    private long id; //nodeId
    private String type;
    private String name;
    private long size;
    private int permission; // whether the file can read, can write
    private String ctime;
    private String mtime;
    private String atime;

    private List<Block> blocks;

    public Inode(long id, String type, String name, long size, int permission, String ctime, String mtime, String atime, List<Block> blocks) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.size = size;
        this.permission = permission;
        this.ctime = ctime;
        this.mtime = mtime;
        this.atime = atime;
        this.blocks = blocks;
    }

    public Inode(Element element){
        id = Long.valueOf(element.getElementsByTagName("id").item(0).getTextContent());
        type = element.getElementsByTagName("type").item(0).getTextContent();
        name = element.getElementsByTagName("name").item(0).getTextContent();
        permission = Integer.valueOf(element.getElementsByTagName("permission").item(0).getTextContent());
        size = Long.valueOf(element.getElementsByTagName("size").item(0).getTextContent());
        ctime = element.getElementsByTagName("ctime").item(0).getTextContent();
        mtime = element.getElementsByTagName("mtime").item(0).getTextContent();
        atime = element.getElementsByTagName("atime").item(0).getTextContent();
        blocks = Block.nodes2Blocks((Element) element.getElementsByTagName("blocks").item(0));
    }

    public Inode(boolean isSetInodeId){
        this.size = 0;
        this.permission = 3;
        String time = Config.getCNDateTimeStr();
        this.ctime = time;
        this.mtime = time;
        this.atime = time;
        this.blocks = new ArrayList<>();
        this.id = 0;
        if(isSetInodeId) {
            this.id = NameNodeImpl.getInodeId();
//            NameNodeImpl.setInodes((int) this.id);
        }
    }

    public Inode(){

    }
    public static Inode newInode(String type, String name,int permission,boolean isSetInodeId) {
        Inode inode = new Inode(isSetInodeId);
        inode.setType(type);
        inode.setName(name);
        inode.setPermission(permission);
        return inode;
    }


    public static Element createInodeElement(Inode inode){
        Document document = null;
        Element inodeElement = null;
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

            inodeElement = document.createElement("inode");

            Element idElement = document.createElement("id");
            idElement.setTextContent(String.valueOf(inode.getId()));
            inodeElement.appendChild(idElement);

            Element typeElement = document.createElement("type");
            typeElement.setTextContent(inode.getType());
            inodeElement.appendChild(typeElement);

            Element nameElement = document.createElement("name");
            nameElement.setTextContent(inode.getName());
            inodeElement.appendChild(nameElement);

            Element permissionElement = document.createElement("permission");
            permissionElement.setTextContent(String.valueOf(inode.getPermission()));
            inodeElement.appendChild(permissionElement);

            if (inode.getType().equals("file")) {
                Element sizeElement = document.createElement("size");
                sizeElement.setTextContent(String.valueOf(inode.getSize()));
                inodeElement.appendChild(sizeElement);
            }

            Element ctimeElement = document.createElement("ctime");
            ctimeElement.setTextContent(inode.getCtime());
            inodeElement.appendChild(ctimeElement);

            Element mtimeElement = document.createElement("mtime");
            mtimeElement.setTextContent(inode.getMtime());
            inodeElement.appendChild(mtimeElement);

            Element atimeElement = document.createElement("atime");
            atimeElement.setTextContent(inode.getAtime());
            inodeElement.appendChild(atimeElement);
            if (inode.getType().equals("file")) {
                Element blocks = document.createElement("blocks");
                blocks.setTextContent("");
                inodeElement.appendChild(blocks);
            }
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        return inodeElement;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Inode)) return false;
        Inode inode = (Inode) o;
        return getId() == inode.getId()
                && Objects.equals(getName(),inode.getName())
                && Objects.equals(getCtime(), inode.getCtime());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getType(), getName(), getSize(), getPermission(), getCtime(), getMtime(), getAtime(), blocks);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getPermission() {
        return permission;
    }

    public void setPermission(int permission) {
        this.permission = permission;
    }

    public String getCtime() {
        return ctime;
    }

    public void setCtime(String ctime) {
        this.ctime = ctime;
    }

    public String getMtime() {
        return mtime;
    }

    public void setMtime(String mtime) {
        this.mtime = mtime;
    }

    public String getAtime() {
        return atime;
    }

    public void setAtime(String atime) {
        this.atime = atime;
    }

    public List<Block> getblocks() {
        return this.blocks;
    }

    public void setblocks(List<Block> blocks) {
        this.blocks = blocks;
    }
}
