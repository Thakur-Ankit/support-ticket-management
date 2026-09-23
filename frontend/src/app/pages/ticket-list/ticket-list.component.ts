import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TicketService } from '../../services/ticket.service';
import { parseApiError } from '../../services/api-error';
import {
  PRIORITIES,
  STATUSES,
  TicketStatus,
  TicketSummary,
} from '../../models/ticket.models';

@Component({
  selector: 'app-ticket-list',
  templateUrl: './ticket-list.component.html',
  styleUrls: ['./ticket-list.component.css'],
})
export class TicketListComponent implements OnInit {
  readonly statuses = STATUSES;
  readonly priorities = PRIORITIES;

  items: TicketSummary[] = [];
  keyword = '';
  statusFilter: TicketStatus | '' = '';
  loading = false;
  errorMessage = '';
  filtersApplied = false;

  constructor(
    private readonly tickets: TicketService,
    private readonly route: ActivatedRoute,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.route.queryParamMap.subscribe((params) => {
      this.keyword = params.get('keyword') || '';
      const status = params.get('status') || '';
      this.statusFilter = (STATUSES as string[]).includes(status)
        ? (status as TicketStatus)
        : '';
      this.filtersApplied = !!(this.keyword.trim() || this.statusFilter);
      this.load();
    });
  }

  load(): void {
    this.loading = true;
    this.errorMessage = '';
    this.tickets.list(this.keyword, this.statusFilter).subscribe({
      next: (res) => {
        this.items = res.items || [];
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.items = [];
        this.errorMessage = parseApiError(err).message;
      },
    });
  }

  applyFilters(): void {
    const queryParams: Record<string, string> = {};
    if (this.keyword.trim()) {
      queryParams['keyword'] = this.keyword.trim();
    }
    if (this.statusFilter) {
      queryParams['status'] = this.statusFilter;
    }
    this.router.navigate(['/tickets'], { queryParams });
  }

  clearFilters(): void {
    this.keyword = '';
    this.statusFilter = '';
    this.router.navigate(['/tickets']);
  }
}
