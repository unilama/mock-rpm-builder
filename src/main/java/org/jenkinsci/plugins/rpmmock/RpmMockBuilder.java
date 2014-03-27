package org.jenkinsci.plugins.rpmmock;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.rpmmock.cmdrunner.CommandRunner;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Sample {@link Builder}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link RpmMockBuilder} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields (like {@link #specFile})
 * to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #perform(AbstractBuild, Launcher, BuildListener)}
 * method will be invoked. 
 *
 * @author Marcin Stanisławski <marcin.stanislawski@gmail.com>
 *
 * TODO: add auto changelog
 */
public class RpmMockBuilder extends Builder {

    private final String specFile;
    private final Boolean downloadSources;
    private final Boolean verbose;
    private final String configName;
    private final String srcRpmRegExp;
    private boolean uniqueMockPerBuild;

    @DataBoundConstructor
    public RpmMockBuilder(String specFile, Boolean downloadSources, Boolean verbose, String configName, String srcRpmRegExp, Boolean uniqueMockPerBuild ) {
        this.specFile = specFile;
        this.downloadSources = downloadSources;
        this.verbose = verbose;
        this.configName = configName;
        this.srcRpmRegExp = srcRpmRegExp;
        this.uniqueMockPerBuild = uniqueMockPerBuild;
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        PrintStream logger = listener.getLogger();
        FilePath workspace = build.getWorkspace();
        CommandRunner commandRunner = getCommandRunner(build, launcher, listener);
        int result;

        //@todo add to configuration
        String sourceDir = workspace+"/SOURCES", specFile = workspace+"/"+getSpecFile();

        if( getDownloadSources() ){

            final SpecToolRunner specToolRunner = SpecToolRunner.buildSourceDownloader(specFile, sourceDir);
            if( getVerbose() ){
                specToolRunner.setVerbose();
            }

            try {
                result = commandRunner.runCommand( specToolRunner );
                if( CommandRunner.isError( result ) ){
                    logger.println( "Spectool doesn't finish properly, exit code: "+result );
                    return false;
                }
            } catch (Exception e) {
                logger.println("Downloading sources fail due to: " + e.getMessage());
                return false;
            }
        }

        //@todo add to configuration
        String resultSRPMDir = workspace+"/SRPMS", resultRPMDir = workspace+"/RPMS";
        MockRunner mockRunner = buildMockRunner(build.getDisplayName());
        mockRunner.setupSrpmBuilder( resultSRPMDir, specFile, sourceDir );
        try {
            result = commandRunner.runCommand(mockRunner );
            if( CommandRunner.isError(result) ){
                logger.println( "Source rpm using mock creation doesn't finish properly, exit code:"+result );
                return false;
            }
        }catch (Exception e) {
            logger.println("Building source RPM fail due to: " + e.getMessage());
            return false;
        }

        mockRunner = buildMockRunner(build.getDisplayName());
        try {
            mockRunner.setupRebuild( resultRPMDir, getSRPMFile(resultSRPMDir) );
            result = commandRunner.runCommand(mockRunner);
            if( CommandRunner.isError(result) ){
                logger.println( "Rpm using mock creation doesn't finish properly, exit code: "+result );
                return false;
            }
        }catch (Exception e) {
            logger.println("Building RPM fail due to: " + e.getMessage());
            e.printStackTrace(logger);
            return false;
        }

        return true;
    }

    private String getSRPMFile(String SRPMDirName) throws IOException {
        //@todo add remove trailing slash
        File SRPMDir = new File(SRPMDirName).getCanonicalFile();
        if( !SRPMDir.isDirectory() ){
            throw new FileNotFoundException( "SRPM dir doesn't exists or is not a dir("+SRPMDirName+")." );
        }

        Pattern pattern = getSrcRpmFilenamePattern();
        String filename;

        for( File file : SRPMDir.listFiles() ){
            filename = file.getName();
            if( pattern.matcher(filename).find() ){
                return SRPMDir+"/"+filename;
            }
        }

        throw new FileNotFoundException( "Can't find any source RPM. Regular expression used: "+pattern );
    }

    private Pattern getSrcRpmFilenamePattern() {
        return Pattern.compile( getSrcRpmRegExp() );
    }

    private CommandRunner getCommandRunner(AbstractBuild build, Launcher launcher, BuildListener listener) {
        try {
            EnvVars envVars =build.getEnvironment(listener);
            for(Map.Entry<String,String> e : ((Map<String,String>)build.getBuildVariables()).entrySet())
                envVars.put(e.getKey(),e.getValue());

            return new CommandRunner(launcher, listener, envVars );
        } catch (IOException e) {
            e.printStackTrace();
            return new CommandRunner( launcher, listener );
        } catch (InterruptedException e) {
            e.printStackTrace();
            return new CommandRunner( launcher, listener );
        }
    }

    private MockRunner buildMockRunner(String buildName) {
        MockRunner mockRunner = new MockRunner(getDescriptor().getMockCmd());
        if (getVerbose()) {
            mockRunner.setVerbose();
        }
        mockRunner.setConfigName(getConfigName());
        if (getUniqueMockPerBuild()) {
            mockRunner.setUniqueText(sanitizeBuildName(buildName));
        }
        return mockRunner;
    }

    private String sanitizeBuildName(String buildName) {
        return buildName.toLowerCase().replaceAll("[^a-z0-9-_]", "-");
    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    public Boolean getDownloadSources() {
        return downloadSources;
    }

    public Boolean getVerbose() {
        return verbose;
    }

    public String getSpecFile() {
        return specFile;
    }

    public String getConfigName() {
        return configName;
    }

    public String getSrcRpmRegExp() { return srcRpmRegExp; }

    public boolean getUniqueMockPerBuild() {
        return uniqueMockPerBuild;
    }

    /**
     * Descriptor for {@link RpmMockBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See <tt>src/main/resources/hudson/plugins/hello_world/RpmMockBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        protected String mockCmd;
        /**
         * In order to load the persisted global configuration, you have to 
         * call load() in the constructor.
         */
        public DescriptorImpl() {
            load();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }


        /**
         * This human readable specFile is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Build RPM using mock";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            setMockCmd(formData.getString("mockCmd"));
            save();
            return super.configure(req,formData);
        }

        public FormValidation doCheckMockCmd( @QueryParameter String mockCmd ){
            File mockExe = new File( mockCmd );
            if( mockExe.canExecute() ){
                return FormValidation.error( "Mock command '"+mockCmd+"' must be executable" );
            }

            return FormValidation.ok();
        }

        public String defaultMockCmd(){
            return "/usr/bin/mock";
        }

        public String getMockCmd() {
            return mockCmd;
        }

        public void setMockCmd( String mockCmd ) {
            this.mockCmd = mockCmd;
        }
    }
}

