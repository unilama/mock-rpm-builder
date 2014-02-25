package org.jenkinsci.plugins.rpmmock;
import com.google.common.io.CharStreams;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Extension;
import hudson.Proc;
import hudson.util.FormValidation;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.AbstractProject;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.MessageFormat;

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
 * @todo add auto changelog
 */
public class RpmMockBuilder extends Builder {

    private final String specFile;
    private final Boolean downloadSources;
    private final Boolean verbose;

    @DataBoundConstructor
    public RpmMockBuilder(String specFile, Boolean downloadSources, Boolean verbose) {
        this.specFile = specFile;
        this.downloadSources = downloadSources;
        this.verbose = verbose;
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        PrintStream logger = listener.getLogger();
        FilePath workspace = build.getWorkspace();

        String sourceDir = workspace+"/SOURCES", specFile = workspace+"/"+getSpecFile();

        if( getDownloadSources() ){
            try {
                logger.append(runShellCommand(launcher, listener, "spectool -C {0} -R -g {1}", specFile, sourceDir));
            } catch (Exception e) {
                logger.append("Downloading sources fail due to: "+e.getMessage());
                return false;
            }
        }

        String result, command, resultSRPMDir = workspace+"/SRPMS", resultRPMDir = workspace+"/RPMS";
        try {
            //@todo check exit code
            command = getMockCmd()+" "+getVerboseOption()+" --resultdir={0} --buildsrpm --spec {1} --sources {2}";
            result = runShellCommand(launcher, listener, command, resultSRPMDir, specFile, sourceDir );
            logger.append(result);
        }catch (Exception e) {
            logger.append("Building source RPM fail due to: "+e.getMessage());
            return false;
        }

        try {
            //@todo check exit code
            command = getMockCmd()+" "+getVerboseOption()+" --resultdir={0} --rebuild {1}";
            result = runShellCommand(launcher, listener, command, resultRPMDir, resultSRPMDir+"/*.src.rpm" );
            logger.append(result);
        }catch (Exception e) {
            logger.append("Building RPM fail due to: "+e.getMessage());
            return false;
        }

        return true;
    }

    private String getMockCmd(){
        return "/usr/bin/mock";
    }

    private String getVerboseOption(){
        if( getVerbose() ){
            return " -v ";
        }

        return "";
    }

    private String runShellCommand( Launcher launcher, BuildListener listener, String cmd, Object... params ) throws Exception {
        String command = MessageFormat.format(cmd, params);
        try {
            Proc proc = launcher.launch().cmdAsSingleString(command).readStdout().start();
            return CharStreams.toString(new InputStreamReader(proc.getStdout()));
        } catch (IOException e) {
            e.printStackTrace(listener.getLogger());
            throw new Exception(MessageFormat.format("Command <{0}> failed", command), e);
        }
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
            //config to
            save();
            return super.configure(req,formData);
        }
    }
}

