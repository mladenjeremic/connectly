import { createContext, useCallback, useContext, useEffect, useState, type ReactNode } from "react";
import { api } from "../api/client";
import { startPolling } from "../lib/poll";

interface Value { unread: number; refresh: () => void }
const NotificationsContext = createContext<Value>({ unread: 0, refresh: () => undefined });

/** Broj nepročitanih notifikacija (osvežava se na 30 s, bez WebSocket-a u MVP verziji). */
export function NotificationsProvider({ children }: { children: ReactNode }) {
  const [unread, setUnread] = useState(0);

  const refresh = useCallback(() => {
    api.get<{ count: number }>("/notifications/unread-count")
      .then((res) => setUnread(res.data.count))
      .catch(() => undefined);
  }, []);

  useEffect(() => {
    refresh();
    return startPolling(refresh, 30000);
  }, [refresh]);

  return <NotificationsContext.Provider value={{ unread, refresh }}>{children}</NotificationsContext.Provider>;
}

// eslint-disable-next-line react-refresh/only-export-components
export function useNotifications(): Value {
  return useContext(NotificationsContext);
}
