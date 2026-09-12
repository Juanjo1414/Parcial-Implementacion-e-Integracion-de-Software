import { useMemo, useState } from "react";
import { Pencil, Save, Trophy } from "lucide-react";
import { useAsync } from "../../hooks/useAsync";
import { resultsApi } from "../../api/results";
import { registrationsApi } from "../../api/registrations";
import { useToast } from "../../context/ToastContext";
import { StatusBadge } from "../../components/StatusBadge";
import { EmptyState, ErrorState, SkeletonTable } from "../../components/StatePanel";
import { ApiError } from "../../api/client";
import { formatSeconds } from "../../utils/formatters";
import type { RaceResponse, ResultRequest, ResultStatus } from "../../api/types";

const RESULT_STATUSES: ResultStatus[] = ["FINISHED", "DISQUALIFIED", "DID_NOT_FINISH", "DID_NOT_START"];

function emptyForm(registrationId: string): ResultRequest {
  return {
    registrationId,
    finalPosition: null,
    completionTimeSeconds: null,
    penaltyTimeSeconds: 0,
    status: "FINISHED",
    notes: "",
  };
}

export function RaceResultsPanel({ race, canManage }: { race: RaceResponse; canManage: boolean }) {
  const { notifySuccess, notifyError } = useToast();

  const { data: results, isLoading, error, reload } = useAsync(() => resultsApi.findByRace(race.id), [race.id]);
  const { data: registrations } = useAsync(() => registrationsApi.findByRace(race.id), [race.id]);

  const [editingResultId, setEditingResultId] = useState<string | null>(null);
  const [form, setForm] = useState<ResultRequest | null>(null);
  const [isSubmitting, setSubmitting] = useState(false);

  const canRecordNow = canManage && race.status === "IN_PROGRESS";

  const eligibleRegistrations = useMemo(() => {
    const resultRegistrationIds = new Set((results ?? []).map((r) => r.registrationId));
    return (registrations ?? []).filter((r) => r.status === "APPROVED" && !resultRegistrationIds.has(r.id));
  }, [registrations, results]);

  const startCreate = () => {
    if (eligibleRegistrations.length === 0) return;
    setEditingResultId(null);
    setForm(emptyForm(eligibleRegistrations[0].id));
  };

  const startEdit = (resultId: string, current: ResultRequest) => {
    setEditingResultId(resultId);
    setForm(current);
  };

  const handleSubmit = async () => {
    if (!form) return;
    setSubmitting(true);
    try {
      if (editingResultId) {
        await resultsApi.update(editingResultId, form);
        notifySuccess("Resultado actualizado.");
      } else {
        await resultsApi.record(race.id, form);
        notifySuccess("Resultado registrado.");
      }
      setForm(null);
      setEditingResultId(null);
      reload();
    } catch (err) {
      notifyError(err instanceof ApiError ? err.message : "No se pudo guardar el resultado.");
    } finally {
      setSubmitting(false);
    }
  };

  if (isLoading) return <SkeletonTable rows={3} />;
  if (error) return <ErrorState title="No se pudieron cargar los resultados" description={error} />;

  return (
    <div className="stack">
      {canRecordNow && !form && eligibleRegistrations.length > 0 && (
        <button type="button" className="btn btn--accent" style={{ width: "fit-content" }} onClick={startCreate}>
          <Trophy size={16} />
          Registrar resultado
        </button>
      )}

      {form && (
        <div className="card stack">
          <h3>{editingResultId ? "Editar resultado" : "Nuevo resultado"}</h3>
          <div className="form-grid">
            {!editingResultId && (
              <div className="field">
                <label>Inscripción</label>
                <select
                  value={form.registrationId}
                  onChange={(e) => setForm((f) => (f ? { ...f, registrationId: e.target.value } : f))}
                >
                  {eligibleRegistrations.map((registration) => (
                    <option key={registration.id} value={registration.id}>
                      {registration.competitorNickname ?? registration.teamName}
                    </option>
                  ))}
                </select>
              </div>
            )}

            <div className="field">
              <label>Estado</label>
              <select
                value={form.status}
                onChange={(e) => setForm((f) => (f ? { ...f, status: e.target.value as ResultStatus } : f))}
              >
                {RESULT_STATUSES.map((status) => (
                  <option key={status} value={status}>
                    {status}
                  </option>
                ))}
              </select>
            </div>

            {form.status === "FINISHED" && (
              <>
                <div className="field">
                  <label>Posición final</label>
                  <input
                    type="number"
                    min="1"
                    value={form.finalPosition ?? ""}
                    onChange={(e) => setForm((f) => (f ? { ...f, finalPosition: Number(e.target.value) } : f))}
                  />
                </div>
                <div className="field">
                  <label>Tiempo (segundos)</label>
                  <input
                    type="number"
                    min="0.01"
                    step="0.01"
                    value={form.completionTimeSeconds ?? ""}
                    onChange={(e) => setForm((f) => (f ? { ...f, completionTimeSeconds: Number(e.target.value) } : f))}
                  />
                </div>
                <div className="field">
                  <label>Penalización (segundos)</label>
                  <input
                    type="number"
                    min="0"
                    step="0.01"
                    value={form.penaltyTimeSeconds ?? 0}
                    onChange={(e) => setForm((f) => (f ? { ...f, penaltyTimeSeconds: Number(e.target.value) } : f))}
                  />
                </div>
              </>
            )}

            <div className="field" style={{ gridColumn: "1 / -1" }}>
              <label>Notas</label>
              <input value={form.notes ?? ""} onChange={(e) => setForm((f) => (f ? { ...f, notes: e.target.value } : f))} />
            </div>
          </div>
          <div className="row" style={{ justifyContent: "flex-end" }}>
            <button type="button" className="btn btn--ghost" onClick={() => { setForm(null); setEditingResultId(null); }}>
              Cancelar
            </button>
            <button type="button" className="btn btn--primary" disabled={isSubmitting} onClick={handleSubmit}>
              <Save size={16} />
              {isSubmitting ? "Guardando…" : "Guardar"}
            </button>
          </div>
        </div>
      )}

      {(!results || results.length === 0) ? (
        <EmptyState icon={<Trophy size={40} strokeWidth={1.4} />} title="Todavía no hay resultados" description="Los resultados aparecerán aquí una vez que se registren los tiempos oficiales." />
      ) : (
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr>
                <th>Posición</th>
                <th>Participante</th>
                <th>Tiempo</th>
                <th>Puntos</th>
                <th>Estado</th>
                {canManage && <th />}
              </tr>
            </thead>
            <tbody>
              {[...results]
                .sort((a, b) => (a.finalPosition ?? 999) - (b.finalPosition ?? 999))
                .map((result) => (
                  <tr key={result.id}>
                    <td>{result.finalPosition ?? "—"}</td>
                    <td>{result.participantLabel}</td>
                    <td>{formatSeconds(result.completionTimeSeconds)}</td>
                    <td>{result.points}</td>
                    <td>
                      <StatusBadge status={result.status} />
                    </td>
                    {canManage && (
                      <td>
                        <button
                          type="button"
                          className="btn btn--sm btn--ghost"
                          onClick={() =>
                            startEdit(result.id, {
                              registrationId: result.registrationId,
                              finalPosition: result.finalPosition,
                              completionTimeSeconds: result.completionTimeSeconds,
                              penaltyTimeSeconds: result.penaltyTimeSeconds,
                              status: result.status,
                              notes: result.notes,
                            })
                          }
                        >
                          <Pencil size={14} /> Editar
                        </button>
                      </td>
                    )}
                  </tr>
                ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
