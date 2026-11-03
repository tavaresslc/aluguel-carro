import { useState } from "react";
import { ApiError } from "../services/http";
import { errorMessage } from "./useFetch";

export function useSubmit() {
  const [pending, setPending] = useState(false);
  const [succeeded, setSucceeded] = useState(false);
  const [error, setError] = useState<ApiError | Error | null>(null);

  async function run(action: () => Promise<unknown>) {
    setPending(true);
    setError(null);
    setSucceeded(false);
    try {
      await action();
      setSucceeded(true);
      return true;
    } catch (e) {
      setError(e instanceof Error ? e : new Error(errorMessage(e)));
      return false;
    } finally {
      setPending(false);
    }
  }

  const fieldErrors = error instanceof ApiError ? error.fieldErrors : {};
  const details = error instanceof ApiError ? error.details : [];

  return { pending, succeeded, error, fieldErrors, details, run };
}
