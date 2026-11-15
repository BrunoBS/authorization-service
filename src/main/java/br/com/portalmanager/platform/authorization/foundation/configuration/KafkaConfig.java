package br.com.portalmanager.platform.authorization.foundation.configuration;

import br.com.portalmanager.platform.authorization.feature.authorization.contract.AccountCacheDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.support.converter.JacksonJsonMessageConverter;
import org.springframework.kafka.support.converter.RecordMessageConverter;

@Configuration
public class KafkaConfig {

    @Bean
    public RecordMessageConverter converter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AccountCacheDTO> kafkaListenerContainerFactory(
            ConsumerFactory<String, AccountCacheDTO> consumerFactory,
            RecordMessageConverter converter
    ) {
        ConcurrentKafkaListenerContainerFactory<String, AccountCacheDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setRecordMessageConverter(converter);
        return factory;
    }
}