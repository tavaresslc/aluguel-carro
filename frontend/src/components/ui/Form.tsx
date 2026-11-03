import type { FormEvent, ReactNode } from "react";
import { useSubmit } from "../../hooks/useSubmit";
import { Alert } from "./Alert";
import { Button } from "./Button";
import { FormErrorsContext } from "./form-context";

export function Form({
  onSubmit,
  onSuccess,
  onCancel,
  submitLabel,
  submitVariant,
  successMessage,
  fullWidth,
  children,
}: {
  onSubmit: (data: FormData) => Promise<unknown>;
  onSuccess?: () => void;
  onCancel?: () => void;
  submitLabel: string;
  submitVariant?: "primary" | "danger";
  successMessage?: string;
  fullWidth?: boolean;
  children: ReactNode;
}) {
  const { pending, succeeded, error, fieldErrors, details, run } = useSubmit();

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const data = new FormData(event.currentTarget);
    if (await run(() => onSubmit(data))) onSuccess?.();
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <FormErrorsContext value={fieldErrors}>{children}</FormErrorsContext>
      {error && (
        <Alert>
          <p>{error.message}</p>
          {details.length > 0 && (
            <ul className="mt-1 list-disc pl-4">
              {details.map((detail) => (
                <li key={detail}>{detail}</li>
              ))}
            </ul>
          )}
        </Alert>
      )}
      {succeeded && successMessage && <Alert tone="success">{successMessage}</Alert>}
      <div className="flex flex-wrap justify-end gap-2 pt-2">
        {onCancel && (
          <Button variant="secondary" onClick={onCancel} disabled={pending}>
            Cancelar
          </Button>
        )}
        <Button type="submit" variant={submitVariant} loading={pending} className={fullWidth ? "w-full" : undefined}>
          {submitLabel}
        </Button>
      </div>
    </form>
  );
}
