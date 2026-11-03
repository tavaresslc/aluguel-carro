import { Dot, History } from "lucide-react";
import type { Avaliacao } from "../../types/api";
import { formatDateTime } from "../../utils/format";
import { perfilLabel } from "../../utils/labels";
import { StatusBadge } from "../ui/Badge";

export function AvaliacaoHistorico({
  avaliacoes,
}: {
  avaliacoes: Avaliacao[];
}) {
  if (avaliacoes.length === 0) {
    return (
      <p className="flex items-center gap-2 text-sm text-slate-500">
        <History className="size-4" aria-hidden />
        Nenhuma avaliação registrada até o momento.
      </p>
    );
  }

  return (
    <ol className="space-y-4">
      {avaliacoes.map((avaliacao) => (
        <li key={avaliacao.id} className="border-l-2 border-brand-100 pl-4">
          <div className="flex flex-wrap items-center gap-2">
            <StatusBadge status={avaliacao.parecer} />
            <span className="text-sm font-medium">
              {avaliacao.instituicao ?? avaliacao.agenteNome}
            </span>
            <span className="text-xs text-slate-500">
              {perfilLabel[avaliacao.tipoAgente]} <Dot className="inline-flex" />{" "}
              {formatDateTime(avaliacao.dataAvaliacao)}
            </span>
          </div>
          {avaliacao.justificativa && (
            <p className="mt-1.5 text-sm text-slate-600">
              {avaliacao.justificativa}
            </p>
          )}
        </li>
      ))}
    </ol>
  );
}
