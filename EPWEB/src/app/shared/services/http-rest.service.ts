import { AppError } from './../models/app-error';
import { UnauthorizedError } from './../models/unauthorized-error';
import { ValidationError } from './../models/validation-error';
import { Map } from './../../core/map';
import { REST_URI } from './../../core/constants';
import { HttpClient, HttpErrorResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { throwError, Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { StorageService } from './storage.service';


@Injectable({
  providedIn: 'root'
})
export class RestService {

  baseURL: any = REST_URI.BASE_URL;

  constructor(public http: HttpClient, public store: StorageService) { }

  post(url: string, params: any): Observable<Object> {
    let body = new URLSearchParams();
    for (let key in params) {
      body.append(key, params[key]);
    }
    return this.http.post(this.baseURL + url, params).pipe(
      catchError(this.handleError)
    );
  }

  put(url: string, params: any): Observable<Object> {
    let body = new URLSearchParams();
    for (let key in params) {
      body.append(key, params[key]);
    }
    return this.http.put(this.baseURL + url, params).pipe(
      catchError(this.handleError)
    );
  }

  get(url: string, searchParams?: Map, options?: {
    headers?: HttpHeaders | {
      [header: string]: string | string[];
    };
    observe: 'response';
    params?: HttpParams | {
      [param: string]: string | string[];
    };
    reportProgress?: boolean;
    responseType: 'blob';
    withCredentials?: boolean;
  }
  ) {
    var searchQueryString: string = "";
    if (searchParams && !searchParams.isEmpty()) {
      searchQueryString = this.buildSearchQueryURI(searchParams);
    }
    let finalURL: string = this.baseURL + url;
    if (searchQueryString)
      finalURL = finalURL + "?" + searchQueryString
    return this.http.get(finalURL, options).pipe(
      catchError(this.handleError)
    );
  }



  private buildSearchQueryURI(searchParams: Map): string {

    let searchQueryURI = "";
    var regex = /[^a-zA-Z,@/ 0-9._-]/g;
    searchParams.getKeys().forEach(key => {
      if (key) {
        let _value: string = searchParams.get(key);
        _value = _value.replace(/  +/g, ' ');
        _value = _value.replace(regex, "")
        if (_value == 'undefined') {
          _value = "";
        }
        searchQueryURI += key + "=" + _value + "&";
      }
    });
    return searchQueryURI ? searchQueryURI.slice(0, -1) : "";
  }


  private handleError(err: HttpErrorResponse) {

    // if (err && err.status == 422 && err.error && err.error.apiReqErrors &&
    //   err.error.apiReqErrors.errors && err.error.apiReqErrors.errors[0].errorMessage === "No Records Found") {
    //   return throwError(new AppError(JSON.parse(JSON.stringify(err.error))));
    // }
    if (err.status == 422)
      return throwError(new ValidationError(JSON.parse(JSON.stringify(err.error))));

    if (err.status == 401)
      return throwError(new UnauthorizedError(err));

    return throwError(new AppError(JSON.parse(JSON.stringify(err.error))));

  }

}
