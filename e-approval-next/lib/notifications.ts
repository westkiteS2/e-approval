import { api } from './api-client'
export interface NotificationItem { id: number; documentId: number | null; message: string; read: boolean; createdAt: string }
export const notificationApi = {
  list: () => api<NotificationItem[]>('/api/notifications'),
  read: (id: number) => api<void>(`/api/notifications/${id}/read`, { method: 'PATCH' }),
}
