import { automovelService } from "../../services/automoveis";
import { FormModal } from "../ui/FormModal";
import { Input, Select } from "../ui/Field";

export function AutomovelFormModal({
  onClose,
  onSuccess,
}: {
  onClose: () => void;
  onSuccess: () => void;
}) {
  const submit = (data: FormData) =>
    automovelService.cadastrar({
      marca: String(data.get("marca")).trim(),
      modelo: String(data.get("modelo")).trim(),
      ano: Number(data.get("ano")),
      placa: String(data.get("placa")).trim().toUpperCase(),
      matricula: String(data.get("matricula")).trim(),
      disponivel: data.get("disponivel") === "true",
    });

  return (
    <FormModal
      title="Novo automóvel"
      submitLabel="Cadastrar"
      onSubmit={submit}
      onClose={onClose}
      onSuccess={onSuccess}
    >
      <div className="grid gap-4 sm:grid-cols-2">
        <Input label="Marca" name="marca" required maxLength={60} />
        <Input label="Modelo" name="modelo" required maxLength={80} />
        <Input
          label="Ano"
          name="ano"
          type="number"
          required
          min={1900}
          max={2100}
        />
        <Input
          label="Placa"
          name="placa"
          required
          pattern="[A-Za-z]{3}-?[0-9][A-Za-z0-9][0-9]{2}"
          title="Ex.: ABC1D23 ou ABC-1234"
          placeholder="ABC1D23"
          maxLength={8}
        />
      </div>
      <Input
        label="Matrícula (RENAVAM)"
        name="matricula"
        required
        maxLength={30}
      />
      <Select
        label="Situação"
        name="disponivel"
        defaultValue="true"
        options={[
          { value: "true", label: "Disponível para aluguel" },
          { value: "false", label: "Indisponível" },
        ]}
      />
    </FormModal>
  );
}
