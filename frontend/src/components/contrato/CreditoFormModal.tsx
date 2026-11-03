import { contratoService } from "../../services/contratos";
import { Input } from "../ui/Field";
import { FormModal } from "../ui/FormModal";

export function CreditoFormModal({
  contratoId,
  onClose,
  onSuccess,
}: {
  contratoId: number;
  onClose: () => void;
  onSuccess: () => void;
}) {
  const submit = (data: FormData) =>
    contratoService.conceder(contratoId, {
      valor: Number(data.get("valor")),
      taxaJuros: Number(data.get("taxaJuros")),
      quantidadeParcelas: Number(data.get("quantidadeParcelas")),
    });

  return (
    <FormModal
      title="Conceder crédito"
      submitLabel="Conceder crédito"
      onSubmit={submit}
      onClose={onClose}
      onSuccess={onSuccess}
    >
      <Input
        label="Valor do crédito (R$)"
        name="valor"
        type="number"
        required
        min={0.01}
        step="0.01"
        hint="Não pode ultrapassar o valor do contrato."
      />
      <div className="grid gap-4 sm:grid-cols-2">
        <Input
          label="Taxa de juros (% ao mês)"
          name="taxaJuros"
          type="number"
          required
          min={0}
          max={100}
          step="0.0001"
        />
        <Input
          label="Parcelas"
          name="quantidadeParcelas"
          type="number"
          required
          min={1}
          max={120}
          step={1}
        />
      </div>
    </FormModal>
  );
}
