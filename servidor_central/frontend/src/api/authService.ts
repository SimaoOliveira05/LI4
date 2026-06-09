import { http, setToken, clearToken, getToken } from './http';

interface LoginResponse {
  token: string;
  nomeUtilizador: string;
}

const USER_KEY = 'trasmum.user';

export async function login(nomeUtilizador: string, palavraPasse: string): Promise<LoginResponse> {
  const res = await http.post<LoginResponse>('/api/auth/login', { nomeUtilizador, palavraPasse });
  setToken(res.token);
  localStorage.setItem(USER_KEY, res.nomeUtilizador);
  return res;
}

export async function logout(): Promise<void> {
  try { await http.post('/api/auth/logout'); } catch { /* ignorar */ }
  clearToken();
  localStorage.removeItem(USER_KEY);
}

export async function criarCEO(nomeUtilizador: string, palavraPasse: string): Promise<void> {
  await http.post('/api/ceo', { nomeUtilizador, palavraPasse });
}

export function isAuthenticated(): boolean {
  return !!getToken();
}

export function isBootstrap(): boolean {
  return utilizadorAtual() === 'admin';
}

export function utilizadorAtual(): string | null {
  return localStorage.getItem(USER_KEY);
}
