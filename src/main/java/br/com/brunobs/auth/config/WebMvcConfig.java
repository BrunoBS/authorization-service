package br.com.brunobs.auth.config;


import br.com.brunobs.auth.security.interceptor.AuthorizationInterceptor;
import br.com.brunobs.auth.service.AuthorizationService;
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