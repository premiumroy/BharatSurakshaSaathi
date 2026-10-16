// Minimal backend proxy for Bharat Suraksha Saathi.
//
// The Android app NEVER talks to an LLM provider directly and never embeds an
// API key. It only POSTs a single message text to THIS server (which you run),
// and this server holds the provider key and rate-limits abuse.
//
// Run:
//   npm install
//   cp .env.example .env      # then put your real key in .env
//   npm start
//
// Endpoint:
//   POST /explain  { "message": "...", "language": "en" | "hi" }
//   -> 200 { "explanation": "..." }

import Fastify from 'fastify';
import rateLimit from '@fastify/rate-limit';

const PORT = Number(process.env.PORT || 8080);
const HOST = process.env.HOST || '0.0.0.0';

const PROVIDER_URL = process.env.PROVIDER_URL || 'https://api.openai.com/v1/chat/completions';
const PROVIDER_MODEL = process.env.PROVIDER_MODEL || 'gpt-4o-mini';
const PROVIDER_API_KEY = process.env.PROVIDER_API_KEY || '';

const MAX_MESSAGE_CHARS = Number(process.env.MAX_MESSAGE_CHARS || 2000);

const app = Fastify({ logger: true, bodyLimit: 16 * 1024 });

// Basic abuse protection. Tune for your expected traffic.
await app.register(rateLimit, {
  max: Number(process.env.RATE_MAX || 30),          // requests
  timeWindow: process.env.RATE_WINDOW || '1 minute',
  allowList: [],
});

const SYSTEM_PROMPT = `You explain scam risk in simple, calm language for an Indian audience.
You are given one message a user received (an SMS/WhatsApp text). In 3-5 short sentences:
- say what kind of scam it most resembles (if any),
- say clearly what the person should NOT do,
- say what to do instead.
Never claim certainty and never invent facts about the sender. Do not output phone numbers or links.`;

function buildUserPrompt(message, language) {
  const lang = language === 'hi' ? 'Hindi' : 'English';
  return `Explain this message in ${lang}. Keep it under 90 words.\n\nMESSAGE:\n"""${message}"""`;
}

app.get('/health', async () => ({ ok: true }));

app.post('/explain', {
  schema: {
    body: {
      type: 'object',
      required: ['message'],
      properties: {
        message: { type: 'string', minLength: 1, maxLength: MAX_MESSAGE_CHARS },
        language: { type: 'string', enum: ['en', 'hi'] },
      },
    },
  },
}, async (request, reply) => {
  if (!PROVIDER_API_KEY) {
    reply.code(503);
    return { error: 'server_not_configured', detail: 'PROVIDER_API_KEY is not set' };
  }

  const { message, language = 'en' } = request.body;

  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), 20_000);

  try {
    const response = await fetch(PROVIDER_URL, {
      method: 'POST',
      signal: controller.signal,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${PROVIDER_API_KEY}`,
      },
      body: JSON.stringify({
        model: PROVIDER_MODEL,
        temperature: 0.2,
        max_tokens: 220,
        messages: [
          { role: 'system', content: SYSTEM_PROMPT },
          { role: 'user', content: buildUserPrompt(message, language) },
        ],
      }),
    });

    if (!response.ok) {
      request.log.warn({ status: response.status }, 'provider error');
      reply.code(502);
      return { error: 'provider_error' };
    }

    const data = await response.json();
    const explanation = data?.choices?.[0]?.message?.content?.trim();
    if (!explanation) {
      reply.code(502);
      return { error: 'empty_explanation' };
    }

    // Do not log the message body — keep user text out of your logs.
    request.log.info({ chars: message.length }, 'explained');
    return { explanation };
  } catch (err) {
    request.log.warn({ err: err?.name }, 'provider request failed');
    reply.code(504);
    return { error: 'timeout_or_network' };
  } finally {
    clearTimeout(timeout);
  }
});

app.listen({ port: PORT, host: HOST }).catch((err) => {
  app.log.error(err);
  process.exit(1);
});
