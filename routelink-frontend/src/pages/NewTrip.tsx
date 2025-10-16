import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { createTrip } from "../lib/trips";
import Nav from "../components/Nav";

type FormState = {
  startPlace: string;
  startLat: string;   // keep inputs as string; cast on submit
  startLng: string;
  endPlace: string;
  endLat: string;
  endLng: string;
  polyline: string;
  rideAt: string;     // ISO like "2025-10-09T14:00"
  pricePerSeat: string;
  seatsTotal: string;
};

export default function NewTrip() {
  const nav = useNavigate();
  const [form, setForm] = useState<FormState>({
    startPlace: "",
    startLat: "",
    startLng: "",
    endPlace: "",
    endLat: "",
    endLng: "",
    polyline: "",
    rideAt: "",          // <input type="datetime-local" />
    pricePerSeat: "",
    seatsTotal: "1",
  });
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState("");

  function setField<K extends keyof FormState>(k: K, v: string) {
    setForm((f) => ({ ...f, [k]: v }));
  }

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setErr("");
    setLoading(true);
    try {
      // Cast string inputs to numbers where required
      const payload = {
        startPlace: form.startPlace.trim(),
        startLat: Number(form.startLat),
        startLng: Number(form.startLng),
        endPlace: form.endPlace.trim(),
        endLat: Number(form.endLat),
        endLng: Number(form.endLng),
        polyline: form.polyline?.trim() || undefined,
        // datetime-local gives "YYYY-MM-DDTHH:mm" (no zone). Add 'Z' or your offset; backend accepts ISO.
        rideAt: form.rideAt ? new Date(form.rideAt).toISOString() : new Date().toISOString(),
        pricePerSeat: Number(form.pricePerSeat),
        seatsTotal: Number(form.seatsTotal),
      };

      // simple client-side guards
      if (
        Number.isNaN(payload.startLat) || Number.isNaN(payload.startLng) ||
        Number.isNaN(payload.endLat)   || Number.isNaN(payload.endLng)  ||
        Number.isNaN(payload.pricePerSeat) || Number.isNaN(payload.seatsTotal)
      ) {
        throw new Error("Please enter valid numbers for coordinates, price and seats.");
      }

      await createTrip(payload);
      nav("/trips"); // or wherever your list page is
    } catch (e: any) {
      const msg =
        e?.response?.data?.error ||
        e?.message ||
        "Failed to create trip";
      setErr(msg);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="page">
      <Nav />
      <main className="container-narrow">
        <h1 className="text-2xl font-semibold mb-4">Create a Trip</h1>
        {err && <div className="glass p-3 mb-4 text-red-700">{err}</div>}

        <form onSubmit={onSubmit} className="grid gap-4 glass p-4">
          <div className="grid md:grid-cols-2 gap-3">
            <label className="grid gap-1">
              <span className="text-sm">Start place</span>
              <input className="input" value={form.startPlace}
                     onChange={(e) => setField("startPlace", e.target.value)} />
            </label>
            <label className="grid gap-1">
              <span className="text-sm">End place</span>
              <input className="input" value={form.endPlace}
                     onChange={(e) => setField("endPlace", e.target.value)} />
            </label>
          </div>

          <div className="grid md:grid-cols-2 gap-3">
            <label className="grid gap-1">
              <span className="text-sm">Start lat</span>
              <input type="number" step="any" className="input" value={form.startLat}
                     onChange={(e) => setField("startLat", e.target.value)} />
            </label>
            <label className="grid gap-1">
              <span className="text-sm">Start lng</span>
              <input type="number" step="any" className="input" value={form.startLng}
                     onChange={(e) => setField("startLng", e.target.value)} />
            </label>
          </div>

          <div className="grid md:grid-cols-2 gap-3">
            <label className="grid gap-1">
              <span className="text-sm">End lat</span>
              <input type="number" step="any" className="input" value={form.endLat}
                     onChange={(e) => setField("endLat", e.target.value)} />
            </label>
            <label className="grid gap-1">
              <span className="text-sm">End lng</span>
              <input type="number" step="any" className="input" value={form.endLng}
                     onChange={(e) => setField("endLng", e.target.value)} />
            </label>
          </div>

          <label className="grid gap-1">
            <span className="text-sm">Polyline (optional)</span>
            <input className="input" value={form.polyline}
                   onChange={(e) => setField("polyline", e.target.value)} />
          </label>

          <div className="grid md:grid-cols-3 gap-3">
            <label className="grid gap-1">
              <span className="text-sm">Ride at</span>
              <input type="datetime-local" className="input" value={form.rideAt}
                     onChange={(e) => setField("rideAt", e.target.value)} />
            </label>
            <label className="grid gap-1">
              <span className="text-sm">Price per seat</span>
              <input type="number" className="input" value={form.pricePerSeat}
                     onChange={(e) => setField("pricePerSeat", e.target.value)} />
            </label>
            <label className="grid gap-1">
              <span className="text-sm">Seats total</span>
              <input type="number" className="input" value={form.seatsTotal}
                     min={1} onChange={(e) => setField("seatsTotal", e.target.value)} />
            </label>
          </div>

          <div className="flex gap-2">
            <button className="btn-primary" disabled={loading}>
              {loading ? "Creating..." : "Create trip"}
            </button>
            <button type="button" className="btn" onClick={() => nav(-1)}>Cancel</button>
          </div>
        </form>
      </main>
    </div>
  );
}

/* Tailwind helpers used above (put these in your global CSS if you don't have them)
.input { @apply px-3 py-2 rounded-lg border border-black/10 bg-white/80 backdrop-blur; }
*/
