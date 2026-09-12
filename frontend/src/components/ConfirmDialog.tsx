import { useState } from "react";
import { TriangleAlert } from "lucide-react";

interface ConfirmOptions {
  title: string;
  description: string;
  confirmLabel?: string;
  onConfirm: () => Promise<void> | void;
}

interface ConfirmState extends ConfirmOptions {
  isSubmitting: boolean;
}

// Hook simple para pedir confirmación antes de una acción destructiva
// (retirar un competidor, cancelar una carrera, rechazar una inscripción)
// sin tener que montar un <Modal> distinto en cada pantalla que lo necesita.
export function useConfirmDialog() {
  const [state, setState] = useState<ConfirmState | null>(null);

  const confirm = (options: ConfirmOptions) => setState({ ...options, isSubmitting: false });
  const close = () => setState(null);

  const handleConfirm = async () => {
    if (!state) return;
    setState({ ...state, isSubmitting: true });
    try {
      await state.onConfirm();
      setState(null);
    } catch {
      setState({ ...state, isSubmitting: false });
    }
  };

  const dialog = state ? (
    <div className="modal-backdrop" role="presentation" onClick={close}>
      <div
        className="modal"
        role="alertdialog"
        aria-modal="true"
        aria-labelledby="confirm-title"
        onClick={(event) => event.stopPropagation()}
      >
        <div className="row" style={{ marginBottom: "var(--space-3)" }}>
          <TriangleAlert size={22} color="var(--color-danger)" />
          <h3 id="confirm-title">{state.title}</h3>
        </div>
        <p className="text-muted">{state.description}</p>
        <div className="row" style={{ justifyContent: "flex-end", marginTop: "var(--space-5)" }}>
          <button type="button" className="btn btn--ghost" onClick={close} disabled={state.isSubmitting}>
            Cancelar
          </button>
          <button type="button" className="btn btn--danger" onClick={handleConfirm} disabled={state.isSubmitting}>
            {state.isSubmitting ? "Procesando…" : (state.confirmLabel ?? "Confirmar")}
          </button>
        </div>
      </div>
    </div>
  ) : null;

  return { confirm, dialog };
}
