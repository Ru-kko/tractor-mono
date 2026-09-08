package com.tractor.monolith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.tractor.catalog.infrastructure.api.dto.CatalogFilterDto;
import com.tractor.catalog.infrastructure.api.dto.SearchCatalogRequest;
import com.tractor.catalog.infrastructure.api.dto.SearchCatalogResponse;
import com.tractor.common.event.TractorAddedEvent;
import com.tractor.common.event.TractorBackInStockEvent;
import com.tractor.common.event.TractorOutOfStockEvent;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;

/**
 * Cross-module test: a real {@code TractorAddedEvent} produced onto the embedded
 * {@code inventory.tractor-added} topic must be consumed by {@code catalog}'s
 * {@code TractorAddedEventListener}, making the tractor searchable via
 * {@code POST /catalog/search}. A subsequent {@code TractorOutOfStockEvent} on
 * {@code inventory.tractor-out-of-stock} must remove it from search results, and a
 * {@code TractorBackInStockEvent} on {@code inventory.tractor-back-in-stock} must bring it back,
 * all through the real Kafka pipeline (no in-process shortcut).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@EmbeddedKafka(
    partitions = 1,
    topics = {"inventory.tractor-added", "inventory.tractor-out-of-stock", "inventory.tractor-back-in-stock"})
@TestPropertySource(properties = {
    "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "spring.autoconfigure.exclude="
        + "org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration"})
class CatalogEventsIntegrationTest {

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private KafkaTemplate<String, Object> kafkaTemplate;

  private List<UUID> searchByBrand(String brand) {
    SearchCatalogRequest request = new SearchCatalogRequest(
        List.of(new CatalogFilterDto("BRAND", "EQUALS", brand, null, null)), null, null, null, null);

    ResponseEntity<SearchCatalogResponse> response = restTemplate.postForEntity(
        "http://localhost:" + port + "/catalog/search", request, SearchCatalogResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    return response.getBody().items().stream().map(item -> item.tractorId()).toList();
  }

  /**
   * Cover: R1, R2, R3, R4, R5
   */
  @Test
  void catalogReflectsTractorAddedOutOfStockAndBackInStockEvents() {
    UUID tractorId = UUID.randomUUID();
    String brand = "CrossModuleBrand-" + tractorId;

    TractorAddedEvent addedEvent = TractorAddedEvent.builder()
        .tractorId(tractorId)
        .stock(5)
        .price(new BigDecimal("20000.00"))
        .description("Cross-module test tractor")
        .imageUrl("https://example.com/t.png")
        .category("utility")
        .brand(brand)
        .model("X200")
        .year(2024)
        .color("red")
        .weight(new BigDecimal("2000.5"))
        .horsepower(90)
        .build();
    kafkaTemplate.send("inventory.tractor-added", tractorId.toString(), addedEvent);

    await().atMost(Duration.ofSeconds(15))
        .untilAsserted(() -> assertThat(searchByBrand(brand)).containsExactly(tractorId));

    TractorOutOfStockEvent outOfStockEvent = TractorOutOfStockEvent.builder().tractorId(tractorId).build();
    kafkaTemplate.send("inventory.tractor-out-of-stock", tractorId.toString(), outOfStockEvent);

    await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> assertThat(searchByBrand(brand)).isEmpty());

    TractorBackInStockEvent backInStockEvent = TractorBackInStockEvent.builder().tractorId(tractorId).build();
    kafkaTemplate.send("inventory.tractor-back-in-stock", tractorId.toString(), backInStockEvent);

    await().atMost(Duration.ofSeconds(15))
        .untilAsserted(() -> assertThat(searchByBrand(brand)).containsExactly(tractorId));
  }
}
