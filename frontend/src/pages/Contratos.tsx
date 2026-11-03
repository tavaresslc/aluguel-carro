import { FileText } from "lucide-react";
import { Link, useNavigate } from "react-router";
import { StatusBadge } from "../components/ui/Badge";
import { DataState } from "../components/ui/DataState";
import { DataTable, type Column } from "../components/ui/DataTable";
import { EmptyState } from "../components/ui/EmptyState";
import { PageHeader } from "../components/ui/PageHeader";
import { useAuth } from "../hooks/useAuth";
import { useFetch } from "../hooks/useFetch";
import { contratoService } from "../services/contratos";
import type { Contrato } from "../types/api";
import { formatCurrency, formatDate, vehicleName } from "../utils/format";

export default function Contratos() {
  const { isCliente } = useAuth();
  const navigate = useNavigate();
  const contratos = useFetch(contratoService.listar);

  const columns: Column<Contrato>[] = [
    {
      header: "Contrato",
      cell: (contrato) => (
        <Link
          to={`/contratos/${contrato.id}`}
          className="font-medium text-brand-700 hover:underline"
        >
          #{contrato.id}
        </Link>
      ),
    },
    {
      header: "Veículo",
      cell: (contrato) =>
        `${vehicleName(contrato.automovel)} - ${contrato.automovel.placa}`,
    },
    ...(isCliente
      ? []
      : [
          {
            header: "Cliente",
            cell: (contrato: Contrato) => contrato.cliente.nome,
          },
        ]),
    {
      header: "Período",
      cell: (contrato) =>
        `${formatDate(contrato.dataInicio)} a ${formatDate(contrato.dataFim)}`,
    },
    { header: "Valor", cell: (contrato) => formatCurrency(contrato.valor) },
    {
      header: "Crédito",
      cell: (contrato) =>
        contrato.credito ? formatCurrency(contrato.credito.valor) : "—",
    },
    {
      header: "Status",
      cell: (contrato) => <StatusBadge status={contrato.status} />,
    },
  ];

  return (
    <>
      <PageHeader
        title="Contratos"
        subtitle={
          isCliente
            ? "Aluguéis formalizados em seu nome."
            : "Contratos em execução e seus créditos."
        }
      />
      <DataState query={contratos}>
        {(lista) =>
          lista.length === 0 ? (
            <EmptyState
              icon={FileText}
              title="Nenhum contrato por aqui"
              text="Os contratos aparecem após a aprovação e execução de um pedido."
            />
          ) : (
            <DataTable
              columns={columns}
              rows={lista}
              onRowClick={(contrato) => navigate(`/contratos/${contrato.id}`)}
            />
          )
        }
      </DataState>
    </>
  );
}
