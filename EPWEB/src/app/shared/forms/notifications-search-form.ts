import { CONSTANTS } from './../../core/constants';

export class notificationsSearchForm {

    public fromDate: string;
    public toDate: string;
    public mode: string;
    public status: string;
    public key: string;
    public pageIndex: number;
    public pageSize: number;
    
    constructor(data) {
        this.fromDate = data.fromDate || '';
        this.toDate = data.toDate || '';
        this.mode = data.mode || '';
        this.status = data.status || '';
        this.key = data.key || '';
        this.pageIndex = data.pageIndex || 1;
        this.pageSize = data.pageSize || CONSTANTS.DEFAULT_TOTALRECORD;
    }

}