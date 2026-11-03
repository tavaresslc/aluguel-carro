import type {
  Avaliacao,
  AvaliacaoDados,
  Contrato,
  ContratoDados,
  Pedido,
  PedidoDados,
  StatusPedido,
} from "../types/api";
import { http } from "./http";

export const pedidoService = {
  criar: (dados: PedidoDados) => http.post<Pedido>("/pedidos", dados),
  listar: (status?: StatusPedido) => http.get<Pedido[]>("/pedidos", { status }),
  buscar: (id: number) => http.get<Pedido>(`/pedidos/${id}`),
  atualizar: (id: number, dados: PedidoDados) => http.put<Pedido>(`/pedidos/${id}`, dados),
  cancelar: (id: number) => http.patch<Pedido>(`/pedidos/${id}/cancelar`),
  avaliar: (id: number, dados: AvaliacaoDados) => http.post<Avaliacao>(`/pedidos/${id}/avaliacoes`, dados),
  listarAvaliacoes: (id: number) => http.get<Avaliacao[]>(`/pedidos/${id}/avaliacoes`),
  executarContrato: (id: number, dados: ContratoDados) => http.post<Contrato>(`/pedidos/${id}/contrato`, dados),
};
