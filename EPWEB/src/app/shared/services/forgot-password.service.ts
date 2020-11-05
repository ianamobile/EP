import { ValidationError } from './../models/validation-error';
import { REST_URI } from './../../core/constants';
import { ForgotPassword } from './../models/forgot-password';
import { BaseService } from './../../core/base-service';
import { Injectable } from '@angular/core';
import { RestService } from './http-rest.service';
import { SubjectService } from './subject.service';

@Injectable({
  providedIn: 'root'
})
export class ForgotPasswordService extends BaseService{

  constructor(private restService: RestService,
    private subjectService: SubjectService) {
    super();
  }
  
  forgotPassword(forgotPasswordData:ForgotPassword, successCallBack: (res: any) => void, errorCallBack: (error: ValidationError) => void) {
    this.restService.post(REST_URI.FORGOT_PASSWORD, forgotPasswordData).subscribe(
    res => this.processSuccessResponse(res,successCallBack), 
    (error: any) => {
      this.subjectService.sendButtonLoaderMessage(false);
      this.processErrorResponse(error, errorCallBack)
    });
  }


}
