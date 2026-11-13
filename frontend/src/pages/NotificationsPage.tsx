import { useEffect, useState, type ReactNode } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Bell, CheckCheck, Heart, MessageCircle, Reply, ShieldAlert, UserPlus } from "lucide-react";
import { api, errorMessage } from "../api/client";
import { Avatar, EmptyState, ErrorText, Skeleton } from "../components/ui";
import { useNotifications } from "../context/NotificationsContext";
import { timeAgo } from "../lib/format";
import type { AppNotification, NotificationType } from "../types";

function text(n: AppNotification): string {
  switch (n.type) {
    case "LIKE": return "je lajkovao/la tvoju objavu.";
    case "COMMENT": return "je komentarisao/la tvoju objavu.";
    case "COMMENT_LIKE": return "je lajkovao/la tvoj komentar.";
    case "COMMENT_REPLY": return "je odgovorio/la na tvoj komentar.";
    case "FOLLOW": return "je počeo/la da te prati.";
    case "STORY_REPLY": return "je odgovorio/la na tvoju priču:";
    case "ACCOUNT_STATUS": return n.message ?? "Status tvog naloga je promenjen.";
  }
}

// mala ikonica tipa na uglu avatara
const BADGES: Record<NotificationType, { icon: ReactNode; color: string }> = {
  LIKE: { icon: <Heart size={11} className="fill-white" />, color: "bg-red-500" },
  COMMENT_LIKE: { icon: <Heart size={11} className="fill-white" />, color: "bg-red-500" },
  COMMENT: { icon: <MessageCircle size={11} className="fill-white" />, color: "bg-sky-500" },
  COMMENT_REPLY: { icon: <Reply size={11} />, color: "bg-sky-500" },
  FOLLOW: { icon: <UserPlus size={11} />, color: "bg-accent" },
  STORY_REPLY: { icon: <MessageCircle size={11} className="fill-white" />, color: "bg-fuchsia-500" },
  ACCOUNT_STATUS: { icon: <ShieldAlert size={11} />, color: "bg-amber-500" },
};

const DAY_MS = 86_400_000;

export default function NotificationsPage() {
  const navigate = useNavigate();
  const { refresh } = useNotifications();
  const [items, setItems] = useState<AppNotification[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api.get<AppNotification[]>("/notifications", { params: { size: 50 } })
      .then((res) => setItems(res.data))
      .catch((err) => setError(errorMessage(err)))
      .finally(() => setLoading(false));
  }, []);

  async function markAll() {
    try {
      await api.patch("/notifications/read-all");
      setItems((list) => list.map((n) => ({ ...n, read: true })));
      refresh();
    } catch (err) {
      setError(errorMessage(err));
    }
  }

  async function open(n: AppNotification) {
    if (!n.read) {
      api.patch(`/notifications/${n.id}/read`).then(refresh).catch(() => undefined);
      setItems((list) => list.map((x) => (x.id === n.id ? { ...x, read: true } : x)));
    }
    if (n.postId) navigate(`/post/${n.postId}`);
    else if (n.actorUsername) navigate(`/profile/${n.actorUsername}`);
  }

  const hasUnread = items.some((n) => !n.read);
  const now = Date.now();
  const recent = items.filter((n) => now - new Date(n.createdAt).getTime() < DAY_MS);
  const older = items.filter((n) => now - new Date(n.createdAt).getTime() >= DAY_MS);

  const row = (n: AppNotification) => {
    const system = n.type === "ACCOUNT_STATUS" || !n.actorUsername;
    const badge = BADGES[n.type];
    return (
      <button key={n.id} onClick={() => open(n)}
        className={`flex w-full items-center gap-3 rounded-2xl px-3 py-3 text-left transition hover:bg-neutral-100 dark:hover:bg-neutral-900 ${n.read ? "" : "bg-accent-soft/70 dark:bg-indigo-950/30"}`}>
        <span className="relative shrink-0">
          {system ? (
            <span className="flex h-11 w-11 items-center justify-center rounded-full bg-amber-100 text-amber-600"><ShieldAlert size={22} /></span>
          ) : (
            <Avatar url={n.actorProfileImageUrl} name={n.actorUsername ?? "?"} size={44} id={n.actorId ?? 0} />
          )}
          {!system && (
            <span className={`absolute -bottom-0.5 -right-0.5 flex h-[20px] w-[20px] items-center justify-center rounded-full text-white ring-2 ring-white dark:ring-neutral-950 ${badge.color}`}>
              {badge.icon}
            </span>
          )}
        </span>
        <div className="min-w-0 flex-1 text-sm">
          <p>
            {n.type !== "ACCOUNT_STATUS" && n.actorUsername && (
              <Link to={`/profile/${n.actorUsername}`} onClick={(e) => e.stopPropagation()} className="mr-1 font-semibold hover:underline">{n.actorUsername}</Link>
            )}
            {text(n)} <span className="text-neutral-500">{timeAgo(n.createdAt)}</span>
          </p>
          {n.type === "STORY_REPLY" && n.message && (
            <p className="mt-1 inline-block max-w-full break-words rounded-2xl rounded-tl-md bg-neutral-100 px-3 py-1.5 text-sm dark:bg-neutral-800">{n.message}</p>
          )}
        </div>
        {!n.read && <span className="h-2.5 w-2.5 shrink-0 rounded-full bg-accent" />}
      </button>
    );
  };

  const section = (title: string, list: AppNotification[]) =>
    list.length > 0 && (
      <section className="mb-4">
        <h2 className="mb-1 px-3 text-sm font-bold">{title}</h2>
        <div className="space-y-0.5">{list.map(row)}</div>
      </section>
    );

  return (
    <div className="mx-auto max-w-xl px-4 pt-6 md:pt-10">
      <div className="mb-4 flex items-center justify-between">
        <h1 className="text-2xl font-bold tracking-tight">Obaveštenja</h1>
        {hasUnread && (
          <button onClick={markAll} className="flex items-center gap-1.5 rounded-full px-3 py-1.5 text-sm font-semibold text-accent hover:bg-accent-soft dark:hover:bg-indigo-950/40">
            <CheckCheck size={16} /> Označi sve kao pročitano
          </button>
        )}
      </div>
      {error && <ErrorText>{error}</ErrorText>}
      {loading && (
        <div className="space-y-4 px-3">
          {[0, 1, 2, 3].map((i) => (
            <div key={i} className="flex items-center gap-3"><Skeleton className="h-11 w-11 !rounded-full" /><Skeleton className="h-3 flex-1" /></div>
          ))}
        </div>
      )}
      {!loading && items.length === 0 && !error && <EmptyState icon={<Bell size={24} />}>Nema obaveštenja. Kad neko lajkuje, komentariše ili odgovori na tvoju priču, videćeš to ovde.</EmptyState>}
      {section("Poslednja 24 sata", recent)}
      {section("Ranije", older)}
    </div>
  );
}
