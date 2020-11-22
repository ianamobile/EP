import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SubjectService {

  private buttonLoader = new Subject<any>();

  constructor() { }

  sendButtonLoaderMessage(isButtonLoader: boolean) {
    this.buttonLoader.next({ isButtonLoader: isButtonLoader });
  }
  getButtonLoaderMessage(): Observable<any> {
    return this.buttonLoader.asObservable();
  }

}
