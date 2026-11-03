import { use, useId, type ComponentProps, type ReactNode } from "react";
import { cx } from "../../utils/cx";
import { FormErrorsContext } from "./form-context";

interface FieldProps {
  label: string;
  name: string;
  hint?: string;
}

interface ControlProps {
  id: string;
  name: string;
  "aria-invalid": boolean;
  "aria-describedby": string | undefined;
  className: string;
}

const controlStyle =
  "block w-full rounded-lg border bg-white px-3 text-sm text-slate-900 placeholder:text-slate-400 disabled:bg-slate-100";

function Field({
  label,
  name,
  hint,
  required,
  children,
}: FieldProps & {
  required?: boolean;
  children: (props: ControlProps) => ReactNode;
}) {
  const id = useId();
  const error = use(FormErrorsContext)[name];
  const messageId = error || hint ? `${id}-msg` : undefined;

  return (
    <div>
      <label
        htmlFor={id}
        className="mb-1.5 block text-sm font-medium text-slate-700"
      >
        {label}
        {required && (
          <span aria-hidden className="ml-0.5 text-rose-600">
            *
          </span>
        )}
      </label>
      {children({
        id,
        name,
        "aria-invalid": Boolean(error),
        "aria-describedby": messageId,
        className: cx(
          controlStyle,
          error ? "border-rose-400" : "border-slate-300",
        ),
      })}
      {(error || hint) && (
        <p
          id={messageId}
          className={cx(
            "mt-1.5 text-xs",
            error ? "text-rose-700" : "text-slate-500",
          )}
        >
          {error ?? hint}
        </p>
      )}
    </div>
  );
}

export function Input({
  label,
  name,
  hint,
  className,
  ...props
}: FieldProps & ComponentProps<"input">) {
  return (
    <Field label={label} name={name} hint={hint} required={props.required}>
      {(control) => (
        <input
          {...props}
          {...control}
          className={cx(control.className, "h-11", className)}
        />
      )}
    </Field>
  );
}

export function Textarea({
  label,
  name,
  hint,
  ...props
}: FieldProps & ComponentProps<"textarea">) {
  return (
    <Field label={label} name={name} hint={hint} required={props.required}>
      {(control) => (
        <textarea
          rows={3}
          {...props}
          {...control}
          className={cx(control.className, "py-2.5")}
        />
      )}
    </Field>
  );
}

export function Select({
  label,
  name,
  hint,
  options,
  placeholder,
  ...props
}: FieldProps &
  ComponentProps<"select"> & {
    options: { value: string | number; label: string }[];
    placeholder?: string;
  }) {
  return (
    <Field label={label} name={name} hint={hint} required={props.required}>
      {(control) => (
        <select
          {...props}
          {...control}
          className={cx(control.className, "h-11")}
        >
          {placeholder && <option value="">{placeholder}</option>}
          {options.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      )}
    </Field>
  );
}
