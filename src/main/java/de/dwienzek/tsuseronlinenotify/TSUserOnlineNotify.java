package de.dwienzek.tsuseronlinenotify;

import com.github.manevolent.ts3j.command.CommandException;
import com.github.manevolent.ts3j.identity.LocalIdentity;
import com.github.manevolent.ts3j.protocol.socket.client.LocalTeamspeakClientSocket;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import de.dwienzek.configuration.yaml.YamlConfiguration;
import de.dwienzek.configuration.yaml.exception.ConfigurationCreateException;
import de.dwienzek.configuration.yaml.exception.ConfigurationLoadException;
import de.dwienzek.configuration.yaml.exception.ConfigurationSaveException;
import de.dwienzek.console.Console;
import de.dwienzek.console.commands.CommandHandler;
import de.dwienzek.console.commands.CommandRegistry;
import de.dwienzek.console.commands.command.ExitCommand;
import de.dwienzek.console.commands.command.HelpCommand;
import de.dwienzek.tsuseronlinenotify.listener.ClientChangedListener;
import de.dwienzek.tsuseronlinenotify.listener.ClientOnlineListener;
import de.dwienzek.tsuseronlinenotify.util.OnlineUser;
import io.sentry.Sentry;
import lombok.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ParameterizedMessage;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@ToString
@EqualsAndHashCode
public class TSUserOnlineNotify {

    private static final Logger LOGGER = LogManager.getLogger(TSUserOnlineNotify.class);

    @Getter
    @Setter(AccessLevel.PROTECTED)
    private static TSUserOnlineNotify instance;

    @Getter
    private Console console;
    @Getter
    private CommandRegistry commandRegistry;

    @Getter
    private YamlConfiguration configuration;
    @Getter
    private LocalTeamspeakClientSocket teamspeakClient;
    @Getter
    private List<OnlineUser> onlineUsers;
    @Getter
    private TelegramBot telegramBot;

    public static void main(String[] args) {
        new TSUserOnlineNotify();
    }

    public TSUserOnlineNotify() {
        if (instance != null) {
            throw new UnsupportedOperationException("tsUserOnlineNotify is already initialized");
        }
        setInstance(this);

        try {
            /*
             * Initialize Logging
             */

            System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");

            Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> LOGGER.error("", exception));

            String version = getVersion();
            String environment;
            String release;
            if (version.contains("-")) {
                String[] split = version.split("-", 2);
                environment = split[1];
                release = split[0];
            } else {
                environment = "RELEASE";
                release = version;
            }
            Sentry.init(options -> {
                options.setEnableExternalConfiguration(true);
                options.addInAppInclude("de.dwienzek.sitecrawl");
                options.setEnvironment(environment);
                options.setRelease(release);
                options.setDist(version);
                options.addEventProcessor((event, hint) -> {
                    event.setTag("os_arch", System.getProperty("os.arch"));
                    event.setTag("os_name", System.getProperty("os.name"));
                    event.setTag("os_version", System.getProperty("os.version"));
                    event.setTag("java_version", System.getProperty("java.version"));
                    event.setTag("user_name", System.getProperty("user.name"));
                    return event;
                });
            });

            /*
             * Starting Message
             */

            long startMillis = System.currentTimeMillis();
            LOGGER.info("Starting {} version {}...", getName(), getVersion());

            /*
             * Initialize Shutdown Hook
             */

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                /*
                 * Stopping Message
                 */

                LOGGER.info("Stopping {} version {}...", getName(), getVersion());

                /*
                 * Finalize TeamSpeak Client
                 */

                try {
                    teamspeakClient.disconnect();
                } catch (IOException | TimeoutException | ExecutionException | InterruptedException exception) {
                    LOGGER.error("Failed to disconnect from teamspeak server", exception);
                }

                /*
                 * Finalize Console
                 */

                console.stop();

                /*
                 * Finalize Logging
                 */

                LogManager.shutdown();
            }));

            /*
             * Initialize Console and Commands
             */

            console = new Console("root@tsUserOnlineNotify:~# ");
            commandRegistry = new CommandRegistry(console);
            console.addHandler(new CommandHandler(commandRegistry));
            console.start();

            commandRegistry.registerCommand(new ExitCommand());
            commandRegistry.registerCommand(new HelpCommand());

            /*
             * Initialize and/or load Configuration
             */

            try {
                configuration = new YamlConfiguration(new File("config.yml"));
            } catch (ConfigurationCreateException exception) {
                LOGGER.error("Failed to create configuration file", exception);
                System.exit(1);
                return;
            } catch (ConfigurationLoadException exception) {
                LOGGER.error("Failed to load configuration file", exception);
                System.exit(1);
                return;
            }

            configuration.setDefault("identity.securityLevel", 10);
            configuration.setDefault("identity.nickname", "TSUserOnlineNotify");
            configuration.setDefault("server.address.host", "localhost");
            configuration.setDefault("server.address.port", 9987);
            configuration.setDefault("server.password", null);
            configuration.setDefault("server.timeout", 10000L);
            configuration.setDefault("telegram.token", "token");
            configuration.setDefault("telegram.chatId", 0L);
            configuration.setDefault("notifyUsers", Collections.singletonList("TestUser"));

            try {
                configuration.save();
            } catch (ConfigurationSaveException exception) {
                LOGGER.error("Failed to save configuration file", exception);
                System.exit(1);
                return;
            }

            /*
             * Initialize TeamSpeak Client
             */

            teamspeakClient = new LocalTeamspeakClientSocket();

            File identityFile = new File("identity.ini");
            if (identityFile.exists()) {
                LOGGER.info("Loading teamspeak identity...");
                try {
                    teamspeakClient.setIdentity(LocalIdentity.read(identityFile));
                } catch (IOException exception) {
                    LOGGER.error("Failed to load teamspeak identity", exception);
                    System.exit(1);
                    return;
                }
                LOGGER.info("TeamSpeak identity successfully loaded.");
            } else {
                int securityLevel = configuration.getInt("identity.securityLevel");
                LOGGER.info("Generating a new teamspeak identity [securityLevel={}]...", securityLevel);

                LocalIdentity identity;
                try {
                    identity = LocalIdentity.generateNew(securityLevel);
                } catch (GeneralSecurityException exception) {
                    LOGGER.error(new ParameterizedMessage("Failed to generate teamspeak identity [securityLevel={}]", securityLevel), exception);
                    System.exit(1);
                    return;
                }

                LOGGER.info("TeamSpeak identity [securityLevel={}] successfully generated.", identity.getSecurityLevel());
                LOGGER.info("Saving teamspeak identity to file...");

                try {
                    identity.save(identityFile);
                } catch (IOException exception) {
                    LOGGER.error("Failed to save teamspeak identity file", exception);
                    System.exit(1);
                    return;
                }

                LOGGER.info("TeamSpeak identity successfully saved to file.");
            }

            String nickname = configuration.getString("identity.nickname");
            teamspeakClient.setNickname(nickname);
            LOGGER.info("Using nickname {} for teamspeak client user.", nickname);

            String host = configuration.getString("server.address.host");
            int port = configuration.getInt("server.address.port");
            String password = configuration.getString("server.password");
            long timeout = configuration.getLong("server.timeout");

            LOGGER.info("Connecting to teamspeak server [address={}:{}, password={}, timeout={}]...", host, port, password, timeout);

            try {
                teamspeakClient.connect(
                        new InetSocketAddress(
                                InetAddress.getByName(host),
                                port
                        ),
                        password,
                        timeout
                );
            } catch (IOException | TimeoutException exception) {
                LOGGER.error("Failed to connect to teamspeak server", exception);
                System.exit(1);
                return;
            }

            LOGGER.info("Successfully connected to teamspeak server [address={}:{}, password={}, timeout={}].", host, port, password, timeout);
            LOGGER.info("Subscribing to all channels...");

            try {
                teamspeakClient.subscribeAll();
            } catch (IOException | TimeoutException | InterruptedException | CommandException exception) {
                LOGGER.error("Failed to subscribe to all channels", exception);
                System.exit(1);
                return;
            }

            LOGGER.info("Successfully subscribed to all channels.");

            LOGGER.info("Registering listeners...");
            teamspeakClient.addListener(new ClientOnlineListener());
            teamspeakClient.addListener(new ClientChangedListener());
            LOGGER.info("Listeners successfully registered.");

            onlineUsers = new ArrayList<>();

            LOGGER.info("Initializing telegram bot...");

            telegramBot = new TelegramBot(configuration.getString("telegram.token"));
            telegramBot.setUpdatesListener(updates -> UpdatesListener.CONFIRMED_UPDATES_ALL);

            LOGGER.info("Telegram bot successfully initialized.");
            LOGGER.info("{} version {} in {}ms started.", getName(), getVersion(), System.currentTimeMillis() - startMillis);
        } catch (Exception exception) {
            LOGGER.error(new ParameterizedMessage("Failed to start {} version {}:", getName(), getVersion()), exception);
            System.exit(1);
        }
    }

    public String getName() {
        return TSUserOnlineNotify.class.getPackage().getImplementationTitle();
    }

    public String getVersion() {
        return TSUserOnlineNotify.class.getPackage().getImplementationVersion();
    }

    public Logger getLogger() {
        return LOGGER;
    }

}
