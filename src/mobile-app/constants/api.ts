// No platform-specific host needed by default; use EXPO_PUBLIC_API_URL to override when developing locally.

// Base URL for backend API. Prefer EXPO_PUBLIC_API_URL at build time.
// const fromEnv = process.env.EXPO_PUBLIC_API_URL?.replace(/\/+$/, "");
const fromEnv = "http://10.0.2.2:8080";

// TODO: Configurar EXPO_PUBLIC_API_URL no script de build

// Default to the production API domain; keep local dev as optional override via env.
const defaultBase = "https://native.videos.vitorweirich.com";

export const API_URL = fromEnv || defaultBase;

export const jsonHeaders: Record<string, string> = {
  "Content-Type": "application/json",
  // Request non-HttpOnly cookies so tokens come in the response body
  "X-Http-Only": "false",
};
