import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatTableDataSource } from '@angular/material/table';
import { setupPageLayout } from '@app-core/common-funcations';
import { ianaAnimations } from '@app-core/iana-animation';
import { IanaConfig } from '@app-models/iana-config';
import { MessageService } from '@app-services/message.service';
import { EpRequirementsDialogComponent } from '../ep-requirements-dialog/ep-requirements-dialog.component';
import { McSpecificSearchDialogComponent } from '../mc-specific-search-dialog/mc-specific-search-dialog.component';

@Component({
  selector: 'app-mclookup',
  templateUrl: './mclookup.component.html',
  styleUrls: ['./mclookup.component.scss'],
  animations: ianaAnimations
})
export class MCLookupComponent implements OnInit {

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
    this.dialogRef = this._matDialog.open(McSpecificSearchDialogComponent, {
      disableClose: true,
      width: '45%',
    });
  }

  epRequirement() {
    this.dialogRef = this._matDialog.open(EpRequirementsDialogComponent, {
      disableClose: true,
      width: '55%',
    });
  }

  displayedColumns: string[] = ['mcName', 'mcSCAC', 'mcAcctNo', 'mcEpStatus', 'overrideUsed', 'epMember', 'knownAs'];
  dataSource = new MatTableDataSource<PeriodicElement>(ELEMENT_DATA);

  ngOnInit(): void {
  }

}


export interface PeriodicElement {

  mcName: string;
  mcSCAC: string;
  mcAcctNo: string;
  mcEpStatus: string;
  overrideUsed: string;
  epMember: string;
  knownAs: string;
}

const ELEMENT_DATA: PeriodicElement[] = [
  { mcName: '101 Transport, Inc.', mcSCAC: 'OZOT', mcAcctNo: 'MC301803', mcEpStatus: 'NOT APPROVED', overrideUsed: 'N', epMember: 'Y', knownAs: 'OZOT' },
  { mcName: '1235496 Ontario Inc.', mcSCAC: 'OFNS', mcAcctNo: 'MC314542', mcEpStatus: 'NOT APPROVED', overrideUsed: 'N', epMember: 'Y', knownAs: 'OFNS' },
  { mcName: '1st Class Transportation & Warehousing Inc.', mcSCAC: 'FCXT', mcAcctNo: 'MC321153', mcEpStatus: 'NOT APPROVED', overrideUsed: 'N', epMember: 'Y', knownAs: 'FCXT' },
  { mcName: '101 Transport, Inc.', mcSCAC: 'OZOT', mcAcctNo: 'MC301803', mcEpStatus: 'NOT APPROVED', overrideUsed: 'N', epMember: 'Y', knownAs: 'OZOT' }

];