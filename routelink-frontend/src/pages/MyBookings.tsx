// src/pages/MyBookings.tsx
import { useEffect, useState, useCallback } from "react";
import { myBookings, cancelBooking } from "../lib/bookings"; // <-- added
import { getTrip } from "../lib/trips";
import type { BookingDto, TripDto } from "../types";
import Nav from "../components/Nav";
import { Link } from "react-router-dom";

type BookingRow = BookingDto & { trip?: TripDto; canRate?: boolean };

export default function MyBookings() {
  const [rows, setRows] = useState<BookingRow[]>([]);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState<string | null>(null);
  const [cancellingId, setCancellingId] = useState<number | null>(null); // <-- added

  const load = useCallback(async () => {
    setLoading(true); setErr(null);
    try {
      const list = await myBookings();

      const withTrips = await Promise.all(list.map(async b => {
        try {
          const trip = await getTrip(b.tripId);
          const canRate = b.status === "CONFIRMED" && trip.status === "CLOSED";
          return { ...b, trip, canRate } as BookingRow;
        } catch {
          return { ...b } as BookingRow;
        }
      }));

      setRows(withTrips);
    } catch (e: any) {
      setErr(e?.response?.data?.error || "Failed to load bookings");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { void load(); }, [load]);

  // <-- added
  const handleCancel = async (b: BookingRow) => {
    if (cancellingId) return;
    const confirmText =
      b.status === "CONFIRMED"
        ? "Cancel this confirmed booking? The driver will be notified."
        : "Withdraw this booking request?";
    if (!window.confirm(confirmText)) return;

    try {
      setCancellingId(b.id);
      await cancelBooking(b.id);
      await load(); // refresh list
      alert("Canceled!");
    } catch (e: any) {
      alert(e?.message ?? "Cancel failed");
    } finally {
      setCancellingId(null);
    }
  };

  return (
    <div className="page">
      <Nav />
      <main className="container-narrow grid gap-4">
        <h1 className="text-xl font-semibold">My bookings</h1>
        {loading && <p>Loading…</p>}
        {err && <p className="text-red-600">{err}</p>}

        <div className="grid gap-3">
          {rows.map(b => {
            const canCancel = b.status === "REQUESTED" || b.status === "CONFIRMED";
            const isBusy = cancellingId === b.id;

            return (
              <div key={b.id} className="card flex items-center justify-between">
                <div className="text-sm">
                  <div className="font-medium">
                    {b.trip ? `${b.trip.startPlace} → ${b.trip.endPlace}` : `#${b.tripId}`}
                  </div>
                  <div className="text-gray-700">
                    seats {b.seats} · <span className="chip">{b.status}</span>
                    {b.trip && <> · trip: <span className="chip">{b.trip.status}</span></>}
                  </div>
                  <div className="text-xs text-gray-600">Booking ID: #{b.id}</div>
                </div>

                <div className="flex items-center gap-2">
                  {b.canRate && (
                    <Link className="btn-primary" to={`/rate?bookingId=${b.id}`}>
                      Rate
                    </Link>
                  )}

                  {canCancel && (
                    <button
                      className="btn-outline"
                      disabled={isBusy}
                      onClick={() => handleCancel(b)}
                      title={b.status === "CONFIRMED" ? "Cancel confirmed booking" : "Withdraw request"}
                    >
                      {isBusy ? "Cancelling…" : "Cancel"}
                    </button>
                  )}
                </div>
              </div>
            );
          })}

          {!loading && rows.length === 0 && (
            <p className="text-sm text-gray-600">No bookings yet.</p>
          )}
        </div>
      </main>
    </div>
  );
}
