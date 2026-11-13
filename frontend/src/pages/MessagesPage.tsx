import { useCallback, useEffect, useRef, useState, type FormEvent, type KeyboardEvent } from "react";
import { Link, useParams } from "react-router-dom";
import { ArrowLeft, MessageCircle, Reply, Send, SquarePen } from "lucide-react";
import { toast } from "sonner";
import { api, errorMessage } from "../api/client";
import NewMessageModal from "../components/NewMessageModal";
import { Avatar, EmptyState, ErrorText, Skeleton, Spinner } from "../components/ui";
import { useMessages } from "../context/MessagesContext";
import { timeAgo } from "../lib/format";
import { startPolling } from "../lib/poll";
import type { ChatMessage, Conversation, Profile } from "../types";

const THREAD_POLL_MS = 4000;
const LIST_POLL_MS = 10000;

function dayLabel(iso: string): string {
  const d = new Date(iso);
  const today = new Date();
  const yesterday = new Date();
  yesterday.setDate(today.getDate() - 1);
  if (d.toDateString() === today.toDateString()) return "Danas";
  if (d.toDateString() === yesterday.toDateString()) return "Juče";
  return d.toLocaleDateString("sr-RS");
}

function clock(iso: string): string {
  return new Date(iso).toLocaleTimeString("sr-RS", { hour: "2-digit", minute: "2-digit" });
}

/* ---------------------------- lista razgovora ---------------------------- */

function ConversationList({ active, version }: { active?: string; version: number }) {
  const [items, setItems] = useState<Conversation[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    const load = () => api.get<Conversation[]>("/messages/conversations", { params: { size: 50 } })
      .then((res) => { if (!cancelled) { setItems(res.data); setError(null); } })
      .catch((err) => { if (!cancelled) setError(errorMessage(err)); });
    load();
    const stop = startPolling(load, LIST_POLL_MS);
    return () => { cancelled = true; stop(); };
  }, [version]);

  if (error && !items) return <div className="p-3"><ErrorText>{error}</ErrorText></div>;
  if (!items) {
    return (
      <div className="space-y-4 p-4">
        {[0, 1, 2, 3].map((i) => (
          <div key={i} className="flex items-center gap-3"><Skeleton className="h-12 w-12 !rounded-full" /><Skeleton className="h-3 flex-1" /></div>
        ))}
      </div>
    );
  }
  if (items.length === 0) {
    return <EmptyState icon={<MessageCircle size={24} />}>Još nemaš razgovora. Dodirni olovku gore da započneš novi.</EmptyState>;
  }

  return (
    <div className="space-y-0.5 p-2">
      {items.map((c) => (
        <Link key={c.id} to={`/messages/${c.user.username}`}
          className={`flex items-center gap-3 rounded-2xl px-3 py-2.5 transition hover:bg-neutral-100 dark:hover:bg-neutral-900 ${active === c.user.username ? "bg-neutral-100 dark:bg-neutral-900" : ""}`}>
          <Avatar url={c.user.profileImageUrl} name={c.user.username} size={48} id={c.user.id} />
          <div className="min-w-0 flex-1 text-sm leading-tight">
            <div className="flex items-baseline justify-between gap-2">
              <span className={`truncate ${c.unreadCount > 0 ? "font-bold" : "font-semibold"}`}>{c.user.username}</span>
              <span className="shrink-0 text-[11px] text-neutral-400">{timeAgo(c.lastMessageAt)}</span>
            </div>
            <p className={`mt-0.5 truncate ${c.unreadCount > 0 ? "font-semibold text-neutral-900 dark:text-white" : "text-neutral-500"}`}>
              {c.lastMessageMine ? "Ti: " : ""}{c.lastMessage}
            </p>
          </div>
          {c.unreadCount > 0 && (
            <span className="flex h-5 min-w-5 items-center justify-center rounded-full bg-accent px-1.5 text-[11px] font-bold text-white">
              {c.unreadCount > 99 ? "99+" : c.unreadCount}
            </span>
          )}
        </Link>
      ))}
    </div>
  );
}

/* --------------------------------- razgovor -------------------------------- */

function ChatThread({ username, onChanged }: { username: string; onChanged: () => void }) {
  const { refresh } = useMessages();
  const [peer, setPeer] = useState<Profile | null>(null);
  const [msgs, setMsgs] = useState<ChatMessage[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [text, setText] = useState("");
  const [sending, setSending] = useState(false);
  const bottom = useRef<HTMLDivElement>(null);
  const stickToBottom = useRef(true);
  const peerId = peer?.id;

  useEffect(() => {
    api.get<Profile>(`/users/${username}`)
      .then((res) => setPeer(res.data))
      .catch((err) => { setError(errorMessage(err)); setLoading(false); });
  }, [username]);

  const load = useCallback(async () => {
    if (peerId === undefined) return;
    try {
      const { data } = await api.get<ChatMessage[]>(`/messages/with/${peerId}`, { params: { size: 100 } });
      setMsgs(data);
      setError(null);
      if (data.some((m) => !m.mine && !m.read)) {
        await api.patch(`/messages/with/${peerId}/read`);
        refresh();
        onChanged();
      }
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [peerId, refresh, onChanged]);

  useEffect(() => {
    load();
    return startPolling(load, THREAD_POLL_MS);
  }, [load]);

  useEffect(() => {
    if (stickToBottom.current) bottom.current?.scrollIntoView({ block: "end" });
  }, [msgs]);

  async function send(e?: FormEvent) {
    e?.preventDefault();
    const content = text.trim();
    if (!content || sending || peerId === undefined) return;
    setSending(true);
    setText("");
    stickToBottom.current = true;
    const temp: ChatMessage = {
      id: -Date.now(), senderId: 0, content, mine: true, storyReply: false, createdAt: new Date().toISOString(), read: false,
    };
    setMsgs((list) => [...list, temp]);   // odmah prikaži, pa uskladi sa serverom
    try {
      await api.post<ChatMessage>(`/messages/with/${peerId}`, { content });
      await load();
      onChanged();
    } catch (err) {
      setMsgs((list) => list.filter((m) => m.id !== temp.id));
      setText(content);
      toast.error(errorMessage(err));
    } finally {
      setSending(false);
    }
  }

  function onKey(e: KeyboardEvent<HTMLTextAreaElement>) {
    if (e.key === "Enter" && !e.shiftKey) { e.preventDefault(); send(); }
  }

  const onScroll = (e: { currentTarget: HTMLElement }) => {
    const el = e.currentTarget;
    stickToBottom.current = el.scrollHeight - el.scrollTop - el.clientHeight < 80;
  };

  return (
    <div className="flex h-full min-h-0 flex-col">
      <header className="flex items-center gap-3 border-b border-neutral-200/80 px-3 py-3 dark:border-neutral-800">
        <Link to="/messages" aria-label="Nazad" className="rounded-full p-1.5 hover:bg-neutral-100 dark:hover:bg-neutral-900 md:hidden"><ArrowLeft size={22} /></Link>
        {peer ? (
          <Link to={`/profile/${peer.username}`} className="flex min-w-0 items-center gap-3">
            <Avatar url={peer.profileImageUrl} name={peer.username} size={40} id={peer.id} />
            <span className="min-w-0 text-sm leading-tight">
              <span className="block truncate font-bold">{peer.username}</span>
              <span className="block truncate text-xs text-neutral-500">{peer.firstName} {peer.lastName}</span>
            </span>
          </Link>
        ) : (
          <div className="flex items-center gap-3"><Skeleton className="h-10 w-10 !rounded-full" /><Skeleton className="h-3 w-28" /></div>
        )}
      </header>

      <div className="min-h-0 flex-1 overflow-y-auto px-3 py-4" onScroll={onScroll}>
        {loading && <Spinner />}
        {error && !peer && <ErrorText>{error}</ErrorText>}
        {!loading && peer && msgs.length === 0 && (
          <EmptyState icon={<MessageCircle size={24} />}>Započni razgovor sa korisnikom {peer.username}.</EmptyState>
        )}
        <div className="space-y-1">
          {msgs.map((m, i) => {
            const newDay = i === 0 || dayLabel(msgs[i - 1].createdAt) !== dayLabel(m.createdAt);
            const sameSender = i > 0 && msgs[i - 1].mine === m.mine && !newDay;
            return (
              <div key={m.id}>
                {newDay && <p className="my-3 text-center text-[11px] font-semibold uppercase tracking-wide text-neutral-400">{dayLabel(m.createdAt)}</p>}
                <div className={`flex ${m.mine ? "justify-end" : "justify-start"} ${sameSender ? "" : "mt-2"}`}>
                  <div className={`max-w-[78%] rounded-3xl px-4 py-2 text-sm shadow-sm ${m.mine
                    ? "brand-gradient rounded-br-lg text-white"
                    : "rounded-bl-lg bg-neutral-100 dark:bg-neutral-800"}`}>
                    {m.storyReply && (
                      <p className={`mb-1 flex items-center gap-1 text-[11px] font-semibold ${m.mine ? "text-white/80" : "text-neutral-500"}`}>
                        <Reply size={12} /> {m.mine ? "Odgovorio/la si na priču" : "Odgovor na tvoju priču"}
                      </p>
                    )}
                    <p className="whitespace-pre-wrap break-words">{m.content}</p>
                    <p className={`mt-0.5 text-right text-[10px] ${m.mine ? "text-white/70" : "text-neutral-400"}`}>
                      {clock(m.createdAt)}{m.mine && m.id > 0 && (m.read ? " · Viđeno" : "")}
                    </p>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
        <div ref={bottom} />
      </div>

      <form onSubmit={send} className="flex items-end gap-2 border-t border-neutral-200/80 p-3 dark:border-neutral-800">
        <textarea
          rows={1} value={text} maxLength={2000} onChange={(e) => setText(e.target.value)} onKeyDown={onKey}
          disabled={!peer}
          placeholder="Napiši poruku..."
          className="max-h-32 min-h-[42px] flex-1 resize-none rounded-3xl bg-neutral-100 px-4 py-2.5 text-sm outline-none placeholder:text-neutral-500 focus:ring-2 focus:ring-indigo-500/30 dark:bg-neutral-900"
        />
        <button disabled={!text.trim() || sending || !peer} aria-label="Pošalji"
          className="brand-gradient flex h-[42px] w-[42px] shrink-0 items-center justify-center rounded-full text-white shadow-md shadow-fuchsia-500/20 transition active:scale-90 disabled:opacity-40">
          <Send size={18} />
        </button>
      </form>
    </div>
  );
}

/* ---------------------------------- stranica ---------------------------------- */

export default function MessagesPage() {
  const { username } = useParams();
  const [version, setVersion] = useState(0);
  const [composing, setComposing] = useState(false);
  const bump = useCallback(() => setVersion((v) => v + 1), []);

  return (
    <div className="mx-auto h-[calc(100dvh-9.5rem)] max-w-5xl md:h-[100dvh] md:p-4">
      <div className="flex h-full overflow-hidden border-neutral-200/80 bg-white dark:border-neutral-800 dark:bg-neutral-950 md:rounded-3xl md:border md:shadow-sm">
        <aside className={`${username ? "hidden md:flex" : "flex"} w-full min-h-0 flex-col border-neutral-200/80 dark:border-neutral-800 md:w-[340px] md:shrink-0 md:border-r`}>
          <div className="flex items-center justify-between px-5 pb-2 pt-5">
            <h1 className="text-xl font-bold tracking-tight">Poruke</h1>
            <button aria-label="Nova poruka" title="Nova poruka" onClick={() => setComposing(true)}
              className="rounded-full p-2 hover:bg-neutral-100 dark:hover:bg-neutral-900"><SquarePen size={20} /></button>
          </div>
          <div className="min-h-0 flex-1 overflow-y-auto"><ConversationList active={username} version={version} /></div>
        </aside>

        <section className={`${username ? "flex" : "hidden md:flex"} min-h-0 min-w-0 flex-1 flex-col`}>
          {username ? (
            <ChatThread key={username} username={username} onChanged={bump} />
          ) : (
            <div className="flex flex-1 flex-col items-center justify-center gap-3 p-6 text-center">
              <span className="brand-gradient flex h-16 w-16 items-center justify-center rounded-3xl text-white shadow-lg shadow-fuchsia-500/25"><Send size={28} /></span>
              <h2 className="text-lg font-bold">Tvoje poruke</h2>
              <p className="max-w-xs text-sm text-neutral-500">Pošalji poruku prijatelju ili odgovori na njegovu priču.</p>
              <button onClick={() => setComposing(true)} className="rounded-xl bg-accent px-4 py-2 text-sm font-semibold text-white hover:bg-accent-hover">Pošalji poruku</button>
            </div>
          )}
        </section>
      </div>
      {composing && <NewMessageModal onClose={() => setComposing(false)} />}
    </div>
  );
}
