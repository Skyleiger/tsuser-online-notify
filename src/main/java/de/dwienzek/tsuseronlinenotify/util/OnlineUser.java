package de.dwienzek.tsuseronlinenotify.util;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OnlineUser {

    private String clientUniqueId;
    private int clientId;
    private String nickname;

}
