import { Component, OnInit } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-ep-search-template-dialog',
  templateUrl: './ep-search-template-dialog.component.html',
  styleUrls: ['./ep-search-template-dialog.component.scss']
})
export class EpSearchTemplateDialogComponent implements OnInit {
  selected = 'ACTIVE';
  constructor(
    public matDialogRef: MatDialogRef<EpSearchTemplateDialogComponent>,
  ) { }

  ngOnInit(): void {
  }

  close() {
    this.matDialogRef.close({  close: false });
  }

}
