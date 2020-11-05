
import { Component, OnInit, ViewChild, ViewEncapsulation } from "@angular/core";
import { ianaAnimations } from '@app-core/iana-animation';
import { IanaConfig } from '@app-models/iana-config';
import { MessageService } from '@app-services/message.service';
import { setupPageLayout } from 'src/app/core/common-funcations';

@Component({
    selector: "app-dashboard",
    templateUrl: "./dashboard.component.html",
    styleUrls: ["./dashboard.component.scss"],
    animations: ianaAnimations
})
export class DashboardComponent implements OnInit {

    ianaConfig: IanaConfig = new IanaConfig();


    constructor(
        private _msgService: MessageService<IanaConfig>,
       
    ) {
        
       //setup public page for removing header, footer & some navigation..
       setupPageLayout(this.ianaConfig, true);
       this._msgService.updateMessage(this.ianaConfig);
       
    }

    ngOnInit(): void {
      
    }

}
