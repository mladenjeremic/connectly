// Koje priče je ovaj pregledač već prikazao (prsten oko avatara postaje siv). Čuva se samo lokalno.
const KEY = "connectly_seen_stories";
const MAX = 500;

export function getSeenStories(): Set<number> {
  try {
    const raw = localStorage.getItem(KEY);
    const list: unknown = raw ? JSON.parse(raw) : [];
    return new Set(Array.isArray(list) ? list.filter((x): x is number => typeof x === "number") : []);
  } catch {
    return new Set();
  }
}

export function markStorySeen(id: number): void {
  try {
    const seen = getSeenStories();
    if (seen.has(id)) return;
    seen.add(id);
    localStorage.setItem(KEY, JSON.stringify([...seen].slice(-MAX)));
  } catch {
    // privatni režim / pun storage: nije bitno
  }
}
