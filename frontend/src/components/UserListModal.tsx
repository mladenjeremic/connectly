import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api, errorMessage } from "../api/client";
import type { UserSummary } from "../types";
import FollowButton from "./FollowButton";
import { Avatar, EmptyState, ErrorText, Modal, Spinner } from "./ui";

interface Props {
  userId: number;
  kind: "followers" | "following";
  onClose: () => void;
}

export default function UserListModal({ userId, kind, onClose }: Props) {
  const [users, setUsers] = useState<UserSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api.get<UserSummary[]>(`/users/${userId}/${kind}`, { params: { size: 50 } })
      .then((res) => setUsers(res.data))
      .catch((err) => setError(errorMessage(err)))
      .finally(() => setLoading(false));
  }, [userId, kind]);

  return (
    <Modal title={kind === "followers" ? "Pratioci" : "Praćeni"} onClose={onClose}>
      <div className="p-4">
        {loading && <Spinner />}
        {error && <ErrorText>{error}</ErrorText>}
        {!loading && !error && users.length === 0 && <EmptyState>Lista je prazna.</EmptyState>}
        <div className="space-y-3">
          {users.map((u) => (
            <div key={u.id} className="flex items-center gap-3 rounded-xl p-1.5 hover:bg-neutral-50 dark:hover:bg-neutral-900">
              <Link to={`/profile/${u.username}`} onClick={onClose}><Avatar url={u.profileImageUrl} name={u.username} size={44} id={u.id} /></Link>
              <div className="min-w-0 flex-1 text-sm leading-tight">
                <Link to={`/profile/${u.username}`} onClick={onClose} className="block truncate font-semibold">{u.username}</Link>
                <span className="block truncate text-neutral-500">{u.fullName}</span>
              </div>
              {!u.self && (
                <FollowButton userId={u.id} following={u.followedByMe}
                  onChange={(r) => setUsers((list) => list.map((x) => (x.id === u.id ? { ...x, followedByMe: r.following } : x)))} />
              )}
            </div>
          ))}
        </div>
      </div>
    </Modal>
  );
}
