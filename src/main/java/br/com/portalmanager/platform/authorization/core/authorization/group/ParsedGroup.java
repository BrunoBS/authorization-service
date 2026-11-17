package br.com.portalmanager.platform.authorization.core.authorization.group;

public record ParsedGroup(
        String fullGroup,
        String profile,
        String environment,
        String authorizer
) {}