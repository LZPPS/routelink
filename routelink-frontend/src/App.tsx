import { Routes, Route, Link } from "react-router-dom";
import Header from "./components/Header";
import Trips from "./pages/Trips";
import NewTrip from "./pages/NewTrip";           // driver post form
import SearchUnified from "./pages/SearchUnified"; // ✅ one-box search (NEAR + ALONG)
import Login from "./pages/Login";
import Signup from "./pages/Signup";

export default function App() {
  return (
    <div className="page">
      <Header />
      <main className="container-narrow">
        <Routes>
          <Route
            path="/"
            element={
              <div className="text-sm card">
                <h1 className="text-xl font-semibold">Routelink UI ✅</h1>
                <p className="mt-2">Minimal UI wired to your backend.</p>
                <ul className="list-disc ml-6 mt-4 space-y-1">
                  <li><Link to="/trips" className="text-blue-600 underline">Trips</Link></li>
                  <li><Link to="/trips/new" className="text-blue-600 underline">New Trip</Link></li>
                  <li><Link to="/search" className="text-blue-600 underline">Search</Link></li>
                  <li>
                    <Link to="/login" className="text-blue-600 underline">Login</Link>
                    {" / "}
                    <Link to="/signup" className="text-blue-600 underline">Sign up</Link>
                  </li>
                </ul>
              </div>
            }
          />
          <Route path="/trips" element={<Trips />} />
          <Route path="/trips/new" element={<NewTrip />} />
          {/* ✅ use the unified search page */}
          <Route path="/search" element={<SearchUnified />} />
          <Route path="/login" element={<Login />} />
          <Route path="/signup" element={<Signup />} />
        </Routes>
      </main>
    </div>
  );
}
