import { Link } from "react-router-dom";
import { MapPinOff } from "lucide-react";

export function NotFound() {
  return (
    <div className="status-screen">
      <div className="stack" style={{ alignItems: "center" }}>
        <MapPinOff size={48} color="var(--color-text-faint)" />
        <div className="status-screen__code">404</div>
        <h2>Esta pista no existe</h2>
        <p className="text-muted">La página que buscas no está en el mapa de la carrera. Puede que la URL esté mal escrita.</p>
        <Link to="/" className="btn btn--primary">
          Volver al panel
        </Link>
      </div>
    </div>
  );
}
