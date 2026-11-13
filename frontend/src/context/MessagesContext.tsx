import { createContext, useCallback, useContext, useEffect, useState, type ReactNode } from "react";
import { api } from "../api/client";
import { startPolling } from "../lib/poll";

interface Value { unread: number; refresh: () => void }
const MessagesContext = createContext<Value>({ unread: 0, refresh: () => undefined });

/** Broj nepročitanih poruka (osvežava se na 8 s, bez WebSocket-a u ovoj verziji). */
export function MessagesProvider({ children }: { children: ReactNode }) {
  const [unread, setUnread] = useState(0);

  const refresh = useCallback(() => {
    api.get<{ count: number }>("/messages/unread-count")
      .then((res) => setUnread(res.data.count))
      .catch(() => undefined);
  }, []);

  useEffect(() => {
    refresh();
    return startPolling(refresh, 8000);
  }, [refresh]);

  return <MessagesContext.Provider value={{ unread, refresh }}>{children}</MessagesContext.Provider>;
}

// eslint-disable-next-line react-refresh/only-export-components
export function useMessages(): Value {
  return useContext(MessagesContext);
}
