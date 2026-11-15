package br.com.portalmanager.platform.authorization.feature.authentication;

import br.com.portalmanager.platform.authorization.core.context.UserSession;

public interface TokenValidator {
    UserSession validate(String token);
}
