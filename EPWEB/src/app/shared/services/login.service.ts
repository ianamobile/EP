import { Map } from './../../core/map';
import { HashMap } from './../../core/hashmap';
import { RegistrationForm } from './../forms/registration-form';
import { ValidationError } from './../models/validation-error';
import { Login } from './../models/login';
import { REST_URI } from './../../core/constants';
import { BaseService } from './../../core/base-service';
import { Injectable } from '@angular/core';
import { RestService } from './http-rest.service';
import { SubjectService } from './subject.service';

@Injectable()
export class LoginService extends BaseService {

  constructor(private restService: RestService, private subjectService: SubjectService) {
    super();
  }

  authenticateLogin(loginData: Login, successCallBack: (res: any) => void, errorCallBack: (error: ValidationError) => void) {
    this.restService.post(REST_URI.AUTH, loginData).subscribe(
      res => this.processSuccessResponse(res, successCallBack),
      (error: any) => {
        this.subjectService.sendButtonLoaderMessage(false);
        this.processErrorResponse(error, errorCallBack);
      });
  }

  userRegistration(data: RegistrationForm, successCallBack: (res: any) => void, errorCallBack: (error: ValidationError) => void) {
    this.restService.post(REST_URI.REGISTER, data).subscribe(
      res => this.processSuccessResponse(res, successCallBack),
      (error: any) => {
        this.subjectService.sendButtonLoaderMessage(false);
        this.processErrorResponse(error, errorCallBack)
      });
  }

  getAccountsetDetail(successCallBack: (res: any) => void, errorCallBack: (error: ValidationError) => void) {
    this.restService.get(REST_URI.GET_ACCOUNT_SETUP).subscribe(
      res => this.processSuccessResponse(res, successCallBack),
      (error: any) => this.processErrorResponse(error, errorCallBack));
  }

  getzipCode(zipCode: string, successCallBack: (res: any) => void, errorCallBack: (error: ValidationError) => void) {
    let params: Map = new HashMap();
    params.put("zipCode", zipCode);
    this.restService.get(REST_URI.GET_ZIPCODE, params).subscribe(
      res => this.processSuccessResponse(res, successCallBack),
      (error: any) => this.processErrorResponse(error, errorCallBack));
  }

  downloadPDF(acNumber: string, successCallBack: (res: any) => void, errorCallBack: (error: ValidationError) => void) {
    this.restService.get(REST_URI.DOWNLOADPDF + acNumber).subscribe(
      res => this.processSuccessResponse(res, successCallBack),
      (error: any) => this.processErrorResponse(error, errorCallBack));
  }

}
