import { NavLink, Outlet } from "react-router-dom";
import { FileText, Flag, LayoutDashboard, ScrollText, Users } from "lucide-react";

const tabs = [
  { to: "/admin", label: "Pregled", icon: LayoutDashboard, end: true },
  { to: "/admin/users", label: "Korisnici", icon: Users, end: false },
  { to: "/admin/reports", label: "Prijave", icon: Flag, end: false },
  { to: "/admin/posts", label: "Objave", icon: FileText, end: false },
  { to: "/admin/audit-log", label: "Audit log", icon: ScrollText, end: false },
];

export default function AdminLayout() {
  return (
    <div className="mx-auto max-w-6xl px-4 pt-6 md:pt-10">
      <h1 className="mb-4 text-2xl font-bold tracking-tight">Administracija</h1>
      <nav className="no-scrollbar mb-6 flex gap-1 overflow-x-auto rounded-2xl bg-neutral-200/60 p-1 dark:bg-neutral-900">
        {tabs.map(({ to, label, icon: Icon, end }) => (
          <NavLink key={to} to={to} end={end}
            className={({ isActive }) =>
              `flex shrink-0 items-center gap-2 rounded-xl px-3.5 py-2 text-sm font-semibold transition ${isActive ? "bg-white text-neutral-950 shadow-sm dark:bg-neutral-800 dark:text-white" : "text-neutral-500 hover:text-neutral-900 dark:hover:text-white"}`}>
            <Icon size={16} /> {label}
          </NavLink>
        ))}
      </nav>
      <Outlet />
    </div>
  );
}
