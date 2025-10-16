// src/lib/auth.ts
import { api } from "./api";

export type Role = "RIDER" | "DRIVER";
export type User = { id: number; name: string; email: string; role: Role };

export type AuthRes = {
  token: string;
  userId: number;
  email: string;
  name: string;
  role: Role;
};

/** Save token + compact user into localStorage, return the user object */
export function storeAuth(r: AuthRes): User {
  const user: User = { id: r.userId, name: r.name, email: r.email, role: r.role };
  localStorage.setItem("token", r.token);            // api.ts reads this
  localStorage.setItem("me", JSON.stringify(user));
  return user;
}

export function clearAuth() {
  localStorage.removeItem("token");
  localStorage.removeItem("me");
}

/** Read cached user (if any) */
export function getStoredUser(): User | null {
  const raw = localStorage.getItem("me");
  if (!raw) return null;
  try { return JSON.parse(raw) as User; } catch { return null; }
}

/** Ping backend to validate the token / get server-side identity */
export async function me() {
  // backend should return the current user (shape can vary)
  return await api.get<User>("/auth/me");
}

/** Signup/login helpers */
export async function signup(name: string, email: string, password: string, role: Role) {
  const res = await api.post<AuthRes>("/auth/signup", { name, email, password, role });
  return res; // caller should call storeAuth(res) if you want to auto-login
}

export async function login(email: string, password: string) {
  const res = await api.post<AuthRes>("/auth/login", { email, password });
  return res; // caller should call storeAuth(res)
}
