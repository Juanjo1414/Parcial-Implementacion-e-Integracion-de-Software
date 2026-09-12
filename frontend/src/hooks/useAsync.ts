import { useCallback, useEffect, useRef, useState } from "react";
import { ApiError } from "../api/client";

interface AsyncState<T> {
  data: T | null;
  error: string | null;
  isLoading: boolean;
}

// Encapsula el patrón que se repite en cada pantalla de listado o detalle:
// pedir datos a la API y exponer sus tres estados posibles (cargando, error,
// listo) sin repetir el mismo try/catch/finally en cada página.
export function useAsync<T>(fetcher: () => Promise<T>, deps: unknown[]) {
  const [state, setState] = useState<AsyncState<T>>({ data: null, error: null, isLoading: true });
  const fetcherRef = useRef(fetcher);
  fetcherRef.current = fetcher;

  const reload = useCallback(() => {
    let cancelled = false;
    setState((prev) => ({ ...prev, isLoading: true, error: null }));
    fetcherRef
      .current()
      .then((data) => {
        if (!cancelled) setState({ data, error: null, isLoading: false });
      })
      .catch((error: unknown) => {
        if (cancelled) return;
        const message = error instanceof ApiError ? error.message : "No se pudo cargar la información.";
        setState({ data: null, error: message, isLoading: false });
      });
    return () => {
      cancelled = true;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps);

  useEffect(() => reload(), [reload]);

  return { ...state, reload };
}
