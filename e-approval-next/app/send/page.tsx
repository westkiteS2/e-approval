import type { Metadata } from "next";
import Header from "@/components/Header";
import Footer from "@/components/Footer";

export const metadata: Metadata = {
  title: "파일 전송",
};

export default function SendPage() {
  return (
    <>
      <Header />

      <main className="form-main">
        <div className="form-box send-form">
          <h1>파일 전송</h1>
          <form>
            <label htmlFor="receiver">결재자</label>
            <select id="receiver">
              <option>결재자를 선택하세요</option>
              <option>김가연</option>
              <option>천서연</option>
              <option>최수민</option>
            </select>

            <label htmlFor="title">문서 제목</label>
            <input type="text" id="title" placeholder="문서 제목을 입력하세요" />

            <label htmlFor="file">파일 선택</label>
            <input type="file" id="file" />

            <label htmlFor="message">전달 내용</label>
            <textarea id="message" placeholder="전달할 내용을 입력하세요"></textarea>

            <button type="submit" className="main-button">
              파일 전송
            </button>
          </form>
        </div>
      </main>

      <Footer />
    </>
  );
}
