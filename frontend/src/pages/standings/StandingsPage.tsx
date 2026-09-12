import { useState } from "react";
import { Trophy } from "lucide-react";
import { useAsync } from "../../hooks/useAsync";
import { standingsApi } from "../../api/standings";
import { EmptyState, ErrorState, SkeletonTable } from "../../components/StatePanel";

type Board = "competitors" | "teams";

export function StandingsPage() {
  const [board, setBoard] = useState<Board>("competitors");
  const { data: competitorStandings, isLoading: loadingCompetitors, error: errorCompetitors } = useAsync(
    () => standingsApi.competitors(),
    [],
  );
  const { data: teamStandings, isLoading: loadingTeams, error: errorTeams } = useAsync(() => standingsApi.teams(), []);

  const isLoading = board === "competitors" ? loadingCompetitors : loadingTeams;
  const error = board === "competitors" ? errorCompetitors : errorTeams;
  const rows = board === "competitors" ? competitorStandings ?? [] : teamStandings ?? [];
  const sorted = [...rows].sort((a, b) => b.totalPoints - a.totalPoints);
  const podium = sorted.slice(0, 3);

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <span className="eyebrow">Clasificación general</span>
          <h1>Standings</h1>
        </div>
      </div>

      <div className="tabs">
        <button type="button" className={`tab ${board === "competitors" ? "is-active" : ""}`} onClick={() => setBoard("competitors")}>
          Competidores
        </button>
        <button type="button" className={`tab ${board === "teams" ? "is-active" : ""}`} onClick={() => setBoard("teams")}>
          Equipos
        </button>
      </div>

      {isLoading && <SkeletonTable rows={5} />}
      {error && !isLoading && <ErrorState title="No se pudo cargar la clasificación" description={error} />}

      {!isLoading && !error && sorted.length === 0 && (
        <EmptyState icon={<Trophy size={40} strokeWidth={1.4} />} title="Todavía no hay puntos registrados" description="Los puntos aparecerán aquí cuando se completen carreras con resultados." />
      )}

      {!isLoading && !error && sorted.length > 0 && (
        <>
          {podium.length === 3 && (
            <div className="podium">
              <PodiumStep place={2} name={nameOf(podium[1], board)} points={podium[1].totalPoints} />
              <PodiumStep place={1} name={nameOf(podium[0], board)} points={podium[0].totalPoints} />
              <PodiumStep place={3} name={nameOf(podium[2], board)} points={podium[2].totalPoints} />
            </div>
          )}

          <div className="table-wrap">
            <table className="table">
              <thead>
                <tr>
                  <th>#</th>
                  <th>{board === "competitors" ? "Competidor" : "Equipo"}</th>
                  <th>Puntos</th>
                  <th>Victorias</th>
                  <th>Carreras</th>
                </tr>
              </thead>
              <tbody>
                {sorted.map((row, index) => (
                  <tr key={"competitorId" in row ? row.competitorId : row.teamId}>
                    <td>{index + 1}</td>
                    <td>{nameOf(row, board)}</td>
                    <td>
                      <strong>{row.totalPoints}</strong>
                    </td>
                    <td>{row.wins}</td>
                    <td>{row.racesCompleted}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}
    </div>
  );
}

function nameOf(row: { nickname?: string; teamName?: string }, board: Board): string {
  return board === "competitors" ? (row.nickname ?? "—") : (row.teamName ?? "—");
}

function PodiumStep({ place, name, points }: { place: 1 | 2 | 3; name: string; points: number }) {
  return (
    <div className={`podium-step podium-step--${place}`}>
      <div className="podium-medal">{place}º</div>
      <strong>{name}</strong>
      <div className="text-muted">{points} pts</div>
    </div>
  );
}
