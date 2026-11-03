import { CarFront, Dot } from "lucide-react";
import type { ReactNode } from "react";
import type { Automovel } from "../../types/api";
import { perfilLabel } from "../../utils/labels";
import { Badge } from "../ui/Badge";

export function AutomovelCard({
  automovel,
  action,
}: {
  automovel: Automovel;
  action?: ReactNode;
}) {
  return (
    <article className="flex flex-col rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
      <div className="flex items-start justify-between gap-3">
        <span className="flex size-11 items-center justify-center rounded-lg bg-brand-50 text-brand-600">
          <CarFront className="size-6" aria-hidden />
        </span>
        <Badge tone={automovel.disponivel ? "emerald" : "slate"}>
          {automovel.disponivel ? "Disponível" : "Indisponível"}
        </Badge>
      </div>
      <h2 className="mt-4 text-lg font-semibold">
        {automovel.marca} {automovel.modelo}
      </h2>
      <p className="text-sm text-slate-600">
        {automovel.ano} <Dot className="inline-flex" /> Placa {automovel.placa}
      </p>
      {automovel.proprietario && (
        <p className="mt-3 text-xs text-slate-500">
          Proprietário: {automovel.proprietario.nome} (
          {perfilLabel[automovel.proprietario.perfil]})
        </p>
      )}
      {action && <div className="mt-auto pt-4">{action}</div>}
    </article>
  );
}
