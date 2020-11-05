import { ResetPassword } from './../../../shared/models/reset-passsword';
import { ValidationError } from './../../../shared/models/validation-error';
import { HashMap } from './../../../core/hashmap';
import { NAVIGATE_URI, CONSTANTS } from './../../../core/constants';
import { setupPageLayout } from 'src/app/core/common-funcations';
import { SubjectService } from './../../../shared/services/subject.service';
import { ResetPasswordService } from './../../../shared/services/reset-password.service';
import { RestService } from './../../../shared/services/http-rest.service';
import { MessageService } from './../../../shared/services/message.service';
import { IanaConfig } from './../../../shared/models/iana-config';
import { ianaAnimations } from './../../../core/iana-animation';
import { Component, OnDestroy, OnInit, ViewEncapsulation } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { SnotifyService } from 'ng-snotify';
import { Subject, Subscription } from 'rxjs';



@Component({
    selector: 'reset-password',
    templateUrl: './reset-password.component.html',
    styleUrls: ['./reset-password.component.scss'],
    encapsulation: ViewEncapsulation.None,
    animations: ianaAnimations
})
export class ResetPasswordComponent implements OnInit, OnDestroy {
    resetPasswordForm: FormGroup;
    passwordToken: string = "";
    isNewPassword: boolean = true;
    isNewCPassword: boolean = true;
    loader: boolean = false
    buttonLoaderSubscription: Subscription;
    ianaConfig: IanaConfig = new IanaConfig();
    // Private
    private _unsubscribeAll: Subject<any>;

    constructor(
        private _msgService: MessageService<IanaConfig>,
        private _formBuilder: FormBuilder,
        private _restService: RestService,
        private route: ActivatedRoute,
        private router: Router,
        private _snotifyService: SnotifyService,
        private _resetPasswordService: ResetPasswordService,
        private _subjectService: SubjectService
    ) {

        //setup public page for removing header, footer & some navigation..
        setupPageLayout(this.ianaConfig, false);
        this._msgService.updateMessage(this.ianaConfig);
        
        // Set the private defaults
        this._unsubscribeAll = new Subject();
        this.buttonLoaderSubscription = this._subjectService.getButtonLoaderMessage().subscribe(loader => { this.loader = loader.isButtonLoader; });
    }

    // -----------------------------------------------------------------------------------------------------
    // @ Lifecycle hooks
    // -----------------------------------------------------------------------------------------------------

    /**
     * On init
     */
    ngOnInit(): void {

        this.route.queryParams.subscribe(params => {
            this.passwordToken = params.q;
        });
        //Start code to authenticate the token if true then redirect to reset password page else login page
        if (this.passwordToken.trim() == '') {
            this._snotifyService.warning("Please click on the reset button from your registered email account.", CONSTANTS.SnotifyToastNotificationConfig);
            this.router.navigate([NAVIGATE_URI.LOGIN]);
            return;
        } else {
            let params = new HashMap();
            params.put("q", this.passwordToken);
            this.loader = true;
            this._resetPasswordService.validateForgotPwdLink(
                params,
                (res: any) => {
                    //success case.
                    this.loader = false;
                },
                (error: ValidationError) => {
                    //error case...
                    this.loader = false;
                    let err = error.obj.apiReqErrors.errors[0];
                    this._snotifyService.error(err.errorMessage, CONSTANTS.ERR_TITLE, CONSTANTS.SnotifyToastNotificationConfig);
                    this.router.navigate([NAVIGATE_URI.LOGIN]);
                });

            this.resetPasswordForm = this._formBuilder.group({
                newpassword: ['', [Validators.required, Validators.maxLength(35)]],
                confirmpassword: ['', [Validators.required, confirmPasswordValidator, Validators.maxLength(35)]]
            });

        }
    }


    resetPassword() {
        //reset password when both password are matched.
        if (this.resetPasswordForm.valid) {
            this.loader = true;
            let resetPasswordData: ResetPassword = new ResetPassword(
                this.resetPasswordForm.value.newpassword,
                this.resetPasswordForm.value.confirmpassword,
                this.passwordToken);

            this.loader = true;
            this._resetPasswordService.resetPassword(
                resetPasswordData,
                (res: any) => {
                    //success case.
                    this.loader = false;
                    this._snotifyService.success(res.message, CONSTANTS.SnotifyToastNotificationConfig);
                    this.router.navigate([NAVIGATE_URI.LOGIN]);
                },
                (error: ValidationError) => {
                    //error case...
                    this.loader = false;
                    let err = error.obj.apiReqErrors.errors[0];
                    this._snotifyService.error(err.errorMessage, CONSTANTS.ERR_TITLE, CONSTANTS.SnotifyToastNotificationConfig);

                });

        }
    }


    /**
     * On destroy
     */
    ngOnDestroy(): void {
        // Unsubscribe from all subscriptions
        this._unsubscribeAll.next();
        this._unsubscribeAll.complete();

        if (this.buttonLoaderSubscription)
            this.buttonLoaderSubscription.unsubscribe();

    }
}

/**
 * Confirm password validator
 *
 * @param {AbstractControl} control
 * @returns {ValidationErrors | null}
 */
export const confirmPasswordValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {

    if (!control.parent || !control) {
        return null;
    }

    const password = control.parent.get('newpassword');
    const passwordConfirm = control.parent.get('confirmpassword');

    if (!password || !passwordConfirm) {
        return null;
    }

    if (passwordConfirm.value === '') {
        return null;
    }

    if (password.value === passwordConfirm.value) {
        return null;
    }

    return { 'passwordsNotMatching': true };
};
