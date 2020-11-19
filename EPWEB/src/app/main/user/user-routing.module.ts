import { APP_URI } from '@app-core/constants';
import { NgModule } from "@angular/core";
import { RouterModule } from "@angular/router";
import { DashboardComponent } from "./dashboard/dashboard.component";

import { CompanyProfileComponent } from "./company-profile/company-profile.component";
import { LoginService } from '@app-services/login.service';
import { AddendumDetailsComponent } from './addendum-details/addendum-details.component';
import { ManageBillingUsersComponent } from './manage-billing-users/manage-billing-users.component';
import { UiiaInvoicesComponent } from './uiia-invoices/uiia-invoices.component';
import { ListOfDeletedMCComponent } from './list-of-deleted-mc/list-of-deleted-mc.component';
import { InvoiceDetailsComponent } from './invoice-details/invoice-details.component';
import { SecondaryUserDetailsComponent } from './secondary-user-details/secondary-user-details.component';
import { TerminalFeedLocationsComponent } from './terminal-feed-locations/terminal-feed-locations.component';
import { BillingPaymentComponent } from './billing-payment/billing-payment.component';
import { ManagePaymentMethodComponent } from './manage-payment-method/manage-payment-method.component';
import { BillingResultComponent } from './billing-result/billing-result.component';
import { MonthlyBookWeeklySupplementComponent } from './monthly-book-weekly-supplement/monthly-book-weekly-supplement.component';
import { EpTemplateComponent } from './ep-template/ep-template.component';
import { EpShowTemplateComponent } from './ep-show-template/ep-show-template.component';
import { MCLookupComponent } from './mclookup/mclookup.component';

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
        path: APP_URI.ADDENDUM_DETAILS,
        component: AddendumDetailsComponent
    },
    {
        path: APP_URI.MANAG_BILLING_USERS,
        component: ManageBillingUsersComponent
    },
    {
        path: APP_URI.UIIA_INVOICES,
        component: UiiaInvoicesComponent
    },
    {
        path: APP_URI.INVOICE_DETAILS,
        component: InvoiceDetailsComponent
    },
    {
        path: APP_URI.LIST_OF_DELETED_MC,
        component: ListOfDeletedMCComponent
    },
    {
        path: APP_URI.SECONDARYUSERDETAILS,
        component: SecondaryUserDetailsComponent
    },
    {
        path: APP_URI.TERMINALFEEDLOCATIONS,
        component: TerminalFeedLocationsComponent
    },
    {
        path: APP_URI.BILLING_PAYMENT,
        component: BillingPaymentComponent
    },
    {
        path: APP_URI.BILLING_RESULT,
        component: BillingResultComponent
    },
    {
        path: APP_URI.MANAGE_PAYMENT_METHOD,
        component: ManagePaymentMethodComponent
    },
    {
        path: APP_URI.MONTHLY_BOOK_WEEKLY_SUPPLEMENT,
        component: MonthlyBookWeeklySupplementComponent
    },
    {
        path: APP_URI.EP_TEMPLATE,
        component: EpTemplateComponent
    },
    {
        path: APP_URI.EP_SHOW_TEMPLATE,
        component: EpShowTemplateComponent
    },
    {
        path: APP_URI.MC_LOOKUP,
        component: MCLookupComponent
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
