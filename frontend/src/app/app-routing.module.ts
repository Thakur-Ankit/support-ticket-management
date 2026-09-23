import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TicketListComponent } from './pages/ticket-list/ticket-list.component';
import { TicketCreateComponent } from './pages/ticket-create/ticket-create.component';
import { TicketDetailComponent } from './pages/ticket-detail/ticket-detail.component';
import { TicketEditComponent } from './pages/ticket-edit/ticket-edit.component';

const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'tickets' },
  { path: 'tickets', component: TicketListComponent },
  { path: 'tickets/new', component: TicketCreateComponent },
  { path: 'tickets/:id/edit', component: TicketEditComponent },
  { path: 'tickets/:id', component: TicketDetailComponent },
  { path: '**', redirectTo: 'tickets' },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
