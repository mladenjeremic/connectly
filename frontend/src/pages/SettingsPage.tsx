import { useEffect, useState, type FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import { toast } from "sonner";
import { api, errorMessage } from "../api/client";
import { useAuth } from "../auth/AuthContext";
import { Avatar, ErrorText, Modal } from "../components/ui";
import { useTheme } from "../context/ThemeContext";
import type { UserSummary } from "../types";
import { btnDanger, btnPrimary, btnSecondary, cardCls, inputCls } from "../lib/styles";

export default function SettingsPage() {
  const { logout, isAdmin } = useAuth();
  const { theme, toggle } = useTheme();
  const navigate = useNavigate();

  const [current, setCurrent] = useState("");
  const [next, setNext] = useState("");
  const [pwError, setPwError] = useState<string | null>(null);
  const [pwBusy, setPwBusy] = useState(false);

  const [blocked, setBlocked] = useState<UserSummary[]>([]);
  useEffect(() => {
    api.get<UserSummary[]>("/blocks").then((r) => setBlocked(r.data)).catch(() => undefined);
  }, []);
  async function unblock(id: number) {
    try {
      await api.delete(`/blocks/${id}`);
      setBlocked((prev) => prev.filter((u) => u.id !== id));
      toast.success("Korisnik je odblokiran.");
    } catch (err) {
      toast.error(errorMessage(err));
    }
  }

  const [deactivating, setDeactivating] = useState(false);
  const [password, setPassword] = useState("");
  const [deError, setDeError] = useState<string | null>(null);
  const [deBusy, setDeBusy] = useState(false);

  async function changePassword(e: FormEvent) {
    e.preventDefault();
    setPwError(null);
    if (next.length < 8) { setPwError("Nova lozinka mora imati najmanje 8 karaktera."); return; }
    setPwBusy(true);
    try {
      await api.put("/users/me/password", { currentPassword: current, newPassword: next });
      toast.success("Lozinka je promenjena.");
      setCurrent(""); setNext("");
    } catch (err) {
      setPwError(errorMessage(err));
    } finally {
      setPwBusy(false);
    }
  }

  async function deactivate(e: FormEvent) {
    e.preventDefault();
    setDeBusy(true);
    setDeError(null);
    try {
      await api.post("/users/me/deactivate", { password });
      logout();
      navigate("/login");
      toast.success("Nalog je deaktiviran.");
    } catch (err) {
      setDeError(errorMessage(err));
    } finally {
      setDeBusy(false);
    }
  }

  return (
    <div className="mx-auto max-w-lg space-y-6 px-4 pt-6 md:pt-10">
      <h1 className="text-2xl font-bold tracking-tight">Podešavanja</h1>

      <section className={`${cardCls} flex items-center justify-between gap-4 p-5`}>
        <div>
          <h2 className="font-bold">Izgled</h2>
          <p className="text-sm text-neutral-500">Trenutno: {theme === "dark" ? "tamna" : "svetla"} tema</p>
        </div>
        <button className={btnSecondary} onClick={toggle}>Promeni temu</button>
      </section>

      <form className={`${cardCls} space-y-3 p-5`} onSubmit={changePassword}>
        <h2 className="font-bold">Promena lozinke</h2>
        <input className={inputCls} type="password" placeholder="Trenutna lozinka" value={current} onChange={(e) => setCurrent(e.target.value)} required />
        <input className={inputCls} type="password" placeholder="Nova lozinka (min. 8)" value={next} onChange={(e) => setNext(e.target.value)} required />
        {pwError && <ErrorText>{pwError}</ErrorText>}
        <button className={btnPrimary} disabled={pwBusy}>{pwBusy ? "Čuvanje..." : "Promeni lozinku"}</button>
      </form>

      <section className={`${cardCls} space-y-3 p-5`}>
        <h2 className="font-bold">Blokirani korisnici</h2>
        {blocked.length === 0 ? (
          <p className="text-sm text-neutral-500">Nisi blokirao nijednog korisnika.</p>
        ) : blocked.map((u) => (
          <div key={u.id} className="flex items-center gap-3">
            <Avatar url={u.profileImageUrl} name={u.username} size={36} id={u.id} />
            <div className="min-w-0 flex-1">
              <p className="truncate text-sm font-semibold">{u.username}</p>
              <p className="truncate text-xs text-neutral-500">{u.fullName}</p>
            </div>
            <button className={btnSecondary} onClick={() => unblock(u.id)}>Odblokiraj</button>
          </div>
        ))}
      </section>

      {!isAdmin && (
        <section className={`${cardCls} space-y-2 !border-red-200 p-5 dark:!border-red-900`}>
          <h2 className="font-semibold text-red-600">Deaktivacija naloga</h2>
          <p className="text-sm text-neutral-500">
            Nalog će biti deaktiviran i nećeš moći da se prijaviš. Da ga ponovo aktiviraš, obrati se administratoru.
          </p>
          <button className={btnDanger} onClick={() => setDeactivating(true)}>Deaktiviraj nalog</button>
        </section>
      )}

      {deactivating && (
        <Modal title="Deaktiviraj nalog" onClose={() => setDeactivating(false)}>
          <form className="space-y-3 p-4" onSubmit={deactivate}>
            <p className="text-sm">Unesi lozinku da potvrdiš deaktivaciju.</p>
            <input className={inputCls} type="password" placeholder="Lozinka" value={password} onChange={(e) => setPassword(e.target.value)} required />
            {deError && <ErrorText>{deError}</ErrorText>}
            <div className="flex justify-end gap-2">
              <button type="button" className={btnSecondary} onClick={() => setDeactivating(false)}>Otkaži</button>
              <button className={btnDanger} disabled={deBusy}>{deBusy ? "Sačekaj..." : "Deaktiviraj"}</button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  );
}
