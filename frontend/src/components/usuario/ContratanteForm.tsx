import { Plus, Trash2 } from "lucide-react";
import { useState } from "react";
import { usuarioService } from "../../services/usuarios";
import type { Contratante, Usuario } from "../../types/api";
import { maskCpf } from "../../utils/format";
import { Button } from "../ui/Button";
import { Input } from "../ui/Field";
import { Form } from "../ui/Form";

const MAX_VINCULOS = 3;

interface Linha {
  entidadeEmpregadora: string;
  rendimento: string;
}

export function ContratanteForm({
  contratante,
  onSuccess,
}: {
  contratante: Contratante | null;
  onSuccess: (usuario: Usuario) => void;
}) {
  const [vinculos, setVinculos] = useState<Linha[]>(
    contratante?.vinculos.map((v) => ({
      ...v,
      rendimento: String(v.rendimento),
    })) ?? [],
  );

  const alterar = (indice: number, campo: keyof Linha, valor: string) =>
    setVinculos((atual) =>
      atual.map((linha, i) =>
        i === indice ? { ...linha, [campo]: valor } : linha,
      ),
    );

  async function submit(data: FormData) {
    const texto = (campo: string) => String(data.get(campo)).trim();
    const usuario = await usuarioService.atualizarContratante({
      nome: texto("nome"),
      rg: texto("rg"),
      cpf: texto("cpf"),
      endereco: texto("endereco"),
      profissao: texto("profissao"),
      vinculos: vinculos.map((v) => ({
        entidadeEmpregadora: v.entidadeEmpregadora.trim(),
        rendimento: Number(v.rendimento),
      })),
    });
    onSuccess(usuario);
  }

  return (
    <Form
      onSubmit={submit}
      submitLabel="Salvar dados"
      successMessage="Dados atualizados com sucesso."
    >
      <div className="grid gap-4 sm:grid-cols-2">
        <Input
          label="Nome completo"
          name="nome"
          required
          maxLength={120}
          defaultValue={contratante?.nome ?? ""}
        />
        <Input
          label="Profissão"
          name="profissao"
          required
          maxLength={100}
          defaultValue={contratante?.profissao ?? ""}
        />
        <Input
          label="RG"
          name="rg"
          required
          maxLength={20}
          defaultValue={contratante?.rg ?? ""}
        />
        <Input
          label="CPF"
          name="cpf"
          required
          inputMode="numeric"
          maxLength={14}
          pattern="[0-9.\-]{11,14}"
          title="Informe um CPF válido"
          placeholder="000.000.000-00"
          defaultValue={contratante?.cpf ?? ""}
          onInput={(event) =>
            (event.currentTarget.value = maskCpf(event.currentTarget.value))
          }
        />
      </div>
      <Input
        label="Endereço"
        name="endereco"
        required
        maxLength={255}
        defaultValue={contratante?.endereco ?? ""}
      />

      <fieldset className="space-y-3">
        <legend className="mb-1 flex w-full items-center justify-between text-sm font-medium text-slate-700">
          Vínculos empregatícios ({vinculos.length}/{MAX_VINCULOS})
        </legend>
        {vinculos.map((vinculo, i) => (
          <div
            key={i}
            className="grid items-start gap-3 rounded-lg bg-slate-50 p-3 sm:grid-cols-[1fr_10rem_auto]"
          >
            <Input
              label="Entidade empregadora"
              name={`vinculos[${i}].entidadeEmpregadora`}
              required
              maxLength={150}
              value={vinculo.entidadeEmpregadora}
              onChange={(e) =>
                alterar(i, "entidadeEmpregadora", e.target.value)
              }
            />
            <Input
              label="Rendimento (R$)"
              name={`vinculos[${i}].rendimento`}
              type="number"
              required
              min={0}
              step="0.01"
              value={vinculo.rendimento}
              onChange={(e) => alterar(i, "rendimento", e.target.value)}
            />
            <Button
              variant="ghost"
              className="justify-self-end text-rose-600 sm:mt-7"
              aria-label={`Remover vínculo ${i + 1}`}
              onClick={() =>
                setVinculos((atual) => atual.filter((_, j) => j !== i))
              }
            >
              <Trash2 className="size-4" aria-hidden />
            </Button>
          </div>
        ))}
        {vinculos.length < MAX_VINCULOS && (
          <Button
            variant="secondary"
            size="sm"
            onClick={() =>
              setVinculos((atual) => [
                ...atual,
                { entidadeEmpregadora: "", rendimento: "" },
              ])
            }
          >
            <Plus className="size-4" aria-hidden />
            Adicionar vínculo
          </Button>
        )}
      </fieldset>
    </Form>
  );
}
