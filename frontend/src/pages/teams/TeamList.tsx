import { Link } from "react-router-dom";
import { Plus, Shield } from "lucide-react";
import { useAsync } from "../../hooks/useAsync";
import { teamsApi } from "../../api/teams";
import { useAuth, isAdmin } from "../../context/AuthContext";
import { StatusBadge } from "../../components/StatusBadge";
import { EmptyState, ErrorState, SkeletonTable } from "../../components/StatePanel";

export function TeamList() {
  const { user } = useAuth();
  const { data: teams, isLoading, error, reload } = useAsync(() => teamsApi.findAll(), []);

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <span className="eyebrow">Módulo de equipos</span>
          <h1>Equipos</h1>
        </div>
        {isAdmin(user?.role) && (
          <Link to="/teams/new" className="btn btn--primary">
            <Plus size={16} />
            Nuevo equipo
          </Link>
        )}
      </div>

      {isLoading && <SkeletonTable rows={4} />}

      {error && !isLoading && (
        <ErrorState title="No se pudo cargar la lista" description={error} action={<button className="btn btn--ghost" onClick={reload}>Reintentar</button>} />
      )}

      {!isLoading && !error && teams && teams.length === 0 && (
        <EmptyState icon={<Shield size={40} strokeWidth={1.4} />} title="Todavía no hay equipos" description="Crea el primer equipo para empezar a inscribirlo en carreras." />
      )}

      {!isLoading && !error && teams && teams.length > 0 && (
        <div className="grid grid--cards">
          {teams.map((team) => (
            <Link key={team.id} to={`/teams/${team.id}`} className="card stack" style={{ textDecoration: "none", color: "inherit" }}>
              <div className="row--between">
                <h3>{team.name}</h3>
                <StatusBadge status={team.status} />
              </div>
              <p className="text-muted" style={{ minHeight: 40 }}>{team.description || "Sin descripción."}</p>
              <div className="row--between text-muted" style={{ fontSize: "0.85rem" }}>
                <span>{team.members.length} miembros</span>
                <span>{team.wins}V – {team.losses}D</span>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
