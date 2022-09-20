/*
 * StringUtils.java Copyright (C) 2022 Daniel H. Huson
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

import jloda.thirdparty.HexUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * string utilities
 * Daniel Huson, 10.2021
 */
public class StringUtils {
	/**
	 * returns a collection in a space-separated string
	 *
	 * @return space-separated string
	 */
	public static String collection2string(Collection<?> collection) {
		return collection2string(collection, " ");
	}

	/**
	 * returns a collection in a string
	 *
	 * @return space-separated string
	 */
	public static String collection2string(Collection<?> collection, String separator) {
		StringBuilder buf = new StringBuilder();
		boolean first = true;
		for (var obj : collection) {
			if (first)
				first = false;
			else
				buf.append(separator);
			buf.append(obj.toString());
		}
		return buf.toString();
	}

	/**
	 * converts a string containing spaces into an array of strings.
	 *
	 * @return array of strings that where originally separated by spaces
	 */
	public static String[] toArray(String str) {
		var list = new LinkedList<String>();

		for (int j, i = skipSpaces(str, 0); i < str.length(); i = skipSpaces(str, j)) {
			for (j = i + 1; j < str.length(); j++)
				if (Character.isSpaceChar(str.charAt(j)))
					break; // found next space
			list.add(str.substring(i, j));
		}
		return list.toArray(new String[0]);

	}

	/**
	 * converts a string containing newlines into a list of string
	 *
	 * @return list of strings
	 */
	public static List<String> toList(String str) {
		List<String> list = new LinkedList<>();

		int i = 0;
		while (i < str.length()) {
			int j = i + 1;
			while (j < str.length() && str.charAt(j) != '\n')
				j++;
			list.add(str.substring(i, j));
			i = j + 1;
		}
		return list;
	}

	/**
	 * folds the given string so that no line is longer than max length, if possible.
	 * Replaces all spaces by single spaces
	 *
	 * @return string folded at spaces
	 */
	public static String fold(String str, int maxLength) {
		return fold(str, maxLength, "\n");
	}

	/**
	 * folds the given string so that no line is longer than max length, if possible.
	 * Replaces all spaces by single spaces
	 *
	 * @return string folded at spaces
	 */
	public static String fold(String str, int maxLength, String lineBreakString) {
		var buf = new StringBuilder();
		var st = new StringTokenizer(str);
		int lineLength = 0;
		boolean first = true;
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			int pos = token.lastIndexOf(lineBreakString);
			if (pos != -1) {
				if (!first) {
					buf.append(" ");
				} else
					first = false;
				buf.append(token, 0, pos + lineBreakString.length());
				lineLength = 0;
				token = token.substring(pos + lineBreakString.length());
			}
			if (lineLength > 0 && lineLength + token.length() >= maxLength) {
				buf.append(lineBreakString);
				lineLength = 0;
			} else {
				if (!first) {
					buf.append(" ");
					lineLength++;
				} else
					first = false;
			}
			lineLength += token.length();
			buf.append(token);
		}
		return buf.toString();
	}

	/**
	 * fold hard to given length
	 *
	 * @return folded string
	 */
	public static String foldHard(String str, int length) {
		var buf = new StringBuilder();
		var pos = 0;
		for (var i = 0; i < str.length(); i++) {
			buf.append(str.charAt(i));
			if (str.charAt(i) == '\n')
				pos = 0;
			else
				pos++;
			if (pos == length) {
				buf.append("\n");
				pos = 0;
			}
		}
		// if ((str.length() % length) != 0)
		//     buf.append("\n");
		return buf.toString();
	}

	/**
	 * returns an array of integers as a separated string
	 *
	 * @return string representation
	 */
	public static String toString(int[] array) {
		return toString(array, 0, array.length, ", ");
	}

	/**
	 * returns an array of integers as a string
	 *
	 * @return string representation
	 */
	public static String toString(int[] array, String separator) {
		return toString(array, 0, array.length, separator);
	}

	/**
	 * returns the content of a file as a string
	 *
	 * @return string representation
	 */
	public static String toString(File file, String endOfLineChar) throws IOException {
		final StringBuilder buf = new StringBuilder();
		try (BufferedReader r = new BufferedReader(new FileReader(file))) {
			String aLine;
			while ((aLine = r.readLine()) != null) {
				buf.append(aLine).append(endOfLineChar);
			}
		}
		return buf.toString();
	}

	/**
	 * returns an array of integers as a tring
	 *
	 * @return string representation
	 */
	public static String toString(int[] array, int offset, int length, String separator) {
		final var buf = new StringBuilder();

		var first = true;
		length = Math.min(offset + length, array.length);
		for (var i = offset; i < length; i++) {
			var x = array[i];
			if (first)
				first = false;
			else
				buf.append(separator);
			buf.append(x);
		}
		return buf.toString();
	}

	/**
	 * returns an array of floats as string
	 */
	public static String toString(float[] array, String separator) {
		return toString(array, 0, array.length, separator, false);
	}

	/**
	 * returns an array of floats as string
	 */
	public static String toString(float[] array, int offset, int length, String separator, boolean roundToInts) {
		final var buf = new StringBuilder();

		var first = true;
		length = Math.min(offset + length, array.length);
		for (var i = offset; i < length; i++) {
			float x = array[i];
			if (first)
				first = false;
			else
				buf.append(separator);
			if (roundToInts)
				buf.append(Math.round(x));
			else
				buf.append(x);
		}
		return buf.toString();
	}

	/**
	 * returns an array of integers as a separated string
	 *
	 * @return string representation
	 */
	public static String toString(Object[] array, String separator) {
		return toString(array, 0, array.length, separator);
	}

	/**
	 * returns an array of integers as a separated string
	 *
	 * @return string representation
	 */
	public static String toString(Object[] array, int offset, int length, String separator) {
		final var buf = new StringBuilder();

		var first = true;
		length = Math.min(length, array.length - offset);
		for (var i = 0; i < length; i++) {
			var anArray = array[i + offset];
			if (first)
				first = false;
			else
				buf.append(separator);
			buf.append(anArray);
		}
		return buf.toString();
	}

	/**
	 * returns an array of integers as a separated string
	 *
	 * @return string representation
	 */
	public static String toString(long[] array, String separator) {
		final var buf = new StringBuilder();

		var first = true;
		for (var a : array) {
			if (first)
				first = false;
			else
				buf.append(separator);
			buf.append(a);
		}
		return buf.toString();
	}

	/**
	 * returns an array of double as a separated string
	 *
	 * @return string representation
	 */
	public static String toString(double[] array, String separator) {
		final var buf = new StringBuilder();

		var first = true;
		for (var a : array) {
			if (first)
				first = false;
			else
				buf.append(separator);
			buf.append(a);
		}
		return buf.toString();
	}

	/**
	 * returns an array of double as a separated string
	 *
	 * @return string representation
	 */
	public static String toString(String format, double[] array, String separator) {
		final var buf = new StringBuilder();

		var first = true;
		for (var a : array) {
			if (first)
				first = false;
			else
				buf.append(separator);
			buf.append(String.format(format, a));
		}
		return buf.toString();
	}

	/**
	 * returns a collection of objects a separated string
	 *
	 * @return string representation
	 */
	public static <T> String toString(Collection<T> collection, String separator) {
		if (collection == null)
			return "";
		final var buf = new StringBuilder();

		var first = true;
		for (T object : collection) {
			if (object != null) {
				if (first)
					first = false;
				else if (separator != null)
					buf.append(separator);
				buf.append(object);
			}
		}
		return buf.toString();
	}

	/**
	 * concatenates a collection of strings and removes any white spaces
	 *
	 * @return concatenated string with no white spaces
	 */
	public static String concatenateAndRemoveWhiteSpaces(Collection<String> strings) {
		final var buf = new StringBuilder();

		for (var s : strings) {
			for (var pos = 0; pos < s.length(); pos++) {
				var ch = s.charAt(pos);
				if (!Character.isWhitespace(ch))
					buf.append(ch);
			}
		}
		return buf.toString();
	}

	/**
	 * returns a set a comma separated string
	 *
	 * @return string representation
	 */
	public static String toString(BitSet set) {
		if (set == null)
			return "null";

		final var buf = new StringBuilder();

		var startRun = 0;
		var inRun = 0;
		var first = true;
		for (var i = set.nextSetBit(0); i >= 0; i = set.nextSetBit(i + 1)) {
			if (first) {
				first = false;
				buf.append(i);
				startRun = inRun = i;
			} else {
				if (i == inRun + 1) {
					inRun = i;
				} else if (i > inRun + 1) {
					if (inRun == startRun || i == startRun + 1)
						buf.append(",").append(i);
					else if (inRun == startRun + 1)
						buf.append(",").append(inRun).append(",").append(i);
					else
						buf.append("-").append(inRun).append(",").append(i);
					inRun = startRun = i;
				}
			}
		}
		// dump last:
		if (inRun == startRun + 1)
			buf.append(",").append(inRun);
		else if (inRun > startRun + 1)
			buf.append("-").append(inRun);
		return buf.toString();
	}

	/**
	 * gets members of bit set as as string
	 *
	 * @return string
	 */
	public static String toString(BitSet set, char separator) {
		var buf = new StringBuilder();
		var first = true;
		for (var i = set.nextSetBit(0); i != -1; i = set.nextSetBit(i + 1)) {
			if (first)
				first = false;
			else
				buf.append(separator);
			buf.append(i);
		}
		return buf.toString();
	}

	/**
	 * returns an iterable collection of objects as separator separated string
	 *
	 * @return string representation
	 */
	public static <T> String toString(Iterable<T> iterable, String separator) {
		return toString(iterable.iterator(), separator);
	}

	/**
	 * returns a collection of objects a separated string
	 *
	 * @return string representation
	 */
	public static <T> String toString(Iterator<T> iterator, String separator) {
		if (iterator == null)
			return "";
		final var buf = new StringBuilder();

		while (iterator.hasNext()) {
			if (buf.length() > 0)
				buf.append(separator);
			T next = iterator.next();
			buf.append(next);
		}
		return buf.toString();
	}

	/**
	 * converts a list of objects to a string
	 *
	 * @return string
	 */
	public static <T> String listAsString(List<T> result, String separator) {
		final var buf = new StringBuilder();
		var first = true;
		for (T aResult : result) {
			if (first)
				first = false;
			else
				buf.append(separator);
			buf.append(aResult.toString());
		}
		return buf.toString();
	}

	/**
	 * trims away empty lines at the beginning and end of a string
	 *
	 * @return string without leading and trailing empty lines
	 */
	public static String trimEmptyLines(String str) {
		var startOfLine = 0;
		for (var p = 0; p < str.length(); p++) {
			if (!Character.isSpaceChar(str.charAt(p)))
				break;
			else if (str.charAt(p) == '\n' || str.charAt(p) == '\r')
				startOfLine = p + 1;
		}

		var endOfLine = str.length();
		for (var p = str.length() - 1; p >= 0; p--) {
			if (!Character.isSpaceChar(str.charAt(p)))
				break;
			else if (str.charAt(p) == '\n' || str.charAt(p) == '\r')
				endOfLine = p;
		}

		if (startOfLine < endOfLine && endOfLine <= str.length()) {
			return str.substring(startOfLine, endOfLine);
		} else
			return str;
	}

	/**
	 * counts the number of occurrences of c in text
	 *
	 * @return count
	 */
	public static int countOccurrences(String text, char c) {
		var count = 0;
		if (text != null) {
			for (var i = 0; i < text.length(); i++)
				if (text.charAt(i) == c)
					count++;
		}
		return count;
	}

	/**
	 * counts the number of occurrences of word in text
	 *
	 * @return count
	 */
	public static int countOccurrences(String text, String word) {
		var count = 0;
		for (var i = text.indexOf(word); i != -1; i = text.indexOf(word, i + 1))
			count++;
		return count;
	}

	/**
	 * counts the number of occurrences of c at beginning of text
	 *
	 * @return count
	 */
	public static int countLeadingOccurrences(String text, char c) {
		var count = 0;
		if (text != null) {
			for (var i = 0; i < text.length(); i++) {
				if (text.charAt(i) == c)
					count++;
				else break;
			}
		}
		return count;
	}

	/**
	 * counts the number of occurrences of c in byte[] text
	 *
	 * @return count
	 */
	public static int countOccurrences(byte[] text, char c) {
		var count = 0;
		if (text != null) {
			for (byte aStr : text)
				if (aStr == c)
					count++;
		}
		return count;
	}

	/**
	 * cleans a taxon name so that it only contains of letters, digits, .'s and _'s
	 *
	 * @return clean taxon name
	 */
	public static String toCleanName(String name) {
		if (name == null)
			return "";
		name = name.replaceAll("\\s+", " ").trim();

		var buf = new StringBuilder();
		for (int i = 0; i < name.length(); i++) {
			char ch = name.charAt(i);
			if (Character.isLetterOrDigit(ch) || ch == '.' || ch == '_' || ch == '-')
				buf.append(ch);
			else
				buf.append("_");
		}
		var str = buf.toString();
		while (str.length() > 1 && str.startsWith("_"))
			str = str.substring(1);
		while (str.length() > 1 && str.endsWith("_"))
			str = str.substring(0, str.length() - 1);

		return str;
	}

	/**
	 * gets all individual non-empty lines from a string
	 *
	 * @return lines
	 */
	public static List<String> getLinesFromString(String string) {
		return getLinesFromString(string, Integer.MAX_VALUE);
	}

	/**
	 * gets all individual non-empty lines from a string
	 *
	 * @return lines
	 */
	public static List<String> getLinesFromString(String string, int maxCount) {
		List<String> result = new LinkedList<>();
		try (var r = new BufferedReader(new StringReader(string))) {
			String aLine;
			while ((aLine = r.readLine()) != null) {
				aLine = aLine.trim();
				if (aLine.length() > 0) {
					result.add(aLine);
					if (result.size() >= maxCount)
						break;
				}
			}
		} catch (IOException ignored) {
		}
		return result;
	}

	/**
	 * remove any strings that are empty or start with #, after trimming. Keep all non-string objects
	 *
	 * @return cleaned listed of strings
	 */
	public static <T> List<T> cleanListOfStrings(Collection<T> list) {
		var result = new LinkedList<T>();
		for (T obj : list) {
			if (obj instanceof String) {
				String str = ((String) obj).trim();
				if (str.length() > 0 && !str.startsWith("#"))
					result.add((T) str);
			} else
				result.add(obj);
		}
		return result;
	}

	/**
	 * insert spaces before uppercases that follow lower case letters
	 */
	public static String insertSpacesBetweenLowerCaseAndUpperCaseLetters(String x) {
		var buf = new StringBuilder();

		for (var i = 0; i < x.length(); i++) {
			if (i > 0 && Character.isLowerCase(x.charAt(i - 1)) && Character.isUpperCase(x.charAt(i)))
				buf.append(' ');
			buf.append(x.charAt(i));
		}
		return buf.toString();
	}

	/**
	 * convert bytes to a string
	 *
	 * @return string
	 */
	static public String toString(byte[] bytes) {
		if (bytes == null)
			return "";
		var array = new char[bytes.length];
		for (var i = 0; i < bytes.length; i++)
			array[i] = (char) bytes[i];
		return new String(array);
	}

	/**
	 * convert bytes to a string
	 *
	 * @param length number of bytes, starting at index 0
	 * @return string
	 */
	static public String toString(byte[] bytes, int length) {
		if (bytes == null)
			return "";
		var array = new char[length];
		for (var i = 0; i < length; i++)
			array[i] = (char) bytes[i];
		return new String(array);
	}

	/**
	 * convert bytes to a string
	 *
	 * @param offset start here
	 * @param length number of bytes
	 * @return string
	 */
	static public String toString(byte[] bytes, int offset, int length) {
		if (bytes == null)
			return "";
		var array = new char[length];
		for (var i = 0; i < length; i++)
			array[i] = (char) bytes[i + offset];
		return new String(array);
	}

	/**
	 * convert boolean to a string
	 *
	 * @return string
	 */
	static public String toString(boolean[] bools) {
		var buf = new StringBuilder();
		if (bools != null) {
			for (var a : bools) {
				buf.append(a ? "1" : "0");
			}
		}
		return buf.toString();
	}

	/**
	 * convert chars to a string
	 *
	 * @return string
	 */
	static public String toString(char[] chars) {
		return new String(chars);
	}

	/**
	 * capitalize the first letter of a string
	 *
	 * @return capitalized word
	 */
	public static String capitalizeFirstLetter(String s) {
		if (s.length() > 0 && Character.isLetter(s.charAt(0)) && Character.isLowerCase(s.charAt(0))) {
			return Character.toUpperCase(s.charAt(0)) + s.substring(1);
		} else
			return s;
	}

	/**
	 * returns the first line of a text
	 *
	 * @return first line1
	 */
	public static String getFirstLine(String text) {
		return text.lines().findFirst().orElse("");
	}

	/**
	 * returns the first line of a text
	 *
	 * @return first line1
	 */
	public static String getFirstLine(String text,boolean notBlank) {
		return text.lines().filter(ln->!notBlank || !ln.isBlank()).findFirst().orElse("");
	}

	/**
	 * returns the first line of a text.
	 * Breaks at \n, \r or 0
	 */
	public static String getFirstLine(byte[] text) {
		if (text == null)
			return "";
		final var buf = new StringBuilder();
		for (var b : text) {
			var ch = (char) b;
			if (ch == '\r' || ch == '\n' || ch == 0)
				break;
			else
				buf.append(ch);
		}
		return buf.toString();
	}

	/**
	 * does text start with given word?
	 */
	public static boolean startsWith(byte[] text, byte[] word) {
		if (text == null || text.length < word.length)
			return false;
		for (int i = 0; i < word.length; i++) {
			if (word[i] != text[i])
				return false;
		}
		return true;
	}

	public static boolean startsWith(byte[] text, String word) {
		return startsWith(text, word.getBytes());
	}

	/**
	 * returns the last line beginning with query
	 *
	 * @return first line
	 */
	public static String getLastLineStartingWith(String query, String text) {
		String result = null;

		try (var r = new BufferedReader(new StringReader(text))) {
			String aLine;
			while ((aLine = r.readLine()) != null) {
				if (aLine.startsWith(query))
					result = aLine;
			}
		} catch (IOException e) {
			Basic.caught(e);
		}
		return Objects.requireNonNullElse(result, "");
	}

	/**
	 * get the last line in a text
	 *
	 * @return last line or empty string
	 */
	public static String getLastLine(String text) {
		if (text == null)
			return "";
		int pos = text.lastIndexOf("\r");
		if (pos == text.length() - 1)
			pos = text.lastIndexOf("\r", pos - 1);
		if (pos != -1)
			return text.substring(pos + 1);
		pos = text.lastIndexOf("\n");
		if (pos == text.length() - 1)
			pos = text.lastIndexOf("\n", pos - 1);
		if (pos != -1)
			return text.substring(pos + 1);
		return text;
	}

	/**
	 * gets the index of the first space in the string
	 *
	 * @return index or -1
	 */
	public static int getIndexOfFirstWhiteSpace(String string) {
		for (var i = 0; i < string.length(); i++)
			if (Character.isWhitespace(string.charAt(i)))
				return i;
		return -1;
	}

	/**
	 * gets the first word in the given text
	 *
	 * @return word (delimited by a white space) or empty string, if the first character is a white space
	 */
	public static String getFirstWord(String text) {
		var i = getIndexOfFirstWhiteSpace(text);
		if (i != -1)
			return text.substring(0, i);
		else
			return text;
	}

	/**
	 * gets the last word in the given text
	 *
	 * @return word (delimited by a white space) or empty string, if the last character is a white space
	 */
	public static String getLastWord(String text) {
		if (text.length() == 0 || Character.isWhitespace(text.charAt(text.length() - 1)))
			return "";
		for (var i = text.length() - 2; i >= 0; i--) {
			if (Character.isWhitespace(text.charAt(i)))
				return text.substring(i + 1);
		}
		return text;
	}

	/**
	 * gets the first word in the given src string and returns it in the target string
	 *
	 * @return length
	 */
	public static int getFirstWord(byte[] src, byte[] target) {
		for (var i = 0; i < src.length; i++) {
			if (Character.isWhitespace((char) src[i]) || src[i] == 0) {
				return i;
			}
			target[i] = src[i];
		}
		return src.length;
	}

	public static String getAccessionWord(String text) {
		var a = 0;
		while (a < text.length()) {
			var ch = text.charAt(a);
			if (Character.isWhitespace(ch) || ch == '@' || ch == '>')
				a++;
			else
				break;
		}
		var b = a;
		while (b < text.length()) {
			var ch = text.charAt(b);
			if (Character.isLetterOrDigit(ch) || ch == '_')
				b++;
			else
				break;
		}
		return text.substring(a, b);
	}

	public static String getAccessionWord(byte[] text) {
		var a = 0;
		while (a < text.length) {
			var ch = text[a];
			if (Character.isWhitespace(ch) || ch == '@' || ch == '>')
				a++;
			else
				break;
		}
		var b = a;
		while (b < text.length) {
			var ch = text[b];
			if (Character.isLetterOrDigit(ch) || ch == '_')
				b++;
			else
				break;
		}
		return toString(text, a, b - a);
	}

	/**
	 * remove all white spaces
	 *
	 * @return string without white spaces
	 */
	public static String removeAllWhiteSpaces(String s) {
		var buf = new StringBuilder();
		for (var i = 0; i < s.length(); i++) {
			if (!Character.isWhitespace(s.charAt(i)))
				buf.append(s.charAt(i));
		}
		return buf.toString();
	}

	/**
	 * reverse a string
	 *
	 * @return reversed string
	 */
	public static String reverseString(String s) {
		var buf = new StringBuilder();
		for (var i = s.length() - 1; i >= 0; i--)
			buf.append(s.charAt(i));
		return buf.toString();
	}

	/**
	 * get a string of spaces
	 *
	 * @return spaces
	 */
	public static String spaces(int count) {
		return " ".repeat(Math.max(0, count));
	}

	/**
	 * gets the common name prefix of a set of files
	 *
	 * @return prefix
	 */
	public static String getCommonPrefix(File[] files, String defaultPrefix) {
		List<String> names = new LinkedList<>();
		for (var file : files)
			names.add(file.getName());
		return getCommonPrefix(names, defaultPrefix);
	}

	/**
	 * gets the common prefix of a set of names
	 *
	 * @return prefix
	 */
	public static String getCommonPrefix(List<String> names, String defaultPrefix) {
		if (names.size() == 0)
			return "";
		else if (names.size() == 1)
			return FileUtils.getFileBaseName(names.get(0));

		var posOfFirstDifference = 0;

		var ok = true;
		while (ok) {
			int ch = 0;
			for (var name : names) {
				if (posOfFirstDifference >= name.length()) {
					ok = false;
					break;
				}
				if (ch == 0)
					ch = name.charAt(posOfFirstDifference);
				else if (name.charAt(posOfFirstDifference) != ch) {
					ok = false;
					break;
				}
			}
			posOfFirstDifference++;
		}

		// get rid of trailing spaces, _ and .
		var name = names.get(0);
		while (posOfFirstDifference > 0) {
			int ch = name.charAt(posOfFirstDifference - 1);
			if (ch != '.' && ch != '_' && !Character.isWhitespace(ch))
				break;
			posOfFirstDifference--;
		}

		if (posOfFirstDifference > 0)
			return name.substring(0, posOfFirstDifference);
		return defaultPrefix;
	}

	/**
	 * swallow leading >, if present
	 *
	 * @return string with leading > removed
	 */
	public static String swallowLeadingGreaterSign(String word) {
		if (word.startsWith(">"))
			return word.substring(1).trim();
		else
			return word;
	}

	/**
	 * swallow a leading @, if present
	 *
	 * @return string with leading > removed
	 */
	public static String swallowLeadingAtSign(String word) {
		if (word.startsWith("@"))
			return word.substring(1).trim();
		else
			return word;
	}

	/**
	 * swallow a leading >, if present
	 *
	 * @return string with leading > removed
	 */
	public static String swallowLeadingGreaterOrAtSign(String word) {
		if (word.startsWith(">") || word.startsWith("@"))
			return word.substring(1).trim();
		else
			return word;
	}

	public static String capitalizeWords(String str) {
		var buf = new StringBuilder();
		boolean previousWasSpaceOrPunctuation = true;
		for (var i = 0; i < str.length(); i++) {
			var ch = str.charAt(i);
			if (Character.isWhitespace(ch) || ".:".contains("" + ch)) {
                buf.append(ch);
                previousWasSpaceOrPunctuation = true;
            } else {
				if (previousWasSpaceOrPunctuation && Character.isLetter(ch))
                    buf.append(Character.toUpperCase(ch));
				else if (Character.isLetter(ch))
                    buf.append(Character.toLowerCase(ch));
				else
                    buf.append(ch);
				previousWasSpaceOrPunctuation = false;
			}
		}
		return buf.toString();
	}

	/**
	 * replace all backslashes by double backslashes
	 *
	 * @return string with protected back slashes
	 */
	public static String protectBackSlashes(String str) {
		if (str == null)
			return null;
		var buf = new StringBuilder();
		for (int i = 0; i < str.length(); i++) {
			buf.append(str.charAt(i));
			if (str.charAt(i) == '\\')
				buf.append('\\');
		}
		return buf.toString();
	}

	/**
	 * split a string by the given separator, but honoring quotes around items
	 *
	 * @return tokens
	 */
	public static String[] splitWithQuotes(String string, char separator) {
		//return string.split(""+separator);

		List<String> list = new LinkedList<>();
		int i = 0;
		while (i < string.length()) {
			if (string.charAt(i) == '\"') { // start of quoted item
				int j = string.indexOf('\"', i + 1);
				if (j == -1) {
					list.add(string.substring(i + 1).trim());
					break;  // unfinished quote, really should throw an exception
				} else {
					list.add(string.substring(i + 1, j).trim());
					i = j + 2;
				}
			} else if (string.charAt(i) == separator) { // separator that follows a separator...
				list.add("");
				i++;
			} else // start of unquoted item
			{
				int j = i + 1;
				while (j < string.length()) {
					if (string.charAt(j) == separator)
						break;
					j++;
				}
				list.add(string.substring(i, j).trim());
				i = j + 1;
			}
		}
		return list.toArray(new String[0]);
	}

	/**
	 * returns a quoted string if the string for value contains a tab and not a quote
	 *
	 * @return string or quoted string
	 */
	public static String quoteIfContainsTab(Object value) {
		String string = value.toString();
		if (string.contains("\t") && !string.contains("\""))
			return "\"" + string + "\"";
		else
			return string;
	}

	/**
	 * gets the name of a read. This is the first word in the line, skipping any '>' or '@' at first position
	 *
	 * @return word (delimited by a space)
	 */
	public static String getReadName(String aLine) {
		if (aLine.length() == 0)
			return "";
		int start;
		if (aLine.charAt(0) == '@' || aLine.charAt(0) == '>')
			start = 1;
		else
			start = 0;
		while (start < aLine.length() && Character.isWhitespace(aLine.charAt(start)))
			start++;
		int finish = start;
		while (finish < aLine.length() && !Character.isWhitespace(aLine.charAt(finish)))
			finish++;
		return aLine.substring(start, finish);
	}

	/**
	 * is either a single word or consists only of spaces
	 *
	 * @return true if a single word or consists only of spaces
	 */
	public static boolean isOneWord(String str) {
		str = str.trim();
		for (int i = 0; i < str.length(); i++) {
			if (Character.isWhitespace(str.charAt(i)))
				return false;
		}
		return true;
	}

	/**
	 * split string on given character. Note that results are subsequently trimmed
	 *
	 * @return split string, trimmed
	 */
	public static String[] split(String aLine, char splitChar) {
		return split(aLine, splitChar, Integer.MAX_VALUE, false);
	}

	/**
	 * split string on given character. Note that results are subsequently trimmed
	 *
	 * @return split string, trimmed
	 */
	public static String[] split(String aLine, char splitChar, int maxTokens) {
		return split(aLine, splitChar, maxTokens, false);
	}

	/**
	 * split string on given character. Note that results are subsequently trimmed
	 *
	 * @return split string, trimmed
	 */
	public static String[] split(String aLine, char splitChar, int maxTokens, boolean skipEmptyTokens) {
		aLine = aLine.trim();
		if (aLine.length() == 0 || maxTokens <= 0)
			return new String[0];

		// need to ignore last position if it is the split character
		final int length = (aLine.charAt(aLine.length() - 1) == splitChar ? aLine.length() - 1 : aLine.length());

		// count the number of tokens
		int count = 1;
		{
			int prev = -1;
			if (maxTokens > 1) {
				for (int i = 0; i < length; i++) {
					if (aLine.charAt(i) == splitChar) {
						if (!skipEmptyTokens || i > prev + 1) {
							if (count < maxTokens)
								count++;
							else
								break;
						}
						prev = i;
					}
				}
			}
		}

		final String[] result = new String[count];
		int prev = 0;
		int which = 0;
		int pos = 0;
		for (; pos < length; pos++) {
			if (aLine.charAt(pos) == splitChar) {
				if (!skipEmptyTokens || pos > prev + 1) {
					result[which++] = aLine.substring(prev, pos).trim();
					prev = pos + 1;
					if (which == count)
						return result;
				}
			}
		}
		if (pos > prev) {
			result[which] = aLine.substring(prev, pos).trim();
		}
		return result;
	}

	/**
	 * split string on white space
	 *
	 * @return split string, trimmed
	 */
	public static String[] splitOnWhiteSpace(String aLine) {
		ArrayList<String> parts = new ArrayList<>();

		int start = -1;
		for (int i = 0; i < aLine.length(); i++) {
			int ch = aLine.charAt(i);
			if (Character.isWhitespace(ch)) {
				if (start != -1) {
					parts.add(aLine.substring(start, i));
					start = -1;
				}
			} else {
				if (start == -1)
					start = i;
			}
		}
		if (start != -1)
			parts.add(aLine.substring(start));
		return parts.toArray(new String[0]);
	}

	/**
	 * split string on given characters. Note that results are subsequently trimmed
	 *
	 * @return split string, trimmed
	 */
	public static String[] split(String aLine, char splitChar, char... splitChars) {
		if (aLine.length() == 0)
			return new String[0];

		int count = (aLine.charAt(aLine.length() - 1) == splitChar || contains(splitChars, aLine.charAt(aLine.length() - 1)) ? 0 : 1);

		for (int i = 0; i < aLine.length(); i++)
			if (aLine.charAt(i) == splitChar || contains(splitChars, aLine.charAt(i)))
				count++;
		if (count == 1)
			return new String[]{aLine};
		final String[] result = new String[count];
		int prev = 0;
		int which = 0;
		int pos = 0;
		for (; pos < aLine.length(); pos++) {
			if (aLine.charAt(pos) == splitChar || contains(splitChars, aLine.charAt(pos))) {
				result[which++] = aLine.substring(prev, pos).trim();
				prev = pos + 1;
			}
		}
		if (pos > prev) {
			result[which] = aLine.substring(prev, pos).trim();
		}
		return result;
	}

	public static boolean notBlank(String s) {
		return s != null && s.trim().length() > 0;
	}

	public static boolean intersects(String a, String b) {
		for (int i = 0; i < b.length(); i++) {
			if (a.contains("" + b.charAt(i)))
				return true;
		}
		return false;
	}

	/**
	 * determines whether longString contains shortString as a sub-sequence.
	 * That is all, letters of the shortString appear in order in the longString, but not necessarily consecutively
	 */
	static boolean subsequenceOf(String longString, String shortString) {
		int where = -1;
		for (int i = 0; i < shortString.length(); i++) {
			if (where == -1)
				where = longString.indexOf(shortString.charAt(i));
			else
				where = longString.indexOf(shortString.charAt(i), where + 1);
			if (where == -1)
				return false;
		}
		return true;
	}

	/**
	 * counts commands in a string.
	 */
	public static int countCommands(String s) {
		s = s.trim();
		if (s.endsWith(";"))
			return countOccurrences(s, ';');
		else
			return countOccurrences(s, ';') + 1;
	}

	/**
	 * Get string representation of a double matrix
	 *
	 * @return string representation
	 */
	public static String toString(double[][] matrix) {
		StringBuilder buf = new StringBuilder();
		for (double[] row : matrix) {
			buf.append(toString(row, " ")).append("\n");
		}
		return buf.toString();
	}

	/**
	 * gets value as binary string, always showing all 64 positions
	 *
	 * @return binary string
	 */
	public static String toBinaryString(long value) {
		StringBuilder buf = new StringBuilder();
		for (int shift = 63; shift >= 0; shift--) {
			buf.append((value & (1L << shift)) >>> shift);
		}
		return buf.toString();
	}

	/**
	 * return array in reverse order
	 */
	public static String[] reverse(String[] strings) {
		String[] result = new String[strings.length];
		for (int i = 0; i < strings.length; i++)
			result[strings.length - 1 - i] = strings[i];
		return result;
	}

	/**
	 * does the given string contain the given count of character ch?
	 *
	 * @return true, if string contains atleast count occurrences of ch
	 */
	public static boolean contains(String string, char ch, int count) {
		for (int i = 0; i < string.length(); i++) {
			if (string.charAt(i) == ch && --count == 0)
				return true;
		}
		return false;
	}

	/**
	 * does the given array of characters contain the given one?
	 *
	 * @return true, if contained
	 */
	public static boolean contains(char[] string, char ch) {
		for (char a : string) {
			if (a == ch)
				return true;
		}
		return false;
	}

	/**
	 * abbreviate a string to the given length
	 *
	 * @return abbreviated string
	 */
	public static String abbreviate(String string, int length) {
		if (string.length() <= length)
			return string;
		else
			return string.substring(0, length - 1) + ".";
	}

	/**
	 * abbreviate a string to the given length
	 *
	 * @return abbreviated string
	 */
	public static String abbreviateDotDotDot(String string, int length) {
		if (string.length() <= length)
			return string;
		else
			return string.substring(0, length - 1) + "...";

	}

	/**
	 * skip the first line in a string
	 *
	 * @return first line
	 */
	public static String skipFirstLine(String string) {
		int pos = string.indexOf('\n');
		if (pos != -1)
			return string.substring(pos + 1);
		else
			return string;
	}

	/**
	 * skip the first word in a string and trim
	 *
	 * @return first word
	 */
	public static String skipFirstWord(String string) {
		for (int pos = 0; pos < string.length(); pos++) {
			if (Character.isWhitespace(string.charAt(pos)))
				return string.substring(pos).trim();
		}
		return "";
	}

	/**
	 * convert a string with spaces and/or underscores to camel case
	 *
	 * @return camel case
	 */
	public static String toCamelCase(String string) {
		final var buf = new StringBuilder();
		if (string != null) {
			var pos = 0;
			while (pos < string.length() && (Character.isWhitespace(string.charAt(pos)) || string.charAt(pos) == '_'))
				pos++;
			var afterWhiteSpace = false;
			while (pos < string.length()) {
				final var ch = string.charAt(pos);
				if (Character.isWhitespace(ch) || ch == '_')
					afterWhiteSpace = true;
				else if (afterWhiteSpace) {
					buf.append(Character.toUpperCase(ch));
					afterWhiteSpace = false;
				} else
					buf.append(Character.toLowerCase(ch));
				pos++;
			}
		}
		return buf.toString();
	}

	/**
	 * convert a string from camel case
	 *
	 * @return camel case
	 */
	public static String fromCamelCase(String string) {
		final var buf = new StringBuilder();
		if (string != null) {
			var afterWhiteSpace = true;
			var afterCapital = false;
			for (var pos = 0; pos < string.length(); pos++) {
				final var ch = string.charAt(pos);
				if (Character.isUpperCase(ch)) {
					if (!afterWhiteSpace && !afterCapital) {
						buf.append(" ");
					}
					afterCapital = true;
				} else
					afterCapital = false;
				buf.append(ch);
				afterWhiteSpace = (Character.isWhitespace(ch));
			}
		}
		return buf.toString();
	}

	/**
	 * gets next word after given first word
	 *
	 * @return next word or null
	 */
	public static String getWordAfter(String first, String text) {
		int start = text.indexOf(first);
		if (start == -1)
			return null;
		start += first.length();
		while (start < text.length() && (Character.isWhitespace(text.charAt(start))))
			start++;
		int finish = start;
		while (finish < text.length() && !Character.isWhitespace(text.charAt(finish)))
			finish++;
		if (finish < text.length())
			return text.substring(start, finish);
		else
			return text.substring(start);
	}

	/**
	 * gets everything after the first word
	 *
	 * @return everything after the given word or null
	 */
	public static String getTextAfter(String first, String text) {
		int start = text.indexOf(first);
		if (start == -1)
			return null;
		start += first.length();
		while (start < text.length() && Character.isWhitespace(text.charAt(start)))
			start++;
		return text.substring(start);
	}

	/**
	 * gets the word between left and right, or left and then end
	 *
	 * @return word or null
	 */
	public static String getWordBetween(String left, String right, String text) {
		int a = text.indexOf(left);
		if (a == -1)
			return null;
		else
			a += left.length();
		final int b = text.indexOf(right, a);
		if (b == -1)
			return text.substring(a);
		else
			return text.substring(a, b);
	}

	/**
	 * determines whether a ends with b, ignoring case
	 *
	 * @return true, if a ends with b, ignoring case
	 */
	public static boolean endsWithIgnoreCase(String a, String b) {
		return a.toLowerCase().endsWith(b.toLowerCase());
	}

	/**
	 * gets the last index of ch, or -1
	 *
	 * @return index or -1
	 */
	public static int lastIndexOf(byte[] bytes, int offset, int len, char ch) {
		while (--len >= 0) {
			if (bytes[offset + len] == ch)
				return offset + len;
		}
		return -1;
	}

	public static int skipNonWhiteSpace(String text, int pos) {
		while (pos < text.length() && !Character.isWhitespace(text.charAt(pos)))
			pos++;
		return pos;
	}

	public static int skipWhiteSpace(String text, int pos) {
		while (pos < text.length() && Character.isWhitespace(text.charAt(pos)))
			pos++;
		return pos;
	}


	public static String removeTrailingZerosAfterDot(double value) {
		return removeTrailingZerosAfterDot("%f", value);
	}

	public static String removeTrailingZerosAfterDot(String format, double value) {
		return removeTrailingZerosAfterDot(String.format(format, value));
	}

	public static String removeTrailingZerosAfterDot(String text) {
		if (text.contains("."))
			return text.replaceAll("\\.([0-9]+?)0*$", ".$1") // note: *? lazy, does keep trailing 0's
					.replaceAll("\\.([0-9]+?)0+(\\s)", ".$1$2")
					.replaceAll("\\.0+$", "").replaceAll("\\.0+(\\s)", "$1").replaceAll("^-0$", "0");
		else
			return text;
	}

	/**
	 * gets members of bit set as as string
	 *
	 * @return string
	 */
	public static String toString(BitSet set, String separator) {
		StringBuilder buf = new StringBuilder();
		boolean first = true;
		for (int i = set.nextSetBit(0); i != -1; i = set.nextSetBit(i + 1)) {
			if (first)
				first = false;
			else
				buf.append(separator);
			buf.append(i);
		}
		return buf.toString();
	}

	/**
	 * pretty print a double value
	 *
	 * @return value without trailing 0's
	 */
	public static String toString(double value, int afterCommaDigits) {
		final String format = "%." + afterCommaDigits + "f";
		return String.format(format, value).replaceAll("0*$", "").replaceAll("\\.$", "");
	}

	public static <S, T> String toString(Pair<S, T> pair, String separator) {
		return pair.getFirst().toString() + separator + pair.getSecond().toString();
	}

	/**
	 * trim all strings
	 *
	 * @return trimmed strings
	 */
	public static String[] trimAll(String[] strings) {
		final String[] result = new String[strings.length];
		for (int i = 0; i < strings.length; i++) {
			if (strings[i] != null)
				result[i] = strings[i].trim();
		}
		return result;
	}

	public static String[] toStrings(Object[] values) {
		final String[] strings = new String[values.length];
		for (int i = 0; i < values.length; i++) {
			if (values[i] != null)
				strings[i] = values[i].toString();
		}
		return strings;
	}

	public static String[] toStrings(Collection<?> values) {
		final String[] strings = new String[values.size()];
		int count = 0;
		for (Object value : values) {
			strings[count++] = (value == null ? null : value.toString());
		}
		return strings;
	}

	public static String[] replaceAll(String[] strings, String regEx, String replacement) {
		final String[] result = new String[strings.length];
		for (int i = 0; i < strings.length; i++) {
			result[i] = strings[i].replaceAll(regEx, replacement);
		}
		return result;
	}

	public static String[] remove(String[] names, String remove) {
		return Arrays.stream(names).filter((x) -> !x.equals(remove)).toArray(String[]::new);
	}

	public static String[] removePositions(BitSet positionsToRemove, String[] strings) {
		if (strings.length > 0) {
			final String[] result = new String[strings.length - positionsToRemove.cardinality()];
			for (int i = 0, j = 0; i < strings.length; i++) {
				if (!positionsToRemove.get(i))
					result[j++] = strings[i];
			}
			return result;
		} else
			return strings;
	}

	/**
	 * replaces all white spaces in the given string str  by the given character c.
	 * Represents consecutive spaces by one c
	 *
	 * @return string was spaces replaced
	 */
	public static String replaceSpaces(String str, char c) {
		StringBuilder buf = new StringBuilder();
		boolean prevWasSpace = false;
		for (int i = 0; i < str.length(); i++) {
			if (Character.isWhitespace(str.charAt(i))) {
				if (!prevWasSpace) {
					buf.append(c);
					prevWasSpace = true;
				}
			} else {
				buf.append(str.charAt(i));
				prevWasSpace = false;
			}
		}
		return buf.toString();
	}

	/**
	 * formats a string so that it looks nice in a dialog box
	 *
	 * @return formatted string
	 */
	public static String toMessageString(String message) {
		// insert line breaks
		StringBuilder buf = new StringBuilder();
		int lineLength = 0;
		int numLines = 0;
		for (int i = 0; i < message.length(); i++) {
			if (lineLength > 50 && Character.isSpaceChar(message.charAt(i))) {
				buf.append("\n");
				lineLength = 0;
				numLines++;
				if (numLines > 10) {
					buf.append("...");
					break;
				}
			}
			if (lineLength == 80) {
				buf.append("\n").append(message.charAt(i));
				lineLength = 0;
				numLines++;
			} else {
				buf.append(message.charAt(i));
				if (message.charAt(i) == '\n')
					lineLength = 0;
				else
					lineLength++;
			}
			if (buf.length() > 2000) {
				buf.append("...");
				break;
			}
		}
		return buf.toString();
	}

	/**
	 * gets the text in quotes, removing any quotes already present
	 *
	 * @return quoted text
	 */
	public static String getInCleanQuotes(String text) {
		return "\"" + text.replaceAll("\"", "") + "\"";
	}

	/**
	 * removes all text between any pair of left- and right-delimiters.
	 * No nesting
	 *
	 * @return string with comments removed
	 */
	public static String removeComments(String str, char leftDelimiter, char rightDelimiter) {
		var buf = new StringBuilder();

		boolean inComment = false;
		for (int i = 0; i < str.length(); i++) {
			char ch = str.charAt(i);
			if (inComment && ch == rightDelimiter) {
				inComment = false;
			} else if (ch == leftDelimiter) {
				inComment = true;
			} else if (!inComment)
				buf.append(ch);
		}
		return buf.toString();
	}

	/**
	 * does label match pattern?
	 *
	 * @return true, if match
	 */
	public static boolean matches(Pattern pattern, String label) {
		if (label == null)
			label = "";
		var matcher = pattern.matcher(label);
		return matcher.find();
	}

	/**
	 * gets the index of a string s in an array of strings
	 *
	 * @return index or -1
	 */
	public static int getIndex(String s, String... array) {
		for (var i = 0; i < array.length; i++)
			if (s.equals(array[i]))
				return i;
		return -1;
	}

	/**
	 * gets the index of a string s in a collection of strings
	 *
	 * @return index or -1
	 */
	public static int getIndex(String s, Collection<String> collection) {
		var count = 0;
		for (var a : collection) {
			if (a.equals(s))
				return count;
			count++;
		}
		return -1;
	}

	/**
	 * gets the number of non-white space characters in a string
	 *
	 * @return non-space chars
	 */
	static public int getNumberOfNonSpaceCharacters(String string) {
		var count = 0;
		for (var i = 0; i < string.length(); i++) {
			if (!Character.isWhitespace(string.charAt(i)))
				count++;
		}
		return count;
	}

	public static boolean isDate(String s) {
		long time;
		try {
			time = DateFormat.getDateInstance().parse(s).getTime();
		} catch (Exception ex) {
			return false;
		}
		return time > 1000;
	}

	/**
	 * Finds the value of the given enumeration by name, case-insensitive.
	 *
	 * @return enumeration value or null
	 */
	public static <T extends Enum<T>> T valueOfIgnoreCase(Class<T> enumeration, String name) {
		return Arrays.stream(enumeration.getEnumConstants()).filter(v -> v.name().equalsIgnoreCase(name)).findFirst().orElse(null);
	}

	/**
	 * Finds the value of the given enumeration by finding the first for which name appears as a subsequence (but not neccessarily substring)
	 *
	 * @return enumeration value or null
	 */
	public static <T extends Enum<T>> T valueOfMatchingSubsequence(Class<T> enumeration, String name) {

		return Arrays.stream(enumeration.getEnumConstants()).filter(v -> subsequenceOf(v.name(), name)).findFirst().orElse(null);
	}

	/**
	 * gets the desired column from a tab-separated line of tags
	 */
	public static String getTokenFromTabSeparatedLine(String aLine, int column) {
		int a = 0;
		int count = 0;
		for (int i = 0; i < aLine.length(); i++) {
			if (aLine.charAt(i) == '\t') {
				if (count == column)
					return aLine.substring(a, i);
				count++;
				if (count == column)
					a = i + 1;
			}
		}
		if (count == column)
			return aLine.substring(a);
		else
			return "";
	}

	/**
	 * gets the desired column from a tab-separated line of tags
	 */
	public static String getTokenFromTabSeparatedLine(byte[] aLine, int column) {
		int a = 0;
		int count = 0;
		for (int i = 0; i < aLine.length; i++) {
			if (aLine[i] == '\t') {
				if (count == column)
					return new String(aLine, a, i - a);
				count++;
				if (count == column)
					a = i + 1;
			}
		}
		if (count == column)
			return new String(aLine, a, aLine.length - a);
		else
			return "";
	}

	/**
	 * get comparator that compares by decreasing length of second and then lexicographical on first
	 *
	 * @return comparator
	 */
	public static Comparator<Pair<String, String>> getComparatorDecreasingLengthOfSecond() {
		return (pair1, pair2) -> { // sorting in decreasing order of length
			if (pair1.getSecond().length() > pair2.getSecond().length())
				return -1;
			else if (pair1.getSecond().length() < pair2.getSecond().length())
				return 1;
			else
				return pair1.getFirst().compareTo(pair2.getFirst());
		};
	}

	/**
	 * surrounds word with quotes if it contains character that is not a digit, letter or _
	 *
	 * @return str, quoted is necessary
	 */
	public static String quoteIfNecessary(String str) {
		for (int i = 0; i < str.length(); i++) {
			if (!Character.isLetterOrDigit(str.charAt(i)) && str.charAt(i) != '_')
				return "'" + str + "'";
		}
		return str;
	}

	/**
	 * get the value of an enumeration ignoring case
	 */
	public static <T extends Enum<T>> T valueOfIgnoreCase(Enum<T> enumeration, String string) {
		for (T value : enumeration.getDeclaringClass().getEnumConstants()) {
			if (value.toString().equalsIgnoreCase(string))
				return value;
		}
		return null;
	}

	/**
	 * count the number of words in a string
	 *
	 * @return number of words
	 */
	public static int countWords(String string) {
		return string.split("\\s+").length;
	}

	public static boolean isArrayOfIntegers(String string) {
		if (string == null)
			return false;
		final String[] tokens = string.split("[\\s+,;]");
		if (tokens.length == 0)
			return false;
		for (String token : tokens) {
			if (!NumberUtils.isInteger(token))
				return false;
		}
		return true;
	}

	public static int[] parseArrayOfIntegers(String string) {
		if (string == null)
			return null;
		final String[] tokens = string.split("[\\s+,;]");
		ArrayList<Integer> values = new ArrayList<>();
		for (String token : tokens) {
			if (NumberUtils.isInteger(token))
				values.add(NumberUtils.parseInt(token));
		}
		int[] result = new int[values.size()];
		for (int i = 0; i < values.size(); i++)
			result[i] = values.get(i);
		return result;
	}

	public static String getUniqueName(String name0, Collection<String> names) {
		int count = 0;
		String name = name0;
		while (names.contains(name)) {
			name = name0 + "-" + (++count);
		}
		return name;
	}

	public static Collection<String> sortSubsetAsContainingSet(ArrayList<String> containingSet, Collection<String> subsetToSort) {
		final ArrayList<String> sorted = new ArrayList<>();
		for (String item : containingSet) {
			if (subsetToSort.contains(item))
				sorted.add(item);
		}
		return sorted;
	}

	public static boolean hasPositiveLengthValue(Map<?, String> map) {
		for (String value : map.values()) {
			if (value != null && value.length() > 0)
				return true;
		}
		return false;
	}

	/**
	 * concatenate a list of byte[] into a single byte[]
	 */
	public static byte[] concatenate(Collection<byte[]> parts) {
		final int size = parts.stream().mapToInt(p -> p.length).sum();
		final byte[] result = new byte[size];
		int offset = 0;
		for (byte[] part : parts) {
			System.arraycopy(part, 0, result, offset, part.length);
			offset += part.length;
		}
		return result;
	}

	/**
	 * concatenate an array of byte[] into a single byte[]
	 */
	public static byte[] concatenate(byte[]... parts) {
		return concatenate(Arrays.asList(parts));
	}

	public static int copyBytes(String string, byte[] target) {
		final byte[] source = string.getBytes();
		final int len = Math.min(source.length, target.length);
		System.arraycopy(source, 0, target, 0, len);
		return len;
	}

	public static byte[] gzip(String string) throws IOException {
		try (ByteArrayOutputStream outs = new ByteArrayOutputStream(); Writer w = new OutputStreamWriter(new GZIPOutputStream(outs))) {
			w.write(string);
			return outs.toByteArray();
		}
	}

	public static String gunzip(byte[] bytes) throws IOException {
		try (ByteArrayInputStream ins = new ByteArrayInputStream(bytes); BufferedReader r = new BufferedReader(new InputStreamReader(new GZIPInputStream(ins)))) {
			return r.lines().collect(Collectors.joining("\n"));
		}
	}

	public static String computeMD5(String string) {
		try {
			final MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.reset();
			messageDigest.update(string.getBytes(StandardCharsets.UTF_8));
			byte[] resultByte = messageDigest.digest();
			return HexUtils.encodeHexString(resultByte);
		} catch (NoSuchAlgorithmException ignored) {
			return "";
		}
	}

	public static String convertPercentEncoding(String uri) {
		return uri.replace("%20", " ")
				.replace("%5C", "\\")
				.replace("%7E", "~")
				.replace("%2F", "/");
	}

	/**
	 * gets the index of a string s in an array of strings
	 *
	 * @return index or -1
	 */
	public static int getIndexIgnoreCase(String s, String... array) {
		for (var i = 0; i < array.length; i++)
			if (s.equalsIgnoreCase(array[i]))
				return i;
		return -1;
	}

	/**
	 * double backslashes
	 *
	 * @return string with doubled back slashes
	 */
	public static String doubleBackSlashes(String str) {
		var buf = new StringBuilder();
		for (var i = 0; i < str.length(); i++) {
			if (str.charAt(i) == '\\')
				buf.append('\\');
			buf.append(str.charAt(i));
		}
		return buf.toString();
	}

	/**
	 * skip all spaces starting at position i
	 *
	 * @return first position containing a non-space character or str.length()
	 */
	public static int skipSpaces(String str, int i) {
		while (i < str.length() && Character.isSpaceChar(str.charAt(i)))
			i++;
		return i;
	}

	public static String toLowerCaseWithUnderScores(String label) {
		return label.toLowerCase().replaceAll("\s+", "_");
	}

	public static BitSet indicesOf(String[] array, Collection<String> queries) {
		var result = new BitSet();
		for (var i = 0; i < array.length; i++)
			if (queries.contains(array[i]))
				result.set(i);
		return result;
	}

	public static int countDigits(String name) {
		var count = 0;
		for (var i = 0; i < name.length(); i++) {
			if (Character.isDigit(name.charAt(i)))
				count++;
		}
		return count;
	}

	public static String stripSurroundingQuotesIfAny(String s) {
		if (s.length() > 1 && (s.startsWith("\"") && s.endsWith("\"") || s.startsWith("'") && s.endsWith("'")))
			return s.substring(1, s.length() - 1);
		else
			return s;
	}

	public static boolean isHttpOrFileURL(String string) {
		return string.startsWith("http:") || string.startsWith("https:") || string.startsWith("file:");
	}

	public static String[] addOrRemove(String[] array, String label, boolean add) {
		var index = getIndex(label, array);
		if (add && index == -1) {
			var tmp = new String[array.length + 1];
			System.arraycopy(array, 0, tmp, 0, array.length);
			tmp[array.length] = label;
			return tmp;
		} else if (!add && index >= 0) {
			var tmp = new String[array.length - 1];
			System.arraycopy(array, 0, tmp, 0, index);
			var remaining = tmp.length - index;
			if (remaining >= 0)
				System.arraycopy(array, index + 1, tmp, index, remaining);
			return tmp;
		} else
			return array;
	}

	public static String getStringUptoDelimiter(String str, int pos, String delimiters) {
		var endPos = pos;
		while (endPos < str.length() && delimiters.indexOf(str.charAt(endPos)) == -1)
			endPos++;
		return str.substring(pos, endPos);
	}

	/**
	 * replaces all Newick tree format special characters by underscores
	 *
	 * @param label original label
	 * @return new label with no special characters
	 */
	public static String getCleanLabelForNewick(String label) {
		return label == null ? null : (label.isBlank() ? "_" : label.replaceAll("[ \\[\\](),:;]+", "_"));
	}

	public static boolean containsIgnoreCase(String[] array, String query) {
		return Arrays.stream(array).anyMatch(item -> item.equalsIgnoreCase(query));
	}

	public static String[] splitFurther(String[] tokens, String separator) {
		var result = new ArrayList<String>();
		for (var str : tokens) {
			var first = true;
			for (var sub : str.split(separator)) {
				if (first)
					first = false;
				else
					result.add(separator);
				result.add(sub);
			}
		}
		return result.toArray(new String[0]);
	}
}
