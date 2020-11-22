import { HttpErrorResponse } from '@angular/common/http';
export class UnauthorizedError{

    private _code: number = 1;
    private _type: string = "error";
    private _message: string = "You are unauthorized to access this page.";

    constructor(public error?: HttpErrorResponse) {
        
    }

    get code(): number {
        return this._code;
    }
     
    get type(): string {
        return this._type;
    }

    get message(): string {
        return this._message;
    }
}

 