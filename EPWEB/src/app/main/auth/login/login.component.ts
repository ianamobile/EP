
import { Component, ComponentFactoryResolver, OnInit, ViewEncapsulation } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { setupPageLayout } from '@app-core/common-funcations';
import { ianaAnimations } from '@app-core/iana-animation';
import { IanaConfig } from '@app-models/iana-config';
import { MessageService } from '@app-services/message.service';
import { SnotifyService } from 'ng-snotify';
import { Subscription } from 'rxjs';

 

@Component({
    selector: 'login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.scss'],
   // encapsulation: ViewEncapsulation.None,
    animations: ianaAnimations
})
export class LoginComponent implements OnInit {
    //loginForm: FormGroup;

    // loader: boolean = false;
    // securityObject: SecurityObject;
    // buttonLoaderSubscription: Subscription;
    ianaConfig: IanaConfig = new IanaConfig();
     
    constructor(
        private _msgService: MessageService<IanaConfig>,
       // private _formBuilder: FormBuilder,
        // private _storageService: StorageService,
        // private _router: Router,
        // private _snotifyService: SnotifyService,
        // private _loginService: LoginService,
        // private _subjectService: SubjectService

    ) {
        //setup public page for removing header, footer & some navigation..
        setupPageLayout(this.ianaConfig, false);
       this._msgService.updateMessage(this.ianaConfig);

        // var data: SecurityObject = this._storageService.getItem(CONSTANTS.SECURITY_OBJ);
        // this.securityObject = data ? data : new SecurityObject();
        // if (this.securityObject.accessToken)
        //     this._router.navigate([NAVIGATE_URI.DASHBOARD]);

        // console.log("in loading...");
        // this.buttonLoaderSubscription = this._subjectService.getButtonLoaderMessage().subscribe(loader => { this.loader = loader.isButtonLoader; }); 
        
    }

   

    // -----------------------------------------------------------------------------------------------------
    // @ Lifecycle hooks
    // -----------------------------------------------------------------------------------------------------

    /**
     * On init
     */
    ngOnInit(): void {
        // this.loginForm = this._formBuilder.group({
        //     username: ['', [Validators.required, Validators.maxLength(50), Validators.pattern(CONSTANTS.SPECIALCHAR_REGEX)]],
        //     password: ['', [Validators.required, Validators.maxLength(35)]]
        // });
    }

    //login() {
        // this.loader = true;
        // let login: Login = new Login(this.loginForm.value.username, this.loginForm.value.password, "IA");
        // //call login service to authenticate the user credentials.
        // this._loginService.authenticateLogin(
        //     login,
        //     (res: any) => {
        //         let securityObject: SecurityObject = res as SecurityObject;
        //         //store session data in session storage
        //         this._storageService.setItem(CONSTANTS.SECURITY_OBJ, securityObject);
        //         this._router.navigate([NAVIGATE_URI.DASHBOARD]);
        //         this.loader = false;
        //     },
        //     (error: ValidationError) => {
        //         //error case...
        //         this.loader = false;
        //         let err = error.obj.apiReqErrors.errors[0];
        //         this._snotifyService.error(err.errorMessage, CONSTANTS.ERR_TITLE, CONSTANTS.SnotifyToastNotificationConfig);
        //     });
   // }

    ngOnDestroy() {
    //     if (this.buttonLoaderSubscription)
    //         this.buttonLoaderSubscription.unsubscribe();
     }
}
