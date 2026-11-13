import { useCallback, useEffect, useRef, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { Ban, Bookmark, Camera, Heart, ImageOff, MessageCircle, Send } from "lucide-react";
import { toast } from "sonner";
import { api, errorMessage } from "../api/client";
import { useAuth } from "../auth/AuthContext";
import { filterImages } from "../components/ImagePicker";
import FollowButton from "../components/FollowButton";
import UserListModal from "../components/UserListModal";
import { Avatar, EmptyState, ErrorText, Spinner } from "../components/ui";
import { gradientFor } from "../lib/format";
import { btnSecondary } from "../lib/styles";
import type { Me, Post, Profile } from "../types";
import { compressImage } from "../lib/image";

const PAGE_SIZE = 12;

export default function ProfilePage() {
  const { username = "" } = useParams();
  const { refreshMe } = useAuth();
  const [profile, setProfile] = useState<Profile | null>(null);
  const [posts, setPosts] = useState<Post[]>([]);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [list, setList] = useState<"followers" | "following" | null>(null);
  const photoInput = useRef<HTMLInputElement>(null);

  const loadPosts = useCallback(async (p: number) => {
    try {
      const { data } = await api.get<Post[]>(`/users/${username}/posts`, { params: { page: p, size: PAGE_SIZE } });
      setPosts((prev) => (p === 0 ? data : [...prev, ...data]));
      setHasMore(data.length === PAGE_SIZE);
      setPage(p);
    } catch (err) {
      setError(errorMessage(err));
    }
  }, [username]);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);
    setProfile(null);
    setPosts([]);
    api.get<Profile>(`/users/${username}`)
      .then(async (res) => {
        if (cancelled) return;
        setProfile(res.data);
        await loadPosts(0);
      })
      .catch((err) => { if (!cancelled) setError(errorMessage(err)); })
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, [username, loadPosts]);

  async function toggleBlock() {
    if (!profile) return;
    if (!profile.blockedByMe && !window.confirm(`Blokirati @${profile.username}? Neće moći da te prati niti da ti piše.`)) return;
    try {
      if (profile.blockedByMe) {
        await api.delete(`/blocks/${profile.id}`);
        setProfile({ ...profile, blockedByMe: false });
        toast.success("Korisnik je odblokiran.");
      } else {
        await api.post(`/blocks/${profile.id}`);
        setProfile({ ...profile, blockedByMe: true, followedByMe: false });
        toast.success("Korisnik je blokiran.");
      }
    } catch (err) {
      toast.error(errorMessage(err));
    }
  }

  async function changePhoto(files: FileList | null) {
    const file = files ? filterImages(Array.from(files))[0] : undefined;
    if (!file || !profile) return;
    try {
      const form = new FormData();
      form.append("photo", await compressImage(file));
      const { data } = await api.post<Me>("/users/me/photo", form);
      setProfile({ ...profile, profileImageUrl: data.profileImageUrl });
      await refreshMe();
      toast.success("Profilna fotografija je promenjena.");
    } catch (err) {
      toast.error(errorMessage(err));
    } finally {
      if (photoInput.current) photoInput.current.value = "";
    }
  }

  if (loading) return <Spinner className="pt-20" />;
  if (error && !profile) return <div className="mx-auto max-w-md px-4 pt-10"><ErrorText>{error}</ErrorText></div>;
  if (!profile) return null;

  return (
    <div className="mx-auto max-w-[935px] px-4 pt-6 md:pt-10">
      <header className="mb-6 flex items-center gap-5 rounded-3xl border border-neutral-200/80 bg-white p-5 shadow-sm dark:border-neutral-800 dark:bg-neutral-950 sm:gap-12 sm:p-8">
        <div className="relative shrink-0">
          <span className="story-ring block rounded-full p-[3px]">
            <span className="block rounded-full bg-white p-[3px] dark:bg-neutral-950">
              <Avatar url={profile.profileImageUrl} name={profile.username} size={96} id={profile.id} />
            </span>
          </span>
          {profile.ownProfile && (
            <>
              <input ref={photoInput} type="file" accept="image/jpeg,image/png,image/webp" hidden onChange={(e) => changePhoto(e.target.files)} />
              <button aria-label="Promeni fotografiju" onClick={() => photoInput.current?.click()}
                className="absolute bottom-1 right-1 rounded-full border-2 border-white bg-accent p-1.5 text-white shadow dark:border-neutral-950">
                <Camera size={16} />
              </button>
            </>
          )}
        </div>

        <div className="min-w-0 flex-1 space-y-3">
          <div className="flex flex-wrap items-center gap-3">
            <h1 className="truncate text-xl font-bold tracking-tight">{profile.username}</h1>
            {profile.ownProfile ? (
              <>
                <Link to="/profile/edit" className={btnSecondary}>Izmeni profil</Link>
                <Link to="/saved" className={btnSecondary}><Bookmark size={15} /> Sačuvano</Link>
              </>
            ) : (
              <>
                {profile.blockedByMe ? (
                  <button className={btnSecondary} onClick={toggleBlock}><Ban size={15} /> Odblokiraj</button>
                ) : (
                  <>
                    <FollowButton userId={profile.id} following={profile.followedByMe}
                      onChange={(r) => setProfile({ ...profile, followedByMe: r.following, followerCount: r.followerCount })} />
                    <Link to={`/messages/${profile.username}`} className={btnSecondary}><Send size={15} /> Poruka</Link>
                    <button className={btnSecondary} onClick={toggleBlock} title="Blokiraj korisnika"><Ban size={15} /></button>
                  </>
                )}
              </>
            )}
          </div>
          <div className="flex gap-6 text-sm sm:gap-10">
            <span><strong className="text-base">{profile.postCount}</strong> <span className="text-neutral-500">objava</span></span>
            <button className="hover:opacity-70" onClick={() => setList("followers")}><strong className="text-base">{profile.followerCount}</strong> <span className="text-neutral-500">pratilaca</span></button>
            <button className="hover:opacity-70" onClick={() => setList("following")}><strong className="text-base">{profile.followingCount}</strong> <span className="text-neutral-500">praćenih</span></button>
          </div>
          <div className="text-sm">
            <p className="font-semibold">{profile.firstName} {profile.lastName}</p>
            {profile.bio && <p className="whitespace-pre-wrap break-words">{profile.bio}</p>}
            <p className="text-xs text-neutral-500">Član od {new Date(profile.createdAt).toLocaleDateString("sr-RS")}</p>
          </div>
        </div>
      </header>

      <div>
        {error && <ErrorText>{error}</ErrorText>}
        {posts.length === 0 && <EmptyState icon={<ImageOff size={24} />}>Još nema objava.</EmptyState>}
        <div className="grid grid-cols-3 gap-1.5 sm:gap-4">
          {posts.map((p) => (
            <Link key={p.id} to={`/post/${p.id}`} className="group relative block aspect-square overflow-hidden rounded-xl bg-neutral-100 dark:bg-neutral-900 sm:rounded-2xl">
              {p.images.length > 0 ? (
                <img src={p.images[0].url} alt="" className="h-full w-full object-cover transition duration-300 group-hover:scale-105" />
              ) : (
                <div className={`flex h-full w-full items-center justify-center bg-gradient-to-br ${gradientFor(p.id)} p-3 text-center`}>
                  <p className="line-clamp-5 break-words text-sm font-semibold text-white">{p.content}</p>
                </div>
              )}
              <div className="absolute inset-0 hidden items-center justify-center gap-5 bg-black/45 text-sm font-semibold text-white group-hover:flex">
                <span className="flex items-center gap-1"><Heart size={18} className="fill-white" /> {p.likeCount}</span>
                <span className="flex items-center gap-1"><MessageCircle size={18} className="fill-white" /> {p.commentCount}</span>
              </div>
            </Link>
          ))}
        </div>
        {hasMore && <button className="mt-4 w-full rounded-xl py-3 text-sm font-semibold text-accent hover:bg-accent-soft dark:hover:bg-indigo-950/40" onClick={() => loadPosts(page + 1)}>Učitaj još</button>}
      </div>

      {list && <UserListModal userId={profile.id} kind={list} onClose={() => setList(null)} />}
    </div>
  );
}
