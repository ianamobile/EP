import { Map } from './../../core/map';
import { HashMap } from './../../core/hashmap';
import { Injectable } from '@angular/core';
import { ValidationError } from './../models/validation-error';
import { REST_URI } from './../../core/constants';
import { BaseService } from './../../core/base-service';
import { RestService } from './http-rest.service';
 

@Injectable({
  providedIn: 'root'
})
export class DashboardService extends BaseService {

  constructor(private restService: RestService) {
    super();
  }

  getDashboardData(data: any, successCallBack: (res: any) => void, errorCallBack: (error: ValidationError) => void) {
    this.restService.get(REST_URI.DASHBOARD, data).subscribe(
      res => this.processSuccessResponse(res, successCallBack),
      (error: any) => this.processErrorResponse(error, errorCallBack));
  }


  getPoliciesByTimeRange(url: string, successCallBack: (res: any) => void, errorCallBack: (error: ValidationError) => void) {
    let params: Map = new HashMap();
    params.put("timeRange", url);
    this.restService.get(REST_URI.POLICIES_BY_TIMERANGE, params).subscribe(
      res => this.processSuccessResponse(res, successCallBack),
      (error: any) => this.processErrorResponse(error, errorCallBack));
  }


}
