package br.com.brunobs.auth.web.headers;


import br.com.brunobs.auth.security.AuthorizationPolicy;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;

public class RequestHeadersFactory {

    public static RequestHeaders from(HttpServletRequest request) {


        return new RequestHeaders(
                request.getHeader("X-Correlation-Id"),
                request.getHeader("Authorization"),
                parseLong(request.getHeader("X-Account-Id")),
                parseLong(request.getHeader("X-Environment")),
                parseLong(request.getHeader("X-Application-Id")),
                parseHttpMethod(request.getMethod()),
                parsePolicy(request.getHeader("X-Policy"))
        );
    }

    private static Long parseLong(String value) {
        try {
            return value != null ? Long.valueOf(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
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