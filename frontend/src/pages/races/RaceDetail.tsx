import { useMemo } from "react";
import { Link, useNavigate, useParams, useSearchParams } from "react-router-dom";
import { ArrowLeft, Pencil, Trash2 } from "lucide-react";
import { useAsync } from "../../hooks/useAsync";
import { racesApi } from "../../api/races";
import { useAuth, isManager } from "../../context/AuthContext";
import { useToast } from "../../context/ToastContext";
import { useConfirmDialog } from "../../components/ConfirmDialog";
import { StatusBadge } from "../../components/StatusBadge";
import { ErrorState, Skeleton } from "../../components/StatePanel";
import { ApiError } from "../../api/client";
import { formatDateTime, label } from "../../utils/formatters";
import { RaceRegistrationsPanel } from "./RaceRegistrationsPanel";
import { RaceResultsPanel } from "./RaceResultsPanel";
import type { RaceStatus } from "../../api/types";

const NEXT_STATUS: Partial<Record<RaceStatus, { to: RaceStatus; label: string }[]>> = {
  DRAFT: [{ to: "OPEN_FOR_REGISTRATION", label: "Abrir inscripciones" }],
  OPEN_FOR_REGISTRATION: [{ to: "CLOSED_FOR_REGISTRATION", label: "Cerrar inscripciones" }],
  CLOSED_FOR_REGISTRATION: [{ to: "IN_PROGRESS", label: "Iniciar carrera" }],
  IN_PROGRESS: [{ to: "COMPLETED", label: "Finalizar carrera" }],
};

type Tab = "overview" | "registrations" | "results";

export function RaceDetail() {
  const { id } = useParams<{ id: string }>();
  const [searchParams, setSearchParams] = useSearchParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const { notifySuccess, notifyError } = useToast();
  const { confirm, dialog } = useConfirmDialog();
  const canManage = isManager(user?.role);

  const { data: race, isLoading, error, reload } = useAsync(() => racesApi.findById(id!), [id]);

  const activeTab = (searchParams.get("tab") as Tab) || "overview";
  const setTab = (tab: Tab) => setSearchParams(tab === "overview" ? {} : { tab });

  const nextActions = useMemo(() => (race ? NEXT_STATUS[race.status] ?? [] : []), [race]);

  const handleStatusChange = async (status: RaceStatus) => {
    try {
      await racesApi.changeStatus(id!, status);
      notifySuccess(`La carrera pasó a ${label(status)}.`);
      reload();
    } catch (err) {
      notifyError(err instanceof ApiError ? err.message : "No se pudo cambiar el estado de la carrera.");
    }
  };

  const handleCancel = () => {
    confirm({
      title: "Cancelar carrera",
      description: "Esta acción no se puede deshacer. Los participantes ya no podrán inscribirse ni recibir resultados.",
      confirmLabel: "Sí, cancelar",
      onConfirm: () => handleStatusChange("CANCELLED"),
    });
  };

  const handleDelete = () => {
    confirm({
      title: "Eliminar carrera",
      description: "Se eliminará esta carrera de forma permanente. ¿Deseas continuar?",
      confirmLabel: "Sí, eliminar",
      onConfirm: async () => {
        try {
          await racesApi.remove(id!);
          notifySuccess("Carrera eliminada.");
          navigate("/races");
        } catch (err) {
          notifyError(err instanceof ApiError ? err.message : "No se pudo eliminar la carrera.");
          throw err;
        }
      },
    });
  };

  if (isLoading) {
    return (
      <div className="page">
        <Skeleton height={32} width={220} />
        <Skeleton height={260} />
      </div>
    );
  }

  if (error || !race) {
    return (
      <div className="page">
        <ErrorState title="No se encontró la carrera" description={error ?? undefined} />
      </div>
    );
  }

  const isTerminal = race.status === "COMPLETED" || race.status === "CANCELLED";

  return (
    <div className="page">
      <Link to="/races" className="row text-muted" style={{ textDecoration: "none", width: "fit-content" }}>
        <ArrowLeft size={16} /> Volver a carreras
      </Link>

      <div className="page-header">
        <div>
          <span className="eyebrow">{label(race.type)} · {race.distanceMeters} m</span>
          <h1>{race.name}</h1>
          <span className="text-muted">{formatDateTime(race.scheduledAt)}</span>
        </div>
        <div className="row row--wrap" style={{ justifyContent: "flex-end" }}>
          <StatusBadge status={race.status} />
          {canManage && !isTerminal && (
            <>
              {nextActions.map((action) => (
                <button key={action.to} type="button" className="btn btn--accent btn--sm" onClick={() => handleStatusChange(action.to)}>
                  {action.label}
                </button>
              ))}
              <Link to={`/races/${race.id}/edit`} className="btn btn--ghost btn--sm">
                <Pencil size={14} /> Editar
              </Link>
              <button type="button" className="btn btn--danger btn--sm" onClick={handleCancel}>
                Cancelar carrera
              </button>
            </>
          )}
          {canManage && race.status === "DRAFT" && (
            <button type="button" className="btn btn--danger btn--sm" onClick={handleDelete}>
              <Trash2 size={14} /> Eliminar
            </button>
          )}
        </div>
      </div>

      <div className="tabs">
        <button type="button" className={`tab ${activeTab === "overview" ? "is-active" : ""}`} onClick={() => setTab("overview")}>
          Resumen
        </button>
        <button type="button" className={`tab ${activeTab === "registrations" ? "is-active" : ""}`} onClick={() => setTab("registrations")}>
          Inscripciones
        </button>
        <button type="button" className={`tab ${activeTab === "results" ? "is-active" : ""}`} onClick={() => setTab("results")}>
          Resultados
        </button>
      </div>

      {activeTab === "overview" && (
        <div className="card">
          <dl className="form-grid" style={{ margin: 0 }}>
            <Field label="Organizador" value={race.organizerName ?? "—"} />
            <Field label="Salida" value={race.startLocation ?? "—"} />
            <Field label="Llegada" value={race.finishLocation ?? "—"} />
            <Field label="Cupo máximo" value={String(race.maxParticipants)} />
            <Field label="Fecha límite de inscripción" value={formatDateTime(race.registrationDeadline)} />
            <Field label="Última actualización" value={formatDateTime(race.updatedAt)} />
          </dl>
          {race.description && (
            <p className="text-muted" style={{ marginTop: "var(--space-4)" }}>
              {race.description}
            </p>
          )}
        </div>
      )}

      {activeTab === "registrations" && <RaceRegistrationsPanel race={race} canManage={canManage} />}
      {activeTab === "results" && <RaceResultsPanel race={race} canManage={canManage} />}
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
