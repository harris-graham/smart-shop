# Deployment guide

## Container image

Build the image from the repository root:

```bash
docker build -t smart-shop:latest .
```

The Dockerfile uses a multi-stage build: Maven compiles and packages the
application, then the jar runs on a minimal JRE image as a non-root user. A
health check probes `/login`.

## GitHub Actions deployment

The CD workflow publishes an immutable image digest to GHCR, then posts that
digest to the deployment webhook configured for the selected GitHub environment.
Configure the `staging` and `production` environments under repository Settings
and add these values to each environment:

- Secret `DEPLOYMENT_WEBHOOK_URL`: HTTPS endpoint accepting a JSON POST.
- Secret `DEPLOYMENT_WEBHOOK_TOKEN`: bearer token expected by that endpoint.
- Variable `DEPLOYMENT_URL`: public URL shown on the GitHub deployment record.

The webhook receives `image`, `digest`, `environment`, and `ref` fields. It
should deploy the supplied immutable `image@digest` and return a successful
HTTP status only after accepting the rollout. Tag a release `v*` to publish and
deploy to production; pushes to `main` deploy to staging. The workflow can also
be started manually to select either environment. Without the secrets, the
deployment job fails explicitly instead of reporting a placeholder deployment
as successful. Configure environment protection rules in GitHub for production
approvals.

GitHub Pages must be enabled in repository Settings with the source set to
GitHub Actions. The Pages workflow publishes the `docs/` directory on changes
to `main`.

Branch protection and rulesets are repository-level settings and cannot be
applied from this source tree. Protect `main` by requiring pull requests and the
CI and CodeQL checks before merge.

## Docker Compose

```bash
docker compose up --build -d
docker compose logs -f app
```

Compose starts PostgreSQL 16 with a named volume, waits for the database health
check, then starts the application on port 8080. Flyway creates and upgrades the
schema on startup.

Stop the stack without deleting data:

```bash
docker compose down
```

Delete the database volume as well:

```bash
docker compose down -v
```

## Environment variables

| Variable              | Required | Default                                             |
| --------------------- | -------- | --------------------------------------------------- |
| `DATABASE_URL`        | yes      | `jdbc:postgresql://localhost:5432/smartshop`         |
| `DATABASE_USER`       | yes      | `postgres`                                          |
| `DATABASE_PASSWORD`   | yes      | `postgres`                                          |
| `PORT`                | no       | `8080`                                              |
| `DB_POOL_SIZE`        | no       | `10`                                                |
| `THYMELEAF_CACHE`     | no       | `false` (set `true` in production)                  |
| `LOG_LEVEL`           | no       | `INFO`                                              |
| `LOW_STOCK_THRESHOLD` | no       | `3`                                                 |
| `STRIPE_API_KEY`      | no       | empty (mock payments)                               |
| `STRIPE_CURRENCY`     | no       | `usd`                                               |

Never commit real credentials. Use your platform's secret store or a local
`application-local.properties` file that is excluded from version control.

## Database migrations

Flyway runs automatically. The relevant settings are:

```properties
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.validate-on-migrate=true
```

Operational rules:

1. Take a backup before deploying a release that contains new migrations.
2. Migrations must be additive and forward-only.
3. Never edit a migration that has been applied in any environment; add a new
   version instead.
4. Test the migration path against a copy of production data.

To inspect the schema history:

```sql
SELECT installed_rank, version, description, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

## Production checklist

- [ ] `THYMELEAF_CACHE=true`
- [ ] `LOG_LEVEL=INFO` (or `WARN`) and structured log collection enabled
- [ ] Strong, unique `DATABASE_PASSWORD` supplied through secrets
- [ ] TLS terminated by a reverse proxy or load balancer
- [ ] `server.forward-headers-strategy=framework` left enabled behind a proxy
- [ ] Database backups scheduled and restore tested
- [ ] Stripe keys configured, or mock payments explicitly accepted
- [ ] Health check monitored (`GET /login` must return 200)
- [ ] Admin demo password changed after the first sign-in
- [ ] Container runs as a non-root user (the provided image does)

## Reverse proxy

The application expects the proxy to forward `Host`, `X-Forwarded-Proto` and
`X-Forwarded-For`. Example nginx location:

```nginx
location / {
    proxy_pass http://127.0.0.1:8080;
    proxy_set_header Host $host;
    proxy_set_header X-Forwarded-Proto $scheme;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
}
```

## Scaling notes

- The application is stateless apart from HTTP sessions. For multiple replicas,
  enable session persistence (sticky sessions) or add Spring Session with a
  shared store.
- Product images are stored in the database as Base64. For large catalogues,
  move images to object storage and keep only URLs in the database.
- The admin reports aggregate in the database; they scale with the `sale` and
  `shop_order` tables. Add indexes as data grows (the migrations already index
  the common filters).
