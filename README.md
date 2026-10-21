# Esercitazione-pratica---Producer-Consumer

- `product-api` – Producer Application: REST API dei prodotti su MySQL
- `product-client` – Consumer Application: interfaccia web che usa la REST API della Producer
  (vedere [`product-client/README.md`](product-client/README.md))

```
Browser → frontend (:8082) → backend (:8081/api) → db (MySQL)
```

## Avvio con Docker Compose

Requisiti: Docker con il plugin Compose.

1. Creare il file `.env` partendo dall'esempio e sostituire le password `change-me`:

   ```bash
   cp .env.example .env
   ```

2. Costruire e avviare i container:

   ```bash
   docker compose up --build
   ```

3. Aprire `http://localhost:8082/prodotti`.

| Servizio | Container | Porta | Descrizione |
|----------|-----------|-------|-------------|
| `db` | `esercitazione-db` | – | MySQL 8.0; al primo avvio crea la tabella e carica i dati di `initDb.sql` |
| `backend` | `esercitazione-backend` | 8081 | Producer (`product-api`), REST API su `http://localhost:8081/api` |
| `frontend` | `esercitazione-frontend` | 8082 | Consumer (`product-client`), pagine web su `http://localhost:8082` |

- Il frontend raggiunge il backend sulla rete interna di Compose (`API_BASE_URL=http://backend:8081/api`).
- `API_USERNAME` e `API_PASSWORD` sono l'utente HTTP Basic della Producer e vengono usati
  dal Consumer per creare, modificare ed eliminare i prodotti.
- La build dell'immagine del frontend esegue i test del Consumer.
- Il backend parte quando il database è pronto; il frontend parte subito dopo il backend:
  nei primi secondi le pagine possono mostrare "servizio non disponibile", finché la
  Producer non ha terminato l'avvio.
- `docker compose down` ferma i container; `docker compose down -v` elimina anche il volume
  del database, che al successivo avvio viene ricreato con i dati di `initDb.sql`.
