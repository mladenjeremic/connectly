import { useState, type FormEvent } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import { errorMessage } from "../api/client";
import { btnPrimary, cardCls, inputCls } from "../lib/styles";
import AuthShell from "../components/AuthShell";
import { ErrorText } from "../components/ui";

export default function RegisterPage() {
  const { user, register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ firstName: "", lastName: "", username: "", email: "", password: "" });
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  if (user) return <Navigate to="/feed" replace />;

  const set = (key: keyof typeof form) => (e: { target: { value: string } }) =>
    setForm((f) => ({ ...f, [key]: e.target.value }));

  async function submit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    if (!/^[a-zA-Z0-9_.]{3,40}$/.test(form.username)) {
      setError("Korisničko ime: 3-40 karaktera, slova, brojevi, _ i .");
      return;
    }
    if (form.password.length < 8) {
      setError("Lozinka mora imati najmanje 8 karaktera.");
      return;
    }
    setBusy(true);
    try {
      await register(form);
      navigate("/feed");
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setBusy(false);
    }
  }

  return (
    <AuthShell subtitle="Registruj se da vidiš objave i fotografije prijatelja.">
      <form className={`${cardCls} space-y-3 p-7`} onSubmit={submit}>
        <div className="flex gap-2">
          <input className={inputCls} placeholder="Ime" value={form.firstName} onChange={set("firstName")} required maxLength={60} />
          <input className={inputCls} placeholder="Prezime" value={form.lastName} onChange={set("lastName")} required maxLength={60} />
        </div>
        <input className={inputCls} placeholder="Korisničko ime" value={form.username} onChange={set("username")} required />
        <input className={inputCls} type="email" placeholder="Email" value={form.email} onChange={set("email")} required />
        <input className={inputCls} type="password" placeholder="Lozinka (min. 8)" value={form.password} onChange={set("password")} required />
        {error && <ErrorText>{error}</ErrorText>}
        <button className={`${btnPrimary} w-full !py-2.5`} disabled={busy}>{busy ? "Kreiranje..." : "Napravi nalog"}</button>
      </form>
      <p className={`${cardCls} p-4 text-center text-sm`}>
        Već imaš nalog? <Link to="/login" className="font-semibold text-accent hover:underline">Prijavi se</Link>
      </p>
    </AuthShell>
  );
}
