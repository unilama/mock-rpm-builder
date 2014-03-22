package org.jenkinsci.plugins.rpmmock.cmdrunner;

/**
 * Created by marcin on 22.03.14.
 */
public interface RunnerInterface {
    public String getCommand();
    public Param[] getParams();
}
