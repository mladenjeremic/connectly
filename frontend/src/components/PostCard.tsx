import { useRef, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Bookmark, Ellipsis, Flag, Heart, Link2, MessageCircle, Pencil, Trash2 } from "lucide-react";
import { toast } from "sonner";
import { api, errorMessage } from "../api/client";
import { useAuth } from "../auth/AuthContext";
import { timeAgo } from "../lib/format";
import type { LikeResult, Post, SaveResult } from "../types";
import CommentSection from "./CommentSection";
import EditPostModal from "./EditPostModal";
import PostMedia from "./PostMedia";
import ReportDialog from "./ReportDialog";
import { Avatar, ConfirmDialog } from "./ui";

interface Props {
  post: Post;
  onDeleted?: (id: number) => void;
  /** na stranici objave komentari su uvek otvoreni */
  expanded?: boolean;
  /** pozvano kad se objava ukloni iz sačuvanih (koristi je stranica „Sačuvano“) */
  onUnsaved?: (id: number) => void;
}

export default function PostCard({ post: initial, onDeleted, expanded = false, onUnsaved }: Props) {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [post, setPost] = useState(initial);
  const [liked, setLiked] = useState(initial.likedByMe);
  const [saved, setSaved] = useState(initial.savedByMe);
  const [likeCount, setLikeCount] = useState(initial.likeCount);
  const [commentCount, setCommentCount] = useState(initial.commentCount);
  const [showComments, setShowComments] = useState(expanded);
  const [menu, setMenu] = useState(false);
  const [editing, setEditing] = useState(false);
  const [reporting, setReporting] = useState(false);
  const [confirmDelete, setConfirmDelete] = useState(false);
  const [busy, setBusy] = useState(false);
  const [popKey, setPopKey] = useState(0);      // veliko srce preko slike (dupli klik)
  const [bumpKey, setBumpKey] = useState(0);    // animacija ikone srca
  const likeBusy = useRef(false);

  async function setLike(next: boolean) {
    if (likeBusy.current) return;
    if (next) { setPopKey((k) => k + 1); setBumpKey((k) => k + 1); }
    if (next === liked) return;
    likeBusy.current = true;
    // odmah u UI-ju, pa usklađivanje sa serverom
    setLiked(next);
    setLikeCount((c) => c + (next ? 1 : -1));
    try {
      const { data } = next
        ? await api.post<LikeResult>(`/posts/${post.id}/likes`)
        : await api.delete<LikeResult>(`/posts/${post.id}/likes`);
      setLiked(data.liked);
      setLikeCount(data.likeCount);
    } catch (err) {
      setLiked(!next);
      setLikeCount((c) => c + (next ? -1 : 1));
      toast.error(errorMessage(err));
    } finally {
      likeBusy.current = false;
    }
  }

  async function toggleSave() {
    const next = !saved;
    setSaved(next);
    try {
      const { data } = next
        ? await api.post<SaveResult>(`/posts/${post.id}/bookmark`)
        : await api.delete<SaveResult>(`/posts/${post.id}/bookmark`);
      setSaved(data.saved);
      if (next) toast.success("Objava je sačuvana.");
      else onUnsaved?.(post.id);
    } catch (err) {
      setSaved(!next);
      toast.error(errorMessage(err));
    }
  }

  async function copyLink() {
    setMenu(false);
    try {
      await navigator.clipboard.writeText(`${window.location.origin}/post/${post.id}`);
      toast.success("Link je kopiran.");
    } catch {
      toast.error("Kopiranje nije uspelo.");
    }
  }

  async function remove() {
    setBusy(true);
    try {
      await api.delete(`/posts/${post.id}`);
      toast.success("Objava je obrisana.");
      setConfirmDelete(false);
      if (onDeleted) onDeleted(post.id);
      else navigate("/feed");
    } catch (err) {
      toast.error(errorMessage(err));
      setBusy(false);
    }
  }

  const hasMedia = post.images.length > 0;
  const menuItem = "flex w-full items-center gap-2.5 px-3.5 py-2.5 text-left hover:bg-neutral-100 dark:hover:bg-neutral-800";

  return (
    <article className="mb-5 rounded-2xl border border-neutral-200/80 bg-white p-3 shadow-sm dark:border-neutral-800 dark:bg-neutral-950 sm:p-4">
      <header className="flex items-center gap-3 pb-3">
        <Link to={`/profile/${post.authorUsername}`} className="rounded-full">
          <Avatar url={post.authorProfileImageUrl} name={post.authorUsername} size={38} id={post.authorId} />
        </Link>
        <div className="min-w-0 flex-1 text-sm leading-tight">
          <Link to={`/profile/${post.authorUsername}`} className="block truncate font-semibold hover:underline">{post.authorUsername}</Link>
          <span className="text-xs text-neutral-500">
            <Link to={`/post/${post.id}`} className="hover:underline">{timeAgo(post.createdAt)}</Link>
            {post.updatedAt !== post.createdAt && <span className="ml-1 text-neutral-400">· izmenjeno</span>}
          </span>
        </div>
        <div className="relative">
          <button aria-label="Opcije" onClick={() => setMenu((m) => !m)} className="rounded-full p-1.5 hover:bg-neutral-100 dark:hover:bg-neutral-900"><Ellipsis size={20} /></button>
          {menu && (
            <>
              <div className="fixed inset-0 z-10" onClick={() => setMenu(false)} />
              <div className="animate-slide-up absolute right-0 z-20 mt-1 w-48 overflow-hidden rounded-2xl border border-neutral-200 bg-white text-sm shadow-xl dark:border-neutral-700 dark:bg-neutral-900">
                <button className={menuItem} onClick={copyLink}><Link2 size={15} /> Kopiraj link</button>
                {post.ownPost ? (
                  <>
                    <button className={menuItem} onClick={() => { setMenu(false); setEditing(true); }}><Pencil size={15} /> Izmeni</button>
                    <button className={`${menuItem} text-red-600`} onClick={() => { setMenu(false); setConfirmDelete(true); }}><Trash2 size={15} /> Obriši</button>
                  </>
                ) : (
                  <button className={`${menuItem} text-red-600`} onClick={() => { setMenu(false); setReporting(true); }}><Flag size={15} /> Prijavi</button>
                )}
              </div>
            </>
          )}
        </div>
      </header>

      <div className="relative overflow-hidden rounded-xl">
        <PostMedia post={post} onDoubleClick={() => setLike(true)} />
        {popKey > 0 && (
          <div key={popKey} className="pointer-events-none absolute inset-0 flex items-center justify-center">
            <Heart size={96} className="animate-heart-pop fill-white text-white drop-shadow-[0_4px_16px_rgba(0,0,0,0.45)]" />
          </div>
        )}
      </div>

      <div className="flex items-center gap-1 pb-1 pt-3">
        <button aria-label="Sviđa mi se" onClick={() => setLike(!liked)}
          className="rounded-full p-1.5 transition hover:bg-neutral-100 active:scale-90 dark:hover:bg-neutral-900">
          <Heart key={bumpKey} size={25} className={liked ? "animate-like-bump fill-red-500 text-red-500" : ""} />
        </button>
        <button aria-label="Komentari" onClick={() => setShowComments((s) => !s)}
          className="flex items-center gap-1.5 rounded-full p-1.5 transition hover:bg-neutral-100 dark:hover:bg-neutral-900">
          <MessageCircle size={25} />
        </button>
        <button aria-label="Kopiraj link" onClick={copyLink}
          className="rounded-full p-1.5 transition hover:bg-neutral-100 dark:hover:bg-neutral-900">
          <Link2 size={24} />
        </button>
        <button aria-label={saved ? "Ukloni iz sačuvanih" : "Sačuvaj"} onClick={toggleSave}
          className="ml-auto rounded-full p-1.5 transition hover:bg-neutral-100 active:scale-90 dark:hover:bg-neutral-900">
          <Bookmark size={24} className={saved ? "fill-current" : ""} />
        </button>
      </div>

      <p className="text-sm font-semibold">{likeCount} {likeCount === 1 ? "sviđanje" : "sviđanja"}</p>
      {hasMedia && post.content && (
        <p className="mt-1 whitespace-pre-wrap break-words text-sm">
          <Link to={`/profile/${post.authorUsername}`} className="mr-1.5 font-semibold hover:underline">{post.authorUsername}</Link>
          {post.content}
        </p>
      )}
      {!expanded && (
        <button className="mt-1.5 text-sm text-neutral-500 hover:text-neutral-700 dark:hover:text-neutral-300" onClick={() => setShowComments((s) => !s)}>
          {showComments ? "Sakrij komentare" : commentCount > 0 ? `Prikaži sve komentare (${commentCount})` : "Dodaj komentar..."}
        </button>
      )}
      {showComments && user && (
        <CommentSection postId={post.id} currentUserId={user.userId} onCountChange={(d) => setCommentCount((c) => Math.max(0, c + d))} />
      )}

      {editing && <EditPostModal post={post} onClose={() => setEditing(false)} onSaved={setPost} />}
      {reporting && <ReportDialog targetType="POST" targetId={post.id} onClose={() => setReporting(false)} />}
      {confirmDelete && (
        <ConfirmDialog title="Obriši objavu?" message="Ova radnja se ne može poništiti." busy={busy}
          onConfirm={remove} onCancel={() => setConfirmDelete(false)} />
      )}
    </article>
  );
}
