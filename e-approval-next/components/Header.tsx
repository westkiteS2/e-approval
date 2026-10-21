"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { logout, useAuthState } from "@/lib/auth";
import NotificationBell from "./NotificationBell";

type Props = { variant?: "home" | "auth" | "default" };

export default function Header({ variant = "default" }: Props) {
  const router = useRouter();
  const { user, isLoggedIn, loading } = useAuthState();

  async function handleLogout() {
    await logout();
    router.push("/");
    router.refresh();
  }

  if (variant === "auth") {
    return (
      <header className="header">
        <Link href="/" className="logo">전자결재 시스템</Link>
        <nav>
          <Link href="/auth">로그인</Link>
          <Link href="/signup">회원가입</Link>
        </nav>
      </header>
    );
  }

  return (
    <header className="header">
      <Link href="/" className="logo">전자결재 시스템</Link>
      <nav>
        {!loading && !isLoggedIn && <Link href="/auth">로그인</Link>}
        {isLoggedIn && user && (
          <>
            <span className="header-user-label">{user.department} · {user.positionLabel} {user.name}님</span>
            <NotificationBell />
            {(user.role === "ADMIN" || user.role === "SUPER_ADMIN") && <Link href="/admin">조직 관리</Link>}
            <Link href="/mypage">마이페이지</Link>
            <a href="/auth" onClick={(event) => { event.preventDefault(); void handleLogout(); }}>로그아웃</a>
          </>
        )}
      </nav>
    </header>
  );
}
