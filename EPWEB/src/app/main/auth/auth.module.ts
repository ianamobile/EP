import { AppSharedModule } from './../../app-shared.module';
import { NgModule } from '@angular/core';
import { AuthRoutingModule } from './auth-routing.module';
import { SecondaryUserLoginComponent } from './secondary-user-login/secondary-user-login.component';
import { BillingUserLoginComponent } from './billing-user-login/billing-user-login.component';
import { SecondaryUserForgotPasswordComponent } from './secondary-user-forgot-password/secondary-user-forgot-password.component';
import { BillingUserForgotPasswordComponent } from './billing-user-forgot-password/billing-user-forgot-password.component';




@NgModule({
  declarations: [
    AuthRoutingModule.COMPONENTS,
    SecondaryUserLoginComponent,
    BillingUserLoginComponent,
    SecondaryUserForgotPasswordComponent,
    BillingUserForgotPasswordComponent
  ],
  imports: [
    AppSharedModule,
    AuthRoutingModule,
  ]
})
export class AuthModule { }
