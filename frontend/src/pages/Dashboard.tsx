import type { ReactNode } from "react";
import { Link } from "react-router-dom";
import { Flag, Trophy, Users, Shield } from "lucide-react";
import { useAsync } from "../hooks/useAsync";
import { competitorsApi } from "../api/competitors";
import { racesApi } from "../api/races";
import { teamsApi } from "../api/teams";
import { Skeleton } from "../components/StatePanel";
import { StatusBadge } from "../components/StatusBadge";
import { formatDateTime } from "../utils/formatters";
import type { RaceResponse } from "../api/types";

async function loadDashboard() {
  const [competitorsPage, races, teams] = await Promise.all([
    competitorsApi.findAll({ size: 200 }),
    racesApi.findAll(),
    teamsApi.findAll(),
  ]);
  return { competitors: competitorsPage.content, races, teams };
}

export function Dashboard() {
  const { data, isLoading, error } = useAsync(loadDashboard, []);

  if (isLoading) {
    return (
      <div className="page">
        <Skeleton height={36} width={280} />
        <div className="grid grid--cards">
          <Skeleton height={110} />
          <Skeleton height={110} />
          <Skeleton height={110} />
          <Skeleton height={110} />
        </div>
        <Skeleton height={240} />
      </div>
    );
  }

  if (error || !data) {
    return (
      <div className="page">
        <p className="field__error">{error ?? "No se pudo cargar el panel."}</p>
      </div>
    );
  }

  const activeCompetitors = data.competitors.filter((c) => c.status === "ACTIVE").length;
  const camelWins = data.competitors.filter((c) => c.type === "CAMEL").reduce((sum, c) => sum + c.wins, 0);
  const dwarfWins = data.competitors.filter((c) => c.type === "DWARF").reduce((sum, c) => sum + c.wins, 0);

  const upcomingRaces = [...data.races]
    .filter((r) => r.status === "OPEN_FOR_REGISTRATION" || r.status === "DRAFT" || r.status === "IN_PROGRESS")
    .sort((a, b) => new Date(a.scheduledAt).getTime() - new Date(b.scheduledAt).getTime())
    .slice(0, 5);

  const recentlyCompleted = [...data.races]
    .filter((r) => r.status === "COMPLETED")
    .sort((a, b) => new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime())
    .slice(0, 5);

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <span className="eyebrow">Panel de control</span>
          <h1>Bienvenido de nuevo</h1>
        </div>
      </div>

      <div className="grid grid--cards">
        <KpiCard icon={<Flag size={20} />} label="Carreras totales" value={data.races.length} />
        <KpiCard icon={<Users size={20} />} label="Competidores activos" value={activeCompetitors} />
        <KpiCard icon={<Shield size={20} />} label="Equipos registrados" value={data.teams.length} />
        <KpiCard
          icon={<Trophy size={20} />}
          label="Victorias camellos vs. enanos"
          value={`${camelWins} – ${dwarfWins}`}
        />
      </div>

      <div className="grid" style={{ gridTemplateColumns: "1fr 1fr" }}>
        <RaceMiniList title="Próximas carreras" races={upcomingRaces} empty="No hay carreras programadas por ahora." />
        <RaceMiniList title="Últimos podios" races={recentlyCompleted} empty="Todavía no hay carreras finalizadas." />
      </div>
    </div>
  );
}

function KpiCard({ icon, label, value }: { icon: ReactNode; label: string; value: string | number }) {
  return (
    <div className="card kpi-card">
      <div className="row text-muted">
        {icon}
        <span>{label}</span>
      </div>
      <span className="kpi-card__value">{value}</span>
    </div>
  );
}

function RaceMiniList({ title, races, empty }: { title: string; races: RaceResponse[]; empty: string }) {
  return (
    <div className="card">
      <h3 style={{ marginBottom: "var(--space-4)" }}>{title}</h3>
      {races.length === 0 ? (
        <p className="text-muted">{empty}</p>
      ) : (
        <div className="stack">
          {races.map((race) => (
            <Link
              key={race.id}
              to={`/races/${race.id}`}
              className="row--between"
              style={{ textDecoration: "none", color: "inherit" }}
            >
              <div>
                <strong>{race.name}</strong>
                <div className="text-muted" style={{ fontSize: "0.82rem" }}>
                  {formatDateTime(race.scheduledAt)}
                </div>
              </div>
              <StatusBadge status={race.status} />
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
