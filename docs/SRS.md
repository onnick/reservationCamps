# SRS (Software Requirements Specification) – reservationCamps

Verze: 1.0  
Datum: 2026-04-12  
Repo: `reservationCamps`

## 1. Účel a rozsah

### 1.1 Účel dokumentu
Tento dokument popisuje požadavky na aplikaci **reservationCamps** (webová aplikace + REST API) pro správu táborů, termínů a rezervací. Slouží jako podklad k obhajobě a k ověření, že implementace splňuje funkční i nefunkční požadavky.

### 1.2 Rozsah systému
Systém umožňuje:
- správu katalogu táborů a jejich termínů (admin),
- vytváření a správu rezervací uživatelů (zákazník),
- základní přehled rezervací pro přihlášeného uživatele,
- admin-only přístup na správu rezervací,
- běh lokálně i v CI/CD, kontejnerizaci a nasazení do Kubernetes.

Systém je záměrně „školní“ a bezpečnost/auth je implementována jednoduše (viz kapitola 6).

## 2. Definice a pojmy
- **Uživatel (AppUser)**: osoba identifikovaná e‑mailem, má roli `CUSTOMER` nebo `ADMIN`.
- **Tábor (Camp)**: produkt/nabídka s názvem a základní cenou.
- **Termín (CampSession)**: konkrétní běh tábora s daty `startDate/endDate` a kapacitou.
- **Rezervace (Reservation)**: vazba uživatele na termín, s životním cyklem stavu.
- **Actor**: „volající“ v API; pro admin operace se čte z HTTP hlaviček (`X-Actor-Role`, volitelně `X-Actor-Id`).

## 3. Přehled systému (overall description)

### 3.1 Uživatelské role
- **CUSTOMER**: může vytvářet rezervaci pro vybraný termín (v rámci pravidel), vidí své rezervace.
- **ADMIN**: může vytvářet tábory/termíny a vidí admin přehled rezervací.

### 3.2 Uživatelská rozhraní
- `/login`: přihlášení (email+heslo) a přehled rezervací uživatele.
- `/app`: rezervace (výběr tábora/termínu, vytvoření rezervace, změny stavu) + vývojová administrace (schovaná v `<details>`).
- `/admin`: jednoduchá stránka pro admin správu (např. výpis rezervací).

### 3.3 Provozní prostředí
- **Default**: PostgreSQL (např. přes Docker Compose), typicky port `8080`.
- **Profil `local`**: file-based H2 (data přežijí restart), typicky port `8081`.

## 4. Funkční požadavky (FR)

### 4.1 Správa uživatelů
- **FR-USER-01**: Systém musí umožnit založit uživatele se vstupy `email` a `password`.
- **FR-USER-02**: Systém musí umožnit přihlášení uživatele (`email` + `password`) a vrátit jeho identifikátor.
- **FR-USER-03**: Systém musí umožnit vyhledat uživatele dle e‑mailu.
- **FR-USER-04**: Systém musí umožnit vypsat seznam existujících uživatelů pro účely výběru v UI (omezený počet položek).

### 4.2 Katalog táborů a termínů
- **FR-CAMP-01**: Admin musí být schopen vytvořit tábor (název, cena).
- **FR-CAMP-02**: Systém musí poskytovat seznam táborů pro UI.
- **FR-SESSION-01**: Admin musí být schopen vytvořit termín pro tábor (start, end, kapacita).
- **FR-SESSION-02**: Systém musí poskytovat seznam termínů pro daný tábor.

### 4.3 Rezervace
- **FR-RES-01**: Systém musí umožnit vytvořit rezervaci pro dvojici (uživatel, termín).
- **FR-RES-02**: Systém musí umožnit změnu stavu rezervace na `CONFIRMED` (potvrzení).
- **FR-RES-03**: Systém musí umožnit změnu stavu rezervace na `PAID` (zaplacení).
- **FR-RES-04**: Systém musí umožnit zrušení rezervace (přechod na `CANCELLED`) dle pravidel.
- **FR-RES-05**: Systém musí umožnit získat detail rezervace podle ID.
- **FR-RES-06**: Systém musí umožnit vypsat rezervace konkrétního uživatele (pro přehled na `/login`).
- **FR-RES-07**: Systém musí umožnit admin výpis rezervací (správa).

## 5. Business pravidla (BR)
Následující pravidla jsou „netriviální“ doménová logika a systém je musí vynucovat serverem.

- **BR-01 (Platnost termínu)**: Termín tábora nelze vytvořit v minulosti a `startDate` musí být před `endDate`.
- **BR-02 (Kapacita)**: Potvrzení rezervace musí respektovat kapacitu termínu (např. počet `CONFIRMED/PAID` nesmí přesáhnout kapacitu).
- **BR-03 (Unikátnost)**: Rezervace je unikátní pro dvojici (`user`, `session`) – systém musí zabránit duplicitě (idempotence/prevence duplicity).
- **BR-04 (Stavové přechody)**:
  - pouze `CREATED` lze potvrdit (`CONFIRMED`),
  - pouze `CONFIRMED` lze zaplatit (`PAID`),
  - `PAID` nelze zrušit.
- **BR-05 (Časové omezení)**: Rezervaci nelze vytvořit/potvrdit/zrušit po začátku termínu.
- **BR-06 (Role/oprání)**: Admin operace jsou povolené pouze aktérovi s rolí `ADMIN` (server ověřuje roli).

## 6. Požadavky na rozhraní (External interface requirements)

### 6.1 HTTP/REST API
- Formát: JSON (`Content-Type: application/json`).
- Chyby:
  - validační chyby těla requestu: `400`,
  - zakázaná operace / nedostatečná role: `403`,
  - nenalezeno: `404`,
  - porušení business pravidel / konflikty: `409`.
- Admin volání: systém očekává hlavičku `X-Actor-Role: ADMIN` (a v některých případech i `X-Actor-Id`).

Poznámka: Nejde o plnohodnotné přihlášení pomocí JWT/session; pro školní projekt je auth zjednodušen.

### 6.2 Databáze
- Relační databáze s ORM (JPA/Hibernate).
- Schéma verzované přes Flyway migrace.

## 7. Datové požadavky a doménový model

### 7.1 Entity
- `AppUser(id, email, passwordHash, role, createdAt)`
- `Camp(id, name, basePriceCents, createdAt)`
- `CampSession(id, camp, startDate, endDate, capacity, createdAt)`
- `Reservation(id, session, user, status, createdAt, confirmedAt, paidAt, cancelledAt)`

### 7.2 Vztahy
- `Camp 1:N CampSession`
- `AppUser 1:N Reservation`
- `CampSession 1:N Reservation`

### 7.3 Integritní omezení
- unikátní `AppUser.email`,
- unikátní dvojice rezervace `(session_id, user_id)`.

## 8. Nefunkční požadavky (NFR)

### 8.1 Kvalita a testy
- **NFR-TEST-01**: Klíčová doménová logika musí být ověřena unit testy (hraniční stavy + business pravidla).
- **NFR-TEST-02**: Integrace vrstev musí být ověřena integračními testy (controller–service–DB) v realistickém prostředí (např. Testcontainers).
- **NFR-TEST-03**: V projektu musí být měření pokrytí kódu (JaCoCo) a report viditelný v CI.

### 8.2 CI/CD
- **NFR-CICD-01**: CI se spouští na push/PR a provádí build + testy.
- **NFR-CICD-02**: CI vyhodnotí code coverage a publikuje report jako artefakt.
- **NFR-CICD-03**: CI sestaví Docker image a publikuje jej (nebo jako artefakt).
- **NFR-CD-01**: Nasazení do Kubernetes pro staging je automatizované (součást pipeline).

### 8.3 Bezpečnost a secrets
- **NFR-SEC-01**: Secrety (hesla/tokény) nesmí být v repozitáři v plaintextu.
- **NFR-SEC-02**: Pro lokální bootstrap admin účtu se heslo bere z environment proměnné; pokud není nastavené, vygeneruje se a uloží do souboru ignorovaného v Gitu.

### 8.4 Observabilita
- **NFR-OBS-01**: Aplikace musí poskytovat health endpoint (`/actuator/health`).
- **NFR-OBS-02**: Aplikace musí poskytovat metriky pro scraping (`/actuator/prometheus`).
- **NFR-OBS-03**: Po nasazení ve staging CI uloží základní provozní artefakty (např. logy/metriky).

### 8.5 Spustitelnost a reprodukovatelnost
- **NFR-RUN-01**: Projekt musí jít spustit podle README lokálně.
- **NFR-RUN-02**: Projekt musí jít spustit v CI v režimu build+test.

## 9. Ověření (verification) a akceptační kritéria
- Akceptace FR/BR je doložena:
  - unit testy (`*Test.java`) pro stavové přechody, kapacitu, validace a chybové kódy,
  - integrační testy (`*IT.java`) pro realistické flow (pokud je dostupný Docker/Testcontainers),
  - CI pipeline (build, testy, coverage, image, staging deploy, smoke test).

## 10. Známé limity / out-of-scope
- Plnohodnotná autentizace/autorizace (JWT, session) není cílem; admin operace jsou školně zajištěné přes hlavičky a kontrolu role v DB.
- E2E testy UI nejsou povinnou částí (lze doplnit jako bonus).

