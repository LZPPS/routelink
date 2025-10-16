// src/atoms.ts
import { atom } from "jotai";

export type User = {
  id: number;
  name: string;
  email: string;
  role: "DRIVER" | "RIDER";
};

// keep token in localStorage so axios can read it outside React
export const tokenAtom = atom<string | null>(
  typeof localStorage !== "undefined" ? localStorage.getItem("token") : null
);

export const meAtom = atom<User | null>(null);

// a write-only atom to update token + persist
export const setTokenAtom = atom(null, (_get, set, token: string | null) => {
  if (token) localStorage.setItem("token", token);
  else localStorage.removeItem("token");
  set(tokenAtom, token);
});
