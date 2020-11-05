import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ianaAnimations } from '@app-core/iana-animation';
import { FormGroup, FormBuilder } from '@angular/forms';
import { Component, OnInit, Inject, ViewEncapsulation } from '@angular/core';


@Component({
  selector: 'app-search-dialog',
  templateUrl: './search-dialog.component.html',
  styleUrls: ['./search-dialog.component.scss'],
  encapsulation: ViewEncapsulation.None,
  animations: ianaAnimations
})
export class SearchDialogComponent implements OnInit {

  searchForm: FormGroup;
  searchdata: any = [];

  constructor(
    public matDialogRef: MatDialogRef<SearchDialogComponent>,
    @Inject(MAT_DIALOG_DATA) private _data: any,
    private _formBuilder: FormBuilder,
  ) {
    this.searchdata = _data.data;
  }

  ngOnInit() {
    this.searchForm = this._formBuilder.group({
      mcName: [this.searchdata.mcName],
      scac: [this.searchdata.scac]
    });
  }

  search() {
    this.matDialogRef.close({ value: this.searchForm.value, close: true });
  }

  clear() {
    this.searchForm.controls.mcName.setValue('');
    this.searchForm.controls.scac.setValue('');
  }

  close() {
    // this.searchForm.controls.mcName.setValue('');
    // this.searchForm.controls.scac.setValue('');
    this.matDialogRef.close({ value: this.searchForm.value, close: false });
  }

}
