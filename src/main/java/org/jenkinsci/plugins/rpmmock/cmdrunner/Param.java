package org.jenkinsci.plugins.rpmmock.cmdrunner;

import java.text.MessageFormat;



public class Param {
    public enum ParamType { SIMPLE, DEFAULT, NO_VALUE, NO_VALUE_LONG, NAMELESS  }

    private String name;
    private String value;
    private ParamType type = ParamType.DEFAULT;

    public Param(String name, ParamType type) {
        this.name = name;
        this.type = type;
    }

    public Param( String name ){
        this( name, ParamType.DEFAULT );
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String toString() {
        final String[] arguments;

        if( type == ParamType.NAMELESS ){
            arguments = new String[]{getValue()};
        }else {
            arguments = new String[]{getName(), getValue()};
        }

        return MessageFormat.format(getFormat(), arguments );
    }



    public String getFormat() {
        switch( type ){
            case DEFAULT:
                return "--{}=\"{}\"";
            case SIMPLE:
                return "-{} {}";
            case NO_VALUE:
                return "-{}";
            case NO_VALUE_LONG:
                return "--{}";
            case NAMELESS:
                return "{}";
            default:
                return "";
        }
    }
}
