// src/components/Nav.tsx
import { Link } from "react-router-dom";
import { useAuth } from "../providers/AuthProvider";

export default function Nav() {
  const { auth, logout } = useAuth();
  const user = auth.user;

  return (
    <header className="sticky top-0 z-40 bg-white/70 backdrop-blur border-b border-gray-100">
      <div className="max-w-6xl mx-auto px-4 py-3 flex items-center gap-4">
        <Link to="/" className="flex items-center gap-2">
          <div className="size-8 rounded-lg bg-black" />
          <span className="font-bold text-lg">RouteLink</span>
        </Link>

        <div className="ml-auto flex items-center gap-3">
          <Link className="px-3 py-1.5 rounded-lg border" to="/search">
            Search
          </Link>
          

          {user ? (
            <>
              <Link className="px-3 py-1.5 rounded-lg border" to="/create">
                Post
              </Link>
              <Link className="px-3 py-1.5 rounded-lg border" to="/driver">
                Driver
              </Link>
              <Link className="px-3 py-1.5 rounded-lg border" to="/bookings">
                My bookings
              </Link>
              <Link className="px-3 py-1.5 rounded-lg border" to="/rate">
                Rate
              </Link>
              <button className="px-3 py-1.5 rounded-lg bg-black text-white" onClick={logout}>
                Logout
              </button>
            </>
          ) : (
            <>
              <Link className="px-3 py-1.5 rounded-lg border" to="/login">
                Login
              </Link>
              <Link className="px-3 py-1.5 rounded-lg bg-black text-white" to="/signup">
                Sign up
              </Link>
            </>
          )}
        </div>
      </div>
    </header>
  );
}
