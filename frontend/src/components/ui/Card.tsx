import type { ReactNode } from "react";
import { cx } from "../../utils/cx";

export function Card({
  title,
  actions,
  className,
  children,
}: {
  title?: string;
  actions?: ReactNode;
  className?: string;
  children: ReactNode;
}) {
  return (
    <section
      className={cx(
        "rounded-xl border border-slate-200 bg-white shadow-sm",
        className,
      )}
    >
      {title && (
        <header className="flex items-center justify-between gap-3 border-b border-slate-100 px-5 py-3.5">
          <h2 className="text-base font-semibold">{title}</h2>
          {actions}
        </header>
      )}
      <div className="p-5">{children}</div>
    </section>
  );
}

export function InfoGrid({
  items,
}: {
  items: { label: string; value: ReactNode }[];
}) {
  return (
    <dl className="grid gap-x-6 gap-y-4 sm:grid-cols-2">
      {items.map(({ label, value }) => (
        <div key={label}>
          <dt className="text-xs font-medium uppercase tracking-wide text-slate-500">
            {label}
          </dt>
          <dd className="mt-0.5 text-sm text-slate-900">{value || "-"}</dd>
        </div>
      ))}
    </dl>
  );
}
