import { SecurityObject } from './../models/security-object';
import { Injectable } from '@angular/core';


@Injectable({
  providedIn: 'root'
})
export class StorageService {

  constructor() { }

  setItem(key: string, value: SecurityObject): void {
    sessionStorage.setItem(key, JSON.stringify(value));
  }

  getItem(key: string): SecurityObject {
    var value = sessionStorage.getItem(key) || undefined;
    if (value) {
      return JSON.parse(value) as SecurityObject;
    }
    return null;
  }
 
 

  deleteItem(key: string) {
    sessionStorage.removeItem(key);
  }

  clear() {
    sessionStorage.clear();
  }

}