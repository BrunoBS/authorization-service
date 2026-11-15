package br.com.portalmanager.platform.authorization.feature.authorization.contract;

public record KafkaPublishRequest(
        String environment,
        String topic,
        String key,
        String message
) {}