import { Car, ClipboardList, FileText, LogOut, UserRound } from "lucide-react";
import { NavLink, Outlet } from "react-router";
import { useAuth } from "../../hooks/useAuth";
import { cx } from "../../utils/cx";
import { perfilLabel } from "../../utils/labels";
import { Button } from "../ui/Button";
import { Brand } from "./Brand";

const links = [
  { to: "/automoveis", label: "Automóveis", icon: Car },
  { to: "/pedidos", label: "Pedidos", icon: ClipboardList },
  { to: "/contratos", label: "Contratos", icon: FileText },
  { to: "/perfil", label: "Perfil", icon: UserRound },
];

export function AppLayout() {
  const { usuario, logout } = useAuth();

  return (
    <div className="min-h-screen">
      <header className="sticky top-0 z-10 border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-6xl items-center justify-between gap-4 px-4 py-3">
          <Brand />
          <div className="flex items-center gap-3">
            <div className="hidden text-right leading-tight sm:block">
              <p className="text-sm font-medium text-slate-900">
                {usuario?.nome}
              </p>
              <p className="text-xs text-slate-500">
                {usuario && perfilLabel[usuario.perfil]}
              </p>
            </div>
            <Button
              variant="ghost"
              size="sm"
              onClick={logout}
              aria-label="Sair"
            >
              <LogOut className="size-4" aria-hidden />
              <span className="hidden sm:inline">Sair</span>
            </Button>
          </div>
        </div>
        <nav
          aria-label="Principal"
          className="mx-auto flex max-w-6xl overflow-x-auto px-2 sm:px-4"
        >
          {links.map(({ to, label, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              className={({ isActive }) =>
                cx(
                  "flex items-center gap-2 border-b-2 px-2.5 py-2.5 text-sm sm:px-3 font-medium whitespace-nowrap transition-colors",
                  isActive
                    ? "border-brand-600 text-brand-700"
                    : "border-transparent text-slate-600 hover:text-slate-900",
                )
              }
            >
              <Icon className="size-4" aria-hidden />
              {label}
            </NavLink>
          ))}
        </nav>
      </header>
      <main className="mx-auto max-w-6xl px-4 py-8">
        <Outlet />
      </main>
    </div>
  );
}
