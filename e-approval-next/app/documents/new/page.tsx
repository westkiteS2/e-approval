"use client";
import { Suspense } from "react";
import Header from "@/components/Header";
import Footer from "@/components/Footer";
import DocumentWritePage from "@/features/approval/pages/DocumentWritePage";
export default function NewDocumentRoute() { return <><Header /><Suspense fallback={<main className="main">작성 화면을 불러오는 중...</main>}><DocumentWritePage /></Suspense><Footer /></>; }
