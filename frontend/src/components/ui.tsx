import { useEffect, type ReactNode } from "react";
import { LoaderCircle, X } from "lucide-react";
import { gradientFor, initials } from "../lib/format";
import { btnDanger, btnSecondary } from "../lib/styles";

export function Avatar({ url, name, size = 40, id = 0 }: { url?: string | null; name: string; size?: number; id?: number }) {
  const style = { width: size, height: size, minWidth: size };
  if (url) {
    return <img src={url} alt={name} style={style} className="rounded-full bg-neutral-200 object-cover dark:bg-neutral-800" />;
  }
  return (
    <div
      style={{ ...style, fontSize: Math.max(10, size * 0.38) }}
      className={`flex items-center justify-center rounded-full bg-gradient-to-br ${gradientFor(id || name.length)} font-semibold text-white`}
      aria-label={name}
    >
      {initials(name)}
    </div>
  );
}

export function Spinner({ className = "" }: { className?: string }) {
  return (
    <div className={`flex justify-center py-6 text-neutral-400 ${className}`}>
      <LoaderCircle className="animate-spin" size={24} />
    </div>
  );
}

export function ErrorText({ children }: { children: ReactNode }) {
  return <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-600 dark:bg-red-950/40 dark:text-red-400">{children}</p>;
}

export function EmptyState({ children, icon }: { children: ReactNode; icon?: ReactNode }) {
  return (
    <div className="flex flex-col items-center gap-3 py-12 text-center text-sm text-neutral-500 dark:text-neutral-400">
      {icon && <span className="flex h-14 w-14 items-center justify-center rounded-full bg-neutral-100 text-neutral-400 dark:bg-neutral-900">{icon}</span>}
      <p className="max-w-xs">{children}</p>
    </div>
  );
}

export function Skeleton({ className = "" }: { className?: string }) {
  return <div className={`skeleton ${className}`} aria-hidden />;
}

/** Skeleton jedne objave u feed-u dok se učitava. */
export function PostSkeleton() {
  return (
    <div className="mb-5 rounded-2xl border border-neutral-200/80 bg-white p-3 dark:border-neutral-800 dark:bg-neutral-950">
      <div className="flex items-center gap-3 pb-3">
        <Skeleton className="h-9 w-9 !rounded-full" />
        <Skeleton className="h-3 w-32" />
      </div>
      <Skeleton className="aspect-square w-full !rounded-xl" />
      <Skeleton className="mt-3 h-3 w-24" />
      <Skeleton className="mt-2 h-3 w-48" />
    </div>
  );
}

interface ModalProps {
  title?: string;
  onClose: () => void;
  children: ReactNode;
  wide?: boolean;
}

export function Modal({ title, onClose, children, wide = false }: ModalProps) {
  useEffect(() => {
    const onKey = (e: KeyboardEvent) => { if (e.key === "Escape") onClose(); };
    document.addEventListener("keydown", onKey);
    const prev = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      document.removeEventListener("keydown", onKey);
      document.body.style.overflow = prev;
    };
  }, [onClose]);

  return (
    <div className="animate-fade-in fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4 backdrop-blur-sm" onMouseDown={onClose}>
      <div
        role="dialog"
        aria-modal="true"
        onMouseDown={(e) => e.stopPropagation()}
        className={`animate-slide-up max-h-[90vh] w-full overflow-hidden rounded-3xl bg-white shadow-2xl dark:bg-neutral-900 ${wide ? "max-w-3xl" : "max-w-md"}`}
      >
        <div className="flex items-center justify-between border-b border-neutral-200 px-4 py-3 dark:border-neutral-800">
          <h2 className="text-base font-bold tracking-tight">{title}</h2>
          <button onClick={onClose} aria-label="Zatvori" className="rounded-full p-1 hover:bg-neutral-100 dark:hover:bg-neutral-800">
            <X size={20} />
          </button>
        </div>
        <div className="max-h-[calc(90vh-3.5rem)] overflow-y-auto">{children}</div>
      </div>
    </div>
  );
}

interface ConfirmProps {
  title: string;
  message: string;
  confirmLabel?: string;
  busy?: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}

export function ConfirmDialog({ title, message, confirmLabel = "Obriši", busy = false, onConfirm, onCancel }: ConfirmProps) {
  return (
    <Modal title={title} onClose={onCancel}>
      <div className="space-y-4 p-4">
        <p className="text-sm text-neutral-600 dark:text-neutral-300">{message}</p>
        <div className="flex justify-end gap-2">
          <button className={btnSecondary} onClick={onCancel}>Otkaži</button>
          <button className={btnDanger} onClick={onConfirm} disabled={busy}>{busy ? "Sačekaj..." : confirmLabel}</button>
        </div>
      </div>
    </Modal>
  );
}
