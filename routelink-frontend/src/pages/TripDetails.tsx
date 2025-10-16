// src/pages/TripDetails.tsx
import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { api } from "@/lib/api";
import { requestBooking } from "@/lib/bookings";   // ✅ use the correct helper
import type { TripDto } from "@/types";

export default function TripDetails() {
  const { id } = useParams<{ id: string }>();
  const [trip, setTrip] = useState<TripDto | null>(null);
  const [seats, setSeats] = useState(1);
  const [msg, setMsg] = useState<string | null>(null);
  const [err, setErr] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    let alive = true;
    (async () => {
      try {
        const data = await api.get<TripDto>(`/api/trips/${id}`);
        if (alive) setTrip(data);
      } catch (e: any) {
        if (alive) setErr(e?.message || "Failed to load trip");
      } finally {
        if (alive) setLoading(false);
      }
    })();
    return () => { alive = false; };
  }, [id]);

  async function onRequestClick() {
    if (!trip) return;
    setErr(null);
    setMsg(null);
    setSubmitting(true);
    try {
      // ✅ This calls POST /api/bookings/request with { tripId, seats }
      await requestBooking(trip.id, seats);
      setMsg("Request sent! Check My bookings.");
    } catch (e: any) {
      setErr(e?.response?.data?.message || e?.message || "Request failed");
    } finally {
      setSubmitting(false);
    }
  }

  if (loading) return <div className="p-4">Loading…</div>;
  if (err) return <div className="p-4 text-red-600">{err}</div>;
  if (!trip) return null;

  return (
    <div className="p-4">
      <div className="card w-80">
        <div className="font-semibold">
          {trip.startPlace} → {trip.endPlace}
        </div>
        <div className="text-sm">
          {new Date(trip.rideAt).toLocaleString()} • Seats left: {trip.seatsLeft} • $
          {Number(trip.pricePerSeat).toFixed(2)}/seat
        </div>

        <div className="mt-3">
          <label className="text-sm">Seats</label>
          <input
            className="input"
            type="number"
            min={1}
            max={trip.seatsLeft}
            value={seats}
            onChange={(e) => setSeats(parseInt(e.target.value || "1", 10))}
          />
        </div>

        {/* ✅ type="button" so it doesn’t submit a form and navigate */}
        <button
          type="button"
          className="btn-primary mt-3"
          disabled={submitting}
          onClick={onRequestClick}
        >
          {submitting ? "Requesting…" : "Request Trip"}
        </button>

        {msg && <div className="text-green-700 text-sm mt-2">{msg}</div>}
        {err && <div className="text-red-600 text-sm mt-2">{err}</div>}

        <div className="text-xs text-gray-500 mt-3">
          Trip ID: {trip.id} · Status: {trip.status} · Total seats: {trip.seatsTotal}
        </div>
      </div>
    </div>
  );
}
