package br.com.portalmanager.platform.authorization.entrypoint.http;

import br.com.portalmanager.platform.authorization.core.authorization.context.AuthorizationContext;
import br.com.portalmanager.platform.authorization.core.authorization.context.UserContext;
import br.com.portalmanager.platform.authorization.core.authentication.UserSession;
import br.com.portalmanager.platform.library.messaging.api.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/authorize")
public class AuthorizationController {
    @PostMapping
    public ResponseEntity<UserSession> authorize(HttpServletRequest request) {
        AuthorizationContext context = UserContext.get();
        if (context == null || context.session() == null) {
            throw new UnauthorizedException("authentication.invalid-token");
        }
        return ResponseEntity.ok(context.session());
    }
}
