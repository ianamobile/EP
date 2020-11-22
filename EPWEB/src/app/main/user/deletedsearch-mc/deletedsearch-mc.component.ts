import { Component, OnInit } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-deletedsearch-mc',
  templateUrl: './deletedsearch-mc.component.html',
  styleUrls: ['./deletedsearch-mc.component.scss']
})
export class DeletedsearchMCComponent implements OnInit {

  constructor(
    public matDialogRef: MatDialogRef<DeletedsearchMCComponent>,
  ) { }

  ngOnInit(): void {
  }

  close() {
    this.matDialogRef.close({  close: false });
  }

}
