import type { Status } from "../../types/api";
import { cx } from "../../utils/cx";
import { statusInfo, type Tone } from "../../utils/labels";

const tones: Record<Tone, string> = {
  amber: "bg-amber-100 text-amber-800 ring-amber-200",
  emerald: "bg-emerald-100 text-emerald-800 ring-emerald-200",
  rose: "bg-rose-100 text-rose-800 ring-rose-200",
  slate: "bg-slate-100 text-slate-700 ring-slate-200",
  sky: "bg-sky-100 text-sky-800 ring-sky-200",
};

export function Badge({ tone, children }: { tone: Tone; children: string }) {
  return (
    <span
      className={cx(
        "inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ring-1 ring-inset",
        tones[tone],
      )}
    >
      {children}
    </span>
  );
}

export function StatusBadge({ status }: { status: Status }) {
  const { label, tone } = statusInfo[status];
  return <Badge tone={tone}>{label}</Badge>;
}
