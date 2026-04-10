package com.onnick.reservationcamps.api;

import com.onnick.reservationcamps.domain.UserRole;
import com.onnick.reservationcamps.service.Actor;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public class ActorResolver {
    public Actor resolve(HttpHeaders headers) {
        var roleHeader = headers.getFirst("X-Actor-Role");
        if (roleHeader == null || roleHeader.isBlank()) {
            return new Actor(null, null);
        }

        UserRole role;
        try {
            role = UserRole.valueOf(roleHeader.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return new Actor(null, null);
        }

        var idHeader = headers.getFirst("X-Actor-Id");
        UUID actorId = null;
        if (idHeader != null && !idHeader.isBlank()) {
            try {
                actorId = UUID.fromString(idHeader.trim());
            } catch (IllegalArgumentException ignored) {
                actorId = null;
            }
        }
        return new Actor(actorId, role);
    }
}

