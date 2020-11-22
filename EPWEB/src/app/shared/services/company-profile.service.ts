import { CompanyInfoForm, ChangePasswordForm } from './../forms/companyInfo-form';
import { Injectable } from "@angular/core";
import { ValidationError } from './../models/validation-error';
import { REST_URI } from './../../core/constants';
import { BaseService } from './../../core/base-service';
import { RestService } from "./http-rest.service";
 

@Injectable({
    providedIn: "root"
})
export class CompanyProfileService extends BaseService {
    constructor(private restService: RestService) {
        super();
    }

    updateCompanyInfo(
        data: CompanyInfoForm,
        successCallBack: (res: any) => void,
        errorCallBack: (error: ValidationError) => void
    ) {
        this.restService
            .put(REST_URI.COMPANYINFO + data.accountNumber, data)
            .subscribe(
                res => this.processSuccessResponse(res, successCallBack),
                (error: any) => this.processErrorResponse(error, errorCallBack)
            );
    }

    getCompanyInfo(
        accountNumber: any,
        successCallBack: (res: any) => void,
        errorCallBack: (error: ValidationError) => void
    ) {
        this.restService
            .get(REST_URI.COMPANYINFO + accountNumber)
            .subscribe(
                res => this.processSuccessResponse(res, successCallBack),
                (error: any) => this.processErrorResponse(error, errorCallBack)
            );
    }

    changePassword(data: ChangePasswordForm,
        successCallBack: (res: any) => void,
        errorCallBack: (error: ValidationError) => void
    ) {
        this.restService
            .post(REST_URI.CHANGEPASSWORD, data)
            .subscribe(
                res => this.processSuccessResponse(res, successCallBack),
                (error: any) => this.processErrorResponse(error, errorCallBack)
            );
    }
}
