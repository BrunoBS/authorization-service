package br.com.portalmanager.platform.authorization.core.authorization.usecase.port;

import java.util.Optional;

/** Reads the required group from the Workspace authorization view when Redis has no value. */
public interface AuthorizationRequirementFallback {
    Optional<String> findRequiredGroup(String cacheKey);
}
