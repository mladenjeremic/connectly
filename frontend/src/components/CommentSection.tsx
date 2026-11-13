import { Fragment, useEffect, useRef, useState, type FormEvent } from "react";
import { Link } from "react-router-dom";
import { ChevronDown, ChevronUp, Flag, Heart, Send, Trash2, X } from "lucide-react";
import { toast } from "sonner";
import { api, errorMessage } from "../api/client";
import { timeAgo } from "../lib/format";
import type { Comment, LikeResult } from "../types";
import ReportDialog from "./ReportDialog";
import { Avatar, Spinner } from "./ui";

interface Props {
  postId: number;
  onCountChange: (delta: number) => void;
  /** komentar je moguće prijaviti samo ako nije moj */
  currentUserId: number;
}

interface ReplyTarget {
  /** komentar na koji se odgovara (autor dobija obaveštenje) */
  id: number;
  /** koren niti u koji odgovor ulazi */
  rootId: number;
  username: string;
}

/** Tekst komentara: @korisnik se prikazuje kao link ka profilu. */
function CommentText({ text }: { text: string }) {
  const parts = text.split(/(@[A-Za-z0-9_.]+)/g);
  return (
    <>
      {parts.map((part, i) =>
        /^@[A-Za-z0-9_.]+$/.test(part) ? (
          <Link key={i} to={`/profile/${part.slice(1)}`} className="font-medium text-accent hover:underline">{part}</Link>
        ) : (
          <Fragment key={i}>{part}</Fragment>
        )
      )}
    </>
  );
}

interface ItemProps {
  c: Comment;
  currentUserId: number;
  small?: boolean;
  onLike: (c: Comment) => void;
  onReply: (c: Comment) => void;
  onDelete: (c: Comment) => void;
  onReport: (id: number) => void;
}

function CommentItem({ c, currentUserId, small = false, onLike, onReply, onDelete, onReport }: ItemProps) {
  return (
    <div className="group flex items-start gap-2.5">
      <Link to={`/profile/${c.authorUsername}`}>
        <Avatar url={c.authorProfileImageUrl} name={c.authorUsername} size={small ? 24 : 30} id={c.authorId} />
      </Link>
      <div className="min-w-0 flex-1 text-sm">
        <p className="break-words">
          <Link to={`/profile/${c.authorUsername}`} className="mr-1.5 font-semibold hover:underline">{c.authorUsername}</Link>
          <CommentText text={c.content} />
        </p>
        <div className="mt-0.5 flex items-center gap-3 text-xs text-neutral-500">
          <span>{timeAgo(c.createdAt)}</span>
          {c.likeCount > 0 && <span className="font-semibold">{c.likeCount} {c.likeCount === 1 ? "sviđanje" : "sviđanja"}</span>}
          <button className="font-semibold hover:text-neutral-800 dark:hover:text-neutral-200" onClick={() => onReply(c)}>Odgovori</button>
          <span className="flex gap-1 opacity-0 transition group-hover:opacity-100 focus-within:opacity-100">
            {c.authorId !== currentUserId && (
              <button title="Prijavi komentar" aria-label="Prijavi komentar" onClick={() => onReport(c.id)} className="hover:text-red-500"><Flag size={13} /></button>
            )}
            {c.canDelete && (
              <button title="Obriši komentar" aria-label="Obriši komentar" onClick={() => onDelete(c)} className="hover:text-red-500"><Trash2 size={13} /></button>
            )}
          </span>
        </div>
      </div>
      <button aria-label={c.likedByMe ? "Ukloni sviđanje komentara" : "Sviđa mi se komentar"} onClick={() => onLike(c)}
        className="mt-0.5 shrink-0 p-1 text-neutral-400 transition active:scale-90">
        <Heart size={14} className={c.likedByMe ? "animate-like-bump fill-red-500 text-red-500" : "hover:text-neutral-700 dark:hover:text-neutral-200"} />
      </button>
    </div>
  );
}

export default function CommentSection({ postId, onCountChange, currentUserId }: Props) {
  const [comments, setComments] = useState<Comment[]>([]);
  const [replies, setReplies] = useState<Record<number, Comment[]>>({});
  const [open, setOpen] = useState<Set<number>>(new Set());
  const [loadingReplies, setLoadingReplies] = useState<number | null>(null);
  const [replyTo, setReplyTo] = useState<ReplyTarget | null>(null);
  const [text, setText] = useState("");
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [reporting, setReporting] = useState<number | null>(null);
  const input = useRef<HTMLInputElement>(null);

  useEffect(() => {
    api.get<Comment[]>(`/posts/${postId}/comments`)
      .then((res) => setComments(res.data))
      .catch((err) => toast.error(errorMessage(err)))
      .finally(() => setLoading(false));
  }, [postId]);

  /** Menja jedan komentar bez obzira da li je prvog nivoa ili odgovor. */
  function patch(id: number, fn: (c: Comment) => Comment) {
    setComments((list) => list.map((c) => (c.id === id ? fn(c) : c)));
    setReplies((map) => {
      const next: Record<number, Comment[]> = {};
      for (const key of Object.keys(map)) next[Number(key)] = map[Number(key)].map((c) => (c.id === id ? fn(c) : c));
      return next;
    });
  }

  async function toggleLike(c: Comment) {
    const wasLiked = c.likedByMe;
    patch(c.id, (x) => ({ ...x, likedByMe: !wasLiked, likeCount: x.likeCount + (wasLiked ? -1 : 1) }));   // odmah u UI-ju
    try {
      const { data } = wasLiked
        ? await api.delete<LikeResult>(`/comments/${c.id}/likes`)
        : await api.post<LikeResult>(`/comments/${c.id}/likes`);
      patch(c.id, (x) => ({ ...x, likedByMe: data.liked, likeCount: data.likeCount }));
    } catch (err) {
      patch(c.id, (x) => ({ ...x, likedByMe: wasLiked, likeCount: c.likeCount }));
      toast.error(errorMessage(err));
    }
  }

  async function loadReplies(rootId: number) {
    setLoadingReplies(rootId);
    try {
      const { data } = await api.get<Comment[]>(`/comments/${rootId}/replies`);
      setReplies((m) => ({ ...m, [rootId]: data }));
      setOpen((s) => new Set(s).add(rootId));
    } catch (err) {
      toast.error(errorMessage(err));
    } finally {
      setLoadingReplies(null);
    }
  }

  function toggleReplies(rootId: number) {
    if (open.has(rootId)) {
      setOpen((s) => { const n = new Set(s); n.delete(rootId); return n; });
    } else if (replies[rootId]) {
      setOpen((s) => new Set(s).add(rootId));
    } else {
      loadReplies(rootId);
    }
  }

  function startReply(c: Comment) {
    const rootId = c.parentId ?? c.id;
    setReplyTo({ id: c.id, rootId, username: c.authorUsername });
    setText(c.authorId === currentUserId ? "" : `@${c.authorUsername} `);
    input.current?.focus();
  }

  function cancelReply() {
    setReplyTo(null);
    setText("");
  }

  async function submit(e: FormEvent) {
    e.preventDefault();
    if (!text.trim()) return;
    setBusy(true);
    try {
      const { data } = await api.post<Comment>(`/posts/${postId}/comments`, {
        content: text,
        parentId: replyTo?.id ?? null,
      });
      onCountChange(1);
      if (data.parentId === null) {
        setComments((prev) => [...prev, data]);
      } else {
        const rootId = data.parentId;
        setComments((list) => list.map((c) => (c.id === rootId ? { ...c, replyCount: c.replyCount + 1 } : c)));
        if (replies[rootId]) {
          setReplies((m) => ({ ...m, [rootId]: [...m[rootId], data] }));
          setOpen((s) => new Set(s).add(rootId));
        } else {
          await loadReplies(rootId);   // niz još nije učitan, pa ga učitavamo sa novim odgovorom
        }
      }
      setText("");
      setReplyTo(null);
    } catch (err) {
      toast.error(errorMessage(err));
    } finally {
      setBusy(false);
    }
  }

  async function remove(c: Comment) {
    try {
      await api.delete(`/comments/${c.id}`);
      if (c.parentId === null) {
        // brisanjem komentara nestaju i njegovi odgovori
        const gone = 1 + c.replyCount;
        setComments((prev) => prev.filter((x) => x.id !== c.id));
        setReplies((m) => { const n = { ...m }; delete n[c.id]; return n; });
        onCountChange(-gone);
      } else {
        const rootId = c.parentId;
        setReplies((m) => ({ ...m, [rootId]: (m[rootId] ?? []).filter((x) => x.id !== c.id) }));
        setComments((list) => list.map((x) => (x.id === rootId ? { ...x, replyCount: Math.max(0, x.replyCount - 1) } : x)));
        onCountChange(-1);
      }
      if (replyTo && (replyTo.id === c.id || replyTo.rootId === c.id)) cancelReply();
    } catch (err) {
      toast.error(errorMessage(err));
    }
  }

  const itemProps = { currentUserId, onLike: toggleLike, onReply: startReply, onDelete: remove, onReport: setReporting };

  return (
    <div className="space-y-3.5 pt-3">
      {loading && <Spinner className="py-2" />}
      {!loading && comments.length === 0 && (
        <p className="py-2 text-center text-sm text-neutral-500">Još nema komentara. Budi prvi!</p>
      )}
      {comments.map((c) => (
        <div key={c.id} className="space-y-2.5">
          <CommentItem c={c} {...itemProps} />
          {c.replyCount > 0 && (
            <div className="ml-10">
              <button onClick={() => toggleReplies(c.id)} className="flex items-center gap-2 text-xs font-semibold text-neutral-500 hover:text-neutral-800 dark:hover:text-neutral-200">
                <span className="h-px w-6 bg-neutral-300 dark:bg-neutral-700" />
                {open.has(c.id)
                  ? <>Sakrij odgovore <ChevronUp size={13} /></>
                  : <>Prikaži odgovore ({c.replyCount}) <ChevronDown size={13} /></>}
              </button>
              {loadingReplies === c.id && <Spinner className="!py-2" />}
              {open.has(c.id) && (
                <div className="mt-3 space-y-3">
                  {(replies[c.id] ?? []).map((r) => <CommentItem key={r.id} c={r} small {...itemProps} />)}
                </div>
              )}
            </div>
          )}
        </div>
      ))}

      <div className="border-t border-neutral-200 pt-2.5 dark:border-neutral-800">
        {replyTo && (
          <div className="mb-2 flex items-center justify-between rounded-lg bg-accent-soft px-3 py-1.5 text-xs text-accent dark:bg-indigo-950/40">
            <span>Odgovaraš korisniku <strong>{replyTo.username}</strong></span>
            <button aria-label="Otkaži odgovor" onClick={cancelReply}><X size={14} /></button>
          </div>
        )}
        <form className="flex items-center gap-2" onSubmit={submit}>
          <input
            ref={input}
            className="min-w-0 flex-1 rounded-full bg-neutral-100 px-4 py-2 text-sm outline-none placeholder:text-neutral-500 focus:ring-2 focus:ring-indigo-500/30 dark:bg-neutral-900"
            placeholder={replyTo ? "Napiši odgovor..." : "Dodaj komentar..."} value={text} maxLength={1000}
            onChange={(e) => setText(e.target.value)}
          />
          <button disabled={busy || !text.trim()} aria-label="Pošalji" className="rounded-full p-2 text-accent transition hover:bg-accent-soft disabled:opacity-40 dark:hover:bg-indigo-950/40">
            <Send size={18} />
          </button>
        </form>
      </div>
      {reporting !== null && <ReportDialog targetType="COMMENT" targetId={reporting} onClose={() => setReporting(null)} />}
    </div>
  );
}
