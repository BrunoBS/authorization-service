package br.com.portalmanager.platform.authorization.core.context;

public record ParsedGroup(
        String fullGroup,
        String profile,
        String environment,
        String authorizer
) {}