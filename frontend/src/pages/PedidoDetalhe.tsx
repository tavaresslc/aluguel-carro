import { CheckCircle2, FilePlus2, Pencil, XCircle } from "lucide-react";
import { useState } from "react";
import { Navigate, useParams } from "react-router";
import { AutomovelCard } from "../components/automovel/AutomovelCard";
import { ContratoFormModal } from "../components/contrato/ContratoFormModal";
import { AvaliacaoHistorico } from "../components/pedido/AvaliacaoHistorico";
import { AvaliacaoModal } from "../components/pedido/AvaliacaoModal";
import { PedidoFormModal } from "../components/pedido/PedidoFormModal";
import { ContratanteResumo } from "../components/usuario/ContratanteResumo";
import { StatusBadge } from "../components/ui/Badge";
import { Button, LinkButton } from "../components/ui/Button";
import { Card, InfoGrid } from "../components/ui/Card";
import { ConfirmModal } from "../components/ui/ConfirmModal";
import { DataState } from "../components/ui/DataState";
import { PageHeader } from "../components/ui/PageHeader";
import { useAuth } from "../hooks/useAuth";
import { useFetch } from "../hooks/useFetch";
import { automovelService } from "../services/automoveis";
import { pedidoService } from "../services/pedidos";
import type { Pedido } from "../types/api";
import { formatDate, vehicleName } from "../utils/format";

type Dialog = "editar" | "cancelar" | "avaliar" | "contrato" | null;

export default function PedidoDetalhe() {
  const id = Number(useParams().id);
  if (!Number.isInteger(id) || id <= 0)
    return <Navigate to="/pedidos" replace />;
  return <PedidoView id={id} />;
}

function PedidoView({ id }: { id: number }) {
  const { isCliente, isAgente } = useAuth();
  const [dialog, setDialog] = useState<Dialog>(null);
  const pedido = useFetch(() => pedidoService.buscar(id), id);
  const avaliacoes = useFetch(() => pedidoService.listarAvaliacoes(id), id);
  const reload = () => {
    pedido.reload();
    avaliacoes.reload();
  };
  const close = () => setDialog(null);

  return (
    <DataState query={pedido}>
      {(p) => {
        const modificavel =
          p.status !== "CANCELADO" && p.status !== "CONTRATADO";
        return (
          <>
            <PageHeader
              title={
                <>
                  Pedido #{p.id} <StatusBadge status={p.status} />
                </>
              }
              subtitle={`Solicitado em ${formatDate(p.dataSolicitacao)}`}
              actions={
                <>
                  {modificavel && (
                    <Button
                      variant="secondary"
                      onClick={() => setDialog("editar")}
                    >
                      <Pencil className="size-4" aria-hidden />
                      Editar
                    </Button>
                  )}
                  {modificavel && isCliente && (
                    <Button
                      variant="secondary"
                      className="text-rose-700"
                      onClick={() => setDialog("cancelar")}
                    >
                      <XCircle className="size-4" aria-hidden />
                      Cancelar pedido
                    </Button>
                  )}
                  {modificavel && isAgente && (
                    <Button
                      variant="secondary"
                      onClick={() => setDialog("avaliar")}
                    >
                      <CheckCircle2 className="size-4" aria-hidden />
                      Avaliar
                    </Button>
                  )}
                  {p.status === "APROVADO" && isAgente && (
                    <Button onClick={() => setDialog("contrato")}>
                      <FilePlus2 className="size-4" aria-hidden />
                      Executar contrato
                    </Button>
                  )}
                  {p.status === "CONTRATADO" && (
                    <LinkButton to="/contratos" variant="secondary">
                      Ver contratos
                    </LinkButton>
                  )}
                </>
              }
            />
            <div className="grid gap-6 lg:grid-cols-3">
              <div className="space-y-6 lg:col-span-2">
                <Card title="Dados do pedido">
                  <InfoGrid
                    items={[
                      {
                        label: "Cliente",
                        value: `${p.cliente.nome} (${p.cliente.email})`,
                      },
                      {
                        label: "Veículo",
                        value: `${vehicleName(p.automovel)} - ${p.automovel.ano} - ${p.automovel.placa}`,
                      },
                      { label: "Observações", value: p.observacoes },
                    ]}
                  />
                </Card>
                {isAgente && p.contratante && (
                  <Card title="Contratante">
                    <ContratanteResumo contratante={p.contratante} />
                  </Card>
                )}
                <Card title="Avaliações financeiras">
                  <DataState query={avaliacoes}>
                    {(lista) => <AvaliacaoHistorico avaliacoes={lista} />}
                  </DataState>
                </Card>
              </div>
              {isAgente && <Proprietario automovelId={p.automovel.id} />}
            </div>
            {dialog === "editar" && (
              <PedidoFormModal pedido={p} onClose={close} onSuccess={reload} />
            )}
            {dialog === "cancelar" && (
              <CancelarModal pedido={p} onClose={close} onSuccess={reload} />
            )}
            {dialog === "avaliar" && (
              <AvaliacaoModal
                pedidoId={p.id}
                onClose={close}
                onSuccess={reload}
              />
            )}
            {dialog === "contrato" && (
              <ContratoFormModal
                pedidoId={p.id}
                onClose={close}
                onSuccess={reload}
              />
            )}
          </>
        );
      }}
    </DataState>
  );
}

function Proprietario({ automovelId }: { automovelId: number }) {
  const automovel = useFetch(
    () => automovelService.buscar(automovelId),
    automovelId,
  );
  return (
    <DataState query={automovel}>
      {(a) => <AutomovelCard automovel={a} />}
    </DataState>
  );
}

function CancelarModal({
  pedido,
  onClose,
  onSuccess,
}: {
  pedido: Pedido;
  onClose: () => void;
  onSuccess: () => void;
}) {
  return (
    <ConfirmModal
      title="Cancelar pedido"
      confirmLabel="Cancelar pedido"
      onConfirm={() => pedidoService.cancelar(pedido.id)}
      onClose={onClose}
      onSuccess={onSuccess}
    >
      Tem certeza que deseja cancelar o pedido #{pedido.id}? Esta ação não pode
      ser desfeita.
    </ConfirmModal>
  );
}
