import { Component, OnInit } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { setupPageLayout } from '@app-core/common-funcations';
import { ianaAnimations } from '@app-core/iana-animation';
import { IanaConfig } from '@app-models/iana-config';
import { MessageService } from '@app-services/message.service';

@Component({
  selector: 'app-terminal-feed-locations',
  templateUrl: './terminal-feed-locations.component.html',
  styleUrls: ['./terminal-feed-locations.component.scss'],
  animations: ianaAnimations
})
export class TerminalFeedLocationsComponent implements OnInit {

  ianaConfig: IanaConfig = new IanaConfig

  constructor(
    private _msgService: MessageService<IanaConfig>,
  ) {
    //setup public page for removing header, footer & some navigation..
    setupPageLayout(this.ianaConfig, true);
    this._msgService.updateMessage(this.ianaConfig);
   }

  ngOnInit(): void {
  }

  displayedColumns: string[] = ['companyName', 'accountNo', 'SCACCode', 'cancelledDate','deletedDate','UIIAStatusCode'];
  dataSource = new MatTableDataSource<PeriodicElement>(ELEMENT_DATA);

}


export interface PeriodicElement {
  TerminalFeedName	: string;
}

const ELEMENT_DATA: PeriodicElement[] = [
  {TerminalFeedName: 'CA United Terminals'},
{TerminalFeedName: 'Cal Cartage - Web Only'},
{TerminalFeedName: 'Ceres Terminals - Web Only'},
{TerminalFeedName: 'Everport Terminals Services'},
{TerminalFeedName: 'FLorida Intl Terminal (Formerly S. Stevendoring) Web Only'},
{TerminalFeedName: 'Georgia Ports Authority'},
{TerminalFeedName: 'H & M International'},
{TerminalFeedName: 'Husky Terminals'},
{TerminalFeedName: 'Husky Terminals - Web Only'},
{TerminalFeedName: 'Maher Terminals'},
{TerminalFeedName: 'Maher Terminals - Parallel Feed'},
{TerminalFeedName: 'Montreal Gateway Terminal'},
{TerminalFeedName: 'Packer Avenue - Web Only'},
{TerminalFeedName: 'PNCT - Web Only'},
{TerminalFeedName: 'Trapac'},

];