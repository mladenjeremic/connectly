export function timeAgo(iso: string): string {
  const diff = Math.max(0, Date.now() - new Date(iso).getTime());
  const s = Math.floor(diff / 1000);
  if (s < 60) return "upravo sada";
  const m = Math.floor(s / 60);
  if (m < 60) return `pre ${m} min`;
  const h = Math.floor(m / 60);
  if (h < 24) return `pre ${h} h`;
  const d = Math.floor(h / 24);
  if (d < 7) return `pre ${d} d`;
  return new Date(iso).toLocaleDateString("sr-RS");
}

export function dateTime(iso: string | null | undefined): string {
  return iso ? new Date(iso).toLocaleString("sr-RS") : "—";
}

export function initials(name: string): string {
  return name.trim().slice(0, 2).toUpperCase() || "?";
}

const GRADIENTS = [
  "from-fuchsia-500 to-orange-400",
  "from-sky-500 to-indigo-600",
  "from-emerald-400 to-teal-600",
  "from-rose-500 to-amber-400",
  "from-violet-500 to-pink-500",
  "from-cyan-400 to-blue-600",
];

/** Stabilan gradijent po id-ju, za objave bez slike. */
export function gradientFor(id: number): string {
  return GRADIENTS[Math.abs(id) % GRADIENTS.length];
}

export const REPORT_CATEGORIES: { value: string; label: string }[] = [
  { value: "SPAM", label: "Spam" },
  { value: "HARASSMENT", label: "Uznemiravanje" },
  { value: "HATE_SPEECH", label: "Govor mržnje" },
  { value: "VIOLENCE", label: "Nasilje" },
  { value: "NUDITY", label: "Neprimeren sadržaj" },
  { value: "MISINFORMATION", label: "Dezinformacije" },
  { value: "OTHER", label: "Ostalo" },
];

export function categoryLabel(value: string): string {
  return REPORT_CATEGORIES.find((c) => c.value === value)?.label ?? value;
}
