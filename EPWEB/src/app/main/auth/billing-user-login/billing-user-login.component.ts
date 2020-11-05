import { Component, OnInit } from '@angular/core';
import { setupPageLayout } from '@app-core/common-funcations';
import { ianaAnimations } from '@app-core/iana-animation';
import { IanaConfig } from '@app-models/iana-config';
import { MessageService } from '@app-services/message.service';

@Component({
  selector: 'app-billing-user-login',
  templateUrl: './billing-user-login.component.html',
  styleUrls: ['./billing-user-login.component.scss'],
  animations: ianaAnimations
})
export class BillingUserLoginComponent implements OnInit {

  ianaConfig: IanaConfig = new IanaConfig();

  constructor(
    private _msgService: MessageService<IanaConfig>,
  ) {

     //setup public page for removing header, footer & some navigation..
     setupPageLayout(this.ianaConfig, false);
      this._msgService.updateMessage(this.ianaConfig);

   }

  ngOnInit(): void {
  }

}
