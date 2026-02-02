package deque;

public interface Deque<T> {
    void addFirst(T item);
    void addLast(T item);
    int size();
    boolean isEmpty();
    void printDeque();
    T removeFirst();
    T removeLast();
    T get(int index);
}
