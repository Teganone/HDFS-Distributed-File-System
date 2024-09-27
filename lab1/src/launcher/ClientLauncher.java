package launcher;

import api.DataNode;
import api.DataNodeHelper;
import api.NameNode;
import api.NameNodeHelper;
import enums.CommandModeEnum;
import enums.CommandTypeEnum;
import impl.ClientImpl;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import utils.Command;
import utils.Config;

import javax.swing.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;
import java.util.Scanner;

public class ClientLauncher {
    private static ClientImpl client;
    public ClientLauncher() {
    }

    public static void main(String[] args) {
        client = new ClientImpl(args);
        work();
    }

    private static void work(){
        boolean flag = true;
        while (flag){
            Command command = readCommand();
            String[] args = command.getArgs();
//            System.out.println(Arrays.asList(args));
            if(!args[3].equals("OK")){
                System.out.println(Config.respINFOPrefix()+args[3]);
            } else {
                switch (command.getCommandTypeEnum()) {
                    case EXIT:
                        System.out.println(Config.respINFOPrefix()+"bye");
                        flag = false;
                        break;
                    case OPEN:
                        CommandModeEnum commandModeEnum = command.getCommandModeEnum();
//                        if (Objects.isNull(commandModeEnum) || commandModeEnum == CommandModeEnum.INVALID_MODE) {
//                            System.out.println(Config.respINFOPrefix() + "Open mode could only be r/w/rw");
//                        } else {
                        int fd = client.open(args[1], commandModeEnum.getModeCode());
                        System.out.println(Config.respINFOPrefix()+ "fd = " + fd + " (fd=0 means open failed)");
//                        }
                        break;
                    case APPEND:
                        client.append(Integer.valueOf(args[1]), args[2].getBytes());
                        break;
                    case READ:
                        byte[] readData = client.read(Integer.valueOf(args[1]));
                        System.out.println(new String(readData));
                        break;
                    case CLOSE:
                        client.close(Integer.valueOf(args[1]));
                        break;
                    default:
                        System.out.println(Config.respINFOPrefix()+ "Invalid command，please check and enter again.");
                        break;
                }
            }
        }
    }

    private static Command readCommand(){
        System.out.print(">>");
        Scanner scanner = new Scanner(System.in);
//        while(!scanner.hasNextLine()){} //确保nextLine()不为空
        String commandRawStr = scanner.nextLine();
//        scanner.close();
        return Command.addressRawCommand(commandRawStr);
    }



}
