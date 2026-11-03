import { ClipboardList, Plus } from "lucide-react";
import { useState } from "react";
import { Link, useNavigate } from "react-router";
import { PedidoFormModal } from "../components/pedido/PedidoFormModal";
import { StatusBadge } from "../components/ui/Badge";
import { Button } from "../components/ui/Button";
import { DataState } from "../components/ui/DataState";
import { DataTable, type Column } from "../components/ui/DataTable";
import { EmptyState } from "../components/ui/EmptyState";
import { Select } from "../components/ui/Field";
import { PageHeader } from "../components/ui/PageHeader";
import { useAuth } from "../hooks/useAuth";
import { useFetch } from "../hooks/useFetch";
import { pedidoService } from "../services/pedidos";
import type { Pedido, StatusPedido } from "../types/api";
import { formatDate, vehicleName } from "../utils/format";
import { pedidoStatusOptions } from "../utils/labels";

export default function Pedidos() {
  const { isCliente } = useAuth();
  const navigate = useNavigate();
  const [status, setStatus] = useState<StatusPedido | "">("");
  const [novo, setNovo] = useState(false);
  const pedidos = useFetch(
    () => pedidoService.listar(status || undefined),
    status,
  );

  const columns: Column<Pedido>[] = [
    {
      header: "Pedido",
      cell: (pedido) => (
        <Link
          to={`/pedidos/${pedido.id}`}
          className="font-medium text-brand-700 hover:underline"
        >
          #{pedido.id}
        </Link>
      ),
    },
    {
      header: "Veículo",
      cell: (pedido) =>
        `${vehicleName(pedido.automovel)} - ${pedido.automovel.placa}`,
    },
    ...(isCliente
      ? []
      : [{ header: "Cliente", cell: (pedido: Pedido) => pedido.cliente.nome }]),
    {
      header: "Solicitado em",
      cell: (pedido) => formatDate(pedido.dataSolicitacao),
    },
    {
      header: "Status",
      cell: (pedido) => <StatusBadge status={pedido.status} />,
    },
  ];

  return (
    <>
      <PageHeader
        title="Pedidos"
        subtitle={
          isCliente
            ? "Acompanhe suas solicitações de aluguel."
            : "Solicitações de aluguel para análise."
        }
        actions={
          isCliente && (
            <Button onClick={() => setNovo(true)}>
              <Plus className="size-4" aria-hidden />
              Novo pedido
            </Button>
          )
        }
      />
      <div className="mb-5 max-w-xs">
        <Select
          label="Status"
          name="status"
          value={status}
          onChange={(event) =>
            setStatus(event.target.value as StatusPedido | "")
          }
          options={[{ value: "", label: "Todos" }, ...pedidoStatusOptions]}
        />
      </div>
      <DataState query={pedidos}>
        {(lista) =>
          lista.length === 0 ? (
            <EmptyState
              icon={ClipboardList}
              title="Nenhum pedido encontrado"
              text={
                isCliente
                  ? "Solicite o aluguel de um automóvel para começar."
                  : "Ainda não há pedidos para este filtro."
              }
            />
          ) : (
            <DataTable
              columns={columns}
              rows={lista}
              onRowClick={(pedido) => navigate(`/pedidos/${pedido.id}`)}
            />
          )
        }
      </DataState>
      {novo && (
        <PedidoFormModal
          onClose={() => setNovo(false)}
          onSuccess={(pedido) => navigate(`/pedidos/${pedido.id}`)}
        />
      )}
    </>
  );
}
