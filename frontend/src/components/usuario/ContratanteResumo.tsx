import type { Contratante } from "../../types/api";
import { formatCurrency } from "../../utils/format";
import { InfoGrid } from "../ui/Card";

export function ContratanteResumo({
  contratante,
}: {
  contratante: Contratante;
}) {
  return (
    <div className="space-y-5">
      <InfoGrid
        items={[
          { label: "Nome", value: contratante.nome },
          { label: "Profissão", value: contratante.profissao },
          { label: "RG", value: contratante.rg },
          { label: "CPF", value: contratante.cpf },
          { label: "Endereço", value: contratante.endereco },
          {
            label: "Renda total",
            value: <strong>{formatCurrency(contratante.rendaTotal)}</strong>,
          },
        ]}
      />
      <div>
        <h3 className="mb-2 text-sm font-semibold">Vínculos empregatícios</h3>
        {contratante.vinculos.length === 0 ? (
          <p className="text-sm text-slate-500">Nenhum vínculo informado.</p>
        ) : (
          <ul className="divide-y divide-slate-100 rounded-lg border border-slate-200 text-sm">
            {contratante.vinculos.map((vinculo) => (
              <li
                key={vinculo.entidadeEmpregadora}
                className="flex justify-between gap-3 px-3 py-2"
              >
                <span>{vinculo.entidadeEmpregadora}</span>
                <span className="font-medium">
                  {formatCurrency(vinculo.rendimento)}
                </span>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}
