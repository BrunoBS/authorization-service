package br.com.brunobs.auth.model;

public record KafkaPublishRequest(
        String environment,
        String topic,
        String key,
        String message
) {}