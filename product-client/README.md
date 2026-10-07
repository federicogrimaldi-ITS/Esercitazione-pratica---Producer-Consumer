# product-client – Consumer Application

Applicazione web Spring Boot che gestisce il catalogo prodotti consumando la REST API
della Producer Application (`product-api`). Il Consumer **non accede al database**:
tutti i dati arrivano dalla Producer via HTTP.

```
Browser → Consumer (:8082) → HTTP/REST → Producer (:8081) → Service → Repository → MySQL
```

## Collaboratori

- Federico Grimaldi – Consumer Application
- Andrea Savoia – Producer Application
- AI: Claude (Anthropic) tramite Claude Code – supporto alla pianificazione, allo sviluppo
  guidato dai test e alla stesura della documentazione del Consumer

## Descrizione

Il Consumer offre:

- `/prodotti` – tabella dei prodotti (ID, nome, categoria, prezzo, quantità);
- ricerca per **nome** e/o **categoria** e **ordinamento per prezzo** dalla stessa pagina;
- `/prodotti/{id}` – pagina di dettaglio del prodotto;
- **creazione, modifica ed eliminazione** dei prodotti, con gli errori di validazione della
  Producer mostrati sotto i campi del form;
- gestione degli errori della REST API (prodotto inesistente, credenziali rifiutate, errori
  del server, Producer non raggiungibile);
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
- Producer Application `product-api` in esecuzione su `http://localhost:8081`
  (context path `/api`), con il relativo database MySQL
- Le credenziali HTTP Basic della Producer (`API_USERNAME` / `API_PASSWORD`) per creare,
  modificare ed eliminare prodotti

## Configurazione MySQL

Il Consumer non usa MySQL. Il database `esercitazione_api` e la tabella `prodotti` sono
gestiti esclusivamente dalla Producer: vedere il README di `product-api` per la creazione
del database e il caricamento dei dati di prova.

## Configurazione della Producer

La Producer espone la REST API su `http://localhost:8081/api/products` (porta 8081 e
context path `/api` definiti nell'`application.yaml` di `product-api`). Le operazioni di
scrittura (POST, PUT, DELETE) sono protette da Spring Security con HTTP Basic; l'utente è
definito dalle variabili d'ambiente `API_USERNAME` e `API_PASSWORD` della Producer.
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
  servlet:
    session:
      tracking-modes: cookie  # niente ;jsessionid negli URL dei redirect

api:
  base-url: http://localhost:8081/api   # indirizzo della Producer API
  categories: Accessori,Audio,Informatica,Mobile,Networking,Storage,Ufficio,Wearable
  username: ${API_USERNAME:}            # credenziali HTTP Basic della Producer
  password: ${API_PASSWORD:}
```

- L'indirizzo della Producer non è scritto nel codice Java: si può cambiare anche all'avvio,
  ad esempio `--api.base-url=http://altro-host:8081/api`.
- `api.categories` riproduce i valori dell'enum `Category` della Producer, che non espone
  un endpoint per elencarli; serve al filtro di ricerca e al form del prodotto.
- Le credenziali sono lette dalle stesse variabili d'ambiente usate dalla Producer e vengono
  inviate **solo** nelle chiamate di scrittura. Senza credenziali la consultazione funziona;
  creare, modificare o eliminare mostra la pagina "Operazione non autorizzata".

## Avvio

1. Avviare MySQL e la Producer (`product-api`) sulla porta 8081.
2. Avviare il Consumer con le stesse credenziali della Producer:

   ```bash
   cd product-client
   export API_USERNAME=<utente della Producer>
   export API_PASSWORD=<password della Producer>
   ./mvnw spring-boot:run
   ```

   oppure

   ```bash
   ./mvnw package
   API_USERNAME=... API_PASSWORD=... java -jar target/productclient-0.0.1-SNAPSHOT.jar
   ```

3. Aprire `http://localhost:8082/prodotti` (anche `http://localhost:8082/` reindirizza lì).

Test automatici (non richiedono né Producer né MySQL: le risposte HTTP sono simulate):

```bash
./mvnw test
```

## Pagine web del Consumer

| URL | Descrizione |
|-----|-------------|
| `GET /prodotti` | Tabella di tutti i prodotti, con pulsante "Nuovo prodotto" |
| `GET /prodotti?nome=laptop` | Ricerca per nome (il nome contiene il testo) |
| `GET /prodotti?categoria=Informatica` | Filtro per categoria |
| `GET /prodotti?priceSort=ASC` / `DESC` | Ordinamento per prezzo crescente / decrescente |
| `GET /prodotti?nome=pro&categoria=Accessori&priceSort=DESC` | Ricerca combinata |
| `GET /prodotti/{id}` | Dettaglio del prodotto, con pulsanti "Modifica" ed "Elimina" |
| `GET /prodotti/nuovo` → `POST /prodotti` | Form di creazione |
| `GET /prodotti/{id}/modifica` → `POST /prodotti/{id}` | Form di modifica |
| `POST /prodotti/{id}/elimina` | Eliminazione (con conferma nel browser) |

Dopo un salvataggio o un'eliminazione il Consumer reindirizza alla pagina del prodotto o
alla lista con un messaggio di conferma.

## REST API utilizzate (esposte dalla Producer)

La Producer non combina filtri e ordinamento in un'unica chiamata: `ProdottoApiClient`
sceglie l'endpoint in base ai criteri inseriti nella pagina.

| Azione nel Consumer | Chiamata alla Producer |
|---------------------|------------------------|
| lista senza filtri | `GET /api/products` |
| solo ordinamento | `GET /api/products/sort?direction=ASC\|DESC` |
| solo nome | `GET /api/products/search?name={nome}` |
| categoria (con o senza nome) | `GET /api/products/category/{categoria}`, poi filtro per nome nel Consumer |
| filtri + ordinamento | endpoint del filtro, poi ordinamento per prezzo nel Consumer |
| dettaglio | `GET /api/products/{id}` |
| creazione | `POST /api/products` (HTTP Basic) |
| modifica | `PUT /api/products/{id}` (HTTP Basic) |
| eliminazione | `DELETE /api/products/{id}` (HTTP Basic) |

La ricerca per nome della Producer usa `LIKE %nome%` (il nome contiene il testo, senza
distinguere maiuscole e minuscole con la collation del database); il filtro per nome applicato
dal Consumer nella ricerca combinata usa lo stesso criterio.

Elenco delle REST API della Producer (vedere README di `product-api`):

| Metodo | Endpoint | Accesso | Risposta |
|--------|----------|---------|----------|
| `GET` | `/api/products` | pubblico | 200 OK |
| `GET` | `/api/products/{id}` | pubblico | 200 OK / 404 Not Found / 400 (id non positivo) |
| `GET` | `/api/products/category/{category}` | pubblico | 200 OK / 400 (categoria non valida) |
| `GET` | `/api/products/search?name={name}` | pubblico | 200 OK |
| `GET` | `/api/products/sort?direction=ASC\|DESC` | pubblico | 200 OK |
| `POST` | `/api/products` | HTTP Basic | 201 Created / 400 Bad Request / 401 |
| `PUT` | `/api/products/{id}` | HTTP Basic | 200 OK / 400 / 404 / 401 |
| `DELETE` | `/api/products/{id}` | HTTP Basic | 204 No Content / 404 / 401 |

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
    "category": "Informatica",
    "quantity": 15,
    "creationDate": "2026-09-01T09:00:00"
  },
  "error": null,
  "httpStatus": "200 OK",
  "message": null,
  "timestamp": "2026-10-07T10:00:00"
}
```

In caso di errore `data` è `null`, il testo mostrato all'utente è `message` e, per i 400 di
validazione, `errors` contiene i messaggi per campo (mostrati sotto i campi del form):

```json
{
  "data": null,
  "error": "Bad Request",
  "errors": { "name": "Name is required", "price": "Price cannot be negative" },
  "httpStatus": "400 BAD_REQUEST",
  "message": "Some fields are invalid.",
  "timestamp": "2026-10-07T10:00:00"
}
```

Le categorie sono i valori dell'enum `Category` della Producer (`Accessori`, `Audio`,
`Informatica`, `Mobile`, `Networking`, `Storage`, `Ufficio`, `Wearable`) e vengono inviate
esattamente in questa forma.

## Gestione degli errori

`ProdottoApiClient` traduce le risposte della Producer in eccezioni applicative, gestite
da `WebExceptionHandler` (`@ControllerAdvice`) con la pagina `errore.html`, oppure dal form:

| Situazione | Eccezione | Risultato |
|------------|-----------|-----------|
| Producer spenta / timeout | `ApiNonDisponibileException` | 503 – "Impossibile recuperare i prodotti. Il servizio API non è attualmente disponibile." |
| Prodotto inesistente (404) | `ProdottoNonTrovatoException` | 404 – messaggio della Producer ("Product not found with Id: …") |
| Dati non validi nel form (400 con `errors`) | `InvalidProductException` | form mostrato di nuovo con i messaggi sotto i campi |
| Valore non numerico in prezzo o quantità | errore di binding | form mostrato di nuovo con "Valore non valido", senza chiamare la Producer |
| Credenziali rifiutate (401/403) | `ApiAuthenticationException` | 502 – "Operazione non autorizzata", con l'indicazione di controllare `API_USERNAME` e `API_PASSWORD` |
| Altro errore HTTP della Producer | `ApiErroreException` | 502 – codice e messaggio dell'API |

## Struttura del progetto

```
src/main/java/com/savoia/productclient
├── ProductClientApplication.java
├── config       ApiProperties (api.*), CacheConfig
├── model        ProdottoDTO, Categoria, CriteriRicerca, PriceSort, ProductForm, RispostaApi
├── client       ProdottoApiClient (RestClient)
├── exception    eccezioni applicative + WebExceptionHandler
├── service      ProdottoService
└── controller   ProdottoWebController (lista, dettaglio), ProductFormController (crea, modifica, elimina)
src/main/resources
├── application.yaml, messages.properties
├── templates    prodotti/lista.html, prodotti/dettaglio.html, prodotti/form.html, errore.html
└── static/css   style.css
```

Separazione dei modelli: l'Entity JPA e i DTO dell'API restano nella Producer; il Consumer
usa un proprio modello client (`ProdottoDTO`, record immutabile con `BigDecimal` per il
prezzo e `LocalDateTime` per la data), mappato sui campi JSON inglesi della Producer tramite
`@JsonProperty`. `ProductForm` è sia il modello del form sia il corpo JSON inviato nelle
chiamate di creazione e modifica.

Le scelte architetturali significative sono registrate in [`docs/decisions.md`](../docs/decisions.md).

## Funzionalità aggiuntive

- **Cache locale** (livello esperto): le risposte delle chiamate di lettura sono conservate
  in una cache Caffeine in memoria per 60 secondi; nel frattempo le stesse richieste non
  generano nuove chiamate HTTP. Gli errori non vengono messi in cache e ogni creazione,
  modifica o eliminazione svuota la cache, così le pagine mostrano subito i dati aggiornati.
- **Autenticazione verso la Producer**: le chiamate di scrittura usano HTTP Basic con le
  credenziali di servizio configurate tramite variabili d'ambiente.
- **Timeout configurabili** verso la Producer, così una Producer lenta produce il messaggio
  di servizio non disponibile invece di bloccare la pagina.
- **Test automatici** (66 test) su client HTTP, gestione errori, pagine web, form e cache.
