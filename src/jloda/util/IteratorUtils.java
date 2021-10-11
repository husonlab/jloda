/*
 * IterationUtils.java Copyright (C) 2021. Daniel H. Huson
 *
 * (Some code written by other authors, as named in code.)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package jloda.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * iteration utilities
 * Daniel Huson, 3.2019
 */
public class IteratorUtils {
    /**
     * join multiple iterables into one
     *
     * @param collections
     * @param <T>
     * @return iterable over multiple iterables
     */
    public static <T, L extends Collection<T>> Iterable<T> join(final Collection<L> collections) {
        return () -> new Iterator<>() {
            private Iterator<T> iterator = null;
            private final Iterator<L> metaIterator = collections.iterator();

            {
                if (metaIterator.hasNext())
                    iterator = metaIterator.next().iterator();
            }

            @Override
            public boolean hasNext() {
                return iterator != null;
            }

            @Override
            public T next() {
                if (iterator == null)
                    throw new NoSuchElementException();

                final T next = iterator.next();
                while (!iterator.hasNext()) {
                    if (metaIterator.hasNext())
                        iterator = metaIterator.next().iterator();
                    else {
                        iterator = null;
                        break;
                    }
                }
                return next;
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] asArray(Iterable<T> iterable) {
        return asList(iterable).toArray((T[]) new Object[0]);
    }

    @SafeVarargs
    public static <T> List<T> asList(Iterable<T> a, Iterable<T>... additional) {
        var result = new ArrayList<T>();
        for (T value : a) {
            result.add(value);
        }
        for (var iterable : additional) {
            if (iterable != null) {
                for (T value : iterable) {
                    result.add(value);
                }
            }
        }
        return result;
    }

    @SafeVarargs
    public static <T> Set<T> asSet(Iterable<T> a, Iterable<T>... additional) {
        var result = new HashSet<T>();
        for (T value : a) {
            result.add(value);
        }
        for (var iterable : additional) {
            if (iterable != null) {
                for (T value : iterable) {
                    result.add(value);
                }
            }
        }
        return result;
    }

    public static <T> Iterator<T> iteratorNonNullElements(Iterator<T> it) {
        return new Iterator<T>() {
            T next = getNextNonNull();

            @Override
            public boolean hasNext() {
                return next != null;
            }

            @Override
            public T next() {
                var result = next;
                next = getNextNonNull();
                return result;
            }

            private T getNextNonNull() {
                while (it.hasNext()) {
                    T a = it.next();
                    if (a != null)
                        return a;
                }
                return null;

            }
        };
    }

    public static <T> int count(Iterable<T> iterable) {
        int count = 0;

        for (T t : iterable) {
            count++;
        }
        return count;
    }

    public static <T> Stream<T> asStream(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    /**
     * iterable over first elements
     *
     * @param src
     * @param <P>
     * @param <Q>
     * @return iterable over all first elements
     */
    public static <P, Q> Iterable<P> firstValues(final Iterable<Pair<P, Q>> src) {
        return () -> new Iterator<>() {
            private final Iterator<Pair<P, Q>> it = src.iterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public P next() {
                return it.next().getFirst();
            }

            @Override
            public void remove() {
                it.remove();
            }
        };
    }

    /**
     * iterable over second elements
     *
     * @param src
     * @param <P>
     * @param <Q>
     * @return iterable over all second elements
     */
    public static <P, Q> Iterable<Q> secondValues(final Iterable<Pair<P, Q>> src) {
        return () -> new Iterator<>() {
            private final Iterator<Pair<P, Q>> it = src.iterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public Q next() {
                return it.next().getSecond();
            }

            @Override
            public void remove() {
                it.remove();
            }
        };
    }

    public static <T> Iterator<T> reverseIterator(ArrayList<T> list) {
        return new Iterator<>() {
            private int pos=list.size();

            @Override
            public boolean hasNext() {
                return pos > 0;
            }

            @Override
            public T next() {
                return list.get(--pos);
            }
        };
    }

    public static LineIterator lines(Reader reader) {
        return new LineIterator(reader);

    }

    public static class LineIterator implements Iterator<String>, AutoCloseable {
        private final BufferedReader br;
        private String next;
        private int lineNumber = 0;

        public LineIterator(Reader reader) {
            br = (reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader));
            {
                try {
                    next = br.readLine();
                } catch (IOException ignored) {
                }
            }

        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public String next() {
            var result = next;
            if (result != null) {
                lineNumber++;
                try {
                    next = br.readLine();
                } catch (IOException ignored) {
                }
            }
            return result;
        }

        public int getLineNumber() {
            return lineNumber;
        }

        @Override
        public void close() {
        }
    }
}
