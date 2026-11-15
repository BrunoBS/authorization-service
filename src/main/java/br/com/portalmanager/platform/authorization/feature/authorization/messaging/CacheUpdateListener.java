package br.com.portalmanager.platform.authorization.feature.authorization.messaging;


import br.com.portalmanager.platform.authorization.feature.authorization.contract.AccountCacheDTO;
import br.com.portalmanager.platform.authorization.feature.authorization.application.AuthorizationCacheService;
import org.springframework.kafka.annotation.KafkaListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CacheUpdateListener {
    private static final Logger log = LoggerFactory.getLogger(CacheUpdateListener.class);

    private final AuthorizationCacheService cacheService;

    public CacheUpdateListener(AuthorizationCacheService cacheService) {
        this.cacheService = cacheService;
    }

    @KafkaListener(
            topics = "account-permissions-cache",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onCacheUpdate(AccountCacheDTO dto) {
        log.debug("Received authorization cache refresh with {} entries",
                dto.groups() == null ? 0 : dto.groups().size());
        cacheService.updateCache(dto);
    }
}