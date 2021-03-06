package org.jenkinsci.plugins.rpmmock;

import org.jenkinsci.plugins.rpmmock.cmdrunner.DefaultRunner;
import org.jenkinsci.plugins.rpmmock.cmdrunner.Param;

public class MockRunner extends DefaultRunner{
    public MockRunner(String cmdName) {
        super(cmdName);
    }


    public void setConfigName(String configName) {
        setConfigName(configName, null);
    }
     public void setConfigName( String configName, String path ){
        addParamWithValue("r", configName );
        if( !( path == null || path.isEmpty() ) ){
            addParamWithValue( "configdir", path, Param.ParamType.DEFAULT );
        }
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

    public void setRebuildSrpm(String srpmFile) {
        addParam( new Param("rebuild", Param.ParamType.NO_VALUE_LONG) );
        addNamelessParam( srpmFile );
    }

    public void setUniqueText(String buildName) {
        addParamWithValue("uniqueext",buildName, Param.ParamType.DEFAULT);
    }

    public void setNoCleanupAfter(){
        addParamWithValue("no-cleanup-after", "", Param.ParamType.NO_VALUE_LONG);
    }

    public void setNoClean(){
        addParamWithValue("no-clean", "", Param.ParamType.NO_VALUE_LONG);
    }
}
