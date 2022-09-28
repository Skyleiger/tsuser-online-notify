package de.dwienzek.tsuseronlinenotify.component.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@lombok.Value
public class TeamSpeakIdentityConfiguration {

    int securityLevel;
    String nickname;

    public TeamSpeakIdentityConfiguration(@Value("${teamspeak.identity.security.level}") int securityLevel,
                                          @Value("${teamspeak.identity.nickname}") String nickname) {
        this.securityLevel = securityLevel;
        this.nickname = nickname;
    }

}
   