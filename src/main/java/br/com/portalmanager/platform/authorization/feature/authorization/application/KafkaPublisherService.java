package br.com.portalmanager.platform.authorization.feature.authorization.application;

import br.com.portalmanager.platform.authorization.feature.authorization.contract.KafkaPublishRequest;
import br.com.portalmanager.platform.library.messaging.api.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Service
public class KafkaPublisherService {
    private static final Logger log = LoggerFactory.getLogger(KafkaPublisherService.class);
    private final RestClient restClient;
    private final RetryTemplate retryTemplate;
    private final String url;

    public KafkaPublisherService(RestClient.Builder builder,
                                 @Value("${authorization.refresh.url:http://localhost:6060/kafka/publish}") String url) {
        this.restClient = builder.build();
        this.url = url;
        this.retryTemplate = new RetryTemplate(RetryPolicy.builder()
                .includes(HttpServerErrorException.class, ResourceAccessException.class)
                .maxRetries(2)
                .delay(Duration.ofSeconds(2))
                .multiplier(2.0)
                .build());
    }

    public void publish(KafkaPublishRequest request) {
        try {
            retryTemplate.invoke(() -> restClient.post().uri(url).body(request).retrieve().toBodilessEntity());
            log.debug("Authorization refresh request published");
        } catch (HttpServerErrorException | ResourceAccessException exception) {
            log.error("Authorization refresh request could not be published after retries", exception);
            throw new ApiException("authorization.dependency-unavailable", exception);
        }
    }
}
