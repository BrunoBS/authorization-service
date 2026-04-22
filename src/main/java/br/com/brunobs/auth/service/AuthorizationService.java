package br.com.brunobs.auth.service;


import br.com.brunobs.auth.config.messages.GlobalMessages;
import br.com.brunobs.auth.context.ParsedGroup;
import br.com.brunobs.auth.context.UserSession;
import br.com.brunobs.auth.exception.AccessDeniedException;
import br.com.brunobs.auth.exception.validation.ValidationResult;
import br.com.brunobs.auth.security.AuthorizationPolicy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Set;

@Service
public class AuthorizationService {

    private final AuthorizationCacheService authCache;
    private final GlobalMessages globalMessages;


    public AuthorizationService(AuthorizationCacheService authCache, GlobalMessages globalMessages) {
        this.authCache = authCache;
        this.globalMessages = globalMessages;
    }

    public void checkAuthorization(UserSession session, AuthorizationPolicy policy) {

        if (session == null) {
            throw new AccessDeniedException(
                    new ValidationResult("authentication", globalMessages.userSessionNotFound()));
        }

        if (AuthorizationPolicy.OPEN.equals(policy)) {
            return;
        }

        if (session.isOwner()) {
            return;
        }

        if (AuthorizationPolicy.OWNER.equals(policy) && !session.isOwner()) {
            throw new AccessDeniedException(new ValidationResult("authentication", globalMessages.userNotOwner()));
        }

        String cacheKey = buildCacheKey(session, policy);
        String group = authCache.getAuthorizationGroup(
                session.getAccountId(),
                policy.name(),
                String.valueOf(session.getAccountId()),
                cacheKey
        );

        if (!StringUtils.hasText(group)) {
            throw new AccessDeniedException(new ValidationResult("authentication", globalMessages.userGroupsNotFound()));
        }
        if (!hasAccess(session.getAuthorizerGroups(), group)) {
            throw new AccessDeniedException(
                    new ValidationResult("authentication", globalMessages.userGroupMissing()));
        }
    }

    private String buildCacheKey(UserSession session, AuthorizationPolicy policy) {
        return String.format(
                "AUTH::%s::%s::%s",
                session.getAccountId(),
                session.getEnvironmentId() != null ? session.getEnvironmentId() : policy.name(),
                session.getApplicationId() != null ? session.getApplicationId()
                        : "NONE"
        );
    }

    public boolean hasAccess(Set<ParsedGroup> userGroups, String requiredGroup) {

        AuthorizationPolicy required = AuthorizationPolicy.resolveEnvironment(requiredGroup);
        String suffixRequired = extractSuffix(requiredGroup);

        return userGroups.stream().anyMatch(group -> {

            String suffixUser = extractSuffix(group.fullGroup());

            if (!suffixRequired.equals(suffixUser)) {
                return false;
            }

            AuthorizationPolicy userLevel = AuthorizationPolicy.resolveEnvironment(group.fullGroup());
            return userLevel.getLevel() >= required.getLevel();
        });
    }

    private String extractSuffix(String groupName) {
        if (groupName == null || !groupName.contains("_")) {
            return "";
        }
        return groupName.substring(groupName.indexOf("_"));
    }
}