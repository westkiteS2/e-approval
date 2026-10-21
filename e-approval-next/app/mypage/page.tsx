import type { Metadata } from "next";
import Header from "@/components/Header";
import Footer from "@/components/Footer";
import MyPageContent from "./MyPageContent";

export const metadata: Metadata = { title: "마이페이지" };

export default function MyPage() {
  return <><Header /><main className="main"><h1>마이페이지</h1><MyPageContent /></main><Footer /></>;
}
