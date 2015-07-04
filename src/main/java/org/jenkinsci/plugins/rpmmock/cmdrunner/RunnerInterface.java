package org.jenkinsci.plugins.rpmmock.cmdrunner;

import hudson.util.ArgumentListBuilder;

/**
 * Created by marcin on 22.03.14.
 */
public interface RunnerInterface {
    String getCommand();
    Param[] getParams();
    ArgumentListBuilder toArgumentListBuilder();
}
