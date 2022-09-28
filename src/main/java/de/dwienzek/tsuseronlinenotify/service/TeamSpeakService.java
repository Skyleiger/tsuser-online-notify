package de.dwienzek.tsuseronlinenotify.service;

import com.github.manevolent.ts3j.command.CommandException;
import com.github.manevolent.ts3j.event.TS3Listener;
import com.github.manevolent.ts3j.identity.LocalIdentity;
import com.github.manevolent.ts3j.protocol.socket.client.LocalTeamspeakClientSocket;
import de.dwienzek.tsuseronlinenotify.component.configuration.TeamSpeakIdentityConfiguration;
import de.dwienzek.tsuseronlinenotify.component.configuration.TeamSpeakServerConfiguration;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Service
@AllArgsConstructor
public class TeamSpeakService implements AutoCloseable {

    private static final Logger LOGGER = LogManager.getLogger(TeamSpeakService.class);
    private static final Path IDENTITY_FILE_PATH = Path.of("configuration/identity.ini");

    private final TeamSpeakIdentityConfiguration identityConfiguration;
    private final TeamSpeakServerConfiguration serverConfiguration;
    private final List<TS3Listener> listeners;
    private LocalTeamspeakClientSocket client;

    @Autowired
    public TeamSpeakService(TeamSpeakIdentityConfiguration identityConfiguration, TeamSpeakServerConfiguration serverConfiguration, List<TS3Listener> listeners) {
        this.identityConfiguration = identityConfiguration;
        this.serverConfiguration = serverConfiguration;
        this.listeners = listeners;
        this.client = new LocalTeamspeakClientSocket();
    }

    public void initClient() throws GeneralSecurityException, IOException {
        client.setIdentity(loadOrGenerateIdentity());
        client.setNickname(loadNickname());
    }

    private LocalIdentity loadOrGenerateIdentity() throws IOException, GeneralSecurityException {
        if (Files.exists(IDENTITY_FILE_PATH)) {
            try (InputStream inputStream = Files.newInputStream(IDENTITY_FILE_PATH)) {
                LocalIdentity identity = LocalIdentity.read(inputStream);
                LOGGER.info("TeamSpeak identity from '{}' loaded.", IDENTITY_FILE_PATH);
                return identity;
            }
        } else {
            int securityLevel = identityConfiguration.getSecurityLevel();
            LOGGER.info("Generating a new teamspeak identity with security level '{}'.", securityLevel);

            LocalIdentity identity = LocalIdentity.generateNew(securityLevel);

            LOGGER.debug("TeamSpeak identity generated.");

            try (OutputStream outputStream = Files.newOutputStream(IDENTITY_FILE_PATH)) {
                identity.save(outputStream);
                LOGGER.info("TeamSpeak identity saved to '{}'.", IDENTITY_FILE_PATH);
                return identity;
            }
        }
    }

    private String loadNickname() {
        String nickname = identityConfiguration.getNickname();
        LOGGER.info("Using nickname '{}' for teamspeak client user.", nickname);

        return nickname;
    }

    public void connectToServer() throws IOException, TimeoutException {
        String hostname = serverConfiguration.getHostname();
        int port = serverConfiguration.getPort();
        String password = serverConfiguration.getPassword();
        int timeout = serverConfiguration.getTimeout();


        if (!StringUtils.isEmpty(password)) {
            LOGGER.info("Connecting to teamspeak server '{}:{}' using a password. Timeout is set to '{}'.", hostname, port, timeout);
        } else {
            LOGGER.info("Connecting to teamspeak server '{}:{}' without a password. Timeout is set to '{}'.", hostname, port, timeout);
        }

        InetSocketAddress address = new InetSocketAddress(
                InetAddress.getByName(hostname),
                port
        );

        client.connect(address, password, timeout);
        LOGGER.info("Connection to teamspeak server established.");
    }

    public void disconnectFromServer() throws IOException, ExecutionException, InterruptedException, TimeoutException {
        client.disconnect();
    }

    public void registerListeners() throws IOException, CommandException, InterruptedException, TimeoutException {
        client.subscribeAll();
        listeners.forEach(client::addListener);

        LOGGER.info("TeamSpeak listeners registered.");
    }

    public void unRegisterListeners() {
        listeners.forEach(client::removeListener);

        LOGGER.info("TeamSpeak listeners unregistered.");
    }

    public String fetchNicknameOfClient(int clientId) throws IOException, CommandException, InterruptedException, TimeoutException {
        return client.getClientInfo(clientId).getNickname();
    }

    @PostConstruct
    void onPostConstruct() throws GeneralSecurityException, IOException, TimeoutException, CommandException, InterruptedException {
        initClient();
        connectToServer();
        registerListeners();
    }

    @Override
    @PreDestroy
    public void close() throws Exception {
        unRegisterListeners();
        disconnectFromServer();
        client.close();
    }

}
