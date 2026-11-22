# Architecture

SmartShop is a server-rendered Spring Boot application with a JSON API layered
on top of the same services. It follows a conventional controller → service →
repository structure so that both the Thymeleaf UI and the REST endpoints share
business rules.

## Layers

```
Browser / API client
        │
        ▼
Controller (MVC)          Controller (REST)
com.smartshop.controller  com.smartshop.controller.api
        │                         │
        └──────────┬──────────────┘
                   ▼
                Service
        com.smartshop.service
                   │
                   ▼
              Repository
        com.smartshop.repository
                   │
                   ▼
             JPA entities
        com.smartshop.entity
                   │
                   ▼
              PostgreSQL
```

- **Controllers** translate HTTP into service calls and select a view or JSON
  response. MVC controllers never contain persistence logic.
- **Services** own transactions, validation that spans multiple entities, and
  business rules such as stock checks, status transitions and moderation.
- **Repositories** are Spring Data interfaces. Fetch plans use `@EntityGraph` to
  avoid lazy-loading errors when `spring.jpa.open-in-view=false`.
- **DTOs** keep forms and API payloads decoupled from entities. Entities with
  credentials annotate password fields with `@JsonIgnore`.

## Domain model

| Entity             | Purpose                                                        |
| ------------------ | -------------------------------------------------------------- |
| `Smartphone`       | Catalogue item, price, stock, RAM, storage and Base64 image    |
| `Employee`         | Staff account with `ADMIN` or `EMPLOYEE` role                  |
| `Customer`         | Shopper account, optional portal credentials                   |
| `Cart` / `CartItem`| Persistent cart keyed by session username                      |
| `Order` / `OrderItem` | Checkout record with payment and fulfilment status          |
| `Sale`             | Revenue line created when an order is placed                   |
| `Review`           | Product rating with moderation status                          |
| `Wishlist`         | Customer/product pair with a uniqueness constraint             |
| `InventoryHistory` | Audit trail for every stock movement                           |

`Sale.orderid` links revenue to the order that produced it, so cancelling an
order can reverse both stock and reported revenue.

## Checkout flow

1. `CartService.addItem` validates quantity and available stock, then persists a
   `CartItem` with the price at the time of adding.
2. `ShopController` (or `CartRestController`) posts the cart to
   `OrderService.createFromCart`.
3. `OrderService` resolves the customer:
   - a customer checks out for themselves (email lookup);
   - staff must select a customer in the checkout form.
4. The order and its items are built, totals are recalculated from the cart
   prices, and the order is persisted.
5. `PaymentService` records a mock payment when Stripe is not configured, or
   creates a Stripe PaymentIntent when `stripe.api.key` is set.
6. Each item becomes a `Sale`, which decrements stock and writes an
   `InventoryHistory` row.
7. The cart is cleared. Cancelling the order later restores stock, marks the
   linked sales as `CANCELLED` and flags a paid order as `REFUNDED`.

## Security model

| Role       | Capabilities                                                        |
| ---------- | ------------------------------------------------------------------- |
| Anonymous  | Sign in, register, browse the shop, read public API product data    |
| `CUSTOMER` | Cart, checkout, own orders, reviews, wishlist, profile              |
| `EMPLOYEE` | Customer-facing features plus in-store checkout and order status    |
| `ADMIN`    | Everything, plus product/employee/customer CRUD, moderation, reports |

`ShopUserDetailsService` resolves a username against employees first and then
customers by email. Passwords are stored as BCrypt hashes.

CSRF protection is enabled for browser routes and disabled only for `/api/**`,
where clients authenticate with a session created by `POST /api/auth/login`.
Method-level checks (`@PreAuthorize`) guard administrative REST endpoints in
addition to the URL rules in `SecurityConfig`.

## View layer

Thymeleaf templates share `fragments/layout.html`, which provides the document
head, navigation bar, flash messages and JavaScript includes. Pages are grouped
by area:

- `auth/` – sign in and registration
- `shop/` – catalogue, product detail, cart, orders, wishlist
- `account/` – profile and staff account settings
- `admin/` – dashboard, CRUD screens, sales, orders, reviews, inventory, reports
- `error/` – 400, 403, 404 and 500 pages

Bootstrap 5 supplies layout and components; DataTables enhances the
administrative tables. Product images are stored as Base64 text and rendered
inline, which keeps the deployment self-contained.

## Design decisions

- **Flyway owns the schema.** Hibernate runs with `ddl-auto=validate` in
  production; migrations are the single source of truth.
- **No Open Session in View.** Fetch plans are explicit through `@EntityGraph`
  and service-level transactions.
- **Mock payments by default.** Local development and CI do not need Stripe
  credentials, while the same `PaymentService` switches to live PaymentIntents
  when a key is present.
- **Legacy compatibility.** `LegacyRedirectController` keeps the old JSP URLs
  working so existing bookmarks do not break.
