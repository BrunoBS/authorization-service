package br.com.brunobs.auth.config;

import br.com.brunobs.auth.model.AccountCacheDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.support.converter.JsonMessageConverter;
import org.springframework.kafka.support.converter.RecordMessageConverter;

@Configuration
public class KafkaConfig {

    @Bean
    public RecordMessageConverter converter() {
        return new JsonMessageConverter();
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