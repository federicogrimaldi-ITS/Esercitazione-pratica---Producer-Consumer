# Esercitazione pratica – Producer / Consumer

Sistema composto da due applicazioni Spring Boot indipendenti e da un database MySQL, eseguibili in locale oppure come container Docker orchestrati con Docker Compose.

- **Producer** (`product-api`, porta `8081`): gestisce i prodotti su MySQL tramite Spring Data JPA ed espone una REST API su `/api/prodotti`.
- **Consumer** (`product-client`, porta `8082`): interfaccia web Thymeleaf che legge i dati **esclusivamente** tramite la REST API del Producer, senza accedere al database.

## Gruppo di lavoro e AI utilizzate

| Componente | Ruolo |
|---|---|
| Federico Grimaldi | _(da compilare)_ |
| _(da compilare)_ | _(da compilare)_ |

| AI | Percentuale di utilizzo |
|---|---|
| Claude | _(da compilare)_ % |
| _(da compilare)_ | _(da compilare)_ % |

## Architettura

```
                  Docker network (esercitazione-api_default)
┌────────────────────────────────────────────────────────────────┐
│                                                                │
│   ┌──────────────┐     HTTP/REST     ┌──────────────┐          │
│   │   Consumer   │ ────────────────> │   Producer   │          │
│   │    :8082     │  producer:8081    │    :8081     │          │
│   └──────────────┘                   └──────┬───────┘          │
│                                             │ JDBC (db:3306)   │
│                                             v                  │
│                                      ┌──────────────┐          │
│                                      │    MySQL     │          │
│                                      │    :3306     │          │
│                                      └──────┬───────┘          │
│                                             │                  │
│                                      ┌──────v───────┐          │
│                                      │ volume       │          │
│                                      │ mysql-data   │          │
│                                      └──────────────┘          │
└────────────────────────────────────────────────────────────────┘
```

Percorso di una richiesta:

```
Browser → Consumer (Controller → ProdottoApiClient) → HTTP → Producer (Controller → Service → Repository) → MySQL
```

Il Producer è organizzato a livelli: il **Controller** gestisce HTTP e parametri, il **Service** contiene la logica applicativa, il **Repository** (`JpaRepository<Prodotto, Long>`) accede al database. L'Entity JPA non viene esposta: il controller riceve `ProdottoRequest` e restituisce `ProdottoResponse`. Il Consumer usa un proprio `ProdottoDTO`.

## Struttura del repository

```
.
├── compose.yaml            # sviluppo: build locale delle immagini
├── compose.release.yaml    # rilascio: immagini scaricate da Docker Hub
├── db/
│   └── init.sql            # tabella prodotti + 20 prodotti di prova
├── api.http                # richieste di test della REST API
├── .env.example            # modello delle credenziali (copiare in .env)
├── producer/
│   ├── Dockerfile
│   ├── pom.xml
│   ├── src/
│   └── README.md
└── consumer/
    ├── Dockerfile
    ├── pom.xml
    ├── src/
    └── README.md
```

## Tecnologie utilizzate

- Java 21
- Spring Boot 4 (Spring Web, Spring Data JPA / Hibernate, Thymeleaf)
- RestClient (client HTTP del Consumer)
- Lombok
- MySQL 8.0
- Maven
- Docker e Docker Compose
- Docker Hub

## Requisiti

Per l'esecuzione con Docker (consigliata):

- Docker Desktop oppure Docker Engine con Docker Compose v2

Per l'esecuzione in locale senza Docker:

- Java 21
- Maven 3.9+
- MySQL 8

```bash
docker --version
docker compose version
java --version
mvn --version
```

## Configurazione MySQL

Database: `esercitazione_api`, tabella: `prodotti`.

| Campo | Tipo |
|---|---|
| id | BIGINT AUTO_INCREMENT PK |
| nome | VARCHAR(200) |
| descrizione | TEXT |
| prezzo | DECIMAL(10,2) |
| categoria | VARCHAR(100) |
| quantita | INT |
| data_creazione | TIMESTAMP |

Lo script [`db/init.sql`](db/init.sql) crea il database (charset `utf8mb4`, collation `utf8mb4_unicode_ci`), la tabella e i 20 prodotti di prova.

- **Con Docker** lo script viene montato in `/docker-entrypoint-initdb.d/` ed eseguito automaticamente al primo avvio, cioè quando il volume è vuoto.
- **In locale**: `mysql -u root -p < db/init.sql`, poi verificare con `SELECT * FROM prodotti;`.

## Configurazione della Producer

`producer/src/main/resources/application.properties`:

```properties
spring.application.name=product-api
server.port=8081

spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:mysql://localhost:3306/esercitazione_api}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:root}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:root}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

| Variabile d'ambiente | Default (locale) | Valore in Docker |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://localhost:3306/esercitazione_api` | `jdbc:mysql://db:3306/esercitazione_api?allowPublicKeyRetrieval=true` |
| `SPRING_DATASOURCE_USERNAME` | `root` | `root` |
| `SPRING_DATASOURCE_PASSWORD` | `root` | `root` |
| `SPRING_SECURITY_USER_NAME` | — | `API_USERNAME` dal file `.env` |
| `SPRING_SECURITY_USER_PASSWORD` | — | `API_PASSWORD` dal file `.env` |

### Credenziali delle API protette

`POST`, `PUT` e `DELETE` richiedono l'autenticazione HTTP Basic. Le credenziali non sono scritte nel codice né nel repository: Docker Compose le legge dal file `.env`, escluso da git, e le passa al Producer come variabili d'ambiente. Il repository contiene solo il modello [`.env.example`](.env.example):

```bash
cp .env.example .env    # poi modificare API_USERNAME e API_PASSWORD
```

```dotenv
API_USERNAME=admin
API_PASSWORD=cambiami
```

Senza il file `.env`, `docker compose up` si ferma con il messaggio `definire API_USERNAME nel file .env`.

In `application.properties` non serve nessuna riga per le credenziali: Spring Boot collega automaticamente le variabili `SPRING_SECURITY_USER_NAME` e `SPRING_SECURITY_USER_PASSWORD` alle proprietà `spring.security.user.name` e `spring.security.user.password`. Per l'esecuzione in locale senza Docker basta impostare le stesse due variabili d'ambiente prima di `mvn spring-boot:run`.

## Configurazione della Consumer

`consumer/src/main/resources/application.properties`:

```properties
spring.application.name=product-client
server.port=8082

api.base-url=${PRODUCER_URL:http://localhost:8081}/api
```

| Variabile d'ambiente | Default (locale) | Valore in Docker |
|---|---|---|
| `PRODUCER_URL` | `http://localhost:8081` | `http://producer:8081` |

L'URL del Producer non è scritto nel codice Java: in Docker `localhost` indicherebbe il container del Consumer stesso, quindi si usa il nome del servizio `producer`.

## Avvio

### Con Docker Compose (sviluppo)

Dalla directory principale:

```bash
cp .env.example .env             # solo la prima volta: credenziali delle API protette
docker compose up --build        # in primo piano
docker compose up --build -d     # in background
```

Docker crea la rete, il volume e le immagini `producer-api:1.0` e `consumer-web:1.0`, quindi avvia i tre container. Il Producer parte solo quando MySQL risulta `healthy`.

Il Dockerfile usa una **build multi-stage**: il primo stage compila il progetto con Maven all'interno del container (`maven:3.9-eclipse-temurin-21`), il secondo esegue il JAR su `eclipse-temurin:21-jre`. Per questo non serve lanciare prima `mvn clean package` in locale.

```bash
docker ps
```

Container attesi: `esercitazione-db`, `esercitazione-producer`, `esercitazione-consumer`.

Verifica:

- Producer: <http://localhost:8081/api/prodotti>
- Consumer: <http://localhost:8082/prodotti>

### In locale senza Docker

1. Avviare MySQL ed eseguire `db/init.sql`.
2. Avviare il Producer:
   ```bash
   cd producer
   mvn spring-boot:run
   ```
3. Avviare il Consumer (in un altro terminale):
   ```bash
   cd consumer
   mvn spring-boot:run
   ```
4. Aprire <http://localhost:8082/prodotti>.

## REST API (Producer)

Base URL: `http://localhost:8081/api/prodotti`

| Metodo | Endpoint | Descrizione | Risposte |
|---|---|---|---|
| GET | `/api/prodotti` | Elenco prodotti (con filtri opzionali) | 200 |
| GET | `/api/prodotti/{id}` | Dettaglio prodotto | 200, 404 |
| POST | `/api/prodotti` | Creazione prodotto (autenticazione) | 201, 400, 401 |
| PUT | `/api/prodotti/{id}` | Aggiornamento prodotto (autenticazione) | 200, 400, 401, 404 |
| DELETE | `/api/prodotti/{id}` | Eliminazione prodotto (autenticazione) | 204, 401, 404 |

`GET` è pubblico. `POST`, `PUT` e `DELETE` richiedono HTTP Basic con le credenziali definite nel file `.env`.

Parametri opzionali di `GET /api/prodotti`, combinabili tra loro:

| Parametro | Esempio | Effetto |
|---|---|---|
| `categoria` | `?categoria=Informatica` | prodotti della categoria |
| `nome` | `?nome=laptop` | nome che contiene il testo, senza distinzione maiuscole/minuscole |
| `sort` | `?sort=prezzo` | ordinamento per prezzo |
| `direction` | `?sort=prezzo&direction=desc` | `asc` (default) o `desc` |

Esempi di combinazione:

```
GET /api/prodotti?categoria=Accessori&sort=prezzo&direction=asc
GET /api/prodotti?nome=pro&sort=prezzo&direction=desc
```

Body di `POST` e `PUT`:

```json
{
  "nome": "Notebook Gaming",
  "descrizione": "Notebook ad alte prestazioni",
  "prezzo": 1599.90,
  "categoria": "Informatica",
  "quantita": 8
}
```

Formato degli errori, gestiti in modo centralizzato con `@RestControllerAdvice`:

```json
{
  "status": 404,
  "message": "Prodotto non trovato",
  "timestamp": "2026-09-28T18:30:00"
}
```

### Test della REST API

Il file [`api.http`](api.http) contiene tutte le richieste di test (lettura, filtri, ordinamento, creazione, aggiornamento, eliminazione e casi di errore 400/404). Si apre con il REST Client di IntelliJ IDEA o di Visual Studio Code.

## Interfaccia web (Consumer)

| Pagina | Descrizione |
|---|---|
| `/prodotti` | tabella prodotti (ID, nome, categoria, prezzo, quantità) con ricerca per nome e categoria |
| `/prodotti/{id}` | dettaglio del prodotto |

Se il Producer non è raggiungibile la pagina mostra il messaggio *"Impossibile recuperare i prodotti. Il servizio API non è attualmente disponibile."*. Gli errori restituiti dalla REST API (es. 404) vengono mostrati con un messaggio appropriato.

## Configurazione Docker

### Immagini

| Servizio | Immagine (sviluppo) | Immagine (Docker Hub) | Porta |
|---|---|---|---|
| db | `mysql:8.0` | `mysql:8.0` | 3306 |
| producer | `producer-api:1.0` | `TUO_USERNAME/producer-api:1.0` | 8081 |
| consumer | `consumer-web:1.0` | `TUO_USERNAME/consumer-web:1.0` | 8082 |

### Rete

Compose crea automaticamente la rete `esercitazione-api_default`, a cui sono collegati tutti e tre i container. All'interno della rete ogni servizio è raggiungibile tramite il proprio nome, risolto dal DNS interno di Docker:

- il Consumer chiama il Producer su `producer:8081`;
- il Producer si collega a MySQL su `db:3306`;
- il Consumer non usa MySQL.

```bash
docker network ls
docker network inspect esercitazione-api_default
```

### Volume

I dati di MySQL sono salvati nel volume `esercitazione-api_mysql-data`, montato su `/var/lib/mysql`:

```yaml
volumes:
  - mysql-data:/var/lib/mysql
```

Il container può essere eliminato e ricreato senza perdere i dati, che restano nel volume:

```bash
docker volume ls
docker compose down    # rimuove i container, il volume resta
docker compose up -d   # i dati inseriti in precedenza sono ancora presenti
```

`docker compose down -v` elimina anche il volume: al riavvio successivo il database viene ricreato da `db/init.sql`.

## Comandi

### Build

```bash
docker compose build                     # costruisce le immagini producer-api:1.0 e consumer-web:1.0
docker images
```

In alternativa, per ogni applicazione:

```bash
cd producer && docker build -t producer-api:1.0 .
cd consumer && docker build -t consumer-web:1.0 .
```

### Avvio

```bash
docker compose up --build    # build e avvio
docker compose up -d         # avvio in background
docker compose start         # riavvio di container fermati
```

### Stop

```bash
docker compose stop          # ferma i container
docker compose down          # ferma e rimuove i container (il volume resta)
```

### Log

```bash
docker compose logs
docker compose logs producer
docker compose logs consumer
docker compose logs -f producer
```

## Repository Docker Hub

- Producer: `https://hub.docker.com/r/TUO_USERNAME/producer-api`
- Consumer: `https://hub.docker.com/r/TUO_USERNAME/consumer-web`

## Procedura di rilascio

### 1. Pubblicazione delle immagini (macchina di sviluppo)

```bash
docker compose build

docker login

docker tag producer-api:1.0 TUO_USERNAME/producer-api:1.0
docker tag consumer-web:1.0 TUO_USERNAME/consumer-web:1.0

docker push TUO_USERNAME/producer-api:1.0
docker push TUO_USERNAME/consumer-web:1.0
```

### 2. Rilascio su una nuova macchina

[`compose.release.yaml`](compose.release.yaml) è identico a `compose.yaml`, ma al posto di `build:` usa le immagini pubblicate su Docker Hub (`image: TUO_USERNAME/...:1.0`). Sulla macchina di destinazione non serve il codice sorgente Java: bastano Docker, Docker Compose, `compose.release.yaml`, il file `.env` con le credenziali e `db/init.sql` per i dati iniziali.

```bash
cp .env.example .env
docker compose -f compose.release.yaml pull     # scarica producer-api:1.0, consumer-web:1.0, mysql:8.0
docker compose -f compose.release.yaml up -d
docker ps
```

Verifica: <http://localhost:8082/prodotti>

## Funzionalità implementate

Livello base:

- [x] CRUD completo dei prodotti sulla REST API del Producer

Livello intermedio:

- [x] filtro per categoria, ricerca per nome, ordinamento per prezzo
- [x] combinazione dei parametri tramite `@RequestParam`
- [x] query Spring Data JPA dedicate
- [x] gestione centralizzata degli errori (404, 400) con `@RestControllerAdvice`

Livello avanzato (Consumer):

- [x] client HTTP con RestClient e DTO dedicato
- [x] pagine Thymeleaf: tabella prodotti, dettaglio e ricerca
- [x] gestione degli errori della REST API e del Producer non raggiungibile

Livello esperto e evolutiva Docker:

- [x] autenticazione con Spring Security su POST/PUT/DELETE, con credenziali fornite tramite variabili d'ambiente
- [ ] cache locale nel Consumer con Spring Cache
- [x] DTO dedicati (`ProdottoRequest` / `ProdottoResponse`)
- [x] containerizzazione con Dockerfile multi-stage e Docker Compose
- [x] inizializzazione automatica del database e attesa di MySQL tramite healthcheck
- [ ] health check del Producer con Spring Boot Actuator
- [ ] pubblicazione delle immagini su Docker Hub
