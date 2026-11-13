import { useCallback, useEffect, useRef, useState, type FormEvent, type PointerEvent } from "react";
import { Link } from "react-router-dom";
import { Send, Trash2, X } from "lucide-react";
import { toast } from "sonner";
import { api, errorMessage } from "../api/client";
import { markStorySeen } from "../lib/seenStories";
import { timeAgo } from "../lib/format";
import type { StoryGroup } from "../types";
import { Avatar } from "./ui";

const DURATION_MS = 5000;
const TICK_MS = 50;
const HOLD_MS = 250;
const QUICK_REACTIONS = ["❤️", "😂", "😮", "😢", "👏", "🔥"];

interface Props {
  groups: StoryGroup[];
  startGroup: number;
  onClose: () => void;
  onDeleted: () => void;
}

function hoursLeft(iso: string): string {
  const h = Math.max(0, Math.ceil((new Date(iso).getTime() - Date.now()) / 3_600_000));
  return h <= 1 ? "Ističe za manje od sat vremena" : `Ističe za ${h} h`;
}

/** Brze reakcije + polje za odgovor; odgovor stiže autoru kao poruka u razgovoru. Stanje se resetuje za svaku priču (key). */
function StoryReplyBar({ storyId, username, onActive }: { storyId: number; username: string; onActive: (active: boolean) => void }) {
  const [reply, setReply] = useState("");
  const [sending, setSending] = useState(false);
  const [focused, setFocused] = useState(false);

  // viewer staje dok korisnik kuca ili se odgovor šalje
  useEffect(() => { onActive(focused || sending); }, [focused, sending, onActive]);
  useEffect(() => () => onActive(false), [onActive]);

  async function send(content: string) {
    const text = content.trim();
    if (!text || sending) return;
    setSending(true);
    try {
      await api.post(`/stories/${storyId}/replies`, { content: text });
      setReply("");
      toast.success(`Poruka poslata korisniku ${username}.`);
      (document.activeElement as HTMLElement | null)?.blur();
    } catch (err) {
      toast.error(errorMessage(err));
    } finally {
      setSending(false);
    }
  }

  function submit(e: FormEvent) {
    e.preventDefault();
    send(reply);
  }

  return (
    <>
      <div className="flex justify-center gap-2">
        {QUICK_REACTIONS.map((emoji) => (
          <button key={emoji} disabled={sending} onClick={() => send(emoji)}
            className="rounded-full bg-white/15 px-2.5 py-1.5 text-xl backdrop-blur transition hover:scale-110 hover:bg-white/25 active:scale-95 disabled:opacity-50">
            {emoji}
          </button>
        ))}
      </div>
      <form onSubmit={submit} className="flex items-center gap-2">
        <input
          value={reply} maxLength={500} placeholder={`Odgovori korisniku ${username}...`}
          onChange={(e) => setReply(e.target.value)}
          onFocus={() => setFocused(true)} onBlur={() => setFocused(false)}
          className="min-w-0 flex-1 rounded-full border border-white/40 bg-black/30 px-4 py-2.5 text-sm text-white outline-none backdrop-blur placeholder:text-white/70 focus:border-white"
        />
        {reply.trim() && (
          <button disabled={sending} aria-label="Pošalji odgovor" className="rounded-full bg-white p-2.5 text-black transition active:scale-90 disabled:opacity-50">
            <Send size={18} />
          </button>
        )}
      </form>
    </>
  );
}

export default function StoryViewer({ groups, startGroup, onClose, onDeleted }: Props) {
  const [g, setG] = useState(startGroup);
  const [s, setS] = useState(0);
  const [holding, setHolding] = useState(false);     // korisnik drži prst/miš na priči
  const [replyActive, setReplyActive] = useState(false);   // kuca ili šalje odgovor
  const elapsed = useRef(0);
  const pressedAt = useRef(0);

  const group = groups[g];
  const story = group?.stories[s];
  const paused = holding || replyActive;

  const next = useCallback(() => {
    if (!group) return;
    if (s < group.stories.length - 1) setS(s + 1);
    else if (g < groups.length - 1) { setG(g + 1); setS(0); }
    else onClose();
  }, [g, s, group, groups.length, onClose]);

  const prev = useCallback(() => {
    if (s > 0) setS(s - 1);
    else if (g > 0) { setG(g - 1); setS(0); }
  }, [g, s]);

  // nova priča: tajmer od nule i zapamti da je viđena
  const storyId = story?.id;
  useEffect(() => {
    elapsed.current = 0;
    if (storyId !== undefined) markStorySeen(storyId);
  }, [storyId]);

  // automatsko listanje; staje dok korisnik drži priču ili kuca odgovor
  useEffect(() => {
    if (paused) return;
    const t = setInterval(() => {
      elapsed.current += TICK_MS;
      if (elapsed.current >= DURATION_MS) next();
    }, TICK_MS);
    return () => clearInterval(t);
  }, [next, paused, storyId]);

  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      const inField = (e.target as HTMLElement | null)?.tagName === "INPUT";
      if (e.key === "Escape") { if (inField) (e.target as HTMLElement).blur(); else onClose(); }
      if (inField) return;
      if (e.key === "ArrowRight") next();
      if (e.key === "ArrowLeft") prev();
    };
    document.addEventListener("keydown", onKey);
    return () => document.removeEventListener("keydown", onKey);
  }, [next, prev, onClose]);

  if (!group || !story) return null;

  async function remove() {
    try {
      await api.delete(`/stories/${story.id}`);
      toast.success("Priča je obrisana.");
      onDeleted();
      onClose();
    } catch (err) {
      toast.error(errorMessage(err));
    }
  }

  // kratak klik listaju priču, duži pritisak je samo pauza
  const down = (e: PointerEvent) => { pressedAt.current = Date.now(); setHolding(true); e.currentTarget.setPointerCapture?.(e.pointerId); };
  const up = () => setHolding(false);
  const tap = (action: () => void) => () => { if (Date.now() - pressedAt.current < HOLD_MS) action(); };

  return (
    <div className="animate-fade-in fixed inset-0 z-50 flex items-center justify-center bg-black/90 backdrop-blur-sm" onClick={onClose}>
      <div className="relative h-full max-h-[100dvh] w-full max-w-[420px] overflow-hidden bg-black sm:h-[calc(100dvh-2rem)] sm:rounded-3xl" onClick={(e) => e.stopPropagation()}>
        <img src={story.imageUrl} alt="" className="h-full w-full select-none object-contain" draggable={false} />
        <div className="pointer-events-none absolute inset-x-0 top-0 h-28 bg-gradient-to-b from-black/60 to-transparent" />
        <div className="pointer-events-none absolute inset-x-0 bottom-0 h-32 bg-gradient-to-t from-black/60 to-transparent" />

        <div className="absolute inset-x-0 top-0 z-20 flex gap-1 p-2.5">
          {group.stories.map((st, i) => (
            <div key={st.id} className="h-[3px] flex-1 overflow-hidden rounded bg-white/30">
              <div
                key={`${g}-${s}`}
                className="h-full bg-white"
                style={i < s ? { width: "100%" } : i === s
                  ? { width: "100%", animation: `storybar ${DURATION_MS}ms linear`, animationPlayState: paused ? "paused" : "running" }
                  : { width: 0 }}
              />
            </div>
          ))}
        </div>

        <div className="absolute inset-x-0 top-5 z-20 flex items-center gap-2 px-3 text-white">
          <Avatar url={group.profileImageUrl} name={group.username} size={34} id={group.userId} />
          <Link to={`/profile/${group.username}`} onClick={onClose} className="text-sm font-semibold drop-shadow">{group.username}</Link>
          <span className="text-xs text-white/75">{timeAgo(story.createdAt)}</span>
          <div className="ml-auto flex items-center gap-3">
            {story.ownStory && <button aria-label="Obriši priču" onClick={remove}><Trash2 size={20} /></button>}
            <button aria-label="Zatvori" onClick={onClose}><X size={26} /></button>
          </div>
        </div>

        <button aria-label="Prethodna priča" className="absolute bottom-28 left-0 top-20 z-10 w-1/3 touch-none"
          onPointerDown={down} onPointerUp={up} onPointerCancel={up} onClick={tap(prev)} />
        <button aria-label="Sledeća priča" className="absolute bottom-28 right-0 top-20 z-10 w-2/3 touch-none"
          onPointerDown={down} onPointerUp={up} onPointerCancel={up} onClick={tap(next)} />

        <div className="absolute inset-x-0 bottom-0 z-20 space-y-3 p-3">
          {story.ownStory ? (
            <p className="text-center text-xs text-white/80">{hoursLeft(story.expiresAt)}</p>
          ) : (
            <StoryReplyBar key={story.id} storyId={story.id} username={group.username} onActive={setReplyActive} />
          )}
        </div>
      </div>
    </div>
  );
}
