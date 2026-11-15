# Burmalda local startup guide

This repository runs a Telegram bot system consisting of:

- **MySQL** - application database.
- **phpMyAdmin** - optional database administration UI.
- **bot-manager** - creates and manages the generated Telegram bots.
- **bot-runtime** - receives Telegram webhooks, processes user conversational flows, handles Crypto Pay payments, and requests video generation from the AI provider.

The recommended local setup uses `docker-compose-local.yml`.

---

## Prerequisites

Install and prepare:

- Docker Desktop with Docker Compose.
- A Telegram bot token created with [@BotFather](https://t.me/BotFather).
- An API key for the configured AI provider ([Kie.ai](https://api.kie.ai)).
- A Crypto Pay API token from [@CryptoBot](https://t.me/CryptoBot) (mainnet) or [@CryptoTestnetBot](https://t.me/CryptoTestnetBot) (testnet).
- A public HTTPS URL if Telegram webhooks, AI callbacks, and payment webhooks must reach a local deployment. Use [ngrok](https://ngrok.com/) or another reverse proxy / tunnel.

Run all commands from the repository root.

---

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
| `AI_API_BASE_URL` | Base URL of the AI API (e.g. `https://api.kie.ai`). |
| `AI_API_KEY` | Secret API key for the AI provider. |
| `AI_CALLBACK_URL` | Public HTTPS URL where the AI provider sends completed-task callbacks, normally ending in `/api/kie/callback/`. |
| `AI_STORAGE_URL` | Storage endpoint used for AI files. |
| `AI_REQUEST_PROMPT` | Default prompt sent with generation requests. |
| `CRYPTO_PAY_TOKEN` | Secret API token for Crypto Pay, generated via `@CryptoBot` or `@CryptoTestnetBot`. |
| `CRYPTO_PAY_BASE_URL` | Base REST API URL for Crypto Pay (`https://testnet-pay.crypt.bot/api` for testnet, or `https://pay.crypt.bot/api` for mainnet). |

For the local Compose network, the database URL should point to the service name:

```dotenv
SQL_LINK=jdbc:mysql://mysql:3306/burmalda
```

---

## Public Webhook Endpoints & Reverse Proxy

The application exposes three public HTTP endpoints on `bot-runtime` (port `8080`) that must be reachable from the Internet via your tunnel (e.g., ngrok) or reverse proxy:

1. **`POST /telegram/webhook/{botId}`**
   - Receives inbound user interactions and message updates from the Telegram Bot API for each managed bot instance.
2. **`POST /api/kie/callback/`**
   - Receives asynchronous status callbacks from the Kie.ai video generation backend when video processing completes.
3. **`POST /crypto-pay/webhook`**
   - Receives real-time payment completion notifications from Crypto Pay (@CryptoBot).

---

## Crypto Pay (@CryptoBot) Integration & Setup

The bot integrates with [Crypto Pay API](https://help.cr.bot/en/articles/10279948-crypto-pay-api) to accept cryptocurrency payments (such as USDT, TON, BTC, etc.) for token balance top-ups.

### 1. Creating your Crypto Pay Application
1. Open [@CryptoBot](https://t.me/CryptoBot) (for production) or [@CryptoTestnetBot](https://t.me/CryptoTestnetBot) (for testing) in Telegram.
2. Send the `/pay` command.
3. Select **Crypto Pay** -> **Create App**.
4. Set the app name and description.
5. Copy the generated **API Token** and paste it into your `.env` as `CRYPTO_PAY_TOKEN`.

### 2. Enabling Webhooks in @CryptoBot
Unlike Telegram's `setWebhook` method, the Crypto Pay REST API does not have an endpoint to register webhooks programmatically. Webhooks must be configured manually inside Telegram:
1. In `@CryptoBot` / `@CryptoTestnetBot`, send `/pay`.
2. Navigate to **Crypto Pay** -> **My Apps** and select your application.
3. Select **Webhooks** and tap **Enable Webhooks**.
4. Enter your public webhook URL:
   ```text
   https://<your-public-domain>/crypto-pay/webhook
   ```

### 3. Webhook Signature Verification
For security, every incoming webhook request includes a `crypto-pay-api-signature` header:
- The signature is an HMAC-SHA256 hex digest of the raw request body.
- The secret key used for HMAC calculation is `SHA-256(CRYPTO_PAY_TOKEN)`.
- `CryptoPayWebhookController` and `CryptoPaySignatureValidator` automatically verify this signature on the byte-exact payload before processing. Unauthorized requests return HTTP 401.

### 4. Direct Payment Verification Fallback
If webhook delivery is delayed or the user completes payment without waiting for the webhook callback, the invoice message in Telegram includes a **"🔄 Проверить статус оплаты"** button. Clicking this triggers an active poll via `CryptoPayClient.getInvoices` to instantly verify invoice status and credit tokens.

---

## Token Pricing & Hot-Reload (`pricing.json`)

Top-up token pricing, package buttons, and currency settings are defined in `pricing.json`:

```json
{
  "asset": "USDT",
  "basePricePerToken": 0.01,
  "minTokens": 10,
  "maxTokens": 50000,
  "invoiceExpiresInSeconds": 1800,
  "fastButtons": [
    { "tokens": 100, "discountPercent": 0 },
    { "tokens": 500, "discountPercent": 10 },
    { "tokens": 1500, "discountPercent": 20 }
  ]
}
```

### Key Properties:
- `asset`: Currency symbol used for invoices (e.g. `USDT`, `TON`).
- `basePricePerToken`: Price per token in the specified asset.
- `minTokens` / `maxTokens`: Allowed boundaries when users type in a custom amount.
- `invoiceExpiresInSeconds`: Invoice lifetime before automatic expiration (default: 30 minutes).
- `fastButtons`: Quick-selection package buttons with optional discount percentages.

### Live Hot-Reloading:
- **Location**: `pricing.json` is loaded from the runtime directory (e.g., `./burmalda-bot-runtime/pricing.json`) or falls back to classpath `pricing.json`.
- `TokenPricingService` monitors the file's modification timestamp every 5 seconds. When the file is edited on the host or inside the container, the new pricing and packages take effect immediately without needing to restart the application.

---

## Declarative Keyboards (`keyboards.json`)

Inline keyboards and navigation buttons are defined declaratively in `burmalda-bot-runtime/src/main/resources/keyboards.json`:
- **Stage mapping**: Keyboards are mapped directly to user flow stages (e.g. `MAIN_MENU`, `PROFILE`, `TOP_UP_PACKAGES`, `TOP_UP_CONFIRMATION`, `TOP_UP_INVOICE`).
- **Dynamic Templating**: Button labels and callback data support placeholder syntax (e.g., `{price}`, `{asset}`, `{pay_url}`, `{order_uuid}`).
- **Conditional Rows**: Buttons with a `"condition": "key"` attribute are only rendered when the corresponding key is present and non-empty in the template context parameters.

---

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

---

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
