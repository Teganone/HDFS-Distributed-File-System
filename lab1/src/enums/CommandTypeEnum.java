package enums;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum CommandTypeEnum {
    OPEN("open",1),
    APPEND("append",2),
    READ("read",3),
    CLOSE("close",4),
    EXIT("exit",5),
    INVALID_TYPE("invalid",0)
    ;

    private static final Map<String, CommandTypeEnum> COMMAND_TYPE_ENUM_MAP;

    private final String commandCode;
    private final int typeCode;
//    private final int argsLeastNum;

    static {
        COMMAND_TYPE_ENUM_MAP = new HashMap<>();
        for (CommandTypeEnum commandTypeEnum : EnumSet.allOf(CommandTypeEnum.class)) {
            COMMAND_TYPE_ENUM_MAP.put(commandTypeEnum.commandCode,commandTypeEnum);
        }
    }

    CommandTypeEnum(String commandCode, int type) {
        this.commandCode = commandCode;
        this.typeCode = type;
    }

    /**
     * 通过commandCode获取CommandTypeEnum
     */
    public static CommandTypeEnum getByCommandCode(String commandCode) {
        return COMMAND_TYPE_ENUM_MAP.get(commandCode);
    }


    public static Map<String, CommandTypeEnum> getCommandTypeEnumMap() {
        return COMMAND_TYPE_ENUM_MAP;
    }

    public String getCommandCode() {
        return commandCode;
    }

    public int getTypeCode() {
        return typeCode;
    }
}
