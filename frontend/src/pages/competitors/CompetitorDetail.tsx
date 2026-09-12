import { Link, useNavigate, useParams } from "react-router-dom";
import { ArrowLeft, Pencil, Trash2 } from "lucide-react";
import { useAsync } from "../../hooks/useAsync";
import { competitorsApi } from "../../api/competitors";
import { useAuth, isAdmin } from "../../context/AuthContext";
import { useToast } from "../../context/ToastContext";
import { useConfirmDialog } from "../../components/ConfirmDialog";
import { StatusBadge } from "../../components/StatusBadge";
import { ErrorState, Skeleton } from "../../components/StatePanel";
import { ApiError } from "../../api/client";
import { formatDate, formatDateTime, label } from "../../utils/formatters";
import type { CompetitorStatus } from "../../api/types";

export function CompetitorDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
  const { notifySuccess, notifyError } = useToast();
  const { confirm, dialog } = useConfirmDialog();

  const { data: competitor, isLoading, error, reload } = useAsync(
    () => competitorsApi.findById(id!),
    [id],
  );

  const canManage = isAdmin(user?.role);

  const handleStatusChange = async (status: CompetitorStatus) => {
    try {
      await competitorsApi.changeStatus(id!, status);
      notifySuccess(`Estado actualizado a ${label(status)}.`);
      reload();
    } catch (err) {
      notifyError(err instanceof ApiError ? err.message : "No se pudo actualizar el estado.");
    }
  };

  const handleDelete = () => {
    confirm({
      title: "Retirar competidor",
      description:
        "Si el competidor tiene resultados oficiales, esto lo marcará como RETIRED en lugar de eliminarlo. ¿Deseas continuar?",
      confirmLabel: "Sí, eliminar",
      onConfirm: async () => {
        try {
          await competitorsApi.remove(id!);
          notifySuccess("Competidor eliminado.");
          navigate("/competitors");
        } catch (err) {
          if (err instanceof ApiError && err.status === 409) {
            await competitorsApi.changeStatus(id!, "RETIRED");
            notifySuccess("El competidor tenía historial oficial: se marcó como RETIRED.");
            reload();
          } else {
            notifyError(err instanceof ApiError ? err.message : "No se pudo eliminar el competidor.");
            throw err;
          }
        }
      },
    });
  };

  if (isLoading) {
    return (
      <div className="page">
        <Skeleton height={32} width={220} />
        <Skeleton height={220} />
      </div>
    );
  }

  if (error || !competitor) {
    return (
      <div className="page">
        <ErrorState title="No se encontró el competidor" description={error ?? undefined} />
      </div>
    );
  }

  return (
    <div className="page">
      <Link to="/competitors" className="row text-muted" style={{ textDecoration: "none", width: "fit-content" }}>
        <ArrowLeft size={16} /> Volver a competidores
      </Link>

      <div className="page-header">
        <div>
          <span className="eyebrow">{label(competitor.type)}</span>
          <h1>{competitor.name}</h1>
          <span className="text-muted">@{competitor.nickname}</span>
        </div>
        {canManage && (
          <div className="row">
            <Link to={`/competitors/${competitor.id}/edit`} className="btn btn--ghost">
              <Pencil size={16} />
              Editar
            </Link>
            <button type="button" className="btn btn--danger" onClick={handleDelete}>
              <Trash2 size={16} />
              Eliminar
            </button>
          </div>
        )}
      </div>

      <div className="grid" style={{ gridTemplateColumns: "2fr 1fr" }}>
        <div className="card stack">
          <div className="row--between">
            <h3>Ficha</h3>
            <StatusBadge status={competitor.status} />
          </div>
          <dl className="form-grid" style={{ margin: 0 }}>
            <Field label="País de origen" value={competitor.originCountry ?? "—"} />
            <Field label="Fecha de nacimiento" value={formatDate(competitor.birthDate)} />
            <Field label="Peso" value={`${competitor.weight} kg`} />
            <Field label="Altura" value={`${competitor.height} m`} />
            <Field label="Registrado" value={formatDateTime(competitor.registeredAt)} />
          </dl>
        </div>

        <div className="card stack">
          <h3>Estadísticas</h3>
          <Field label="Victorias" value={String(competitor.wins)} />
          <Field label="Derrotas" value={String(competitor.losses)} />
          <Field label="Carreras completadas" value={String(competitor.racesCompleted)} />

          {canManage && (
            <div className="field">
              <label htmlFor="status-select">Cambiar estado</label>
              <select
                id="status-select"
                value={competitor.status}
                onChange={(e) => handleStatusChange(e.target.value as CompetitorStatus)}
              >
                {["ACTIVE", "INJURED", "SUSPENDED", "RETIRED"].map((status) => (
                  <option key={status} value={status}>
                    {label(status)}
                  </option>
                ))}
              </select>
            </div>
          )}
        </div>
      </div>
      {dialog}
    </div>
  );
}

function Field({ label: fieldLabel, value }: { label: string; value: string }) {
  return (
    <div>
      <dt className="text-muted" style={{ fontSize: "0.8rem" }}>
        {fieldLabel}
      </dt>
      <dd style={{ margin: 0, fontWeight: 600 }}>{value}</dd>
    </div>
  );
}
