/**
 * Periodično osvežavanje koje staje dok je tab skriven (štedi bateriju i besplatni hosting)
 * i odmah osvežava kada se korisnik vrati na tab. Vraća funkciju za čišćenje.
 */
export function startPolling(fn: () => void, intervalMs: number): () => void {
  const tick = () => { if (!document.hidden) fn(); };
  const timer = setInterval(tick, intervalMs);
  const onVisible = () => { if (!document.hidden) fn(); };
  document.addEventListener("visibilitychange", onVisible);
  return () => {
    clearInterval(timer);
    document.removeEventListener("visibilitychange", onVisible);
  };
}
