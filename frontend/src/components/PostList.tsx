import { useCallback, useEffect, useRef, useState, type ReactNode } from "react";
import { api, errorMessage } from "../api/client";
import type { Post } from "../types";
import PostCard from "./PostCard";
import { EmptyState, ErrorText, PostSkeleton } from "./ui";

const PAGE_SIZE = 10;

interface Props {
  endpoint: string;
  emptyText: string;
  emptyIcon?: ReactNode;
  reloadKey?: number;
  /** na stranici „Sačuvano“ objava nestaje iz liste čim se ukloni iz sačuvanih */
  removeWhenUnsaved?: boolean;
}

export default function PostList({ endpoint, emptyText, emptyIcon, reloadKey = 0, removeWhenUnsaved = false }: Props) {
  const [posts, setPosts] = useState<Post[]>([]);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async (p: number) => {
    setLoading(true);
    setError(null);
    try {
      const { data } = await api.get<Post[]>(endpoint, { params: { page: p, size: PAGE_SIZE } });
      setPosts((prev) => (p === 0 ? data : [...prev, ...data]));
      setHasMore(data.length === PAGE_SIZE);
      setPage(p);
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [endpoint]);

  useEffect(() => {
    load(0);
  }, [load, reloadKey]);

  // beskonačno skrolovanje: kad se sentinel približi ekranu, učitaj sledeću stranu
  const sentinel = useRef<HTMLDivElement>(null);
  const busy = useRef(false);
  busy.current = loading;
  useEffect(() => {
    const el = sentinel.current;
    if (!el || !hasMore || error) return;
    const io = new IntersectionObserver((entries) => {
      if (entries[0].isIntersecting && !busy.current) load(page + 1);
    }, { rootMargin: "400px" });
    io.observe(el);
    return () => io.disconnect();
  }, [hasMore, page, load, error, posts.length]);

  const drop = (id: number) => setPosts((prev) => prev.filter((x) => x.id !== id));

  return (
    <div>
      {error && <ErrorText>{error}</ErrorText>}
      {!loading && !error && posts.length === 0 && <EmptyState icon={emptyIcon}>{emptyText}</EmptyState>}
      {posts.map((p) => (
        <PostCard key={p.id} post={p} onDeleted={drop} onUnsaved={removeWhenUnsaved ? drop : undefined} />
      ))}
      {loading && posts.length === 0 && <><PostSkeleton /><PostSkeleton /></>}
      {loading && posts.length > 0 && <PostSkeleton />}
      <div ref={sentinel} aria-hidden />
      {!loading && hasMore && posts.length > 0 && (
        <button className="mb-6 w-full rounded-xl py-3 text-sm font-semibold text-accent hover:bg-accent-soft dark:hover:bg-indigo-950/40" onClick={() => load(page + 1)}>Učitaj još</button>
      )}
    </div>
  );
}
