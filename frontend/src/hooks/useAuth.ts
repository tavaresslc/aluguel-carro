import { use } from "react";
import { AuthContext } from "../context/auth-context";

export function useAuth() {
  const context = use(AuthContext);
  if (!context) throw new Error("useAuth deve ser usado dentro de AuthProvider");
  const perfil = context.usuario?.perfil;
  return {
    ...context,
    isCliente: perfil === "CLIENTE",
    isAgente: perfil === "EMPRESA" || perfil === "BANCO",
    isBanco: perfil === "BANCO",
  };
}
