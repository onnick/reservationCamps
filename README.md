# reservationCamps

Spring Boot REST API pro rezervace táborů.

## Doména
Entity:
- `AppUser` (role: `ADMIN` or `CUSTOMER`)
- `Camp`
- `CampSession` (kapacita, rozsah dat)
- `Reservation` (životní cyklus stavu)

Business pravidla (příklady):
- termín nelze vytvořit v minulosti
- rezervaci nelze vytvořit/potvrdit/zrušit po začátku termínu
- rezervace je unikátní pro dvojici (uživatel, termín)
- potvrzení rezervace respektuje kapacitu termínu
- pouze `CREATED` lze potvrdit; pouze `CONFIRMED` lze zaplatit; `PAID` nelze zrušit

## Spuštění lokálně
Požadavky: Java 21+, Docker (volitelně, ale doporučeno).

Spuštění přes Docker Compose (aplikace + PostgreSQL):
```bash
docker compose up --build
```

Spuštění přímo (je potřeba běžící PostgreSQL):
```bash
export DB_URL=jdbc:postgresql://localhost:5432/reservationcamps
export DB_USERNAME=reservationcamps
export DB_PASSWORD=reservationcamps
mvn -B spring-boot:run
```

Spuštění bez PostgreSQL (file-based H2, profil `local`, port 8081):
```bash
mvn -B clean spring-boot:run -Dspring-boot.run.profiles=local
```

Pokud chceš v profilu `local` mít bootstrap admin účet s heslem `admin`, spusť:
```bash
BOOTSTRAP_ADMIN_PASSWORD=admin mvn -B clean spring-boot:run -Dspring-boot.run.profiles=local
```

Důvod: hesla (secrety) nechceme mít napevno v repozitáři v plaintextu. Proto se heslo pro lokální bootstrap bere z environment proměnné (případně se jednorázově vygeneruje a uloží do `./data/bootstrap-admin.txt`, která je v `.gitignore`).

Health:
```bash
curl -fsS http://localhost:8080/actuator/health
```

Health v profilu `local`:
```bash
curl -fsS http://localhost:8081/actuator/health
```

## API
Admin endpointy vyžadují hlavičku `X-Actor-Role: ADMIN`.

Vytvoření uživatele:
```bash
curl -sS -X POST http://localhost:8080/api/users \
  -H 'Content-Type: application/json' \
  -d '{"email":"a@example.com"}'
```

Vytvoření tábora:
```bash
curl -sS -X POST http://localhost:8080/api/camps \
  -H 'Content-Type: application/json' \
  -H 'X-Actor-Role: ADMIN' \
  -d '{"name":"Camp","basePriceCents":1000}'
```

## Architektura
Aplikace má jednoduchou vrstvenou architekturu:
- API vrstva: REST controllery v `src/main/java/.../api` vystavují HTTP endpointy a mapují request/response.
- Aplikační/doménová logika: služby v `src/main/java/.../service` implementují business pravidla (stavové přechody, kontrola kapacity, kontrola rolí, prevence duplicit).
- Perzistence: JPA entity v `src/main/java/.../domain` a Spring Data repository v `src/main/java/.../domain/repo` ukládají data do relační databáze.
- Migrace: Flyway migrace v `src/main/resources/db/migration` (a `db/migration/h2` pro profil `local`) drží databázové schéma verzované.
- Error handling: `ApiExceptionHandler` převádí doménové výjimky na konzistentní HTTP odpovědi (`403/404/409` + `ApiError`); validační chyby těla requestu jsou `400`.
- Resolving aktéra/role: `ActorResolver` čte request hlavičky (`X-Actor-Role`, volitelně `X-Actor-Id`) pro autorizaci admin-only operací.
- UI: minimální statická stránka je servírovaná z `src/main/resources/static/index.html` jako lehký klient nad API (admin/diagnostika jsou záměrně schované).

## Testy
Jednotkové testy: `*Test.java` (doménová pravidla a controllery).

Integrační testy: `*IT.java` s Testcontainers, když je dostupný Docker.

Spuštění:
```bash
mvn -B verify
```

## Testovací strategie
Cílem je mít většinu business pravidel pokrytou rychlými unit testy a k tomu jen pár realistických integračních testů:
- Unit testy (`*Test.java`): zaměřují se na business pravidla a hraniční stavy ve službách (např. neplatné přechody stavů, plná kapacita, duplicity) a na `wiring` controllerů (HTTP statusy, tvar JSONu, header-based actor resolution).
- Integrační testy (`*IT.java`): ověřují end-to-end chování přes vrstvy (controller -> service -> DB) s reálnou PostgreSQL databází přes Testcontainers.
- Mocky/test doubles: Mockito je použité pro repository a pro `NotificationPort` (externí side effect). `Clock.fixed(...)` je použité, aby byla pravidla závislá na čase deterministická.
- BDD: v projektu nepoužíváme; místo toho ověřujeme API flow integračními testy.
- Co záměrně netestujeme: čistý `plumbing`/boilerplate (detaily JPA mapování anotacemi, jednoduché DTO/recordy) a statické UI HTML (UI je tenký klient nad už otestovanými pravidly v API).

## CI/CD
GitHub Actions:
- build + unit/integration testy
- JaCoCo coverage check + report jako artefakt
- Checkstyle
- build + push Docker image do GHCR na `main`
- deploy do Kubernetes `staging` na `main` (kind na runneru) + smoke test
- observability (bonus): po staging deploy se stáhnou logy z Kubernetes a export metrik z `/actuator/prometheus` jako artefakt

## Kubernetes
Manifesty používají Kustomize overlay:
- staging: `k8s/overlays/staging`
- prod: `k8s/overlays/prod`

Secrety nejsou committované. Vytvoř `secret.env` podle `secret.env.example` a aplikuj:
```bash
kubectl apply -k k8s/overlays/staging
kubectl -n staging port-forward svc/reservationcamps 8080:80
```
