package br.com.portalmanager.platform.authorization.foundation.configuration;


import br.com.portalmanager.platform.authorization.entrypoint.security.interceptor.AuthorizationInterceptor;
import br.com.portalmanager.platform.authorization.feature.authorization.application.AuthorizationService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthorizationService authorizationService;


    public WebMvcConfig(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;

    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(new AuthorizationInterceptor(authorizationService))
                .addPathPatterns("/**");
    }
}