// src/pages/Login.tsx
import React, { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { api } from "../lib/api";
import { useAuth } from "../providers/AuthProvider";

type AuthRes = {
  token: string;
  userId: number;
  email: string;
  name: string;
  role: "RIDER" | "DRIVER";
};

export default function Login() {
  const { setSession } = useAuth();
  const nav = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState<string | null>(null);

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErr(null);
    setLoading(true);
    try {
      // api.post returns the body directly
      const data = await api.post<AuthRes>("/auth/login", { email, password });

      // store session: token first, then user
      const user = { id: data.userId, name: data.name, email: data.email, role: data.role };
      setSession(data.token, user);

      nav("/search");
    } catch (e: any) {
      setErr(e?.message || "Login failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-md mx-auto p-6">
      <h1 className="text-2xl font-semibold mb-4">Login</h1>
      {err && <div className="mb-3 text-red-600">{err}</div>}
      <form onSubmit={onSubmit} className="space-y-3">
        <input
          className="input"
          type="email"
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
        <input
          className="input"
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        <button className="btn w-full" disabled={loading}>
          {loading ? "Signing in..." : "Login"}
        </button>
      </form>
      <p className="text-sm mt-3">
        No account? <Link className="link" to="/signup">Sign up</Link>
      </p>
    </div>
  );
}
