import type { ReactNode } from "react";
import { AlertTriangle, Inbox } from "lucide-react";

interface StatePanelProps {
  icon?: ReactNode;
  title: string;
  description?: string;
  action?: ReactNode;
}

export function EmptyState({ icon, title, description, action }: StatePanelProps) {
  return (
    <div className="state-panel">
      {icon ?? <Inbox size={40} strokeWidth={1.4} />}
      <h3>{title}</h3>
      {description && <p className="text-muted">{description}</p>}
      {action}
    </div>
  );
}

export function ErrorState({ title, description, action }: StatePanelProps) {
  return (
    <div className="state-panel">
      <AlertTriangle size={40} strokeWidth={1.4} color="var(--color-danger)" />
      <h3>{title}</h3>
      {description && <p className="text-muted">{description}</p>}
      {action}
    </div>
  );
}

export function Skeleton({ height = 16, width = "100%" }: { height?: number | string; width?: number | string }) {
  return <div className="skeleton" style={{ height, width }} />;
}

export function SkeletonTable({ rows = 5 }: { rows?: number }) {
  return (
    <div className="stack">
      {Array.from({ length: rows }).map((_, index) => (
        <Skeleton key={index} height={44} />
      ))}
    </div>
  );
}
