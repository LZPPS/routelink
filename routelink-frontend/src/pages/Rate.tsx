// src/pages/Rate.tsx
import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { rateBooking } from "../lib/ratings";
import Nav from "../components/Nav";

export default function Rate() {
  const [sp] = useSearchParams();
  const [bookingId, setBookingId] = useState<number | "">("");
  const [stars, setStars] = useState<number>(5);
  const [comment, setComment] = useState("");
  const [msg, setMsg] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    const raw = sp.get("bookingId");
    if (raw) setBookingId(Number(raw));
  }, [sp]);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setMsg(null);
    if (!bookingId || stars < 1 || stars > 5) {
      setMsg("Please provide a valid booking and stars (1..5).");
      return;
    }
    setBusy(true);
    try {
      await rateBooking({ bookingId: Number(bookingId), stars, comment });
      setMsg("Thanks! Your rating was submitted.");
    } catch (e: any) {
      setMsg(e?.response?.data?.message || e?.response?.data?.error || "Failed to submit rating");
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="page">
      <Nav />
      <main className="container-narrow">
        <h1 className="text-xl font-semibold mb-3">Rate your ride</h1>

        {msg && <p className={msg.startsWith("Thanks") ? "text-green-700" : "text-red-600"}>{msg}</p>}

        <form onSubmit={submit} className="card grid gap-3 max-w-sm">
          <label className="grid gap-1">
            <span className="text-sm">Booking ID</span>
            <input
              className="btn w-full"
              type="number"
              value={bookingId}
              onChange={(e) => setBookingId(e.target.value ? Number(e.target.value) : "")}
              placeholder="e.g., 42"
            />
          </label>

          <label className="grid gap-1">
            <span className="text-sm">Stars (1..5)</span>
            <input
              className="btn w-full"
              type="number"
              min={1}
              max={5}
              value={stars}
              onChange={(e) => setStars(Number(e.target.value))}
            />
          </label>

          <label className="grid gap-1">
            <span className="text-sm">Comment (optional)</span>
            <textarea
              className="btn w-full"
              value={comment}
              onChange={(e) => setComment(e.target.value)}
              placeholder="What went well?"
            />
          </label>

          <button className="btn-primary" disabled={busy}>
            {busy ? "Submitting…" : "Submit"}
          </button>
        </form>
      </main>
    </div>
  );
}
