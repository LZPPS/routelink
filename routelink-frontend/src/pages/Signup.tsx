// src/pages/Signup.tsx
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

export default function Signup() {
  const { setSession } = useAuth();
  const nav = useNavigate();

  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [role, setRole] = useState<"RIDER" | "DRIVER">("RIDER");
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState<string | null>(null);

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErr(null);
    setLoading(true);
    try {
      // ✅ api.post returns AuthRes (no { data })
      const res = await api.post<AuthRes>("/auth/signup", {
        name,
        email,
        password,
        role,
      });

      const user = { id: res.userId, name: res.name, email: res.email, role: res.role };
      setSession(res.token, user);
      nav("/search");
    } catch (e: any) {
      setErr(e?.message ?? "Sign up failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-md mx-auto p-6">
      <h1 className="text-2xl font-semibold mb-4">Sign up</h1>
      {err && <div className="mb-3 text-red-600">{err}</div>}

      <form onSubmit={onSubmit} className="space-y-3">
        <input className="input" placeholder="Full name" value={name} onChange={(e) => setName(e.target.value)} required />
        <input className="input" type="email" placeholder="Email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        <input className="input" type="password" placeholder="Password" value={password} onChange={(e) => setPassword(e.target.value)} required />
        <select className="input" value={role} onChange={(e) => setRole(e.target.value as "RIDER" | "DRIVER")}>
          <option value="RIDER">Rider</option>
          <option value="DRIVER">Driver</option>
        </select>
        <button className="btn w-full" disabled={loading}>
          {loading ? "Creating..." : "Create account"}
        </button>
      </form>

      <p className="text-sm mt-3">
        Already have an account? <Link className="link" to="/login">Login</Link>
      </p>
    </div>
  );
}
