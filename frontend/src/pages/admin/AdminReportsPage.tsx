import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { toast } from "sonner";
import { api, errorMessage } from "../../api/client";
import { EmptyState, ErrorText, Spinner } from "../../components/ui";
import { categoryLabel, dateTime } from "../../lib/format";
import { btnDanger, btnSecondary, cardCls, inputCls } from "../../lib/styles";
import type { Report, ReportDecision, ReportStatus } from "../../types";
import { ReportStatusBadge } from "./StatusBadge";

function ReportCard({ report, onChanged }: { report: Report; onChanged: (r: Report) => void }) {
  const [note, setNote] = useState("");
  const [busy, setBusy] = useState(false);

  async function decide(decision: ReportDecision) {
    setBusy(true);
    try {
      const { data } = await api.patch<Report>(`/admin/reports/${report.id}`, { decision, note });
      toast.success("Prijava je obrađena.");
      onChanged(data);
    } catch (err) {
      toast.error(errorMessage(err));
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className={`${cardCls} space-y-3 p-4`}>
      <div className="flex flex-wrap items-center gap-2 text-sm">
        <span className="font-semibold">#{report.id}</span>
        <ReportStatusBadge status={report.status} />
        <span className="rounded-full bg-neutral-100 px-2 py-0.5 text-xs font-medium dark:bg-neutral-800">{report.targetType === "POST" ? "Objava" : "Komentar"}</span>
        <span className="text-neutral-500">{categoryLabel(report.category)} · prijavio @{report.reporterUsername ?? "?"} · {dateTime(report.createdAt)}</span>
      </div>

      <div className="flex gap-3 rounded-lg bg-neutral-50 p-3 dark:bg-neutral-900">
        {report.imageUrl && <img src={report.imageUrl} alt="" className="h-20 w-20 rounded object-cover" />}
        <div className="min-w-0 text-sm">
          {report.targetAvailable ? (
            <>
              <p className="text-xs text-neutral-500">Autor: @{report.targetAuthorUsername ?? "?"}</p>
              <p className="whitespace-pre-wrap break-words">{report.preview || <em className="text-neutral-500">(bez teksta)</em>}</p>
              {report.postId && <Link to={`/post/${report.postId}`} className="text-xs font-semibold text-accent">Otvori objavu</Link>}
            </>
          ) : (
            <p className="text-neutral-500">Sadržaj je već uklonjen.</p>
          )}
        </div>
      </div>

      {report.description && <p className="text-sm"><span className="font-semibold">Opis prijave:</span> {report.description}</p>}

      {report.status === "PENDING" ? (
        <div className="space-y-2">
          <input className={inputCls} placeholder="Napomena (opciono, ide u audit log; za uklanjanje sadržaja preporučena)" value={note} maxLength={500} onChange={(e) => setNote(e.target.value)} />
          <div className="flex flex-wrap gap-2">
            <button className={btnSecondary} disabled={busy} onClick={() => decide("DISMISS")}>Odbaci prijavu</button>
            <button className={btnSecondary} disabled={busy} onClick={() => decide("REVIEW")}>Označi kao obrađenu</button>
            {report.targetAvailable && <button className={btnDanger} disabled={busy} onClick={() => decide("REMOVE_CONTENT")}>Ukloni sadržaj</button>}
          </div>
        </div>
      ) : (
        <p className="text-xs text-neutral-500">Obradio @{report.reviewedByUsername ?? "?"} · {dateTime(report.reviewedAt)}</p>
      )}
    </div>
  );
}

export default function AdminReportsPage() {
  const [status, setStatus] = useState<ReportStatus | "">("PENDING");
  const [reports, setReports] = useState<Report[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async (st: ReportStatus | "") => {
    setLoading(true);
    setError(null);
    try {
      const { data } = await api.get<Report[]>("/admin/reports", { params: { status: st || undefined, size: 50 } });
      setReports(data);
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { load(status); }, [status, load]);

  return (
    <div className="space-y-4">
      <select className={`${inputCls} max-w-[220px]`} value={status} onChange={(e) => setStatus(e.target.value as ReportStatus | "")}>
        <option value="PENDING">Na čekanju</option>
        <option value="REVIEWED">Obrađene</option>
        <option value="DISMISSED">Odbačene</option>
        <option value="">Sve prijave</option>
      </select>
      {error && <ErrorText>{error}</ErrorText>}
      {loading && <Spinner />}
      {!loading && reports.length === 0 && <EmptyState>Nema prijava.</EmptyState>}
      <div className="space-y-4">
        {reports.map((r) => (
          <ReportCard key={r.id} report={r}
            onChanged={(updated) => setReports((list) => (status === "PENDING" ? list.filter((x) => x.id !== updated.id) : list.map((x) => (x.id === updated.id ? updated : x))))} />
        ))}
      </div>
    </div>
  );
}
