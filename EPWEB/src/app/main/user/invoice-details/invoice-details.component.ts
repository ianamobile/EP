import { Component, OnInit, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { setupPageLayout } from '@app-core/common-funcations';
import { ianaAnimations } from '@app-core/iana-animation';
import { IanaConfig } from '@app-models/iana-config';
import { MessageService } from '@app-services/message.service';

@Component({
  selector: 'app-invoice-details',
  templateUrl: './invoice-details.component.html',
  styleUrls: ['./invoice-details.component.scss'],
  animations: ianaAnimations
})
export class InvoiceDetailsComponent implements OnInit {

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

  displayedColumns: string[] = ['billCode', 'description', 'GLAccNo', 'amount'];
  dataSource = new MatTableDataSource<PeriodicElement>(ELEMENT_DATA);

  displayedColumns1: string[] = ['batchCode', 'paymentDate', 'paidAmount', 'paymentMode','chequeNoAuthCode'];
  dataSource1 = new MatTableDataSource<PeriodicElement1>(ELEMENT_DATA1);


  @ViewChild(MatPaginator) paginator: MatPaginator;

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
  }

}


export interface PeriodicElement {
  billCode	: string;
  description	: string;
  GLAccNo	: string;
  amount	: string;
}

const ELEMENT_DATA: PeriodicElement[] = [
  {billCode: '30', description: 'UIIA Standard Interchange Contract -- Equipment Provider Annual Service Fee',GLAccNo:'14210221', amount: '44556.90'},
  {billCode: '31', description: 'UIIA Standard Interchange Contract -- Equipment Provider ',GLAccNo:'14210223', amount: '44554.90'},
  {billCode: '32', description: 'Equipment Provider ',GLAccNo:'14210223', amount: '44554.90'},
  {billCode: '33', description: 'UIIA Standard Interchange',GLAccNo:'14210224', amount: '44554.90'},
  
];

export interface PeriodicElement1 {
  batchCode	: string;
  paymentDate	: string;
  paidAmount	: string;
  paymentMode	: string;
  chequeNoAuthCode	: string;
}

const ELEMENT_DATA1: PeriodicElement1[] = [
  {batchCode: '170117LCHK', paymentDate: '01/17/2020',paidAmount:'44,556.90', paymentMode: 'Cheque',chequeNoAuthCode :'7071924'},
  {batchCode: '180117LBHK', paymentDate: '01/18/2020',paidAmount:'44,556.90', paymentMode: 'Cheque',chequeNoAuthCode :'7071924'},
  {batchCode: '190117LAHK', paymentDate: '01/19/2020',paidAmount:'44,556.90', paymentMode: 'Cheque',chequeNoAuthCode :'7071924'},
  {batchCode: '130117LFHK', paymentDate: '01/20/2020',paidAmount:'44,556.90', paymentMode: 'Cheque',chequeNoAuthCode :'7071924'},
  
];