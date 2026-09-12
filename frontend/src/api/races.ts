import { api } from "./client";
import type { RaceRequest, RaceResponse, RaceStatus } from "./types";

export const racesApi = {
  findAll: () => api.get<RaceResponse[]>("/races"),
  findById: (id: string) => api.get<RaceResponse>(`/races/${id}`),
  create: (payload: RaceRequest) => api.post<RaceResponse>("/races", payload),
  update: (id: string, payload: RaceRequest) => api.put<RaceResponse>(`/races/${id}`, payload),
  changeStatus: (id: string, status: RaceStatus) =>
    api.patch<RaceResponse>(`/races/${id}/status`, undefined, { status }),
  remove: (id: string) => api.delete<void>(`/races/${id}`),
};
