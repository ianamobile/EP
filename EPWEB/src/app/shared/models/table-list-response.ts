import { Pagination } from './pagination';

export class tableListResponse<R>{

    public resultList: R[];
    public page: Pagination;
    constructor() {

    }

}