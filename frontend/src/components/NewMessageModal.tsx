import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Search } from "lucide-react";
import { api, errorMessage } from "../api/client";
import { inputCls } from "../lib/styles";
import type { UserSummary } from "../types";
import { Avatar, EmptyState, ErrorText, Modal, Spinner } from "./ui";

/** Pretraga korisnika za početak novog razgovora. */
export default function NewMessageModal({ onClose }: { onClose: () => void }) {
  const navigate = useNavigate();
  const [q, setQ] = useState("");
  const [results, setResults] = useState<UserSummary[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const term = q.trim();
    if (!term) return;
    const t = setTimeout(() => {
      setLoading(true);
      api.get<UserSummary[]>("/users/search", { params: { q: term } })
        .then((res) => { setResults(res.data.filter((u) => !u.self)); setError(null); })
        .catch((err) => setError(errorMessage(err)))
        .finally(() => setLoading(false));
    }, 300);
    return () => clearTimeout(t);
  }, [q]);

  const shown = q.trim() ? results : [];

  return (
    <Modal title="Nova poruka" onClose={onClose}>
      <div className="space-y-3 p-4">
        <div className="relative">
          <Search size={18} className="absolute left-3 top-1/2 -translate-y-1/2 text-neutral-400" />
          <input className={`${inputCls} pl-10`} placeholder="Traži korisnika..." value={q} autoFocus onChange={(e) => setQ(e.target.value)} />
        </div>
        {error && <ErrorText>{error}</ErrorText>}
        {loading && <Spinner />}
        {!loading && q.trim() && shown.length === 0 && !error && <EmptyState>Nema rezultata.</EmptyState>}
        <div className="space-y-1">
          {shown.map((u) => (
            <button key={u.id} onClick={() => { onClose(); navigate(`/messages/${u.username}`); }}
              className="flex w-full items-center gap-3 rounded-xl p-2 text-left hover:bg-neutral-100 dark:hover:bg-neutral-800">
              <Avatar url={u.profileImageUrl} name={u.username} size={42} id={u.id} />
              <span className="min-w-0 text-sm leading-tight">
                <span className="block truncate font-semibold">{u.username}</span>
                <span className="block truncate text-neutral-500">{u.fullName}</span>
              </span>
            </button>
          ))}
        </div>
      </div>
    </Modal>
  );
}
