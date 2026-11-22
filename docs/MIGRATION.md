# Legacy migration notes

The original SmartShop implementation was a JSP/Servlet application backed by
plain JDBC DAOs. It is preserved under [`legacy/`](../legacy/) for reference and
is excluded from the Maven build. Nothing in `legacy/` is packaged or deployed.

## Why the code was archived rather than deleted

- The old screens document the original workflows and labels.
- The JSPs are useful when comparing behaviour during acceptance testing.
- The DAOs show the legacy SQL and column names that the Flyway migrations
  reproduce.

The legacy sources use `javax.servlet` and a MySQL driver, so keeping them on
the compile path would break the Spring Boot 3 build. Moving them to `legacy/`
keeps them readable without affecting compilation.

## Route mapping

Old servlet URLs are kept alive by `LegacyRedirectController` and redirect to
the new pages:

| Legacy URL        | New destination          |
| ----------------- | ------------------------ |
| `/smartLogin`     | `/login`                 |
| `/smartLogout`    | `/logout`                |
| `/smartShop`      | `/shop`                  |
| `/smartSelect`    | `/admin/smartphones`     |
| `/empSelect`      | `/admin/employees`       |
| `/custSelect`     | `/admin/customers`       |
| `/saleSelect`     | `/admin/sales`           |
| `/smartSale`      | `/cart`                  |
| `/smartMore`      | `/admin/smartphones`     |
| `/smartInsert`    | `/admin/smartphones`     |
| `/smartUpdate`    | `/admin/smartphones`     |
| `/smartDelete`    | `/admin/smartphones`     |
| `/empInsert`      | `/admin/employees`       |
| `/empUpdate`      | `/admin/employees`       |
| `/empDelete`      | `/admin/employees`       |
| `/empAccUpdate`   | `/account`               |
| `/custInsert`     | `/admin/customers`       |
| `/custQuiInsert`  | `/admin/customers`       |
| `/custUpdate`     | `/admin/customers`       |
| `/custDelete`     | `/admin/customers`       |
| `/addToCart`      | `/shop`                  |
| `/remFromCart`    | `/cart`                  |

Because the new application is server-rendered in the same way, most legacy
links continue to work through these redirects.

## Architectural changes

| Legacy                          | Modern equivalent                                  |
| ------------------------------- | -------------------------------------------------- |
| JSP views                       | Thymeleaf templates with a shared layout fragment  |
| Servlets with request handling  | Spring MVC controllers                             |
| JDBC DAOs and manual SQL        | Spring Data JPA repositories and services          |
| Manual connection management    | HikariCP via Spring Boot                           |
| Hand-rolled authentication      | Spring Security form login and BCrypt              |
| SQL scripts applied by hand     | Flyway migrations V1–V12                           |
| Session-only cart               | Persistent `cart` and `cart_item` tables           |
| Single admin screen             | Role-based admin dashboard and management screens  |

## Data migration

The legacy database used MySQL; the new application targets PostgreSQL. There
is no automatic data importer. A typical migration:

1. Export the legacy tables (`smartphone`, `employee`, `customer`, `sale`) to
   CSV.
2. Create the PostgreSQL schema by starting the application once so Flyway
   applies V1–V12.
3. Import the exported rows, preserving primary keys. Adjust column names to
   the case used by the migrations (PostgreSQL folds unquoted identifiers to
   lower case; the application is case-insensitive).
4. Re-hash employee passwords with BCrypt. Legacy passwords cannot be reused
   directly; reset them or ask staff to set new ones.
5. Customers without a password can still be managed by staff. They gain portal
   access by registering with the same email address or by having an admin set
   a password.

Always validate imported data against the new constraints (unique usernames,
unique customer emails, review ratings between 1 and 5).

## Known follow-ups

- Payment capture and webhooks are not implemented; the Stripe integration
  creates a PaymentIntent only.
- Product images are Base64-encoded in the database. Object storage is the
  recommended next step for large catalogues.
- The archived JSPs are reference material and are not maintained.
