import { Component, OnInit } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { ianaAnimations } from '@app-core/iana-animation';

@Component({
  selector: 'app-secondary-user-form-dialog',
  templateUrl: './secondary-user-form-dialog.component.html',
  styleUrls: ['./secondary-user-form-dialog.component.scss'],
  animations: ianaAnimations
})
export class SecondaryUserFormDialogComponent implements OnInit {

  constructor(
    public matDialogRef: MatDialogRef<SecondaryUserFormDialogComponent>,
  ) { }

  ngOnInit(): void {
  }

  close() {
    this.matDialogRef.close({  close: false });
  }

}
