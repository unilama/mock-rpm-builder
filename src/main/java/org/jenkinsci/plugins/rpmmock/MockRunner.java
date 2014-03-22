package org.jenkinsci.plugins.rpmmock;

import org.jenkinsci.plugins.rpmmock.cmdrunner.DefaultRunner;
import org.jenkinsci.plugins.rpmmock.cmdrunner.Param;

public class MockRunner extends DefaultRunner{
    public MockRunner(String cmdName) {
        super(cmdName);
    }


    public void setConfigName(String configName) {
        addParamWithValue("r", configName );
    }

    public void setVerbose() {
        addParam(new Param("v", Param.ParamType.NO_VALUE));
    }

    public void setupSrpmBuilder(String resultDir, String specFile, String sourcesDir) {
        setResultDir( resultDir );
        setBuildSrpm();
        setSpecFile( specFile );
        setSourcesDir( sourcesDir );
    }

    private void setSourcesDir(String sourcesDir) {
        addParamWithValue("sources", sourcesDir, Param.ParamType.DEFAULT);
    }

    private void setSpecFile(String specFile) {
        addParamWithValue( "spec", specFile, Param.ParamType.DEFAULT);
    }

    private void setBuildSrpm() {
        addParam(new Param("buildsrpm", Param.ParamType.NO_VALUE_LONG));
    }

    private void setResultDir(String resultDir) {
        addParamWithValue("resultdir", resultDir, Param.ParamType.DEFAULT);
    }

    public void setupRebuild(String resultDir, String srpmFile) {
        setResultDir( resultDir );
        setRebuildSrpm(srpmFile);
    }

    private void setRebuildSrpm(String srpmFile) {
        addParamWithValue("rebuild", srpmFile, Param.ParamType.DEFAULT);
    }
}
