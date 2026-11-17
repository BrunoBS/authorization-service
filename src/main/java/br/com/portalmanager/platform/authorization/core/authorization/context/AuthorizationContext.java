package br.com.portalmanager.platform.authorization.core.authorization.context;


import br.com.portalmanager.platform.authorization.core.authorization.AuthorizationPolicy;

public record AuthorizationContext(
        UserSession session,
        AuthorizationPolicy requiredPolicy
) {
}