import type { Perfil, Status } from "../types/api";

export type Tone = "amber" | "emerald" | "rose" | "slate" | "sky";

export const statusInfo: Record<Status, { label: string; tone: Tone }> = {
  PENDENTE: { label: "Pendente", tone: "amber" },
  APROVADO: { label: "Aprovado", tone: "emerald" },
  REPROVADO: { label: "Reprovado", tone: "rose" },
  CANCELADO: { label: "Cancelado", tone: "slate" },
  CONTRATADO: { label: "Contratado", tone: "sky" },
  EXECUTADO: { label: "Executado", tone: "sky" },
};

export const perfilLabel: Record<Perfil, string> = {
  CLIENTE: "Cliente",
  EMPRESA: "Empresa",
  BANCO: "Banco",
};

export const pedidoStatusOptions = (["PENDENTE", "APROVADO", "REPROVADO", "CANCELADO", "CONTRATADO"] as const).map(
  (value) => ({ value, label: statusInfo[value].label }),
);
