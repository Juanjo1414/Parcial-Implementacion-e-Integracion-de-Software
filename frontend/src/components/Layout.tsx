import { useState } from "react";
import { NavLink, Outlet } from "react-router-dom";
import {
  LayoutDashboard,
  Users,
  Shield,
  Flag,
  ClipboardList,
  Trophy,
  ScrollText,
  UserCircle,
  LogOut,
  Menu,
  X,
} from "lucide-react";
import { useAuth, isAdmin } from "../context/AuthContext";
import { roleLabel } from "../utils/formatters";

const NAV_ITEMS = [
  { to: "/", label: "Panel", icon: LayoutDashboard, end: true },
  { to: "/competitors", label: "Competidores", icon: Users },
  { to: "/teams", label: "Equipos", icon: Shield },
  { to: "/races", label: "Carreras", icon: Flag },
  { to: "/registrations", label: "Inscripciones", icon: ClipboardList },
  { to: "/standings", label: "Clasificación", icon: Trophy },
];

export function Layout() {
  const { user, logout } = useAuth();
  const [isSidebarOpen, setSidebarOpen] = useState(false);

  return (
    <div className="app-shell">
      <aside className={`sidebar ${isSidebarOpen ? "is-open" : ""}`}>
        <div className="sidebar__brand">
          <BrandMark />
          <h1>
            Camel<span>Racing</span>
          </h1>
        </div>
        <nav className="sidebar__nav">
          {NAV_ITEMS.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className={({ isActive }) => `sidebar__link ${isActive ? "is-active" : ""}`}
              onClick={() => setSidebarOpen(false)}
            >
              <item.icon size={18} />
              {item.label}
            </NavLink>
          ))}
          {isAdmin(user?.role) && (
            <NavLink
              to="/audit"
              className={({ isActive }) => `sidebar__link ${isActive ? "is-active" : ""}`}
              onClick={() => setSidebarOpen(false)}
            >
              <ScrollText size={18} />
              Auditoría
            </NavLink>
          )}
        </nav>
        <div className="sidebar__footer">
          <NavLink to="/profile" className="sidebar__user" style={{ textDecoration: "none" }}>
            <UserCircle size={20} />
            <span>
              {user?.username}
              <br />
              <span className="text-muted" style={{ fontWeight: 500, fontSize: "0.78rem" }}>
                {roleLabel(user?.role)}
              </span>
            </span>
          </NavLink>
          <button type="button" className="btn btn--ghost btn--sm" onClick={logout}>
            <LogOut size={16} />
            Cerrar sesión
          </button>
        </div>
      </aside>

      <div className="app-main">
        <header className="topbar">
          <button
            type="button"
            className="btn btn--ghost btn--icon topbar__menu-toggle"
            aria-label="Abrir menú"
            onClick={() => setSidebarOpen((open) => !open)}
          >
            {isSidebarOpen ? <X size={18} /> : <Menu size={18} />}
          </button>
          <span className="eyebrow">The Great EIA Camel vs. Dwarf Racing System</span>
        </header>
        <Outlet />
      </div>
    </div>
  );
}

function BrandMark() {
  return (
    <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ color: "var(--color-primary)" }}>
      <path
        d="M4 20 L4 10 Q4 6 8 6 Q10 6 10 9 L10 12 Q13 12 13 9 Q13 5 17 5 Q20 5 20 9 L20 20"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
}
