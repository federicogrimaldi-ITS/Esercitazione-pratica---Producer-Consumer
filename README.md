# Esercitazione pratica – Producer / Consumer

## Collaboratori

- Andrea Savoia
- Federico Grimaldi
- Luca Daniel Iosipescu 
- AI: Claude, tramite Claude Code – supporto alla pianificazione, allo sviluppo guidato dai test e alla documentazione.

## Descrizione del progetto

Il progetto implementa un catalogo di prodotti composto da due applicazioni Spring Boot:

- `product-api` è la **Producer**: espone una REST API, applica la logica di business e salva i prodotti in MySQL;
- `product-client` è la **Consumer**: offre un'interfaccia web Thymeleaf e consuma la REST API della Producer tramite HTTP.

Il Consumer non accede direttamente al database.

```text
Browser → Consumer (:8082) → HTTP/REST → Producer (:8081/api) → MySQL
```

Le operazioni di consultazione sono pubbliche, mentre creazione, modifica ed eliminazione sono protette da HTTP Basic Authentication.

## Tecnologie utilizzate

- Java 25;
- Spring Boot 4.1.1, Spring Web MVC e RestClient;
- Spring Data JPA / Hibernate;
- Spring Security con HTTP Basic;
- MySQL 8.0;
- Thymeleaf;
- Spring Cache con Caffeine;
- Lombok;
- Maven e Maven Wrapper;
- JUnit 5, AssertJ, Mockito e MockMvc;
- Docker e Docker Compose;
- Springdoc OpenAPI / Swagger UI.

## Requisiti necessari

Per Docker Compose servono Docker Desktop o Docker Engine e Docker Compose v2 (`docker compose`).

Per l'avvio locale servono JDK 25, Maven 3.9 (oppure il Maven Wrapper), MySQL 8.0 e le credenziali MySQL e HTTP Basic della Producer.

## Configurazione MySQL

Il database utilizzato è `esercitazione_api`. Copiare `.env.example` in `.env` e sostituire le password:

```dotenv
MYSQL_ROOT_PASSWORD=change-me-root
MYSQL_DATABASE=esercitazione_api
MYSQL_USER=product_api
MYSQL_PASSWORD=change-me-db
MYSQL_HOSTNAME=db
MYSQL_PORT=3306
MYSQL_DATABASE_NAME=esercitazione_api
MYSQL_USERNAME=product_api
```

Al primo avvio MySQL esegue `initDb.sql`, che crea e popola la tabella `prodotti`. Il database usa il volume Docker nominato `esercitazione-mysql-data`, montato in `/var/lib/mysql`.

Dopo una modifica — per esempio l'eliminazione del prodotto con ID `1` — la modifica rimane anche eseguendo:

```bash
docker compose down
docker compose up --build
```

Non usare `docker compose down -v` se si vogliono conservare i dati: `-v` elimina il volume e fa ripartire MySQL dai dati iniziali di `initDb.sql`.

## Configurazione della Producer

La Producer è configurata in `product-api/src/main/resources/application.yaml` e usa porta `8081` e context path `/api`:

```yaml
server:
  port: 8081
  servlet:
    context-path: /api
spring:
  datasource:
    url: jdbc:mysql://${MYSQL_HOSTNAME}:${MYSQL_PORT}/${MYSQL_DATABASE_NAME}
    username: ${MYSQL_USERNAME}
    password: ${MYSQL_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: update
```

Le credenziali HTTP Basic sono definite in `.env`:

```dotenv
API_USERNAME=admin
API_PASSWORD=change-me-api
```

La Producer è raggiungibile su `http://localhost:8081/api`; Swagger UI è disponibile su `http://localhost:8081/api/swagger-ui.html`.

## Configurazione della Consumer

La Consumer è configurata in `product-client/src/main/resources/application.yaml`:

```yaml
server:
  port: 8082
api:
  base-url: ${API_BASE_URL:http://localhost:8081/api}
  categories: Accessori,Audio,Informatica,Mobile,Networking,Storage,Ufficio,Wearable
  username: ${API_USERNAME:}
  password: ${API_PASSWORD:}
```

Con Docker Compose usa `http://backend:8081/api`; in locale usa `http://localhost:8081/api`. La Consumer espone l'elenco su `/prodotti`, il dettaglio su `/prodotti/{id}`, i form di creazione/modifica/eliminazione e la gestione degli errori della Producer.

## Istruzioni per l'avvio

### Avvio completo con Docker Compose

Dalla cartella principale del progetto:

```powershell
Copy-Item .env.example .env
```

Modificare le password nel file `.env`, quindi eseguire:

```bash
docker compose up --build
```

Servizi disponibili:

| Servizio | Container | Porta | Funzione |
|---|---|---:|---|
| MySQL | `esercitazione-db` | 3306 interna | Database della Producer |
| Producer | `esercitazione-backend` | 8081 | REST API su `/api` |
| Consumer | `esercitazione-frontend` | 8082 | Interfaccia web |

Aprire [http://localhost:8082/prodotti](http://localhost:8082/prodotti).

Per fermare i container mantenendo i dati usare `docker compose down`.

### Avvio locale

1. Avviare MySQL e creare il database `esercitazione_api`.
2. Impostare le variabili della Producer e avviare `product-api`:

   ```powershell
   cd product-api
   $env:MYSQL_HOSTNAME="localhost"
   $env:MYSQL_PORT="3306"
   $env:MYSQL_DATABASE_NAME="esercitazione_api"
   $env:MYSQL_USERNAME="product_api"
   $env:MYSQL_PASSWORD="la-password-del-database"
   $env:API_USERNAME="admin"
   $env:API_PASSWORD="la-password-api"
   .\mvnw.cmd spring-boot:run
   ```

3. In un secondo terminale avviare `product-client`:

   ```powershell
   cd product-client
   $env:API_BASE_URL="http://localhost:8081/api"
   $env:API_USERNAME="admin"
   $env:API_PASSWORD="la-password-api"
   .\mvnw.cmd spring-boot:run
   ```

4. Aprire [http://localhost:8082/prodotti](http://localhost:8082/prodotti).

## REST API disponibili

Base URL: `http://localhost:8081/api/products`

| Metodo | Endpoint | Autenticazione | Descrizione |
|---|---|---|---|
| `GET` | `/products` | No | Restituisce tutti i prodotti. |
| `GET` | `/products/{id}` | No | Restituisce il prodotto con l'ID indicato. |
| `GET` | `/products/category/{category}` | No | Filtra per categoria. |
| `GET` | `/products/search?name={testo}` | No | Cerca prodotti per nome. |
| `GET` | `/products/sort?direction=ASC` | No | Ordina per prezzo (`ASC` o `DESC`). |
| `POST` | `/products` | HTTP Basic | Crea un prodotto. |
| `PUT` | `/products/{id}` | HTTP Basic | Modifica un prodotto. |
| `DELETE` | `/products/{id}` | HTTP Basic | Elimina un prodotto. |

Categorie ammesse: `Accessori`, `Audio`, `Informatica`, `Mobile`, `Networking`, `Storage`, `Ufficio`, `Wearable`.

Esempio di creazione:

```bash
curl -u admin:change-me-api -X POST http://localhost:8081/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Mouse wireless","description":"Mouse Bluetooth","price":19.90,"category":"Accessori","quantity":10}'
```

Esempio di eliminazione del prodotto con ID `1`:

```bash
curl -u admin:change-me-api -X DELETE http://localhost:8081/api/products/1
```
