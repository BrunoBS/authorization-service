package br.com.portalmanager.platform.authorization.feature.authorization.application;

import br.com.portalmanager.platform.authorization.feature.authorization.contract.AccountCacheDTO;
import br.com.portalmanager.platform.authorization.feature.authorization.contract.KafkaPublishRequest;
import br.com.portalmanager.platform.library.redis.api.store.RedisStore;
import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class AuthorizationCacheService {
    private static final Logger log = LoggerFactory.getLogger(AuthorizationCacheService.class);
    private static final String PENDING = "__PENDING__";
    private static final String REFRESH_PREFIX = "authorization:refresh:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(40);
    private static final Duration REFRESH_TTL = Duration.ofSeconds(30);
    private final RedisStore redis;
    private final KafkaPublisherService publisher;
    private final ObjectMapper mapper;

    public AuthorizationCacheService(RedisStore redis, KafkaPublisherService publisher, ObjectMapper mapper) {
        this.redis = redis;
        this.publisher = publisher;
        this.mapper = mapper;
    }

    public String getAuthorizationGroup(Long accountId, String defaultType, String keyKafka, String cacheKey) {
        String value = redis.get(cacheKey, String.class).orElse(null);
        if (value != null && !PENDING.equals(value)) return value;

        requestRefresh(accountId, defaultType, keyKafka, cacheKey);
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                Thread.sleep(150);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting for authorization cache refresh", exception);
            }
            value = redis.get(cacheKey, String.class).orElse(null);
            if (value != null && !PENDING.equals(value)) return value;
        }
        return null;
    }

    private void requestRefresh(Long accountId, String defaultType, String keyKafka, String cacheKey) {
        String guardKey = REFRESH_PREFIX + cacheKey;
        if (!redis.setIfAbsent(guardKey, true, REFRESH_TTL)) return;
        redis.set(cacheKey, PENDING, REFRESH_TTL);
        try {
            String payload = mapper.writeValueAsString(new AccountCacheDTO(accountId, defaultType, null));
            publisher.publish(new KafkaPublishRequest("PROD", "auth-refresh-request", keyKafka, payload));
        } catch (Exception exception) {
            redis.delete(guardKey);
            redis.delete(cacheKey);
            log.warn("Could not publish authorization refresh request; falling back to the authorization view", exception);
        }
    }

    public void cacheFallbackValue(String cacheKey, String group) {
        redis.set(cacheKey, group, CACHE_TTL);
    }

    public void updateCache(AccountCacheDTO dto) {
        if (dto.groups() == null) return;
        dto.groups().forEach(group -> {
            redis.set(group.key(), group.group(), CACHE_TTL);
            redis.delete(REFRESH_PREFIX + group.key());
        });
    }

}
