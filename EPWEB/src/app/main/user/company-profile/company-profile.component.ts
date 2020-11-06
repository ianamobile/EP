
import { Component, OnInit, ViewEncapsulation } from "@angular/core";
import {
    AbstractControl,
    FormBuilder,
    FormGroup,
    ValidationErrors,
    ValidatorFn,
    Validators
} from "@angular/forms";
import { Router } from "@angular/router";
import { setupPageLayout } from '@app-core/common-funcations';
import { ianaAnimations } from '@app-core/iana-animation';
import { IanaConfig } from '@app-models/iana-config';
import { MessageService } from '@app-services/message.service';
import { SnotifyService } from "ng-snotify";
import { NgxSpinnerService } from "ngx-spinner";


@Component({
    selector: "app-company-profile",
    templateUrl: "./company-profile.component.html",
    styleUrls: ["./company-profile.component.scss"],
    encapsulation: ViewEncapsulation.None,
    animations: ianaAnimations
})
export class CompanyProfileComponent implements OnInit {

    ianaConfig: IanaConfig = new IanaConfig();

    // companyForm: FormGroup;
    // changePasswordForm: FormGroup;
    // CompanyInfoForm: CompanyInfoForm;
    // isPasswordRequired: boolean = false;

    // loader: boolean = false;
    // passwordloader: boolean = false;
    // securityObject: SecurityObject;
    // accountSetup: any = [];
    // zipCodeList: any = [];
    // companyInfoData: CompanyInfoForm = new CompanyInfoForm();
    // commonFuncation: any;

     constructor(
        private _msgService: MessageService<IanaConfig>,
        // private _formBuilder: FormBuilder,
        // private _companyProfileService: CompanyProfileService,
        // private _router: Router,
        // private _loginService: LoginService,
        // private _snotifyService: SnotifyService,
        // private _spinner: NgxSpinnerService,
        // private _storageService: StorageService
    ) {
        
        //setup public page for removing header, footer & some navigation..
        setupPageLayout(this.ianaConfig, true);
        this._msgService.updateMessage(this.ianaConfig);
 
        // this.commonFuncation = commonFuncation;
        // var data: SecurityObject = this._storageService.getItem(
        //     CONSTANTS.SECURITY_OBJ
        // );
        // this.securityObject = data ? data : new SecurityObject();
    }

    ngOnInit(): void {
        // this.createCompanyProfileForm();
        // this.createChangePasswordForm();
        // this.getCompanyInformation();
        // this.getAccountsetDetail();
    }

    // createCompanyProfileForm() {
    //     this.companyForm = this._formBuilder.group({
    //         prefix: [this.companyInfoData.prefix],
    //         accountNumber: [
    //             this.companyInfoData.accountNumber,
    //             [Validators.required]
    //         ],
    //         contactFirstName: [
    //             this.companyInfoData.contactFirstName,
    //             [Validators.required]
    //         ],
    //         contactMiddleName: [this.companyInfoData.contactMiddleName],
    //         contactLastName: [
    //             this.companyInfoData.contactLastName,
    //             [Validators.required]
    //         ],
    //         companyName: [
    //             this.companyInfoData.companyName,
    //             [Validators.required]
    //         ],
    //         iaLicenseNo: [this.companyInfoData.iaLicenseNo],
    //         title: [this.companyInfoData.title],
    //         contactSuffix: [this.companyInfoData.contactSuffix],
    //         busaddln1: [this.companyInfoData.busaddln1, [Validators.required]],
    //         busaddln2: [this.companyInfoData.busaddln2],
    //         pinCode: [this.companyInfoData.pinCode, [Validators.required, Validators.pattern(CONSTANTS.ZIP_CODE_REGEX)]],
    //         city: [this.companyInfoData.city, [Validators.required, Validators.maxLength(60)]],
    //         state: [this.companyInfoData.state, [Validators.required]],
    //         country: [this.companyInfoData.country, [Validators.required]],
    //         phone: [this.companyInfoData.phone, [Validators.required, Validators.pattern(CONSTANTS.REGISTRATION_PHONENUMBER_REGEX)]],
    //         fax: [this.companyInfoData.fax, [Validators.required, Validators.pattern(CONSTANTS.REGISTRATION_FAX_REGEX)]],
    //         contactEmail: [
    //             this.companyInfoData.contactEmail,
    //             [Validators.required, , Validators.pattern(CONSTANTS.EMAIL_REGEX)]
    //         ],
    //         url: [this.companyInfoData.url],
    //         remarks: [this.companyInfoData.remarks],

    //         iaId: [this.companyInfoData.iaId],
    //         contactInfoId: [this.companyInfoData.contactInfoId],
    //         contactAddressId: [this.companyInfoData.contactAddressId]
    //     });
    // }

    // createChangePasswordForm() {
    //     this.changePasswordForm = this._formBuilder.group({
    //         newPassword: ['', [Validators.required, Validators.maxLength(35)]],
    //         confirmPassword: ['', [Validators.required, confirmPasswordValidator]]
    //     });
    // }

    // updateValidation() {
    //     if (
    //         this.changePasswordForm.valid
    //     ) {
    //         this.isPasswordRequired = true;
    //         this.changePasswordForm
    //             .get("newPassword")
    //             .setValidators([Validators.required, Validators.maxLength(35)]);
    //         this.changePasswordForm
    //             .get("confirmPassword")
    //             .setValidators([Validators.required, confirmPasswordValidator]);
    //     } else {
    //         this.isPasswordRequired = false;
    //         this.changePasswordForm.get("newPassword").clearValidators();
    //         this.changePasswordForm.get("confirmPassword").clearValidators();
    //         this.changePasswordForm
    //             .get("newPassword")
    //             .setValidators([Validators.required]);
    //         this.changePasswordForm
    //             .get("confirmPassword")
    //             .setValidators([Validators.required]);
    //     }
    //     this.changePasswordForm.get("newPassword").updateValueAndValidity();
    //     this.changePasswordForm.get("confirmPassword").updateValueAndValidity();
    // }

    // updateBtn() {
    //     if (this.companyForm.valid) {
    //         this.loader = true;
    //         let data: CompanyInfoForm = this.companyForm
    //             .value as CompanyInfoForm;
    //         this._companyProfileService.updateCompanyInfo(
    //             data,
    //             (res: any) => {
    //                 this.loader = false;
    //                 this._snotifyService.success(
    //                     "Company info updated successfully.",
    //                     CONSTANTS.SnotifyToastNotificationConfig
    //                 );
    //             },
    //             (error: ValidationError) => {
    //                 //error case...
    //                 this.loader = false;
    //                 let err = error.obj.apiReqErrors.errors[0];
    //                 this._snotifyService.error(
    //                     err.errorMessage,
    //                     CONSTANTS.ERR_TITLE,
    //                     CONSTANTS.SnotifyToastNotificationConfig
    //                 );
    //             }
    //         );
    //     }
    // }

    // getAccountsetDetail() {
    //     this._loginService.getAccountsetDetail(
    //         (res: any) => {
    //             this.accountSetup = res;
    //         },
    //         (error: ValidationError) => {
    //             //error case...
    //             this.loader = false;
    //             let err = error.obj.apiReqErrors.errors[0];
    //             this._snotifyService.error(
    //                 err.errorMessage,
    //                 CONSTANTS.ERR_TITLE,
    //                 CONSTANTS.SnotifyToastNotificationConfig
    //             );
    //         }
    //     );
    // }

    // getzipCode(zipCode) {
    //     if (zipCode && zipCode.length > 2) {
    //         this._loginService.getzipCode(
    //             zipCode,
    //             (res: any) => {
    //                 this.zipCodeList = res;
    //             },
    //             (error: ValidationError) => {
    //                 //error case...
    //                 this.loader = false;
    //                 let err = error.obj.apiReqErrors.errors[0];
    //                 this._snotifyService.error(
    //                     err.errorMessage,
    //                     CONSTANTS.ERR_TITLE,
    //                     CONSTANTS.SnotifyToastNotificationConfig
    //                 );
    //             }
    //         );
    //     }
    // }

    // onSelectZip(zip) {
    //     if (zip.label) {
    //         let data = zip.label.split(":");
    //         if (data.length == 4) {
    //             this.companyForm.controls.pinCode.setValue(data[0]);
    //             this.companyForm.controls.city.setValue(data[1]);
    //             this.companyForm.controls.state.setValue(data[2]);
    //             this.companyForm.controls.country.setValue(data[3]);
    //         }
    //     }
    // }

    // getCompanyInformation() {
    //     this._spinner.show();
    //     this._companyProfileService.getCompanyInfo(
    //         this.securityObject.accountNumber,
    //         (res: CompanyInfoForm) => {
    //             this._spinner.hide();
    //             this.companyInfoData = res as CompanyInfoForm;
    //             this.createCompanyProfileForm();
    //         },
    //         (error: ValidationError) => {
    //             this._spinner.hide();
    //             let err = error.obj.apiReqErrors.errors[0];
    //             this._snotifyService.error(
    //                 err.errorMessage,
    //                 CONSTANTS.ERR_TITLE,
    //                 CONSTANTS.SnotifyToastNotificationConfig
    //             );
    //         }
    //     );
    // }

    // changePassword() {
    //     if (this.changePasswordForm.valid) {
    //         this._spinner.show();
    //         this._companyProfileService.changePassword(this.changePasswordForm.value,
    //             (res: any) => {
    //                 this._spinner.hide();
    //                 this._snotifyService.success(
    //                     res.message,
    //                     CONSTANTS.SnotifyToastNotificationConfig
    //                 );
    //                 this.clearChangePasswordForm()

    //             },
    //             (error: ValidationError) => {
    //                 this._spinner.hide();
    //                 let err = error.obj.apiReqErrors.errors[0];
    //                 this._snotifyService.error(
    //                     err.errorMessage,
    //                     CONSTANTS.ERR_TITLE,
    //                     CONSTANTS.SnotifyToastNotificationConfig
    //                 );
    //             })
    //     }
    // }

    // clearChangePasswordForm() {
    //     for (const name in this.changePasswordForm.controls) {
    //         if (name) {
    //             this.changePasswordForm.controls[name].setValue('');
    //             this.changePasswordForm.controls[name].setErrors(null);
    //         }
    //     }
    //     this.createChangePasswordForm()
    // }

    tabChanged() {
      //  this.clearChangePasswordForm();
    }

}

// export const confirmPasswordValidator: ValidatorFn = (
//     control: AbstractControl
// ): ValidationErrors | null => {
//     if (!control.parent || !control) {
//         return null;
//     }

//     const password = control.parent.get("newPassword");
//     const passwordConfirm = control.parent.get("confirmPassword");

//     if (!password || !passwordConfirm) {
//         return null;
//     }

//     if (passwordConfirm.value === "") {
//         return null;
//     }

//     if (password.value === passwordConfirm.value) {
//         return null;
//     }

//     return { passwordsNotMatching: true };
// };
