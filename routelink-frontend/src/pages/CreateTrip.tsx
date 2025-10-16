import { useEffect, useRef, useState } from "react";
import { api } from "@/lib/api";
import { loadGooglePlaces } from "@/lib/loadGoogle";

declare global { interface Window { google: any } }

type Place = { text: string; lat: number; lng: number };

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

async function ensureCoords(p: Place) {
  if ((p.lat !== 0 || p.lng !== 0) && p.text) return p; // already from Autocomplete
  if (!p.text.trim()) return p;
  const g = await geocodeText(p.text.trim());
  return g ? { ...p, lat: g.lat, lng: g.lng } : p;
}

type TripDto = {
  id: number;
  startPlace: string; startLat: number; startLng: number;
  endPlace: string;   endLat: number;   endLng: number;
  rideAt: string; pricePerSeat: number | string;
  seatsTotal: number; seatsLeft: number;
};

export default function CreateTrip() {
  const [start, setStart] = useState<Place>({ text: "", lat: 0, lng: 0 });
  const [end,   setEnd]   = useState<Place>({ text: "", lat: 0, lng: 0 });
  const [rideAt, setRideAt] = useState(new Date().toISOString().slice(0,16)); // local datetime
  const [pricePerSeat, setPricePerSeat] = useState<number | string>(100);
  const [seatsTotal, setSeatsTotal] = useState(3);
  const [msg, setMsg] = useState<string>("");

  const startRef = useRef<HTMLInputElement>(null);
  const endRef   = useRef<HTMLInputElement>(null);

  // ✅ Load Places first, then wire Autocomplete
  useEffect(() => {
    const key = import.meta.env.VITE_GOOGLE_MAPS_KEY as string;
    console.log("[GMAPS] key present?", !!key);
    loadGooglePlaces(key)
      .then(() => {
        const g = (window as any).google;
        console.log("[GMAPS] places loaded?", !!g?.maps?.places);
        if (!g?.maps?.places) return;

        if (startRef.current) {
          const ac = new g.maps.places.Autocomplete(startRef.current, { types: ["geocode"] });
          ac.addListener("place_changed", () => {
            const p = ac.getPlace(); if (!p?.geometry) return;
            setStart({
              text: p.formatted_address || p.name || "",
              lat: p.geometry.location.lat(),
              lng: p.geometry.location.lng(),
            });
          });
        }
        if (endRef.current) {
          const ac2 = new g.maps.places.Autocomplete(endRef.current, { types: ["geocode"] });
          ac2.addListener("place_changed", () => {
            const p = ac2.getPlace(); if (!p?.geometry) return;
            setEnd({
              text: p.formatted_address || p.name || "",
              lat: p.geometry.location.lat(),
              lng: p.geometry.location.lng(),
            });
          });
        }
      })
      .catch((e) => console.error("[GMAPS] load error", e));
  }, []);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setMsg("");

    // Fallback if driver typed and didn’t pick a suggestion
    const s = await ensureCoords(start);
    const d = await ensureCoords(end);

    if (!s.text || !d.text) { setMsg("Please enter Start and End."); return; }
    if (s.lat === 0 && s.lng === 0) { setMsg("Could not locate Start."); return; }
    if (d.lat === 0 && d.lng === 0) { setMsg("Could not locate End."); return; }

    try {
      const payload = {
        startPlace: s.text, startLat: s.lat, startLng: s.lng,
        endPlace: d.text,   endLat: d.lat,   endLng: d.lng,
        polyline: "",
        rideAt: new Date(rideAt).toISOString(), // send ISO
        pricePerSeat: typeof pricePerSeat === "string" ? parseFloat(pricePerSeat) : pricePerSeat,
        seatsTotal,
      };
      const trip = await api.post<TripDto>("/api/trips", payload);
      setMsg(`Trip created! ID: ${trip.id}`);
    } catch (err: any) {
      // surface backend error nicely
      const status = err?.response?.status;
      const apiMsg = err?.response?.data?.message || err?.message;
      if (status === 401 || status === 403) {
        setMsg("You must be logged in to create a trip.");
      } else {
        setMsg(apiMsg || "Failed to create trip");
      }
      console.error("Create trip error:", err);
    }
  }

  return (
    <div className="card max-w-2xl mx-auto">
      <h2 className="text-lg font-semibold mb-3">Post a trip</h2>
      <form onSubmit={onSubmit} className="grid gap-3">
        <div className="grid md:grid-cols-2 gap-3">
          <div>
            <label className="text-sm">Start</label>
            <input
              ref={startRef}
              className="input"
              placeholder="Start city/address (e.g., St Louis, MO)"
              value={start.text}
              onChange={(e)=> setStart(s=>({ ...s, text: e.target.value }))}
            />
          </div>
          <div>
            <label className="text-sm">End</label>
            <input
              ref={endRef}
              className="input"
              placeholder="Destination (e.g., Chicago, IL)"
              value={end.text}
              onChange={(e)=> setEnd(s=>({ ...s, text: e.target.value }))}
            />
          </div>
        </div>

        <div className="grid md:grid-cols-2 gap-3">
          <div>
            <label className="text-sm">Departure time</label>
            <input
              type="datetime-local"
              className="input"
              value={rideAt}
              onChange={(e)=> setRideAt(e.target.value)}
            />
          </div>
          <div>
            <label className="text-sm">Seats</label>
            <input
              type="number" min={1}
              className="input"
              value={seatsTotal}
              onChange={(e)=> setSeatsTotal(parseInt(e.target.value || "1", 10))}
            />
          </div>
        </div>

        <div className="grid md:grid-cols-2 gap-3">
          <div>
            <label className="text-sm">Price per seat</label>
            <input
              type="number" min={0}
              className="input"
              value={pricePerSeat}
              onChange={(e)=> setPricePerSeat(e.target.value)}
            />
          </div>
          <div className="self-end">
            <button className="btn-primary w-full">Create</button>
          </div>
        </div>

        {msg && <div className="text-sm mt-1">{msg}</div>}
      </form>
    </div>
  );
}
