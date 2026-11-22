# SmartShop

A modern smartphone shop built with Spring Boot 3 and PostgreSQL. SmartShop
replaces the original JSP/Servlet application with a maintainable layered
architecture, database migrations, a responsive Thymeleaf storefront, a staff
dashboard and a documented REST API.

[![Java 17](https://img.shields.io/badge/Java-17-blue)](https://adoptium.net/)
[![Spring Boot 3.2](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen)](https://spring.io/projects/spring-boot)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## Highlights

- **Storefront** – search and filter by price, RAM and storage, product detail
  pages with stock indicators and customer reviews.
- **Accounts** – one sign-in form for staff and customers. Customers register
  with an email address; passwords are hashed with BCrypt.
- **Cart and checkout** – persistent carts, stock validation, order creation,
  mock payments out of the box and optional Stripe PaymentIntents.
- **Orders** – customer order history, printable invoices, staff status
  transitions (`PENDING → PROCESSING → SHIPPED → DELIVERED`), cancellation with
  automatic stock restoration.
- **Reviews** – customers rate products; administrators moderate before reviews
  become public.
- **Wishlist** – signed-in customers save products for later.
- **Inventory** – restocking and manual adjustments with a full movement
  history (sale, restock, adjustment).
- **Admin dashboard** – revenue, order, customer and low-stock metrics, top
  products, employee performance and CSV/Excel/PDF report exports.
- **Catalogue admin** – bulk price updates, CSV import and export, stock
  adjustments with a movement history.
- **Promotions** – coupon codes with percentage or fixed discounts, minimum
  spend, expiry dates and usage counters.
- **Real time** – stock, order status and cart counters pushed over STOMP/WebSocket.
- **Platform** – installable PWA with an offline page, persisted dark mode, JSON
  API documented with OpenAPI/Swagger, Flyway migrations, JUnit/Mockito tests,
  GitHub Actions CI, CD to GHCR and a GitHub Pages documentation site.

## Technology stack

| Layer      | Technology                                                        |
| ---------- | ----------------------------------------------------------------- |
| Runtime    | Java 17, Spring Boot 3.2                                           |
| Web        | Spring MVC, Thymeleaf, Bootstrap 5, jQuery, DataTables             |
| Security   | Spring Security 6, form login, role-based access, BCrypt           |
| Persistence| Spring Data JPA / Hibernate, PostgreSQL 16, Flyway                 |
| API docs   | springdoc-openapi (Swagger UI)                                     |
| Testing    | JUnit 5, Mockito, AssertJ, MockMvc, H2                            |
| Tooling    | Maven Wrapper, Checkstyle, SpotBugs, JaCoCo, Docker, GitHub Actions |

## Project layout

```text
src/main/java/com/smartshop
├── config          Security, WebSocket and startup configuration
├── controller      MVC controllers (storefront, account, admin, promotions, redirects)
│   └── api         REST controllers
├── dto             Forms, API payloads and reporting projections
├── entity          JPA entities
├── exception       API and MVC exception handlers
├── repository      Spring Data repositories
├── security        UserDetailsService bridging employees and customers
└── service         Business logic

src/main/resources
├── db/migration    Flyway migrations (V1 … V14)
├── static          Bootstrap, jQuery, DataTables, service worker and app assets
└── templates       Thymeleaf views

legacy              Archived JSP/Servlet implementation (not compiled)
docs                Architecture, API, deployment, migration and testing notes
```

## Quick start with Docker

```bash
docker compose up --build
```

The application is available at <http://localhost:8080>. PostgreSQL data is
kept in the `smartshop-db` volume. Flyway applies the schema automatically on
startup.

## Local development

Prerequisites: JDK 17, a running PostgreSQL instance, and no global Maven
installation (the wrapper downloads Maven on first use).

```bash
createdb smartshop          # or use Docker: docker compose up db -d

export DATABASE_URL=jdbc:postgresql://localhost:5432/smartshop
export DATABASE_USER=postgres
export DATABASE_PASSWORD=postgres

./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
$env:DATABASE_URL="jdbc:postgresql://localhost:5432/smartshop"
$env:DATABASE_USER="postgres"
$env:DATABASE_PASSWORD="postgres"
.\mvnw.cmd spring-boot:run
```

The shared `src/main/resources/application.properties` contains no secrets and
reads every environment-specific value from environment variables. Copy
`application.properties.example` to `application-local.properties` for local
overrides, or export the variables directly.

### Demo accounts

| Role       | Username / email        | Password   |
| ---------- | ----------------------- | ---------- |
| Admin      | `admin`                 | `admin123` |
| Customer   | register at `/register` | your choice|

The administrator account is seeded by Flyway. `DataInitializer` verifies the
seeded hash and replaces it once if it does not match the documented demo
password, so the credentials above always work on a fresh database.

## Configuration

| Variable              | Default                                             | Purpose                                  |
| --------------------- | --------------------------------------------------- | ---------------------------------------- |
| `DATABASE_URL`        | `jdbc:postgresql://localhost:5432/smartshop`         | JDBC URL                                 |
| `DATABASE_USER`       | `postgres`                                          | Database user                            |
| `DATABASE_PASSWORD`   | `postgres`                                          | Database password                        |
| `PORT`                | `8080`                                              | HTTP port                                |
| `LOG_LEVEL`           | `INFO`                                              | `com.smartshop` log level                |
| `THYMELEAF_CACHE`     | `false`                                             | Enable template caching in production    |
| `LOW_STOCK_THRESHOLD` | `3`                                                 | Low-stock warning threshold              |
| `STRIPE_API_KEY`      | *(empty)*                                           | Enables real Stripe PaymentIntents       |
| `STRIPE_CURRENCY`     | `usd`                                               | Currency sent to Stripe                  |
| `COMMISSION_RATE`     | `0.05`                                              | Employee commission used in reports      |

When `STRIPE_API_KEY` is empty the application records a mock transaction so
the checkout flow works end to end without external services. Creating a
PaymentIntent is not the same as capturing a payment; a production integration
should add webhooks and a confirmation step.

## Database migrations

| Version | Purpose                                             |
| ------- | --------------------------------------------------- |
| V1      | Smartphone catalogue                                |
| V2      | Employees and seeded administrator                  |
| V3      | Customers                                           |
| V4      | Sales                                               |
| V5      | Carts and cart items                                |
| V6      | Orders and order items                              |
| V7      | Product reviews                                     |
| V8      | Wishlist                                            |
| V9      | Inventory movement history                          |
| V10     | Customer credentials for the portal                 |
| V11     | Allow online orders without an employee             |
| V12     | Link sales to the order that produced them          |
| V13     | Theme preference and inventory index                |
| V14     | Coupons and order discount columns                  |

Migrations are additive. Never edit a migration that has already been applied;
add a new version instead.

## API overview

Interactive documentation: <http://localhost:8080/swagger-ui.html>

| Method | Path                                      | Access            |
| ------ | ----------------------------------------- | ----------------- |
| POST   | `/api/auth/login`                         | Public            |
| POST   | `/api/auth/register`                      | Public            |
| GET    | `/api/auth/me`                            | Public            |
| GET    | `/api/smartphones`                        | Public            |
| GET    | `/api/smartphones/{smartid}`              | Public            |
| POST   | `/api/smartphones`                        | Admin             |
| PUT    | `/api/smartphones/{smartid}`              | Admin             |
| DELETE | `/api/smartphones/{smartid}`              | Admin             |
| GET    | `/api/products/{smartid}/reviews`         | Public            |
| POST   | `/api/products/{smartid}/reviews`         | Customer          |
| GET    | `/api/cart`                               | Authenticated     |
| POST   | `/api/cart/items`                         | Authenticated     |
| PUT    | `/api/cart/items/{itemId}`                | Authenticated     |
| DELETE | `/api/cart/items/{itemId}`                | Authenticated     |
| POST   | `/api/cart/checkout`                      | Authenticated     |
| GET    | `/api/orders`                             | Authenticated     |
| GET    | `/api/orders/{orderid}`                   | Owner or staff    |
| POST   | `/api/orders/{orderid}/cancel`            | Owner or staff    |
| PUT    | `/api/orders/{orderid}/status`            | Admin / employee  |
| GET    | `/api/wishlist`                           | Customer          |
| POST   | `/api/wishlist/{smartid}`                 | Customer          |
| DELETE | `/api/wishlist/{smartid}`                 | Customer          |
| PUT    | `/api/reviews/{reviewid}/moderate`        | Admin             |
| GET    | `/api/coupons/validate`                   | Authenticated     |
| GET    | `/api/admin/dashboard`                    | Admin             |
| GET    | `/api/admin/inventory/low-stock`          | Admin             |
| GET    | `/api/admin/inventory/history`            | Admin             |
| POST   | `/api/admin/inventory/{smartid}/restock`  | Admin             |
| POST   | `/api/admin/inventory/{smartid}/adjust`   | Admin             |

`POST /api/auth/login` creates an HTTP session, so the returned `JSESSIONID`
cookie can be reused for the authenticated endpoints. CSRF protection is
disabled for `/api/**`; the browser UI keeps CSRF enabled.

See [docs/API.md](docs/API.md) for request and response examples.

## Real-time updates

The STOMP endpoint is served at `/ws` (SockJS plus raw WebSocket) with three
destinations:

| Destination          | Payload                                         |
| -------------------- | ----------------------------------------------- |
| `/topic/inventory`   | `STOCK_CHANGED` with the new stock level        |
| `/topic/orders`      | `ORDER_CHANGED` with the new status             |
| `/user/queue/cart`   | `CART_CHANGED` with the new item count          |

`static/js/realtime.js` implements a small STOMP client, subscribes on load and
reconnects with a backoff. Pages annotate live regions with
`data-live="inventory"`, `data-live="orders"` or `data-stock-smartid` so the
events update the DOM without a refresh. User-scoped cart events require an
authenticated WebSocket session; the pages fall back to a normal reload.

## Progressive web app

`manifest.webmanifest` and `sw.js` make the storefront installable and usable
offline: the app shell is precached, static assets use
stale-while-revalidate, navigations are network-first with an `/offline`
fallback, and `/api/**` is never cached.

## Testing

```bash
./mvnw test
```

The suite uses an in-memory H2 database (`test` profile) with Flyway disabled,
so no Docker daemon is required. Testcontainers dependencies are present for
future PostgreSQL integration tests.

Coverage is written to `target/site/jacoco/`.

## Continuous integration

`.github/workflows/ci.yml` runs `./mvnw -B verify` on Java 17 for every push and
pull request, uploads test and coverage reports, and builds the Docker image on
pushes to `main`. CodeQL performs a security and quality scan on a schedule.
`.github/workflows/cd.yml` publishes the image to GitHub Container Registry
(tagged per branch, per tag and by SHA) and sends the immutable image digest to
the deployment webhook configured in the `staging` environment on `main` and in
the `production` environment on `v*` tags. See
[docs/DEPLOYMENT.md](docs/DEPLOYMENT.md) for required GitHub environment setup.
`.github/workflows/pages.yml` builds `docs/` with Jekyll and publishes the
documentation site to GitHub Pages.

## Legacy code

The original JSP/Servlet implementation is preserved under [`legacy/`](legacy/)
for reference but is excluded from the build. See
[docs/MIGRATION.md](docs/MIGRATION.md) for the route mapping and the decisions
taken during the migration.

## Documentation

- [Architecture](docs/ARCHITECTURE.md)
- [API reference](docs/API.md)
- [Deployment guide](docs/DEPLOYMENT.md)
- [Legacy migration notes](docs/MIGRATION.md)
- [Testing guide](docs/TESTING.md)

## License

Released under the [MIT License](LICENSE).
