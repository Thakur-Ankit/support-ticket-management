import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { TicketService } from '../../services/ticket.service';
import { parseApiError } from '../../services/api-error';
import { PRIORITIES, Priority } from '../../models/ticket.models';

@Component({
  selector: 'app-ticket-create',
  templateUrl: './ticket-create.component.html',
  styleUrls: ['./ticket-create.component.css'],
})
export class TicketCreateComponent {
  readonly priorities = PRIORITIES;

  title = '';
  description = '';
  priority: Priority = 'MEDIUM';
  assignee = '';
  submitting = false;
  formError = '';
  fieldErrors: Record<string, string> = {};

  constructor(
    private readonly tickets: TicketService,
    private readonly router: Router
  ) {}

  submit(): void {
    this.submitting = true;
    this.formError = '';
    this.fieldErrors = {};

    const payload: {
      title: string;
      description: string;
      priority: Priority;
      assignee?: string;
    } = {
      title: this.title,
      description: this.description,
      priority: this.priority,
    };
    if (this.assignee.trim()) {
      payload.assignee = this.assignee.trim();
    }

    this.tickets.create(payload).subscribe({
      next: (created) => {
        this.submitting = false;
        this.router.navigate(['/tickets', created.id]);
      },
      error: (err) => {
        this.submitting = false;
        const parsed = parseApiError(err);
        this.formError = parsed.message;
        this.fieldErrors = parsed.fieldErrors;
      },
    });
  }
}
