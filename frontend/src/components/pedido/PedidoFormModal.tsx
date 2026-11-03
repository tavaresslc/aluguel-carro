import { useAuth } from "../../hooks/useAuth";
import { useFetch } from "../../hooks/useFetch";
import { automovelService } from "../../services/automoveis";
import { pedidoService } from "../../services/pedidos";
import type { AutomovelResumo, Pedido } from "../../types/api";
import { vehicleName } from "../../utils/format";
import { Alert } from "../ui/Alert";
import { DataState } from "../ui/DataState";
import { Select, Textarea } from "../ui/Field";
import { Form } from "../ui/Form";
import { LinkButton } from "../ui/Button";
import { Modal } from "../ui/Modal";

interface Props {
  pedido?: Pedido;
  automovelId?: number;
  onClose: () => void;
  onSuccess: (pedido: Pedido) => void;
}

export function PedidoFormModal({
  pedido,
  automovelId,
  onClose,
  onSuccess,
}: Props) {
  const { usuario, isCliente } = useAuth();
  const automoveis = useFetch(() => automovelService.listar(true));
  const editando = Boolean(pedido);
  const incompleto = isCliente && !usuario?.contratante?.dadosCompletos;

  if (incompleto) {
    return (
      <Modal title="Novo pedido" onClose={onClose}>
        <Alert
          tone="warning"
          action={
            <LinkButton to="/perfil" size="sm" variant="secondary">
              Completar
            </LinkButton>
          }
        >
          Complete seus dados de contratante (com ao menos um vínculo
          empregatício) antes de solicitar um aluguel.
        </Alert>
      </Modal>
    );
  }

  return (
    <Modal
      title={editando ? `Editar pedido #${pedido?.id}` : "Novo pedido"}
      onClose={onClose}
    >
      <DataState query={automoveis}>
        {(lista) => (
          <PedidoFields
            pedido={pedido}
            automovelId={automovelId}
            opcoes={withCurrent(lista, pedido?.automovel)}
            onClose={onClose}
            onSuccess={onSuccess}
          />
        )}
      </DataState>
    </Modal>
  );
}

const withCurrent = (lista: AutomovelResumo[], atual?: AutomovelResumo) =>
  atual && !lista.some((a) => a.id === atual.id) ? [atual, ...lista] : lista;

function PedidoFields({
  pedido,
  automovelId,
  opcoes,
  onClose,
  onSuccess,
}: Props & { opcoes: AutomovelResumo[] }) {
  async function submit(data: FormData) {
    const dados = {
      automovelId: Number(data.get("automovelId")),
      observacoes: String(data.get("observacoes")).trim(),
    };
    const resultado = pedido
      ? await pedidoService.atualizar(pedido.id, dados)
      : await pedidoService.criar(dados);
    onSuccess(resultado);
  }

  return (
    <Form
      onSubmit={submit}
      onCancel={onClose}
      onSuccess={onClose}
      submitLabel={pedido ? "Salvar alterações" : "Solicitar aluguel"}
    >
      <Select
        label="Automóvel"
        name="automovelId"
        required
        placeholder="Selecione um automóvel"
        defaultValue={pedido?.automovel.id ?? automovelId ?? ""}
        options={opcoes.map((a) => ({
          value: a.id,
          label: `${vehicleName(a)} - ${a.ano} - ${a.placa}`,
        }))}
      />
      <Textarea
        label="Observações"
        name="observacoes"
        maxLength={1000}
        defaultValue={pedido?.observacoes ?? ""}
        placeholder="Ex.: Preciso do carro para uma viagem."
      />
    </Form>
  );
}
