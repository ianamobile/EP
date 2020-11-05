import { RegistrationComponent } from './registration/registration.component';
import { ResetPasswordComponent } from './reset-password/reset-password.component';
import { ForgotPasswordComponent } from './forgot-password/forgot-password.component';
import { LoginService } from './../../shared/services/login.service';
import { APP_URI } from './../../core/constants';
import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { UiDemoComponent } from './ui-demo/ui-demo.component';
import { SecondaryUserLoginComponent } from './secondary-user-login/secondary-user-login.component';
import { BillingUserLoginComponent } from './billing-user-login/billing-user-login.component';
import { SecondaryUserForgotPasswordComponent } from './secondary-user-forgot-password/secondary-user-forgot-password.component';
import { BillingUserForgotPasswordComponent } from './billing-user-forgot-password/billing-user-forgot-password.component';




const routes = [
  {
    path: APP_URI.LOGIN,
    component: LoginComponent
  },
  {
    path: APP_URI.REGISTER,
    component: RegistrationComponent
  }, 
  {
    path: APP_URI.RESET_PASSWORD,
    component: ResetPasswordComponent
  },
  {
    path: APP_URI.FORGOT_PASSWORD,
    component: ForgotPasswordComponent
   },
   {
    path: "uiDemo",
    component: UiDemoComponent
   },
   {
    path: "billingUserLogin",
    component: BillingUserLoginComponent
   },
   {
    path: "secondaryUserLogin",
    component: SecondaryUserLoginComponent
   },
   {
    path: "secondaryUserForgotPassword",
    component: SecondaryUserForgotPasswordComponent
   },
   {
    path: "billingUserForgotPassword",
    component: BillingUserForgotPasswordComponent
   }

   
   
];
@NgModule({
  declarations: [],
  imports: [
    RouterModule.forChild(routes),
  ],
  providers: [
    LoginService
  ]
})
export class AuthRoutingModule {
  static COMPONENTS = [
    LoginComponent,
    ResetPasswordComponent,
    ForgotPasswordComponent,
    RegistrationComponent,
    UiDemoComponent
  ];

}
