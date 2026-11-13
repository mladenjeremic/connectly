import { useState } from "react";
import { useLocation } from "react-router-dom";
import { Newspaper } from "lucide-react";
import PostList from "../components/PostList";
import StoriesBar from "../components/StoriesBar";
import SuggestionsSidebar from "../components/SuggestionsSidebar";

type Tab = "following" | "all";

export default function FeedPage() {
  const location = useLocation();
  const [tab, setTab] = useState<Tab>("following");
  // posle objave nove objave (state.refresh) lista se ponovo učitava
  const reloadKey = (location.state as { refresh?: number } | null)?.refresh ?? 0;
  const endpoint = tab === "following" ? "/posts/following" : "/posts/feed";

  const tabCls = (active: boolean) =>
    `flex-1 rounded-full px-4 py-1.5 text-sm font-semibold transition ${active
      ? "bg-white text-neutral-950 shadow-sm dark:bg-neutral-800 dark:text-white"
      : "text-neutral-500 hover:text-neutral-800 dark:hover:text-neutral-200"}`;

  return (
    <div className="mx-auto flex max-w-[960px] justify-center gap-12 px-3 pt-4 md:pt-8">
      <div className="w-full max-w-[500px]">
        <StoriesBar />
        <div className="mb-4 flex rounded-full bg-neutral-200/70 p-1 dark:bg-neutral-900">
          <button className={tabCls(tab === "following")} onClick={() => setTab("following")}>Praćeni</button>
          <button className={tabCls(tab === "all")} onClick={() => setTab("all")}>Najnovije</button>
        </div>
        <PostList
          key={`${endpoint}-${reloadKey}`}
          endpoint={endpoint}
          reloadKey={reloadKey}
          emptyIcon={<Newspaper size={24} />}
          emptyText={tab === "following"
            ? "Još nema objava. Zaprati nekoga ili pogledaj tab „Najnovije“."
            : "Još nema objava. Budi prvi!"}
        />
      </div>
      <SuggestionsSidebar />
    </div>
  );
}
