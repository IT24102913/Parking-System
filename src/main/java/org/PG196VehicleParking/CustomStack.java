package org.PG196VehicleParking;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;

public class CustomStack<T> {
    private int maxSize;
    private T[] stackArray;
    private int top;

    @SuppressWarnings("unchecked")
    public CustomStack(int size) {
        maxSize = size;
        stackArray = (T[]) new Object[maxSize];
        top = -1;
    }

    public void push(T item) {
        if (top + 1 == maxSize) {
            throw new IllegalStateException("Stack is full");
        }
        stackArray[++top] = item;
    }

    public T pop() {
        if (isEmpty()) {
            throw new EmptyStackException();
        }
        T item = stackArray[top];
        stackArray[top--] = null;
        return item;
    }

    public boolean isEmpty() {
        return top == -1;
    }

    public int size() {
        return top + 1;
    }

    public List<T> getAll() {
        List<T> list = new ArrayList<>();
        for (int i = 0; i <= top; i++) {
            list.add(stackArray[i]);
        }
        return list;
    }
}