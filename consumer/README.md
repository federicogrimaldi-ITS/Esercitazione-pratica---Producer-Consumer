# Consumer – product-client

Applicazione Spring Boot con interfaccia web Thymeleaf sulla porta `8082`. Legge i prodotti **solo** tramite la REST API del Producer: non ha accesso a MySQL e non usa JPA.

Documentazione completa (architettura, API, Docker, rilascio): [README principale](../README.md).

## Tecnologie

Java 21, Spring Boot 4, Spring Web, Thymeleaf, RestClient, Lombok, Maven.

## Flusso

```
Browser → Controller → ProdottoApiClient (RestClient) → HTTP → Producer REST API → JSON → ProdottoDTO → Thymeleaf
```

## Configurazione

| Variabile d'ambiente | Default | Docker |
|---|---|---|
| `PRODUCER_URL` | `http://localhost:8081` | `http://producer:8081` |

```properties
api.base-url=${PRODUCER_URL:http://localhost:8081}/api
```

## Avvio

In locale, con il Producer avviato:

```bash
mvn spring-boot:run
```

Con Docker:

```bash
docker build -t consumer-web:1.0 .
```

oppure, dalla directory principale, `docker compose up --build`.

## Pagine

| URL | Descrizione |
|---|---|
| <http://localhost:8082/prodotti> | tabella prodotti con ricerca per nome e categoria |
| <http://localhost:8082/prodotti/{id}> | dettaglio prodotto |

Se il Producer non è raggiungibile viene mostrato il messaggio *"Impossibile recuperare i prodotti. Il servizio API non è attualmente disponibile."*.
