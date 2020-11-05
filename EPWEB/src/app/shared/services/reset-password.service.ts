import { ResetPassword } from './../models/reset-passsword';
import { ValidationError } from './../models/validation-error';
import { REST_URI } from './../../core/constants';
import { HashMap } from './../../core/hashmap';
import { BaseService } from './../../core/base-service';
import { Injectable } from '@angular/core';
import { RestService } from './http-rest.service';
import { SubjectService } from './subject.service';

@Injectable({
  providedIn: 'root'
})
export class ResetPasswordService extends BaseService {

  constructor(private restService: RestService,
    private subjectService: SubjectService) {
    super();
  }

  validateForgotPwdLink(params: HashMap, successCallBack: (res: any) => void, errorCallBack: (error: ValidationError) => void) {
    this.restService.get(REST_URI.VALIDATE_FORGOTPWD_LINK, params).subscribe(
      res => this.processSuccessResponse(res, successCallBack),
      (error: any) => {
        this.subjectService.sendButtonLoaderMessage(false);
        this.processErrorResponse(error, errorCallBack)
      });
  }

  resetPassword(data: ResetPassword, successCallBack: (res: any) => void, errorCallBack: (error: ValidationError) => void, ) {
    this.restService.post(REST_URI.RESET_PASSWORD, data).subscribe(
      res => this.processSuccessResponse(res, successCallBack),
      (error: any) => {
        this.subjectService.sendButtonLoaderMessage(false);
        this.processErrorResponse(error, errorCallBack)
      });
  }
}