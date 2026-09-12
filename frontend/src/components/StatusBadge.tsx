import { label } from "../utils/formatters";

type BadgeTone = "success" | "warning" | "danger" | "info" | "accent" | "muted";

const TONE_BY_STATUS: Record<string, BadgeTone> = {
  ACTIVE: "success",
  INJURED: "warning",
  SUSPENDED: "danger",
  RETIRED: "muted",
  INACTIVE: "muted",
  DRAFT: "muted",
  OPEN_FOR_REGISTRATION: "info",
  CLOSED_FOR_REGISTRATION: "warning",
  IN_PROGRESS: "accent",
  COMPLETED: "success",
  CANCELLED: "danger",
  PENDING: "warning",
  APPROVED: "success",
  REJECTED: "danger",
  FINISHED: "success",
  DISQUALIFIED: "danger",
  DID_NOT_FINISH: "warning",
  DID_NOT_START: "muted",
};

export function StatusBadge({ status }: { status: string }) {
  const tone = TONE_BY_STATUS[status] ?? "muted";
  return <span className={`badge badge--${tone}`}>{label(status)}</span>;
}
