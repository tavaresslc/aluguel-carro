import { useEffect, useState, type ReactNode } from "react";
import { authService } from "../services/auth";
import { setUnauthorizedHandler } from "../services/http";
import { clearSession, readSession, saveSession } from "../services/session";
import { usuarioService } from "../services/usuarios";
import type { Usuario } from "../types/api";
import { Spinner } from "../components/ui/Spinner";
import { AuthContext } from "./auth-context";

const MAX_TIMEOUT = 2 ** 31 - 1;

export function AuthProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<Usuario | null>(null);
  const [pronto, setPronto] = useState(() => !readSession());

  useEffect(() => {
    setUnauthorizedHandler(() => {
      clearSession();
      setUsuario(null);
    });
    if (!readSession()) return;
    usuarioService
      .me()
      .then(setUsuario)
      .catch(clearSession)
      .finally(() => setPronto(true));
  }, []);

  useEffect(() => {
    const session = readSession();
    if (!usuario || !session) return;
    const timer = setTimeout(
      () => {
        clearSession();
        setUsuario(null);
      },
      Math.min(session.expiraEm - Date.now(), MAX_TIMEOUT),
    );
    return () => clearTimeout(timer);
  }, [usuario]);

  async function login(email: string, senha: string) {
    const resposta = await authService.login(email, senha);
    saveSession({
      token: resposta.token,
      expiraEm: Date.now() + resposta.expiraEmSegundos * 1000,
    });
    setUsuario(resposta.usuario);
  }

  async function logout() {
    await authService.logout().catch(() => undefined);
    clearSession();
    setUsuario(null);
  }

  if (!pronto) return <Spinner fullScreen />;

  return (
    <AuthContext
      value={{ usuario, login, logout, atualizarUsuario: setUsuario }}
    >
      {children}
    </AuthContext>
  );
}
