import { CarFront } from "lucide-react";
import { Link } from "react-router";
import { cx } from "../../utils/cx";

export function Brand({ light }: { light?: boolean }) {
  return (
    <Link to="/" className="inline-flex items-center gap-2.5">
      <span
        className={cx(
          "flex size-9 items-center justify-center rounded-lg",
          light ? "bg-white/15" : "bg-brand-600",
        )}
      >
        <CarFront className="size-5 text-white" aria-hidden />
      </span>
      <span
        className={cx(
          "font-display text-xl font-bold",
          light ? "text-white" : "text-slate-900",
        )}
      >
        Carrent
      </span>
    </Link>
  );
}
