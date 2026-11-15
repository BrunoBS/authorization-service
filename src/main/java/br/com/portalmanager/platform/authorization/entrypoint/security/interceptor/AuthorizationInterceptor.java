package br.com.portalmanager.platform.authorization.entrypoint.security.interceptor;


import br.com.portalmanager.platform.authorization.core.context.AuthorizationContext;
import br.com.portalmanager.platform.authorization.core.context.UserContext;
import br.com.portalmanager.platform.authorization.core.authorization.AuthorizationPolicy;
import br.com.portalmanager.platform.authorization.core.authorization.AuthorizationRequired;
import br.com.portalmanager.platform.authorization.feature.authorization.application.AuthorizationService;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthorizationInterceptor implements HandlerInterceptor {


    private final AuthorizationService authorizationService;


    public AuthorizationInterceptor(
            AuthorizationService authorizationService
    ) {
        this.authorizationService = authorizationService;
    }

    @Override
    public boolean preHandle(
            @Nonnull HttpServletRequest request,
            @Nonnull HttpServletResponse response,
            @Nonnull Object handler
    ) throws Exception {

        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        AuthorizationContext authorizationContext = UserContext.get();
        AuthorizationRequired annotation = handlerMethod.getMethodAnnotation(AuthorizationRequired.class);

        if (annotation == null) {
            annotation = handlerMethod.getBeanType().getAnnotation(AuthorizationRequired.class);
        }

        AuthorizationPolicy policy = annotation != null ? annotation.policy() : authorizationContext.requiredPolicy();
        authorizationService.checkAuthorization(authorizationContext.session(), policy
        );

        return true;
    }
}