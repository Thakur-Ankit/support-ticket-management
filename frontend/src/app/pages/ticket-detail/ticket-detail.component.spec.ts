import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { of } from 'rxjs';
import { TicketDetailComponent } from './ticket-detail.component';
import { environment } from '../../../environments/environment';

describe('TicketDetailComponent', () => {
  let fixture: ComponentFixture<TicketDetailComponent>;
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FormsModule, HttpClientTestingModule, RouterTestingModule],
      declarations: [TicketDetailComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            paramMap: of(convertToParamMap({ id: 'abc' })),
          },
        },
      ],
    }).compileComponents();

    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(TicketDetailComponent);
    fixture.detectChanges();
  });

  afterEach(() => http.verify());

  function flushTicket(status: string): void {
    const req = http.expectOne(`${environment.apiBaseUrl}/api/v1/tickets/abc`);
    req.flush({
      id: 'abc',
      title: 'Detail',
      description: 'Body',
      priority: 'HIGH',
      status,
      assignee: null,
      createdAt: '2026-09-23T10:00:00Z',
      updatedAt: '2026-09-23T10:00:00Z',
      comments: [],
    });
    fixture.detectChanges();
  }

  it('shows ticket fields and status actions for OPEN', () => {
    flushTicket('OPEN');
    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Detail');
    expect(text).toContain('IN_PROGRESS');
    expect(text).toContain('CANCELLED');
    expect(text).toContain('Add comment');
  });

  it('hides comment form on CLOSED', () => {
    flushTicket('CLOSED');
    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Comments cannot be added');
    expect(text).not.toContain('Add comment');
  });

  it('keeps prior status and shows server message on illegal transition 409', () => {
    flushTicket('OPEN');
    const component = fixture.componentInstance;
    component.changeStatus('CLOSED');
    const statusReq = http.expectOne(`${environment.apiBaseUrl}/api/v1/tickets/abc/status`);
    statusReq.flush(
      {
        status: 409,
        error: 'ILLEGAL_TRANSITION',
        message: 'Illegal status transition from OPEN to CLOSED',
        details: [],
      },
      { status: 409, statusText: 'Conflict' }
    );
    fixture.detectChanges();
    expect(component.ticket?.status).toBe('OPEN');
    expect(fixture.nativeElement.textContent).toContain('Illegal status transition');
  });
});
