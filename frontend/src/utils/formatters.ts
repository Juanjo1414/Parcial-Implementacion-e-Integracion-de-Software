export function formatDate(value: string | null | undefined): string {
  if (!value) return "—";
  return new Date(value).toLocaleDateString("es-CO", { year: "numeric", month: "short", day: "2-digit" });
}

export function formatDateTime(value: string | null | undefined): string {
  if (!value) return "—";
  return new Date(value).toLocaleString("es-CO", {
    year: "numeric",
    month: "short",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
}

// El <input type="datetime-local"> necesita "YYYY-MM-DDTHH:mm" exacto,
// sin segundos ni zona horaria, o el navegador rechaza el valor en silencio.
export function toDateTimeLocalInput(value: string | null | undefined): string {
  if (!value) return "";
  return value.slice(0, 16);
}

export function formatSeconds(value: number | null | undefined): string {
  if (value === null || value === undefined) return "—";
  return `${value.toFixed(2)} s`;
}

const TYPE_LABELS: Record<string, string> = {
  DWARF: "Enano",
  CAMEL: "Camello",
  MEDIUM: "Mediano",
  OTHER: "Otro",
};

const STATUS_LABELS: Record<string, string> = {
  ACTIVE: "Activo",
  INJURED: "Lesionado",
  SUSPENDED: "Suspendido",
  RETIRED: "Retirado",
  INACTIVE: "Inactivo",
  DRAFT: "Borrador",
  OPEN_FOR_REGISTRATION: "Inscripciones abiertas",
  CLOSED_FOR_REGISTRATION: "Inscripciones cerradas",
  IN_PROGRESS: "En curso",
  COMPLETED: "Finalizada",
  CANCELLED: "Cancelada",
  PENDING: "Pendiente",
  APPROVED: "Aprobada",
  REJECTED: "Rechazada",
  FINISHED: "Finalizó",
  DISQUALIFIED: "Descalificado",
  DID_NOT_FINISH: "No terminó",
  DID_NOT_START: "No se presentó",
  INDIVIDUAL: "Individual",
  TEAM: "Por equipos",
  MIXED: "Mixta",
};

export function label(code: string | null | undefined): string {
  if (!code) return "—";
  return TYPE_LABELS[code] ?? STATUS_LABELS[code] ?? code;
}

const ROLE_LABELS: Record<string, string> = {
  ADMIN: "Administrador",
  ORGANIZER: "Organizador",
  VIEWER: "Espectador",
};

export function roleLabel(role: string | null | undefined): string {
  if (!role) return "—";
  return ROLE_LABELS[role] ?? role;
}
