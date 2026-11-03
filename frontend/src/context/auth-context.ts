import { createContext } from "react";
import type { Usuario } from "../types/api";

export interface AuthValue {
  usuario: Usuario | null;
  login: (email: string, senha: string) => Promise<void>;
  logout: () => Promise<void>;
  atualizarUsuario: (usuario: Usuario) => void;
}

export const AuthContext = createContext<AuthValue | null>(null);
