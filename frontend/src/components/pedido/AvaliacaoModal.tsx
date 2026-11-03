import { useState } from "react";
import { pedidoService } from "../../services/pedidos";
import type { Parecer } from "../../types/api";
import { Select, Textarea } from "../ui/Field";
import { FormModal } from "../ui/FormModal";

export function AvaliacaoModal({
  pedidoId,
  onClose,
  onSuccess,
}: {
  pedidoId: number;
  onClose: () => void;
  onSuccess: () => void;
}) {
  const [parecer, setParecer] = useState<Parecer>("APROVADO");

  const submit = (data: FormData) =>
    pedidoService.avaliar(pedidoId, {
      parecer,
      justificativa: String(data.get("justificativa")).trim(),
    });

  return (
    <FormModal
      title="Avaliação financeira"
      submitLabel="Registrar parecer"
      onSubmit={submit}
      onClose={onClose}
      onSuccess={onSuccess}
    >
      <Select
        label="Parecer"
        name="parecer"
        value={parecer}
        onChange={(event) => setParecer(event.target.value as Parecer)}
        options={[
          { value: "APROVADO", label: "Aprovado" },
          { value: "REPROVADO", label: "Reprovado" },
        ]}
      />
      <Textarea
        label="Justificativa"
        name="justificativa"
        maxLength={1000}
        required={parecer === "REPROVADO"}
        hint={
          parecer === "REPROVADO"
            ? "Obrigatória em caso de reprovação."
            : "Opcional."
        }
      />
    </FormModal>
  );
}
