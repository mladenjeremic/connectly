import { useState, type FormEvent } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import { errorMessage } from "../api/client";
import { btnPrimary, cardCls, inputCls } from "../lib/styles";
import AuthShell from "../components/AuthShell";
import { ErrorText } from "../components/ui";

export default function LoginPage() {
  const { user, login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  if (user) return <Navigate to="/feed" replace />;

  async function submit(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      await login(email, password);
      navigate("/feed");
    } catch (err) {
      setError(errorMessage(err));   // npr. razlog i trajanje zabrane
    } finally {
      setBusy(false);
    }
  }

  return (
    <AuthShell subtitle="Dobrodošao nazad! Prijavi se da nastaviš.">
      <form className={`${cardCls} space-y-3 p-7`} onSubmit={submit}>
        <input className={inputCls} type="email" placeholder="Email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        <input className={inputCls} type="password" placeholder="Lozinka" value={password} onChange={(e) => setPassword(e.target.value)} required />
        {error && <ErrorText>{error}</ErrorText>}
        <button className={`${btnPrimary} w-full !py-2.5`} disabled={busy}>{busy ? "Prijavljivanje..." : "Prijavi se"}</button>
      </form>
      <p className={`${cardCls} p-4 text-center text-sm`}>
        Nemaš nalog? <Link to="/register" className="font-semibold text-accent hover:underline">Registruj se</Link>
      </p>
    </AuthShell>
  );
}
