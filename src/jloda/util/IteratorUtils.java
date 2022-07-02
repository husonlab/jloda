/*
 * IteratorUtils.java Copyright (C) 2022 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
        return new Iterator<>() {
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

	public static <T> Iterable<T> reverseIterator(ArrayList<T> list) {
		return () -> new Iterator<T>() {
			private int pos = list.size();

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

	/**
	 * given a iterator, returns a new iterator in random order
	 *
	 * @return iterator in random order
	 */
	public static <T> Iterator<T> randomize(Iterator<T> it, long seed) {
		return randomize(it, new Random(seed));
	}

	/**
	 * given a iterator, returns a new iterator in random order
	 *
	 * @return iterator in random order
	 */
	public static <T> Iterator<T> randomize(Iterator<T> it, Random random) {
		final ArrayList<T> input = new ArrayList<>();
		while (it.hasNext())
			input.add(it.next());
		final ArrayList<T> array = CollectionUtils.randomize(input, random);

		return new Iterator<>() {
			private int i = 0;

			@Override
			public boolean hasNext() {
				return i < array.size();
			}

			@Override
			public T next() {
				return array.get(i++);
			}

			@Override
			public void remove() {
			}
		};
	}

	/**
	 * given a iterator, returns a new iterator in random order
	 *
	 * @return iterator in random order
	 */
	public static <T> Iterable<T> randomize(Iterable<T> iterable, Random random) {
		return () -> randomize(iterable.iterator(), random);
	}

	public static Iterable<Integer> getIterable(int[] values) {
		return () -> new Iterator<>() {
			private int i = 0;

			@Override
			public boolean hasNext() {
				return i < values.length;
			}

			@Override
			public Integer next() {
				return values[i++];
			}
		};
	}

	public static <T> Iterable<T> asIterable(Iterator<T> it) {
		return () -> it;
	}

	public static <T> Set<T> asSet(Iterable<T> iterable) {
		final Set<T> set = new HashSet<>();
		for (T t : iterable) {
			set.add(t);
		}
		return set;
	}

	public static <T> ArrayList<T> asList(Iterable<T> iterable) {
		final ArrayList<T> list = new ArrayList<>();
		for (T t : iterable) {
			list.add(t);
		}
		return list;
	}

	public static <T> Collection<T> asCollection(Iterable<T> iterable, Collection<T> collection) {
		if (collection == null)
			collection = new ArrayList<>();
		for (T t : iterable) {
			collection.add(t);
		}
		return collection;
	}

	public static <T> int size(Iterable<T> values) {
		int count = 0;
		for (T value : values) {
			count++;
		}
		return count;
	}

	public static <T> Iterator<T> emptyIterator() {
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public T next() {
                return null;
            }
        };
    }

	/**
	 * iterates over items in sorted order
	 *
	 * @return iterator in sorted order
	 */
	public static <T> Iterable<T> sorted(Collection<T> list, Comparator<T> comparator) {
		var sorted = new ArrayList<>(list);
		sorted.sort(comparator);
		return sorted;
	}

	/**
	 * gets first item or null
	 *
	 * @return first item or null
	 */
	public static <T> T getFirst(Iterable<T> iterable) {
		var iterator = iterable.iterator();
		return iterator.hasNext() ? iterator.next() : null;
	}

	public static <T> Iterable<T> withAdditionalItems(Iterable<T> iterable, T... additional) {
		return () -> new Iterator<T>() {
			private final Iterator<T> it = iterable.iterator();
			private int pos = 0;

			@Override
			public boolean hasNext() {
				return it.hasNext() || pos < additional.length;
			}

			@Override
			public T next() {
				if (it.hasNext())
					return it.next();
				else if (pos < additional.length)
					return additional[pos++];
				else
					throw new NoSuchElementException();
			}
		};
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
