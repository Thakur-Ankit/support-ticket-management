import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { of } from 'rxjs';
import { TicketEditComponent } from './ticket-edit.component';
import { environment } from '../../../environments/environment';

describe('TicketEditComponent', () => {
  let fixture: ComponentFixture<TicketEditComponent>;
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FormsModule, HttpClientTestingModule, RouterTestingModule],
      declarations: [TicketEditComponent],
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
    fixture = TestBed.createComponent(TicketEditComponent);
    fixture.detectChanges();
  });

  afterEach(() => http.verify());

  it('loads ticket into the form including CLOSED tickets', () => {
    const req = http.expectOne(`${environment.apiBaseUrl}/api/v1/tickets/abc`);
    req.flush({
      id: 'abc',
      title: 'Closed item',
      description: 'Still editable',
      priority: 'LOW',
      status: 'CLOSED',
      assignee: 'a@b.co',
      createdAt: '2026-09-23T10:00:00Z',
      updatedAt: '2026-09-23T11:00:00Z',
      comments: [],
    });
    fixture.detectChanges();

    const component = fixture.componentInstance;
    expect(component.title).toBe('Closed item');
    expect(component.statusLabel).toBe('CLOSED');
    expect(component.assignee).toBe('a@b.co');
    expect(fixture.nativeElement.textContent).toContain('content editable');
  });
});
