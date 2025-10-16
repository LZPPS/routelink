import { Link, useNavigate } from "react-router-dom";
import { useAtom } from "jotai";
import { meAtom, setTokenAtom, tokenAtom } from "../atoms";

export default function Header() {
  const [token] = useAtom(tokenAtom);
  const [me] = useAtom(meAtom);
  const [, setToken] = useAtom(setTokenAtom);
  const nav = useNavigate();

  function logout() {
    setToken(null);
    nav("/login");
  }

  return (
    <header className="sticky top-0 z-50 bg-white/80 backdrop-blur border-b border-black/5">
      <div className="container-narrow h-14 flex items-center justify-between">
        <Link to="/" className="font-semibold tracking-tight">Routelink</Link>
        <nav className="flex items-center gap-4 text-sm">
          <Link to="/trips" className="btn-ghost">Trips</Link>
          <Link to="/search" className="btn-ghost">Search</Link>

          {token ? (
            <div className="flex items-center gap-2">
              <span className="chip">{me?.name ?? "You"}</span>
              <button onClick={logout} className="btn">Logout</button>
            </div>
          ) : (
            <div className="flex items-center gap-2">
              <Link to="/login" className="btn">Login</Link>
              <Link to="/signup" className="btn-primary">Sign up</Link>
            </div>
          )}
        </nav>
      </div>
    </header>
  );
}
