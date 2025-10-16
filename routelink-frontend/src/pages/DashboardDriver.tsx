// src/pages/DashboardDriver.tsx
import { useEffect, useState } from "react";
import Nav from "../components/Nav";
import { useAuth } from "../providers/AuthProvider";

import { listTrips, closeTrip, reopenTrip } from "../lib/trips";
import type { TripDto } from "../types";


import {
  bookingsForTrip,
  confirmBooking,
  declineBooking,
} from "../lib/bookings";
import type { BookingDto } from "../types";

export default function DashboardDriver() {
  const { auth } = useAuth();

  const [trips, setTrips] = useState<TripDto[]>([]);
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [selectedTrip, setSelectedTrip] = useState<TripDto | null>(null);

  const [rows, setRows] = useState<BookingDto[]>([]);
  const [loadingTrips, setLoadingTrips] = useState(false);
  const [loadingBookings, setLoadingBookings] = useState(false);
  const [errTrips, setErrTrips] = useState<string | null>(null);
  const [errBookings, setErrBookings] = useState<string | null>(null);

  // load my trips
  useEffect(() => {
    (async () => {
      setErrTrips(null);
      setLoadingTrips(true);
      try {
        const all = await listTrips();
        const mine = all.filter((t) => t.driverId === auth.user?.id);
        setTrips(mine);

        if (mine.length > 0) {
          const first = mine[0];
          setSelectedId(first.id);
          setSelectedTrip(first);
          await loadBookings(first.id);
        } else {
          setSelectedId(null);
          setSelectedTrip(null);
          setRows([]);
        }
      } catch (e: any) {
        setErrTrips(e?.response?.data?.error || "Failed to load trips");
      } finally {
        setLoadingTrips(false);
      }
    })();
  }, [auth.user?.id]);

  async function loadBookings(tripId: number) {
    setErrBookings(null);
    setLoadingBookings(true);
    try {
      const data = await bookingsForTrip(tripId);
      setRows(data);
    } catch (e: any) {
      setErrBookings(e?.response?.data?.error || "Failed to load bookings");
    } finally {
      setLoadingBookings(false);
    }
  }

  // select a trip chip
  async function selectTrip(t: TripDto) {
    setSelectedId(t.id);
    setSelectedTrip(t);
    await loadBookings(t.id);
  }

  // close / reopen selected trip
  async function toggleTripStatus() {
    if (!selectedTrip) return;
    try {
      if (selectedTrip.status === "CLOSED") {
        await reopenTrip(selectedTrip.id);
      } else {
        await closeTrip(selectedTrip.id);
      }

      // refresh trips list and the selected trip
      const all = await listTrips();
      const mine = all.filter((t) => t.driverId === auth.user?.id);
      setTrips(mine);
      const updated = mine.find((t) => t.id === selectedTrip.id) || null;
      setSelectedTrip(updated);
    } catch (e: any) {
      alert(e?.response?.data?.error || "Trip action failed");
    }
  }

  async function actBooking(id: number, kind: "confirm" | "decline") {
    try {
      if (kind === "confirm") await confirmBooking(id);
      else await declineBooking(id);
      if (selectedId) await loadBookings(selectedId);
    } catch (e: any) {
      alert(e?.response?.data?.error || "Booking action failed");
    }
  }

  return (
    <div className="page">
      <Nav />

      <main className="container-narrow grid gap-4">
        {/* My trips */}
        <div className="card">
          <div className="flex items-center justify-between mb-2">
            <h2 className="font-semibold">Your trips</h2>
            {selectedTrip && (
              <div className="flex items-center gap-2">
                <span className="chip">Status: {selectedTrip.status}</span>
                <button className="btn" onClick={toggleTripStatus}>
                  {selectedTrip.status === "CLOSED" ? "Reopen" : "Close"}
                </button>
              </div>
            )}
          </div>

          {loadingTrips && <p>Loading trips…</p>}
          {errTrips && <p className="text-red-600">{errTrips}</p>}
          {!loadingTrips && trips.length === 0 && (
            <p className="text-sm text-gray-600">No trips for your account.</p>
          )}

          <div className="flex gap-2 overflow-x-auto">
            {trips.map((t) => (
              <button
                key={t.id}
                onClick={() => selectTrip(t)}
                className={`btn px-3 py-1.5 rounded-lg ${
                  selectedId === t.id ? "bg-black/10" : ""
                }`}
                title={`#${t.id} · ${t.startPlace} → ${t.endPlace}`}
              >
                #{t.id} · {t.startPlace} → {t.endPlace}
              </button>
            ))}
          </div>
        </div>

        {/* Booking requests */}
        <div className="card">
          <h2 className="font-semibold mb-2">Booking requests</h2>

          {loadingBookings && <p>Loading…</p>}
          {errBookings && <p className="text-red-600">{errBookings}</p>}
          {!loadingBookings && rows.length === 0 && (
            <p className="text-sm text-gray-600">No bookings.</p>
          )}

          <div className="grid gap-2">
            {rows.map((b) => (
              <div
                key={b.id}
                className="flex items-center justify-between rounded border p-3 bg-white/70"
              >
                <div className="text-sm">
                  <div className="font-medium">
                    #{b.id} · seats {b.seats} ·{" "}
                    <span className="font-mono">{b.status}</span>
                  </div>
                  <div>
                    {b.rider?.name}{" "}
                    {b.rider?.email ? `(${b.rider.email})` : null}
                  </div>
                </div>

                <div className="flex gap-2">
                  {b.status === "REQUESTED" && (
                    <>
                      <button
                        className="btn-primary"
                        onClick={() => actBooking(b.id, "confirm")}
                      >
                        Confirm
                      </button>
                      <button
                        className="btn"
                        onClick={() => actBooking(b.id, "decline")}
                      >
                        Decline
                      </button>
                    </>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>
      </main>
    </div>
  );
}
