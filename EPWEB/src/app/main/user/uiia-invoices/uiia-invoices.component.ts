import { Component, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { setupPageLayout } from '@app-core/common-funcations';
import { ianaAnimations } from '@app-core/iana-animation';
import { IanaConfig } from '@app-models/iana-config';
import { MessageService } from '@app-services/message.service';
import { PaymentFailureAttemptsDialogComponent } from '../payment-failure-attempts-dialog/payment-failure-attempts-dialog.component';

@Component({
  selector: 'app-uiia-invoices',
  templateUrl: './uiia-invoices.component.html',
  styleUrls: ['./uiia-invoices.component.scss'],
  animations: ianaAnimations
})
export class UiiaInvoicesComponent implements OnInit {

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

  viewPaymentFailureAttempts() {
    this.dialogRef = this._matDialog.open(PaymentFailureAttemptsDialogComponent, {
      disableClose: true,
      width: '60%',
    });
    
  }

  ngOnInit(): void {
  }

  displayedColumns: string[] = ['invoiceNo', 'invoiceDate', 'invoiceAmount', 'invoiceStatus','paymentDate','paidAmount','Action'];
  dataSource = new MatTableDataSource<PeriodicElement>(ELEMENT_DATA);

  @ViewChild(MatPaginator) paginator: MatPaginator;

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
  }

}


export interface PeriodicElement {
  invoiceNo: string;
  invoiceDate: string;
  invoiceAmount: string;
  invoiceStatus: string;
  paymentDate: string;
  paidAmount: string;
}

const ELEMENT_DATA: PeriodicElement[] = [
  {invoiceNo: '296446', invoiceDate: '2016-11-03',invoiceAmount:'44,556.90', invoiceStatus: 'CLOSED',paymentDate:'2017-01-17',paidAmount:'44,556.90'},
  {invoiceNo: '296447', invoiceDate: '2016-11-04',invoiceAmount:'43,456.90', invoiceStatus: 'OPEN',paymentDate:'2017-01-18',paidAmount:'43,456.90'},
  
];