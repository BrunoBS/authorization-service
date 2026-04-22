package br.com.brunobs.auth.web;

import br.com.brunobs.auth.config.messages.GlobalMessages;
import br.com.brunobs.auth.context.AuthorizationContext;
import br.com.brunobs.auth.context.UserContext;
import br.com.brunobs.auth.context.UserSession;
import br.com.brunobs.auth.exception.ValidationException;
import br.com.brunobs.auth.exception.validation.ValidationResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/authorize")
public class AuthorizationController {

    private final GlobalMessages globalMessages;

    public AuthorizationController(GlobalMessages globalMessages) {
        this.globalMessages = globalMessages;
    }


    @PostMapping
    public ResponseEntity<UserSession> authorize(HttpServletRequest request) {

        AuthorizationContext authorizationContext = UserContext.get();

        if (authorizationContext == null || authorizationContext.session() == null) {
            throw new ValidationException(
                    new ValidationResult("authentication", globalMessages.userSessionNotFound())
            );
        }

        return ResponseEntity.ok(authorizationContext.session());
    }

}