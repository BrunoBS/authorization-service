package br.com.brunobs.auth.security.filter;

import br.com.brunobs.auth.context.AuthorizationContext;
import br.com.brunobs.auth.context.UserContext;
import br.com.brunobs.auth.context.UserSession;
import br.com.brunobs.auth.util.TokenHashUtil;
import br.com.brunobs.auth.web.headers.RequestHeaders;
import br.com.brunobs.auth.web.headers.RequestHeadersFactory;
import com.github.benmanes.caffeine.cache.Cache;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;

@Component
public class AuthorizationFilter extends OncePerRequestFilter {

    private final Cache<String, UserSession> tokenCache;
    private final Validator validator;

    public AuthorizationFilter(Cache<String, UserSession> tokenCache, Validator validator) {
        this.tokenCache = tokenCache;
        this.validator = validator;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            RequestHeaders headers = RequestHeadersFactory.from(request);
            Set<ConstraintViolation<RequestHeaders>> violations = validator.validate(headers);

            if (!violations.isEmpty()) {
                unauthorized(response, "Invalid headers", formatViolations(violations));
                return;
            }


            String token = headers.authorization().substring(7);
            String tokenHash = TokenHashUtil.sha256(token);


            UserSession session = tokenCache.get(tokenHash, k -> parseToken(token));


            session.setAccountId(headers.account());
            session.setEnvironmentId(headers.environment());
            session.setApplicationId(headers.application());

            AuthorizationContext authorizationContext = new AuthorizationContext(
                    session,
                    headers.policy()
            );
            UserContext.set(authorizationContext);
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            unauthorized(response, "Unauthorized", e.getMessage());
        } finally {
            UserContext.clear();
        }
    }

    private UserSession parseToken(String token) {
// A chave precisa ter pelo menos 32 caracteres para o HS256
        String secretString = "12345678901234567890123456789012";
        SecretKey key = Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));

        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token).getPayload();


        return new UserSession(
                claims.getExpiration().getTime(),
                claims.get("username", String.class),
                claims.get("email", String.class),
                Set.copyOf((List<String>) claims.get("groups"))
        );
    }


    private String formatViolations(Set<ConstraintViolation<RequestHeaders>> violations) {
        return violations.stream()
                .map(v -> v.getPropertyPath() + " " + v.getMessage())
                .reduce((a, b) -> a + ", " + b)
                .orElse("Invalid headers");
    }

    private void unauthorized(HttpServletResponse response, String message, String details)
            throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write("""
                {
                  "code": "UNAUTHORIZED",
                  "message": "%s",
                  "details": "%s"
                }
                """.formatted(message, details));
    }
}