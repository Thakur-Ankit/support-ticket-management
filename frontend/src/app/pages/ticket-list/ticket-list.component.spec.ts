import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { of } from 'rxjs';
import { TicketListComponent } from './ticket-list.component';
import { environment } from '../../../environments/environment';

describe('TicketListComponent', () => {
  let fixture: ComponentFixture<TicketListComponent>;
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FormsModule, HttpClientTestingModule, RouterTestingModule],
      declarations: [TicketListComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            queryParamMap: of(convertToParamMap({})),
          },
        },
      ],
    }).compileComponents();

    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(TicketListComponent);
    fixture.detectChanges();
  });

  afterEach(() => http.verify());

  it('loads tickets from API', () => {
    const req = http.expectOne(`${environment.apiBaseUrl}/api/v1/tickets`);
    expect(req.request.method).toBe('GET');
    req.flush({
      items: [
        {
          id: '1',
          title: 'Printer',
          status: 'OPEN',
          priority: 'LOW',
          assignee: null,
          createdAt: '2026-09-23T10:00:00Z',
        },
      ],
    });
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Printer');
  });

  it('shows no-matches empty state when filters applied and list empty', () => {
    const req = http.expectOne(`${environment.apiBaseUrl}/api/v1/tickets`);
    req.flush({ items: [] });

    const component = fixture.componentInstance;
    component.filtersApplied = true;
    component.loading = false;
    component.items = [];
    component.errorMessage = '';
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('No matches');
  });
});
