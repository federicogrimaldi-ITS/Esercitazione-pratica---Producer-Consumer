# Producer – product-api

Applicazione Spring Boot che gestisce i prodotti su MySQL (Spring Data JPA) ed espone la REST API `/api/prodotti` sulla porta `8081`.

Documentazione completa (architettura, API, Docker, rilascio): [README principale](../README.md).

## Tecnologie

Java 21, Spring Boot 4, Spring Web, Spring Data JPA / Hibernate, MySQL 8, Lombok, Maven.

## Struttura

```
controller  → ProdottoController      (HTTP, @RequestParam / @PathVariable)
service     → ProdottoService         (logica applicativa)
repository  → ProdottoRepository      (JpaRepository<Prodotto, Long>)
model       → Prodotto                (Entity JPA, tabella prodotti)
dto         → ProdottoRequest / ProdottoResponse
exception   → ProdottoNotFoundException / GlobalExceptionHandler (@RestControllerAdvice)
```

## Configurazione

| Variabile d'ambiente | Default |
|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://localhost:3306/esercitazione_api` |
| `SPRING_DATASOURCE_USERNAME` | `root` |
| `SPRING_DATASOURCE_PASSWORD` | `root` |
| `SPRING_SECURITY_USER_NAME` | `API_USERNAME` dal file `.env` |
| `SPRING_SECURITY_USER_PASSWORD` | `API_PASSWORD` dal file `.env` |

`POST`, `PUT` e `DELETE` richiedono HTTP Basic. Le credenziali non sono nel codice: arrivano dalle variabili d'ambiente (vedi [`../.env.example`](../.env.example)).

Il database si inizializza con [`../db/init.sql`](../db/init.sql).

## Avvio

In locale, con MySQL avviato:

```bash
mvn spring-boot:run
```

Con Docker:

```bash
docker build -t producer-api:1.0 .
```

oppure, dalla directory principale, `docker compose up --build`.

Verifica: <http://localhost:8081/api/prodotti>

## REST API

| Metodo | Endpoint | Risposte |
|---|---|---|
| GET | `/api/prodotti` (`categoria`, `nome`, `sort`, `direction`) | 200 |
| GET | `/api/prodotti/{id}` | 200, 404 |
| POST | `/api/prodotti` 🔒 | 201, 400, 401 |
| PUT | `/api/prodotti/{id}` 🔒 | 200, 400, 401, 404 |
| DELETE | `/api/prodotti/{id}` 🔒 | 204, 401, 404 |

Le richieste di test sono in [`../api.http`](../api.http).
