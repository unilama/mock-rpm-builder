package org.jenkinsci.plugins.rpmmock;

import com.google.common.io.CharStreams;
import hudson.EnvVars;
import hudson.Launcher;
import hudson.Proc;
import hudson.model.TaskListener;

import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;

public class CommandRunner {
    private final TaskListener listener;
    private final EnvVars envVars;
    private final Launcher launcher;

    public static class CommandResult{
        private int exitCode = 0;
        private String stdErr;
        private String stdOut;

        public CommandResult( int exitCode, String stdOut ){
            this(exitCode, stdOut, "");
        }

        public CommandResult( int exitCode, String stdOut, String stdErr ){
            this.exitCode = exitCode;
            this.stdOut = stdOut;
            this.stdErr = stdErr;
        }

        public boolean isError(){
            return exitCode != 0;
        }

        public int getExitCode(){
            return exitCode;
        }


        public String getStdError() {
            return stdErr;
        }

        public String getStdOutput() {
            return stdOut;
        }
    }

    public CommandRunner( Launcher launcher, TaskListener listener, EnvVars envVars ){
        this.launcher = launcher;
        this.listener = listener;
        this.envVars = envVars;
    }

    public CommandRunner( Launcher launcher, TaskListener listener ){
        this(launcher, listener, new EnvVars());
    }

    public CommandResult runCommand( String command ) throws Exception {
        try {
            Proc proc = launcher.launch().envs(envVars).cmdAsSingleString(command).readStdout().start();
            String stdOut = CharStreams.toString(new InputStreamReader(proc.getStdout()));
            int exitCode = proc.join();
            return new CommandResult( exitCode, stdOut );
        } catch (IOException e) {
            e.printStackTrace(listener.getLogger());
            throw new Exception(MessageFormat.format("Command <{0}> failed", command), e);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new Exception(MessageFormat.format("Command <{0}> failed", command), e);
        }

    }

    public CommandResult runCommand( String cmd, Object... params ) throws Exception {
        return runCommand(MessageFormat.format(cmd, params));
    }
}
