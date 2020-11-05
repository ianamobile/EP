import { SecurityObject } from './../shared/models/security-object';
import { StorageService } from './../shared/services/storage.service';
import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot } from '@angular/router';
import { SnotifyService } from 'ng-snotify';
import { CONSTANTS, NAVIGATE_URI } from './constants';

@Injectable({
  providedIn: 'root'
})

export class AuthGurdService {

  constructor(
    private Router: Router,
    private _storageService: StorageService,
    private _snotifyService: SnotifyService
    )
     {

  }
  isLoggedIn(): boolean {
    try {
      const theUser: SecurityObject = this._storageService.getItem(CONSTANTS.SECURITY_OBJ);
      if (theUser) {
        return true;
      } else {
        return false;
      }
    } catch (e) {
      return false;
    }
  }

  canActivate(_RoutesActive: ActivatedRouteSnapshot, _state: RouterStateSnapshot): boolean {
    if (this.isLoggedIn()) {
      return true;
    }

    this._snotifyService.warning("You are not allowed to access this page. Please login first.", CONSTANTS.SnotifyToastNotificationConfig);
    this._storageService.clear();
    this.Router.navigate([NAVIGATE_URI.LOGIN]);
    return false;

  }

} 
