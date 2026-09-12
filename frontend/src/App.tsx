import { Navigate, Route, Routes } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import { ToastProvider } from "./context/ToastContext";
import { ProtectedRoute } from "./components/ProtectedRoute";
import { Layout } from "./components/Layout";

import { Login } from "./pages/Login";
import { Dashboard } from "./pages/Dashboard";
import { Profile } from "./pages/Profile";
import { Forbidden } from "./pages/Forbidden";
import { NotFound } from "./pages/NotFound";

import { CompetitorList } from "./pages/competitors/CompetitorList";
import { CompetitorDetail } from "./pages/competitors/CompetitorDetail";
import { CompetitorForm } from "./pages/competitors/CompetitorForm";

import { TeamList } from "./pages/teams/TeamList";
import { TeamDetail } from "./pages/teams/TeamDetail";
import { TeamForm } from "./pages/teams/TeamForm";

import { RaceList } from "./pages/races/RaceList";
import { RaceDetail } from "./pages/races/RaceDetail";
import { RaceForm } from "./pages/races/RaceForm";

import { RegistrationsHub } from "./pages/RegistrationsHub";
import { StandingsPage } from "./pages/standings/StandingsPage";
import { AuditPage } from "./pages/audit/AuditPage";

export default function App() {
  return (
    <AuthProvider>
      <ToastProvider>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/403" element={<Forbidden />} />

          <Route
            element={
              <ProtectedRoute>
                <Layout />
              </ProtectedRoute>
            }
          >
            <Route index element={<Dashboard />} />
            <Route path="profile" element={<Profile />} />

            <Route path="competitors" element={<CompetitorList />} />
            <Route
              path="competitors/new"
              element={
                <ProtectedRoute roles={["ADMIN"]}>
                  <CompetitorForm />
                </ProtectedRoute>
              }
            />
            <Route
              path="competitors/:id/edit"
              element={
                <ProtectedRoute roles={["ADMIN"]}>
                  <CompetitorForm />
                </ProtectedRoute>
              }
            />
            <Route path="competitors/:id" element={<CompetitorDetail />} />

            <Route path="teams" element={<TeamList />} />
            <Route
              path="teams/new"
              element={
                <ProtectedRoute roles={["ADMIN"]}>
                  <TeamForm />
                </ProtectedRoute>
              }
            />
            <Route
              path="teams/:id/edit"
              element={
                <ProtectedRoute roles={["ADMIN"]}>
                  <TeamForm />
                </ProtectedRoute>
              }
            />
            <Route path="teams/:id" element={<TeamDetail />} />

            <Route path="races" element={<RaceList />} />
            <Route
              path="races/new"
              element={
                <ProtectedRoute roles={["ADMIN", "ORGANIZER"]}>
                  <RaceForm />
                </ProtectedRoute>
              }
            />
            <Route
              path="races/:id/edit"
              element={
                <ProtectedRoute roles={["ADMIN", "ORGANIZER"]}>
                  <RaceForm />
                </ProtectedRoute>
              }
            />
            <Route path="races/:id" element={<RaceDetail />} />

            <Route path="registrations" element={<RegistrationsHub />} />
            <Route path="standings" element={<StandingsPage />} />

            <Route
              path="audit"
              element={
                <ProtectedRoute roles={["ADMIN"]}>
                  <AuditPage />
                </ProtectedRoute>
              }
            />

            <Route path="404" element={<NotFound />} />
            <Route path="*" element={<Navigate to="/404" replace />} />
          </Route>
        </Routes>
      </ToastProvider>
    </AuthProvider>
  );
}
