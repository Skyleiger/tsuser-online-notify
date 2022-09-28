package de.dwienzek.tsuseronlinenotify.component.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@lombok.Value
public class TeamSpeakServerConfiguration {

    String hostname;
    int port;
    String password;
    int timeout;

    public TeamSpeakServerConfiguration(@Value("${teamspeak.server.address.hostname}") String hostname,
                                        @Value("${teamspeak.server.address.port}") int port,
                                        @Value("${teamspeak.server.password}") String password,
                                        @Value("${teamspeak.server.timeout}") int timeout) {
        this.hostname = hostname;
        this.port = port;
        this.password = password;
        this.timeout = timeout;
    }

}
