package utils;

import enums.CommandModeEnum;
import enums.CommandTypeEnum;

import java.util.Objects;
import java.util.Optional;

public class Command {

    private CommandTypeEnum commandTypeEnum;
    private static String[] args;
    private CommandModeEnum commandModeEnum;



    public Command(CommandTypeEnum commandTypeEnum, String[] args) {
        this.commandTypeEnum = commandTypeEnum;
        this.args = args;
    }

    public Command(CommandTypeEnum commandTypeEnum, String[] args, CommandModeEnum commandModeEnum) {
        this.commandTypeEnum = commandTypeEnum;
        this.args = args;
        this.commandModeEnum = commandModeEnum;
    }

    public void setCommandModeEnum(CommandModeEnum commandModeEnum) {
        this.commandModeEnum = commandModeEnum;
    }

    public Command(){
    }


    public static Command addressRawCommand(String commandRawStr){
        String commandStr = removeExtraSpaces(commandRawStr);
        String[] commandArgs = commandStr.split(" ");
        CommandTypeEnum commandType = CommandTypeEnum.getByCommandCode(commandArgs[0]);
        if(Objects.isNull(commandType)){
            commandType = CommandTypeEnum.INVALID_TYPE;
        }
        String[] checkedArgs = checkArgs(commandArgs);
        Command command = new Command(commandType,checkedArgs);
        if("open".equals(commandArgs[0])) {
            CommandModeEnum commandModeEnum = CommandModeEnum.get(checkedArgs[2]);
            command.setCommandModeEnum(commandModeEnum);
        }
        return command;
    }

    public static String removeExtraSpaces(String commandRawStr){
        return commandRawStr.replaceAll("\\s+"," ");
    }

    //判断参数的检验函数并修改为default，确保能正常调用接口。
    public static String[] checkArgs(String commandArgs[]){
        String[] res = new String[4];
        res[3] = "OK";
        switch (commandArgs[0]) {
            case "open":
                System.arraycopy(commandArgs,0,res,0,commandArgs.length);
                if (commandArgs.length < 3) {
                    res[2] = "invalid";
                    res[3] = "open command should have another 2 args. (eg: open file.txt r)";
                } else {
                    CommandModeEnum commandModeEnum = CommandModeEnum.get(commandArgs[2]);
                    if (Objects.isNull(commandModeEnum)) {
//                        commandModeEnum = CommandModeEnum.INVALID_MODE;
                        res[3] = "Open mode could only be r/w/rw";
                    }
                }
                break;
            case "append":
                if(commandArgs.length<3) {
                    res[1] = "0";
                    res[3] = "append command should have at least another 2 args. (eg: append 1 hello world)";
                } else if(!canConvertToInt(commandArgs[1])){
                    res[1] = "0";
                    res[3] = "fd has to be an integer.";
                } else {
                    StringBuffer stringBuffer = new StringBuffer();
                    for (int i = 2; i < commandArgs.length-1; i++) {
                        stringBuffer.append(commandArgs[i]).append(" ");
                    }
                    stringBuffer.append(commandArgs[commandArgs.length-1]);
                    System.arraycopy(commandArgs,0,res,0,2);
                    res[2] = stringBuffer.toString();
                }
                break;
            case "read":
                res[0] = commandArgs[0];
                if(commandArgs.length<2) {
                    res[1] = "0";
                    res[3] = "read command should have another 2 args. (eg: read 1)";
                } else if(!canConvertToInt(commandArgs[1])){
                    res[1] = "0";
                    res[3] = "fd has to be an integer.";
                } else {
                    res[1] = commandArgs[1];
                }
                break;
            case "close":
                res[0] = commandArgs[0];
                if(commandArgs.length<2) {
                    res[1] = "0";
                    res[3] = "close command should have another 1 args. (eg: close 1)";
                } else if(!canConvertToInt(commandArgs[1])){
                    res[1] = "0";
                    res[3] = "fd has to be an integer.";
                } else {
                    res[1] = commandArgs[1];
                }
                break;
            default:
                break;
        }
        return res;
    }
    public CommandTypeEnum getCommandTypeEnum() {
        return commandTypeEnum;
    }

    public void setCommandTypeEnum(CommandTypeEnum commandTypeEnum) {
        this.commandTypeEnum = commandTypeEnum;
    }

    public static String[] getArgs() {
        return args;
    }

    public static void setArgs(String[] args) {
        Command.args = args;
    }

    public CommandModeEnum getCommandModeEnum() {
        return commandModeEnum;
    }

    public static boolean canConvertToInt(String str){
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }



}

