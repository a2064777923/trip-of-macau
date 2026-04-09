import type { AdminCurrentUser } from '../types/admin';

const TOKEN_KEY = 'admin_token';
const REFRESH_TOKEN_KEY = 'admin_refresh_token';
const USER_KEY = 'admin_user';

export function getAdminToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function setAdminToken(token: string) {
  localStorage.setItem(TOKEN_KEY, token);
}

export function getRefreshToken() {
  return localStorage.getItem(REFRESH_TOKEN_KEY);
}

export function setRefreshToken(token: string) {
  localStorage.setItem(REFRESH_TOKEN_KEY, token);
}

export function getAdminUser(): AdminCurrentUser | null {
  const raw = localStorage.getItem(USER_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw) as AdminCurrentUser;
  } catch {
    return null;
  }
}

export function setAdminUser(user: AdminCurrentUser) {
  localStorage.setItem(USER_KEY, JSON.stringify(user));
}

export function clearAdminAuth() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

export function isLoggedIn() {
  return Boolean(getAdminToken());
}
