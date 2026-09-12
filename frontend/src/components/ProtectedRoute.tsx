import type { ReactNode } from "react";
import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import type { Role } from "../api/types";
import { Skeleton } from "./StatePanel";

interface ProtectedRouteProps {
  children: ReactNode;
  roles?: Role[];
}

// Envuelve una ruta y decide, en este orden: (1) si todavía se está
// resolviendo la sesión, (2) si no hay usuario autenticado -> a /login,
// (3) si el rol no alcanza -> a /403, (4) si todo está bien, renderiza.
export function ProtectedRoute({ children, roles }: ProtectedRouteProps) {
  const { user, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="page">
        <Skeleton height={32} width={240} />
        <Skeleton height={200} />
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (roles && !roles.includes(user.role)) {
    return <Navigate to="/403" replace />;
  }

  return <>{children}</>;
}
