import { api } from "./client";
import type { CompetitorStandingResponse, TeamStandingResponse } from "./types";

export const standingsApi = {
  competitors: () => api.get<CompetitorStandingResponse[]>("/standings/competitors"),
  teams: () => api.get<TeamStandingResponse[]>("/standings/teams"),
};
