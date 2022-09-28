package de.dwienzek.tsuseronlinenotify.component.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@lombok.Value
public class OnlineNotifyConfiguration {

    List<String> users;

    public OnlineNotifyConfiguration(@Value("${onlineNotify.users}") List<String> users) {
        this.users = Collections.unmodifiableList(users);
    }

}
