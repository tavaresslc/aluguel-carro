export type Perfil = "CLIENTE" | "EMPRESA" | "BANCO";
export type StatusPedido = "PENDENTE" | "APROVADO" | "REPROVADO" | "CANCELADO" | "CONTRATADO";
export type StatusContrato = "EXECUTADO" | "CANCELADO";
export type StatusCredito = "APROVADO";
export type Parecer = "APROVADO" | "REPROVADO";
export type Status = StatusPedido | StatusContrato | StatusCredito | Parecer;

export interface Vinculo {
  entidadeEmpregadora: string;
  rendimento: number;
}

export interface Contratante {
  id: number;
  nome: string;
  rg: string | null;
  cpf: string | null;
  endereco: string | null;
  profissao: string | null;
  vinculos: Vinculo[];
  rendaTotal: number;
  dadosCompletos: boolean;
}

export interface Usuario {
  id: number;
  nome: string;
  email: string;
  perfil: Perfil;
  nomeInstituicao: string | null;
  contratante: Contratante | null;
}

export interface LoginResposta {
  token: string;
  tipoToken: string;
  expiraEmSegundos: number;
  usuario: Usuario;
}

export interface ClienteResumo {
  id: number;
  nome: string;
  email: string;
}

export interface AutomovelResumo {
  id: number;
  marca: string;
  modelo: string;
  ano: number;
  placa: string;
}

export interface Automovel extends AutomovelResumo {
  matricula: string;
  disponivel: boolean;
  proprietario: { id: number; nome: string; perfil: Perfil } | null;
}

export interface Pedido {
  id: number;
  dataSolicitacao: string;
  status: StatusPedido;
  observacoes: string | null;
  cliente: ClienteResumo;
  automovel: AutomovelResumo;
  contratante: Contratante | null;
}

export interface Avaliacao {
  id: number;
  pedidoId: number;
  agenteId: number;
  agenteNome: string;
  instituicao: string | null;
  tipoAgente: Perfil;
  dataAvaliacao: string;
  parecer: Parecer;
  justificativa: string | null;
}

export interface Credito {
  id: number;
  contratoId: number;
  bancoId: number;
  bancoNome: string;
  valor: number;
  taxaJuros: number;
  quantidadeParcelas: number;
  valorParcela: number;
  status: StatusCredito;
}

export interface Contrato {
  id: number;
  pedidoId: number;
  cliente: ClienteResumo;
  automovel: AutomovelResumo;
  dataInicio: string;
  dataFim: string;
  valor: number;
  status: StatusContrato;
  credito: Credito | null;
}

export interface CadastroUsuario {
  nome: string;
  email: string;
  senha: string;
}

export interface ContratanteDados {
  nome: string;
  rg: string;
  cpf: string;
  endereco: string;
  profissao: string;
  vinculos: Vinculo[];
}

export interface AutomovelDados {
  matricula: string;
  ano: number;
  marca: string;
  modelo: string;
  placa: string;
  disponivel: boolean;
}

export interface PedidoDados {
  automovelId: number;
  observacoes: string;
}

export interface AvaliacaoDados {
  parecer: Parecer;
  justificativa: string;
}

export interface ContratoDados {
  dataInicio: string;
  dataFim: string;
  valor: number;
}

export interface CreditoDados {
  valor: number;
  taxaJuros: number;
  quantidadeParcelas: number;
}
