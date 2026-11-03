import { useEffect, useRef, useState } from "react";
import { ApiError } from "../services/http";

interface Result<T> {
  dep?: string | number;
  version: number;
  data?: T;
  error?: string;
}

export interface Query<T> {
  data?: T;
  error?: string;
  loading: boolean;
  reload: () => void;
}

export const errorMessage = (error: unknown) =>
  error instanceof ApiError ? error.message : "Ocorreu um erro inesperado. Tente novamente.";

export function useFetch<T>(fetcher: () => Promise<T>, dep?: string | number): Query<T> {
  const [version, setVersion] = useState(0);
  const [result, setResult] = useState<Result<T>>();
  const latest = useRef(fetcher);

  useEffect(() => {
    latest.current = fetcher;
  });

  useEffect(() => {
    let active = true;
    latest.current().then(
      (data) => active && setResult({ dep, version, data }),
      (error) => active && setResult({ dep, version, error: errorMessage(error) }),
    );
    return () => {
      active = false;
    };
  }, [dep, version]);

  const current = result?.dep === dep ? result : undefined;
  return {
    data: current?.data,
    error: current?.error,
    loading: current?.version !== version,
    reload: () => setVersion((v) => v + 1),
  };
}
