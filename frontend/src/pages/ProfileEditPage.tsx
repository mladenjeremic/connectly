import { useEffect, useState, type FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import { toast } from "sonner";
import { api, errorMessage } from "../api/client";
import { useAuth } from "../auth/AuthContext";
import { ErrorText, Spinner } from "../components/ui";
import { btnPrimary, cardCls, inputCls } from "../lib/styles";
import type { Me } from "../types";

export default function ProfileEditPage() {
  const navigate = useNavigate();
  const { refreshMe } = useAuth();
  const [me, setMe] = useState<Me | null>(null);
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [bio, setBio] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api.get<Me>("/users/me")
      .then((res) => {
        setMe(res.data);
        setFirstName(res.data.firstName);
        setLastName(res.data.lastName);
        setBio(res.data.bio);
      })
      .catch((err) => setError(errorMessage(err)));
  }, []);

  async function submit(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      const { data } = await api.put<Me>("/users/me", { firstName, lastName, bio });
      await refreshMe();
      toast.success("Profil je sačuvan.");
      navigate(`/profile/${data.username}`);
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setBusy(false);
    }
  }

  if (!me) return error ? <div className="mx-auto max-w-md p-6"><ErrorText>{error}</ErrorText></div> : <Spinner className="pt-20" />;

  return (
    <div className="mx-auto max-w-lg px-4 pt-6 md:pt-10">
      <h1 className="mb-5 text-2xl font-bold tracking-tight">Izmeni profil</h1>
      <form className={`${cardCls} space-y-4 p-6`} onSubmit={submit}>
        <label className="block text-sm font-medium">Korisničko ime
          <input className={`${inputCls} mt-1 opacity-60`} value={me.username} disabled />
        </label>
        <div className="flex gap-3">
          <label className="block flex-1 text-sm font-medium">Ime
            <input className={`${inputCls} mt-1`} value={firstName} maxLength={60} required onChange={(e) => setFirstName(e.target.value)} />
          </label>
          <label className="block flex-1 text-sm font-medium">Prezime
            <input className={`${inputCls} mt-1`} value={lastName} maxLength={60} required onChange={(e) => setLastName(e.target.value)} />
          </label>
        </div>
        <label className="block text-sm font-medium">Biografija
          <textarea className={`${inputCls} mt-1`} rows={4} maxLength={300} value={bio} onChange={(e) => setBio(e.target.value)} />
          <span className="text-xs font-normal text-neutral-500">{bio.length}/300</span>
        </label>
        <p className="text-xs text-neutral-500">Profilnu fotografiju menjaš klikom na ikonu kamere na svom profilu.</p>
        {error && <ErrorText>{error}</ErrorText>}
        <button className={btnPrimary} disabled={busy}>{busy ? "Čuvanje..." : "Sačuvaj"}</button>
      </form>
    </div>
  );
}
