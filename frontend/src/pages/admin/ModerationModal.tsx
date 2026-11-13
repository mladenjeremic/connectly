import { useState, type FormEvent } from "react";
import { toast } from "sonner";
import { api, errorMessage } from "../../api/client";
import { ErrorText, Modal } from "../../components/ui";
import { btnDanger, btnPrimary, btnSecondary, inputCls } from "../../lib/styles";
import type { AdminUser } from "../../types";

export type ModerationMode = "ban" | "permanent-ban" | "unban" | "deactivate" | "reactivate";

const TITLES: Record<ModerationMode, string> = {
  ban: "Privremena zabrana",
  "permanent-ban": "Trajna zabrana",
  unban: "Skini zabranu",
  deactivate: "Deaktiviraj nalog",
  reactivate: "Ponovo aktiviraj nalog",
};

interface Props {
  user: AdminUser;
  mode: ModerationMode;
  onClose: () => void;
  onDone: (updated: AdminUser) => void;
}

export default function ModerationModal({ user, mode, onClose, onDone }: Props) {
  const [days, setDays] = useState(7);
  const [reason, setReason] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const destructive = mode === "ban" || mode === "permanent-ban" || mode === "deactivate";

  async function submit(e: FormEvent) {
    e.preventDefault();
    if (!reason.trim()) { setError("Razlog je obavezan."); return; }
    setBusy(true);
    setError(null);
    try {
      const body = mode === "ban" ? { days, reason } : { reason };
      const { data } = await api.post<AdminUser>(`/admin/users/${user.id}/${mode}`, body);
      toast.success("Akcija je sprovedena i zabeležena u audit log.");
      onDone(data);
      onClose();
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setBusy(false);
    }
  }

  return (
    <Modal title={`${TITLES[mode]}: @${user.username}`} onClose={onClose}>
      <form className="space-y-3 p-4" onSubmit={submit}>
        {mode === "ban" && (
          <label className="block text-sm font-medium">Trajanje
            <select className={`${inputCls} mt-1`} value={days} onChange={(e) => setDays(Number(e.target.value))}>
              {[1, 3, 7, 30].map((d) => <option key={d} value={d}>{d} {d === 1 ? "dan" : "dana"}</option>)}
            </select>
          </label>
        )}
        {mode === "permanent-ban" && (
          <p className="rounded-lg bg-red-50 p-3 text-sm text-red-700 dark:bg-red-950/40 dark:text-red-300">
            Trajna zabrana se ne skida automatski. Korisnik se više neće moći prijaviti dok je administrator ne skine.
          </p>
        )}
        <label className="block text-sm font-medium">Razlog (obavezno)
          <textarea className={`${inputCls} mt-1`} rows={3} maxLength={500} value={reason} onChange={(e) => setReason(e.target.value)}
            placeholder="Razlog se čuva u audit logu i prikazuje korisniku." />
        </label>
        {error && <ErrorText>{error}</ErrorText>}
        <div className="flex justify-end gap-2">
          <button type="button" className={btnSecondary} onClick={onClose}>Otkaži</button>
          <button className={destructive ? btnDanger : btnPrimary} disabled={busy}>{busy ? "Sačekaj..." : "Potvrdi"}</button>
        </div>
      </form>
    </Modal>
  );
}
