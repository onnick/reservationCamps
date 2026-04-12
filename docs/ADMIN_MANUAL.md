# Administrátorská příručka – reservationCamps

Datum: 2026-04-12  
Verze: 1.0

Tato příručka je pro **administrátora** (role `ADMIN`) a popisuje, jak spustit systém, vytvořit katalog (tábory/termíny) a jak provádět správu.

## 1. Spuštění

### 1.1 Docker Compose (doporučeno)
Spustí aplikaci + PostgreSQL a data DB přežijí restart díky volume.
```bash
docker compose up --build
```
UI:
- `/login`
- `/app`
- `/admin`

### 1.2 Profil `local` (H2, file-based)
Spustí aplikaci s H2 uloženou v `./data/` (data přežijí restart).
```bash
mvn -B clean spring-boot:run -Dspring-boot.run.profiles=local
```

### 1.3 Bootstrap admin účet (local)
V profilu `local` se admin vytvoří automaticky, pokud neexistuje.

Pokud chceš heslo `admin`, spusť:
```bash
BOOTSTRAP_ADMIN_PASSWORD=admin mvn -B clean spring-boot:run -Dspring-boot.run.profiles=local
```

Důvod: nechceme mít hesla/secrety napevno v repozitáři. Heslo se bere z env proměnné, případně se jednorázově vygeneruje a uloží do `./data/bootstrap-admin.txt` (soubor je v `.gitignore`).

## 2. Vytvoření katalogu (tábory a termíny)

### 2.1 Přes UI v `/app` (vývojová administrace)
V `/app` je schovaná sekce **Administrace (vývoj)**:
1. Otevřete `/app`.
2. Rozklikněte „Administrace (vývoj)“.
3. Vytvořte tábor (název + cena) tlačítkem **Vytvořit tábor**.
4. Vytvořte termín (start/end/kapacita) tlačítkem **Vytvořit termín**.

Pozn.: Tato UI sekce posílá admin hlavičku `X-Actor-Role: ADMIN` (demo přístup).

### 2.2 Přes API (curl)
Admin endpointy vyžadují hlavičku `X-Actor-Role: ADMIN`.

Vytvoření tábora:
```bash
curl -sS -X POST http://localhost:8080/api/camps \
  -H 'Content-Type: application/json' \
  -H 'X-Actor-Role: ADMIN' \
  -d '{"name":"Camp","basePriceCents":1000}'
```

Vytvoření termínu:
```bash
curl -sS -X POST http://localhost:8080/api/camps/<campId>/sessions \
  -H 'Content-Type: application/json' \
  -H 'X-Actor-Role: ADMIN' \
  -d '{"startDate":"2026-05-01","endDate":"2026-05-07","capacity":10}'
```

## 3. Správa rezervací

### 3.1 Admin přehled (UI)
Admin UI je na `/admin`. Slouží jako jednoduchá správa/přehled (např. výpis rezervací).

### 3.2 Admin API
Admin výpis rezervací je:
- `GET /api/admin/reservations`

Endpoint kontroluje:
- `X-Actor-Role: ADMIN`
- `X-Actor-Id` musí odpovídat uživateli s rolí `ADMIN` v DB

## 4. Monitoring a logging

### 4.1 Health
```bash
curl -fsS http://localhost:8080/actuator/health
```

### 4.2 Metriky (Prometheus)
```bash
curl -fsS http://localhost:8080/actuator/prometheus
```

### 4.3 Logy
- Lokálně: logy jsou v konzoli aplikace.
- V CI (staging deploy): pipeline ukládá logy z Kubernetes jako artefakty.

## 5. Kubernetes (staging/prod)
Manifesty jsou v `k8s/` (Kustomize).
- staging: `k8s/overlays/staging`
- prod: `k8s/overlays/prod`

Secrety se necommitují. Postup:
1. Vytvoř `secret.env` podle `secret.env.example` (např. v overlay).
2. Aplikuj overlay:
```bash
kubectl apply -k k8s/overlays/staging
kubectl -n staging port-forward svc/reservationcamps 8080:80
```

## 6. CI/CD
GitHub Actions pipeline (`.github/workflows/ci.yml`) automaticky:
- build + testy,
- JaCoCo report jako artefakt,
- build + push Docker image (na push do `main`),
- deploy do staging (kind) + smoke test,
- ukládá logy/metriky jako artefakty.

