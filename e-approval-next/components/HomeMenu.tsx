"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useAuthState } from "@/lib/auth";

export default function HomeMenu() {
  const router = useRouter();
  const { isLoggedIn, loading } = useAuthState();

  function handleProtectedClick(event: React.MouseEvent<HTMLAnchorElement>) {
    if (!isLoggedIn) {
      event.preventDefault();
      alert("로그인이 필요한 서비스입니다.");
      router.push("/auth");
    }
  }

  return (
    <>
      <div className={isLoggedIn ? "login-notice hidden" : "login-notice"}>
        <p>{loading ? "로그인 상태를 확인하고 있어요." : "로그인 후 사용 가능합니다."}</p>
        {!isLoggedIn && <Link href="/auth">로그인하기</Link>}
      </div>
      <div className="menu-grid">
        <Link href="/documents/new" className="menu-box send-box protected-menu" onClick={handleProtectedClick}>
          <h2>문서 작성</h2><p>기안서·지출결의서·휴가신청서를 작성하고 결재를 요청합니다.</p>
        </Link>
        <Link href="/documents" className="menu-box document-box protected-menu" onClick={handleProtectedClick}>
          <h2>전송 확인하기</h2><p>전송된 결재 문서를 확인합니다.</p>
        </Link>
        <Link href="/documents?tab=INBOX" className="menu-box approval-box protected-menu" onClick={handleProtectedClick}>
          <h2>결재 대기 문서</h2><p>내가 결재할 차례인 문서를 확인합니다.</p>
        </Link>
      </div>
    </>
  );
}
