package br.com.portalmanager.platform.authorization.feature.authentication;

import br.com.portalmanager.platform.authorization.core.context.UserSession;
import br.com.portalmanager.platform.library.redis.api.store.RedisStore;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Component
public class TokenSessionStore {
    private static final String PREFIX = "authorization:session:";
    private final RedisStore redis;

    public TokenSessionStore(RedisStore redis) {
        this.redis = redis;
    }

    public Optional<UserSession> find(String tokenHash) {
        return redis.get(PREFIX + tokenHash, UserSession.class);
    }

    public UserSession save(String tokenHash, UserSession session) {
        Duration ttl = Duration.between(Instant.now(), Instant.ofEpochMilli(session.getExpirationTime()));
        if (ttl.isNegative() || ttl.isZero()) {
            throw new IllegalArgumentException("Token is expired");
        }
        redis.set(PREFIX + tokenHash, session, ttl);
        return session;
    }
}
