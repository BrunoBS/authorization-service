package br.com.portalmanager.platform.authorization.core.authorization.usecase.operations;

import br.com.portalmanager.platform.library.redis.api.store.RedisStore;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class AuthorizationCacheService {
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);
    private final RedisStore redis;

    public AuthorizationCacheService(RedisStore redis) {
        this.redis = redis.withNamespace("authorization-cache");
    }

    public String getAuthorizationGroup(String cacheKey) {
        return redis.get(cacheKey, String.class).orElse(null);
    }

    public void cacheFallbackValue(String cacheKey, String group) {
        redis.setIfAbsent(cacheKey, group, CACHE_TTL);
    }
}
