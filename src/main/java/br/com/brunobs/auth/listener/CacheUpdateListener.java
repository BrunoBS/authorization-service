package br.com.brunobs.auth.listener;


import br.com.brunobs.auth.model.AccountCacheDTO;
import br.com.brunobs.auth.service.AuthorizationCacheService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class CacheUpdateListener {

    private final AuthorizationCacheService cacheService;

    public CacheUpdateListener(AuthorizationCacheService cacheService) {
        this.cacheService = cacheService;
    }

    @KafkaListener(
            topics = "account-permissions-cache",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onCacheUpdate(AccountCacheDTO dto) {
        System.out.println(dto.toString());
        cacheService.updateLocalCache(dto);
    }
}