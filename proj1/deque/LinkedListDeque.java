package deque;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {
    private Node sentinel;
    private int size;

    public class Node {
        public Node prev;
        public T value;
        public Node next;

        public Node() {
            prev = null;
            value = null;
            next = null;
        }

        public Node(T val) {
            prev = null;
            value = val;
            next = null;
        }
    }

    public LinkedListDeque() {
        size = 0;
        sentinel = new Node();
        sentinel.prev = sentinel;
        sentinel.next = sentinel;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void printDeque() {
        if (size == 0) {
            return;
        }
        Node ptr = sentinel.next;
        while (ptr.next != sentinel) {
            System.out.print(ptr.value + " ");
            ptr = ptr.next;
        }
        System.out.println(ptr.value);
    }

    public void addFirst(T item) {
        Node temp = new Node(item);
        temp.prev = sentinel;
        temp.next = sentinel.next;
        sentinel.next.prev = temp;
        sentinel.next = temp;
        size++;
    }

    public void addLast(T item) {
        Node temp = new Node(item);
        temp.prev = sentinel.prev;
        temp.next = sentinel;
        sentinel.prev.next = temp;
        sentinel.prev = temp;
        size++;
    }

    public T removeFirst() {
        if (size == 0) {
            return null;
        }
        T val = sentinel.next.value;
        sentinel.next.next.prev = sentinel;
        sentinel.next = sentinel.next.next;
        size--;
        return val;
    }

    public T removeLast() {
        if (size == 0) {
            return null;
        }
        T val = sentinel.prev.value;
        sentinel.prev.prev.next = sentinel;
        sentinel.prev = sentinel.prev.prev;
        size--;
        return val;
    }

    public T get(int index) {
        if (index < 0 || index >= size) {
            return null;
        }
        Node ptr = sentinel.next;
        for (int i = 0; i < index; i++) {
            ptr = ptr.next;
        }
        return ptr.value;
    }

    public T getRecursive(int index) {
        if (index < 0 || index >= size) {
            return null;
        }
        return getRecursiveHelper(index, sentinel.next);
    }

    private T getRecursiveHelper(int index, Node ptr) {
        if (index == 0) {
            return ptr.value;
        }
        return getRecursiveHelper(index - 1, ptr.next);
    }

    @Override
    public Iterator<T> iterator() {
        return new DequeIterator();
    }

    private class DequeIterator implements Iterator<T> {
        private Node curr = sentinel.next;

        @Override
        public boolean hasNext() {
            return curr != sentinel;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            T val = curr.value;
            curr = curr.next;
            return val;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null) {
            return false;
        }

        if (!(o instanceof Deque)) {
            return false;
        }

        Deque<T> other = (Deque<T>) o;

        if (this.size() != other.size()) {
            return false;
        }

        for (int i = 0; i < this.size(); i++) {
            if (!(this.get(i).equals(other.get(i)))) {
                return false;
            }
        }

        return true;
    }
}
