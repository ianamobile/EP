import { Router } from '@angular/router';
import { Component, OnInit } from '@angular/core';
import { CONSTANTS, NAVIGATE_URI } from '@app-core/constants';
import { SecurityObject } from '@app-models/security-object';
import { StorageService } from '@app-services/storage.service';

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
