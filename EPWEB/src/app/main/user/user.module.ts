import { AppSharedModule } from './../../app-shared.module';
import { NgModule } from '@angular/core';
import { UserRoutingModule } from './user-routing.module'; 
import { ExpiringModalComponent } from './expiring-modal/expiring-modal.component';
import { AddendumDetailsComponent } from './addendum-details/addendum-details.component';
import { ManageBillingUsersComponent } from './manage-billing-users/manage-billing-users.component';
import { UserFormDialogComponent } from './user-form-dialog/user-form-dialog.component';
import { DeleteDialogComponent } from './delete-dialog/delete-dialog.component';
import { UiiaInvoicesComponent } from './uiia-invoices/uiia-invoices.component'; 
import { ListOfDeletedMCComponent } from './list-of-deleted-mc/list-of-deleted-mc.component'; 
import { DeletedsearchMCComponent } from './deletedsearch-mc/deletedsearch-mc.component';
import { InvoiceDetailsComponent } from './invoice-details/invoice-details.component';
import { SecondaryUserDetailsComponent } from './secondary-user-details/secondary-user-details.component';
import { SearchSecondaryDialogComponent } from './search-secondary-dialog/search-secondary-dialog.component';
import { SecondaryUserFormDialogComponent } from './secondary-user-form-dialog/secondary-user-form-dialog.component';
import { TerminalFeedLocationsComponent } from './terminal-feed-locations/terminal-feed-locations.component';
import { PaymentFailureAttemptsDialogComponent } from './payment-failure-attempts-dialog/payment-failure-attempts-dialog.component';
import { BillingPaymentComponent } from './billing-payment/billing-payment.component';
import { ManagePaymentMethodComponent } from './manage-payment-method/manage-payment-method.component';
import { BillingResultComponent } from './billing-result/billing-result.component';
import { MonthlyBookWeeklySupplementComponent } from './monthly-book-weekly-supplement/monthly-book-weekly-supplement.component';
import { EpTemplateComponent } from './ep-template/ep-template.component';
import { EpSearchTemplateDialogComponent } from './ep-search-template-dialog/ep-search-template-dialog.component';
import { EpShowTemplateComponent } from './ep-show-template/ep-show-template.component';
import { MCLookupComponent } from './mclookup/mclookup.component';
import { McSpecificSearchDialogComponent } from './mc-specific-search-dialog/mc-specific-search-dialog.component';
import { EpRequirementsDialogComponent } from './ep-requirements-dialog/ep-requirements-dialog.component';

@NgModule({
  declarations: [
    UserRoutingModule.COMPONENTS,
    ExpiringModalComponent,
    AddendumDetailsComponent,
    ManageBillingUsersComponent,
    UserFormDialogComponent,
    DeleteDialogComponent,
    UiiaInvoicesComponent,
    ListOfDeletedMCComponent,
    DeletedsearchMCComponent,
    InvoiceDetailsComponent,
    SecondaryUserDetailsComponent,
    SearchSecondaryDialogComponent,
    SecondaryUserFormDialogComponent,
    TerminalFeedLocationsComponent,
    PaymentFailureAttemptsDialogComponent,
    BillingPaymentComponent,
    ManagePaymentMethodComponent,
    BillingResultComponent,
    MonthlyBookWeeklySupplementComponent,
    EpTemplateComponent,
    EpSearchTemplateDialogComponent,
    EpShowTemplateComponent,
    MCLookupComponent,
    McSpecificSearchDialogComponent,
    EpRequirementsDialogComponent, 
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
