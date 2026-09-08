package com.tractor.monolith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.tractor.common.event.OrderLine;
import com.tractor.common.event.OrderPlacedEvent;
import com.tractor.common.event.StockUpdatedEvent;
import com.tractor.inventory.infrastructure.api.dto.AddTractorRequest;
import com.tractor.inventory.infrastructure.api.dto.StockResponse;
import com.tractor.inventory.infrastructure.api.dto.TractorCreatedResponse;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;

/**
 * Cross-module test: a real {@code OrderPlacedEvent} produced onto the embedded
 * {@code order.order-placed} topic must be consumed by {@code inventory}'s
 * {@code OrderPlacedEventListener}, decreasing stock and producing a {@code StockUpdatedEvent}
 * onto {@code inventory.stock-updated}, through the real Kafka pipeline (no in-process shortcut).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@EmbeddedKafka(
    partitions = 1,
    topics = {"order.order-placed", "inventory.stock-updated", "inventory.tractor-added"})
@TestPropertySource(properties = {
    "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "spring.autoconfigure.exclude="
        + "org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration"})
class InventoryOrderPlacedIntegrationTest {

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private KafkaTemplate<String, Object> kafkaTemplate;

  @Autowired
  private EmbeddedKafkaBroker embeddedKafkaBroker;

  private Consumer<String, StockUpdatedEvent> stockUpdatedConsumer;

  @BeforeEach
  void setUp() {
    Map<String, Object> consumerProps =
        KafkaTestUtils.consumerProps(embeddedKafkaBroker, "stock-updated-test-group", true);
    consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class);
    consumerProps.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, "com.tractor.common.event");
    consumerProps.put(JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, StockUpdatedEvent.class.getName());
    consumerProps.put(JacksonJsonDeserializer.USE_TYPE_INFO_HEADERS, false);

    stockUpdatedConsumer = new KafkaConsumer<>(consumerProps);
    embeddedKafkaBroker.consumeFromAnEmbeddedTopic(stockUpdatedConsumer, "inventory.stock-updated");
  }

  @AfterEach
  void tearDown() {
    stockUpdatedConsumer.close();
  }

  private UUID createTractor(int initialStock) {
    AddTractorRequest request = new AddTractorRequest(
        initialStock, new BigDecimal("15000.00"), "Compact utility tractor",
        "https://example.com/t.png", "utility", "Acme", "X100", 2024, "green",
        new BigDecimal("1800.5"), 75);

    ResponseEntity<TractorCreatedResponse> response = restTemplate.postForEntity(
        "http://localhost:" + port + "/inventory/tractors", request, TractorCreatedResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    return response.getBody().tractorId();
  }

  private int currentStock(UUID tractorId) {
    ResponseEntity<StockResponse> response = restTemplate.getForEntity(
        "http://localhost:" + port + "/inventory/stock/" + tractorId, StockResponse.class);
    return response.getBody().stock();
  }

  /**
   * Cover: R16, R17
   */
  @Test
  void orderPlacedEventDecreasesStockAndPublishesStockUpdated() {
    UUID tractorId = createTractor(10);

    OrderPlacedEvent event = new OrderPlacedEvent(UUID.randomUUID(), List.of(new OrderLine(tractorId, 4)));
    kafkaTemplate.send("order.order-placed", tractorId.toString(), event);

    await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> assertThat(currentStock(tractorId)).isEqualTo(6));

    ConsumerRecord<String, StockUpdatedEvent> record =
        KafkaTestUtils.getSingleRecord(stockUpdatedConsumer, "inventory.stock-updated", Duration.ofSeconds(15));

    assertThat(record.value().tractorId()).isEqualTo(tractorId);
    assertThat(record.value().stock()).isEqualTo(6);
  }

  /**
   * Cover: R18
   */
  @Test
  void orderPlacedEventNeverDecreasesStockBelowZero() {
    UUID tractorId = createTractor(2);

    OrderPlacedEvent event = new OrderPlacedEvent(UUID.randomUUID(), List.of(new OrderLine(tractorId, 5)));
    kafkaTemplate.send("order.order-placed", tractorId.toString(), event);

    await().pollDelay(Duration.ofSeconds(5)).atMost(Duration.ofSeconds(20))
        .untilAsserted(() -> assertThat(currentStock(tractorId)).isGreaterThanOrEqualTo(0));

    assertThat(currentStock(tractorId)).isEqualTo(2);
  }
}
