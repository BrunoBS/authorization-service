package br.com.portalmanager.platform.authorization.core.authentication;

import br.com.portalmanager.platform.authorization.core.authentication.UserSession;

public interface TokenValidator {
    UserSession validate(String token);
}
