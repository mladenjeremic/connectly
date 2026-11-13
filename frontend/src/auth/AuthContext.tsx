import { createContext, useCallback, useContext, useEffect, useState, type ReactNode } from "react";
import { api, TOKEN_KEY, USER_KEY } from "../api/client";
import type { AuthResult, Me, Role } from "../types";

interface AuthUser {
  userId: number;
  username: string;
  role: Role;
  profileImageUrl: string | null;
}

interface RegisterData {
  firstName: string;
  lastName: string;
  username: string;
  email: string;
  password: string;
}

interface AuthContextValue {
  user: AuthUser | null;
  isAdmin: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (data: RegisterData) => Promise<void>;
  logout: () => void;
  /** Ponovo učitava podatke trenutnog korisnika (npr. posle promene profilne slike). */
  refreshMe: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

function loadStoredUser(): AuthUser | null {
  try {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? (JSON.parse(raw) as AuthUser) : null;
  } catch {
    return null;
  }
}

function store(u: AuthUser) {
  localStorage.setItem(USER_KEY, JSON.stringify(u));
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(loadStoredUser);

  function persist(res: AuthResult) {
    const u: AuthUser = { userId: res.userId, username: res.username, role: res.role, profileImageUrl: null };
    localStorage.setItem(TOKEN_KEY, res.token);
    store(u);
    setUser(u);
  }

  const refreshMe = useCallback(async () => {
    const { data } = await api.get<Me>("/users/me");
    const u: AuthUser = { userId: data.id, username: data.username, role: data.role, profileImageUrl: data.profileImageUrl };
    store(u);
    setUser(u);
  }, []);

  // Uloga i slika se uvek osvežavaju sa servera; frontend im ne veruje slepo.
  const loggedIn = user !== null;
  useEffect(() => {
    if (loggedIn) refreshMe().catch(() => undefined);
  }, [loggedIn, refreshMe]);

  async function login(email: string, password: string) {
    const { data } = await api.post<AuthResult>("/auth/login", { email, password });
    persist(data);
  }

  async function register(form: RegisterData) {
    const { data } = await api.post<AuthResult>("/auth/register", form);
    persist(data);
  }

  function logout() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    setUser(null);
  }

  return (
    <AuthContext.Provider value={{ user, isAdmin: user?.role === "ADMIN", login, register, logout, refreshMe }}>
      {children}
    </AuthContext.Provider>
  );
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used inside AuthProvider");
  return ctx;
}
