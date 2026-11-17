package br.com.portalmanager.platform.authorization.core.authentication;

import br.com.portalmanager.platform.authorization.core.authentication.UserSession;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

/** Current test-token implementation. Replace its key source before production token rollout. */
@Component
public class TestJwtTokenValidator implements TokenValidator {
    @Override
    public UserSession validate(String token) {
        String secretString = "12345678901234567890123456789012";
        SecretKey key = Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        Object tokenGroups = claims.get("groups");
        if (!(tokenGroups instanceof List<?> values)) {
            throw new IllegalArgumentException("Token groups claim is invalid");
        }
        Set<String> groups = values.stream().filter(String.class::isInstance).map(String.class::cast)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        if (claims.getExpiration() == null) {
            throw new IllegalArgumentException("Token expiration claim is required");
        }
        return new UserSession(claims.getExpiration().getTime(), claims.get("username", String.class),
                claims.get("email", String.class), groups);
    }
}
