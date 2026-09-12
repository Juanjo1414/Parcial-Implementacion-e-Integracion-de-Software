import { useState, type FormEvent } from "react";
import { Navigate, useLocation, useNavigate } from "react-router-dom";
import { LogIn } from "lucide-react";
import { useAuth } from "../context/AuthContext";
import { ApiError } from "../api/client";

export function Login() {
  const { user, login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setSubmitting] = useState(false);

  if (user) {
    const redirectTo = (location.state as { from?: string } | null)?.from ?? "/";
    return <Navigate to={redirectTo} replace />;
  }

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await login(username, password);
      navigate("/", { replace: true });
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "No se pudo iniciar sesión.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth-screen">
      <div className="auth-card">
        <div className="auth-card__brand">
          <svg width="34" height="34" viewBox="0 0 24 24" fill="none" stroke="var(--color-primary)" strokeWidth="2">
            <path d="M4 20 L4 10 Q4 6 8 6 Q10 6 10 9 L10 12 Q13 12 13 9 Q13 5 17 5 Q20 5 20 9 L20 20" strokeLinecap="round" strokeLinejoin="round" />
          </svg>
          <div>
            <h1>Camel Racing</h1>
            <span className="eyebrow">League Control Center</span>
          </div>
        </div>

        <form className="stack" onSubmit={handleSubmit} noValidate>
          <div className="field">
            <label htmlFor="username">Usuario</label>
            <input
              id="username"
              name="username"
              autoComplete="username"
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              required
            />
          </div>
          <div className="field">
            <label htmlFor="password">Contraseña</label>
            <input
              id="password"
              type="password"
              name="password"
              autoComplete="current-password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              required
            />
          </div>

          {error && <p className="field__error" role="alert">{error}</p>}

          <button type="submit" className="btn btn--primary" disabled={isSubmitting}>
            <LogIn size={16} />
            {isSubmitting ? "Ingresando…" : "Iniciar sesión"}
          </button>
        </form>

        <div className="auth-card__hint">
          Cuentas de demostración: <code>admin</code> / <code>Admin123!</code>,{" "}
          <code>organizador</code> / <code>Organizer123!</code>, <code>viewer</code> / <code>Viewer123!</code>
        </div>
      </div>
    </div>
  );
}
