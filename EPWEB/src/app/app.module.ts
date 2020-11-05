import { SearchDialogComponent } from '@app-components/search-dialog/search-dialog.component';
import { HttpsInterceptorService } from './core/https-interceptor.service';
import { GlobalErrorHandlerService } from './core/global-error-handle.service';
import { AuthGurdService } from './core/auth-gurd.service';
import { StorageService } from './shared/services/storage.service';
import { NgxSpinnerModule } from 'ngx-spinner';
import { SnotifyModule, ToastDefaults, SnotifyService } from 'ng-snotify';
import { AppSharedModule } from './app-shared.module';
import { BrowserModule } from '@angular/platform-browser';
import { NgModule, ErrorHandler } from '@angular/core';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HeaderComponent } from './layout/header/header.component';
import { FooterComponent } from './layout/footer/footer.component';
import { TopNavigationComponent } from './layout/top-navigation/top-navigation.component';
import { LeftNavigationComponent } from './layout/left-navigation/left-navigation.component';
import { RightNavigationComponent } from './layout/right-navigation/right-navigation.component';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { SplashScreenComponent } from './splash-screen/splash-screen.component';


@NgModule({

  declarations: [

    AppComponent,
    HeaderComponent,
    FooterComponent,
    TopNavigationComponent,
    LeftNavigationComponent,
    RightNavigationComponent,
    SplashScreenComponent,
    SearchDialogComponent
    
  ],
  imports: [

    BrowserModule,
    BrowserAnimationsModule,
    HttpClientModule,
    
    AppRoutingModule,
    AppSharedModule,

     //thirdparty Plugin Import
     SnotifyModule,
     NgxSpinnerModule,
     
  ],
  exports: [
    
  ],
  providers: [

    StorageService,
    AuthGurdService,
    { provide: ErrorHandler, useClass: GlobalErrorHandlerService },
    // { provide: HTTP_INTERCEPTORS, useClass: HttpsInterceptorService, multi: true},

    // thirdparty Plugin Provider
    { provide: 'SnotifyToastConfig', useValue: ToastDefaults},
    SnotifyService

  ],
  bootstrap: [AppComponent],
  entryComponents: [
    SearchDialogComponent
  ]
})
export class AppModule { }
