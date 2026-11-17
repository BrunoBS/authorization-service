package br.com.portalmanager.platform.authorization.entrypoint.http.security.filter;

import br.com.portalmanager.platform.authorization.core.authorization.context.AuthorizationContext;
import br.com.portalmanager.platform.authorization.core.authorization.context.UserContext;
import br.com.portalmanager.platform.authorization.core.authentication.UserSession;
import br.com.portalmanager.platform.authorization.core.authentication.TokenSessionStore;
import br.com.portalmanager.platform.authorization.core.authentication.TokenValidator;
import br.com.portalmanager.platform.authorization.foundation.security.TokenHashUtil;
import br.com.portalmanager.platform.authorization.entrypoint.http.headers.RequestHeaders;
import br.com.portalmanager.platform.authorization.entrypoint.http.headers.RequestHeadersFactory;
import br.com.portalmanager.platform.library.messaging.api.model.ApiErrorResponse;
import br.com.portalmanager.platform.library.messaging.api.model.ApiMessage;
import br.com.portalmanager.platform.library.messaging.api.resolver.ApiMessageResolver;
import tools.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Locale;

@Component
public class AuthorizationFilter extends OncePerRequestFilter {
    private final TokenSessionStore sessions;
    private final TokenValidator tokenValidator;
    private final ObjectMapper mapper;
    private final ApiMessageResolver messages;

    public AuthorizationFilter(TokenSessionStore sessions, TokenValidator tokenValidator,
                               ObjectMapper mapper, ApiMessageResolver messages) {
        this.sessions = sessions;
        this.tokenValidator = tokenValidator;
        this.mapper = mapper;
        this.messages = messages;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        RequestHeaders headers = RequestHeadersFactory.from(request);
        String authorization = headers.authorization();
        if (authorization == null || !authorization.startsWith("Bearer ") || authorization.length() <= 7) {
            writeError(response, request, "authentication.invalid-token", HttpServletResponse.SC_UNAUTHORIZED,
                    headers.correlationId());
            return;
        }

        String token = authorization.substring(7);
        String tokenHash = TokenHashUtil.sha256(token);
        UserSession session;
        try {
            session = sessions.find(tokenHash).orElse(null);
        } catch (RuntimeException exception) {
            writeError(response, request, "authorization.dependency-unavailable",
                    HttpServletResponse.SC_SERVICE_UNAVAILABLE, headers.correlationId());
            return;
        }
        if (session == null) {
            try {
                session = tokenValidator.validate(token);
            } catch (JwtException | IllegalArgumentException exception) {
                writeError(response, request, "authentication.invalid-token", HttpServletResponse.SC_UNAUTHORIZED,
                        headers.correlationId());
                return;
            }
            try {
                sessions.save(tokenHash, session);
            } catch (RuntimeException exception) {
                writeError(response, request, "authorization.dependency-unavailable",
                        HttpServletResponse.SC_SERVICE_UNAVAILABLE, headers.correlationId());
                return;
            }
        }

        session.setAccountId(headers.account());
        session.setEnvironmentId(headers.environment());
        session.setApplicationId(headers.application());
        session.setTraceId(headers.correlationId());
        UserContext.set(new AuthorizationContext(session, headers.policy()));
        putMdc("account", session.getAccountId());
        putMdc("application", session.getApplicationId());
        putMdc("environment", session.getEnvironmentId());
        putMdc("correlationId", session.getTraceId());
        putMdc("username", session.getUserName());
        putMdc("clientIp", request.getRemoteAddr());
        putMdc("userAgent", request.getHeader("User-Agent"));
        try {
            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
            MDC.remove("account");
            MDC.remove("application");
            MDC.remove("environment");
            MDC.remove("correlationId");
            MDC.remove("username");
            MDC.remove("clientIp");
            MDC.remove("userAgent");
        }
    }

    private void putMdc(String key, Object value) {
        if (value != null) MDC.put(key, value.toString());
    }

    private void writeError(HttpServletResponse response, HttpServletRequest request, String key,
                            int fallbackStatus, String correlationId) throws IOException {
        ApiMessage message = messages.resolve(key, Locale.forLanguageTag("pt-BR"));
        response.setStatus(message == null ? fallbackStatus : message.httpStatus());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        ApiErrorResponse body = message == null
                ? new ApiErrorResponse("AUTH-000", "Falha de autorização.", null, Instant.now(),
                request.getRequestURI(), correlationId)
                : new ApiErrorResponse(message.code(), message.message(), message.solution(), Instant.now(),
                request.getRequestURI(), correlationId);
        mapper.writeValue(response.getWriter(), body);
    }
}
