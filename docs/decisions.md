# Decision Log

Significant decisions for this project. Newest at the bottom. Entries are never deleted.

---

## 001 — Consumer authenticates to the Producer with a shared HTTP Basic service account
- **Date:** 2026-10-07
- **Status:** Accepted
- **Context:** The Producer (product-api) protects POST, PUT and DELETE on `/products` with Spring Security HTTP Basic; GET endpoints are public. The Consumer needs to create, update and delete products.
- **Decision:** The Consumer reads `API_USERNAME` / `API_PASSWORD` from the environment (`api.username` / `api.password`) and sends them as HTTP Basic credentials only on write calls. The Consumer UI itself has no login.
- **Alternatives:** Per-user login on the Consumer forwarded to the Producer — out of scope for the exercise and the Producer has a single in-memory user.
- **Consequences:** Anyone who can reach the Consumer can modify products; acceptable for a local exercise, to revisit before any real deployment.
