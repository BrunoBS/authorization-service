package br.com.portalmanager.platform.authorization.entrypoint.http.headers;

import br.com.portalmanager.platform.authorization.core.authorization.AuthorizationPolicy;
import org.springframework.http.HttpMethod;

public record RequestHeaders(

        String correlationId,
        String authorization,
        Long account,
        Long environment,
        Long application,
        HttpMethod method,
        AuthorizationPolicy policy

) {
}