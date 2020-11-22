import { Map } from './map';

export class HashMap implements Map{

    private items: { [key: string]: any };

    constructor() {
        this.items = {};
    }
    clear(): void {
        this.items = {};
    }
    containsKey(key: string): boolean {
        if (this.items && !this.isEmpty()) {
            return key.trim() in this.items;
        }
        return false;
    }
    containsValue(value: string): boolean {
        if (this.items && !this.isEmpty()) {
            for (var key in this.items) {
                var _value = this.items[key.trim()];
                if (_value == value) {
                    return true;
                }
            }
        }
        return false;
    }
    get(key: string): any | undefined {
        if (this.items && !this.isEmpty()) {
            return this.items[key.trim()];
        }
        return undefined;
    }
    isEmpty(): boolean {
        return Object.keys(this.items).length <= 0 ? true : false;
    }
    size(): number {
        return Object.keys(this.items).length;
    }
    put(key: string, v: any): string {
        if(v == undefined || String(v) == 'undefined')
            return "";
        if (this.items && key && v
                && key.trim().length > 0 && key.trim().length > 0)
            this.items[key.trim()] = String(v).trim();
        return String(v).trim();
    }
    remove(key: string): boolean {
        if (this.items && !this.isEmpty() && this.get(key) != undefined) {
            delete this.items[key.trim()];
            return true;
        }
        return false;

    }

    getKeys():Array<string> | undefined {
        let keys :Array<string> = [];
        if (this.items && !this.isEmpty()){
            for (var key in this.items) {
                if(key)
                    keys.push(key);
            }
            return keys;
        }
        return undefined;
    }

}
