import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { Search } from "lucide-react";
import { api, errorMessage } from "../api/client";
import FollowButton from "../components/FollowButton";
import { Avatar, EmptyState, ErrorText, Skeleton } from "../components/ui";
import { inputCls } from "../lib/styles";
import type { UserSummary } from "../types";

export default function SearchPage() {
  const [q, setQ] = useState("");
  const [results, setResults] = useState<UserSummary[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // debounce: upit se šalje 300 ms nakon poslednjeg kucanja
  useEffect(() => {
    const term = q.trim();
    if (!term) { setResults([]); setError(null); return; }
    setLoading(true);
    const t = setTimeout(() => {
      api.get<UserSummary[]>("/users/search", { params: { q: term } })
        .then((res) => { setResults(res.data); setError(null); })
        .catch((err) => setError(errorMessage(err)))
        .finally(() => setLoading(false));
    }, 300);
    return () => clearTimeout(t);
  }, [q]);

  return (
    <div className="mx-auto max-w-xl px-4 pt-6 md:pt-10">
      <h1 className="mb-4 text-2xl font-bold tracking-tight">Pretraga</h1>
      <div className="relative">
        <Search size={18} className="absolute left-3 top-1/2 -translate-y-1/2 text-neutral-400" />
        <input className={`${inputCls} pl-10`} placeholder="Traži po korisničkom imenu ili imenu..." value={q}
          autoFocus onChange={(e) => setQ(e.target.value)} />
      </div>
      {error && <div className="mt-3"><ErrorText>{error}</ErrorText></div>}
      {loading && (
        <div className="mt-4 space-y-4">
          {[0, 1, 2].map((i) => (
            <div key={i} className="flex items-center gap-3"><Skeleton className="h-12 w-12 !rounded-full" /><Skeleton className="h-3 flex-1" /></div>
          ))}
        </div>
      )}
      {!loading && q.trim() && results.length === 0 && !error && <EmptyState icon={<Search size={24} />}>Nema rezultata za „{q.trim()}“.</EmptyState>}
      {!q.trim() && <EmptyState icon={<Search size={24} />}>Pronađi ljude po korisničkom imenu ili imenu i prezimenu.</EmptyState>}
      <div className="mt-4 space-y-2">
        {results.map((u) => (
          <div key={u.id} className="flex items-center gap-3 rounded-2xl border border-neutral-200/80 bg-white p-3 shadow-sm dark:border-neutral-800 dark:bg-neutral-950">
            <Link to={`/profile/${u.username}`}><Avatar url={u.profileImageUrl} name={u.username} size={46} id={u.id} /></Link>
            <div className="min-w-0 flex-1 text-sm leading-tight">
              <Link to={`/profile/${u.username}`} className="block truncate font-semibold">{u.username}</Link>
              <span className="block truncate text-neutral-500">{u.fullName}</span>
            </div>
            {!u.self && (
              <FollowButton userId={u.id} following={u.followedByMe}
                onChange={(r) => setResults((list) => list.map((x) => (x.id === u.id ? { ...x, followedByMe: r.following } : x)))} />
            )}
          </div>
        ))}
      </div>
    </div>
  );
}
