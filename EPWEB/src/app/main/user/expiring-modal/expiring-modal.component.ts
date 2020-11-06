
import { MatSort } from '@angular/material/sort';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { Component, OnInit, Inject, ViewEncapsulation, ViewChild } from '@angular/core';
import { SnotifyService } from "ng-snotify";
import { NgxSpinnerService } from 'ngx-spinner';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { CONSTANTS } from '@app-core/constants';
import { ianaAnimations } from '@app-core/iana-animation';
import { ValidationError } from '@app-models/validation-error';
import { DashboardService } from '@app-services/dashboard.service';

@Component({
  selector: 'app-expiring-modal',
  templateUrl: './expiring-modal.component.html',
  styleUrls: ['./expiring-modal.component.scss'],
  encapsulation: ViewEncapsulation.None,
  animations: ianaAnimations
})
export class ExpiringModalComponent implements OnInit {

  dataSource = new MatTableDataSource();
  @ViewChild(MatPaginator, { static: false }) paginator: MatPaginator;
  @ViewChild(MatSort, { static: true }) sort: MatSort;

  displayedColumns = [
    "mcName",
    "alExpDate",
    "glExpDate",
    "cargoExpDate",
    "tiExpDate",
    "wcExpDate",
    "empLiabExpDate",
    "contCargoExpDate",
    "refTrailerExpDate",
    "empDishHonestyExpDate",
    "umbExpDate"
  ];

  data: any = {};

  constructor(
    public matDialogRef: MatDialogRef<ExpiringModalComponent>,
    @Inject(MAT_DIALOG_DATA) private _data: any,
    private _dashboardService: DashboardService,
    private _snotifyService: SnotifyService,
    private _ngxSpinnerService: NgxSpinnerService
  ) {
    this.data = _data;
  }

  ngOnInit() {
    setTimeout(() => {
      this.getPoliciesByTimeRange(this.data.API_URL_TYPE);
    }, 0);
  }

  getPoliciesByTimeRange(API_URL) {
    this._ngxSpinnerService.show();
    this._dashboardService.getPoliciesByTimeRange(API_URL, (res: any) => {
      this._ngxSpinnerService.hide();
      this.dataSource.data = res;
      setTimeout(() => {
        this.dataSource.paginator = this.paginator;
        this.dataSource.sort = this.sort;
      }, 1);
    },
      (error: ValidationError) => {
        let err = error.obj.apiReqErrors.errors[0];
        if (err.errorMessage !== "No Records Found") {
          this._snotifyService.error(
            err.errorMessage,
            CONSTANTS.ERR_TITLE,
            CONSTANTS.SnotifyToastNotificationConfig
          );
        }
      }
    );
  }

  close() {
    this.matDialogRef.close();
  }

}
