---
layout: default
title: SmartShop documentation
---

# SmartShop

A smartphone shop rebuilt from a legacy JSP/Servlet application into a layered
Spring Boot 3 service with PostgreSQL, Flyway migrations, a Thymeleaf
storefront and a JSON API.

## Documentation

- [Architecture](ARCHITECTURE.md) – layers, modules and data model.
- [API reference](API.md) – every REST endpoint with examples.
- [Deployment](DEPLOYMENT.md) – Docker, environment variables and pipelines.
- [Migration notes](MIGRATION.md) – how the legacy code maps to the new app.
- [Testing](TESTING.md) – how to run and extend the test suite.

## Quick start

```bash
docker compose up --build
```

Then open [http://localhost:8080](http://localhost:8080) and sign in with the
seeded administrator account (`admin` / `admin123`).

## Features

| Area        | Highlights                                                              |
| ----------- | ----------------------------------------------------------------------- |
| Storefront  | Search and filters, product pages, reviews, wishlist                    |
| Accounts    | Staff and customer sign-in, BCrypt hashing, role-based access           |
| Cart/orders | Persistent carts, coupon codes, order history, printable invoices       |
| Inventory   | Restock and adjust with a full movement history and low-stock alerts    |
| Admin       | Dashboard, reports (CSV/XLSX/PDF), promotions, bulk price updates        |
| Platform    | REST API with OpenAPI, WebSocket live updates, PWA, dark mode, CI/CD    |

## Source

The project lives on GitHub; the build, container image and documentation site
are produced by the `ci`, `cd` and `pages` workflows.