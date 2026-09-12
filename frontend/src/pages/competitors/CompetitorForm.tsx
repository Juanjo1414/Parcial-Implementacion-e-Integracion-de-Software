import { useState, type FormEvent } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { ArrowLeft, Save } from "lucide-react";
import { useAsync } from "../../hooks/useAsync";
import { competitorsApi } from "../../api/competitors";
import { useToast } from "../../context/ToastContext";
import { ApiError } from "../../api/client";
import { Skeleton } from "../../components/StatePanel";
import type { CompetitorRequest, CompetitorType } from "../../api/types";

const EMPTY_FORM: CompetitorRequest = {
  name: "",
  nickname: "",
  type: "DWARF",
  birthDate: null,
  weight: 0,
  height: 0,
  originCountry: "",
};

export function CompetitorForm() {
  const { id } = useParams<{ id: string }>();
  const isEditing = Boolean(id);
  const navigate = useNavigate();
  const { notifySuccess, notifyError } = useToast();

  const [form, setForm] = useState<CompetitorRequest>(EMPTY_FORM);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [isSubmitting, setSubmitting] = useState(false);
  const [isReady, setReady] = useState(!isEditing);

  useAsync(async () => {
    if (!id) return null;
    const existing = await competitorsApi.findById(id);
    setForm({
      name: existing.name,
      nickname: existing.nickname,
      type: existing.type,
      birthDate: existing.birthDate,
      weight: existing.weight,
      height: existing.height,
      originCountry: existing.originCountry,
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
        await competitorsApi.update(id!, form);
        notifySuccess("Competidor actualizado.");
      } else {
        await competitorsApi.create(form);
        notifySuccess("Competidor creado.");
      }
      navigate("/competitors");
    } catch (err) {
      if (err instanceof ApiError && err.validationErrors) {
        setFieldErrors(err.validationErrors);
      } else {
        notifyError(err instanceof ApiError ? err.message : "No se pudo guardar el competidor.");
      }
    } finally {
      setSubmitting(false);
    }
  };

  if (!isReady) {
    return (
      <div className="page">
        <Skeleton height={32} width={220} />
        <Skeleton height={320} />
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
          <span className="eyebrow">{isEditing ? "Editar" : "Nuevo"}</span>
          <h1>{isEditing ? "Editar competidor" : "Nuevo competidor"}</h1>
        </div>
      </div>

      <form className="card stack" onSubmit={handleSubmit} noValidate>
        <div className="form-grid">
          <TextField label="Nombre" value={form.name} error={fieldErrors.name} onChange={(v) => setForm((f) => ({ ...f, name: v }))} required />
          <TextField label="Apodo (único)" value={form.nickname} error={fieldErrors.nickname} onChange={(v) => setForm((f) => ({ ...f, nickname: v }))} required />

          <div className={`field ${fieldErrors.type ? "field--error" : ""}`}>
            <label htmlFor="type">Tipo de competidor</label>
            <select id="type" value={form.type} onChange={(e) => setForm((f) => ({ ...f, type: e.target.value as CompetitorType }))}>
              <option value="DWARF">Enano</option>
              <option value="CAMEL">Camello</option>
              <option value="MEDIUM">Mediano</option>
              <option value="OTHER">Otro</option>
            </select>
            {fieldErrors.type && <span className="field__error">{fieldErrors.type}</span>}
          </div>

          <div className={`field ${fieldErrors.birthDate ? "field--error" : ""}`}>
            <label htmlFor="birthDate">Fecha de nacimiento</label>
            <input
              id="birthDate"
              type="date"
              value={form.birthDate ?? ""}
              onChange={(e) => setForm((f) => ({ ...f, birthDate: e.target.value || null }))}
            />
            {fieldErrors.birthDate && <span className="field__error">{fieldErrors.birthDate}</span>}
          </div>

          <div className={`field ${fieldErrors.weight ? "field--error" : ""}`}>
            <label htmlFor="weight">Peso (kg)</label>
            <input
              id="weight"
              type="number"
              step="0.1"
              min="0"
              value={form.weight}
              onChange={(e) => setForm((f) => ({ ...f, weight: Number(e.target.value) }))}
              required
            />
            {fieldErrors.weight && <span className="field__error">{fieldErrors.weight}</span>}
          </div>

          <div className={`field ${fieldErrors.height ? "field--error" : ""}`}>
            <label htmlFor="height">Altura (m)</label>
            <input
              id="height"
              type="number"
              step="0.01"
              min="0"
              value={form.height}
              onChange={(e) => setForm((f) => ({ ...f, height: Number(e.target.value) }))}
              required
            />
            {fieldErrors.height && <span className="field__error">{fieldErrors.height}</span>}
          </div>

          <TextField
            label="País de origen"
            value={form.originCountry ?? ""}
            onChange={(v) => setForm((f) => ({ ...f, originCountry: v }))}
          />
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

function TextField({
  label: fieldLabel,
  value,
  onChange,
  error,
  required,
}: {
  label: string;
  value: string;
  onChange: (value: string) => void;
  error?: string;
  required?: boolean;
}) {
  return (
    <div className={`field ${error ? "field--error" : ""}`}>
      <label>{fieldLabel}</label>
      <input value={value} onChange={(e) => onChange(e.target.value)} required={required} />
      {error && <span className="field__error">{error}</span>}
    </div>
  );
}
