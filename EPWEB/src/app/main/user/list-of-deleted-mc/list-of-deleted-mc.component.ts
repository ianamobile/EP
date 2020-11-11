import { Component, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { setupPageLayout } from '@app-core/common-funcations';
import { ianaAnimations } from '@app-core/iana-animation';
import { IanaConfig } from '@app-models/iana-config';
import { MessageService } from '@app-services/message.service';
import { DeletedsearchMCComponent } from '../deletedsearch-mc/deletedsearch-mc.component';

@Component({
  selector: 'app-list-of-deleted-mc',
  templateUrl: './list-of-deleted-mc.component.html',
  styleUrls: ['./list-of-deleted-mc.component.scss'],
  animations: ianaAnimations
})
export class ListOfDeletedMCComponent implements OnInit {

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

  openSearchModel() {
    this.dialogRef = this._matDialog.open(DeletedsearchMCComponent, {
      panelClass: 'notification-dialog-container',
      disableClose: true,
      width: '45%',
    });

    this.dialogRef.afterClosed().subscribe(result => {
      console.log('Dialog result: ${result}');
    });
  }

  ngOnInit(): void {
  }

  displayedColumns: string[] = ['companyName', 'accountNo', 'SCACCode', 'cancelledDate','deletedDate','UIIAStatusCode'];
  dataSource = new MatTableDataSource<PeriodicElement>(ELEMENT_DATA);

  @ViewChild(MatPaginator) paginator: MatPaginator;

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
  }

}


export interface PeriodicElement {

  companyName	: string;
accountNo	: string;
SCACCode	: string;
cancelledDate	: string;
deletedDate	: string;
UIIAStatusCode: string;
}

const ELEMENT_DATA: PeriodicElement[] = [
  {companyName: '1 Sly Transport Corp.', accountNo: 'MC306488',SCACCode:'OSYC', cancelledDate: '06/15/2013',deletedDate:'07/05/2020',UIIAStatusCode:'DELETED (C1,C5)'},
  {companyName: '101 Trucking', accountNo: 'MC306489',SCACCode:'OZMV', cancelledDate: '08/19/2014',deletedDate:'07/05/2020',UIIAStatusCode:'DELETED (C5)'},
  
];