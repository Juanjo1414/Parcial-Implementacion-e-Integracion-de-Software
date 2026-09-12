import { api } from "./client";
import type {
  CompetitorRequest,
  CompetitorResponse,
  CompetitorStatus,
  CompetitorType,
  PageResponse,
} from "./types";

export interface CompetitorFilters {
  name?: string;
  type?: CompetitorType | "";
  status?: CompetitorStatus | "";
  page?: number;
  size?: number;
}

export const competitorsApi = {
  findAll: (filters: CompetitorFilters = {}) =>
    api.get<PageResponse<CompetitorResponse>>("/competitors", {
      name: filters.name,
      type: filters.type || undefined,
      status: filters.status || undefined,
      page: filters.page ?? 0,
      size: filters.size ?? 12,
      sort: "name,asc",
    }),
  findById: (id: string) => api.get<CompetitorResponse>(`/competitors/${id}`),
  create: (payload: CompetitorRequest) => api.post<CompetitorResponse>("/competitors", payload),
  update: (id: string, payload: CompetitorRequest) =>
    api.put<CompetitorResponse>(`/competitors/${id}`, payload),
  changeStatus: (id: string, status: CompetitorStatus) =>
    api.patch<CompetitorResponse>(`/competitors/${id}/status`, undefined, { status }),
  remove: (id: string) => api.delete<void>(`/competitors/${id}`),
};
