/*
 * CollectionUtils.java Copyright (C) 2022 Daniel H. Huson
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

import java.util.*;

/**
 * some utilities for collections
 * Daniel Huson, 2020
 */
public class CollectionUtils {
	/**
	 * given a list, returns a new collection in random order
	 *
	 * @return iterator in random order
	 */
	public static <T> ArrayList<T> randomize(List<T> list, long seed) {
		return randomize(list, new Random(seed));
	}

	/**
	 * given an array, returns it randomized (Durstenfeld 1964)
	 */
	public static <T> T[] randomize(T[] array, long seed) {
		return randomize(array, new Random(seed));
	}

	/**
	 * given an array, returns it randomized (Durstenfeld 1964)
	 *
	 * @return array in random order
	 */
	public static <T> T[] randomize(T[] array, Random random) {
		final T[] result = Arrays.copyOf(array, array.length);

		for (int i = result.length - 1; i >= 1; i--) {
			int j = random.nextInt(i + 1);
			if (j != i) {
				T tmp = result[i];
				result[i] = result[j];
				result[j] = tmp;
			}
		}
		return result;
	}

	/**
	 * given an array, returns it randomized (Durstenfeld 1964)
	 *
	 * @return array in random order
	 */
	public static <T> ArrayList<T> randomize(Collection<T> array, Random random) {
		final ArrayList<T> result = new ArrayList<>(array);

		for (int i = result.size() - 1; i >= 1; i--) {
			int j = random.nextInt(i + 1);
			if (j != i) {
				T tmp = result.get(i);
				result.set(i, result.get(j));
				result.set(j, tmp);
			}
		}
		return result;
	}

	/**
	 * randomize array of longs using (Durstenfeld 1964)
	 */
	public static void randomize(long[] array, long seed) {
		Random random = new Random(seed);
		for (int i = array.length - 1; i >= 1; i--) {
			int j = random.nextInt(i + 1);
			if (j != i) {
				long tmp = array[i];
				array[i] = array[j];
				array[j] = tmp;
			}
		}
	}

	/**
	 * randomize array of int using (Durstenfeld 1964)
	 */
	public static void randomize(int[] array, long seed) {
		Random random = new Random(seed);
		for (int i = array.length - 1; i >= 1; i--) {
			int j = random.nextInt(i + 1);
			if (j != i) {
				int tmp = array[i];
				array[i] = array[j];
				array[j] = tmp;
			}
		}
	}

	/**
	 * get list will objects in reverse order
	 *
	 * @return reverse order list
	 */
	public static <T> List<T> reverseList(Collection<T> list) {
		final var result = new LinkedList<T>();
		for (T aList : list) {
			result.add(0, aList);
		}
		return result;
	}

	/**
	 * get list with objects in rotated order
	 *
	 * @return rotated order
	 */
	public static <T> List<T> rotateList(Collection<T> list) {
		final var result = new LinkedList<T>();
		if (list.size() > 0) {
			result.addAll(list);
			result.add(result.remove(0));
		}
		return result;
	}

	/**
	 * get the sum of values
	 *
	 * @return sum
	 */
	public static int getSum(Integer[] values) {
		var sum = 0;
		for (var value : values) {
			if (value != null)
				sum += value;
		}
		return sum;
	}

	/**
	 * get the sum of values
	 *
	 * @return sum
	 */
	public static int getSum(int[] values) {
		var sum = 0;
		for (var value : values) {
			sum += value;
		}
		return sum;
	}

	/**
	 * get the sum of values
	 *
	 * @return sum
	 */
	public static float getSum(float[] values) {
		var sum = 0;
		for (var value : values) {
			sum += value;
		}
		return sum;
	}

	/**
	 * get the sum of values
	 *
	 * @return sum
	 */
	public static int getSum(Collection<Integer> values) {
		var sum = 0;
		for (var value : values)
			sum += value;
		return sum;
	}

	/**
	 * get the sum of values
	 *
	 * @return sum
	 */
	public static int getSum(int[] values, int offset, int len) {
		var sum = 0;
		for (var i = offset; i < len; i++) {
			var value = values[i];
			sum += value;
		}
		return sum;
	}

	public static long getSum(long[] array) {
		var result = 0L;
		for (var value : array)
			result += value;
		return result;
	}

	/**
	 * computes the intersection different of two collections
	 *
	 * @return symmetric different
	 */
	public static <T> Collection<T> intersection(final Collection<T> set1, final Collection<T> set2) {
		final Collection<T> result = new HashSet<>();
		for (T element : set1) {
			if (set2.contains(element))
				result.add(element);
		}
		return result;
	}

	public static <T> boolean intersects(Collection<T> a, Collection<T> b) {
		if (a.size() == 0 || b.size() == 0)
			return false;
		for (T element : a) {
			if (b.contains(element))
				return true;
		}
		return false;
	}

	public static <T> HashSet<T> union(Collection<T> a, Collection<T> b) {
		final HashSet<T> union = new HashSet<>(a);
		union.addAll(b);
		return union;
	}

	public static <T> ArrayList<T> difference(Collection<T> a, Collection<T> b) {
		final ArrayList<T> result = new ArrayList<>(a);
		for (var t : b) {
			result.remove(t);
		}
		return result;
	}

	public static <T> ArrayList<T> concatenate(Collection<T> a, Collection<T> b) {
		final ArrayList<T> result = new ArrayList<>(a);
		result.addAll(b);
		return result;

	}

	/**
	 * return list in reverse order
	 */
	public static <T> ArrayList<T> reverse(Collection<T> list) {
		var source = new ArrayList<>(list);
		var target = new ArrayList<T>(list.size());
		for (var i = source.size() - 1; i >= 0; i--)
			target.add(source.get(i));
		return target;
	}

	/**
	 * reverses an array list in place
	 */
	public static <T> void reverseInPlace(ArrayList<T> list) {
		final var top = list.size() / 2;
		final var size1 = list.size() - 1;
		for (var i = 0; i < top; i++) {
			var tmp = list.get(i);
			list.set(i, list.get(size1 - i));
			list.set(size1 - i, tmp);
		}
	}

	/**
	 * gets the rank of a value in a list
	 */
	public static <T> int getRank(List<T> list, T value) {
		return list.indexOf(value);
	}

	/**
	 * gets the rank of a value in an array
	 */
	public static <T> int getRank(T[] list, T value) {
		for (int i = 0; i < list.length; i++) {
			if (list[i] != null && list[i].equals(value))
				return i;
		}
		return -1;
	}

	/**
	 * does the given array contain the given object`?
	 *
	 * @return true, if contained
	 */
	public static <T> boolean contains(T[] array, T obj) {
		if (obj == null) {
			for (T a : array)
				if (a == null)
					return true;
		} else
			for (T a : array) {
				if (a != null && a.equals(obj))
					return true;
			}
		return false;
	}

	/**
	 * sort a list using the given comparator
	 */
	public static <T> void sort(List<T> list, Comparator<T> comparator) {
		T[] array = (T[]) list.toArray();
		Arrays.sort(array, comparator);
		list.clear();
		list.addAll(Arrays.asList(array));
	}

	/**
	 * find a element in the list for which clazz is assignable from
	 */
	public static <T> T findByClass(Collection<T> list, Class<?> clazz) {
		for (T t : list) {
			if (clazz.isAssignableFrom(t.getClass()))
				return t;
		}
		return null;
	}

	/**
	 * computes the weighted sum of two arrays (not necessarily of the same length_
	 */
	public static double[] weightedSum(double p, double[] array1, double q, double[] array2) {
		final int top = Math.max(array1.length, array2.length);

		final double[] sum = new double[top];
		for (int i = 0; i < top; i++) {
			sum[i] = (i < array1.length ? p * array1[i] : 0) + (i < array2.length ? q * array2[i] : 0);
		}
		return sum;
	}

	/**
	 * gets the index of an object s in an array of objects
	 *
	 * @return index or -1
	 */
	@SafeVarargs
	public static <T> int getIndex(T s, T... array) {
		for (var i = 0; i < array.length; i++)
			if (s.equals(array[i]))
				return i;
		return -1;
	}

	public static <T> boolean equalsAsSets(Collection<T> aList, Collection<T> bList) {
		return aList.size() == bList.size() && (new HashSet<>(aList)).containsAll(bList) && (new HashSet<>(bList)).containsAll(aList);
	}
}
