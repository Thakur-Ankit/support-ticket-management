export type TicketStatus =
  | 'OPEN'
  | 'IN_PROGRESS'
  | 'RESOLVED'
  | 'CLOSED'
  | 'CANCELLED';

export type Priority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';

export interface TicketSummary {
  id: string;
  title: string;
  status: TicketStatus;
  priority: Priority;
  assignee: string | null;
  createdAt: string;
}

export interface Comment {
  id: string;
  content: string;
  author: string;
  createdAt: string;
}

export interface TicketDetail {
  id: string;
  title: string;
  description: string;
  priority: Priority;
  status: TicketStatus;
  assignee: string | null;
  createdAt: string;
  updatedAt: string;
  comments: Comment[];
}

export interface TicketListResponse {
  items: TicketSummary[];
}

export interface CreateTicketPayload {
  title: string;
  description: string;
  priority: Priority;
  assignee?: string | null;
}

export interface PatchTicketPayload {
  title?: string;
  description?: string;
  priority?: Priority;
  assignee?: string | null;
}

export interface ApiErrorDetail {
  field: string;
  message: string;
}

export interface ApiErrorBody {
  timestamp?: string;
  status: number;
  error: string;
  message: string;
  details?: ApiErrorDetail[];
}

/**
 * Display-only next statuses mirroring the backend state machine.
 * The server remains authoritative; illegal clicks still yield 409.
 */
export function allowedNextStatuses(status: TicketStatus): TicketStatus[] {
  switch (status) {
    case 'OPEN':
      return ['IN_PROGRESS', 'CANCELLED'];
    case 'IN_PROGRESS':
      return ['RESOLVED', 'CANCELLED'];
    case 'RESOLVED':
      return ['CLOSED'];
    default:
      return [];
  }
}

export function commentsAllowed(status: TicketStatus): boolean {
  return status === 'OPEN' || status === 'IN_PROGRESS' || status === 'RESOLVED';
}

export const PRIORITIES: Priority[] = ['LOW', 'MEDIUM', 'HIGH', 'URGENT'];
export const STATUSES: TicketStatus[] = [
  'OPEN',
  'IN_PROGRESS',
  'RESOLVED',
  'CLOSED',
  'CANCELLED',
];
