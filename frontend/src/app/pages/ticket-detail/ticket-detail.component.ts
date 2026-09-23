import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TicketService } from '../../services/ticket.service';
import { parseApiError } from '../../services/api-error';
import {
  TicketDetail,
  TicketStatus,
  allowedNextStatuses,
  commentsAllowed,
} from '../../models/ticket.models';

@Component({
  selector: 'app-ticket-detail',
  templateUrl: './ticket-detail.component.html',
  styleUrls: ['./ticket-detail.component.css'],
})
export class TicketDetailComponent implements OnInit {
  ticket: TicketDetail | null = null;
  loading = true;
  errorMessage = '';
  actionError = '';
  statusBusy = false;

  commentContent = '';
  commentAuthor = '';
  commentBusy = false;
  commentError = '';
  commentFieldErrors: Record<string, string> = {};

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly tickets: TicketService
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      const id = params.get('id');
      if (!id) {
        this.router.navigate(['/tickets']);
        return;
      }
      this.load(id);
    });
  }

  get nextStatuses(): TicketStatus[] {
    return this.ticket ? allowedNextStatuses(this.ticket.status) : [];
  }

  get canComment(): boolean {
    return !!this.ticket && commentsAllowed(this.ticket.status);
  }

  load(id: string): void {
    this.loading = true;
    this.errorMessage = '';
    this.actionError = '';
    this.tickets.get(id).subscribe({
      next: (ticket) => {
        this.ticket = ticket;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.ticket = null;
        const parsed = parseApiError(err);
        this.errorMessage =
          parsed.code === 'NOT_FOUND' ? 'Ticket not found.' : parsed.message;
      },
    });
  }

  changeStatus(target: TicketStatus): void {
    if (!this.ticket || this.statusBusy) {
      return;
    }
    this.statusBusy = true;
    this.actionError = '';
    const previous = this.ticket.status;
    this.tickets.changeStatus(this.ticket.id, target).subscribe({
      next: (updated) => {
        this.ticket = updated;
        this.statusBusy = false;
      },
      error: (err) => {
        this.statusBusy = false;
        if (this.ticket) {
          this.ticket = { ...this.ticket, status: previous };
        }
        const parsed = parseApiError(err);
        this.actionError = parsed.message;
      },
    });
  }

  addComment(): void {
    if (!this.ticket || this.commentBusy) {
      return;
    }
    this.commentBusy = true;
    this.commentError = '';
    this.commentFieldErrors = {};
    this.tickets.addComment(this.ticket.id, this.commentContent, this.commentAuthor).subscribe({
      next: () => {
        this.commentBusy = false;
        this.commentContent = '';
        this.commentAuthor = '';
        this.load(this.ticket!.id);
      },
      error: (err) => {
        this.commentBusy = false;
        const parsed = parseApiError(err);
        this.commentError = parsed.message;
        this.commentFieldErrors = parsed.fieldErrors;
      },
    });
  }
}
