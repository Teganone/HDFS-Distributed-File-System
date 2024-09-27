package impl;
//TODO: your implementation
import api.DataNodePOA;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import utils.Config;
import utils.FsImageParser;

import javax.swing.plaf.synth.SynthOptionPaneUI;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;

public class DataNodeImpl extends DataNodePOA {
    private long id;
    private String dataNodePath;

    private String dnMetaDataPath;
    private boolean[] blocks;
//    private int blockNum;
    private boolean lock = true;

    public DataNodeImpl(long id){
        this.id = id;
        this.dataNodePath = Config.getDataNodePath(this.id);
        this.dnMetaDataPath = this.dataNodePath + "metaData.xml";
        loadMetaDataFromDisk();
    }


    public DataNodeImpl(){}

    @Override
    public byte[] read(int block_id) {
//        byte[] buf = new byte[1024];
//        byte[] result = new byte[Config.DATA_BLOCK_SIZE];
//        FileInputStream fileInputStream = null;
//        String filePath = dataNodePath + block_id + ".block";
//        File file = new File(filePath);
//        int readData = 0;
//        int startIndex = 0;
//        try {
//            if(file.exists()) {
//                fileInputStream = new FileInputStream(file);
//                while ((readData = fileInputStream.read(buf)) != -1) {
//                    System.arraycopy(buf, 0, result, startIndex, readData);
//                    startIndex += readData;
//                }
//                fileInputStream.close();
//            }
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//        return result;
        String filePath = dataNodePath + block_id + ".block";
        File file = new File(filePath);
        byte[] buffer = new byte[Config.DATA_BLOCK_SIZE];
        byte[] result = new byte[0];
        try
        {
            if(file.exists()) {
                FileInputStream inputStream = new FileInputStream(file);
                int byteCount = inputStream.read(buffer);
                if (byteCount > 0) {
                    result = new byte[Config.DATA_BLOCK_SIZE];
                    System.arraycopy(buffer, 0, result, 0, byteCount);
                }

                inputStream.close();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public void append(int block_id, byte[] bytes) {
        String filePath = dataNodePath + block_id + ".block";
        System.out.println(filePath);
        File file = new File(filePath);
        try {
            if (!file.exists()) {
                // 如果文件不存在，则创建文件及其父目录
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            int length = bytes.length;
            for (int i = 0; i < bytes.length; ++i) {
                if (bytes[i] == 0) {
                    length = i;
                    break;
                }
            }
            byte[] tmp = new byte[length];
            FileOutputStream fileOutputStream = new FileOutputStream(file, true);
            System.arraycopy(bytes,0, tmp, 0, length);
            fileOutputStream.write(tmp);
            fileOutputStream.flush();
            fileOutputStream.close();
//            blocks[block_id] = true;
            saveToDisk(block_id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int randomBlockId() {
        while (!lock) {}
        lock = false;
        for (int i = 0; i < blocks.length; i++) {
            if (blocks[i] == false) {
                blocks[i] = true;
                lock=true;
                return i;
            }
        }
        lock=true;
        return -1; //磁盘已满
    }

    public int getUsedBlockNum() {
        return (int) IntStream.range(0, blocks.length)
                .mapToObj(i -> blocks[i])
                .filter(b -> b)
                .count();
    }

    public int getBlocksCapacity(){
        return blocks.length;
    }
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void loadMetaDataFromDisk(){
        try {
            File file = new File(dnMetaDataPath);
            if(!file.exists()){
                initializeMetaData();
            }
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(dnMetaDataPath);
            doc.normalizeDocument();
            Element rootElement = doc.getDocumentElement();
            String capacityStr = rootElement.getElementsByTagName("capacity").item(0).getTextContent();
            int capacity = Config.DEFAULT_MAX_BLOKC_NUM;
            if(!Objects.isNull(capacityStr) && !capacityStr.equals("")) {
                capacity = Integer.valueOf(capacityStr);
            }
            Node usedNumEle = rootElement.getElementsByTagName("usedNum").item(0);
            Element usedBlocksElement = (Element) rootElement.getElementsByTagName("usedBlocks").item(0);
            NodeList usedBlockId = usedBlocksElement.getElementsByTagName("id");
            blocks = new boolean[capacity];
            Arrays.fill(blocks,false);
            int count = 0;
            for (int i = 0; i < usedBlockId.getLength(); i++) {
                Integer blockId = Integer.valueOf(usedBlockId.item(i).getTextContent());
                blocks[blockId]  = true;
                count++;
            }
            usedNumEle.setTextContent(String.valueOf(count));
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveToDisk(int blockId){
        try {
            File file = new File(dnMetaDataPath);
            if(!file.exists()){
                initializeMetaData();
            }
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(dnMetaDataPath));
            doc.normalizeDocument();
            Element rootElement = doc.getDocumentElement();
            Node usedNum = rootElement.getElementsByTagName("usedNum").item(0);
            usedNum.setTextContent(String.valueOf(Integer.valueOf(usedNum.getTextContent())+1));
            Element usedBlocksElement = (Element) rootElement.getElementsByTagName("usedBlocks").item(0);
            NodeList idNodeList = usedBlocksElement.getElementsByTagName("id");
            for (int i = 0; i < idNodeList.getLength(); i++) {
                Node idNode = idNodeList.item(i);
                if(idNode.getTextContent().equals(String.valueOf(blockId))){
                    return; //已有。
                }
            }
            Element block = doc.createElement("block");
            Element id = doc.createElement("id");
            id.setTextContent(String.valueOf(blockId));
            block.appendChild(id);
            usedBlocksElement.appendChild(block);
            FsImageParser.saveXMLByDom(doc,dnMetaDataPath);

        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }

    }

    private void initializeMetaData() {
        try {
//            File file = new File(dnMetaDataPath);
//            if (!file.exists()) {
//                file.getParentFile().mkdirs();
//                file.createNewFile();
//
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(Config.metaDataConfigPath);
            FsImageParser.saveXMLByDom(doc,dnMetaDataPath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
