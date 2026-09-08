# Verification — How to prove the work does what it claims

> Golden rule: **the agent does not say "it works", it proves it**.
> Every feature ends with executable evidence, not with claims.

## Verification levels

### Level 1 — Use case unit tests (mandatory)

Every use case in `<module>/src/main/java/com/tractor/<module>/application/` has at least one test
class in `<module>/src/test/java/com/tractor/<module>/application/` that:

1. Covers the happy path, asserting the concrete returned value or the concrete interaction with
   the output port.
2. Covers at least one failure path, asserting the specific `TractorStoreException` subtype.

Use cases are plain objects with constructor-injected ports, so these tests run **without a Spring
context**. Ports are replaced with Mockito mocks; domain objects are never mocked.

```java
class AddTractorToCartUseCaseTest {

  private final CartRepository cartRepository = mock(CartRepository.class);
  private final TractorCatalog tractorCatalog = mock(TractorCatalog.class);
  private final AddTractorToCartUseCase useCase =
      new AddTractorToCartUseCase(cartRepository, tractorCatalog);

  /**
   * Cover: R1
   */
  @Test
  void addsTractorToExistingCart() {
    when(tractorCatalog.findById(TRACTOR_ID)).thenReturn(Optional.of(aTractor()));
    when(cartRepository.findByCustomer(CUSTOMER_ID)).thenReturn(Optional.of(anEmptyCart()));

    Cart cart = useCase.execute(new AddTractorToCartCommand(CUSTOMER_ID, TRACTOR_ID, 2));

    assertEquals(1, cart.items().size());
    assertEquals(2, cart.items().getFirst().quantity());
    verify(cartRepository).save(cart);
  }

  /**
   * Cover: R2
   */
  @Test
  void rejectsUnknownTractor() {
    when(tractorCatalog.findById(TRACTOR_ID)).thenReturn(Optional.empty());

    TractorNotFoundException exception = assertThrows(
        TractorNotFoundException.class,
        () -> useCase.execute(new AddTractorToCartCommand(CUSTOMER_ID, TRACTOR_ID, 2)));

    assertEquals(TRACTOR_ID, exception.tractorId());
    verifyNoInteractions(cartRepository);
  }
}
```

Command:

```bash
./gradlew test            # all modules
./gradlew :cart:test      # a single module
```

### Level 2 — Controller integration tests (mandatory for features exposing HTTP)

Every feature that adds or changes a REST endpoint is verified through the real HTTP layer:
routing, status codes, JSON payload, validation and exception mapping. Calling the controller
method directly is **not** an integration test.

Default slice: `@WebMvcTest` over a single controller, with the use case replaced by
`@MockitoBean` (`@MockBean` no longer exists in Spring Boot 4, and `@Autowired` on fields is
forbidden by `docs/architecture.md`).

```java
@WebMvcTest(CartController.class)
class CartControllerTest {

  private final MockMvc mockMvc;

  @MockitoBean
  private AddTractorToCartUseCase useCase;

  CartControllerTest(@Autowired MockMvc mockMvc) {
    this.mockMvc = mockMvc;
  }

  /**
   * Cover: R1
   */
  @Test
  void returnsCartWithAddedTractor() throws Exception {
    when(useCase.execute(any())).thenReturn(aCartWith(TRACTOR_ID, 2));

    mockMvc.perform(post("/carts/{customerId}/items", CUSTOMER_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"tractorId": "%s", "quantity": 2}
                """.formatted(TRACTOR_ID)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.items", hasSize(1)))
        .andExpect(jsonPath("$.items[0].quantity").value(2));
  }

  /**
   * Cover: R2
   */
  @Test
  void mapsUnknownTractorTo404() throws Exception {
    when(useCase.execute(any())).thenThrow(new TractorNotFoundException(TRACTOR_ID));

    mockMvc.perform(post("/carts/{customerId}/items", CUSTOMER_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"tractorId": "%s", "quantity": 2}
                """.formatted(TRACTOR_ID)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("TRACTOR_NOT_FOUND"));
  }
}
```

When the feature crosses module boundaries (an event published by `cart` and consumed by
`inventory`, for example), add one full-context test in `monolith`:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CartCheckoutIntegrationTest { ... }
```

Command:

```bash
./gradlew :cart:test --tests '*ControllerTest'
./gradlew :monolith:test
```

### Level 3 — Manual smoke test with a running server (recommended before closing)

Before closing the session, boot the real application and exercise the end-to-end flow with `curl`.
This catches what the slices hide: bean wiring, JSON serialization of the real DTOs, springdoc
annotations, and the actual property configuration.

Terminal 1:

```bash
./gradlew :monolith:bootRun
```

Terminal 2:

```bash
BASE=http://localhost:8080

# health first: if this is not UP, nothing below is meaningful
curl -s $BASE/actuator/health

curl -s -X POST $BASE/carts/demo-customer/items \
  -H 'Content-Type: application/json' \
  -d '{"tractorId": "T-100", "quantity": 2}' -i

curl -s $BASE/carts/demo-customer | jq .

# error path too, not only the happy one
curl -s -X POST $BASE/carts/demo-customer/items \
  -H 'Content-Type: application/json' \
  -d '{"tractorId": "does-not-exist", "quantity": 1}' -i

# the API contract is documented, so check it renders
curl -s $BASE/v3/api-docs | jq '.paths | keys'
```

Stop the server with `Ctrl+C` when finished. Paste the relevant request/response pairs into
`progress/impl_<name>.md` — the transcript is the evidence.

On Windows PowerShell use `curl.exe` explicitly (bare `curl` is an alias for
`Invoke-WebRequest`), and quote the JSON body with single quotes.

### Level 4 — Requirements traceability (mandatory for features with `sdd: true`)

Every `R<n>` in `specs/<feature-name>/requirements.md` maps to at least one concrete test, and
every test declares the requirements it covers with a `Cover:` Javadoc block, as defined in
`docs/spec.md`. The reviewer rejects the feature if a requirement has no test, or a test covers no
requirement.

The implementer documents the map in `progress/impl_<name>.md`:

```markdown
## Traceability
- R1 → `AddTractorToCartUseCaseTest#addsTractorToExistingCart`, `CartControllerTest#returnsCartWithAddedTractor`
- R2 → `AddTractorToCartUseCaseTest#rejectsUnknownTractor`, `CartControllerTest#mapsUnknownTractorTo404`
- R3 → `CartCheckoutIntegrationTest#reservesStockOnCheckout`
```

Every `T<n>` in `specs/<feature-name>/tasks.md` must be checked `[x]`, and every checked task must
be backed by tests listed in this map.

## Anti-patterns (do not do)

- ❌ "I added the endpoint, it should work." → no executable test, no evidence.
- ❌ A test that only asserts no exception is thrown (`assertDoesNotThrow`, empty body). → it must
  assert the concrete result, status code or interaction.
- ❌ Calling `cartController.addItem(...)` directly and calling it an integration test. → go through
  `MockMvc` or a real HTTP client.
- ❌ Mocking domain objects (`mock(Cart.class)`). → mock ports only; build real domain objects.
- ❌ `@Autowired` on fields in production code. → constructor injection, per `docs/architecture.md`.
- ❌ `Thread.sleep()` to wait for an async event. → use Awaitility or `ApplicationModuleTest`
  scenarios.
- ❌ Asserting on a hardcoded `localhost:8080` in tests. → `@SpringBootTest(webEnvironment =
  RANDOM_PORT)` with an injected `@LocalServerPort`.
- ❌ Disabling or `@Disabled`-ing a failing test to make the build green. → fix it or mark the
  feature `blocked`.
- ❌ Marking a feature as `done` without a green `./gradlew clean build`.

## Final verification before closing

```bash
./gradlew clean build     # compiles all modules and runs every test
```

The build must end with `BUILD SUCCESSFUL`. Test reports are written to
`<module>/build/reports/tests/test/index.html`.

If `./gradlew clean build` is red, **do not** mark anything as `done`. Set the feature state to
`blocked` in `features/<id>-<feature-name>.md` and write the reason, the failing task and the exact
error output in `progress/current.md`.
