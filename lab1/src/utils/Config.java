package utils;

import enums.CommandModeEnum;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Random;

public class Config {
    public static Integer MAX_DATANODE_COUNT = 3;
//    public static Integer[] CURRENT_DATANODE_BLOCKNUM;
    public static final String NAMENODE_PATH = "lab1/NameNodeDir/";
    public static String BASIC_DATANODE_PATH = "lab1/DataNode";
//    public static final String NAMENODE_PATH = "../NameNodeDir/";//..for test
//    public static String BASIC_DATANODE_PATH = "../DataNode"; //..for test
    public static Integer DATA_BLOCK_SIZE = 4*1024; //4KB
    public static Integer DEFAULT_MAX_BLOKC_NUM = 40;
    public static Integer DEFAULT_MAX_INODE_NUM = 400;
    public static Integer MAX_CLIENT_FD_NUM = 64;
//    public static Long InodeId;
    public static Long fdId;
    public static String metaDataConfigPath = "lab1/Config/metaData.xml";
//    public static String metaDataConfigPath = "../Config/metaData.xml";  //for test
    public static String fsImageConfigPath = "lab1/Config/fsImage.xml";
//    public static String fsImageConfigPath = "../Config/fsImage.xml"; //for test


    static {
//        CURRENT_DATANODE_BLOCKNUM = new Integer[DEFULT_MAX_BLOKC_NUM];
//        Arrays.fill(CURRENT_DATANODE_BLOCKNUM,0);
        fdId = randomFd(); //每次新开一个fd
//        InodeId = FsImageParser.randomInodeId();
    }
    public static String getDataNodePath(long id){
        return BASIC_DATANODE_PATH + id + "Dir/";
    }

    public static void main(String[]args){
        String data = "654321";
        System.out.println("MD5: " + getMD5(data));
        System.out.println(System.currentTimeMillis());

        System.out.println(Long.valueOf(Config.getStartIndex(String.valueOf(System.currentTimeMillis()))));
        System.out.println(Long.valueOf(Config.getStartIndex(String.valueOf(System.currentTimeMillis()))));
    }

    public static String getMD5(String txt)
    {
        String result = "";
        String hexDigits = "0123456789abcdef";

        try
        {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte res[] = messageDigest.digest(txt.getBytes(StandardCharsets.UTF_8));
            StringBuffer buffer = new StringBuffer();
            for (byte r:res)
            {
                int b = r;
                if (b < 0)
                {
                    b += 256;
                }
                buffer.append(hexDigits.charAt(b / 16));
                buffer.append(hexDigits.charAt(b % 16));
            }
            result = buffer.toString();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return  result;
    }
    public static int getHash(String str)
    {
        String num = getMD5(str).substring(0, 7);  // will get 15*4=60bit, which is long enough
        System.out.println(num);
        return Integer.valueOf(num, 16);
    }

    public static int getStartIndex(String str){
        String num = getMD5(str).substring(str.length()-5,str.length());
        System.out.println(num);
        return Integer.valueOf(num, 16);
    }

    public static int random(){
        return 0;
    }

    public static String getCNDateTimeStr() {
        LocalDateTime now = LocalDateTime.now();
        return now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public static String laterDateStr(String dateStr1,String dateStr2){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date1 = dateFormat.parse(dateStr1);
            Date date2 = dateFormat.parse(dateStr2);
            int result = date1.compareTo(date2);

            if (result < 0) {
                return dateStr2;
            } else {
                return dateStr1;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateStr1;
    }

    public static LocalDateTime getCNDateTime(){
        return LocalDateTime.now();
    }

    public static int isModePermitted(int permission, int mode){
        boolean hasReadPermission = (permission & CommandModeEnum.READ_MODE.getModeCode()) != 0;
        boolean hasWritePermission = (permission & CommandModeEnum.WRITE_MODE.getModeCode()) != 0;
        boolean hasReadMode = (mode & CommandModeEnum.READ_MODE.getModeCode()) != 0;
        boolean hasWriteMode = (mode & CommandModeEnum.WRITE_MODE.getModeCode()) != 0;
        int isModePermitted = 0;
        if((hasReadMode && hasReadPermission)) {
            isModePermitted = 1;
        } else if(hasWriteMode && hasWritePermission) {
            isModePermitted = 2;
        } else if(hasReadMode && hasWriteMode && hasReadPermission && hasWritePermission){
            isModePermitted = 3;
        }
        return isModePermitted;
    }

//    public static boolean isWritePermitted(int permission, int mode){
//        boolean hasReadPermission = (permission & CommandModeEnum.READ_MODE.getModeCode()) != 0;
//
//    }

    public static boolean canModeRead(int mode){
        return (mode & CommandModeEnum.READ_MODE.getModeCode()) != 0;
    }

    public static boolean canModeWrite(int mode){
        return (mode & CommandModeEnum.WRITE_MODE.getModeCode()) != 0;
    }

//    public static int getModeCode(int mode){
//        int result = 0;
//        switch (mode){
//            case CommandModeEnum.READ_MODE.getModeCode():
//
//        }
//        boolean canModeRead = (mode & CommandModeEnum.READ_MODE.getModeCode()) != 0;
//        boolean canModeWrite = (mode & CommandModeEnum.WRITE_MODE.getModeCode()) != 0;
//
//    }

    public static long randomFd() {
        Random random = new Random();
        long num = random.nextInt(4096); // 生成一个随机长整型
        long hex = Long.parseLong(Long.toHexString(num), 16); // 将十六进制字符串转换为长整型
        return hex;
    }

    public static String respINFOPrefix(){
        return "["+ Config.getCNDateTimeStr()+": INFO] ";
    }
}
