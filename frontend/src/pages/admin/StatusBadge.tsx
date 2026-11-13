import type { AccountStatus, ReportStatus } from "../../types";

const ACCOUNT: Record<AccountStatus, { label: string; cls: string }> = {
  ACTIVE: { label: "Aktivan", cls: "bg-emerald-100 text-emerald-700 dark:bg-emerald-950 dark:text-emerald-300" },
  SUSPENDED: { label: "Suspendovan", cls: "bg-amber-100 text-amber-700 dark:bg-amber-950 dark:text-amber-300" },
  BANNED: { label: "Trajno blokiran", cls: "bg-red-100 text-red-700 dark:bg-red-950 dark:text-red-300" },
  DEACTIVATED: { label: "Deaktiviran", cls: "bg-neutral-200 text-neutral-700 dark:bg-neutral-800 dark:text-neutral-300" },
};

const REPORT: Record<ReportStatus, { label: string; cls: string }> = {
  PENDING: { label: "Na čekanju", cls: "bg-amber-100 text-amber-700 dark:bg-amber-950 dark:text-amber-300" },
  REVIEWED: { label: "Obrađena", cls: "bg-emerald-100 text-emerald-700 dark:bg-emerald-950 dark:text-emerald-300" },
  DISMISSED: { label: "Odbačena", cls: "bg-neutral-200 text-neutral-700 dark:bg-neutral-800 dark:text-neutral-300" },
};

function Pill({ label, cls }: { label: string; cls: string }) {
  return <span className={`inline-block whitespace-nowrap rounded-full px-2.5 py-0.5 text-xs font-semibold ${cls}`}>{label}</span>;
}

export function AccountStatusBadge({ status }: { status: AccountStatus }) {
  return <Pill {...ACCOUNT[status]} />;
}

export function ReportStatusBadge({ status }: { status: ReportStatus }) {
  return <Pill {...REPORT[status]} />;
}
