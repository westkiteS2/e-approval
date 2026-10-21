"use client";

import { useEffect, useState } from "react";

export type User = {
  id: number;
  loginId: string;
  name: string;
  department: string;
  position: string | null;
  positionLabel: string;
  role: "USER" | "ADMIN" | "SUPER_ADMIN";
  status: "PENDING" | "ACTIVE" | "REJECTED";
  createdAt: string;
};

const EVENT = "auth-change";
let authRevision = 0;
const API_BASE = "";

function notifyAuthChanged() {
  authRevision += 1;
  if (typeof window !== "undefined") window.dispatchEvent(new Event(EVENT));
}

function readCachedUser(): User | null {
  try {
    const value = localStorage.getItem("approval-user");
    return value ? (JSON.parse(value) as User) : null;
  } catch {
    return null;
  }
}

export async function apiRequest<T>(path: string, init: RequestInit = {}): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    ...init,
    credentials: "include",
    headers: { "Content-Type": "application/json", ...init.headers },
  });

  if (!response.ok) {
    let message = "요청에 실패했습니다.";
    try {
      const body = await response.json();
      if (body?.message) message = body.message;
    } catch {
      if (response.status === 401) message = "로그인이 필요합니다.";
      if (response.status === 403) message = "권한이 없거나 관리자 승인 대기 중인 계정입니다.";
    }
    throw new Error(message);
  }

  const text = await response.text();
  return (text ? JSON.parse(text) : undefined) as T;
}

export async function login(loginId: string, password: string): Promise<User> {
  const user = await apiRequest<User>("/api/auth/login", {
    method: "POST",
    body: JSON.stringify({ loginId, password }),
  });
  localStorage.setItem("approval-user", JSON.stringify(user));
  notifyAuthChanged();
  return user;
}

export type SignupFormData = {
  loginId: string;
  password: string;
  name: string;
  department: string;
  position: string;
};

export async function signup(form: SignupFormData): Promise<{ message: string; user: User }> {
  return apiRequest("/api/auth/signup", {
    method: "POST",
    body: JSON.stringify(form),
  });
}

export async function logout(): Promise<void> {
  try {
    await apiRequest<void>("/api/auth/logout", { method: "POST" });
  } finally {
    localStorage.removeItem("approval-user");
    notifyAuthChanged();
  }
}

/** 서버 세션을 확인해 새로고침 후에도 실제 로그인 상태를 복구한다. */
export function useAuthState() {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;
    const revisionAtStart = authRevision;
    const syncFromCache = () => {
      if (active) setUser(readCachedUser());
    };
    window.addEventListener(EVENT, syncFromCache);
    window.addEventListener("storage", syncFromCache);

    apiRequest<User>("/api/auth/me")
      .then((currentUser) => {
        if (!active || revisionAtStart !== authRevision) return;
        localStorage.setItem("approval-user", JSON.stringify(currentUser));
        setUser(currentUser);
      })
      .catch(() => {
        if (!active || revisionAtStart !== authRevision) return;
        localStorage.removeItem("approval-user");
        setUser(null);
      })
      .finally(() => {
        if (active) setLoading(false);
      });

    return () => {
      active = false;
      window.removeEventListener(EVENT, syncFromCache);
      window.removeEventListener("storage", syncFromCache);
    };
  }, []);

  return { user, isLoggedIn: Boolean(user), loading };
}
