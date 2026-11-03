import type { Credito } from "../../types/api";
import { formatCurrency } from "../../utils/format";
import { StatusBadge } from "../ui/Badge";
import { InfoGrid } from "../ui/Card";

export function CreditoResumo({ credito }: { credito: Credito }) {
  return (
    <InfoGrid
      items={[
        { label: "Banco", value: credito.bancoNome },
        { label: "Situação", value: <StatusBadge status={credito.status} /> },
        { label: "Valor concedido", value: formatCurrency(credito.valor) },
        { label: "Taxa de juros", value: `${credito.taxaJuros}% ao mês` },
        {
          label: "Parcelas",
          value: `${credito.quantidadeParcelas}x de ${formatCurrency(credito.valorParcela)}`,
        },
      ]}
    />
  );
}
