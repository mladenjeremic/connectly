import { useCallback, useEffect, useRef, useState } from "react";
import { Plus } from "lucide-react";
import { toast } from "sonner";
import { api, errorMessage } from "../api/client";
import { useAuth } from "../auth/AuthContext";
import { getSeenStories } from "../lib/seenStories";
import type { StoryGroup } from "../types";
import { filterImages } from "./ImagePicker";
import StoryViewer from "./StoryViewer";
import { Avatar, Skeleton } from "./ui";
import { compressImage } from "../lib/image";

/** Red priča na vrhu feed-a (priče nestaju posle 24 sata). Pregledane priče imaju sivi prsten. */
export default function StoriesBar() {
  const { user } = useAuth();
  const [groups, setGroups] = useState<StoryGroup[]>([]);
  const [loaded, setLoaded] = useState(false);
  const [seen, setSeen] = useState<Set<number>>(getSeenStories);
  const [viewing, setViewing] = useState<number | null>(null);
  const [uploading, setUploading] = useState(false);
  const input = useRef<HTMLInputElement>(null);

  const load = useCallback(() => {
    api.get<StoryGroup[]>("/stories")
      .then((res) => setGroups(res.data))
      .catch(() => setGroups([]))
      .finally(() => setLoaded(true));
  }, []);

  useEffect(() => { load(); }, [load]);

  async function upload(list: FileList | null) {
    const file = list ? filterImages(Array.from(list))[0] : undefined;
    if (!file) return;
    setUploading(true);
    try {
      const form = new FormData();
      form.append("image", await compressImage(file));
      await api.post("/stories", form);
      toast.success("Priča je objavljena.");
      load();
    } catch (err) {
      toast.error(errorMessage(err));
    } finally {
      setUploading(false);
      if (input.current) input.current.value = "";
    }
  }

  function closeViewer() {
    setViewing(null);
    setSeen(getSeenStories());
  }

  if (!user) return null;
  const ownIndex = groups.findIndex((g) => g.ownGroup);
  const others = groups.map((g, i) => ({ g, i })).filter((x) => !x.g.ownGroup);
  const allSeen = (g: StoryGroup) => g.stories.every((s) => seen.has(s.id));
  const ringFor = (g: StoryGroup) => (allSeen(g) ? "story-ring-seen" : "story-ring");

  return (
    <div className="no-scrollbar -mx-1 mb-4 flex gap-4 overflow-x-auto rounded-2xl border border-neutral-200/80 bg-white px-4 py-3.5 shadow-sm dark:border-neutral-800 dark:bg-neutral-950">
      <input ref={input} type="file" accept="image/jpeg,image/png,image/webp" hidden onChange={(e) => upload(e.target.files)} />

      <div className="flex w-[68px] shrink-0 flex-col items-center gap-1.5">
        <div className="relative">
          <button
            onClick={() => (ownIndex >= 0 ? setViewing(ownIndex) : input.current?.click())}
            className={`rounded-full p-[3px] transition active:scale-95 ${ownIndex >= 0 ? ringFor(groups[ownIndex]) : "bg-neutral-200 dark:bg-neutral-800"}`}
            aria-label="Moja priča"
          >
            <span className="block rounded-full bg-white p-[2px] dark:bg-neutral-950">
              <Avatar url={user.profileImageUrl} name={user.username} size={56} id={user.userId} />
            </span>
          </button>
          <button onClick={() => input.current?.click()} disabled={uploading} aria-label="Dodaj priču"
            className="absolute -bottom-0.5 -right-0.5 rounded-full border-2 border-white bg-accent p-0.5 text-white shadow dark:border-neutral-950">
            <Plus size={14} />
          </button>
        </div>
        <span className="w-full truncate text-center text-xs text-neutral-600 dark:text-neutral-300">{uploading ? "Slanje..." : "Tvoja priča"}</span>
      </div>

      {!loaded && [0, 1, 2, 3].map((i) => (
        <div key={i} className="flex w-[68px] shrink-0 flex-col items-center gap-1.5">
          <Skeleton className="h-[66px] w-[66px] !rounded-full" />
          <Skeleton className="h-2.5 w-12" />
        </div>
      ))}

      {others.map(({ g, i }) => (
        <button key={g.userId} onClick={() => setViewing(i)} className="flex w-[68px] shrink-0 flex-col items-center gap-1.5 transition active:scale-95">
          <span className={`${ringFor(g)} rounded-full p-[3px]`}>
            <span className="block rounded-full bg-white p-[2px] dark:bg-neutral-950">
              <Avatar url={g.profileImageUrl} name={g.username} size={56} id={g.userId} />
            </span>
          </span>
          <span className={`w-full truncate text-center text-xs ${allSeen(g) ? "text-neutral-400" : "text-neutral-700 dark:text-neutral-200"}`}>{g.username}</span>
        </button>
      ))}

      {viewing !== null && (
        <StoryViewer groups={groups} startGroup={viewing} onClose={closeViewer} onDeleted={load} />
      )}
    </div>
  );
}
