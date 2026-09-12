import { api } from "./client";
import type { ResultRequest, ResultResponse } from "./types";

export const resultsApi = {
  findByRace: (raceId: string) => api.get<ResultResponse[]>(`/races/${raceId}/results`),
  record: (raceId: string, payload: ResultRequest) =>
    api.post<ResultResponse>(`/races/${raceId}/results`, payload),
  update: (id: string, payload: ResultRequest) => api.put<ResultResponse>(`/results/${id}`, payload),
};
