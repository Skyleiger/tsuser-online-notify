package de.dwienzek.tsuseronlinenotify.repository;

import de.dwienzek.tsuseronlinenotify.dto.OnlineTS3User;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
@ToString
@EqualsAndHashCode
public class OnlineTS3UserRepository {

    private final Map<Integer, OnlineTS3User> onlineTS3Users = new HashMap<>();

    public OnlineTS3User getOnlineTS3User(int clientId) {
        return onlineTS3Users.get(clientId);
    }

    public void addOnlineTS3User(OnlineTS3User user) {
        if (!onlineTS3Users.containsValue(user)) {
            onlineTS3Users.put(user.getClientId(), user);
        }
    }

    public void removeOnlineTS3User(OnlineTS3User user) {
        onlineTS3Users.remove(user.getClientId());
    }

}
