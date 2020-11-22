import { Component, OnInit } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { MatTableDataSource } from '@angular/material/table';

@Component({
  selector: 'app-ep-requirements-dialog',
  templateUrl: './ep-requirements-dialog.component.html',
  styleUrls: ['./ep-requirements-dialog.component.scss']
})
export class EpRequirementsDialogComponent implements OnInit {

  constructor(
    public matDialogRef: MatDialogRef<EpRequirementsDialogComponent>,
  ) { }

  ngOnInit(): void {
  }

  displayedColumns: string[] = ['name', 'youNeed', 'youHave'];
  dataSource = new MatTableDataSource<PeriodicElement>(ELEMENT_DATA);

  displayedColumns1: string[] = ['name', 'youNeed', 'youHave'];
  dataSource1 = new MatTableDataSource<AutoLiability>(ELEMENT_DATA1);

  displayedColumns2: string[] = ['name', 'youNeed', 'youHave'];
  dataSource2 = new MatTableDataSource<GeneralLiability>(ELEMENT_DATA2);

  displayedColumns3: string[] = ['name', 'youNeed', 'youHave'];
  dataSource3 = new MatTableDataSource<Cargo>(ELEMENT_DATA3);

  displayedColumns4: string[] = ['name', 'youNeed', 'youHave'];
  dataSource4 = new MatTableDataSource<TrailerInsurance>(ELEMENT_DATA4);

  displayedColumns5: string[] = ['name', 'youNeed', 'youHave'];
  dataSource5 = new MatTableDataSource<AdditionalRequired>(ELEMENT_DATA5);

  close() {
    this.matDialogRef.close({ close: false });
  }

}



export interface PeriodicElement {
  name: string;
  youNeed: string;
  youHave: string;
}

const ELEMENT_DATA: PeriodicElement[] = [
  { name: 'Ramp Details Required', youNeed: 'N', youHave: 'N' },
  { name: 'See Member Additional Requirement Below', youNeed: 'Y', youHave: 'N' },
];

export interface AutoLiability {
  name: string;
  youNeed: string;
  youHave: string;
}

const ELEMENT_DATA1: AutoLiability[] = [
  { name: 'Minimum Limit', youNeed: '1,000,000', youHave: '-' },
  { name: 'Maximum Deductible Allowed', youNeed: 'NA', youHave: '-' },
  { name: 'EP Allows Self Insurance', youNeed: 'Y', youHave: '-' },
  { name: 'Additional Insured Required', youNeed: 'Y', youHave: 'N' },
  { name: 'Risk Retention Insurance (RRG) Allowed', youNeed: 'N', youHave: '-' },
  { name: 'EP Specific Insurance Policy Allowed', youNeed: 'N', youHave: '-' },
  { name: 'Blanket Additional Insured Allowed', youNeed: 'N', youHave: '-' },
  { name: 'Standard Endorsement Missing(UIIE-1, CA23-17, TE23-17B)	', youNeed: 'N', youHave: 'N' },
  { name: 'Insufficient Type of Auto Policy - Any, Scheduled & hired, or All Owned & hired required', youNeed: 'N', youHave: 'N' },
];


export interface GeneralLiability {
  name: string;
  youNeed: string;
  youHave: string;
}

const ELEMENT_DATA2: GeneralLiability[] = [
  { name: 'Minimum Limit', youNeed: '1,000,000', youHave: '-' },
  { name: 'Maximum Deductible Allowed', youNeed: 'NA', youHave: '-' },
  { name: 'EP Allows Self Insurance', youNeed: 'N', youHave: '-' },
  { name: 'Additional Insured Required', youNeed: 'Y', youHave: 'N' },
  { name: 'Risk Retention Insurance (RRG) Allowed', youNeed: 'Y', youHave: '-' },
  { name: 'EP Specific Insurance Policy Allowed', youNeed: 'Y', youHave: '-' },
  { name: 'Blanket Additional Insured Allowed', youNeed: 'Y', youHave: '-' },

];


export interface Cargo {
  name: string;
  youNeed: string;
  youHave: string;
}

const ELEMENT_DATA3: Cargo[] = [
  { name: 'Minimum Limit', youNeed: '1,000,000', youHave: '-' },
  { name: 'Maximum Deductible Allowed', youNeed: 'NA', youHave: '-' },
  { name: 'EP Allows Self Insurance', youNeed: 'N', youHave: '-' },
  { name: 'Additional Insured Required', youNeed: 'Y', youHave: 'N' },
  { name: 'Risk Retention Insurance (RRG) Allowed', youNeed: 'Y', youHave: '-' },
  { name: 'EP Specific Insurance Policy Allowed', youNeed: 'Y', youHave: '-' },
  { name: 'Blanket Additional Insured Allowed', youNeed: 'Y', youHave: '-' },

];

export interface TrailerInsurance {
  name: string;
  youNeed: string;
  youHave: string;
}

const ELEMENT_DATA4: TrailerInsurance[] = [
  { name: 'Minimum Limit', youNeed: '1,000,000', youHave: '-' },
  { name: 'Maximum Deductible Allowed', youNeed: 'NA', youHave: '-' },
  { name: 'EP Allows Self Insurance', youNeed: 'N', youHave: '-' },
  { name: 'Additional Insured Required', youNeed: 'Y', youHave: 'N' },
  { name: 'Risk Retention Insurance (RRG) Allowed', youNeed: 'Y', youHave: '-' },
  { name: 'EP Specific Insurance Policy Allowed', youNeed: 'Y', youHave: '-' },
  { name: 'Blanket Additional Insured Allowed', youNeed: 'Y', youHave: '-' },

];

export interface AdditionalRequired {
  name: string;
  youNeed: string;
  youHave: string;
}

const ELEMENT_DATA5: AdditionalRequired[] = [
  { name: 'Addendum', youNeed: 'N', youHave: 'N' },
  { name: 'Download Member Instructions', youNeed: 'Y', youHave: 'N' },
];