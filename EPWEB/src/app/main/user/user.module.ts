import { AppSharedModule } from './../../app-shared.module';
import { NgModule } from '@angular/core';
import { UserRoutingModule } from './user-routing.module'; 
import { ExpiringModalComponent } from './expiring-modal/expiring-modal.component';
import { AddendumDetailsComponent } from './addendum-details/addendum-details.component';

@NgModule({
  declarations: [
    UserRoutingModule.COMPONENTS,
    ExpiringModalComponent,
    AddendumDetailsComponent, 
  ],
  imports: [
    
    AppSharedModule,
    UserRoutingModule,
   
  ],
  entryComponents: [
    ExpiringModalComponent
  ]
})
export class UserModule { }
