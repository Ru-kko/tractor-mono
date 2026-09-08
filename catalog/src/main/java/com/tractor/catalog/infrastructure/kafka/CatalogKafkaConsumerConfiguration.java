package com.tractor.catalog.infrastructure.kafka;

import com.tractor.common.event.TractorAddedEvent;
import com.tractor.common.event.TractorBackInStockEvent;
import com.tractor.common.event.TractorOutOfStockEvent;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@Configuration
@RequiredArgsConstructor
public class CatalogKafkaConsumerConfiguration {

  private static final String TRUSTED_PACKAGE = "com.tractor.common.event";
  private static final String GROUP_ID = "catalog";

  private final KafkaProperties kafkaProperties;

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, TractorAddedEvent> tractorAddedListenerContainerFactory() {
    return listenerContainerFactory(consumerFactory(TractorAddedEvent.class));
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, TractorOutOfStockEvent>
      tractorOutOfStockListenerContainerFactory() {
    return listenerContainerFactory(consumerFactory(TractorOutOfStockEvent.class));
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, TractorBackInStockEvent>
      tractorBackInStockListenerContainerFactory() {
    return listenerContainerFactory(consumerFactory(TractorBackInStockEvent.class));
  }

  private <T> ConsumerFactory<String, T> consumerFactory(Class<T> eventType) {
    Map<String, Object> configs = new HashMap<>();
    configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
    configs.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
    configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

    JsonDeserializer<T> valueDeserializer = new JsonDeserializer<>(eventType)
        .trustedPackages(TRUSTED_PACKAGE)
        .ignoreTypeHeaders();

    return new DefaultKafkaConsumerFactory<>(configs, new StringDeserializer(), valueDeserializer);
  }

  private <T> ConcurrentKafkaListenerContainerFactory<String, T> listenerContainerFactory(
      ConsumerFactory<String, T> consumerFactory) {
    ConcurrentKafkaListenerContainerFactory<String, T> factory = new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory);
    return factory;
  }
}
