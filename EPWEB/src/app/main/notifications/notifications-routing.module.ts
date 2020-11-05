import { NotificationSearchDialogComponent } from './notification-search-dialog/notification-search-dialog.component';
import { APP_URI } from "@app-core/constants";
import { CommonModule } from "@angular/common";
import { NgModule } from "@angular/core";
import { RouterModule } from "@angular/router";
import { NotificationsComponent } from './notifications.component';

const routes = [
    {
        path: APP_URI.NOTIFICATIONS,
        component: NotificationsComponent
    }
];
@NgModule({
    declarations: [],
    imports: [CommonModule, RouterModule.forChild(routes)],
    entryComponents: [NotificationSearchDialogComponent]
})
export class NotificationsRoutingModule {
    static COMPONENTS = [
        NotificationsComponent, NotificationsComponent
    ];
}
