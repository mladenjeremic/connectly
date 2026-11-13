import type { ReactNode } from "react";

/** Zajednički okvir za prijavu/registraciju: mekani gradijent u pozadini i logo iznad kartice. */
export default function AuthShell({ children, subtitle }: { children: ReactNode; subtitle?: string }) {
  return (
    <div className="relative flex min-h-screen items-center justify-center overflow-hidden px-4 py-8">
      <div aria-hidden className="pointer-events-none absolute -left-24 -top-24 h-96 w-96 rounded-full bg-indigo-500/25 blur-3xl" />
      <div aria-hidden className="pointer-events-none absolute -bottom-32 -right-16 h-[26rem] w-[26rem] rounded-full bg-fuchsia-500/20 blur-3xl" />
      <div aria-hidden className="pointer-events-none absolute right-1/3 top-10 h-64 w-64 rounded-full bg-orange-400/15 blur-3xl" />
      <div className="animate-slide-up relative w-full max-w-sm space-y-4">
        <div className="flex flex-col items-center gap-3 pb-1">
          <span className="brand-gradient flex h-14 w-14 items-center justify-center rounded-2xl shadow-lg shadow-fuchsia-500/30">
            <svg viewBox="0 0 24 24" width="34" height="34" fill="none" stroke="#fff" strokeWidth="2.4">
              <circle cx="8" cy="12" r="4" /><circle cx="16" cy="12" r="4" />
            </svg>
          </span>
          <h1 className="brand-text text-4xl font-extrabold tracking-tight">Connectly</h1>
          {subtitle && <p className="text-center text-sm text-neutral-500 dark:text-neutral-400">{subtitle}</p>}
        </div>
        {children}
      </div>
    </div>
  );
}
