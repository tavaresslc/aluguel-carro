import { Landmark } from "lucide-react";
import { useState } from "react";
import { Navigate, useParams } from "react-router";
import { CreditoFormModal } from "../components/contrato/CreditoFormModal";
import { CreditoResumo } from "../components/contrato/CreditoResumo";
import { StatusBadge } from "../components/ui/Badge";
import { Button, LinkButton } from "../components/ui/Button";
import { Card, InfoGrid } from "../components/ui/Card";
import { DataState } from "../components/ui/DataState";
import { PageHeader } from "../components/ui/PageHeader";
import { useAuth } from "../hooks/useAuth";
import { useFetch } from "../hooks/useFetch";
import { contratoService } from "../services/contratos";
import { formatCurrency, formatDate, vehicleName } from "../utils/format";

export default function ContratoDetalhe() {
  const id = Number(useParams().id);
  if (!Number.isInteger(id) || id <= 0)
    return <Navigate to="/contratos" replace />;
  return <ContratoView id={id} />;
}

function ContratoView({ id }: { id: number }) {
  const { isBanco } = useAuth();
  const [credito, setCredito] = useState(false);
  const contrato = useFetch(() => contratoService.buscar(id), id);

  return (
    <DataState query={contrato}>
      {(c) => (
        <>
          <PageHeader
            title={
              <>
                Contrato #{c.id} <StatusBadge status={c.status} />
              </>
            }
            subtitle={`Origem: pedido #${c.pedidoId}`}
            actions={
              <>
                <LinkButton to={`/pedidos/${c.pedidoId}`} variant="secondary">
                  Ver pedido
                </LinkButton>
                {isBanco && c.status === "EXECUTADO" && !c.credito && (
                  <Button onClick={() => setCredito(true)}>
                    <Landmark className="size-4" aria-hidden />
                    Conceder crédito
                  </Button>
                )}
              </>
            }
          />
          <div className="space-y-6">
            <Card title="Dados do contrato">
              <InfoGrid
                items={[
                  {
                    label: "Cliente",
                    value: `${c.cliente.nome} (${c.cliente.email})`,
                  },
                  {
                    label: "Veículo",
                    value: `${vehicleName(c.automovel)} - ${c.automovel.ano} - ${c.automovel.placa}`,
                  },
                  {
                    label: "Período",
                    value: `${formatDate(c.dataInicio)} a ${formatDate(c.dataFim)}`,
                  },
                  {
                    label: "Valor",
                    value: <strong>{formatCurrency(c.valor)}</strong>,
                  },
                ]}
              />
            </Card>
            <Card title="Crédito">
              {c.credito ? (
                <CreditoResumo credito={c.credito} />
              ) : (
                <p className="text-sm text-slate-500">
                  Nenhum crédito concedido para este contrato.
                </p>
              )}
            </Card>
          </div>
          {credito && (
            <CreditoFormModal
              contratoId={c.id}
              onClose={() => setCredito(false)}
              onSuccess={contrato.reload}
            />
          )}
        </>
      )}
    </DataState>
  );
}
