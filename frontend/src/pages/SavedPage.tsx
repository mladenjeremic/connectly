import { Bookmark } from "lucide-react";
import PostList from "../components/PostList";

/** Privatna lista objava koje je korisnik sačuvao. */
export default function SavedPage() {
  return (
    <div className="mx-auto max-w-[500px] px-3 pt-4 md:pt-8">
      <div className="mb-5 flex items-center gap-3">
        <span className="flex h-10 w-10 items-center justify-center rounded-2xl bg-accent-soft text-accent dark:bg-indigo-950/50"><Bookmark size={20} /></span>
        <div>
          <h1 className="text-xl font-bold tracking-tight">Sačuvano</h1>
          <p className="text-xs text-neutral-500">Vidiš ih samo ti</p>
        </div>
      </div>
      <PostList
        endpoint="/posts/saved"
        removeWhenUnsaved
        emptyIcon={<Bookmark size={24} />}
        emptyText="Još nemaš sačuvanih objava. Dodirni ikonicu oznake ispod objave da je sačuvaš."
      />
    </div>
  );
}
