# Authorization Service

Authorization Service follows the Golden Platform package layout: `core`, `feature`, `foundation`, and `entrypoint`. Its build inherits `platform-parent` and imports `platform-libraries-bom` at version `1.0.0`.

## Platform Libraries

- `platform-redis` stores token sessions and authorization groups. Token sessions expire at the JWT expiry; authorization entries use a 40-minute TTL, with a 30-second refresh guard.
- `platform-messaging` resolves the platform error response format. Message definitions are in `src/main/resources/META-INF/platform-messages`.
- `platform-observability` provides the platform JSON logging defaults.
- `platform-message-queue` publishes optional authorization repair requests. Set `AUTHORIZATION_REPAIR_ENABLED=true` and configure AWS credentials and `AUTHORIZATION_REPAIR_QUEUE` to enable publication.

Kafka remains the existing cache refresh channel. `AUTHORIZATION_REFRESH_URL` configures its HTTP publisher endpoint.

## Authorization view fallback

The cache lookup falls back to the Workspace authorization view when Redis has no usable value or is unavailable. Enable it with `AUTHORIZATION_VIEW_FALLBACK_ENABLED=true`, then configure:

- `WORKSPACE_AUTHORIZATION_JDBC_URL`
- `WORKSPACE_AUTHORIZATION_JDBC_USERNAME`
- `WORKSPACE_AUTHORIZATION_JDBC_PASSWORD`
- `WORKSPACE_AUTHORIZATION_VIEW_QUERY`

The query must accept the logical authorization cache key as its single positional `?` parameter and return zero or one row, with the required group in column 1. A successful empty result is treated as a denial (403); a database/query failure is treated as unavailable (503). The database account should have read-only access to the view. Redis is repopulated from a successful view result when available.
