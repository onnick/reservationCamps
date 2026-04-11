# reservationCamps

Spring Boot REST API for camp reservations.

## Domain
Entities:
- `AppUser` (role: `ADMIN` or `CUSTOMER`)
- `Camp`
- `CampSession` (capacity, date range)
- `Reservation` (status lifecycle)

Business rules (examples):
- session cannot be created in the past
- reservation cannot be created/confirmed/cancelled after session start
- reservation is unique per (user, session)
- confirm respects session capacity
- only `CREATED` can be confirmed; only `CONFIRMED` can be paid; `PAID` cannot be cancelled

## Run Locally
Requirements: Java 21+, Docker (optional but recommended).

Run with Docker Compose (app + PostgreSQL):
```bash
docker compose up --build
```

Run directly (needs PostgreSQL running):
```bash
export DB_URL=jdbc:postgresql://localhost:5432/reservationcamps
export DB_USERNAME=reservationcamps
export DB_PASSWORD=reservationcamps
mvn -B spring-boot:run
```

Run without PostgreSQL (in-memory H2, profile `local`, port 8081):
```bash
mvn -B clean spring-boot:run -Dspring-boot.run.profiles=local
```

Health:
```bash
curl -fsS http://localhost:8080/actuator/health
```

Local profile health:
```bash
curl -fsS http://localhost:8081/actuator/health
```

## API
Admin endpoints require header `X-Actor-Role: ADMIN`.

Create user:
```bash
curl -sS -X POST http://localhost:8080/api/users \
  -H 'Content-Type: application/json' \
  -d '{"email":"a@example.com"}'
```

Create camp:
```bash
curl -sS -X POST http://localhost:8080/api/camps \
  -H 'Content-Type: application/json' \
  -H 'X-Actor-Role: ADMIN' \
  -d '{"name":"Camp","basePriceCents":1000}'
```

## Tests
Unit tests: `*Test.java` (domain rules and controllers).

Integration tests: `*IT.java` using Testcontainers when Docker is available.

Run:
```bash
mvn -B verify
```

## CI/CD
GitHub Actions:
- build + unit/integration tests
- JaCoCo coverage check + report artifact
- Checkstyle
- build + push Docker image to GHCR on `main`
- deploy to Kubernetes `staging` on `main` (kind on runner) + smoke test

## Kubernetes
Manifests use Kustomize overlays:
- staging: `k8s/overlays/staging`
- prod: `k8s/overlays/prod`

Secrets are not committed. Create `secret.env` based on `secret.env.example` and apply:
```bash
kubectl apply -k k8s/overlays/staging
kubectl -n staging port-forward svc/reservationcamps 8080:80
```
