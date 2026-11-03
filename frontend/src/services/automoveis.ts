import type { Automovel, AutomovelDados } from "../types/api";
import { http } from "./http";

export const automovelService = {
  listar: (disponivel?: boolean) => http.get<Automovel[]>("/automoveis", { disponivel }),
  buscar: (id: number) => http.get<Automovel>(`/automoveis/${id}`),
  cadastrar: (dados: AutomovelDados) => http.post<Automovel>("/automoveis", dados),
};
