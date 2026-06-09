const BASE_URL = (import.meta.env.VITE_API_URL as string) || 'http://localhost:8080';
const TOKEN_KEY = 'trasmum.token';

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token: string) {
  localStorage.setItem(TOKEN_KEY, token);
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY);
}

interface RequestOptions extends RequestInit {
  query?: Record<string, string | number | undefined | null>;
}

function buildUrl(path: string, query?: RequestOptions['query']): string {
  const url = new URL(BASE_URL + path);
  if (query) {
    for (const [k, v] of Object.entries(query)) {
      if (v !== undefined && v !== null && v !== '') url.searchParams.set(k, String(v));
    }
  }
  return url.toString();
}

export async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { query, headers, ...rest } = options;
  const token = getToken();
  const finalHeaders: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(headers as Record<string, string>),
  };
  if (token) finalHeaders['Authorization'] = `Bearer ${token}`;

  const res = await fetch(buildUrl(path, query), { ...rest, headers: finalHeaders });

  if (res.status === 401) {
    clearToken();
    if (!path.startsWith('/api/auth/')) {
      window.location.href = '/login';
      throw new Error('Sessão expirada.');
    }
  }

  const text = await res.text();
  const data = text ? JSON.parse(text) : null;
  if (!res.ok) {
    const msg = data?.erro || `HTTP ${res.status}`;
    throw new Error(msg);
  }
  return data as T;
}

export const http = {
  get<T>(path: string, query?: RequestOptions['query']) {
    return request<T>(path, { method: 'GET', query });
  },
  post<T>(path: string, body?: unknown) {
    return request<T>(path, { method: 'POST', body: body ? JSON.stringify(body) : undefined });
  },
};
