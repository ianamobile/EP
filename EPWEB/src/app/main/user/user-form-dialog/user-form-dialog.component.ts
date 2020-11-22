import { Component, OnInit } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-user-form-dialog',
  templateUrl: './user-form-dialog.component.html',
  styleUrls: ['./user-form-dialog.component.scss']
})
export class UserFormDialogComponent implements OnInit {

  constructor(
    public matDialogRef: MatDialogRef<UserFormDialogComponent>,
  ) { }

  ngOnInit(): void {
  }
  
  close() {
  this.matDialogRef.close({  close: false });
}

}
