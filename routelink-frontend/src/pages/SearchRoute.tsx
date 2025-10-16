import { useState } from "react";
import { searchRoute } from "../lib/trips";
import type { TripDto } from "../types";
import Nav from "../components/Nav";
import { requestBooking } from "../lib/bookings";

export default function SearchRoute() {
  const [date, setDate] = useState(() => new Date().toISOString().slice(0,10));
  const [pickupLat, setPickupLat] = useState(12.9716);
  const [pickupLng, setPickupLng] = useState(77.5946);
  const [dropLat, setDropLat] = useState(12.9352);
  const [dropLng, setDropLng] = useState(77.6245);
  const [radiusKm, setRadiusKm] = useState(10);
  const [list, setList] = useState<TripDto[]>([]);
  const [err, setErr] = useState<string | null>(null);

  async function run(e: React.FormEvent) {
    e.preventDefault();
    setErr(null);
    try {
      const data = await searchRoute({ date, pickupLat, pickupLng, dropLat, dropLng, radiusKm, size: 50 });
      setList(data);
    } catch (e:any) { setErr(e?.response?.data?.error || "Search failed"); }
  }

  return (
    <div className="page">
      <Nav/>
      <main className="container-narrow">
        <form className="card grid sm:grid-cols-3 gap-3 mb-4" onSubmit={run}>
          <input className="input" type="date" value={date} onChange={e=>setDate(e.target.value)}/>
          <input className="input" placeholder="Pickup lat" value={pickupLat} onChange={e=>setPickupLat(+e.target.value)}/>
          <input className="input" placeholder="Pickup lng" value={pickupLng} onChange={e=>setPickupLng(+e.target.value)}/>
          <input className="input" placeholder="Drop lat" value={dropLat} onChange={e=>setDropLat(+e.target.value)}/>
          <input className="input" placeholder="Drop lng" value={dropLng} onChange={e=>setDropLng(+e.target.value)}/>
          <input className="input" placeholder="Radius (km)" value={radiusKm} onChange={e=>setRadiusKm(+e.target.value)}/>
          <button className="btn-primary">Search</button>
        </form>

        {err && <p className="text-red-600 mb-2">{err}</p>}

        <div className="grid gap-3">
          {list.map(t => (
            <div key={t.id} className="card flex items-center justify-between">
              <div>
                <div className="font-medium">{t.startPlace} → {t.endPlace}</div>
                <div className="text-sm">{new Date(t.rideAt).toLocaleString()} · ₹{t.pricePerSeat}</div>
              </div>
              <button className="btn-primary" onClick={()=>requestBooking(t.id,1)}>Request</button>
            </div>
          ))}
        </div>
      </main>
    </div>
  );
}
