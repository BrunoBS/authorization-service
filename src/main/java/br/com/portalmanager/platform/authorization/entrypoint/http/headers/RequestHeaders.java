package br.com.portalmanager.platform.authorization.entrypoint.http.headers;

import br.com.portalmanager.platform.authorization.core.authorization.AuthorizationPolicy;
import org.springframework.http.HttpMethod;
import java.util.UUID;

public record RequestHeaders(

        String correlationId,
        String authorization,
        UUID account,
        UUID environment,
        UUID application,
        HttpMethod method,
        AuthorizationPolicy policy

) {
}