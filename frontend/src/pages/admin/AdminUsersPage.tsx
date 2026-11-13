import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api, errorMessage } from "../../api/client";
import { Avatar, EmptyState, ErrorText, Modal, Spinner } from "../../components/ui";
import { dateTime } from "../../lib/format";
import { btnSecondary, cardCls, inputCls } from "../../lib/styles";
import type { AccountStatus, AdminUser, AdminUserDetail } from "../../types";
import ModerationModal, { type ModerationMode } from "./ModerationModal";
import { AccountStatusBadge } from "./StatusBadge";

const PAGE_SIZE = 20;

function UserDetailModal({ id, onClose }: { id: number; onClose: () => void }) {
  const [detail, setDetail] = useState<AdminUserDetail | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api.get<AdminUserDetail>(`/admin/users/${id}`)
      .then((res) => setDetail(res.data))
      .catch((err) => setError(errorMessage(err)));
  }, [id]);

  return (
    <Modal title="Detalji naloga" onClose={onClose} wide>
      <div className="space-y-5 p-4 text-sm">
        {error && <ErrorText>{error}</ErrorText>}
        {!detail && !error && <Spinner />}
        {detail && (
          <>
            <div className="flex items-center gap-3">
              <Avatar url={detail.user.profileImageUrl} name={detail.user.username} size={52} id={detail.user.id} />
              <div>
                <p className="font-semibold">{detail.user.firstName} {detail.user.lastName} <span className="font-normal text-neutral-500">@{detail.user.username}</span></p>
                <p className="text-neutral-500">{detail.user.email} · registrovan {dateTime(detail.user.createdAt)} · {detail.user.postCount} objava</p>
              </div>
              <div className="ml-auto"><AccountStatusBadge status={detail.user.status} /></div>
            </div>

            <section>
              <h3 className="mb-2 font-semibold">Istorija zabrana</h3>
              {detail.bans.length === 0 && <p className="text-neutral-500">Nema zabrana.</p>}
              <ul className="space-y-2">
                {detail.bans.map((b) => (
                  <li key={b.id} className="rounded-lg border border-neutral-200 p-3 dark:border-neutral-800">
                    <p><strong>{b.permanent ? "Trajna zabrana" : "Privremena zabrana"}</strong> {b.active && <span className="text-amber-600">(aktivna)</span>}</p>
                    <p className="text-neutral-500">{dateTime(b.startsAt)} → {b.permanent ? "bez isteka" : dateTime(b.endsAt)} · admin: {b.adminUsername ?? "?"}</p>
                    <p>Razlog: {b.reason}</p>
                  </li>
                ))}
              </ul>
            </section>

            <section>
              <h3 className="mb-2 font-semibold">Moderatorske akcije</h3>
              {detail.actions.length === 0 && <p className="text-neutral-500">Nema evidentiranih akcija.</p>}
              <ul className="space-y-1">
                {detail.actions.map((a) => (
                  <li key={a.id} className="text-neutral-600 dark:text-neutral-300">
                    <span className="text-neutral-500">{dateTime(a.createdAt)}</span> · <strong>{a.actionType}</strong> · {a.adminUsername ?? "?"}{a.reason ? ` · ${a.reason}` : ""}
                  </li>
                ))}
              </ul>
            </section>
          </>
        )}
      </div>
    </Modal>
  );
}

export default function AdminUsersPage() {
  const [q, setQ] = useState("");
  const [status, setStatus] = useState<AccountStatus | "">("");
  const [users, setUsers] = useState<AdminUser[]>([]);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [action, setAction] = useState<{ user: AdminUser; mode: ModerationMode } | null>(null);
  const [detailId, setDetailId] = useState<number | null>(null);

  const load = useCallback(async (p: number, term: string, st: AccountStatus | "") => {
    setLoading(true);
    setError(null);
    try {
      const { data } = await api.get<AdminUser[]>("/admin/users", {
        params: { q: term, status: st || undefined, page: p, size: PAGE_SIZE },
      });
      setUsers((prev) => (p === 0 ? data : [...prev, ...data]));
      setHasMore(data.length === PAGE_SIZE);
      setPage(p);
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  }, []);

  // pretraga po username-u ili email adresi, sa debounce-om
  useEffect(() => {
    const t = setTimeout(() => { load(0, q.trim(), status); }, 300);
    return () => clearTimeout(t);
  }, [q, status, load]);

  const replace = (updated: AdminUser) => setUsers((list) => list.map((u) => (u.id === updated.id ? updated : u)));

  const small = "rounded-md border border-neutral-300 px-2 py-1 text-xs font-medium hover:bg-neutral-100 dark:border-neutral-700 dark:hover:bg-neutral-800";

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap gap-3">
        <input className={`${inputCls} max-w-xs`} placeholder="Pretraga po username-u ili email-u..." value={q} onChange={(e) => setQ(e.target.value)} />
        <select className={`${inputCls} max-w-[200px]`} value={status} onChange={(e) => setStatus(e.target.value as AccountStatus | "")}>
          <option value="">Svi statusi</option>
          <option value="ACTIVE">Aktivni</option>
          <option value="SUSPENDED">Suspendovani</option>
          <option value="BANNED">Trajno blokirani</option>
          <option value="DEACTIVATED">Deaktivirani</option>
        </select>
      </div>

      {error && <ErrorText>{error}</ErrorText>}

      <div className={`${cardCls} overflow-x-auto`}>
        <table className="w-full text-left text-sm">
          <thead className="border-b border-neutral-200 text-xs uppercase text-neutral-500 dark:border-neutral-800">
            <tr>
              <th className="px-4 py-3">Korisnik</th><th className="px-4 py-3">Email</th><th className="px-4 py-3">Status</th>
              <th className="px-4 py-3">Objave</th><th className="px-4 py-3">Registrovan</th><th className="px-4 py-3">Akcije</th>
            </tr>
          </thead>
          <tbody>
            {users.map((u) => (
              <tr key={u.id} className="border-b border-neutral-100 last:border-0 dark:border-neutral-900">
                <td className="px-4 py-3">
                  <div className="flex items-center gap-2">
                    <Avatar url={u.profileImageUrl} name={u.username} size={32} id={u.id} />
                    <div className="leading-tight">
                      <Link to={`/profile/${u.username}`} className="font-semibold hover:underline">{u.username}</Link>
                      <p className="text-xs text-neutral-500">{u.firstName} {u.lastName}{u.role === "ADMIN" ? " · admin" : ""}</p>
                    </div>
                  </div>
                </td>
                <td className="px-4 py-3">{u.email}</td>
                <td className="px-4 py-3">
                  <AccountStatusBadge status={u.status} />
                  {u.activeBan && !u.activeBan.permanent && <p className="mt-1 text-xs text-neutral-500">do {dateTime(u.activeBan.endsAt)}</p>}
                </td>
                <td className="px-4 py-3">{u.postCount}</td>
                <td className="px-4 py-3 whitespace-nowrap">{new Date(u.createdAt).toLocaleDateString("sr-RS")}</td>
                <td className="px-4 py-3">
                  <div className="flex flex-wrap gap-1.5">
                    <button className={small} onClick={() => setDetailId(u.id)}>Detalji</button>
                    {u.role !== "ADMIN" && (
                      <>
                        {u.status === "ACTIVE" && <button className={small} onClick={() => setAction({ user: u, mode: "ban" })}>Ban</button>}
                        {(u.status === "ACTIVE" || u.status === "SUSPENDED") && <button className={`${small} text-red-600`} onClick={() => setAction({ user: u, mode: "permanent-ban" })}>Trajni ban</button>}
                        {(u.status === "SUSPENDED" || u.status === "BANNED") && <button className={small} onClick={() => setAction({ user: u, mode: "unban" })}>Skini ban</button>}
                        {u.status !== "DEACTIVATED" && <button className={small} onClick={() => setAction({ user: u, mode: "deactivate" })}>Deaktiviraj</button>}
                        {u.status === "DEACTIVATED" && <button className={small} onClick={() => setAction({ user: u, mode: "reactivate" })}>Aktiviraj</button>}
                      </>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {loading && <Spinner />}
        {!loading && users.length === 0 && <EmptyState>Nema korisnika za zadati filter.</EmptyState>}
      </div>

      {hasMore && !loading && <button className={btnSecondary} onClick={() => load(page + 1, q.trim(), status)}>Učitaj još</button>}

      {action && <ModerationModal user={action.user} mode={action.mode} onClose={() => setAction(null)} onDone={replace} />}
      {detailId !== null && <UserDetailModal id={detailId} onClose={() => setDetailId(null)} />}
    </div>
  );
}
