import { notificationsSearchForm } from './../forms/notifications-search-form';
import { HashMap } from './../../core/hashmap';
import { Map } from './../../core/map';
import { ValidationError } from './../models/validation-error';
import { REST_URI } from './../../core/constants';
import { BaseService } from './../../core/base-service';
import { Injectable } from '@angular/core';
import { RestService } from './http-rest.service';
import { HttpResponse } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class NotificationService extends BaseService {

  constructor(private restService: RestService) {
    super();
  }

  getsetupNotification(successCallBack: (res: any) => void, errorCallBack: (error: ValidationError) => void) {
    this.restService.get(REST_URI.SETUP_NOTIFICATION).subscribe(
      res => this.processSuccessResponse(res, successCallBack),
      (error: any) => this.processErrorResponse(error, errorCallBack));
  }

  getNotificationList(data: notificationsSearchForm, successCallBack: (res: any) => void, errorCallBack: (error: ValidationError) => void) {
    let params: Map = new HashMap();
    params.put("pageIndex", data.pageIndex);
    params.put("pageSize", data.pageSize);
    params.put("fromDate", data.fromDate);
    params.put("toDate", data.toDate);
    params.put("mode", data.mode);
    params.put("status", data.status);
    params.put("key", data.key);
    this.restService.get(REST_URI.NOTIFICATIONS, params).subscribe(
      res => this.processSuccessResponse(res, successCallBack),
      (error: any) => this.processErrorResponse(error, errorCallBack));
  }

  downloadPDF(data: any, successCallBack: (res: HttpResponse<Blob>) => void, errorCallBack: (error: ValidationError) => void) {
    let params: Map = new HashMap();
    params.put("selectedIds", data.selectedIds);
    params.put("mode", data.mode);
    params.put("status", data.status); 
    this.restService.get(REST_URI.NOTIFICATIONS_DOWNLOAD_PDF, params,{ responseType: 'blob' , observe: 'response' }).subscribe(
      res => this.processSuccessResponse(res, successCallBack),
      (error: any) => this.processErrorResponse(error, errorCallBack));
  }


}
