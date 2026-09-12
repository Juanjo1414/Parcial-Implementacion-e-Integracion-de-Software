import { Link } from "react-router-dom";
import { ShieldAlert } from "lucide-react";

export function Forbidden() {
  return (
    <div className="status-screen">
      <div className="stack" style={{ alignItems: "center" }}>
        <ShieldAlert size={48} color="var(--color-danger)" />
        <div className="status-screen__code">403</div>
        <h2>Acceso denegado</h2>
        <p className="text-muted">
          El camello no tiene los permisos requeridos para entrar aquí. Vuelve al panel o inicia sesión con otra cuenta.
        </p>
        <Link to="/" className="btn btn--primary">
          Volver al panel
        </Link>
      </div>
    </div>
  );
}
