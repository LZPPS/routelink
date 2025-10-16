import { useEffect, useRef, useState } from "react";
import { api } from "@/lib/api";

declare global { interface Window { google: any } }

export default function PostTrip() {
  const [start, setStart] = useState({ text: "", lat: 0, lng: 0 });
  const [end,   setEnd]   = useState({ text: "", lat: 0, lng: 0 });
  const [rideAt, setRideAt] = useState(new Date().toISOString().slice(0,16));
  const [seatsTotal, setSeatsTotal] = useState(3);
  const [pricePerSeat, setPricePerSeat] = useState(25);
  const [msg, setMsg] = useState<string>("");

  const startRef = useRef<HTMLInputElement>(null);
  const endRef   = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (!window.google) return;
    if (startRef.current) {
      const ac = new window.google.maps.places.Autocomplete(startRef.current, { types: ["geocode"] });
      ac.addListener("place_changed", () => {
        const p = ac.getPlace(); if (!p?.geometry) return;
        setStart({ text: p.formatted_address || p.name, lat: p.geometry.location.lat(), lng: p.geometry.location.lng() });
      });
    }
    if (endRef.current) {
      const ac = new window.google.maps.places.Autocomplete(endRef.current, { types: ["geocode"] });
      ac.addListener("place_changed", () => {
        const p = ac.getPlace(); if (!p?.geometry) return;
        setEnd({ text: p.formatted_address || p.name, lat: p.geometry.location.lat(), lng: p.geometry.location.lng() });
      });
    }
  }, []);

  async function submit(e: React.FormEvent) {
    e.preventDefault(); setMsg("");
    try {
      const id = await api.post<number>("/api/trips", {
        startPlace: start.text, startLat: start.lat, startLng: start.lng,
        endPlace: end.text,     endLat: end.lat,     endLng: end.lng,
        polyline: "",
        rideAt: new Date(rideAt).toISOString(),
        pricePerSeat, seatsTotal
      });
      setMsg(`Trip posted! ID: ${id}`);
    } catch (e:any) {
      setMsg(e.message || "Failed to post");
    }
  }

  return (
    <div className="card">
      <h2 className="text-lg font-semibold mb-2">Post a trip</h2>
      <form onSubmit={submit} className="grid md:grid-cols-2 gap-3">
        <input ref={startRef} className="input" placeholder="Start location"
               onChange={(e)=> setStart(s=>({...s, text: e.target.value}))} />
        <input ref={endRef} className="input" placeholder="Destination"
               onChange={(e)=> setEnd(s=>({...s, text: e.target.value}))} />
        <input type="datetime-local" className="input" value={rideAt} onChange={(e)=>setRideAt(e.target.value)} />
        <input type="number" min={1} className="input" value={seatsTotal} onChange={(e)=>setSeatsTotal(parseInt(e.target.value||"1",10))} />
        <input type="number" min={0} className="input" value={pricePerSeat} onChange={(e)=>setPricePerSeat(parseFloat(e.target.value||"25"))} />
        <button className="btn-primary md:col-span-2">Post Trip</button>
      </form>
      {msg && <p className="mt-3 text-sm">{msg}</p>}
    </div>
  );
}
