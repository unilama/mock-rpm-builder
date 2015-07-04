package org.jenkinsci.plugins.rpmmock.cmdrunner;

import hudson.EnvVars;
import hudson.Launcher;
import hudson.Proc;
import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;

import java.io.IOException;
import java.text.MessageFormat;

public class CommandRunner {
    private final TaskListener listener;
    private final EnvVars envVars;
    private final Launcher launcher;

    public CommandRunner( Launcher launcher, TaskListener listener, EnvVars envVars ){
        this.launcher = launcher;
        this.listener = listener;
        this.envVars = envVars;
    }

    public CommandRunner( Launcher launcher, TaskListener listener ){
        this(launcher, listener, (EnvVars) EnvVars.masterEnvVars);
    }

    public int runCommand( String command ) throws Exception {
        try {
            Proc proc = launcher.launch().envs(envVars).cmdAsSingleString(command).stdout(listener).start();
            return proc.join();
        } catch (IOException e) {
            e.printStackTrace(listener.getLogger());
            throw new Exception(MessageFormat.format("Command <{0}> failed", command), e);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new Exception(MessageFormat.format("Command <{0}> failed", command), e);
        }

    }

    public int runCommand(ArgumentListBuilder argumentListBuilder) throws Exception {
        try {
            Proc proc = launcher.launch().envs(envVars).cmds(argumentListBuilder).stdout(listener).start();
            return proc.join();
        } catch (IOException e) {
            e.printStackTrace(listener.getLogger());
            throw new Exception(MessageFormat.format("Command <{0}> failed", argumentListBuilder.toString()), e);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new Exception(MessageFormat.format("Command <{0}> failed", argumentListBuilder.toString()), e);
        }
    }

    public int runCommand( String cmd, Object... params ) throws Exception {
        return runCommand(MessageFormat.format(cmd, params));
    }

    public int runCommand( RunnerInterface runner ) throws Exception {
        return runCommand( runner.toArgumentListBuilder());
    }

    public static boolean isError(int exitCode){
        return exitCode != 0;
    }
}
