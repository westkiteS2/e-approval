import { api, ApiError } from '@/lib/api-client'
import type { Approver, DocumentDetail, DocumentPayload, DocumentStatus, DocumentSummary } from './types'

/** 파일 업로드용 요청 (Content-Type 은 브라우저가 boundary 와 함께 자동 설정) */
async function sendMultipart<T>(url: string, payload: DocumentPayload, files: File[]): Promise<T> {
  const form = new FormData()
  form.append('data', new Blob([JSON.stringify(payload)], { type: 'application/json' }))
  files.forEach((f) => form.append('files', f))

  const res = await fetch(url, { method: 'POST', body: form, credentials: 'include' })
  if (!res.ok) {
    let message = '저장에 실패했습니다.'
    try {
      const body = await res.json()
      if (body?.message) message = body.message
    } catch {
      if (res.status === 401) message = '로그인이 필요합니다.'
      if (res.status === 413) message = '첨부파일 용량이 너무 큽니다.'
    }
    throw new ApiError(res.status, message)
  }
  return (await res.json()) as T
}

export const documentApi = {
  myDocuments: (status?: DocumentStatus) =>
    api<DocumentSummary[]>(`/api/documents${status ? `?status=${status}` : ''}`),

  inbox: () => api<DocumentSummary[]>('/api/documents/inbox'),

  detail: (id: number) => api<DocumentDetail>(`/api/documents/${id}`),

  create: (payload: DocumentPayload, files: File[]) =>
    sendMultipart<DocumentDetail>('/api/documents', payload, files),

  update: (id: number, payload: DocumentPayload, files: File[]) =>
    sendMultipart<DocumentDetail>(`/api/documents/${id}`, payload, files),

  approve: (id: number, comment: string) => api<DocumentDetail>(`/api/documents/${id}/approve`, { method: 'POST', body: JSON.stringify({ comment }) }),
  reject: (id: number, comment: string) => api<DocumentDetail>(`/api/documents/${id}/reject`, { method: 'POST', body: JSON.stringify({ comment }) }),

  withdraw: (id: number) => api<DocumentDetail>(`/api/documents/${id}/withdraw`, { method: 'POST' }),

  remove: (id: number) => api<void>(`/api/documents/${id}`, { method: 'DELETE' }),

  attachmentUrl: (docId: number, attachmentId: number) =>
    `/api/documents/${docId}/attachments/${attachmentId}`,
}

export const approvalLineApi = {
  defaultLine: () => api<Approver[]>('/api/approval-line/default'),
  search: (keyword: string) =>
    api<Approver[]>(`/api/approval-line/approvers?keyword=${encodeURIComponent(keyword)}`),
}
