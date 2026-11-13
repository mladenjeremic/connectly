import { useCallback, useEffect, useState, type FormEvent } from "react";
import { Link } from "react-router-dom";
import { toast } from "sonner";
import { api, errorMessage } from "../../api/client";
import { EmptyState, ErrorText, Modal, Spinner } from "../../components/ui";
import { gradientFor, timeAgo } from "../../lib/format";
import { btnDanger, btnSecondary, cardCls, inputCls } from "../../lib/styles";
import type { Post } from "../../types";

const PAGE_SIZE = 20;

function RemoveModal({ post, onClose, onRemoved }: { post: Post; onClose: () => void; onRemoved: () => void }) {
  const [reason, setReason] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function submit(e: FormEvent) {
    e.preventDefault();
    if (!reason.trim()) { setError("Razlog je obavezan."); return; }
    setBusy(true);
    try {
      await api.delete(`/admin/posts/${post.id}`, { data: { reason } });
      toast.success("Objava je uklonjena i zabeležena u audit log.");
      onRemoved();
      onClose();
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setBusy(false);
    }
  }

  return (
    <Modal title={`Ukloni objavu #${post.id}`} onClose={onClose}>
      <form className="space-y-3 p-4" onSubmit={submit}>
        <textarea className={inputCls} rows={3} maxLength={500} placeholder="Razlog uklanjanja (obavezno)" value={reason} onChange={(e) => setReason(e.target.value)} />
        {error && <ErrorText>{error}</ErrorText>}
        <div className="flex justify-end gap-2">
          <button type="button" className={btnSecondary} onClick={onClose}>Otkaži</button>
          <button className={btnDanger} disabled={busy}>{busy ? "Sačekaj..." : "Ukloni"}</button>
        </div>
      </form>
    </Modal>
  );
}

export default function AdminPostsPage() {
  const [posts, setPosts] = useState<Post[]>([]);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [removing, setRemoving] = useState<Post | null>(null);

  const load = useCallback(async (p: number) => {
    setLoading(true);
    try {
      const { data } = await api.get<Post[]>("/admin/posts", { params: { page: p, size: PAGE_SIZE } });
      setPosts((prev) => (p === 0 ? data : [...prev, ...data]));
      setHasMore(data.length === PAGE_SIZE);
      setPage(p);
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { load(0); }, [load]);

  return (
    <div className="space-y-4">
      {error && <ErrorText>{error}</ErrorText>}
      {!loading && posts.length === 0 && <EmptyState>Nema objava.</EmptyState>}
      <div className={`${cardCls} divide-y divide-neutral-100 dark:divide-neutral-900`}>
        {posts.map((p) => (
          <div key={p.id} className="flex items-center gap-4 p-3">
            {p.images.length > 0 ? (
              <img src={p.images[0].url} alt="" className="h-14 w-14 shrink-0 rounded object-cover" />
            ) : (
              <div className={`h-14 w-14 shrink-0 rounded bg-gradient-to-br ${gradientFor(p.id)}`} />
            )}
            <div className="min-w-0 flex-1 text-sm">
              <p className="truncate"><Link to={`/profile/${p.authorUsername}`} className="font-semibold hover:underline">@{p.authorUsername}</Link> · <span className="text-neutral-500">{timeAgo(p.createdAt)}</span></p>
              <p className="truncate text-neutral-600 dark:text-neutral-300">{p.content || "(samo fotografije)"}</p>
              <p className="text-xs text-neutral-500">{p.likeCount} sviđanja · {p.commentCount} komentara · {p.images.length} fotografija</p>
            </div>
            <Link to={`/post/${p.id}`} className={btnSecondary}>Otvori</Link>
            <button className={btnDanger} onClick={() => setRemoving(p)}>Ukloni</button>
          </div>
        ))}
      </div>
      {loading && <Spinner />}
      {hasMore && !loading && <button className={btnSecondary} onClick={() => load(page + 1)}>Učitaj još</button>}
      {removing && (
        <RemoveModal post={removing} onClose={() => setRemoving(null)}
          onRemoved={() => setPosts((list) => list.filter((x) => x.id !== removing.id))} />
      )}
    </div>
  );
}
