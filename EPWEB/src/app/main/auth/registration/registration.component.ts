import { setupPageLayout } from 'src/app/core/common-funcations';
import { MessageService } from './../../../shared/services/message.service';
import { IanaConfig } from './../../../shared/models/iana-config';
import { NAVIGATE_URI, CONSTANTS } from './../../../core/constants';
import { ValidationError } from './../../../shared/models/validation-error';
import { SubjectService } from './../../../shared/services/subject.service';
import { LoginService } from './../../../shared/services/login.service';
import { StorageService } from './../../../shared/services/storage.service';
import { SecurityObject } from './../../../shared/models/security-object';
import { RegistrationForm } from './../../../shared/forms/registration-form';
import { ianaAnimations } from './../../../core/iana-animation';
import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { SnotifyService } from 'ng-snotify';
import { Subscription } from 'rxjs';
import * as commonFuncation from './../../../core/common-funcations';

@Component({
    selector: 'app-registration',
    templateUrl: './registration.component.html',
    styleUrls: ['./registration.component.scss'],
    encapsulation: ViewEncapsulation.None,
    animations: ianaAnimations
})
export class RegistrationComponent implements OnInit {

    registerForm: FormGroup;
    RegistrationForm: RegistrationForm;
    loader: boolean = false;
    securityObject: SecurityObject;
    accountSetup: any = [];
    zipCodeList: any = [];
    isRegiterSuccess: boolean = false;
    registerSuccessRes: any = {};
    buttonLoaderSubscription: Subscription;
    commonFuncation: any;
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
        private _storageService: StorageService,
        private _router: Router,
        private _loginService: LoginService,
        private _snotifyService: SnotifyService,
        private _subjectService: SubjectService
    ) {

        //setup public page for removing header, footer & some navigation..
        setupPageLayout(this.ianaConfig, false);
        this._msgService.updateMessage(this.ianaConfig);

        var data: SecurityObject = this._storageService.getItem(CONSTANTS.SECURITY_OBJ);
        this.securityObject = data ? data : new SecurityObject();
        if (this.securityObject.accessToken)
            this._router.navigate([NAVIGATE_URI.DASHBOARD]);

       
        this.commonFuncation = commonFuncation;
        this.buttonLoaderSubscription = this._subjectService.getButtonLoaderMessage().subscribe(loader => { this.loader = loader.isButtonLoader; });
    }

    ngOnInit(): void {

        this.registerForm = this._formBuilder.group({
            prefix: [''],
            contactFirstName: ['', [Validators.required]],
            contactMiddleName: [''],
            contactLastName: ['', [Validators.required]],
            companyName: ['', [Validators.required]],
            iaLicenseNo: [''],
            title: [''],
            contactSuffix: [''],
            busaddln1: ['', [Validators.required]],
            busaddln2: [''],
            pinCode: ['', [Validators.required, Validators.pattern(CONSTANTS.ZIP_CODE_REGEX)]],
            city: ['', [Validators.required, Validators.maxLength(60)]],
            state: ['', [Validators.required]],
            country: ['', [Validators.required]],
            phone: ['', [Validators.required, Validators.pattern(CONSTANTS.REGISTRATION_PHONENUMBER_REGEX)]],
            fax: ['', [Validators.required, Validators.pattern(CONSTANTS.REGISTRATION_FAX_REGEX)]],
            contactEmail: ['', [Validators.required, Validators.pattern(CONSTANTS.EMAIL_REGEX)]],
            url: ['']
        });
        this.getAccountsetDetail();
    }

    registerBtn() {
        if (this.registerForm.valid) {
            this.loader = true;
            let data: RegistrationForm = this.registerForm.value as RegistrationForm;
            this._loginService.userRegistration(data, (res: any) => {
                this.loader = false;
                this.isRegiterSuccess = true;
                this.registerSuccessRes = res;
                // registerSuccessRes.message
                // details
                // this._snotifyService.success("Account info updated successfully.", CONSTANTS.SnotifyToastNotificationConfig);
                // this._router.navigate([NAVIGATE_URI.LOGIN]);
            }, (error: ValidationError) => {
                //error case...
                this.loader = false;
                let err = error.obj.apiReqErrors.errors[0];
                this._snotifyService.error(err.errorMessage, CONSTANTS.ERR_TITLE, CONSTANTS.SnotifyToastNotificationConfig);
            });
        }
    }

    resetBtn() {
        this.registerForm.controls.prefix.setValue('');
        this.registerForm.controls.contactFirstName.setValue('');
        this.registerForm.controls.contactLastName.setValue('');
        this.registerForm.controls.contactMiddleName.setValue('');
        this.registerForm.controls.companyName.setValue('');
        this.registerForm.controls.iaLicenseNo.setValue('');
        this.registerForm.controls.title.setValue('');
        this.registerForm.controls.contactSuffix.setValue('');
        this.registerForm.controls.busaddln1.setValue('');
        this.registerForm.controls.busaddln2.setValue('');
        this.registerForm.controls.pinCode.setValue('');
        this.registerForm.controls.city.setValue('');
        this.registerForm.controls.state.setValue('');
        this.registerForm.controls.country.setValue('');
        this.registerForm.controls.phone.setValue('');
        this.registerForm.controls.fax.setValue('');
        this.registerForm.controls.contactEmail.setValue('');
        this.registerForm.controls.url.setValue('');
    }


    getAccountsetDetail() {
        this._loginService.getAccountsetDetail((res: any) => {
            this.accountSetup = res;
        }, (error: ValidationError) => {
            //error case...
            this.loader = false;
            let err = error.obj.apiReqErrors.errors[0];
            this._snotifyService.error(err.errorMessage, CONSTANTS.ERR_TITLE, CONSTANTS.SnotifyToastNotificationConfig);
        });
    }


    getzipCode(zipCode) {
        if (zipCode && zipCode.length > 2) {
            this._loginService.getzipCode(zipCode, (res: any) => {
                this.zipCodeList = res;
            }, (error: ValidationError) => {
                //error case...
                this.loader = false;
                let err = error.obj.apiReqErrors.errors[0];
                this._snotifyService.error(err.errorMessage, CONSTANTS.ERR_TITLE, CONSTANTS.SnotifyToastNotificationConfig);
            });
        }
    }

    onSelectZip(zip) {
        if (zip.label) {
            let data = zip.label.split(':');
            if (data.length == 4) {
                this.registerForm.controls.pinCode.setValue(data[0]);
                this.registerForm.controls.city.setValue(data[1]);
                this.registerForm.controls.country.setValue(data[2]);
                this.registerForm.controls.state.setValue(data[3]);
            }
        }
    }

    downloadPDF() {
        this._loginService.downloadPDF(this.registerSuccessRes.details, (res: any) => {
        }, (error: ValidationError) => {
            //error case...
            this.loader = false;
            let err = error.obj.apiReqErrors.errors[0];
            this._snotifyService.error(err.errorMessage, CONSTANTS.ERR_TITLE, CONSTANTS.SnotifyToastNotificationConfig);
        });
    }

    ngOnDestroy() {
        if (this.buttonLoaderSubscription)
            this.buttonLoaderSubscription.unsubscribe();
    }

}
