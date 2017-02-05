package com.nachtraben.commandrework;

/**
 * Created by NachtRaben on 2/3/2017.
 */
public class CommandArg {
    public String name = "";
    public boolean isDynamic = true;
    public boolean isRequired = false;
    public boolean isRest = false;

    public CommandArg(String name, boolean isDynamic, boolean isRequired, boolean isRest) {
        this.name = name;
        this.isDynamic = isDynamic;
        this.isRequired = isRequired;
        this.isRest = isRest;
    }

}
