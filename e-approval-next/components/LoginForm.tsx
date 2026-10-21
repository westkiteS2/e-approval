"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { login } from "@/lib/auth";

export default function LoginForm() {
  const router = useRouter();
  const [loginId, setLoginId] = useState("");
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState("");
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    const notice = sessionStorage.getItem("signup-notice");
    if (notice) {
      setMessage(notice);
      sessionStorage.removeItem("signup-notice");
    }
  }, []);

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setMessage("");
    setSubmitting(true);
    try {
      await login(loginId.trim(), password);
      router.push("/");
      router.refresh();
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "로그인에 실패했습니다.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="auth-section">
      <h1>전자결재 시스템</h1>
      <p className="auth-description">사내 계정으로 로그인하세요.</p>
      <form id="login-form" onSubmit={handleSubmit}>
        <label htmlFor="login-id">아이디</label>
        <input id="login-id" value={loginId} onChange={(e) => setLoginId(e.target.value)}
          placeholder="아이디를 입력하세요" autoComplete="username" required />
        <label htmlFor="login-password">비밀번호</label>
        <input id="login-password" type="password" value={password}
          onChange={(e) => setPassword(e.target.value)} placeholder="비밀번호를 입력하세요"
          autoComplete="current-password" required />
        <button type="submit" className="main-button" disabled={submitting}>
          {submitting ? "로그인 중..." : "로그인"}
        </button>
      </form>
      {message && <p className="message" role="status">{message}</p>}
      <p className="form-link">계정이 없나요? <Link href="/signup">회원가입</Link></p>
    </section>
  );
}
