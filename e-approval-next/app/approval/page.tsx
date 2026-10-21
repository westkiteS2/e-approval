import type { Metadata } from "next";
import Header from "@/components/Header";
import Footer from "@/components/Footer";

export const metadata: Metadata = {
  title: "결재",
};

export default function ApprovalPage() {
  return (
    <>
      <Header />

      <main className="main">
        <h1>결재</h1>

        <div className="approval-page">
          <div className="approval-info">
            <h2>업무보고서.pdf</h2>

            <p>
              <strong>기안자 :</strong> 중부대
            </p>

            <p>
              <strong>결재자 :</strong> 김가연
            </p>

            <p>
              <strong>작성일 :</strong> 2026-09-30
            </p>
          </div>

          <div className="document-preview">
            <h2>문서 내용</h2>

            <div className="document-paper">
              <h3>업무보고서</h3>

              <p>이곳에 결재할 문서의 내용이 표시됩니다.</p>

              <p>현재는 화면 구성을 위한 임시 문서입니다.</p>
            </div>
          </div>

          <div className="approval-actions">
            <button className="approve">승인</button>

            <button className="reject">반려</button>
          </div>
        </div>
      </main>

      <Footer />
    </>
  );
}
