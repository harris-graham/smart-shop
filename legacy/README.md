# Legacy SmartShop (JSP / Servlet)

This directory contains the original implementation for reference only. It is
**not** part of the Maven build and is not deployed.

```
legacy/
├── java/smart      Old beans, JDBC DAOs and servlets (javax.servlet)
└── webapp          JSP views, WEB-INF, old static assets
```

The modern application lives in `src/main/java/com/smartshop` and
`src/main/resources`. See [`docs/MIGRATION.md`](../docs/MIGRATION.md) for the
route mapping, architecture comparison and data migration guidance.

Do not add new features here. If a legacy screen contains behaviour that has
not been ported yet, reimplement it in the Spring Boot application and add a
test.
