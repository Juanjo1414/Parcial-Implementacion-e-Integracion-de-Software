import { api } from "./client";
import type { AuditLogResponse, PageResponse } from "./types";

export const auditApi = {
  findAll: (page = 0, size = 20) =>
    api.get<PageResponse<AuditLogResponse>>("/audit", { page, size, sort: "timestamp,desc" }),
};
