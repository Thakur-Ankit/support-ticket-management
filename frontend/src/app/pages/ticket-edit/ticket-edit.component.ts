import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TicketService } from '../../services/ticket.service';
import { parseApiError } from '../../services/api-error';
import { PRIORITIES, Priority, TicketDetail } from '../../models/ticket.models';

@Component({
  selector: 'app-ticket-edit',
  templateUrl: './ticket-edit.component.html',
  styleUrls: ['./ticket-edit.component.css'],
})
export class TicketEditComponent implements OnInit {
  readonly priorities = PRIORITIES;

  ticketId = '';
  title = '';
  description = '';
  priority: Priority = 'MEDIUM';
  assignee = '';
  statusLabel = '';
  loading = true;
  submitting = false;
  errorMessage = '';
  formError = '';
  fieldErrors: Record<string, string> = {};

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
      this.ticketId = id;
      this.load(id);
    });
  }

  load(id: string): void {
    this.loading = true;
    this.errorMessage = '';
    this.tickets.get(id).subscribe({
      next: (ticket) => {
        this.applyTicket(ticket);
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        const parsed = parseApiError(err);
        this.errorMessage =
          parsed.code === 'NOT_FOUND' ? 'Ticket not found.' : parsed.message;
      },
    });
  }

  submit(): void {
    this.submitting = true;
    this.formError = '';
    this.fieldErrors = {};

    const assigneeValue = this.assignee.trim() === '' ? null : this.assignee.trim();

    this.tickets
      .patch(this.ticketId, {
        title: this.title,
        description: this.description,
        priority: this.priority,
        assignee: assigneeValue,
      })
      .subscribe({
        next: (updated) => {
          this.submitting = false;
          this.router.navigate(['/tickets', updated.id]);
        },
        error: (err) => {
          this.submitting = false;
          const parsed = parseApiError(err);
          this.formError = parsed.message;
          this.fieldErrors = parsed.fieldErrors;
        },
      });
  }

  private applyTicket(ticket: TicketDetail): void {
    this.title = ticket.title;
    this.description = ticket.description;
    this.priority = ticket.priority;
    this.assignee = ticket.assignee || '';
    this.statusLabel = ticket.status;
  }
}
