import { Component, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatTableDataSource } from '@angular/material/table';
import { setupPageLayout } from '@app-core/common-funcations';
import { ianaAnimations } from '@app-core/iana-animation';
import { IanaConfig } from '@app-models/iana-config';
import { MessageService } from '@app-services/message.service';
import { EpSearchTemplateDialogComponent } from '../ep-search-template-dialog/ep-search-template-dialog.component';


@Component({
  selector: 'app-ep-template',
  templateUrl: './ep-template.component.html',
  styleUrls: ['./ep-template.component.scss'],
  animations: ianaAnimations
})
export class EpTemplateComponent implements OnInit {

  ianaConfig: IanaConfig = new IanaConfig
  selected = 'ACTIVE';
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
    this.dialogRef = this._matDialog.open(EpSearchTemplateDialogComponent, {
      disableClose: true,
      width: '25%',
    });
  }

  ngOnInit(): void {
  }

   displayedColumns: string[] = ['templateid', 'effectiveDate', 'createdDate'];
   dataSource = new MatTableDataSource<PeriodicElement>(ELEMENT_DATA);

  // @ViewChild(MatPaginator) paginator: MatPaginator;

  // ngAfterViewInit() {
  //   this.dataSource.paginator = this.paginator;
  // }

}

export interface PeriodicElement {
  templateid	: string;
  effectiveDate	: string;
  createdDate	: string;
  
}

const ELEMENT_DATA: PeriodicElement[] = [
  { templateid: '861', effectiveDate: '06/16/2020',createdDate:'06/15/2020'},
  { templateid: '862', effectiveDate: '06/17/2020',createdDate:'06/15/2020'}
];