export interface Map{

    // Removes all of the mappings from this map (optional operation).
    clear(): void;
  
    //Returns true if this map contains a mapping for the specified key.
    containsKey(o: string): boolean;

    //Returns true if this map maps one or more keys to the specified value.
    containsValue(o: string): boolean;

    //Returns the value to which the specified key is mapped, or null if this map contains no mapping for the key.
    get(o: string): any | undefined;
    
    // Returns true if this map contains no key-value mappings.
    isEmpty(): boolean;
  
    //Returns the number of key-value mappings in this map.
    size(): number;

    //Associates the specified value with the specified key in this map (optional operation).
    put(k: string, v: any): string;

    //Removes the mapping for a key from this map if it is present (optional operation).
    remove(o: string): boolean;

    getKeys():Array<string> | undefined;
  
  }