import { api } from "./client";
import type { RegistrationRequest, RegistrationResponse } from "./types";

export const registrationsApi = {
  findByRace: (raceId: string) => api.get<RegistrationResponse[]>(`/races/${raceId}/registrations`),
  register: (raceId: string, payload: RegistrationRequest) =>
    api.post<RegistrationResponse>(`/races/${raceId}/registrations`, payload),
  approve: (id: string) => api.patch<RegistrationResponse>(`/registrations/${id}/approve`),
  reject: (id: string, reason: string) =>
    api.patch<RegistrationResponse>(`/registrations/${id}/reject`, { reason }),
  cancel: (id: string) => api.delete<void>(`/registrations/${id}`),
};
