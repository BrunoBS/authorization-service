package br.com.portalmanager.platform.authorization.entrypoint.http.headers;


import br.com.portalmanager.platform.authorization.core.authorization.AuthorizationPolicy;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.http.HttpMethod;

public class RequestHeadersFactory {

    public static RequestHeaders from(HttpServletRequest request) {


        return new RequestHeaders(
                header(request, "correlationId", "X-Correlation-Id"),
                request.getHeader("Authorization"),
                parseIdentifier(header(request, "workspaceIdentifier", "X-Account-Id")),
                parseIdentifier(header(request, "environmentIdentifier", "X-Environment")),
                parseIdentifier(header(request, "applicationIdentifier", "X-Application-Id")),
                parseHttpMethod(request.getMethod()),
                parsePolicy(header(request, "policy", "X-Policy"))
        );
    }

    private static String header(HttpServletRequest request, String name, String legacyName) {
        String value = request.getHeader(name);
        return value == null ? request.getHeader(legacyName) : value;
    }

    private static UUID parseIdentifier(String value) {
        if (value == null) {
            return null;
        }
        UUID identifier = UUID.fromString(value);
        if (!identifier.toString().equalsIgnoreCase(value)) {
            throw new IllegalArgumentException("Invalid resource identifier");
        }
        return identifier;
    }

    private static HttpMethod parseHttpMethod(String method) {
        try {
            return method != null ? HttpMethod.valueOf(method) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static AuthorizationPolicy parsePolicy(String value) {
        try {
            return value != null ? AuthorizationPolicy.valueOf(value) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}