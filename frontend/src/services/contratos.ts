import type { Contrato, Credito, CreditoDados } from "../types/api";
import { http } from "./http";

export const contratoService = {
  listar: () => http.get<Contrato[]>("/contratos"),
  buscar: (id: number) => http.get<Contrato>(`/contratos/${id}`),
  conceder: (id: number, dados: CreditoDados) => http.post<Credito>(`/contratos/${id}/credito`, dados),
};
