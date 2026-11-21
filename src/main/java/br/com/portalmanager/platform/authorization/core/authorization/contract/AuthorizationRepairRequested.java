package br.com.portalmanager.platform.authorization.core.authorization.contract;

import br.com.portalmanager.platform.authorization.core.authorization.AuthorizationPolicy;

public record AuthorizationRepairRequested(
        Long accountId,
        Long environmentId,
        Long applicationId,
        AuthorizationPolicy policy,
        String cacheKey
) {}
