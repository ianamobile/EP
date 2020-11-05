import { ValidationError } from '../shared/models/validation-error';

export class BaseService {

    processSuccessResponse(res: any, successCallBack: (res: any) => void) {
        if (res) 
            successCallBack(res);
    }
    processErrorResponse(error: any, errorCallBack: (error: ValidationError) => void) {
        if (error instanceof ValidationError)
            errorCallBack(error);
        else
            throw error;
    }


}