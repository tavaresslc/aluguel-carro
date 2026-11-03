import type { ComponentProps } from "react";
import { Form } from "./Form";
import { Modal } from "./Modal";

export function FormModal({
  title,
  onClose,
  onSuccess,
  ...form
}: { title: string; onClose: () => void; onSuccess: () => void } & Omit<
  ComponentProps<typeof Form>,
  "onSuccess" | "onCancel"
>) {
  return (
    <Modal title={title} onClose={onClose}>
      <Form
        {...form}
        onCancel={onClose}
        onSuccess={() => {
          onClose();
          onSuccess();
        }}
      />
    </Modal>
  );
}
