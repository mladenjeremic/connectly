import { Link, Navigate } from "react-router-dom";
import { Bookmark, Heart, MessageCircle, Sparkles, Users } from "lucide-react";
import { useAuth } from "../auth/AuthContext";
import { btnPrimary, btnSecondary } from "../lib/styles";

const FEATURES = [
  { icon: Heart, title: "Lajkuj", text: "Objave i komentare, jednim dodirom." },
  { icon: MessageCircle, title: "Razgovaraj", text: "Odgovaraj na komentare i priče." },
  { icon: Users, title: "Prati", text: "Budi u toku sa ljudima koje voliš." },
  { icon: Bookmark, title: "Sačuvaj", text: "Privatna lista omiljenih objava." },
];

export default function LandingPage() {
  const { user } = useAuth();
  if (user) return <Navigate to="/feed" replace />;

  return (
    <div className="relative flex min-h-screen flex-col items-center justify-center overflow-hidden px-6 py-12 text-center">
      <div aria-hidden className="pointer-events-none absolute -left-32 -top-32 h-[28rem] w-[28rem] rounded-full bg-indigo-500/25 blur-3xl" />
      <div aria-hidden className="pointer-events-none absolute -bottom-40 -right-24 h-[30rem] w-[30rem] rounded-full bg-fuchsia-500/20 blur-3xl" />
      <div aria-hidden className="pointer-events-none absolute left-1/2 top-1/4 h-72 w-72 -translate-x-1/2 rounded-full bg-orange-400/15 blur-3xl" />

      <div className="animate-slide-up relative flex flex-col items-center">
        <span className="brand-gradient mb-6 flex h-[72px] w-[72px] items-center justify-center rounded-3xl shadow-xl shadow-fuchsia-500/30">
          <svg viewBox="0 0 24 24" width="42" height="42" fill="none" stroke="#fff" strokeWidth="2.4">
            <circle cx="8" cy="12" r="4" /><circle cx="16" cy="12" r="4" />
          </svg>
        </span>
        <span className="mb-4 inline-flex items-center gap-1.5 rounded-full bg-accent-soft px-3 py-1 text-xs font-semibold text-accent dark:bg-indigo-950/50">
          <Sparkles size={13} /> Društvena mreža za tvoje trenutke
        </span>
        <h1 className="brand-text text-5xl font-extrabold tracking-tight sm:text-6xl">Connectly</h1>
        <p className="mt-4 max-w-md text-lg text-neutral-600 dark:text-neutral-300">
          Deli trenutke, prati ljude koje voliš i budi u toku sa onim što je važno tebi.
        </p>
        <div className="mt-8 flex gap-3">
          <Link to="/register" className={`${btnPrimary} !px-6 !py-2.5`}>Napravi nalog</Link>
          <Link to="/login" className={`${btnSecondary} !px-6 !py-2.5`}>Prijavi se</Link>
        </div>
        <div className="mt-14 grid max-w-2xl grid-cols-2 gap-3 sm:grid-cols-4">
          {FEATURES.map(({ icon: Icon, title, text }) => (
            <div key={title} className="rounded-2xl border border-neutral-200/80 bg-white/70 p-4 text-left shadow-sm backdrop-blur dark:border-neutral-800 dark:bg-neutral-950/60">
              <span className="mb-2 flex h-9 w-9 items-center justify-center rounded-xl bg-accent-soft text-accent dark:bg-indigo-950/50"><Icon size={18} /></span>
              <p className="text-sm font-bold">{title}</p>
              <p className="mt-0.5 text-xs text-neutral-500 dark:text-neutral-400">{text}</p>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
