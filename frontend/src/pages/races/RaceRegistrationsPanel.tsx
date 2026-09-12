import { useState } from "react";
import { Link } from "react-router-dom";
import { Check, X, UserPlus, ClipboardList } from "lucide-react";
import { useAsync } from "../../hooks/useAsync";
import { registrationsApi } from "../../api/registrations";
import { competitorsApi } from "../../api/competitors";
import { teamsApi } from "../../api/teams";
import { useToast } from "../../context/ToastContext";
import { useConfirmDialog } from "../../components/ConfirmDialog";
import { StatusBadge } from "../../components/StatusBadge";
import { EmptyState, ErrorState, SkeletonTable } from "../../components/StatePanel";
import { ApiError } from "../../api/client";
import type { RaceResponse } from "../../api/types";

export function RaceRegistrationsPanel({ race, canManage }: { race: RaceResponse; canManage: boolean }) {
  const { notifySuccess, notifyError } = useToast();
  const { confirm, dialog } = useConfirmDialog();
  const [rejectReason, setRejectReason] = useState<Record<string, string>>({});

  const { data: registrations, isLoading, error, reload } = useAsync(
    () => registrationsApi.findByRace(race.id),
    [race.id],
  );

  const canRegisterNow = canManage && race.status === "OPEN_FOR_REGISTRATION";

  const [participantKind, setParticipantKind] = useState<"COMPETITOR" | "TEAM">(
    race.type === "TEAM" ? "TEAM" : "COMPETITOR",
  );
  const [selectedId, setSelectedId] = useState("");
  const [isRegistering, setRegistering] = useState(false);

  const { data: competitors } = useAsync(
    () => (canRegisterNow ? competitorsApi.findAll({ status: "ACTIVE", size: 200 }) : Promise.resolve(null)),
    [canRegisterNow],
  );
  const { data: teams } = useAsync(() => (canRegisterNow ? teamsApi.findAll() : Promise.resolve(null)), [canRegisterNow]);

  const handleRegister = async () => {
    if (!selectedId) return;
    setRegistering(true);
    try {
      await registrationsApi.register(race.id, {
        competitorId: participantKind === "COMPETITOR" ? selectedId : null,
        teamId: participantKind === "TEAM" ? selectedId : null,
        startingPosition: null,
      });
      notifySuccess("Inscripción registrada.");
      setSelectedId("");
      reload();
    } catch (err) {
      notifyError(err instanceof ApiError ? err.message : "No se pudo registrar la inscripción.");
    } finally {
      setRegistering(false);
    }
  };

  const handleApprove = async (id: string) => {
    try {
      await registrationsApi.approve(id);
      notifySuccess("Inscripción aprobada.");
      reload();
    } catch (err) {
      notifyError(err instanceof ApiError ? err.message : "No se pudo aprobar la inscripción.");
    }
  };

  const handleReject = (id: string) => {
    const reason = rejectReason[id]?.trim();
    if (!reason) {
      notifyError("Debes indicar un motivo de rechazo.");
      return;
    }
    confirm({
      title: "Rechazar inscripción",
      description: `Motivo: "${reason}"`,
      confirmLabel: "Rechazar",
      onConfirm: async () => {
        await registrationsApi.reject(id, reason);
        notifySuccess("Inscripción rechazada.");
        reload();
      },
    });
  };

  if (isLoading) return <SkeletonTable rows={3} />;
  if (error) return <ErrorState title="No se pudieron cargar las inscripciones" description={error} />;

  return (
    <div className="stack">
      {canRegisterNow && (
        <div className="card card--tight row row--wrap">
          {race.type === "MIXED" && (
            <select value={participantKind} onChange={(e) => { setParticipantKind(e.target.value as "COMPETITOR" | "TEAM"); setSelectedId(""); }}>
              <option value="COMPETITOR">Competidor individual</option>
              <option value="TEAM">Equipo</option>
            </select>
          )}
          <select value={selectedId} onChange={(e) => setSelectedId(e.target.value)} style={{ flex: 1, minWidth: 200 }}>
            <option value="">
              {participantKind === "COMPETITOR" ? "Selecciona un competidor activo…" : "Selecciona un equipo…"}
            </option>
            {participantKind === "COMPETITOR"
              ? (competitors?.content ?? []).map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.nickname} — {c.name}
                  </option>
                ))
              : (teams ?? []).map((t) => (
                  <option key={t.id} value={t.id}>
                    {t.name} ({t.members.length} miembros)
                  </option>
                ))}
          </select>
          <button type="button" className="btn btn--primary" disabled={!selectedId || isRegistering} onClick={handleRegister}>
            <UserPlus size={16} />
            Inscribir
          </button>
        </div>
      )}

      {(!registrations || registrations.length === 0) ? (
        <EmptyState icon={<ClipboardList size={40} strokeWidth={1.4} />} title="Todavía no hay inscripciones" description="Cuando alguien se inscriba, aparecerá aquí para su aprobación." />
      ) : (
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr>
                <th>Participante</th>
                <th>Carril</th>
                <th>Estado</th>
                {canManage && <th>Acciones</th>}
              </tr>
            </thead>
            <tbody>
              {registrations.map((registration) => (
                <tr key={registration.id}>
                  <td>
                    {registration.competitorId ? (
                      <Link to={`/competitors/${registration.competitorId}`} style={{ color: "var(--color-accent)", textDecoration: "none" }}>
                        {registration.competitorNickname}
                      </Link>
                    ) : (
                      <Link to={`/teams/${registration.teamId}`} style={{ color: "var(--color-accent)", textDecoration: "none" }}>
                        {registration.teamName}
                      </Link>
                    )}
                  </td>
                  <td>{registration.startingPosition ?? "—"}</td>
                  <td>
                    <StatusBadge status={registration.status} />
                    {registration.status === "REJECTED" && registration.validationNotes && (
                      <div className="text-muted" style={{ fontSize: "0.78rem" }}>{registration.validationNotes}</div>
                    )}
                  </td>
                  {canManage && (
                    <td>
                      {registration.status === "PENDING" ? (
                        <div className="row">
                          <button type="button" className="btn btn--sm btn--ghost" onClick={() => handleApprove(registration.id)}>
                            <Check size={14} /> Aprobar
                          </button>
                          <input
                            placeholder="Motivo de rechazo"
                            style={{ width: 140 }}
                            value={rejectReason[registration.id] ?? ""}
                            onChange={(e) => setRejectReason((prev) => ({ ...prev, [registration.id]: e.target.value }))}
                          />
                          <button type="button" className="btn btn--sm btn--danger" onClick={() => handleReject(registration.id)}>
                            <X size={14} /> Rechazar
                          </button>
                        </div>
                      ) : (
                        <span className="text-faint">—</span>
                      )}
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      {dialog}
    </div>
  );
}
