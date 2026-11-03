import type { CadastroUsuario, LoginResposta, Usuario } from "../types/api";
import { http } from "./http";

export const authService = {
  cadastrar: (dados: CadastroUsuario) => http.post<Usuario>("/auth/cadastro", dados),
  login: (email: string, senha: string) => http.post<LoginResposta>("/auth/login", { email, senha }),
  logout: () => http.post<void>("/auth/logout"),
};
