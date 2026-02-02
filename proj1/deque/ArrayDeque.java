package deque;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ArrayDeque<T> implements Deque<T>, Iterable<T> {
    private int size;
    private T[] elems;
    private int next_first;
    private int next_last;
    private int capacity;
    private int resize_index;

    public ArrayDeque() {
        size = 0;
        capacity = 8;
        elems = (T[]) new Object[capacity];
        next_first = 0;
        next_last = 1;
        resize_index = 2;
    }

    public void addFirst(T item) {
        elems[next_first] = item;
        next_first = (next_first - 1 + capacity) % capacity;
        size++;
        if (size == capacity) {
            resize(capacity * resize_index);
        }
    }

    public void addLast(T item) {
        elems[next_last] = item;
        next_last = (next_last + 1) % capacity;
        size++;
        if (size == capacity) {
            resize(capacity * resize_index);
        }
    }

    public T removeFirst() {
        if (size == 0) {
            return null;
        }
        T val = elems[(next_first + 1) % capacity];
        elems[(next_first + 1) % capacity] = null;
        next_first = (next_first + 1) % capacity;
        size--;
        if (capacity > 16 && size * 4 < capacity) {
            resize(capacity / resize_index);
        }
        return val;
    }

    public T removeLast() {
        if (size == 0) {
            return null;
        }
        T val = elems[(next_last - 1 + capacity) % capacity];
        elems[(next_last - 1 + capacity) % capacity] = null;
        next_last = (next_last - 1 + capacity) % capacity;
        size--;
        if (capacity > 16 && 4 * size < capacity) {
            resize(capacity / resize_index);
        }
        return val;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void printDeque() {
        for (int i = 1; i < size; i++) {
            System.out.print(elems[(next_first + i) % capacity] + " ");
        }
        System.out.println(elems[(next_first + size) % capacity]);
    }

    public T get(int index) {
        if (index < 0 || index >= size) {
            return null;
        }
        return elems[(next_first + index + 1) % capacity];
    }

    private void resize(int new_capacity) {
        T[] temp = (T[]) new Object[new_capacity];
        for (int i = 0; i < size; i++) {
            temp[i] = elems[(next_first + 1 + i) % capacity];
        }
        elems = temp;
        capacity = new_capacity;
        next_first = new_capacity - 1;
        next_last = size;
    }

    @Override
    public Iterator<T> iterator() {
        return new DequeIterator();
    }

    private class DequeIterator implements Iterator<T> {
        private int curr = (next_first + 1) % capacity;

        @Override
        public boolean hasNext() {
            return curr != next_last;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            T val = elems[curr];
            curr = (curr + 1) % capacity;
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

        Deque<T> other= (Deque<T>) o;
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
