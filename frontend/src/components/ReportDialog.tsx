import { useState, type FormEvent } from "react";
import { toast } from "sonner";
import { api, errorMessage } from "../api/client";
import { REPORT_CATEGORIES } from "../lib/format";
import { btnDanger, btnSecondary, inputCls } from "../lib/styles";
import type { ReportTargetType } from "../types";
import { ErrorText, Modal } from "./ui";

interface Props {
  targetType: ReportTargetType;
  targetId: number;
  onClose: () => void;
}

export default function ReportDialog({ targetType, targetId, onClose }: Props) {
  const [category, setCategory] = useState("SPAM");
  const [description, setDescription] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function submit(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      await api.post("/reports", { targetType, targetId, category, description });
      toast.success("Prijava je poslata. Hvala!");
      onClose();
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setBusy(false);
    }
  }

  return (
    <Modal title={targetType === "POST" ? "Prijavi objavu" : "Prijavi komentar"} onClose={onClose}>
      <form className="space-y-3 p-4" onSubmit={submit}>
        <label className="block text-sm font-medium">
          Razlog
          <select className={`${inputCls} mt-1`} value={category} onChange={(e) => setCategory(e.target.value)}>
            {REPORT_CATEGORIES.map((c) => <option key={c.value} value={c.value}>{c.label}</option>)}
          </select>
        </label>
        <label className="block text-sm font-medium">
          Opis (opciono)
          <textarea className={`${inputCls} mt-1`} rows={3} maxLength={1000} value={description}
            onChange={(e) => setDescription(e.target.value)} placeholder="Šta nije u redu sa ovim sadržajem?" />
        </label>
        {error && <ErrorText>{error}</ErrorText>}
        <div className="flex justify-end gap-2">
          <button type="button" className={btnSecondary} onClick={onClose}>Otkaži</button>
          <button className={btnDanger} disabled={busy}>{busy ? "Slanje..." : "Prijavi"}</button>
        </div>
      </form>
    </Modal>
  );
}
