import { AppSharedModule } from './../../app-shared.module';
import { NgModule } from '@angular/core';
import { UserRoutingModule } from './user-routing.module'; 
import { ExpiringModalComponent } from './expiring-modal/expiring-modal.component';
import { AddendumDetailsComponent } from './addendum-details/addendum-details.component';
import { UserFormDialogComponent } from './user-form-dialog/user-form-dialog.component';
import { DeleteDialogComponent } from './delete-dialog/delete-dialog.component';


@NgModule({
  declarations: [
    UserRoutingModule.COMPONENTS,
    ExpiringModalComponent,
    AddendumDetailsComponent,
    UserFormDialogComponent,
    DeleteDialogComponent,
 
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
