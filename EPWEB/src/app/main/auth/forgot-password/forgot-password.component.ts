
import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { setupPageLayout } from '@app-core/common-funcations';
import { CONSTANTS, NAVIGATE_URI } from '@app-core/constants';
import { ianaAnimations } from '@app-core/iana-animation';
import { ForgotPassword } from '@app-models/forgot-password';
import { IanaConfig } from '@app-models/iana-config';
import { ValidationError } from '@app-models/validation-error';
import { ForgotPasswordService } from '@app-services/forgot-password.service';
import { RestService } from '@app-services/http-rest.service';
import { MessageService } from '@app-services/message.service';
import { SubjectService } from '@app-services/subject.service';
import { SnotifyService } from 'ng-snotify';
import { Subscription } from 'rxjs';


const specialcharRegex = /^[0-9a-zA-Z]+$/;

@Component({
    selector: 'forgot-password',
    templateUrl: './forgot-password.component.html',
    styleUrls: ['./forgot-password.component.scss'],
    encapsulation: ViewEncapsulation.None,
    animations: ianaAnimations
})
export class ForgotPasswordComponent implements OnInit {
    forgotPasswordForm: FormGroup;
    loader: boolean = false
    buttonLoaderSubscription: Subscription;
    ianaConfig: IanaConfig = new IanaConfig();
    /**
     * Constructor
     *
     * @param {FuseConfigService} _fuseConfigService
     * @param {FormBuilder} _formBuilder
     */
    constructor(
        private _msgService: MessageService<IanaConfig>,
        private _formBuilder: FormBuilder,
        private _restService: RestService,
        private router: Router,
        private _snotifyService: SnotifyService,
        private _forgotPasswordService: ForgotPasswordService,
        private _subjectService: SubjectService
    ) {
        
        //setup public page for removing header, footer & some navigation..
        setupPageLayout(this.ianaConfig, false);
        this._msgService.updateMessage(this.ianaConfig);

        this.buttonLoaderSubscription = this._subjectService.getButtonLoaderMessage().subscribe(loader => { this.loader = loader.isButtonLoader; });

    }

    // -----------------------------------------------------------------------------------------------------
    // @ Lifecycle hooks
    // -----------------------------------------------------------------------------------------------------

    /**
     * On init
     */

    ngOnInit(): void {
        this.forgotPasswordForm = this._formBuilder.group({
            accountNumber: ['', [Validators.required, Validators.pattern(specialcharRegex), Validators.maxLength(50)]]
        });
    }

    forgotPassword() {
        if (this.forgotPasswordForm.valid) {
            this.loader = true;
            let forgotPasswordData: ForgotPassword = new ForgotPassword(this.forgotPasswordForm.value.accountNumber)

            //call forgot password service to send mail on the registered email address.
            this._forgotPasswordService.forgotPassword(
                forgotPasswordData,
                (res: any) => {
                    //success case.
                    this._snotifyService.success(res.message, CONSTANTS.SnotifyToastNotificationConfig);
                    this.router.navigate([NAVIGATE_URI.LOGIN]);
                    this.loader = false;
                },
                (error: ValidationError) => {

                    //error case...
                    this.loader = false;
                    let err = error.obj.apiReqErrors.errors[0];
                    this._snotifyService.error(err.errorMessage, CONSTANTS.ERR_TITLE, CONSTANTS.SnotifyToastNotificationConfig);
                });
        }
    }

    ngOnDestroy() {
        if (this.buttonLoaderSubscription)
            this.buttonLoaderSubscription.unsubscribe();
    }

}
