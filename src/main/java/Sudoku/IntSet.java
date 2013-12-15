package Sudoku;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This is a set designed specifically to hold the numbers 1 through 9.
 * It provides a Collection-like interface, but it does *not* implement Collection.
 *
 * O(1) -- add, remove, size, contains
 * O(n) -- clear, constructor
 *
 */
public class IntSet implements Iterable<Integer> {

    private static class IntSetIterator implements Iterator<Integer> {
        private final boolean[] entries;
        private int i;

        public IntSetIterator(boolean[] entries) {
            this.entries = entries;
            this.i = 0;
        }

        private void advance() {
            while (this.i < this.entries.length && !this.entries[this.i]) {
                this.i++;
            }
        }

        @Override
        public boolean hasNext() {
            this.advance();
            return this.i < this.entries.length;
        }

        @Override
        public Integer next() {
            this.advance();
            if (this.i < this.entries.length) {
                return ++this.i;
            }
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            throw new NotImplementedException();
        }
    }

    private boolean[] entries;
    private int size;

    public IntSet() {
        this.entries = new boolean[9];
        this.size = 0;
    }

    public void add(int n) {
        if (!this.entries[n-1]) {
            this.entries[n-1] = true;
            this.size++;
        }
    }

    public boolean contains(int n) {
        return this.entries[n-1];
    }

    public void clear() {
        for (int i = 0; i < 9; ++i) {
            this.entries[i] = false;
        }
        this.size = 0;
    }

    public void remove(int n) {
        if (this.entries[n-1]) {
            this.entries[n-1] = false;
            this.size--;
        }
    }

    public int size() {
        return this.size;
    }

    public int[] toArray() {
        int[] result = new int[this.size];
        int k = 0;
        for (int i = 0; i < 9; ++i) {
            if (this.entries[i])
                result[k++] = i + 1;
        }
        return result;
    }

    @Override
    public Iterator<Integer> iterator() {
        return new IntSetIterator(this.entries);
    }
}
