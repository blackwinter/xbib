package org.xbib.io.sftp;

import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.shell.ProcessShellFactory;
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MockSshSftpServer {

    private static final int PORT = 22999;

    private SshServer sshd;

    public MockSshSftpServer() {
        sshd = SshServer.setUpDefaultServer();
        sshd.setPort(PORT);
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(new File("/src/test/resources/Sftp/hostkey.ser")));
        sshd.setPasswordAuthenticator((username, password, session) -> true);
        String TIME_OUT = String.valueOf(5000.0);
        sshd.getProperties().put(SshServer.IDLE_TIMEOUT, TIME_OUT);
        CommandFactory myCommandFactory = command -> {
            // to execute commands as processes on the server side
            // https://mina.apache.org/sshd-project/tips.html
            //LOGGER.info("Command to execute received: " + command);
            return new ProcessShellFactory(command.split(" ")).create();
        };
        sshd.setCommandFactory(myCommandFactory);
        //sshd.setShellFactory(new ProcessShellFactory());
        List<NamedFactory<Command>> namedFactoryList = new ArrayList<>();
        namedFactoryList.add(new SftpSubsystemFactory());
        sshd.setSubsystemFactories(namedFactoryList);
    }

    public void start() {
        try {
            sshd.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            sshd.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
        sshd = null;
    }
}