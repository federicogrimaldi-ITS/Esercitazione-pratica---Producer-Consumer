# product-client – Consumer Application

Applicazione web Spring Boot che visualizza il catalogo prodotti consumando la REST API
della Producer Application (`product-api`). Il Consumer **non accede al database**:
tutti i dati arrivano via HTTP dalla Producer.

```
Browser → Consumer (:8082) → HTTP/REST → Producer (:8080) → Service → Repository → MySQL
```

## Collaboratori

- Federico Grimaldi – Consumer Application
- Andrea Savoia – Producer Application
- AI: Claude (Anthropic) tramite Claude Code – supporto alla pianificazione, allo sviluppo
  guidato dai test e alla stesura della documentazione del Consumer

## Descrizione

Il Consumer offre:

- `/prodotti` – tabella dei prodotti (ID, nome, categoria, prezzo, quantità);
- ricerca per **nome** e/o **categoria** dalla stessa pagina;
- `/prodotti/{id}` – pagina di dettaglio del prodotto;
- gestione degli errori della REST API (prodotto inesistente, errori del server,
  Producer non raggiungibile);
- cache locale delle risposte della Producer.

Flusso di generazione delle pagine:

```
Controller → Service → API Client → REST API → JSON → DTO → Thymeleaf
```

## Tecnologie

- Java 25
- Spring Boot 4.1.1 (stessa versione della Producer)
- Spring Web MVC
- RestClient (client HTTP)
- Thymeleaf
- Spring Cache + Caffeine
- Lombok
- JUnit 5, AssertJ, Mockito, MockMvc, MockRestServiceServer

## Requisiti

- JDK 25
- Maven 3.9+ (oppure il Maven wrapper incluso, `./mvnw`)
- Producer Application `product-api` in esecuzione su `http://localhost:8080` (context path `/api`)
  (con il relativo database MySQL)

## Configurazione MySQL

Il Consumer non usa MySQL. Il database `esercitazione_api` e la tabella `prodotti` sono
gestiti esclusivamente dalla Producer: vedere il README di `product-api` per la creazione
del database e il caricamento dei dati di prova.

## Configurazione della Producer

La Producer espone la REST API su `http://localhost:8080/api/products` (porta 8080 e context path `/api` definiti nell'`application.yaml` di `product-api`).
Configurazione e avvio sono descritti nel README di `product-api`.

## Configurazione del Consumer

`src/main/resources/application.yaml`:

```yaml
spring:
  application:
    name: product-client
  http:
    clients:
      connect-timeout: 2s     # timeout di connessione verso la Producer
      read-timeout: 5s        # timeout di lettura della risposta
  cache:
    cache-names: prodotti,prodotto
    caffeine:
      spec: maximumSize=500,expireAfterWrite=60s   # durata della cache locale

server:
  port: 8082

api:
  base-url: http://localhost:8080/api   # indirizzo della Producer API
```

L'indirizzo della Producer non è scritto nel codice Java: si può cambiare anche all'avvio,
ad esempio `--api.base-url=http://altro-host:8080/api`.

## Avvio

1. Avviare MySQL e la Producer (`product-api`) sulla porta 8080.
2. Avviare il Consumer:

   ```bash
   cd product-client
   ./mvnw spring-boot:run
   ```

   oppure

   ```bash
   ./mvnw package
   java -jar target/productclient-0.0.1-SNAPSHOT.jar
   ```

3. Aprire `http://localhost:8082/prodotti` (anche `http://localhost:8082/` reindirizza lì).

Test automatici (non richiedono né Producer né MySQL: le risposte HTTP sono simulate):

```bash
./mvnw test
```

## Pagine web del Consumer

| URL | Descrizione |
|-----|-------------|
| `GET /prodotti` | Tabella di tutti i prodotti |
| `GET /prodotti?nome=Laptop Pro 15` | Ricerca per nome (nome completo, vedi nota sotto) |
| `GET /prodotti?categoria=INFORMATICA` | Filtro per categoria |
| `GET /prodotti?nome=Monitor 27 4K&categoria=INFORMATICA` | Ricerca combinata |
| `GET /prodotti/{id}` | Dettaglio del prodotto |

## REST API utilizzate (esposte dalla Producer)

La Producer non combina i filtri in un'unica chiamata: `ProdottoApiClient` sceglie l'endpoint
in base ai criteri inseriti nella pagina.

| Ricerca nella pagina `/prodotti` | Chiamata alla Producer |
|----------------------------------|------------------------|
| nessun filtro | `GET /api/products` |
| solo nome | `GET /api/products/search?name={nome}` |
| solo categoria | `GET /api/products/category/{CATEGORIA}` |
| nome + categoria | `GET /api/products/category/{CATEGORIA}`, poi filtro per nome nel Consumer |
| dettaglio `/prodotti/{id}` | `GET /api/products/{id}` |

> Nota: la ricerca per nome della Producer confronta il **nome completo** senza distinguere
> maiuscole e minuscole (`Laptop Pro 15` trova il prodotto, `laptop` no). Il filtro applicato dal
> Consumer nella ricerca combinata usa lo stesso criterio, così i risultati sono coerenti.

Elenco delle REST API della Producer (vedere README di `product-api`):

| Metodo | Endpoint | Risposta |
|--------|----------|----------|
| `GET` | `/api/products` | 200 OK |
| `GET` | `/api/products/{id}` | 200 OK / 404 Not Found / 400 (id non positivo) |
| `GET` | `/api/products/category/{category}` | 200 OK / 400 (categoria non valida) |
| `GET` | `/api/products/search?name={name}` | 200 OK |
| `GET` | `/api/products/sort?direction=ASC\|DESC` | 200 OK |
| `POST` | `/api/products` | 201 Created / 400 Bad Request |
| `PUT` | `/api/products/{id}` | 200 OK / 404 Not Found |
| `DELETE` | `/api/products/{id}` | 204 No Content / 404 Not Found |

La configurazione Swagger della Producer dichiara uno schema `bearerAuth`, ma la Producer non
usa Spring Security: il Consumer non invia token.

### Formato delle risposte della Producer

Tutte le risposte (corrette ed errori) sono avvolte nella busta `ResponseApi`
(record `RispostaApi<T>` nel Consumer). Il prodotto o la lista si trovano in `data`:

```json
{
  "data": {
    "id": 1,
    "name": "Laptop Pro 15",
    "description": "Notebook professionale...",
    "price": 1299.90,
    "category": "INFORMATICA",
    "quantity": 15,
    "dataCreazione": "2026-09-01T09:00:00"
  },
  "error": null,
  "errors": null,
  "httpStatus": "200 OK",
  "message": null,
  "timestamp": "2026-09-29T10:00:00"
}
```

In caso di errore `data` è `null` e il testo mostrato all'utente è `message`
(per i 400 di validazione anche `errors`, mappa campo → messaggio):

```json
{
  "data": null,
  "error": "Not Found",
  "errors": null,
  "httpStatus": "404 NOT_FOUND",
  "message": "Product not found with Id: 9999",
  "timestamp": "2026-09-29T10:41:49"
}
```

Le categorie sono i valori dell'enum `Category` della Producer (`ACCESSORI`, `AUDIO`,
`INFORMATICA`, `MOBILE`, `NETWORKING`, `STORAGE`, `UFFICIO`, `WEARABLE`): il Consumer invia il
codice in maiuscolo e mostra all'utente l'etichetta leggibile ("Informatica").

## Gestione degli errori

`ProdottoApiClient` traduce le risposte della Producer in eccezioni applicative, gestite
da `WebExceptionHandler` (`@ControllerAdvice`) con la pagina `errore.html`:

| Situazione | Eccezione | Pagina mostrata |
|------------|-----------|-----------------|
| Producer spenta / timeout | `ApiNonDisponibileException` | 503 – "Impossibile recuperare i prodotti. Il servizio API non è attualmente disponibile." |
| Prodotto inesistente (404) | `ProdottoNonTrovatoException` | 404 – messaggio restituito dall'API ("Product not found with Id: …") |
| Altro errore HTTP della Producer | `ApiErroreException` | 502 – codice e messaggio dell'API |

## Struttura del progetto

```
src/main/java/com/savoia/productclient
├── ProductClientApplication.java
├── config       ApiProperties (api.base-url), CacheConfig
├── model        ProdottoDTO, Categoria, CriteriRicerca, RispostaApi
├── client       ProdottoApiClient (RestClient)
├── exception    eccezioni applicative + WebExceptionHandler
├── service      ProdottoService
└── controller   ProdottoWebController
src/main/resources
├── application.yaml
├── templates    prodotti/lista.html, prodotti/dettaglio.html, errore.html
└── static/css   style.css
```

Separazione dei modelli: l'Entity JPA e i DTO dell'API restano nella Producer; il Consumer
usa un proprio modello client (`ProdottoDTO`, record immutabile con `BigDecimal` per il
prezzo e `LocalDateTime` per la data) con nomi italiani, mappati sui campi JSON inglesi della
Producer tramite `@JsonProperty`.

## Funzionalità aggiuntive

- **Cache locale** (livello esperto): le risposte di `GET /api/products` (per criteri di
  ricerca) e `GET /api/products/{id}` sono conservate in una cache Caffeine in memoria per
  60 secondi; nel frattempo le stesse richieste non generano nuove chiamate HTTP. Gli errori
  non vengono messi in cache.
- **Timeout configurabili** verso la Producer, così una Producer lenta produce il messaggio
  di servizio non disponibile invece di bloccare la pagina.
- **Test automatici** (37 test) su client HTTP, gestione errori, pagine web e cache.
