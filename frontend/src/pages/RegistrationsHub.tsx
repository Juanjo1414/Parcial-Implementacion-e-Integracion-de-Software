import { Link } from "react-router-dom";
import { ClipboardList } from "lucide-react";
import { useAsync } from "../hooks/useAsync";
import { racesApi } from "../api/races";
import { StatusBadge } from "../components/StatusBadge";
import { EmptyState, ErrorState, SkeletonTable } from "../components/StatePanel";
import { formatDateTime } from "../utils/formatters";

// Las inscripciones están siempre ligadas a una carrera concreta en la API
// (/api/races/{raceId}/registrations), así que este panel funciona como
// punto de entrada: se elige la carrera y de ahí se pasa a su pestaña de
// inscripciones, donde vive la gestión de aprobar/rechazar.
export function RegistrationsHub() {
  const { data: races, isLoading, error, reload } = useAsync(() => racesApi.findAll(), []);

  const relevantRaces = (races ?? [])
    .filter((r) => r.status !== "DRAFT")
    .sort((a, b) => new Date(b.scheduledAt).getTime() - new Date(a.scheduledAt).getTime());

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <span className="eyebrow">Módulo de inscripciones</span>
          <h1>Inscripciones por carrera</h1>
        </div>
      </div>

      {isLoading && <SkeletonTable rows={4} />}

      {error && !isLoading && (
        <ErrorState title="No se pudo cargar la lista" description={error} action={<button className="btn btn--ghost" onClick={reload}>Reintentar</button>} />
      )}

      {!isLoading && !error && relevantRaces.length === 0 && (
        <EmptyState icon={<ClipboardList size={40} strokeWidth={1.4} />} title="No hay carreras con inscripciones todavía" />
      )}

      {!isLoading && !error && relevantRaces.length > 0 && (
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr>
                <th>Carrera</th>
                <th>Fecha</th>
                <th>Estado</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {relevantRaces.map((race) => (
                <tr key={race.id}>
                  <td>{race.name}</td>
                  <td>{formatDateTime(race.scheduledAt)}</td>
                  <td>
                    <StatusBadge status={race.status} />
                  </td>
                  <td>
                    <Link to={`/races/${race.id}?tab=registrations`} className="btn btn--ghost btn--sm">
                      Gestionar
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
