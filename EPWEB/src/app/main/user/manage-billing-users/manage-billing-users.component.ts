import { Component, OnInit, ViewChild } from '@angular/core';
import { FormControl } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { setupPageLayout } from '@app-core/common-funcations';
import { ianaAnimations } from '@app-core/iana-animation';
import { IanaConfig } from '@app-models/iana-config';
import { MessageService } from '@app-services/message.service';
import { DeleteDialogComponent } from '../delete-dialog/delete-dialog.component';
import { UserFormDialogComponent } from '../user-form-dialog/user-form-dialog.component';

@Component({
  selector: 'app-manage-billing-users',
  templateUrl: './manage-billing-users.component.html',
  styleUrls: ['./manage-billing-users.component.scss'],
  animations: ianaAnimations
})
export class ManageBillingUsersComponent implements OnInit {

  ianaConfig: IanaConfig = new IanaConfig
  dialogRef: any;

  constructor(
    private _msgService: MessageService<IanaConfig>,
    public _matDialog: MatDialog,
  ) {

     //setup public page for removing header, footer & some navigation..
     setupPageLayout(this.ianaConfig, true);
     this._msgService.updateMessage(this.ianaConfig);
   }

  ngOnInit(): void {
  }

  addUserModel() {
    this.dialogRef = this._matDialog.open(UserFormDialogComponent, {
      panelClass: 'user-form-dialog',
      disableClose: true,
      width: '45%',
    });

    this.dialogRef.afterClosed().subscribe(result => {
      console.log('Dialog result: close');
    });
    
  }

  deleteUserModel() {
    this._matDialog.open(DeleteDialogComponent);
  }


  displayedColumns: string[] = ['userName', 'password', 'firstName', 'lastName','title','phone','email','Action'];
  dataSource = new MatTableDataSource<PeriodicElement>(ELEMENT_DATA);

  @ViewChild(MatPaginator) paginator: MatPaginator;

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
  }


}



export interface PeriodicElement {
  userName: string;
  password: string;
  firstName: string;
  lastName: string;
  title: string;
  phone: string;
  email: string;
}

const ELEMENT_DATA: PeriodicElement[] = [
  {userName: 'mehul', password: 'test',firstName:'Mehul', lastName: 'Patel',title:'Accountant',phone:'123564646',email:'mehul@ianaoffshore.com'},
  {userName: 'test', password: 'test',firstName:'test', lastName: 'Patel',title:'Accountant',phone:'123564646',email:'test@ianaoffshore.com'},
];