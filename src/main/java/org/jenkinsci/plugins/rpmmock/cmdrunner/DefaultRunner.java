package org.jenkinsci.plugins.rpmmock.cmdrunner;

import hudson.util.ArgumentListBuilder;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

public class DefaultRunner implements RunnerInterface {
    protected StringBuilder stringBuilder;
    private String cmdName;
    protected LinkedList<Param> params = new LinkedList<Param>();

    public DefaultRunner(String cmdName) {
        this.cmdName = cmdName;
    }

    public String getCommand() {
        stringBuilder = new StringBuilder();
        stringBuilder.append(getCmdName());
        for (Param param : getParams()) {
            stringBuilder.append(" ");
            stringBuilder.append(param);
        }
        return stringBuilder.toString();
    }

    public ArgumentListBuilder toArgumentListBuilder(){
        ArgumentListBuilder argsBuilder = new ArgumentListBuilder();
        argsBuilder.add(getCmdName());
        for (Param param : getParams()) {
            argsBuilder.add(param, param.isMasked());
        }
        return argsBuilder;
    }

    public Param[] getParams() {
        return params.toArray(new Param[params.size()]);
    }

    public String getCmdName() {
        return cmdName;
    }

    public void resetParams(){
        params.clear();
    }

    public void addParam(Param param) {
        if( params.indexOf(param) == -1 ){
            params.addFirst( param );
        }
    }

    public void addParamWithValue( String name, String value, Param.ParamType type ){
        final Param param = new Param( name, type );
        param.setValue( value );
        addParam( param );
    }

    public void addParamWithValue( String name, String value ){
        addParamWithValue(name, value, Param.ParamType.SIMPLE);
    }

    public void addNamelessParam(String value) {
        final Param param = new Param( "", Param.ParamType.NAMELESS );
        param.setValue(value);
        params.addLast(param);
    }
}
