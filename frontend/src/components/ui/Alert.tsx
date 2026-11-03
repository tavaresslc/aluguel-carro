import { AlertCircle, CheckCircle2, Info, TriangleAlert } from "lucide-react";
import type { ReactNode } from "react";
import { cx } from "../../utils/cx";

const tones = {
  error: {
    style: "border-rose-200 bg-rose-50 text-rose-800",
    Icon: AlertCircle,
  },
  warning: {
    style: "border-amber-200 bg-amber-50 text-amber-900",
    Icon: TriangleAlert,
  },
  info: { style: "border-brand-100 bg-brand-50 text-brand-900", Icon: Info },
  success: {
    style: "border-emerald-200 bg-emerald-50 text-emerald-800",
    Icon: CheckCircle2,
  },
};

export function Alert({
  tone = "error",
  children,
  action,
}: {
  tone?: keyof typeof tones;
  children: ReactNode;
  action?: ReactNode;
}) {
  const { style, Icon } = tones[tone];
  return (
    <div
      role={tone === "error" ? "alert" : "status"}
      className={cx(
        "flex items-start gap-3 rounded-lg border p-3 text-sm",
        style,
      )}
    >
      <Icon className="mt-0.5 size-4 shrink-0" aria-hidden />
      <div className="min-w-0 flex-1">{children}</div>
      {action}
    </div>
  );
}
