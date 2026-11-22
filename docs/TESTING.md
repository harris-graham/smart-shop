# Testing guide

## Running the suite

```bash
./mvnw test
```

On Windows:

```powershell
.\mvnw.cmd test
```

`verify` runs the same tests and also produces the JaCoCo report:

```bash
./mvnw verify
```

## Test profile

Tests activate the `test` profile, which loads
`src/test/resources/application-test.properties`:

```properties
spring.datasource.url=jdbc:h2:mem:smartshop;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop
spring.flyway.enabled=false
stripe.api.key=
```

H2 runs in PostgreSQL compatibility mode and Hibernate creates the schema from
the entities. No database server or Docker daemon is required, which keeps CI
fast and deterministic.

## Test inventory

| Test class                        | Focus                                              |
| --------------------------------- | -------------------------------------------------- |
| `SmartphoneServiceTest`           | Product creation, not-found handling, restock delegation |
| `CartServiceTest`                 | Adding lines and rejecting quantities above stock  |
| `OrderServiceTest`                | Checkout, payment, cancellation, status transitions|
| `ShopUserDetailsServiceTest`      | Employee and customer authentication               |
| `SmartShopApplicationTests`       | Context startup, public login page, protected shop |

## Writing new tests

- Prefer unit tests with Mockito for services. They run in milliseconds and do
  not need a Spring context.
- Use `@SpringBootTest` with `@ActiveProfiles("test")` only for wiring,
  security or template behaviour.
- For MVC endpoints, combine `@SpringBootTest` with `@AutoConfigureMockMvc` and
  `spring-security-test` request post-processors.
- Use AssertJ assertions (`assertThat`, `assertThatThrownBy`) for readable
  failures.

Example service test:

```java
@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock ReviewRepository reviewRepository;
    @Mock SmartphoneRepository smartphoneRepository;
    @Mock CustomerRepository customerRepository;
    @InjectMocks ReviewService reviewService;

    @Test
    void rejectsDuplicateReview() {
        // arrange mocks, then:
        assertThatThrownBy(() -> reviewService.addReview("a@b.c", form))
                .isInstanceOf(IllegalStateException.class);
    }
}
```

## Testcontainers

`org.testcontainers:postgresql` and `org.testcontainers:junit-jupiter` are on the
test classpath for PostgreSQL integration tests. No test currently requires
Docker. If you add one, annotate it with `@Testcontainers` and
`@Container static PostgreSQLContainer<?> postgres = ...` and point
`spring.datasource.url` at the container.

## Coverage

JaCoCo writes reports to `target/site/jacoco/` and CI uploads them as an
artifact. The project does not enforce a coverage threshold yet; add a
`jacoco:check` execution when the suite has broader coverage.
