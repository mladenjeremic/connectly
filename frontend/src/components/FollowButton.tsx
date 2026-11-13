import { useState } from "react";
import { toast } from "sonner";
import { api, errorMessage } from "../api/client";
import { btnPrimary, btnSecondary } from "../lib/styles";
import type { FollowResult } from "../types";

interface Props {
  userId: number;
  following: boolean;
  onChange?: (result: FollowResult) => void;
  /** kompaktan tekstualni stil za sidebar */
  link?: boolean;
}

export default function FollowButton({ userId, following, onChange, link = false }: Props) {
  const [busy, setBusy] = useState(false);

  async function toggle() {
    setBusy(true);
    try {
      const { data } = following
        ? await api.delete<FollowResult>(`/users/${userId}/follow`)
        : await api.post<FollowResult>(`/users/${userId}/follow`);
      onChange?.(data);
    } catch (err) {
      toast.error(errorMessage(err));
    } finally {
      setBusy(false);
    }
  }

  if (link) {
    return (
      <button onClick={toggle} disabled={busy}
        className={`rounded-full px-3 py-1 text-xs font-semibold transition disabled:opacity-50 ${following
          ? "bg-neutral-100 text-neutral-600 hover:bg-neutral-200 dark:bg-neutral-800 dark:text-neutral-300"
          : "bg-accent-soft text-accent hover:bg-indigo-100 dark:bg-indigo-950/50 dark:hover:bg-indigo-950"}`}>
        {following ? "Otprati" : "Zaprati"}
      </button>
    );
  }
  return (
    <button onClick={toggle} disabled={busy} className={following ? btnSecondary : btnPrimary}>
      {following ? "Otprati" : "Zaprati"}
    </button>
  );
}
