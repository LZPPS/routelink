import React, { createContext, useContext, useEffect, useState } from "react";
import type { AuthResponse, AuthState, AuthUser } from "../types";
import { api } from "../lib/api";

type AuthContextType = {
  auth: AuthState;
  setSession: (token: string, user: AuthUser) => void;
  logout: () => void;
};

const AuthCtx = createContext<AuthContextType | undefined>(undefined);

function getStored(): AuthState {
  const token = localStorage.getItem("token") || "";
  const meStr = localStorage.getItem("me");
  const user: AuthUser | null = meStr ? JSON.parse(meStr) : null;
  return { token, user };
}

export default function AuthProvider({ children }: { children: React.ReactNode }) {
  const [auth, setAuth] = useState<AuthState>(getStored());

  // optional background token check – won't block UI
  useEffect(() => {
    if (!auth.token) return;
    api
      .get("/auth/me")
      .catch(() => {
        localStorage.removeItem("token");
        localStorage.removeItem("me");
        setAuth({ token: "", user: null });
      });
  }, [auth.token]);

  const setSession = (token: string, user: AuthUser) => {
    localStorage.setItem("token", token);
    localStorage.setItem("me", JSON.stringify(user));
    setAuth({ token, user });
  };

  const logout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("me");
    setAuth({ token: "", user: null });
  };

  return (
    <AuthCtx.Provider value={{ auth, setSession, logout }}>
      {children}
    </AuthCtx.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthCtx);
  if (!ctx) throw new Error("useAuth must be used inside <AuthProvider>");
  return ctx;
}
