import { useEffect, useState, type ReactNode } from "react";
import { Navigate } from "react-router-dom";
import { getStoredUser, me } from "../lib/auth";

export default function Protected({ children }: { children: ReactNode }) {
  const [state, setState] = useState<"loading" | "ok" | "no">("loading");

  useEffect(() => {
    const token = localStorage.getItem("token");
    if (!token) { setState("no"); return; }

    const cached = getStoredUser();
    if (cached) { setState("ok"); return; }

    me().then(() => setState("ok")).catch(() => setState("no"));
  }, []);

  if (state === "loading") return <div className="p-6">Loading…</div>;
  if (state === "no") return <Navigate to="/login" replace />;
  return <>{children}</>;
}
