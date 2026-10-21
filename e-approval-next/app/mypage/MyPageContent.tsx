"use client";

import { useAuthState } from "@/lib/auth";

const history = [
  { title: "업무보고서.pdf", status: "waiting", label: "결재 대기" },
  { title: "프로젝트 계획서.pdf", status: "complete", label: "결재 완료" },
  { title: "회의록.pdf", status: "reject", label: "반려" },
];

export default function MyPageContent() {
  const { user, loading } = useAuthState();
  return (
    <>
      <section className="mypage-section">
        <h2>회원정보</h2>
        <div className="info-box">
          {loading ? <p>회원정보를 불러오는 중입니다.</p> : user ? <>
            <p><strong>이름</strong>{user.name}</p>
            <p><strong>아이디</strong>{user.loginId}</p>
            <p><strong>부서</strong>{user.department}</p>
            <p><strong>직책</strong>{user.positionLabel}</p>
            <p><strong>가입 상태</strong>{user.status === "ACTIVE" ? "정상" : user.status === "PENDING" ? "승인 대기" : "승인 거절"}</p>
          </> : <p>로그인이 필요합니다. <a href="/auth">로그인하기</a></p>}
        </div>
      </section>
      <section className="mypage-section">
        <h2>결재 내역</h2>
        <div className="history-box">
          {history.map((item) => <div className="history-item" key={item.title}>
            <span>{item.title}</span><span className={`status ${item.status}`}>{item.label}</span>
          </div>)}
        </div>
      </section>
    </>
  );
}
