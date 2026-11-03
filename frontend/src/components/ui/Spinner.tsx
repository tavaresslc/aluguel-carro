import { Loader2 } from "lucide-react";
import { cx } from "../../utils/cx";

export function Spinner({ fullScreen }: { fullScreen?: boolean }) {
  return (
    <div
      role="status"
      aria-label="Carregando"
      className={cx(
        "flex items-center justify-center text-brand-600",
        fullScreen ? "min-h-screen" : "py-16",
      )}
    >
      <Loader2 className="size-7 animate-spin" aria-hidden />
    </div>
  );
}
