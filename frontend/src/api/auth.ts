import { api } from "./client";
import type { AuthResponse } from "./types";

export const authApi = {
  login: (username: string, password: string) =>
    api.post<AuthResponse>("/auth/login", { username, password }),
  register: (username: string, password: string) =>
    api.post<AuthResponse>("/auth/register", { username, password }),
  profile: () => api.get<AuthResponse>("/auth/profile"),
};
