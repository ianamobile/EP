import { REDIRECT_URI, MODULE_PATH } from './core/constants';
import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { AuthGurdService } from './core/auth-gurd.service';


const appRoutes: Routes = [
  {
      path: MODULE_PATH.AUTH,
      loadChildren: () => import('./main/auth/auth.module').then(m => m.AuthModule)
     
  },
  {
    path: MODULE_PATH.USER,
    loadChildren: () => import('./main/user/user.module').then(m => m.UserModule),
    // canActivate: [AuthGurdService]
  },
  {
    path: MODULE_PATH.NOTIFICATIONS,
    loadChildren: () => import('./main/notifications/notifications.module').then(m => m.NotificationsModule),
    //canActivate: [AuthGurdService]
  },
  {
      path: '',
      redirectTo: REDIRECT_URI.LOGIN,
      pathMatch: 'full'
  },
  {
      path: '**',
      redirectTo: REDIRECT_URI.LOGIN
  }
];

@NgModule({
  imports: [RouterModule.forRoot(appRoutes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
