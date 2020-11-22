import { IanaConfig } from '../models/iana-config';
import { Injectable } from '@angular/core';
import { Subject, Observable } from 'rxjs';
@Injectable({
    providedIn: 'root',
  })
export class MessageService<T> {
  private siblingMsg = new Subject<T>();
  constructor() { }
  /*
   * @return {Observable<string>} : siblingMsg
   */
  public getMessage(): Observable<T> {
    return this.siblingMsg.asObservable();
  }
  /*
   * @param {string} message : siblingMsg
   */
  public updateMessage(message: T): void {
    this.siblingMsg.next(message);
  }
}