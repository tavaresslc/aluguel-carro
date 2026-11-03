import { pedidoService } from "../../services/pedidos";
import { today } from "../../utils/format";
import { Input } from "../ui/Field";
import { FormModal } from "../ui/FormModal";

export function ContratoFormModal({
  pedidoId,
  onClose,
  onSuccess,
}: {
  pedidoId: number;
  onClose: () => void;
  onSuccess: () => void;
}) {
  const submit = (data: FormData) => {
    const dataInicio = String(data.get("dataInicio"));
    const dataFim = String(data.get("dataFim"));
    if (dataFim < dataInicio)
      throw new Error(
        "A data de término deve ser igual ou posterior à data de início.",
      );
    return pedidoService.executarContrato(pedidoId, {
      dataInicio,
      dataFim,
      valor: Number(data.get("valor")),
    });
  };

  return (
    <FormModal
      title="Executar contrato"
      submitLabel="Executar contrato"
      onSubmit={submit}
      onClose={onClose}
      onSuccess={onSuccess}
    >
      <div className="grid gap-4 sm:grid-cols-2">
        <Input
          label="Início"
          name="dataInicio"
          type="date"
          required
          min={today()}
        />
        <Input
          label="Término"
          name="dataFim"
          type="date"
          required
          min={today()}
        />
      </div>
      <Input
        label="Valor do contrato (R$)"
        name="valor"
        type="number"
        required
        min={0.01}
        step="0.01"
      />
    </FormModal>
  );
}
