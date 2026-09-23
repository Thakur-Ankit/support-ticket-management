import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  Comment,
  CreateTicketPayload,
  PatchTicketPayload,
  TicketDetail,
  TicketListResponse,
  TicketStatus,
} from '../models/ticket.models';

@Injectable({ providedIn: 'root' })
export class TicketService {
  private readonly baseUrl = `${environment.apiBaseUrl}/api/v1/tickets`;

  constructor(private readonly http: HttpClient) {}

  create(payload: CreateTicketPayload): Observable<TicketDetail> {
    return this.http.post<TicketDetail>(this.baseUrl, payload);
  }

  list(keyword?: string, status?: TicketStatus | ''): Observable<TicketListResponse> {
    let params = new HttpParams();
    if (keyword != null && keyword.trim() !== '') {
      params = params.set('keyword', keyword.trim());
    }
    if (status) {
      params = params.set('status', status);
    }
    return this.http.get<TicketListResponse>(this.baseUrl, { params });
  }

  get(id: string): Observable<TicketDetail> {
    return this.http.get<TicketDetail>(`${this.baseUrl}/${id}`);
  }

  patch(id: string, payload: PatchTicketPayload): Observable<TicketDetail> {
    return this.http.patch<TicketDetail>(`${this.baseUrl}/${id}`, payload);
  }

  changeStatus(id: string, status: TicketStatus): Observable<TicketDetail> {
    return this.http.post<TicketDetail>(`${this.baseUrl}/${id}/status`, { status });
  }

  addComment(id: string, content: string, author: string): Observable<Comment> {
    return this.http.post<Comment>(`${this.baseUrl}/${id}/comments`, { content, author });
  }
}
