import { APIReqErrors } from './api-req-errors';

export class AppError {

    public code: number;
    public type: string;
    public message: string;
    public details: string;
    public apiReqErrors: APIReqErrors;

    constructor(public obj?: AppError,extraInfo?:any){
        
    }

    
}

 