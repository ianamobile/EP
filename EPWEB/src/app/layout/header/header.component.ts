import { SecurityObject } from './../../shared/models/security-object';
import { Router } from '@angular/router';
import { StorageService } from './../../shared/services/storage.service';
import { CONSTANTS, NAVIGATE_URI } from './../../core/constants';
import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {

  securityObject: SecurityObject;

  constructor( 
    private _storageService: StorageService,
    private Router: Router
   ) { 
    var data: SecurityObject = this._storageService.getItem(
        CONSTANTS.SECURITY_OBJ
    );
    this.securityObject = data ? data : new SecurityObject();

   }

 ngOnInit(): void {
 }

 logout() {
   this._storageService.deleteItem(CONSTANTS.SECURITY_OBJ);
   this._storageService.clear();
   this.Router.navigate([NAVIGATE_URI.LOGIN]);
}

}
