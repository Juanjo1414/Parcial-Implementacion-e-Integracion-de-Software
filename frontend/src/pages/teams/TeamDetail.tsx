import { useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { ArrowLeft, Pencil, Trash2, UserPlus, UserMinus } from "lucide-react";
import { useAsync } from "../../hooks/useAsync";
import { teamsApi } from "../../api/teams";
import { competitorsApi } from "../../api/competitors";
import { useAuth, isAdmin } from "../../context/AuthContext";
import { useToast } from "../../context/ToastContext";
import { useConfirmDialog } from "../../components/ConfirmDialog";
import { StatusBadge } from "../../components/StatusBadge";
import { ErrorState, Skeleton, EmptyState } from "../../components/StatePanel";
import { ApiError } from "../../api/client";
import { formatDate } from "../../utils/formatters";

export function TeamDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
  const { notifySuccess, notifyError } = useToast();
  const { confirm, dialog } = useConfirmDialog();
  const canManage = isAdmin(user?.role);

  const { data: team, isLoading, error, reload } = useAsync(() => teamsApi.findById(id!), [id]);
  const { data: candidates } = useAsync(
    () => (canManage ? competitorsApi.findAll({ status: "ACTIVE", size: 200 }) : Promise.resolve(null)),
    [canManage],
  );

  const [selectedCompetitor, setSelectedCompetitor] = useState("");
  const [isAdding, setAdding] = useState(false);

  const handleAddMember = async () => {
    if (!selectedCompetitor) return;
    setAdding(true);
    try {
      await teamsApi.addMember(id!, selectedCompetitor);
      notifySuccess("Miembro agregado al equipo.");
      setSelectedCompetitor("");
      reload();
    } catch (err) {
      notifyError(err instanceof ApiError ? err.message : "No se pudo agregar el miembro.");
    } finally {
      setAdding(false);
    }
  };

  const handleRemoveMember = (competitorId: string, nickname: string) => {
    confirm({
      title: "Quitar miembro",
      description: `¿Quitar a ${nickname} de este equipo?`,
      confirmLabel: "Quitar",
      onConfirm: async () => {
        await teamsApi.removeMember(id!, competitorId);
        notifySuccess("Miembro removido del equipo.");
        reload();
      },
    });
  };

  const handleDeleteTeam = () => {
    confirm({
      title: "Eliminar equipo",
      description: "Si el equipo tiene historial oficial, no podrá eliminarse. ¿Deseas continuar?",
      confirmLabel: "Sí, eliminar",
      onConfirm: async () => {
        try {
          await teamsApi.remove(id!);
          notifySuccess("Equipo eliminado.");
          navigate("/teams");
        } catch (err) {
          notifyError(err instanceof ApiError ? err.message : "No se pudo eliminar el equipo.");
          throw err;
        }
      },
    });
  };

  if (isLoading) {
    return (
      <div className="page">
        <Skeleton height={32} width={220} />
        <Skeleton height={260} />
      </div>
    );
  }

  if (error || !team) {
    return (
      <div className="page">
        <ErrorState title="No se encontró el equipo" description={error ?? undefined} />
      </div>
    );
  }

  const memberIds = new Set(team.members.map((m) => m.id));
  const availableCandidates = (candidates?.content ?? []).filter((c) => !memberIds.has(c.id));

  return (
    <div className="page">
      <Link to="/teams" className="row text-muted" style={{ textDecoration: "none", width: "fit-content" }}>
        <ArrowLeft size={16} /> Volver a equipos
      </Link>

      <div className="page-header">
        <div>
          <span className="eyebrow">Equipo</span>
          <h1>{team.name}</h1>
          {team.coachName && <span className="text-muted">Entrenador: {team.coachName}</span>}
        </div>
        {canManage && (
          <div className="row">
            <Link to={`/teams/${team.id}/edit`} className="btn btn--ghost">
              <Pencil size={16} />
              Editar
            </Link>
            <button type="button" className="btn btn--danger" onClick={handleDeleteTeam}>
              <Trash2 size={16} />
              Eliminar
            </button>
          </div>
        )}
      </div>

      <div className="grid" style={{ gridTemplateColumns: "2fr 1fr" }}>
        <div className="card stack">
          <div className="row--between">
            <h3>Miembros ({team.members.length})</h3>
            <StatusBadge status={team.status} />
          </div>

          {team.members.length === 0 ? (
            <EmptyState title="Este equipo todavía no tiene miembros" description="Un equipo necesita al menos un competidor para poder inscribirse en carreras." />
          ) : (
            <div className="table-wrap">
              <table className="table">
                <thead>
                  <tr>
                    <th>Apodo</th>
                    <th>Nombre</th>
                    {canManage && <th />}
                  </tr>
                </thead>
                <tbody>
                  {team.members.map((member) => (
                    <tr key={member.id}>
                      <td>
                        <Link to={`/competitors/${member.id}`} style={{ color: "var(--color-accent)", textDecoration: "none" }}>
                          {member.nickname}
                        </Link>
                      </td>
                      <td>{member.name}</td>
                      {canManage && (
                        <td>
                          <button
                            type="button"
                            className="btn btn--ghost btn--sm"
                            onClick={() => handleRemoveMember(member.id, member.nickname)}
                          >
                            <UserMinus size={14} />
                            Quitar
                          </button>
                        </td>
                      )}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          {canManage && (
            <div className="row" style={{ borderTop: "1px solid var(--color-border)", paddingTop: "var(--space-4)" }}>
              <select value={selectedCompetitor} onChange={(e) => setSelectedCompetitor(e.target.value)} style={{ flex: 1 }}>
                <option value="">Selecciona un competidor activo…</option>
                {availableCandidates.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.nickname} — {c.name}
                  </option>
                ))}
              </select>
              <button type="button" className="btn btn--primary" disabled={!selectedCompetitor || isAdding} onClick={handleAddMember}>
                <UserPlus size={16} />
                Agregar
              </button>
            </div>
          )}
        </div>

        <div className="card stack">
          <h3>Detalles</h3>
          <p className="text-muted">{team.description || "Sin descripción."}</p>
          <div>
            <div className="text-muted" style={{ fontSize: "0.8rem" }}>Creado</div>
            <strong>{formatDate(team.createdAt)}</strong>
          </div>
          <div className="row--between">
            <div>
              <div className="text-muted" style={{ fontSize: "0.8rem" }}>Victorias</div>
              <strong>{team.wins}</strong>
            </div>
            <div>
              <div className="text-muted" style={{ fontSize: "0.8rem" }}>Derrotas</div>
              <strong>{team.losses}</strong>
            </div>
          </div>
        </div>
      </div>
      {dialog}
    </div>
  );
}
