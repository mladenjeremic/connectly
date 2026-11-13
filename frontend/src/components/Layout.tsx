import { useState, type ReactNode } from "react";
import { Link, NavLink, Outlet, useNavigate } from "react-router-dom";
import { Bookmark, Heart, House, LogOut, Moon, Search, Send, Settings, ShieldAlert, SquarePlus, Sun } from "lucide-react";
import { useAuth } from "../auth/AuthContext";
import { MessagesProvider, useMessages } from "../context/MessagesContext";
import { NotificationsProvider, useNotifications } from "../context/NotificationsContext";
import { useTheme } from "../context/ThemeContext";
import NewPostModal from "./NewPostModal";
import { Avatar } from "./ui";

// uska traka sa ikonicama na md, a na xl se širi i prikazuje nazive
const railItem =
  "relative flex h-12 w-12 items-center justify-center gap-4 rounded-2xl text-neutral-700 transition hover:bg-neutral-100 hover:text-neutral-950 dark:text-neutral-300 dark:hover:bg-neutral-900 dark:hover:text-white xl:w-full xl:justify-start xl:px-3.5";
const railActive = "bg-neutral-100 font-semibold !text-neutral-950 dark:bg-neutral-900 dark:!text-white";

function LogoMark({ size = 32 }: { size?: number }) {
  return (
    <span className="brand-gradient flex items-center justify-center rounded-xl shadow-md shadow-fuchsia-500/20" style={{ width: size, height: size }}>
      <svg viewBox="0 0 24 24" width={size * 0.62} height={size * 0.62} fill="none" stroke="#fff" strokeWidth="2.6">
        <circle cx="8" cy="12" r="4" /><circle cx="16" cy="12" r="4" />
      </svg>
    </span>
  );
}

function Logo() {
  return (
    <Link to="/feed" aria-label="Connectly" className="mb-2 flex h-14 w-12 items-center justify-center gap-3 rounded-2xl xl:w-full xl:justify-start xl:px-3.5">
      <LogoMark />
      <span className="brand-text hidden text-xl font-extrabold tracking-tight xl:inline">Connectly</span>
    </Link>
  );
}

function Badge({ count }: { count: number }) {
  if (count <= 0) return null;
  return (
    <span className="absolute -right-2 -top-1.5 flex h-[18px] min-w-[18px] items-center justify-center rounded-full bg-red-500 px-1 text-[10px] font-bold text-white ring-2 ring-white dark:ring-black">
      {count > 99 ? "99+" : count}
    </span>
  );
}

function Shell() {
  const { user, isAdmin, logout } = useAuth();
  const { theme, toggle } = useTheme();
  const { unread } = useNotifications();
  const { unread: unreadMessages } = useMessages();
  const navigate = useNavigate();
  const [creating, setCreating] = useState(false);

  if (!user) return null;

  const label = (text: string) => <span className="hidden text-[15px] xl:inline">{text}</span>;

  const link = (to: string, text: string, icon: ReactNode, badge = 0) => (
    <NavLink to={to} title={text} aria-label={text}
      className={({ isActive }) => `${railItem} ${isActive ? railActive : ""}`}>
      <span className="relative">{icon}<Badge count={badge} /></span>
      {label(text)}
    </NavLink>
  );

  return (
    <div className="min-h-screen">
      {/* leva traka (desktop) */}
      <nav className="glass fixed inset-y-0 left-0 z-30 hidden w-[76px] flex-col items-center border-r border-neutral-200/80 px-3 py-4 dark:border-neutral-800 md:flex xl:w-[244px] xl:items-stretch">
        <Logo />
        <div className="mt-3 flex flex-1 flex-col items-center gap-1 xl:items-stretch">
          {link("/feed", "Početna", <House size={25} />)}
          {link("/search", "Pretraga", <Search size={25} />)}
          {link("/messages", "Poruke", <Send size={25} />, unreadMessages)}
          {link("/notifications", "Obaveštenja", <Heart size={25} />, unread)}
          {link("/saved", "Sačuvano", <Bookmark size={25} />)}
          <button title="Nova objava" aria-label="Nova objava" className={railItem} onClick={() => setCreating(true)}>
            <SquarePlus size={25} />{label("Nova objava")}
          </button>
          {link(`/profile/${user.username}`, "Profil", <Avatar url={user.profileImageUrl} name={user.username} size={26} id={user.userId} />)}
          {isAdmin && link("/admin", "Admin panel", <ShieldAlert size={25} />)}
        </div>
        <div className="flex flex-col items-center gap-1 xl:items-stretch">
          <button title={theme === "dark" ? "Svetla tema" : "Tamna tema"} aria-label="Promeni temu" className={railItem} onClick={toggle}>
            {theme === "dark" ? <Sun size={24} /> : <Moon size={24} />}{label(theme === "dark" ? "Svetla tema" : "Tamna tema")}
          </button>
          {link("/settings", "Podešavanja", <Settings size={24} />)}
          <button title="Odjavi se" aria-label="Odjavi se" className={railItem} onClick={() => { logout(); navigate("/login"); }}>
            <LogOut size={24} />{label("Odjavi se")}
          </button>
        </div>
      </nav>

      {/* gornja traka (mobilni) */}
      <header className="glass sticky top-0 z-30 flex items-center justify-between border-b border-neutral-200/80 px-4 py-2.5 dark:border-neutral-800 md:hidden">
        <Link to="/feed" className="flex items-center gap-2">
          <LogoMark size={28} />
          <span className="brand-text text-xl font-extrabold tracking-tight">Connectly</span>
        </Link>
        <div className="flex items-center gap-4">
          <Link to="/messages" aria-label="Poruke" className="relative">
            <Send size={22} /><Badge count={unreadMessages} />
          </Link>
          <Link to="/saved" aria-label="Sačuvano"><Bookmark size={22} /></Link>
          <button aria-label="Promeni temu" onClick={toggle}>{theme === "dark" ? <Sun size={22} /> : <Moon size={22} />}</button>
          <Link to="/settings" aria-label="Podešavanja"><Settings size={22} /></Link>
        </div>
      </header>

      <main className="pb-24 md:pb-0 md:pl-[76px] xl:pl-[244px]">
        <Outlet />
      </main>

      {/* donja navigacija (mobilni) */}
      <nav className="glass fixed inset-x-0 bottom-0 z-30 flex items-center justify-around border-t border-neutral-200/80 pb-[env(safe-area-inset-bottom)] pt-1.5 dark:border-neutral-800 md:hidden">
        {link("/feed", "Početna", <House size={26} />)}
        {link("/search", "Pretraga", <Search size={26} />)}
        <button aria-label="Nova objava" className="brand-gradient flex h-11 w-11 items-center justify-center rounded-2xl text-white shadow-lg shadow-fuchsia-500/30 transition active:scale-95" onClick={() => setCreating(true)}>
          <SquarePlus size={24} />
        </button>
        {link("/notifications", "Obaveštenja", <Heart size={26} />, unread)}
        {link(`/profile/${user.username}`, "Profil", <Avatar url={user.profileImageUrl} name={user.username} size={28} id={user.userId} />)}
      </nav>

      {creating && (
        <NewPostModal onClose={() => setCreating(false)} onCreated={() => navigate("/feed", { state: { refresh: Date.now() } })} />
      )}
    </div>
  );
}

export default function Layout() {
  return (
    <NotificationsProvider>
      <MessagesProvider>
        <Shell />
      </MessagesProvider>
    </NotificationsProvider>
  );
}
