"use client";
import { useCallback, useEffect, useState } from "react";
import Link from "next/link";
import { apiRequest, User, useAuthState } from "@/lib/auth";
const POSITIONS=[{value:"STAFF",label:"사원"},{value:"TEAM_LEADER",label:"팀장"},{value:"DIRECTOR",label:"본부장"},{value:"VICE_CEO",label:"부대표"}];
export default function AdminPage(){
 const {user,loading}=useAuthState(); const [users,setUsers]=useState<User[]>([]);const [error,setError]=useState("");const [busy,setBusy]=useState<number|null>(null);
 const load=useCallback(async()=>{try{setUsers(await apiRequest<User[]>("/api/admin/users/all"));}catch(e){setError(e instanceof Error?e.message:"조회 실패");}},[]);
 useEffect(()=>{if(user&&(user.role==="ADMIN"||user.role==="SUPER_ADMIN"))void load();},[user,load]);
 async function change(id:number,field:"position"|"role",value:string){setBusy(id);setError("");try{await apiRequest(`/api/admin/users/${id}/${field}`,{method:"PATCH",body:JSON.stringify({[field]:value})});await load();}catch(e){setError(e instanceof Error?e.message:"변경 실패");}finally{setBusy(null);}}
 async function decide(id:number,action:"approve"|"reject"){setBusy(id);setError("");try{await apiRequest(`/api/admin/users/${id}/${action}`,{method:"PATCH"});await load();}catch(e){setError(e instanceof Error?e.message:"처리 실패");}finally{setBusy(null);}}
 if(loading)return <main style={{padding:30}}>권한 확인 중...</main>;
 if(!user||user.role==="USER")return <main style={{padding:30}}>관리자만 접근할 수 있습니다. <Link href="/auth">로그인</Link></main>;
 return <main style={{maxWidth:1100,margin:"36px auto",padding:20}}><h1>조직 및 계정 관리</h1><p>대표만 관리자 권한을 부여하거나 회수할 수 있습니다. 대표 직책은 변경할 수 없습니다.</p>
 {error&&<p role="alert" style={{color:"crimson"}}>{error}</p>}
 <div style={{overflowX:"auto"}}><table style={{width:"100%",borderCollapse:"collapse",marginTop:20}}><thead><tr>{["이름 / ID","부서","상태","직책","시스템 권한","가입 승인"].map(x=><th key={x} style={{padding:12,borderBottom:"1px solid #aaa",textAlign:"left"}}>{x}</th>)}</tr></thead><tbody>
 {users.map(u=><tr key={u.id}><td style={{padding:10}}>{u.name}<small style={{display:"block"}}>{u.loginId}</small></td><td>{u.department}</td><td>{u.status}</td>
 <td>{u.role==="SUPER_ADMIN"?"대표":<select aria-label={`${u.name} 직책`} value={u.position??"STAFF"} disabled={busy===u.id} onChange={e=>void change(u.id,"position",e.target.value)}>{POSITIONS.filter(x=>x.value!=="VICE_CEO"||user.role==="SUPER_ADMIN").map(x=><option key={x.value} value={x.value}>{x.label}</option>)}</select>}</td>
 <td>{u.role==="SUPER_ADMIN"?"최고 관리자":user.role==="SUPER_ADMIN"?<select aria-label={`${u.name} 권한`} value={u.role} disabled={busy===u.id||u.status!=="ACTIVE"} onChange={e=>void change(u.id,"role",e.target.value)}><option value="USER">일반 사용자</option><option value="ADMIN">관리자</option></select>:u.role==="ADMIN"?"관리자":"일반 사용자"}</td>
 <td>{u.status==="PENDING"?<><button disabled={busy===u.id} onClick={()=>void decide(u.id,"approve")}>승인</button> <button disabled={busy===u.id} onClick={()=>void decide(u.id,"reject")}>거절</button></>:"-"}</td></tr>)}
 </tbody></table></div></main>;
}
