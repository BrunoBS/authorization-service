package br.com.brunobs.auth.web.headers;

import br.com.brunobs.auth.security.AuthorizationPolicy;
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