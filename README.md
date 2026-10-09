# TaskFlow

TaskFlow is a responsive task-board application for individuals and small teams. The repository is a monorepo containing the Angular client, Spring Boot API, architecture contracts, and local infrastructure.

## Repository layout

- `task-flow-frontend/` — Angular client.
- `task-flow-backend/` — Java 25 / Spring Boot 4.1.1 API.
- `docs/` — product scope, decisions, API and real-time contracts, and the supplied UI reference.
- `infra/` — local development services.

## Local services

Copy `.env.example` to `.env`, then start PostgreSQL and Mailpit:

```bash
docker compose --env-file .env -f infra/compose.yaml up -d
```

Mailpit's web UI is available at <http://localhost:8025>; its SMTP endpoint is `localhost:1025`.

## Backend

The backend targets Java 25. With a Java 25 JDK active:

```bash
cd task-flow-backend
./mvnw verify
```

## Frontend

The Angular project uses Node 22.22.3 or newer in the Node 22 line:

```bash
cd task-flow-frontend
npm ci
npm start
```

## Contract-first workflow

Business rules and API/event contracts live in `docs/`. Implement each capability as a vertical slice: contract, backend and tests, Angular integration, and end-to-end verification. The backend remains authoritative for authorization.

See `docs/architecture/v1-scope.md` for the V1 boundaries and the linked architecture documents for detailed decisions.
