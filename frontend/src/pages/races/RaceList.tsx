import { useState } from "react";
import { Link } from "react-router-dom";
import { Plus, Flag } from "lucide-react";
import { useAsync } from "../../hooks/useAsync";
import { racesApi } from "../../api/races";
import { useAuth, isManager } from "../../context/AuthContext";
import { StatusBadge } from "../../components/StatusBadge";
import { EmptyState, ErrorState, SkeletonTable } from "../../components/StatePanel";
import { formatDateTime, label } from "../../utils/formatters";
import type { RaceStatus } from "../../api/types";

const STATUS_FILTERS: Array<RaceStatus | "ALL"> = [
  "ALL",
  "DRAFT",
  "OPEN_FOR_REGISTRATION",
  "CLOSED_FOR_REGISTRATION",
  "IN_PROGRESS",
  "COMPLETED",
  "CANCELLED",
];

export function RaceList() {
  const { user } = useAuth();
  const [statusFilter, setStatusFilter] = useState<RaceStatus | "ALL">("ALL");
  const { data: races, isLoading, error, reload } = useAsync(() => racesApi.findAll(), []);

  const visibleRaces = (races ?? [])
    .filter((race) => statusFilter === "ALL" || race.status === statusFilter)
    .sort((a, b) => new Date(b.scheduledAt).getTime() - new Date(a.scheduledAt).getTime());

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <span className="eyebrow">Módulo de carreras</span>
          <h1>Carreras</h1>
        </div>
        {isManager(user?.role) && (
          <Link to="/races/new" className="btn btn--primary">
            <Plus size={16} />
            Nueva carrera
          </Link>
        )}
      </div>

      <div className="row row--wrap">
        {STATUS_FILTERS.map((status) => (
          <button
            key={status}
            type="button"
            className={`btn btn--sm ${statusFilter === status ? "btn--primary" : "btn--ghost"}`}
            onClick={() => setStatusFilter(status)}
          >
            {status === "ALL" ? "Todas" : label(status)}
          </button>
        ))}
      </div>

      {isLoading && <SkeletonTable rows={4} />}

      {error && !isLoading && (
        <ErrorState title="No se pudo cargar la lista" description={error} action={<button className="btn btn--ghost" onClick={reload}>Reintentar</button>} />
      )}

      {!isLoading && !error && visibleRaces.length === 0 && (
        <EmptyState icon={<Flag size={40} strokeWidth={1.4} />} title="No hay carreras con este filtro" description="Prueba con otro estado o crea una nueva carrera." />
      )}

      {!isLoading && !error && visibleRaces.length > 0 && (
        <div className="grid grid--cards">
          {visibleRaces.map((race) => (
            <Link key={race.id} to={`/races/${race.id}`} className="card stack" style={{ textDecoration: "none", color: "inherit" }}>
              <div className="row--between">
                <span className="eyebrow">{label(race.type)}</span>
                <StatusBadge status={race.status} />
              </div>
              <h3>{race.name}</h3>
              <div className="text-muted" style={{ fontSize: "0.85rem" }}>{formatDateTime(race.scheduledAt)}</div>
              <div className="row--between text-muted" style={{ fontSize: "0.85rem" }}>
                <span>{race.distanceMeters} m</span>
                <span>Cupo: {race.maxParticipants}</span>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
