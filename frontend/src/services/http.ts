import { readSession } from "./session";

const API_URL = (import.meta.env.VITE_API_URL || "http://localhost:8080").replace(/\/+$/, "");

export class ApiError extends Error {
  status: number;
  fieldErrors: Record<string, string>;
  details: string[];

  constructor(status: number, message: string, fieldErrors: Record<string, string> = {}, details: string[] = []) {
    super(message);
    this.status = status;
    this.fieldErrors = fieldErrors;
    this.details = details;
  }
}

let onUnauthorized = () => { };

export function setUnauthorizedHandler(handler: () => void) {
  onUnauthorized = handler;
}

interface ErrorBody {
  message?: string;
  errors?: { field?: string; defaultMessage?: string }[];
}

async function toApiError(response: Response) {
  const body: ErrorBody = await response.json().catch(() => ({}));
  const fieldErrors: Record<string, string> = {};
  const details: string[] = [];
  for (const error of body.errors ?? []) {
    if (!error.defaultMessage) continue;
    if (error.field) fieldErrors[error.field] = error.defaultMessage;
    else details.push(error.defaultMessage);
  }
  return new ApiError(response.status, body.message || "Não foi possível concluir a operação.", fieldErrors, details);
}

async function request<T>(method: string, path: string, body?: unknown): Promise<T> {
  const token = readSession()?.token;
  const headers: Record<string, string> = { Accept: "application/json" };
  if (body !== undefined) headers["Content-Type"] = "application/json";
  if (token) headers.Authorization = `Bearer ${token}`;

  let response: Response;
  try {
    response = await fetch(`${API_URL}/api${path}`, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
      credentials: "omit",
      referrerPolicy: "no-referrer",
    });
  } catch {
    throw new ApiError(0, "Não foi possível conectar ao servidor. Verifique sua conexão e tente novamente.");
  }

  if (response.status === 401 && token) onUnauthorized();
  if (!response.ok) throw await toApiError(response);
  return response.status === 204 ? (undefined as T) : response.json();
}

export const http = {
  get: <T>(path: string, params?: Record<string, string | boolean | undefined>) => {
    const query = new URLSearchParams();
    for (const [key, value] of Object.entries(params ?? {})) {
      if (value !== undefined && value !== "") query.set(key, String(value));
    }
    const suffix = query.size ? `?${query}` : "";
    return request<T>("GET", path + suffix);
  },
  post: <T>(path: string, body?: unknown) => request<T>("POST", path, body ?? {}),
  put: <T>(path: string, body: unknown) => request<T>("PUT", path, body),
  patch: <T>(path: string) => request<T>("PATCH", path),
};
