import { Component, OnInit } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-mc-specific-search-dialog',
  templateUrl: './mc-specific-search-dialog.component.html',
  styleUrls: ['./mc-specific-search-dialog.component.scss']
})
export class McSpecificSearchDialogComponent implements OnInit {

  constructor(
    public matDialogRef: MatDialogRef<McSpecificSearchDialogComponent>,
  ) { }

  ngOnInit(): void {
  }

  close() {
    this.matDialogRef.close({ close: false });
  }

}
