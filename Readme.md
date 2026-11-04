# Burmalda local startup guide

This repository runs a Telegram bot system consisting of:

- **MySQL** - application database.
- **phpMyAdmin** - optional database administration UI.
- **bot-manager** - creates and manages the generated Telegram bots.
- **bot-runtime** - receives Telegram webhooks and requests video generation from the AI provider.

The recommended local setup uses `docker-compose-local.yml`.

## Prerequisites

Install and start:

- Docker Desktop with Docker Compose
- A Telegram bot token created with [@BotFather](https://t.me/BotFather)
- An API key for the configured AI provider
- A public HTTPS URL if Telegram webhooks and AI callbacks must reach a local deployment. Use [ngrok](https://ngrok.com/) to create a tunnel.

Run all commands from the repository root.

## Configure environment variables

Create the Compose environment file from the template:

```powershell
Copy-Item example.env .env
```

Edit `.env` and replace every placeholder value. `.env` is ignored by Git and must not be committed.

| Variable | Description |
| --- | --- |
| `SQL_LINK` | JDBC URL used by the Java services. In Compose, use `jdbc:mysql://mysql:3306/burmalda` (not `localhost`). Add connection parameters if required by the local MySQL driver. |
| `SQL_USER` | MySQL application username. |
| `SQL_PASSWORD` | MySQL application password. |
| `SQL_ROOT_PASSWORD` | MySQL root password used by the container health check and administration. |
| `MANAGER_BOT_TOKEN` | Token of the manager Telegram bot. |
| `MANAGER_BOT_USERNAME` | Username of the manager bot, normally without the `@` prefix. |
| `MANAGED_BOT_ADJECTIVE` | Comma-separated adjectives used when naming managed bots. |
| `MANAGED_BOT_NOUN` | Comma-separated nouns used when naming managed bots. |
| `RUNTIME_WEBHOOK_BASE_URL` | Public HTTPS base URL for runtime Telegram webhooks, including the `/telegram/webhook/` path and a trailing slash. |
| `AI_API_BASE_URL` | Base URL of the AI API. |
| `AI_API_KEY` | Secret API key for the AI provider. |
| `AI_CALLBACK_URL` | Public HTTPS URL where the AI provider sends completed-task callbacks, normally ending in `/api/kie/callback/`. |
| `AI_STORAGE_URL` | Storage endpoint used for AI files. |
| `AI_REQUEST_PROMPT` | Default prompt sent with generation requests. Replace the template value with an appropriate application prompt. |
| `AI_REQUEST_RESOLUTION` | Default generation resolution, for example `480P`. |
| `AI_REQUEST_DURATION` | Default generation duration in seconds. |

For the local Compose network, the database URL should point to the service name:

```dotenv
SQL_LINK=jdbc:mysql://mysql:3306/burmalda
```

`RUNTIME_WEBHOOK_BASE_URL` and `AI_CALLBACK_URL` must be reachable from the Internet. If the application is behind a reverse proxy, route the public paths to port `8080` of `bot-runtime`:

- `POST /telegram/webhook/{botId}`
- `POST /api/kie/callback/`

## Start the application

Build the two Java services and start all containers:

```powershell
docker compose --env-file .env -f docker-compose-local.yml up -d --build
```

MySQL is started first and its health check must pass before the Java services start. Check the service state:

```powershell
docker compose --env-file .env -f docker-compose-local.yml ps
```

Follow application logs when troubleshooting:

```powershell
docker compose --env-file .env -f docker-compose-local.yml logs -f bot-manager bot-runtime
```

The local endpoints are:

- Runtime HTTP service: `http://localhost:8080`
- phpMyAdmin: `http://localhost:8081`
- Runtime Java debug port: `localhost:5005` (bound only to loopback)
- MySQL: `localhost:3306`

phpMyAdmin uses the `mysql` Compose service as its database host. Log in with `SQL_USER`/`SQL_PASSWORD`, or use the root account and `SQL_ROOT_PASSWORD`.

## Stop and restart

Stop containers without deleting the database volume:

```powershell
docker compose --env-file .env -f docker-compose-local.yml down
```

Start them again without rebuilding:

```powershell
docker compose --env-file .env -f docker-compose-local.yml up -d
```

To remove the persisted MySQL data as well, use the following command only when a database reset is intended:

```powershell
docker compose --env-file .env -f docker-compose-local.yml down -v
```
