import { useState } from "react";
import { searchByDate } from "../lib/trips";
import type { TripDto } from "../types";
import { requestBooking } from "../lib/bookings";
import Nav from "../components/Nav";

export default function Search() {
  const [date, setDate] = useState(() => new Date().toISOString().slice(0,10));
  const [minSeats, setMinSeats] = useState(1);
  const [list, setList] = useState<TripDto[]>([]);
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState<string | null>(null);

  const run = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true); setErr(null);
    try { setList(await searchByDate({ date, minSeats, sort:"time", order:"asc", size:50 })); }
    catch (e: any) { setErr(e?.response?.data?.error || "Search failed"); }
    finally { setLoading(false); }
  };

  async function book(id: number) {
    try {
      await requestBooking(id, 1);
      alert("Requested!");
    } catch (e: any) {
      alert(e?.response?.data?.error || "Booking failed");
    }
  }

  return (
    <div className="page">
      <Nav/>
      <main className="container-narrow">
        <form className="card grid sm:grid-cols-3 gap-3 mb-4" onSubmit={run}>
          <input className="input" type="date" value={date} onChange={e=>setDate(e.target.value)}/>
          <input className="input" type="number" min={1} value={minSeats} onChange={e=>setMinSeats(+e.target.value)}/>
          <button className="btn-primary">Search</button>
        </form>

        {err && <p className="text-red-600 mb-2">{err}</p>}
        {loading && <p>Loading…</p>}

        <div className="grid gap-3">
          {list.map(t => (
            <div key={t.id} className="card flex items-center justify-between">
              <div>
                <div className="font-medium">{t.startPlace} → {t.endPlace}</div>
                <div className="text-sm">{new Date(t.rideAt).toLocaleString()} · ₹{t.pricePerSeat} · seats {t.seatsLeft}/{t.seatsTotal}</div>
              </div>
              <button className="btn-primary" onClick={()=>book(t.id)}>Request</button>
            </div>
          ))}
        </div>
      </main>
    </div>
  );
}
