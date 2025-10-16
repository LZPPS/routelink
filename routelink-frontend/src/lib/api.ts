// src/lib/api.ts
type Method = "GET" | "POST" | "PUT" | "DELETE";

type RequestOpts = {
  params?: Record<string, string | number | boolean | undefined | null>;
  headers?: Record<string, string>;
};

const BASE =
  import.meta.env.VITE_API_BASE?.replace(/\/$/, "") || "http://localhost:8080";

function qs(params?: RequestOpts["params"]) {
  if (!params) return "";
  const sp = new URLSearchParams();
  for (const [k, v] of Object.entries(params)) {
    if (v === undefined || v === null) continue;
    sp.append(k, String(v));
  }
  const s = sp.toString();
  return s ? `?${s}` : "";
}

async function request<T>(
  path: string,
  method: Method,
  body?: any,
  opts?: RequestOpts
): Promise<T> {
  const token = localStorage.getItem("token") || "";
  const url = `${BASE}${path}${qs(opts?.params)}`;

  const res = await fetch(url, {
    method,
    headers: {
      Accept: "application/json",
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(opts?.headers || {}),
    },
    body: body === undefined ? undefined : JSON.stringify(body),
  });

  if (!res.ok) {
    // try to surface JSON error shape if backend returns one
    const text = await res.text().catch(() => "");
    try {
      const j = text ? JSON.parse(text) : null;
      const msg = j?.message || j?.error || text || `HTTP ${res.status}`;
      throw new Error(msg);
    } catch {
      throw new Error(text || `HTTP ${res.status}`);
    }
  }

  if (res.status === 204) return undefined as unknown as T; // no body
  return (await res.json()) as T;
}

export const api = {
  get<T>(path: string, opts?: RequestOpts) {
    return request<T>(path, "GET", undefined, opts);
  },
  post<T>(path: string, body?: any, opts?: RequestOpts) {
    return request<T>(path, "POST", body, opts);
  },
  put<T>(path: string, body?: any, opts?: RequestOpts) {
    return request<T>(path, "PUT", body, opts);
  },
  del<T>(path: string, opts?: RequestOpts) {
    return request<T>(path, "DELETE", undefined, opts);
  },
};
