"use client";
import { useEffect, useState } from 'react';
import Link from 'next/link';
import { notificationApi, type NotificationItem } from '@/lib/notifications';
export default function NotificationBell() {
 const [items,setItems]=useState<NotificationItem[]>([]);
 const [open,setOpen]=useState(false);
 useEffect(()=>{
   let active=true;
   const refresh=()=>notificationApi.list().then(data=>{if(active)setItems(data)}).catch(()=>{});
   refresh(); const timer=setInterval(refresh,30000);
   return ()=>{active=false;clearInterval(timer)};
 },[]);
 const unread=items.filter(n=>!n.read).length;
 async function read(id:number){try{await notificationApi.read(id);setItems(prev=>prev.map(n=>n.id===id?{...n,read:true}:n))}catch{}}
 return <div className="notification-wrap">
  <button type="button" className="notification-toggle" onClick={()=>setOpen(v=>!v)} aria-label={`알림 ${unread}개`}>🔔 알림 {unread>0&&<span className="notification-count">{unread}</span>}</button>
  {open&&<div className="notification-panel" role="region" aria-label="알림 목록">
    <strong>최근 알림</strong>{items.length===0?<p>알림이 없습니다.</p>:items.map(n=><div key={n.id} className={`notification-item ${n.read?'':'notification-unread'}`}>
      <Link href={n.documentId?`/documents/${n.documentId}`:'/documents'} onClick={()=>{void read(n.id);setOpen(false)}}>{n.message}</Link>
      <small>{new Date(n.createdAt).toLocaleString('ko-KR')}</small>
      {!n.read&&<button onClick={()=>void read(n.id)}>읽음</button>}
    </div>)}
  </div>}
 </div>;
}
