package Sudoku;

/*
 * This class is specifically designed to store the numbers 1-9.
 * It has constant time add, remove, contains, and clear methods.
 */
public class IntSet {
    /*
     * Each bit represents the presence of a number as follows:
     *  bits = 0000 0000 0000 0000
     *  n    =        9 8765 4321
     */
    private short bits;
    private short size;

    public IntSet() {
        this.bits = 0;
        this.size = 0;
    }

    public void add(int n) {
//        this.size += ~(this.bits >> n) & 1;
        if (!this.contains(n)) {
            this.bits |= (1 << n);
            this.size += 1;
        }
    }

    public void remove(int n) {
//        this.size -= (this.bits >> n) & 1;
        if (this.contains(n)) {
            this.bits &= ~(1 << n);
            this.size -= 1;
        }
    }

    public void clear() {
        this.bits = 0;
        this.size = 0;
    }

    public boolean contains(int n) {
        return (this.bits & (1 << n)) > 0;
    }

    public int[] toArray() {
        int[] result = new int[this.size];
        short b = this.bits;
        int k = 0, n = 1;
        while (b != 0) {
            b >>= 1;
            if ((b & 1) > 0)
                result[k++] = n;
            n += 1;
        }
        return result;
    }

    public int size() {
        return size;
    }

    public int getFirst() {
        short b = this.bits;
        int k = 0, n = 1;
        while (b != 0) {
            b >>= 1;
            if ((b & 1) > 0)
                return n;
            n += 1;
        }
        return -1;
    }

}

//import sun.reflect.generics.reflectiveObjects.NotImplementedException;
//import java.util.Iterator;
//import java.util.NoSuchElementException;
//
///**
// * This is a set designed specifically to hold the numbers 1 through 9.
// * It provides a Collection-like interface, but it does *not* implement Collection.
// *
// * O(1) -- add, remove, size, contains
// * O(n) -- clear, constructor
// *
// */
//public class IntSet implements Iterable<Integer> {
//
//    private static class IntSetIterator implements Iterator<Integer> {
//        private final boolean[] entries;
//        private int i;
//
//        public IntSetIterator(boolean[] entries) {
//            this.entries = entries;
//            this.i = 0;
//        }
//
//        private void advance() {
//            while (this.i < this.entries.length && !this.entries[this.i]) {
//                this.i++;
//            }
//        }
//
//        @Override
//        public boolean hasNext() {
//            this.advance();
//            return this.i < this.entries.length;
//        }
//
//        @Override
//        public Integer next() {
//            this.advance();
//            if (this.i < this.entries.length) {
//                return ++this.i;
//            }
//            throw new NoSuchElementException();
//        }
//
//        @Override
//        public void remove() {
//            throw new NotImplementedException();
//        }
//    }
//
//    private boolean[] entries;
//    private int size;
//
//    public IntSet() {
//        this.entries = new boolean[9];
//        this.size = 0;
//    }
//
//    public void add(int n) {
//        if (!this.entries[n-1]) {
//            this.entries[n-1] = true;
//            this.size++;
//        }
//    }
//
//    public boolean contains(int n) {
//        return this.entries[n-1];
//    }
//
//    public void clear() {
//        for (int i = 0; i < 9; ++i) {
//            this.entries[i] = false;
//        }
//        this.size = 0;
//    }
//
//    public void remove(int n) {
//        if (this.entries[n-1]) {
//            this.entries[n-1] = false;
//            this.size--;
//        }
//    }
//
//    public int size() {
//        return this.size;
//    }
//
//    public int[] toArray() {
//        int[] result = new int[this.size];
//        int k = 0;
//        for (int i = 0; i < 9; ++i) {
//            if (this.entries[i])
//                result[k++] = i + 1;
//        }
//        return result;
//    }
//
//    @Override
//    public Iterator<Integer> iterator() {
//        return new IntSetIterator(this.entries);
//    }
//}
