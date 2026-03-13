// No platform-specific host needed by default; use EXPO_PUBLIC_API_URL to override when developing locally.

// Base URL for backend API. Prefer EXPO_PUBLIC_API_URL at build time.
const fromEnv = process.env.EXPO_PUBLIC_API_URL?.replace(/\/+$/, "");

// TODO: Configurar envs no script de build

// Default to the production API domain; keep local dev as optional override via env.
const defaultBase = "https://native.videos.vitorweirich.com";

export const API_URL = fromEnv || defaultBase;

// Base URL for frontend web. Used for session transfer links.
const webFromEnv = process.env.EXPO_PUBLIC_WEB_URL?.replace(/\/+$/, "");
const webDefaultBase = "https://videos.vitorweirich.com";

export const WEB_URL = webFromEnv || webDefaultBase;

export const jsonHeaders: Record<string, string> = {
  "Content-Type": "application/json",
  // Request non-HttpOnly cookies so tokens come in the response body
  "X-Http-Only": "false",
};
