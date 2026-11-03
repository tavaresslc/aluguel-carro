import type { ContratanteDados, Usuario } from "../types/api";
import { http } from "./http";

export const usuarioService = {
  me: () => http.get<Usuario>("/usuarios/me"),
  atualizarContratante: (dados: ContratanteDados) => http.put<Usuario>("/usuarios/me/contratante", dados),
};
