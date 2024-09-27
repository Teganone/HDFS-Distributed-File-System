package utils;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.bind.annotation.*;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@XmlRootElement
@XmlType(propOrder = {"dataNodeNo","blockNo"})
public class Block {

    private int dataNodeNo;
    private int blockNo;

//    private

    public Block(int dataNodeNo, int blockNo) {
        this.dataNodeNo = dataNodeNo;
        this.blockNo = blockNo;
    }

    public static List<Block> nodes2Blocks(Element element){
        ArrayList<Block> blocks = new ArrayList<>();
        if(!Objects.isNull(element)) {
            NodeList blockNodeList = element.getElementsByTagName("block");
            if (!Objects.isNull(blockNodeList)) {
                for (int i = 0; i < blockNodeList.getLength(); i++) {
                    Element blockNode = (Element) blockNodeList.item(i);
                    blocks.add(new Block(blockNode));
                }
            }
        }
        return blocks;
    }

    public Block(Element blockNode){
        this.dataNodeNo = Integer.valueOf(blockNode.getElementsByTagName("dataNodeNo").item(0).getTextContent());
        this.blockNo = Integer.valueOf(blockNode.getElementsByTagName("blockNo").item(0).getTextContent());
    }

    public static Element createBlockElement(Block block){
        Document document = null;
        Element blockElement = null;
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

            blockElement = document.createElement("block");

            Element dataNodeNoElement = document.createElement("dataNodeNo");
            dataNodeNoElement.setTextContent(String.valueOf(block.getDataNodeNo()));
            blockElement.appendChild(dataNodeNoElement);

            Element blockNoElement = document.createElement("blockNo");
            blockNoElement.setTextContent(String.valueOf(block.getBlockNo()));
            blockElement.appendChild(blockNoElement);
        } catch (Exception e){

        }
        return blockElement;
    }
    public Block(){}


    public static Element createBlockElement(){
        return null;
    }

    public void setDataNodeNo(int dataNodeNo) {
        this.dataNodeNo = dataNodeNo;
    }

    public int getBlockNo() {
        return blockNo;
    }

    public void setBlockNo(int blockNo) {
        this.blockNo = blockNo;
    }

    public int getDataNodeNo() {
        return dataNodeNo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Block)) return false;
        Block block = (Block) o;
        return getDataNodeNo() == block.getDataNodeNo() && getBlockNo() == block.getBlockNo();
    }


    @Override
    public int hashCode() {
        return Objects.hash(getDataNodeNo(), getBlockNo());
    }
}
