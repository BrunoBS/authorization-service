package br.com.brunobs.auth.context;


import br.com.brunobs.auth.security.AuthorizationPolicy;

public record AuthorizationContext(
        UserSession session,
        AuthorizationPolicy requiredPolicy
) {
}