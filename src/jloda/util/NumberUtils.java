/*
 * NumberUtils.java Copyright (C) 2022 Daniel H. Huson
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

import java.util.ArrayList;
import java.util.Collection;

public class NumberUtils {
	/**
	 * Round to a given number of significant figures
	 *
	 * @param num    double
	 * @param digits number of digits
	 * @return double
	 */
	public static double roundSigFig(double num, int digits) {
		if (num == 0) {
			return 0;
		}

		final double d = Math.ceil(Math.log10(num < 0 ? -num : num));
		final int power = digits - (int) d;

		final double magnitude = Math.pow(10, power);
		final long shifted = Math.round(num * magnitude);
		return shifted / magnitude;
	}

	/**
	 * returns the sign of x
	 *
	 * @return sign of x
	 */
	public static int sign(double x) {
		if (x > 0)
			return 1;
		else if (x < 0)
			return -1;
		else
			return 0;
	}

	/**
	 * gets the min value of an array
	 *
	 * @return min
	 */
	public static int min(int... array) {
		var m = Integer.MAX_VALUE;
		for (var x : array) {
			if (x < m)
				m = x;

		}
		return m;
	}

	/**
	 * gets the max value of an array
	 *
	 * @return max
	 */
	public static int max(int... array) {
		var m = Integer.MIN_VALUE;
		for (var x : array) {
			if (x > m)
				m = x;
		}
		return m;
	}

	/**
	 * gets the max value of an array
	 */
	public static int max(Iterable<Integer> list) {
		var m = Integer.MIN_VALUE;
		for (var x : list) {
			if (x != null && x > m)
				m = x;
		}
		return m;
	}

	/**
	 * returns true, if string can be parsed as int
	 *
	 * @return true, if int
	 */
	public static boolean isInteger(String text) {
		return isInteger(text, text.startsWith("x") ? 16 : 10);
	}

	/**
	 * returns true, if string can be parsed as int
	 *
	 * @return true, if int
	 */
	public static boolean isInteger(String text, int base) {
		try {
			Integer.parseInt(text, base);
		} catch (Exception ex) {
			return false;
		}
		return true;
	}

	/**
	 * returns true, if string can be parsed as a boolean
	 *
	 * @return true, if boolean
	 */
	public static boolean isBoolean(String text) {
		return text.equalsIgnoreCase("true") || text.equalsIgnoreCase("false");
	}

	/**
	 * returns true, if string can be parsed as boolean
	 *
	 * @return true, if boolean
	 */
	public static boolean parseBoolean(String text) {
		text = text.trim();
		return text.length() >= 4 && text.substring(0, 4).toLowerCase().startsWith("true");
	}

	/**
	 * returns true, if string can be parsed as long
	 *
	 * @return true, if long
	 */
	public static boolean isLong(String text) {
		try {
			Long.parseLong(text);
		} catch (Exception ex) {
			return false;
		}
		return true;
	}

	/**
	 * returns true, if string can be parsed as float
	 *
	 * @return true, if float
	 */
	public static boolean isFloat(String text) {
		try {
			Float.parseFloat(text);
		} catch (Exception ex) {
			return false;
		}
		return true;
	}

	/**
	 * returns true, if string can be parsed as double
	 *
	 * @return true, if double
	 */
	public static boolean isDouble(String text) {
		try {
			Double.parseDouble(text);
		} catch (Exception ex) {
			return false;
		}
		return true;
	}

	/**
	 * attempts to parse the string as an integer, skipping leading chars and trailing characters, if necessary.
	 * Returns 0, if no number found
	 *
	 * @return value or 0
	 */
	public static int parseInt(String string) {
		try {
			if (string != null) {
				var start = 0;
				while (start < string.length()) {
					var ch = string.charAt(start);
					if (Character.isDigit(ch) || ch == '-')
						break;
					start++;
				}
				if (start < string.length()) {
					var finish = start + 1;
					while (finish < string.length() && Character.isDigit(string.charAt(finish)))
						finish++;
					if (start < finish)
						return Integer.parseInt(string.substring(start, finish));
				}
			}
		} catch (Exception ex) {
			if (string.equalsIgnoreCase("-inf"))
				return Integer.MIN_VALUE;
			else if (string.equalsIgnoreCase("inf"))
				return Integer.MAX_VALUE;

		}
		return 0;
	}

	/**
	 * attempt to parse an integer
	 *
	 * @param text the text
	 * @param base the base
	 * @return integer or 0
	 */
	public static int parseInt(String text, int base) {
		try {
			if (text != null) {
				var start = 0;
				while (start < text.length()) {
					var ch = text.charAt(start);
					if (Character.isLetterOrDigit(ch) || ch == '-')
						break;
					start++;
				}
				if (start < text.length()) {
					var finish = start + 1;
					while (finish < text.length() && isInteger(String.valueOf(text.charAt(finish)), base))
						finish++;
					if (start < finish)
						return Integer.parseInt(text.substring(start, finish), 16);
				}
			}
		} catch (Exception ex) {
			if (text.equalsIgnoreCase("-inf"))
				return Integer.MIN_VALUE;
			else if (text.equalsIgnoreCase("inf"))
				return Integer.MAX_VALUE;
		}
		return 0;
	}

	/**
	 * attempts to parse the string as a long, skipping leading chars and trailing characters, if necessary
	 *
	 * @return value or 0
	 */
	public static long parseLong(String string) {
		try {
			if (string != null) {
				var start = 0;
				while (start < string.length()) {
					var ch = string.charAt(start);
					if (Character.isDigit(ch) || ch == '+' || ch == '-')
						break;
					start++;
				}
				if (start < string.length()) {
					var finish = start + 1;
					while (finish < string.length() && Character.isDigit(string.charAt(finish)))
						finish++;
					if (start < finish)
						return Long.parseLong(string.substring(start, finish));
				}
			}
		} catch (Exception ex) {
			if (string.equalsIgnoreCase("-inf"))
				return Long.MIN_VALUE;
			else if (string.equalsIgnoreCase("inf"))
				return Long.MAX_VALUE;
		}
		return 0;
	}

	/**
	 * attempts to parse the string as an float, skipping leading chars and trailing characters, if necessary
	 *
	 * @return value or 0
	 */
	public static float parseFloat(String string) {
		try {
			if (string != null) {
				var start = 0;
				while (start < string.length()) {
					var ch = string.charAt(start);
					if (Character.isDigit(ch) || ch == '+' || ch == '-')
						break;
					start++;
				}
				if (start < string.length()) {
					var finish = start + 1;
					while (finish < string.length() && (Character.isDigit(string.charAt(finish)) || string.charAt(finish) == '.'
														|| string.charAt(finish) == 'E' || string.charAt(finish) == 'e' || string.charAt(finish) == '-'))
						finish++;
					if (start < finish)
						return Float.parseFloat(string.substring(start, finish));
				}
			}
		} catch (Exception ex) {
			if (string.equalsIgnoreCase("-inf"))
				return Float.NEGATIVE_INFINITY;
			else if (string.equalsIgnoreCase("inf"))
				return Float.POSITIVE_INFINITY;
		}
		return 0;
	}

	/**
	 * attempts to parse the string as a double, skipping leading chars and trailing characters, if necessary
	 *
	 * @return value or 0
	 */
	public static double parseDouble(String string) {
		try {
			if (string != null) {
				var start = 0;
				while (start < string.length()) {
					var ch = string.charAt(start);
					if (Character.isDigit(ch) || ch == '+' || ch == '-')
						break;
					start++;
				}
				if (start < string.length()) {
					var finish = start + 1;
					while (finish < string.length() && (Character.isDigit(string.charAt(finish)) || string.charAt(finish) == '.'
														|| string.charAt(finish) == 'E' || string.charAt(finish) == 'e' || string.charAt(finish) == '-' || string.charAt(finish) == '+'))
						finish++;
					if (start < finish)
						return Double.parseDouble(string.substring(start, finish));
				}
			}
		} catch (Exception ex) {
			if (string.equalsIgnoreCase("-inf"))
				return Double.NEGATIVE_INFINITY;
			else if (string.equalsIgnoreCase("inf"))
				return Double.POSITIVE_INFINITY;
		}
		return 0;
	}

	/**
	 * restrict a value to a given range
	 *
	 * @return value between min and max
	 */
	public static int restrictToRange(int min, int max, int value) {
		if (value < min)
			return min;
		return Math.min(value, max);
	}

	/**
	 * restrict a value to a given range
	 *
	 * @return value between min and max
	 */
	public static double restrictToRange(double min, double max, double value) {
		if (value < min)
			return min;
		return Math.min(value, max);
	}

	/**
	 * transposes a matrix
	 *
	 * @return transposed
	 */
	public static float[][] transposeMatrix(float[][] matrix) {
		final float[][] transposed = new float[matrix[0].length][matrix.length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < transposed.length; j++) {
				transposed[j][i] = matrix[i][j];
			}
		}
		return transposed;
	}

	public static boolean equals(double a, double b, double threshold) {
		return Math.abs(a - b) <= threshold;
	}

	/**
	 * create a range of numbers
	 *
	 * @param low  lowest number (inclusive)
	 * @param high highest number (exclusive)
	 * @return all numbers [low,high)
	 */
	public static Collection<Integer> range(int low, int high) {
		var list = new ArrayList<Integer>();
		for (var i = low; i < high; i++)
			list.add(i);
		return list;
	}
}
