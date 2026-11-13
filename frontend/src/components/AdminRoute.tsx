import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

/**
 * Samo UX: sakriva admin stranice običnim korisnicima.
 * Prava zaštita je na backend-u (/api/admin/** traži ROLE_ADMIN).
 */
export default function AdminRoute() {
  const { isAdmin } = useAuth();
  return isAdmin ? <Outlet /> : <Navigate to="/feed" replace />;
}
