import { Component, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { setupPageLayout } from '@app-core/common-funcations';
import { ianaAnimations } from '@app-core/iana-animation';
import { IanaConfig } from '@app-models/iana-config';
import { MessageService } from '@app-services/message.service';
import { DeleteDialogComponent } from '../delete-dialog/delete-dialog.component';
import { SearchSecondaryDialogComponent } from '../search-secondary-dialog/search-secondary-dialog.component';
import { SecondaryUserFormDialogComponent } from '../secondary-user-form-dialog/secondary-user-form-dialog.component';

@Component({
  selector: 'app-secondary-user-details',
  templateUrl: './secondary-user-details.component.html',
  styleUrls: ['./secondary-user-details.component.scss'],
  animations: ianaAnimations
})
export class SecondaryUserDetailsComponent implements OnInit {

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

  openSearchModel() {
    this.dialogRef = this._matDialog.open(SearchSecondaryDialogComponent, {
      disableClose: true,
      width: '55%',
    });

    // this.dialogRef.afterClosed().subscribe(result => {
    //   console.log('Dialog result: ');
    // });
  }

  openAddMCDialog() {
    this.dialogRef = this._matDialog.open(SecondaryUserFormDialogComponent, {
      disableClose: true,
      width: '45%',
    });
  }

  deleteUserModel() {
    this._matDialog.open(DeleteDialogComponent);
  }


  displayedColumns: string[] = ['userName', 'password', 'email', 'download','attr1','attr2','Action'];
  dataSource = new MatTableDataSource<PeriodicElement>(ELEMENT_DATA);

  @ViewChild(MatPaginator) paginator: MatPaginator;

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
  }

}


export interface PeriodicElement {

  userName	: string;
  password	: string;
  email	: string;
  download	: string;
  attr1	: string;
  attr2: string;
  
}

const ELEMENT_DATA: PeriodicElement[] = [
  {userName: 'Greg Stefflre', password: '*****',email:'Greg@ianaoffshore.com', download: 'YES',attr1:'YES',attr2:'YES'},
  {userName: 'Martin Stefflre', password: '*****',email:'Martin@ianaoffshore.com', download: 'YES',attr1:'NO',attr2:'YES'},
  {userName: 'Erin', password: '*****',email:'Erin@ianaoffshore.com', download: 'YES',attr1:'YES',attr2:'NO'},
  {userName: 'Sharon Brooks', password: '*****',email:'Sharon@ianaoffshore.com', download: 'NO',attr1:'YES',attr2:'YES'},
  {userName: 'Piyush', password: '*****',email:'Piyush@ianaoffshore.com', download: 'YES',attr1:'YES',attr2:'NO'},
  
  
];