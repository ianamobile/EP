import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ianaAnimations } from '@app-core/iana-animation';
import { FormGroup, FormBuilder } from '@angular/forms';
import { Component, OnInit, Inject, ViewEncapsulation } from '@angular/core';

import { notificationsSearchForm } from '@app-forms/notifications-search-form';
@Component({
  selector: 'app-notification-search-dialog',
  templateUrl: './notification-search-dialog.component.html',
  styleUrls: ['./notification-search-dialog.component.scss'],
  encapsulation: ViewEncapsulation.None,
  animations: ianaAnimations
})
export class NotificationSearchDialogComponent implements OnInit {

  searchForm: FormGroup;
  searchSetupData: any;
  notificationsSearchForm: notificationsSearchForm;

  constructor(
    public matDialogRef: MatDialogRef<NotificationSearchDialogComponent>,
    @Inject(MAT_DIALOG_DATA) private _data: any,
    private _formBuilder: FormBuilder,
  ) {
    var _data = JSON.parse(JSON.stringify(_data));
    this.searchSetupData = _data.searchSetupData;
    this.notificationsSearchForm = _data.formData;
    this.searchSetupData.notfStartDt = new Date(_data.searchSetupData.notfStartDt);
    this.searchSetupData.notfEndDt = new Date(_data.searchSetupData.notfEndDt);
  }

  ngOnInit() {

    this.searchForm = this._formBuilder.group({
      fromDate: [this.notificationsSearchForm.fromDate ? new Date(this.notificationsSearchForm.fromDate) : ""],
      toDate: [this.notificationsSearchForm.toDate ? new Date(this.notificationsSearchForm.toDate) : ""],
      mode: [this.notificationsSearchForm.mode],
      status: [this.notificationsSearchForm.status]
    });
  }

  search() {
    this.matDialogRef.close({ value: this.searchForm.value, close: true });
  }

  clear() {
    this.searchForm.controls.fromDate.setValue('');
    this.searchForm.controls.toDate.setValue('');
    this.searchForm.controls.mode.setValue('');
    this.searchForm.controls.status.setValue('');
  }


  close() {
    this.matDialogRef.close({ value: this.searchForm.value, close: false });
  }

}
