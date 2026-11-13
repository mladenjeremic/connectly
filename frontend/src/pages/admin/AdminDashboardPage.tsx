import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { FileText, Flag, ShieldBan, UserCheck, UserMinus, Users } from "lucide-react";
import { api, errorMessage } from "../../api/client";
import { ErrorText, Skeleton } from "../../components/ui";
import { cardCls } from "../../lib/styles";
import type { Dashboard } from "../../types";

export default function AdminDashboardPage() {
  const [data, setData] = useState<Dashboard | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api.get<Dashboard>("/admin/dashboard")
      .then((res) => setData(res.data))
      .catch((err) => setError(errorMessage(err)));
  }, []);

  if (error) return <ErrorText>{error}</ErrorText>;
  if (!data) {
    return (
      <div className="grid grid-cols-2 gap-4 md:grid-cols-3">
        {[0, 1, 2, 3, 4, 5].map((i) => <Skeleton key={i} className="h-28 !rounded-2xl" />)}
      </div>
    );
  }

  const stats = [
    { label: "Ukupno korisnika", value: data.totalUsers, to: "/admin/users", icon: Users, tint: "bg-indigo-100 text-indigo-600 dark:bg-indigo-950/60" },
    { label: "Aktivni nalozi", value: data.activeUsers, to: "/admin/users", icon: UserCheck, tint: "bg-emerald-100 text-emerald-600 dark:bg-emerald-950/60" },
    { label: "Suspendovani nalozi", value: data.suspendedUsers, to: "/admin/users", icon: UserMinus, tint: "bg-amber-100 text-amber-600 dark:bg-amber-950/60" },
    { label: "Trajno blokirani", value: data.bannedUsers, to: "/admin/users", icon: ShieldBan, tint: "bg-red-100 text-red-600 dark:bg-red-950/60" },
    { label: "Objave", value: data.totalPosts, to: "/admin/posts", icon: FileText, tint: "bg-sky-100 text-sky-600 dark:bg-sky-950/60" },
    { label: "Nerešene prijave", value: data.pendingReports, to: "/admin/reports", icon: Flag, tint: "bg-fuchsia-100 text-fuchsia-600 dark:bg-fuchsia-950/60", highlight: data.pendingReports > 0 },
  ];

  return (
    <div className="grid grid-cols-2 gap-4 md:grid-cols-3">
      {stats.map((s) => (
        <Link key={s.label} to={s.to} className={`${cardCls} p-5 transition hover:-translate-y-0.5 hover:shadow-md ${s.highlight ? "!border-amber-400" : ""}`}>
          <span className={`mb-3 flex h-10 w-10 items-center justify-center rounded-xl ${s.tint}`}><s.icon size={20} /></span>
          <p className="text-3xl font-extrabold tracking-tight">{s.value}</p>
          <p className="mt-1 text-sm text-neutral-500">{s.label}</p>
        </Link>
      ))}
    </div>
  );
}
