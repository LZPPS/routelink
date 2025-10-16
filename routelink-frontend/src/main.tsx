import React from "react";
import ReactDOM from "react-dom/client";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import "./index.css";

import AuthProvider, { useAuth } from "./providers/AuthProvider";

import Landing from "./pages/Landing";
import Login from "./pages/Login";
import Signup from "./pages/Signup";

import SearchUnified from "./pages/SearchUnified";
import SearchNear from "./pages/SearchNear";     // optional (kept for testing)
import SearchRoute from "./pages/SearchRoute";   // optional (kept for testing)

import Trips from "./pages/Trips";               // driver’s trip list
import CreateTrip from "./pages/CreateTrip";
import DashboardDriver from "./pages/DashboardDriver";

import MyBookings from "./pages/MyBookings";
import Rate from "./pages/Rate";

import TripDetails from "./pages/TripDetails";   // ✅ rider view/book page

import { loadGooglePlaces } from "@/lib/loadGoogle";

function Private({ children }: { children: React.ReactNode }) {
  const { auth } = useAuth();
  if (!auth.token) return <Navigate to="/login" replace />;
  return <>{children}</>;
}

function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Public */}
        <Route path="/" element={<Landing />} />
        <Route path="/login" element={<Login />} />
        <Route path="/signup" element={<Signup />} />

        {/* Search */}
        <Route path="/search" element={<SearchUnified />} />
        <Route path="/search-near" element={<SearchNear />} />
        <Route path="/search-route" element={<SearchRoute />} />

        {/* Rider booking details (must stay TripDetails) */}
        <Route path="/trip/:id" element={<Private><TripDetails /></Private>} />

        {/* Driver & auth-required pages */}
        <Route path="/trips" element={<Private><Trips /></Private>} />
        <Route path="/create" element={<Private><CreateTrip /></Private>} />
        <Route path="/driver" element={<Private><DashboardDriver /></Private>} />
        <Route path="/bookings" element={<Private><MyBookings /></Private>} />
        <Route path="/rate" element={<Private><Rate /></Private>} />

        {/* Fallback */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

// Load Google Places first, then render the app
const key = import.meta.env.VITE_GOOGLE_MAPS_KEY as string;
loadGooglePlaces(key).finally(() => {
  ReactDOM.createRoot(document.getElementById("root")!).render(
    <AuthProvider>
      <App />
    </AuthProvider>
  );
});
