package ru.nsu.ccfit.beloglazov.socksproxyserver.models;

import java.util.TreeMap;

public class FiniteTreeMap<K extends Comparable<K>,V> {
    private TreeMap<K,V> map  = new TreeMap<>();
    private int capacity;

    public FiniteTreeMap(int capacity) { this.capacity = capacity; }

    public V get(K key){
        return map.get(key);
    }

    public void put(K key, V value){
        if (map.size() >= capacity) {
            map.remove(map.firstKey());
        }

        map.put(key, value);
    }
}