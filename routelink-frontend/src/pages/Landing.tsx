// src/pages/Landing.tsx
import { Link } from "react-router-dom";
import Nav from "../components/Nav";

export default function Landing() {
  return (
    <div className="min-h-screen flex flex-col bg-white">
      <Nav />

      {/* HEADLINE ON TOP */}
      <div className="w-full border-b border-gray-100">
        <div className="mx-auto max-w-6xl px-4 py-10">
          <h1 className="text-[34px] md:text-[40px] leading-[1.15] font-semibold text-gray-900 tracking-tight">
            “Drive smarter with RouteLink — match, share, and save on every journey."
          </h1>
        </div>
      </div>

      {/* HERO */}
      <section className="relative">
        <div className="mx-auto max-w-6xl px-4 py-10">
          <div className="grid items-center gap-8 md:grid-cols-2">
            <div>
              <p className="text-[15px] text-gray-700">
               “From booking to cost-sharing, RouteLink makes every trip smarter and smoother."
              </p>

              <div className="mt-6 flex gap-3">
                <Link
                  to="/search"
                  className="inline-flex items-center rounded-xl px-4 py-2 border border-gray-300 bg-white hover:bg-gray-50 text-gray-900"
                >
                  Search rides
                </Link>
                <Link
                  to="/create"
                  className="inline-flex items-center rounded-xl px-5 py-2.5 bg-amber-500 text-white hover:bg-amber-600"
                >
                  Publish a ride
                </Link>
              </div>
            </div>

            <div className="relative">
              <div className="rounded-3xl overflow-hidden shadow-[0_8px_28px_rgba(16,24,40,0.12)] ring-1 ring-black/5">
                <img
                  src="/bg.webp"
                  alt=""
                  className="block w-full h-[320px] md:h-[420px] object-cover object-center md:object-right"
                />
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* CENTERED INFO BLOCK */}
      <section className="border-t border-gray-100 bg-white">
        <div className="max-w-4xl mx-auto px-4 py-16 text-center">
          <h2 className="text-3xl font-bold text-gray-900">Find and share rides</h2>
          <p className="text-gray-600 mt-2">
            Search nearby routes, along-route matches, and book seats fast.
          </p>
        </div>
      </section>

      <section className="bg-gray-50">
        <div className="max-w-6xl mx-auto px-4 py-12 grid md:grid-cols-3 gap-8">
          <div>
            <h3 className="text-lg font-semibold text-gray-900">Start a Trip</h3>
            <p className="text-gray-700 mt-2">
              Post your route, set your price, and make every seat count.
                You’re in full control — from stops to who rides with you.
            </p>
          </div>
          <div>
            <h3 className="text-lg font-semibold text-gray-900">Join a Ride</h3>
            <p className="text-gray-700 mt-2">
              Find trusted drivers heading your way.
              Ride comfortably, safely, and affordably to your destination.
            </p>
          </div>
          <div>
            <h3 className="text-lg font-semibold text-gray-900">Split the Journey</h3>
            <p className="text-gray-700 mt-2">
              Share fuel and toll costs, reduce your carbon footprint,
              and make travel smarter together.
            </p>
          </div>
        </div>
      </section>

      <section className="bg-white">
        <div className="max-w-6xl mx-auto px-4 py-10 flex flex-col md:flex-row items-center justify-between gap-4">
          <h3 className="text-xl font-medium text-gray-900">
            Ready to roll? Publish your next ride.
          </h3>
          <Link
            to="/create"
            className="inline-flex items-center rounded-xl px-5 py-2.5 bg-amber-500 text-white hover:bg-amber-600"
          >
            Publish a ride
          </Link>
        </div>
      </section>
    </div>
  );
}
