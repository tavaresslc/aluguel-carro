import type { ReactNode } from "react";
import type { Query } from "../../hooks/useFetch";
import { Alert } from "./Alert";
import { Button } from "./Button";
import { Spinner } from "./Spinner";

export function DataState<T>({
  query,
  children,
}: {
  query: Query<T>;
  children: (data: T) => ReactNode;
}) {
  if (query.data !== undefined) return <>{children(query.data)}</>;
  if (!query.loading && query.error) {
    return (
      <Alert
        action={
          <Button size="sm" variant="secondary" onClick={query.reload}>
            Tentar novamente
          </Button>
        }
      >
        {query.error}
      </Alert>
    );
  }
  return <Spinner />;
}
