import { REST_URI, CONSTANTS } from './constants';
import { HttpHandler, HttpRequest } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { SecurityObject } from '../shared/models/security-object';
import { StorageService } from '../shared/services/storage.service';



@Injectable({
  providedIn: 'root'
})
export class HttpsInterceptorService {

  securityObject: SecurityObject;
  SKIP_URLS: string[];

  constructor(private _storageService: StorageService) {
    this.SKIP_URLS = [REST_URI.AUTH, REST_URI.FORGOT_PASSWORD, REST_URI.RESET_PASSWORD,
    REST_URI.VALIDATE_FORGOTPWD_LINK, REST_URI.REGISTER,
    REST_URI.GET_ZIPCODE, REST_URI.GET_ACCOUNT_SETUP, REST_URI.DOWNLOADPDF];
  }

  intercept(req: HttpRequest<any>, next: HttpHandler) {
    var data: SecurityObject = this._storageService.getItem(CONSTANTS.SECURITY_OBJ);
    this.securityObject = data ? data : new SecurityObject();

    console.log(`AddHeaderInterceptor - ${req.url}`);
    //console.log(this.securityObject.accessToken);

    if (!this.isURLSkippable(req.url)) {
      // add authorization header with jwt token if available
      let Request: HttpRequest<any> = req.clone({
        setHeaders: {
          "Authorization": `Bearer ${this.securityObject.accessToken}`
        }
      });
      return next.handle(Request);
    }
    return next.handle(req);
  }

  private isURLSkippable(requestURL: string): boolean {
    let finalURL = REST_URI.BASE_URL + requestURL;
    return this.SKIP_URLS.some(e => finalURL.indexOf(e) > -1);
  }
}
