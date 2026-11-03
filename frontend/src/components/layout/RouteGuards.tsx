import { Navigate, Outlet } from "react-router";
import { useAuth } from "../../hooks/useAuth";

export function RequireAuth() {
  const { usuario } = useAuth();
  return usuario ? <Outlet /> : <Navigate to="/login" replace />;
}

export function GuestOnly() {
  const { usuario } = useAuth();
  return usuario ? <Navigate to="/" replace /> : <Outlet />;
}
