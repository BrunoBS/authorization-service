package br.com.portalmanager.platform.authorization.core.authorization.application;

import br.com.portalmanager.platform.authorization.core.authorization.group.ParsedGroup;
import br.com.portalmanager.platform.authorization.core.authentication.UserSession;
import br.com.portalmanager.platform.authorization.core.authorization.contract.AuthorizationRequirementFallback;
import br.com.portalmanager.platform.authorization.core.authorization.AuthorizationPolicy;
import org.springframework.beans.factory.ObjectProvider;
import br.com.portalmanager.platform.library.messaging.api.exception.ForbiddenException;
import br.com.portalmanager.platform.library.messaging.api.exception.UnauthorizedException;
import br.com.portalmanager.platform.library.messaging.api.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Set;

@Service
public class AuthorizationService {
    private final AuthorizationCacheService authCache;
    private final ObjectProvider<AuthorizationRequirementFallback> fallbackProvider;
    private final AuthorizationRepairPublisher repairPublisher;

    public AuthorizationService(AuthorizationCacheService authCache,
                                 ObjectProvider<AuthorizationRequirementFallback> fallbackProvider,
                                 AuthorizationRepairPublisher repairPublisher) {
        this.authCache = authCache;
        this.fallbackProvider = fallbackProvider;
        this.repairPublisher = repairPublisher;
    }

    public void checkAuthorization(UserSession session, AuthorizationPolicy policy) {
        if (session == null) throw new UnauthorizedException("authentication.invalid-token");
        if (AuthorizationPolicy.OPEN.equals(policy) || session.isOwner()) return;
        if (AuthorizationPolicy.OWNER.equals(policy)) throw new ForbiddenException("authorization.access-denied");

        if (policy == null) throw new ApiException("authorization.policy-required");
        String cacheKey = buildCacheKey(session, policy);
        String group = null;
        RuntimeException cacheFailure = null;
        try {
            group = authCache.getAuthorizationGroup(cacheKey);
        } catch (RuntimeException exception) {
            cacheFailure = exception;
        }
        if (!StringUtils.hasText(group)) {
            AuthorizationRequirementFallback fallback = fallbackProvider.getIfAvailable();
            if (fallback == null) {
                repairPublisher.request(session, policy, cacheKey);
                throw new ApiException("authorization.dependency-unavailable", cacheFailure);
            }
            try {
                group = fallback.findRequiredGroup(cacheKey).orElse(null);
            } catch (RuntimeException exception) {
                repairPublisher.request(session, policy, cacheKey);
                throw new ApiException("authorization.dependency-unavailable", exception);
            }
            if (!StringUtils.hasText(group)) {
                repairPublisher.request(session, policy, cacheKey);
                throw new ForbiddenException("authorization.access-denied");
            }
            try {
                authCache.cacheFallbackValue(cacheKey, group);
            } catch (RuntimeException ignored) {
                // The view answered, so use its value for this request even if Redis remains unavailable.
            }
        }
        if (!hasAccess(session.getAuthorizerGroups(), group)) {
            throw new ForbiddenException("authorization.access-denied");
        }
    }

    private String buildCacheKey(UserSession session, AuthorizationPolicy policy) {
        return String.format("AUTH::%s::%s::%s", session.getAccountId(),
                session.getEnvironmentId() != null ? session.getEnvironmentId() : policy.name(),
                session.getApplicationId() != null ? session.getApplicationId() : "NONE");
    }

    public boolean hasAccess(Set<ParsedGroup> userGroups, String requiredGroup) {
        AuthorizationPolicy required = AuthorizationPolicy.resolveEnvironment(requiredGroup);
        String suffixRequired = extractSuffix(requiredGroup);
        return userGroups.stream().anyMatch(group -> {
            String suffixUser = extractSuffix(group.fullGroup());
            if (!suffixRequired.equals(suffixUser)) return false;
            AuthorizationPolicy userLevel = AuthorizationPolicy.resolveEnvironment(group.fullGroup());
            return userLevel.getLevel() >= required.getLevel();
        });
    }

    private String extractSuffix(String groupName) {
        if (groupName == null || !groupName.contains("_")) return "";
        return groupName.substring(groupName.indexOf("_"));
    }
}
