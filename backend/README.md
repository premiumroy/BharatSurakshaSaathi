# Suraksha Saathi — optional backend proxy

The Android app is offline by default. If the user switches **Online mode** on
and confirms, the app sends **only the single message text they chose** to the
server address they configured, to get a better plain-language explanation.

This folder is a minimal, self-hosted proxy so that:

- the LLM provider **API key lives on your server**, never in the app;
- you control **rate limiting** and can cap request size;
- you can log metadata without logging user message text.

## Run

```bash
cd backend
npm install
cp .env.example .env      # then edit .env and set PROVIDER_API_KEY
npm start                 # listens on http://0.0.0.0:8080
```

Then in the app: **Settings → Online mode → Server address** and enter your
endpoint, e.g. `https://your-host.example/explain`.

## API

`POST /explain`

```json
{ "message": "Dear customer, your KYC is expiring…", "language": "en" }
```

Response:

```json
{ "explanation": "This looks like a fake-KYC scam…" }
```

`GET /health` → `{ "ok": true }`

## Notes

- Rate limit defaults to 30 requests / minute per IP (see `RATE_MAX`, `RATE_WINDOW`).
- Request body is capped at 16 KB, and the message at `MAX_MESSAGE_CHARS` (default 2000).
- The message body is intentionally **not** written to logs.
- Provider is configurable via `PROVIDER_URL` / `PROVIDER_MODEL`, so any
  OpenAI-compatible endpoint works.
- Put this behind HTTPS and your own auth/quota if you open it publicly.
