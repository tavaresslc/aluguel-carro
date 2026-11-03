import type { ReactNode } from "react";
import { Form } from "./Form";
import { Modal } from "./Modal";

export function ConfirmModal({
  title,
  confirmLabel,
  onConfirm,
  onClose,
  onSuccess,
  children,
}: {
  title: string;
  confirmLabel: string;
  onConfirm: () => Promise<unknown>;
  onClose: () => void;
  onSuccess: () => void;
  children: ReactNode;
}) {
  return (
    <Modal title={title} onClose={onClose}>
      <Form
        onSubmit={onConfirm}
        onCancel={onClose}
        submitLabel={confirmLabel}
        submitVariant="danger"
        onSuccess={() => {
          onClose();
          onSuccess();
        }}
      >
        <p className="text-sm text-slate-600">{children}</p>
      </Form>
    </Modal>
  );
}
