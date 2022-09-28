package de.dwienzek.tsuseronlinenotify.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
public class OnlineTS3User {

    private final int clientId;
    private final String clientUniqueId;
    @EqualsAndHashCode.Exclude
    private String nickname;

}
