# ERP Finance Backend (MVP)

Minimal Spring Boot service aligned to internship PDFs: COA, Journals, Posting, Trial Balance (basic), Reconciliation (to be added).

## Tech
- Java 17, Spring Boot 3.2
- JPA/Hibernate
- H2 (dev) and PostgreSQL (prod profile)

## Run (Dev: H2)
```bash
# from backend/
mvn spring-boot:run
```

Service starts on http://localhost:8080

## Endpoints (MVP)
- Accounts
  - GET /api/accounts
  - POST /api/accounts
    - body: { code, name, type, parentId }
- Journals
  - POST /api/journals (with lines; debit/credit BigDecimal)
  - POST /api/journals/{id}/approve
  - POST /api/journals/{id}/post (validates Debit=Credit, updates account balances)

- GL
  - GET /api/gl/trial-balance?period=YYYY-MM
    - returns totals for assets, liabilities, equity, revenue, expenses, and equation check

- Reconciliation
  - GET /api/recon
  - POST /api/recon/import (list of items with date, amount, reference)
  - GET /api/recon/matches?period=YYYY-MM (suggested matches by amount/date)
  - POST /api/recon/{id}/resolve?journalId=&journalLineId=&variance=

## Switch to PostgreSQL
```bash
# ensure local postgres is running and database exists
# default credentials from application.yml: postgres/postgres
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

## Next
- Add GL trial balance endpoint
- Add bank reconciliation endpoints
- Integrate auth (OAuth2/JWT) and audit logging
- Wire prototype/Angular front-end to these APIs
 - Persist per-period balances and performance indexes
