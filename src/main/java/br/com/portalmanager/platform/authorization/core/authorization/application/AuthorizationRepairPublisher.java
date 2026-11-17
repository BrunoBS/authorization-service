package br.com.portalmanager.platform.authorization.core.authorization.application;

import br.com.portalmanager.platform.authorization.core.authorization.contract.AuthorizationRepairRequested;
import br.com.portalmanager.platform.authorization.core.authentication.UserSession;
import br.com.portalmanager.platform.authorization.core.authorization.AuthorizationPolicy;
import br.com.portalmanager.platform.library.messagequeue.api.contract.MessageQueuePublisher;
import br.com.portalmanager.platform.library.redis.api.store.RedisStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Duration;

@Component
public class AuthorizationRepairPublisher {
    private static final Logger log = LoggerFactory.getLogger(AuthorizationRepairPublisher.class);
    private final ObjectProvider<MessageQueuePublisher> publisher;
    private final RedisStore redis;
    private final String destination;
    private final boolean enabled;

    public AuthorizationRepairPublisher(ObjectProvider<MessageQueuePublisher> publisher, RedisStore redis,
                                         @Value("${platform.authorization.repair-destination:}") String destination,
                                         @Value("${platform.authorization.repair-enabled:false}") boolean enabled) {
        this.publisher = publisher;
        this.redis = redis;
        this.destination = destination;
        this.enabled = enabled;
    }

    public void request(UserSession session, AuthorizationPolicy policy, String cacheKey) {
        MessageQueuePublisher queue = publisher.getIfAvailable();
        if (!enabled || queue == null || destination.isBlank()) return;
        try {
            if (!redis.setIfAbsent("authorization:repair:" + cacheKey, true, Duration.ofMinutes(5))) return;
        } catch (RuntimeException exception) {
            log.warn("Could not reserve authorization repair request", exception);
            return;
        }
        try {
            queue.publish(destination, new AuthorizationRepairRequested(
                    session.getAccountId(), session.getEnvironmentId(), session.getApplicationId(), policy, cacheKey),
                    session.getTraceId(), java.util.Map.of());
        } catch (RuntimeException exception) {
            log.error("Could not publish authorization repair request", exception);
        }
    }
}
