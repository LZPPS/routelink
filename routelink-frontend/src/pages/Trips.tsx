// src/pages/Trips.tsx
import { useEffect, useState } from "react";
import { Link } from "react-router-dom";

import { closeTrip, listTrips, reopenTrip } from "@/lib/trips";
import type { TripDto } from "@/types";

export default function Trips() {
  const [items, setItems] = useState<TripDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState<string | null>(null);
  const [busyIds, setBusyIds] = useState<Record<number, boolean>>({});

  async function load() {
    setLoading(true);
    setErr(null);
    try {
      const data = await listTrips();
      setItems(data);
    } catch (e: any) {
      setErr(
        e?.response?.data?.message ||
          e?.response?.data?.error ||
          "Failed to load trips"
      );
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, []);

  async function doClose(id: number) {
    setBusyIds((m) => ({ ...m, [id]: true }));
    try {
      await closeTrip(id);
      await load();
    } catch (e: any) {
      alert(e?.response?.data?.message || "Close failed");
    } finally {
      setBusyIds((m) => ({ ...m, [id]: false }));
    }
  }

  async function doReopen(id: number) {
    setBusyIds((m) => ({ ...m, [id]: true }));
    try {
      await reopenTrip(id);
      await load();
    } catch (e: any) {
      alert(e?.response?.data?.message || "Reopen failed");
    } finally {
      setBusyIds((m) => ({ ...m, [id]: false }));
    }
  }

  const fmtPrice = (p: number | string | null | undefined) => {
    if (p == null) return "—";
    const n = typeof p === "string" ? Number(p) : p;
    return isFinite(n) ? n.toFixed(2) : String(p);
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-3">
        <h2 className="text-lg font-semibold">Trips</h2>
        <Link to="/create" className="btn-primary">
          + New
        </Link>
      </div>

      {loading && <p>Loading…</p>}
      {err && <p className="text-red-600">{err}</p>}

      <div className="grid gap-3">
        {items.map((t) => {
          const isBusy = !!busyIds[t.id];
          return (
            <div key={t.id} className="card flex items-center justify-between">
              <div className="text-sm">
                <div className="font-medium">
                  {t.startPlace} → {t.endPlace}
                </div>
                <div className="text-gray-700">
                  {t.rideAt ? new Date(t.rideAt).toLocaleString() : "—"} · ₹
                  {fmtPrice(t.pricePerSeat)} per seat · seats {t.seatsLeft}/
                  {t.seatsTotal} · <span className="chip">{t.status}</span>
                </div>
              </div>

              <div className="flex items-center gap-2">
                {/* View trip details / booking page */}
                <Link to={`/trip/${t.id}`} className="btn-outline">
                  View
                </Link>

                {t.status !== "CLOSED" && (
                  <button
                    onClick={() => doClose(t.id)}
                    className="btn"
                    disabled={isBusy}
                    title="Close this trip to stop new bookings"
                  >
                    {isBusy ? "Closing…" : "Close"}
                  </button>
                )}
                {t.status === "CLOSED" && (
                  <button
                    onClick={() => doReopen(t.id)}
                    className="btn"
                    disabled={isBusy}
                    title="Reopen this trip to accept bookings again"
                  >
                    {isBusy ? "Reopening…" : "Reopen"}
                  </button>
                )}
              </div>
            </div>
          );
        })}

        {!loading && items.length === 0 && (
          <p className="text-sm text-gray-600">No trips yet.</p>
        )}
      </div>
    </div>
  );
}
