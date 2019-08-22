package jloda.util;

import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * LRU cache
 * Based on https://www.geeksforgeeks.org/design-a-data-structure-for-lru-cache/
 *
 * @param <V> Daniel Huson, 8.2019
 */
public class LRUCache<K, V> {
    private final HashMap<K, Node> map;
    private final int capacity;
    private final Node head = new Node();
    private final Node tail = new Node();

    private ReadWriteLock lock = new ReentrantReadWriteLock();

    private Lock writeLock = lock.writeLock();

    private Lock readLock = lock.readLock();

    private final boolean lruAlways;

    /**
     * constructor
     *
     * @param capacity
     */
    public LRUCache(int capacity) {
        this(capacity, false);
    }

    /**
     * constructor
     *
     * @param capacity
     * @param lruOnlyOnceCapacityExceeded don't update LRU until capacity is exceeded, turn on for efficiency
     */
    public LRUCache(int capacity, boolean lruOnlyOnceCapacityExceeded) {
        this.capacity = capacity;
        this.lruAlways = !lruOnlyOnceCapacityExceeded;
        map = new HashMap<>((int) (1.2 * capacity));
        head.next = tail;
        tail.pre = head;
    }

    /**
     * get
     *
     * @param key
     * @return
     */
    public V get(K key) {
        readLock.lock();
        try {
            final Node node = map.get(key);
            if (node != null) {
                final V result = node.value;
                if (lruAlways || map.size() >= capacity) {
                    moveToHeadDuringRead(node);
                }
                return result;
            }
            return null;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * set
     *
     * @param key
     * @param value
     */
    public void put(K key, V value) {
        writeLock.lock();
        try {
            final Node node = map.get(key);
            if (node != null) {
                node.value = value;
                if (lruAlways || map.size() >= capacity) {
                    deleteNodeDuringWrite(node);
                    addToHeadDuringWrite(node);
                }
            } else {
                final Node newNode = new Node(key, value);
                map.put(key, newNode);
                if (map.size() < capacity) {
                    addToHeadDuringWrite(newNode);
                } else {
                    map.remove(tail.pre.key);
                    deleteNodeDuringWrite(tail.pre);
                    addToHeadDuringWrite(newNode);
                }
            }
        } finally {
            writeLock.unlock();
        }
    }

    private void deleteNodeDuringWrite(Node node) {
        node.pre.next = node.next;
        node.next.pre = node.pre;
    }

    private void addToHeadDuringWrite(Node node) {
        node.next = head.next;
        node.next.pre = node;
        node.pre = head;
        head.next = node;
    }

    private synchronized void moveToHeadDuringRead(Node node) {
        node.pre.next = node.next;
        node.next.pre = node.pre;

        node.next = head.next;
        node.next.pre = node;
        node.pre = head;
        head.next = node;
    }

    public int size() {
        return map.size();
    }

    private class Node {
        K key;
        V value;
        Node pre;
        Node next;

        public Node() {
        }

        public Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
}
