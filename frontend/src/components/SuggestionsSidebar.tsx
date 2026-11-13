import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../api/client";
import { useAuth } from "../auth/AuthContext";
import type { UserSummary } from "../types";
import FollowButton from "./FollowButton";
import { Avatar, Skeleton } from "./ui";

/** Desna kolona feed-a: moj nalog + „Predloženo za tebe“. */
export default function SuggestionsSidebar() {
  const { user, logout } = useAuth();
  const [suggestions, setSuggestions] = useState<UserSummary[]>([]);
  const [loaded, setLoaded] = useState(false);

  useEffect(() => {
    api.get<UserSummary[]>("/users/suggestions", { params: { limit: 5 } })
      .then((res) => setSuggestions(res.data))
      .catch(() => setSuggestions([]))
      .finally(() => setLoaded(true));
  }, []);

  if (!user) return null;

  return (
    <aside className="sticky top-8 hidden h-fit w-[310px] shrink-0 space-y-4 lg:block">
      <div className="flex items-center gap-3 rounded-2xl border border-neutral-200/80 bg-white p-4 shadow-sm dark:border-neutral-800 dark:bg-neutral-950">
        <Link to={`/profile/${user.username}`}><Avatar url={user.profileImageUrl} name={user.username} size={48} id={user.userId} /></Link>
        <div className="min-w-0 flex-1 text-sm">
          <Link to={`/profile/${user.username}`} className="block truncate font-semibold">{user.username}</Link>
          <span className="text-xs text-neutral-500">Tvoj nalog</span>
        </div>
        <button onClick={logout} className="text-xs font-semibold text-accent hover:underline">Odjavi se</button>
      </div>

      <div className="rounded-2xl border border-neutral-200/80 bg-white p-4 shadow-sm dark:border-neutral-800 dark:bg-neutral-950">
        <div className="mb-3 flex items-center justify-between text-sm">
          <span className="font-bold">Predloženo za tebe</span>
          <Link to="/search" className="text-xs font-semibold text-accent hover:underline">Vidi sve</Link>
        </div>

        <div className="space-y-3.5">
          {!loaded && [0, 1, 2].map((i) => (
            <div key={i} className="flex items-center gap-3"><Skeleton className="h-10 w-10 !rounded-full" /><Skeleton className="h-3 flex-1" /></div>
          ))}
          {loaded && suggestions.length === 0 && <p className="text-sm text-neutral-500">Nema novih predloga.</p>}
          {suggestions.map((s) => (
            <div key={s.id} className="flex items-center gap-3">
              <Link to={`/profile/${s.username}`}><Avatar url={s.profileImageUrl} name={s.username} size={40} id={s.id} /></Link>
              <div className="min-w-0 flex-1 text-sm leading-tight">
                <Link to={`/profile/${s.username}`} className="block truncate font-semibold hover:underline">{s.username}</Link>
                <span className="block truncate text-xs text-neutral-500">{s.fullName}</span>
              </div>
              <FollowButton link userId={s.id} following={s.followedByMe}
                onChange={(r) => setSuggestions((list) => list.map((x) => (x.id === s.id ? { ...x, followedByMe: r.following } : x)))} />
            </div>
          ))}
        </div>
      </div>

      <p className="px-2 text-xs text-neutral-400">© 2026 Connectly</p>
    </aside>
  );
}
