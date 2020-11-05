import { AppSharedModule } from './../../app-shared.module';
import { NotificationsRoutingModule } from './notifications-routing.module';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { NotificationSearchDialogComponent } from './notification-search-dialog/notification-search-dialog.component';



@NgModule({
  declarations: 
          [ NotificationsRoutingModule.COMPONENTS, 
            NotificationSearchDialogComponent
          ],
  imports: [
    CommonModule,
    NotificationsRoutingModule,
    AppSharedModule,

  ]
})
export class NotificationsModule { }
