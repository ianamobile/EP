import { Component, OnInit, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { setupPageLayout } from '@app-core/common-funcations';
import { ianaAnimations } from '@app-core/iana-animation';
import { IanaConfig } from '@app-models/iana-config';
import { MessageService } from '@app-services/message.service';

@Component({
  selector: 'app-monthly-book-weekly-supplement',
  templateUrl: './monthly-book-weekly-supplement.component.html',
  styleUrls: ['./monthly-book-weekly-supplement.component.scss'],
  animations: ianaAnimations
})
export class MonthlyBookWeeklySupplementComponent implements OnInit {

  ianaConfig: IanaConfig = new IanaConfig

  constructor(
    private _msgService: MessageService<IanaConfig>,
  ) {

    //setup public page for removing header, footer & some navigation..
    setupPageLayout(this.ianaConfig, true);
    this._msgService.updateMessage(this.ianaConfig);
   }

  ngOnInit(): void {
  }

  displayedColumns: string[] = ['typeOfReport', 'createdDate', 'fileNames'];
  dataSource = new MatTableDataSource<PeriodicElement>(ELEMENT_DATA);

  @ViewChild(MatPaginator) paginator: MatPaginator;

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
  }

}


export interface PeriodicElement {

  typeOfReport	: string;
  createdDate	: string;
  fileNames	: string;
}

const ELEMENT_DATA: PeriodicElement[] = [
  {typeOfReport: 'Supplement', createdDate: '06/15/2020',fileNames:'S13112020.pdf'},
  {typeOfReport: 'Supplement', createdDate: '06/15/2020',fileNames:'S1692020.pdf'},
  {typeOfReport: 'Book', createdDate: '06/15/2015',fileNames:'B2652017.pdf'},
  {typeOfReport: 'Book', createdDate: '06/15/2019',fileNames:'B2452017.pdf'},
  {typeOfReport: 'Supplement', createdDate: '06/15/2018',fileNames:'S2452017.pdf'},
  {typeOfReport: 'Book', createdDate: '04/25/2017',fileNames:'S2542017.pdf'},

  
];