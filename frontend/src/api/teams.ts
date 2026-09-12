import { api } from "./client";
import type { TeamRequest, TeamResponse } from "./types";

export const teamsApi = {
  findAll: () => api.get<TeamResponse[]>("/teams"),
  findById: (id: string) => api.get<TeamResponse>(`/teams/${id}`),
  create: (payload: TeamRequest) => api.post<TeamResponse>("/teams", payload),
  update: (id: string, payload: TeamRequest) => api.put<TeamResponse>(`/teams/${id}`, payload),
  remove: (id: string) => api.delete<void>(`/teams/${id}`),
  addMember: (teamId: string, competitorId: string) =>
    api.post<TeamResponse>(`/teams/${teamId}/members/${competitorId}`),
  removeMember: (teamId: string, competitorId: string) =>
    api.delete<TeamResponse>(`/teams/${teamId}/members/${competitorId}`),
};
