"use client";
import { useEffect, useState } from "react";
import Link from "next/link";
import { apiRequest } from "@/lib/auth";
export default function SetupPage() {
  const [available,setAvailable] = useState(false);
  const [loading,setLoading] = useState(true);
  const [form,setForm] = useState({setupToken:"",loginId:"",password:"",name:"",department:"경영지원팀"});
  const [message,setMessage] = useState("");
  const [done,setDone] = useState(false);
  useEffect(()=>{apiRequest<{available:boolean}>("/api/setup/status").then(x=>setAvailable(x.available)).catch(e=>setMessage(String(e))).finally(()=>setLoading(false));},[]);
  async function submit(e:React.FormEvent) {e.preventDefault();setMessage("");try {await apiRequest("/api/setup/ceo",{method:"POST",body:JSON.stringify(form)});setDone(true);}catch(e){setMessage(e instanceof Error?e.message:"등록 실패");}}
  return <main style={{maxWidth:520,margin:"48px auto",padding:24}}><h1>최초 대표 등록</h1>
    {loading?<p>확인 중...</p>:done?<p>대표 계정이 생성되었습니다. <Link href="/auth">로그인하기</Link></p>:!available?<p>등록이 완료되었거나 서버에 CEO_SETUP_TOKEN이 설정되지 않았습니다.</p>:
    <form onSubmit={submit} style={{display:"grid",gap:12,marginTop:20}}>
    {([ ["setupToken","초기 설정 토큰"],["loginId","대표 로그인 ID"],["password","비밀번호"],["name","대표 이름"],["department","부서"] ] as const).map(([key,label])=><label key={key}>{label}<input style={{display:"block",width:"100%",padding:10,marginTop:5}} type={key==="password"||key==="setupToken"?"password":"text"} minLength={key==="password"?8:undefined} required value={form[key]} onChange={e=>setForm(x=>({...x,[key]:e.target.value}))}/></label>)}
    <button className="main-button" type="submit">대표 계정 만들기</button></form>}
    {message&&<p role="alert">{message}</p>}
  </main>;
}
