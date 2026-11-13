import { useCallback, useEffect, useState } from "react";
import { api, errorMessage } from "../../api/client";
import { EmptyState, ErrorText, Spinner } from "../../components/ui";
import { dateTime } from "../../lib/format";
import { btnSecondary, cardCls } from "../../lib/styles";
import type { AdminAction } from "../../types";

const PAGE_SIZE = 30;

export default function AdminAuditLogPage() {
  const [items, setItems] = useState<AdminAction[]>([]);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async (p: number) => {
    setLoading(true);
    try {
      const { data } = await api.get<AdminAction[]>("/admin/audit-log", { params: { page: p, size: PAGE_SIZE } });
      setItems((prev) => (p === 0 ? data : [...prev, ...data]));
      setHasMore(data.length === PAGE_SIZE);
      setPage(p);
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { load(0); }, [load]);

  return (
    <div className="space-y-4">
      {error && <ErrorText>{error}</ErrorText>}
      <div className={`${cardCls} overflow-x-auto`}>
        <table className="w-full text-left text-sm">
          <thead className="border-b border-neutral-200 text-xs uppercase text-neutral-500 dark:border-neutral-800">
            <tr>
              <th className="px-4 py-3">Vreme</th><th className="px-4 py-3">Administrator</th><th className="px-4 py-3">Akcija</th>
              <th className="px-4 py-3">Cilj</th><th className="px-4 py-3">Razlog</th><th className="px-4 py-3">Detalji</th>
            </tr>
          </thead>
          <tbody>
            {items.map((a) => (
              <tr key={a.id} className="border-b border-neutral-100 last:border-0 dark:border-neutral-900">
                <td className="whitespace-nowrap px-4 py-3">{dateTime(a.createdAt)}</td>
                <td className="px-4 py-3">@{a.adminUsername ?? "?"}</td>
                <td className="px-4 py-3 font-medium">{a.actionType}</td>
                <td className="px-4 py-3">{a.targetUsername ? `@${a.targetUsername}` : "—"}</td>
                <td className="max-w-xs px-4 py-3">{a.reason ?? "—"}</td>
                <td className="px-4 py-3 text-xs text-neutral-500">{a.metadata ?? ""}</td>
              </tr>
            ))}
          </tbody>
        </table>
        {loading && <Spinner />}
        {!loading && items.length === 0 && <EmptyState>Još nema moderatorskih akcija.</EmptyState>}
      </div>
      {hasMore && !loading && <button className={btnSecondary} onClick={() => load(page + 1)}>Učitaj još</button>}
    </div>
  );
}
