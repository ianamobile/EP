import { ErrorHandler, Injectable } from '@angular/core';
import { SnotifyService } from 'ng-snotify';
import { NgxSpinnerService } from 'ngx-spinner';
import { AppError } from '../shared/models/app-error';
import { UnauthorizedError } from '../shared/models/unauthorized-error';
import { CONSTANTS } from './constants';

@Injectable({
  providedIn: 'root'
})
export class GlobalErrorHandlerService implements ErrorHandler {
  constructor(
    private _snotifyService: SnotifyService,
    private _ngxSpinnerService: NgxSpinnerService
  ) { }
  handleError(error: Error | AppError | UnauthorizedError) {
    let message: string;
    //let stackTrace;
    console.log(error);

    if (error instanceof AppError) {
      // Server error
      message = this.getServerErrorMessage(error);
      //stackTrace = errorService.getServerErrorStackTrace(error);
      // notifier.showError(message);
    } else if (error instanceof UnauthorizedError) {
      // Server error
      message = this.getServerUnauthorizedErrorMessage(error);

    } else {
      // Client Error
      message = this.getClientErrorMessage(error as Error);
      console.log("Client Error:" + message);

      message = "An error has occured while processing your request.";
      // notifier.showError(message);
    }
    this.logError(message);
    this._ngxSpinnerService.hide();
  }
  getClientErrorMessage(error: Error): string {
    return error.message ? error.message : error.toString();
  }

  getServerErrorMessage(error: AppError): string {
    let errorServerMessage = "Something went wrong while processing your request.";
    if (error && error.obj && error.obj.apiReqErrors && error.obj.apiReqErrors.errors) {
      errorServerMessage = error.obj.apiReqErrors.errors[0].errorMessage;
    }
    return navigator.onLine ? errorServerMessage : 'No Internet Connection';
  }

  getServerUnauthorizedErrorMessage(error: UnauthorizedError): string {
    return error.message ? error.message : error.toString();
  }

  logError(message: string) {
    console.log('Global Logging Error:' + message);
      // Send errors to server here
      this._snotifyService.error(message, CONSTANTS.ERR_TITLE, CONSTANTS.SnotifyToastNotificationConfig);
    console.log(message)
  }

}