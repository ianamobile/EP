import { APP_URI } from '@app-core/constants';
import { NgModule } from "@angular/core";
import { RouterModule } from "@angular/router";
import { DashboardComponent } from "./dashboard/dashboard.component";

import { CompanyProfileComponent } from "./company-profile/company-profile.component";
import { LoginService } from '@app-services/login.service';
import { AddendumDetailsComponent } from './addendum-details/addendum-details.component';

const routes = [
    {
        path: APP_URI.DASHBOARD,
        component: DashboardComponent
    },
    {
        path: APP_URI.COMPANY_PROFILE,
        component: CompanyProfileComponent
    },
    {
     path: "addendumDetails",
     component: AddendumDetailsComponent
    }
];

@NgModule({
    declarations: [],
    imports: [
              RouterModule.forChild(routes)
             ],
    providers: [LoginService]
})
export class UserRoutingModule {
    static COMPONENTS = [DashboardComponent, CompanyProfileComponent];
}
