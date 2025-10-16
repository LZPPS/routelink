import { useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom"; // ✅ use Link
import { api } from "@/lib/api";
import type { TripDto, TripSearchDto, UnifiedSearchReq } from "@/types";
import { loadGooglePlaces } from "@/lib/loadGoogle";

declare global { interface Window { google: any } }

type PlaceState = { text: string; lat: number; lng: number };

// --- helpers: geocode free-text & ensure coords ---
async function geocodeText(q: string): Promise<{lat:number; lng:number} | null> {
  return new Promise((resolve) => {
    const g = (window as any).google;
    if (!g?.maps) return resolve(null);
    const geocoder = new g.maps.Geocoder();
    geocoder.geocode({ address: q }, (results: any, status: string) => {
      if (status === "OK" && results?.[0]?.geometry?.location) {
        const loc = results[0].geometry.location;
        resolve({ lat: loc.lat(), lng: loc.lng() });
      } else {
        resolve(null);
      }
    });
  });
}
async function ensureCoords(p: PlaceState) {
  if ((p.lat !== 0 || p.lng !== 0) && p.text) return p; // already set via Autocomplete
  if (!p.text.trim()) return p;
  const g = await geocodeText(p.text.trim());
  return g ? { ...p, lat: g.lat, lng: g.lng } : p;
}

export default function SearchUnified() {
  const [start, setStart] = useState<PlaceState>({ text: "", lat: 0, lng: 0 });
  const [end,   setEnd]   = useState<PlaceState>({ text: "", lat: 0, lng: 0 });
  const [date,  setDate]  = useState(new Date().toISOString().slice(0,10));
  const [seats, setSeats] = useState(1);

  const [loading, setLoading] = useState(false);
  const [error,   setError]   = useState<string|null>(null);
  const [rows,    setRows]    = useState<Array<TripSearchDto | TripDto>>([]);

  const startInput = useRef<HTMLInputElement>(null);
  const endInput   = useRef<HTMLInputElement>(null);

  // Load Places first, then wire Autocomplete
  useEffect(() => {
    const key = import.meta.env.VITE_GOOGLE_MAPS_KEY as string;
    loadGooglePlaces(key).then(() => {
      const g = (window as any).google;
      if (!g?.maps?.places) return;

      if (startInput.current) {
        const ac = new g.maps.places.Autocomplete(startInput.current, { types: ["geocode"] });
        ac.addListener("place_changed", () => {
          const p = ac.getPlace();
          if (!p?.geometry) return;
          setStart({
            text: p.formatted_address || p.name || "",
            lat: p.geometry.location.lat(),
            lng: p.geometry.location.lng(),
          });
        });
      }
      if (endInput.current) {
        const ac2 = new g.maps.places.Autocomplete(endInput.current, { types: ["geocode"] });
        ac2.addListener("place_changed", () => {
          const p = ac2.getPlace();
          if (!p?.geometry) return;
          setEnd({
            text: p.formatted_address || p.name || "",
            lat: p.geometry.location.lat(),
            lng: p.geometry.location.lng(),
          });
        });
      }
    });
  }, []);

  async function onSearch(e: React.FormEvent) {
    e.preventDefault();

    // 🔧 Ensure coords even if user didn't pick a suggestion
    const s = await ensureCoords(start);
    const d = await ensureCoords(end);

    if (!s.text || !d.text) { setError("Please enter Start and End"); return; }
    if (s.lat === 0 && s.lng === 0) { setError("Could not locate Start"); return; }
    if (d.lat === 0 && d.lng === 0) { setError("Could not locate End"); return; }

    const body: UnifiedSearchReq = {
      startText: s.text, endText: d.text,
      startLat: s.lat, startLng: s.lng,
      endLat: d.lat, endLng: d.lng,
      seats, date
    };

    setLoading(true); setError(null);
    try {
      const data = await api.post<Array<TripSearchDto | TripDto>>("/api/trips/search-unified", body);
      setRows(data);
    } catch (err: any) {
      setError(err.message || "Search failed");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="max-w-3xl mx-auto p-4">
      <form onSubmit={onSearch} className="grid grid-cols-1 md:grid-cols-4 gap-3 bg-white/80 p-4 rounded-2xl shadow">
        <div className="md:col-span-2">
          <label className="text-sm">Start</label>
          <input
            ref={startInput}
            className="w-full border rounded px-3 py-2"
            placeholder="Enter start city/address"
            value={start.text}
            onChange={(e)=> setStart(s=>({ ...s, text: e.target.value }))}
          />
        </div>

        <div className="md:col-span-2">
          <label className="text-sm">End</label>
          <input
            ref={endInput}
            className="w-full border rounded px-3 py-2"
            placeholder="Enter destination"
            value={end.text}
            onChange={(e)=> setEnd(s=>({ ...s, text: e.target.value }))}
          />
        </div>

        <div>
          <label className="text-sm">Date</label>
          <input
            type="date"
            className="w-full border rounded px-3 py-2"
            value={date}
            onChange={(e)=> setDate(e.target.value)}
          />
        </div>

        <div>
          <label className="text-sm">Seats</label>
          <input
            type="number" min={1}
            className="w-full border rounded px-3 py-2"
            value={seats}
            onChange={(e)=> setSeats(parseInt(e.target.value || "1", 10))}
          />
        </div>

        <button type="submit" disabled={loading}
          className="md:col-span-2 bg-amber-500 hover:bg-amber-600 text-white rounded-xl px-4 py-2">
          {loading ? "Searching…" : "Search"}
        </button>

        {error && <div className="md:col-span-4 text-red-600">{error}</div>}
      </form>

      <div className="mt-6 space-y-3">
        {rows.length === 0 && !loading ? <p>No trips found.</p> : null}

        {rows.map((row, idx) => {
          const trip = (row as any).trip ? (row as TripSearchDto).trip : (row as TripDto);
          const matchedBy = (row as any).matchedBy || "—";
          const score = (row as any).score;

          return (
            <div key={trip.id ?? idx} className="border rounded-xl p-3 bg-white">
              <div className="flex justify-between gap-3">
                <div>
                  <div className="font-semibold">{trip.startPlace} → {trip.endPlace}</div>
                  <div className="text-sm text-gray-600">
                    {new Date(trip.rideAt).toLocaleString()} • Seats left: {trip.seatsLeft} • ${Number(trip.pricePerSeat).toFixed(2)}/seat
                  </div>
                  <div className="text-xs text-gray-500">
                    Match: {matchedBy}{score !== undefined ? ` • Score: ${Number(score).toFixed(2)}` : ""}
                  </div>
                </div>

                {/* ✅ use Link so SPA routing doesn't fall into the "*" redirect */}
                <Link
                  className="self-center bg-black text-white px-4 py-2 rounded-lg"
                  to={`/trip/${trip.id}`}
                >
                  View / Book
                </Link>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
