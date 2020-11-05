import { MatDialog } from '@angular/material/dialog';
import { ianaAnimations } from '@app-core/iana-animation';
import { MatSort } from '@angular/material/sort';
import { CONSTANTS } from '@app-core/constants';
import { Component, OnInit, ViewEncapsulation, ViewChild, Input } from '@angular/core';
import { SecurityObject } from '@app-models/security-object';
import { StorageService } from '@app-services/storage.service';
import { NotificationSearchDialogComponent } from './notification-search-dialog/notification-search-dialog.component';
import { NotificationService } from '@app-services/notification.service';
import { ValidationError } from '@app-models/validation-error';
import { SnotifyService } from 'ng-snotify';
import { NgxSpinnerService } from 'ngx-spinner';
import { notificationsSearchForm } from '@app-forms/notifications-search-form';
import { tableListResponse } from '@app-models/table-list-response';
import { DatePipe } from '@angular/common';
import { HttpResponse } from '@angular/common/http';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { setupPageLayout } from '@app-core/common-funcations';
import { IanaConfig } from '@app-models/iana-config';
import { MessageService } from '@app-services/message.service';
import { FormControl } from '@angular/forms';
import { AfterViewInit } from '@angular/core';
 

@Component({
  selector: 'app-notifications',
  templateUrl: './notifications.component.html',
  styleUrls: ['./notifications.component.scss'],
  encapsulation: ViewEncapsulation.None,
  animations: ianaAnimations
})
export class NotificationsComponent implements OnInit, AfterViewInit {

  ianaConfig: IanaConfig = new IanaConfig

  // securityObject: SecurityObject;
  // displayedColumns = [
  //   "checkbox", "notifName", "status", "mode", "notifDate"
  // ];
  // dialogRef: any;
  // dataSource = new MatTableDataSource();
  // searchSetupData: any = {};
  // notificationsSearchForm = new notificationsSearchForm({});
  // @ViewChild(MatPaginator, { static: false }) paginator: MatPaginator;
  // @ViewChild(MatSort, { static: true }) sort: MatSort;
  // pageIndex: number;
  // pageSize: number;
  // length: number;
  // isSelectedAll: boolean = false;

  constructor(
    private _msgService: MessageService<IanaConfig>,
    // private _storageService: StorageService,
    // private _matDialog: MatDialog,
    // private _notificationService: NotificationService,
    // private _snotifyService: SnotifyService,
    // private _spinner: NgxSpinnerService,
    // public datepipe: DatePipe
  ) {

    //setup public page for removing header, footer & some navigation..
    setupPageLayout(this.ianaConfig, true);
    this._msgService.updateMessage(this.ianaConfig);

    // var data: SecurityObject = this._storageService.getItem(CONSTANTS.SECURITY_OBJ);
    // this.securityObject = data ? data : new SecurityObject();
    // this.notificationsSearchForm.mode = 'MAIL';
  }

  ngOnInit() {
    // this.getsetupNotification();
    // this.getNotificationList();
  }

  // openSearchModel() {
  //   this.dialogRef = this._matDialog.open(NotificationSearchDialogComponent, {
  //     panelClass: 'notification-dialog-container',
  //     disableClose: true,
  //     width: '45%',
  //     data: {
  //       searchSetupData: this.searchSetupData,
  //       formData: this.notificationsSearchForm
  //     }
  //   });
  //   this.dialogRef.afterClosed()
  //     .subscribe((res) => {
  //       if (res.close) {
  //         this.notificationsSearchForm = res.value;
  //         this.getNotificationList();
  //       }
  //     });
  // }

  // getsetupNotification() {
  //   this._spinner.show();
  //   this._notificationService.getsetupNotification((res: any) => {
  //     this._spinner.hide();
  //     this.searchSetupData = res;
  //   },
  //     (error: ValidationError) => {
  //       this._spinner.hide();
  //       let err = error.obj.apiReqErrors.errors[0];
  //       this._snotifyService.error(
  //         err.errorMessage,
  //         CONSTANTS.ERR_TITLE,
  //         CONSTANTS.SnotifyToastNotificationConfig
  //       );
  //     }
  //   );
  // }

  // getNotificationList() {

  //   if (undefined != this.paginator) {
  //     if (undefined == this.paginator.pageSize) {
  //       this.notificationsSearchForm.pageSize = CONSTANTS.DEFAULT_TOTALRECORD;
  //     } else {
  //       this.notificationsSearchForm.pageSize = this.paginator.pageSize;
  //     }
  //     this.notificationsSearchForm.pageIndex = this.paginator.pageIndex + 1;
  //   }

  //   this.notificationsSearchForm.key = this.securityObject.accessToken;
  //   this.notificationsSearchForm.fromDate = this.datepipe.transform(this.notificationsSearchForm.fromDate, 'MM/dd/yyyy');
  //   this.notificationsSearchForm.toDate = this.datepipe.transform(this.notificationsSearchForm.toDate, 'MM/dd/yyyy');
  //   this.isSelectedAll = false;
  //   this._spinner.show();
  //   this._notificationService.getNotificationList(this.notificationsSearchForm, (res: any) => {
  //     this._spinner.hide();
  //     let result: tableListResponse<notificationsSearchForm> = res;
  //     this.dataSource.data = result.resultList;
  //     this.length = result.page.totalElements;
  //     this.pageIndex = result.page.currentPage - 1;
  //     this.pageSize = result.page.size;
  //   },
  //     (error: ValidationError) => {
  //       this.dataSource.data = [];
  //       this.length = 0;
  //       this.pageIndex = 0;
  //       this.pageSize = 0;
  //       this._spinner.hide();
  //       let err = error.obj.apiReqErrors.errors[0];
  //       if (err.errorMessage !== "No Records Found") {
  //         this._snotifyService.error(
  //           err.errorMessage,
  //           CONSTANTS.ERR_TITLE,
  //           CONSTANTS.SnotifyToastNotificationConfig
  //         );
  //       }
  //     }
  //   );
  // }

  // removeSearchField(field) {
  //   this.notificationsSearchForm[field] = "";
  //   this.getNotificationList();
  // }

  // clear() {
  //   this.notificationsSearchForm.fromDate = "";
  //   this.notificationsSearchForm.toDate = "";
  //   this.notificationsSearchForm.mode = "MAIL";
  //   this.notificationsSearchForm.status = "";
  //   this.getNotificationList();
  // }

  // selectAll() {
  //   if (this.dataSource.data && this.dataSource.data.length > 0) {
  //     this.dataSource.data.map(e => {
  //       e['isChecked'] = this.isSelectedAll;
  //     });
  //   }
  // }

  // checkSelectAll() {
  //   this.isSelectedAll = true;
  //   if (this.dataSource.data && this.dataSource.data.length > 0) {
  //     this.dataSource.data.map(e => {
  //       if (!e['isChecked'])
  //         this.isSelectedAll = false;
  //     });
  //   }
  // }

  // downLoadSelected() {

  //   let isCheckedList = [];
  //   if (this.dataSource.data && this.dataSource.data.length > 0) {
  //     this.dataSource.data.map(e => {
  //       if (e['isChecked'] == true) {
  //         isCheckedList.push(e['notifId']);
  //       }
  //     });
  //   }

  //   if (isCheckedList.length == 0) {
  //     this._snotifyService.error("No Notification has Been Selected", CONSTANTS.SnotifyToastNotificationConfig);
  //   } else {
  //     this._spinner.show();
  //     const data = {
  //       selectedIds: isCheckedList.join(),
  //       mode: this.notificationsSearchForm.mode,
  //       status: this.notificationsSearchForm.status
  //     }
  //     this._notificationService.downloadPDF(data, (res: HttpResponse<Blob>) => {
  //       this._spinner.hide();
  //       var fileName = res.headers.get('content-disposition').split(';')[1].split('filename')[1].split('=')[1].trim();
  //       //var fileName = "notification_" + new Date().getTime();
  //       const element = document.createElement('a');
  //       let myBlob: Blob = new Blob([res.body], { type: CONSTANTS.PDF_TYPE }); // replace the type by whatever type is your response
  //       var downloadURL = URL.createObjectURL(myBlob);
  //       element.href = downloadURL;
  //       element.download = fileName;
  //       document.body.appendChild(element);
  //       element.click();
  //       this._snotifyService.success("PDF generated  successfully", CONSTANTS.SnotifyToastNotificationConfig);
  //       this.getNotificationList();
  //     },
  //       (error: ValidationError) => {
  //         this._spinner.hide();
  //         let err = error.obj.apiReqErrors.errors[0];
  //         this._snotifyService.error(err.errorMessage, CONSTANTS.ERR_TITLE, CONSTANTS.SnotifyToastNotificationConfig
  //         );
  //       });
  //   }

  // }
  disableSelect = new FormControl(false);

  displayedColumns: string[] = ['NotificationName', 'Status', 'Mode', 'NotificationDate'];
  dataSource = new MatTableDataSource<PeriodicElement>(ELEMENT_DATA);

  @ViewChild(MatPaginator) paginator: MatPaginator;

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
  }

}



export interface PeriodicElement {
  NotificationName: string;
  Status: string;
  Mode: string;
  NotificationDate: string;
}

const ELEMENT_DATA: PeriodicElement[] = [
  {NotificationName: 'MCINVLDIATOIA', Status: 'PENDING',Mode:'MAIL', NotificationDate: '09/26/2019'},
];