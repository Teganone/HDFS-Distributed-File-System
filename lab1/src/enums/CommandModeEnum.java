package enums;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum CommandModeEnum {
    READ_MODE("r",0b01),
    WRITE_MODE("w",0b10),
    READ_WRITE_MODE("rw",0b11),

    INVALID_MODE("invalid",0)
    ;

    private final static Map<String,CommandModeEnum> COMMAND_MODE_ENUM_MAP;

    static {
        COMMAND_MODE_ENUM_MAP = new HashMap<>();
        for(CommandModeEnum commandModeEnum: EnumSet.allOf(CommandModeEnum.class)){
            COMMAND_MODE_ENUM_MAP.put(commandModeEnum.mode, commandModeEnum);
        }
    }
    private String mode;
    private int modeCode;


    CommandModeEnum(String mode, int modeCode) {
        this.mode = mode;
        this.modeCode = modeCode;
    }

    public static CommandModeEnum get(String mode){
        return COMMAND_MODE_ENUM_MAP.get(mode);
    }

    public Map<String,CommandModeEnum> getCommandModeEnumMap(){
        return COMMAND_MODE_ENUM_MAP;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public int getModeCode() {
        return modeCode;
    }

    public void setModeCode(int modeCode) {
        this.modeCode = modeCode;
    }
}
