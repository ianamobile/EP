import { Component, OnInit, ViewChild } from '@angular/core';
import { FormControl } from '@angular/forms';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { setupPageLayout } from '@app-core/common-funcations';
import { ianaAnimations } from '@app-core/iana-animation';
import { IanaConfig } from '@app-models/iana-config';
import { MessageService } from '@app-services/message.service';

@Component({
  selector: 'app-ep-show-template',
  templateUrl: './ep-show-template.component.html',
  styleUrls: ['./ep-show-template.component.scss'],
  animations: ianaAnimations
})
export class EpShowTemplateComponent implements OnInit {

  ianaConfig: IanaConfig = new IanaConfig();
  
    MemberSpecific = "Yes";
    KnownAs = "Yes";
    RampDetails = "No";
    BlanketAdditional = "Yes";
    Required = "Yes";
    AdditionalInsuredRequired = "Yes";
    EPAllowsSelfInsurance  = "Yes";
    RiskRetentionInsuranceAllowed  = "Yes";
    EPSpecificInsurancePolicyAllowed  = "Yes";
    Policy = "AL";

    constructor(
        private _msgService: MessageService<IanaConfig>,
       
    ) {
        
       //setup public page for removing header, footer & some navigation..
       setupPageLayout(this.ianaConfig, true);
       this._msgService.updateMessage(this.ianaConfig);
       
    }

  ngOnInit(): void {
  }

  disableSelect = new FormControl(false);

  displayedColumns: string[] = ['Policy', 'Required', 'MinimumLimit', 'MaximumDeductibleAllowed','AdditionalInsuredRequired','EPAllowsSelfInsurance','MinBESTRating','RiskRetentionInsuranceAllowed','EPSpecificInsurancePolicyAllowed'];
  dataSource = new MatTableDataSource<InsuranceRequirements>(ELEMENT_DATA);

  displayedColumns1: string[] = ['Policy', 'MinimumLimit', 'MaximumDeductibleAllowed'];
  dataSource1 = new MatTableDataSource<MultipleLimits>(ELEMENT_DATA1);

  displayedColumns2: string[] = ['Description', 'Code', 'Required', 'OriginalRequiredinDays','FilePath','Download'];
  dataSource2 = new MatTableDataSource<AdditionalRequirements>(ELEMENT_DATA2);

  @ViewChild(MatPaginator) paginator: MatPaginator;

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
  }

}


export interface InsuranceRequirements {
  Policy: string;
  Required: string;
  MinimumLimit	: string;
  MaximumDeductibleAllowed	: string;
  AdditionalInsuredRequired	: string;
  EPAllowsSelfInsurance	: string;
  MinBESTRating	: string;
  RiskRetentionInsuranceAllowed	: string;
  EPSpecificInsurancePolicyAllowed: string;
}

const ELEMENT_DATA: InsuranceRequirements[] = [
  {Policy: 'AL', Required: 'YES',MinimumLimit:'1,000,000', MaximumDeductibleAllowed: '0',AdditionalInsuredRequired	: 'YES',EPAllowsSelfInsurance	: 'YES',
  MinBESTRating	: '',RiskRetentionInsuranceAllowed	: 'YES',EPSpecificInsurancePolicyAllowed: 'YES'},
  {Policy: 'GL', Required: 'YES',MinimumLimit:'1,000,000', MaximumDeductibleAllowed: '0',AdditionalInsuredRequired	: 'YES',EPAllowsSelfInsurance	: 'YES',
  MinBESTRating	: '',RiskRetentionInsuranceAllowed	: 'YES',EPSpecificInsurancePolicyAllowed: 'YES'},

];


export interface MultipleLimits {
  Policy: string;
  MinimumLimit	: string;
  MaximumDeductibleAllowed	: string;
 
}

const ELEMENT_DATA1: MultipleLimits[] = [
  {Policy: 'AL', MinimumLimit:'1,000,000', MaximumDeductibleAllowed: '0'},
  {Policy: 'AL', MinimumLimit:'1,000,000', MaximumDeductibleAllowed: '0'},

];



export interface AdditionalRequirements {
  Description: string;
  Code: string;
  Required	: string;
  OriginalRequiredinDays	: string;
  FilePath	: string;
  Download	: string;
  
}

const ELEMENT_DATA2: AdditionalRequirements[] = [
  {Description: 'Addendum', Code: 'ADDM',Required:'No', OriginalRequiredinDays: '0',FilePath	: 'YES',Download	: 'aplu20161124.pdf'},
];