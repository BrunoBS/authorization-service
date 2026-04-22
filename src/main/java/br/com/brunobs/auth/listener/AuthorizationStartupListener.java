package br.com.brunobs.auth.listener;


import br.com.brunobs.auth.security.AuthorizationPolicy;
import br.com.brunobs.auth.service.AuthorizationCacheService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationStartupListener {

    private final AuthorizationCacheService cacheService;

    public AuthorizationStartupListener(AuthorizationCacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Scheduled(initialDelay = 0, fixedRate = 3600000)
    public void onApplicationReady() {
        System.out.println( " Aplicação iniciou - carregando cache de autorização...");
        cacheService.getAuthorizationGroup(null, AuthorizationPolicy.DEV.name(), "BOOTSTRAP", "BOOTSTRAP");
        System.out.println(" Solicitado o reflesh do cache de autorização com sucesso");
    }
}