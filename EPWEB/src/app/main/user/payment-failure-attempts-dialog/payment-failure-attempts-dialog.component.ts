import { Component, OnInit } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { MatTableDataSource } from '@angular/material/table';

@Component({
  selector: 'app-payment-failure-attempts-dialog',
  templateUrl: './payment-failure-attempts-dialog.component.html',
  styleUrls: ['./payment-failure-attempts-dialog.component.scss']
})
export class PaymentFailureAttemptsDialogComponent implements OnInit {

  constructor(
    public matDialogRef: MatDialogRef<PaymentFailureAttemptsDialogComponent>,
  ) { }

  ngOnInit(): void {
  }

  close() {
    this.matDialogRef.close({  close: false });
  }

  displayedColumns: string[] = ['SrNo', 'Errordic', 'ErrorDescription', 'PaymentAttemptBy','PaymentAttemptDate'];
  dataSource = new MatTableDataSource<PeriodicElement>(ELEMENT_DATA);

}


export interface PeriodicElement {
  SrNo: string;
  Errordic: string;
  ErrorDescription: string;
  PaymentAttemptBy: string;
  PaymentAttemptDate: string;
  
}

const ELEMENT_DATA: PeriodicElement[] = [
  {SrNo: '1', Errordic: 'Postal/Zip code doesnt match the information associated with the credit card being used. and street address doesnt match the information associated with the credit card being used.',ErrorDescription:'', PaymentAttemptBy: 'MC004792',PaymentAttemptDate:'2017-01-17'},
  {SrNo: '1', Errordic: 'US bank account payment method must be verified prior to transaction.',ErrorDescription:'', PaymentAttemptBy: 'MC004792',PaymentAttemptDate:'2017-01-18'},
  
];