package br.com.portalmanager.platform.authorization.core.authorization.contract;

import br.com.portalmanager.platform.authorization.core.authorization.AuthorizationPolicy;
import java.util.UUID;

public record AuthorizationRepairRequested(
        UUID accountId,
        UUID environmentId,
        UUID applicationId,
        AuthorizationPolicy policy,
        String cacheKey
) {}
