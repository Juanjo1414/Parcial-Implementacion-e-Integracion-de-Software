import { useState } from "react";
import { ChevronLeft, ChevronRight, ScrollText } from "lucide-react";
import { useAsync } from "../../hooks/useAsync";
import { auditApi } from "../../api/audit";
import { EmptyState, ErrorState, SkeletonTable } from "../../components/StatePanel";
import { formatDateTime } from "../../utils/formatters";

export function AuditPage() {
  const [page, setPage] = useState(0);
  const { data, isLoading, error, reload } = useAsync(() => auditApi.findAll(page), [page]);

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <span className="eyebrow">Solo administradores</span>
          <h1>Registro de auditoría</h1>
        </div>
      </div>

      {isLoading && <SkeletonTable rows={6} />}

      {error && !isLoading && (
        <ErrorState title="No se pudo cargar el registro" description={error} action={<button className="btn btn--ghost" onClick={reload}>Reintentar</button>} />
      )}

      {!isLoading && !error && data && data.content.length === 0 && (
        <EmptyState icon={<ScrollText size={40} strokeWidth={1.4} />} title="Todavía no hay acciones registradas" />
      )}

      {!isLoading && !error && data && data.content.length > 0 && (
        <>
          <div className="table-wrap">
            <table className="table">
              <thead>
                <tr>
                  <th>Fecha</th>
                  <th>Usuario</th>
                  <th>Acción</th>
                  <th>Entidad</th>
                  <th>Descripción</th>
                </tr>
              </thead>
              <tbody>
                {data.content.map((entry) => (
                  <tr key={entry.id}>
                    <td>{formatDateTime(entry.timestamp)}</td>
                    <td>{entry.username}</td>
                    <td>{entry.action}</td>
                    <td>{entry.entityType}</td>
                    <td>{entry.description ?? "—"}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <div className="row--between">
            <span className="text-muted">
              Página {data.number + 1} de {Math.max(data.totalPages, 1)}
            </span>
            <div className="row">
              <button type="button" className="btn btn--ghost btn--sm" disabled={data.number === 0} onClick={() => setPage((p) => p - 1)}>
                <ChevronLeft size={16} /> Anterior
              </button>
              <button
                type="button"
                className="btn btn--ghost btn--sm"
                disabled={data.number + 1 >= data.totalPages}
                onClick={() => setPage((p) => p + 1)}
              >
                Siguiente <ChevronRight size={16} />
              </button>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
