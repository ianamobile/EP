import { Component, OnInit } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { ianaAnimations } from '@app-core/iana-animation';

@Component({
  selector: 'app-search-secondary-dialog',
  templateUrl: './search-secondary-dialog.component.html',
  styleUrls: ['./search-secondary-dialog.component.scss'],
  animations: ianaAnimations
})
export class SearchSecondaryDialogComponent implements OnInit {

  constructor(
    public matDialogRef: MatDialogRef<SearchSecondaryDialogComponent>,
  ) { }

  ngOnInit(): void {
  }

  close() {
    this.matDialogRef.close({  close: false });
  }

}
