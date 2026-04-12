# Uživatelská příručka – reservationCamps

Datum: 2026-04-12  
Verze: 1.0

Tato příručka je pro **běžného uživatele** (role `CUSTOMER`) a popisuje, jak se přihlásit a jak si vytvořit rezervaci.

## 1. Přístup do aplikace
- Přihlášení a přehled rezervací: `/login`
- Aplikace pro vytvoření rezervace: `/app`

Pozn.: Po přihlášení jako admin se uživatel přesměruje rovnou do `/app`. Běžný uživatel zůstává na `/login` a vidí svůj přehled.

## 2. Přihlášení (login)
1. Otevřete `/login`.
2. Vyplňte email a heslo.
3. Klikněte na **Přihlásit**.
4. Po přihlášení se načte tabulka „Moje rezervace“ (tábor, od, do, stav).

Pokud žádné rezervace neexistují, uvidíte hlášku „Zatím nemáte žádné rezervace.“

## 3. Vytvoření účtu
Účet se vytváří v `/app` v části „1) Kontakt“:
1. Otevřete `/app`.
2. Zadejte email a heslo (min. 5 znaků).
3. Klikněte na **Pokračovat**.

Chování:
- Pokud účet existuje, aplikace provede přihlášení.
- Pokud účet neexistuje, aplikace ho vytvoří a pokračuje dál.

## 4. Výběr existujícího uživatele (rychlé testování)
V `/app` lze vybrat existujícího uživatele z DB (bez zadání hesla), což je určené primárně pro rychlé testování:
1. Klikněte na **Vybrat existujícího**.
2. Pokud je email prázdný, otevře se okno se seznamem uživatelů.
3. Vyberte uživatele ze seznamu.

Pozn.: V reálné produkční aplikaci by tento krok typicky byl omezen autentizací/rolí, zde je to záměrně jednoduché pro školní projekt.

## 5. Vytvoření rezervace
V `/app`:
1. Nejdřív musíte mít vybraného/přihlášeného uživatele (v poli „ID zákazníka“ se objeví UUID).
2. Vyberte **Tábor**.
3. Vyberte **Termín**.
4. Klikněte na **Vytvořit rezervaci**.

Po vytvoření se vyplní „ID rezervace“ a stav.

## 6. Potvrzení / zaplacení / zrušení rezervace
V `/app` u rezervace:
- **Potvrdit**: rezervace přejde do stavu `CONFIRMED` (kontroluje se kapacita).
- **Zaplatit**: rezervace přejde do stavu `PAID`.
- **Zrušit**: rezervace přejde do stavu `CANCELLED` (zaplacenou rezervaci nelze zrušit).
- **Obnovit**: načte aktuální stav rezervace z API.

## 7. Nejčastější chyby
- `403` (forbidden): špatné přihlašovací údaje nebo nedostatečná role (typicky admin-only akce).
- `404` (not found): uživatel/rezervace/termín neexistuje.
- `409` (conflict): porušení business pravidla (duplicitní rezervace, plná kapacita, špatný stavový přechod, termín už začal).

