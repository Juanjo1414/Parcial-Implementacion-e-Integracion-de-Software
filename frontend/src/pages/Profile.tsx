import { LogOut, UserCircle } from "lucide-react";
import { useAuth } from "../context/AuthContext";
import { roleLabel } from "../utils/formatters";

export function Profile() {
  const { user, logout } = useAuth();

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <span className="eyebrow">Cuenta</span>
          <h1>Mi perfil</h1>
        </div>
      </div>

      <div className="card" style={{ maxWidth: 420 }}>
        <div className="row" style={{ marginBottom: "var(--space-4)" }}>
          <UserCircle size={40} />
          <div>
            <h3>{user?.username}</h3>
            <span className="badge badge--accent">{roleLabel(user?.role)}</span>
          </div>
        </div>
        <p className="text-muted">
          Tu rol determina qué acciones puedes realizar en el sistema. Si necesitas más permisos, pídele a un
          administrador que actualice tu cuenta.
        </p>
        <button type="button" className="btn btn--danger" style={{ marginTop: "var(--space-5)" }} onClick={logout}>
          <LogOut size={16} />
          Cerrar sesión
        </button>
      </div>
    </div>
  );
}
