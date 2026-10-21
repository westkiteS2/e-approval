"use client";
import { Suspense } from "react";
import Header from "@/components/Header";
import Footer from "@/components/Footer";
import DocumentListPage from "@/features/approval/pages/DocumentListPage";
export default function DocumentsRoute() { return <><Header /><Suspense fallback={<main className="main">문서함을 불러오는 중...</main>}><DocumentListPage /></Suspense><Footer /></>; }
