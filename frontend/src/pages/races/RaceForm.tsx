import { useState, type FormEvent } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { ArrowLeft, Save } from "lucide-react";
import { useAsync } from "../../hooks/useAsync";
import { racesApi } from "../../api/races";
import { useToast } from "../../context/ToastContext";
import { ApiError } from "../../api/client";
import { Skeleton } from "../../components/StatePanel";
import { toDateTimeLocalInput } from "../../utils/formatters";
import type { RaceRequest, RaceType } from "../../api/types";

function defaultForm(): RaceRequest {
  const inTwoWeeks = new Date(Date.now() + 14 * 24 * 60 * 60 * 1000);
  const deadline = new Date(Date.now() + 7 * 24 * 60 * 60 * 1000);
  return {
    name: "",
    description: "",
    scheduledAt: inTwoWeeks.toISOString().slice(0, 16),
    startLocation: "",
    finishLocation: "",
    distanceMeters: 1000,
    maxParticipants: 6,
    type: "MIXED",
    organizerName: "",
    registrationDeadline: deadline.toISOString().slice(0, 16),
  };
}

export function RaceForm() {
  const { id } = useParams<{ id: string }>();
  const isEditing = Boolean(id);
  const navigate = useNavigate();
  const { notifySuccess, notifyError } = useToast();

  const [form, setForm] = useState<RaceRequest>(defaultForm);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [isSubmitting, setSubmitting] = useState(false);
  const [isReady, setReady] = useState(!isEditing);

  useAsync(async () => {
    if (!id) return null;
    const existing = await racesApi.findById(id);
    setForm({
      name: existing.name,
      description: existing.description,
      scheduledAt: toDateTimeLocalInput(existing.scheduledAt),
      startLocation: existing.startLocation,
      finishLocation: existing.finishLocation,
      distanceMeters: existing.distanceMeters,
      maxParticipants: existing.maxParticipants,
      type: existing.type,
      organizerName: existing.organizerName,
      registrationDeadline: toDateTimeLocalInput(existing.registrationDeadline),
    });
    setReady(true);
    return existing;
  }, [id]);

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setFieldErrors({});
    setSubmitting(true);
    try {
      if (isEditing) {
        await racesApi.update(id!, form);
        notifySuccess("Carrera actualizada.");
      } else {
        await racesApi.create(form);
        notifySuccess("Carrera creada en estado DRAFT.");
      }
      navigate("/races");
    } catch (err) {
      if (err instanceof ApiError && err.validationErrors) {
        setFieldErrors(err.validationErrors);
      } else {
        notifyError(err instanceof ApiError ? err.message : "No se pudo guardar la carrera.");
      }
    } finally {
      setSubmitting(false);
    }
  };

  if (!isReady) {
    return (
      <div className="page">
        <Skeleton height={32} width={220} />
        <Skeleton height={360} />
      </div>
    );
  }

  const today = new Date().toISOString().slice(0, 16);

  return (
    <div className="page">
      <Link to="/races" className="row text-muted" style={{ textDecoration: "none", width: "fit-content" }}>
        <ArrowLeft size={16} /> Volver a carreras
      </Link>

      <div className="page-header">
        <div>
          <span className="eyebrow">{isEditing ? "Editar" : "Nueva"}</span>
          <h1>{isEditing ? "Editar carrera" : "Nueva carrera"}</h1>
        </div>
      </div>

      <form className="card stack" onSubmit={handleSubmit} noValidate>
        <div className="form-grid">
          <div className={`field ${fieldErrors.name ? "field--error" : ""}`} style={{ gridColumn: "1 / -1" }}>
            <label htmlFor="name">Nombre de la carrera</label>
            <input id="name" value={form.name} onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))} required />
            {fieldErrors.name && <span className="field__error">{fieldErrors.name}</span>}
          </div>

          <div className={`field ${fieldErrors.type ? "field--error" : ""}`}>
            <label htmlFor="type">Tipo de carrera</label>
            <select id="type" value={form.type} onChange={(e) => setForm((f) => ({ ...f, type: e.target.value as RaceType }))}>
              <option value="INDIVIDUAL">Individual</option>
              <option value="TEAM">Por equipos</option>
              <option value="MIXED">Mixta</option>
            </select>
          </div>

          <div className={`field ${fieldErrors.distanceMeters ? "field--error" : ""}`}>
            <label htmlFor="distance">Distancia (metros)</label>
            <input
              id="distance"
              type="number"
              min="1"
              value={form.distanceMeters}
              onChange={(e) => setForm((f) => ({ ...f, distanceMeters: Number(e.target.value) }))}
              required
            />
            {fieldErrors.distanceMeters && <span className="field__error">{fieldErrors.distanceMeters}</span>}
          </div>

          <div className={`field ${fieldErrors.maxParticipants ? "field--error" : ""}`}>
            <label htmlFor="maxParticipants">Cupo máximo</label>
            <input
              id="maxParticipants"
              type="number"
              min="2"
              value={form.maxParticipants}
              onChange={(e) => setForm((f) => ({ ...f, maxParticipants: Number(e.target.value) }))}
              required
            />
            {fieldErrors.maxParticipants && <span className="field__error">{fieldErrors.maxParticipants}</span>}
          </div>

          <div className="field">
            <label htmlFor="organizerName">Organizador</label>
            <input id="organizerName" value={form.organizerName ?? ""} onChange={(e) => setForm((f) => ({ ...f, organizerName: e.target.value }))} />
          </div>

          <div className="field">
            <label htmlFor="startLocation">Punto de partida</label>
            <input id="startLocation" value={form.startLocation ?? ""} onChange={(e) => setForm((f) => ({ ...f, startLocation: e.target.value }))} />
          </div>

          <div className="field">
            <label htmlFor="finishLocation">Punto de llegada</label>
            <input id="finishLocation" value={form.finishLocation ?? ""} onChange={(e) => setForm((f) => ({ ...f, finishLocation: e.target.value }))} />
          </div>

          <div className={`field ${fieldErrors.scheduledAt ? "field--error" : ""}`}>
            <label htmlFor="scheduledAt">Fecha y hora de la carrera</label>
            <input
              id="scheduledAt"
              type="datetime-local"
              min={today}
              value={form.scheduledAt}
              onChange={(e) => setForm((f) => ({ ...f, scheduledAt: e.target.value }))}
              required
            />
            {fieldErrors.scheduledAt && <span className="field__error">{fieldErrors.scheduledAt}</span>}
          </div>

          <div className={`field ${fieldErrors.registrationDeadline ? "field--error" : ""}`}>
            <label htmlFor="registrationDeadline">Fecha límite de inscripción</label>
            <input
              id="registrationDeadline"
              type="datetime-local"
              max={form.scheduledAt}
              value={form.registrationDeadline}
              onChange={(e) => setForm((f) => ({ ...f, registrationDeadline: e.target.value }))}
              required
            />
            {fieldErrors.registrationDeadline && <span className="field__error">{fieldErrors.registrationDeadline}</span>}
          </div>

          <div className="field" style={{ gridColumn: "1 / -1" }}>
            <label htmlFor="description">Descripción</label>
            <textarea id="description" value={form.description ?? ""} onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))} />
          </div>
        </div>

        <div className="row" style={{ justifyContent: "flex-end" }}>
          <button type="submit" className="btn btn--primary" disabled={isSubmitting}>
            <Save size={16} />
            {isSubmitting ? "Guardando…" : "Guardar"}
          </button>
        </div>
      </form>
    </div>
  );
}
