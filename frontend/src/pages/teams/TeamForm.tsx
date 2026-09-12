import { useState, type FormEvent } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { ArrowLeft, Save } from "lucide-react";
import { useAsync } from "../../hooks/useAsync";
import { teamsApi } from "../../api/teams";
import { useToast } from "../../context/ToastContext";
import { ApiError } from "../../api/client";
import { Skeleton } from "../../components/StatePanel";
import type { TeamRequest } from "../../api/types";

const EMPTY_FORM: TeamRequest = { name: "", description: "", coachName: "" };

export function TeamForm() {
  const { id } = useParams<{ id: string }>();
  const isEditing = Boolean(id);
  const navigate = useNavigate();
  const { notifySuccess, notifyError } = useToast();

  const [form, setForm] = useState<TeamRequest>(EMPTY_FORM);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [isSubmitting, setSubmitting] = useState(false);
  const [isReady, setReady] = useState(!isEditing);

  useAsync(async () => {
    if (!id) return null;
    const existing = await teamsApi.findById(id);
    setForm({ name: existing.name, description: existing.description, coachName: existing.coachName });
    setReady(true);
    return existing;
  }, [id]);

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setFieldErrors({});
    setSubmitting(true);
    try {
      if (isEditing) {
        await teamsApi.update(id!, form);
        notifySuccess("Equipo actualizado.");
      } else {
        await teamsApi.create(form);
        notifySuccess("Equipo creado.");
      }
      navigate("/teams");
    } catch (err) {
      if (err instanceof ApiError && err.validationErrors) {
        setFieldErrors(err.validationErrors);
      } else {
        notifyError(err instanceof ApiError ? err.message : "No se pudo guardar el equipo.");
      }
    } finally {
      setSubmitting(false);
    }
  };

  if (!isReady) {
    return (
      <div className="page">
        <Skeleton height={32} width={220} />
        <Skeleton height={280} />
      </div>
    );
  }

  return (
    <div className="page">
      <Link to="/teams" className="row text-muted" style={{ textDecoration: "none", width: "fit-content" }}>
        <ArrowLeft size={16} /> Volver a equipos
      </Link>

      <div className="page-header">
        <div>
          <span className="eyebrow">{isEditing ? "Editar" : "Nuevo"}</span>
          <h1>{isEditing ? "Editar equipo" : "Nuevo equipo"}</h1>
        </div>
      </div>

      <form className="card stack" onSubmit={handleSubmit} noValidate>
        <div className={`field ${fieldErrors.name ? "field--error" : ""}`}>
          <label htmlFor="name">Nombre del equipo</label>
          <input id="name" value={form.name} onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))} required />
          {fieldErrors.name && <span className="field__error">{fieldErrors.name}</span>}
        </div>

        <div className="field">
          <label htmlFor="description">Descripción</label>
          <textarea
            id="description"
            value={form.description ?? ""}
            onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))}
          />
        </div>

        <div className="field">
          <label htmlFor="coachName">Entrenador o responsable</label>
          <input id="coachName" value={form.coachName ?? ""} onChange={(e) => setForm((f) => ({ ...f, coachName: e.target.value }))} />
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
