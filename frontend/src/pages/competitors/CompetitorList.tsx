import { useState } from "react";
import { Link } from "react-router-dom";
import { Plus, Search, Users, ChevronLeft, ChevronRight } from "lucide-react";
import { useAsync } from "../../hooks/useAsync";
import { competitorsApi, type CompetitorFilters } from "../../api/competitors";
import { useAuth, isAdmin } from "../../context/AuthContext";
import { StatusBadge } from "../../components/StatusBadge";
import { EmptyState, ErrorState, SkeletonTable } from "../../components/StatePanel";
import { label } from "../../utils/formatters";
import type { CompetitorStatus, CompetitorType } from "../../api/types";

const TYPES: CompetitorType[] = ["DWARF", "CAMEL", "MEDIUM", "OTHER"];
const STATUSES: CompetitorStatus[] = ["ACTIVE", "INJURED", "SUSPENDED", "RETIRED"];

export function CompetitorList() {
  const { user } = useAuth();
  const [filters, setFilters] = useState<CompetitorFilters>({ name: "", type: "", status: "", page: 0 });

  const { data, isLoading, error, reload } = useAsync(
    () => competitorsApi.findAll(filters),
    [filters.name, filters.type, filters.status, filters.page],
  );

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <span className="eyebrow">Módulo de competidores</span>
          <h1>Competidores</h1>
        </div>
        {isAdmin(user?.role) && (
          <Link to="/competitors/new" className="btn btn--primary">
            <Plus size={16} />
            Nuevo competidor
          </Link>
        )}
      </div>

      <div className="card card--tight row row--wrap">
        <div className="field" style={{ flex: "1 1 220px" }}>
          <label htmlFor="search">Buscar por apodo</label>
          <div className="row" style={{ position: "relative" }}>
            <Search size={16} style={{ position: "absolute", left: 10, color: "var(--color-text-faint)" }} />
            <input
              id="search"
              style={{ paddingLeft: 32 }}
              placeholder="ej. byte"
              value={filters.name}
              onChange={(e) => setFilters((f) => ({ ...f, name: e.target.value, page: 0 }))}
            />
          </div>
        </div>
        <div className="field">
          <label htmlFor="type">Tipo</label>
          <select
            id="type"
            value={filters.type}
            onChange={(e) => setFilters((f) => ({ ...f, type: e.target.value as CompetitorType, page: 0 }))}
          >
            <option value="">Todos</option>
            {TYPES.map((type) => (
              <option key={type} value={type}>
                {label(type)}
              </option>
            ))}
          </select>
        </div>
        <div className="field">
          <label htmlFor="status">Estado</label>
          <select
            id="status"
            value={filters.status}
            onChange={(e) => setFilters((f) => ({ ...f, status: e.target.value as CompetitorStatus, page: 0 }))}
          >
            <option value="">Todos</option>
            {STATUSES.map((status) => (
              <option key={status} value={status}>
                {label(status)}
              </option>
            ))}
          </select>
        </div>
      </div>

      {isLoading && <SkeletonTable rows={6} />}

      {error && !isLoading && (
        <ErrorState title="No se pudo cargar la lista" description={error} action={<button className="btn btn--ghost" onClick={reload}>Reintentar</button>} />
      )}

      {!isLoading && !error && data && data.content.length === 0 && (
        <EmptyState
          icon={<Users size={40} strokeWidth={1.4} />}
          title="Sin competidores para estos filtros"
          description="Ajusta la búsqueda o crea un nuevo competidor."
        />
      )}

      {!isLoading && !error && data && data.content.length > 0 && (
        <>
          <div className="table-wrap">
            <table className="table">
              <thead>
                <tr>
                  <th>Apodo</th>
                  <th>Nombre</th>
                  <th>Tipo</th>
                  <th>Estado</th>
                  <th>Victorias</th>
                  <th>Carreras</th>
                </tr>
              </thead>
              <tbody>
                {data.content.map((competitor) => (
                  <tr key={competitor.id}>
                    <td>
                      <Link to={`/competitors/${competitor.id}`} style={{ color: "var(--color-accent)", textDecoration: "none", fontWeight: 600 }}>
                        {competitor.nickname}
                      </Link>
                    </td>
                    <td>{competitor.name}</td>
                    <td>{label(competitor.type)}</td>
                    <td>
                      <StatusBadge status={competitor.status} />
                    </td>
                    <td>{competitor.wins}</td>
                    <td>{competitor.racesCompleted}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="row--between">
            <span className="text-muted">
              Página {data.number + 1} de {Math.max(data.totalPages, 1)} · {data.totalElements} competidores
            </span>
            <div className="row">
              <button
                type="button"
                className="btn btn--ghost btn--sm"
                disabled={data.number === 0}
                onClick={() => setFilters((f) => ({ ...f, page: (f.page ?? 0) - 1 }))}
              >
                <ChevronLeft size={16} />
                Anterior
              </button>
              <button
                type="button"
                className="btn btn--ghost btn--sm"
                disabled={data.number + 1 >= data.totalPages}
                onClick={() => setFilters((f) => ({ ...f, page: (f.page ?? 0) + 1 }))}
              >
                Siguiente
                <ChevronRight size={16} />
              </button>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
