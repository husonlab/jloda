/*
 * NexusStreamParser.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.util.parse;

import jloda.util.AColor;
import jloda.util.IOExceptionWithLineNumber;
import jloda.util.NumberUtils;
import jloda.util.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;

/**
 * Parser for NexusBlock files
 * 2003, Daniel Huson
 */
public class NexusStreamParser extends NexusStreamTokenizer implements Closeable {
	private final Reader reader; // keep a reference only so that we can close the reader...

	/**
	 * Construct a new NexusStreamParser object
	 *
	 * @param r the corresponding reader
	 */
	public NexusStreamParser(Reader r) {
		super(r);
		reader = r;
	}

	/**
	 * Match the tokens in the string with the ones obtained from the reader
	 *
	 * @param tokens string of tokens
	 */
	public void matchIgnoreCase(String tokens) throws IOExceptionWithLineNumber {
		try (var s = new NexusStreamTokenizer(new StringReader(tokens))) {
			s.setSquareBracketsSurroundComments(isSquareBracketsSurroundComments());
			while (s.nextToken() != NexusStreamParser.TT_EOF) {
				nextToken();
				if (!toString().equalsIgnoreCase(s.toString())) {
					throw new IOExceptionWithLineNumber("'" + s + "' expected, got: '" + this + "'", lineno());
				}
			}
		} catch (IOException ex) {
			throw new IOExceptionWithLineNumber(lineno(), ex);
		}
	}

	/**
	 * Match the tokens in the string with the one obtained from the reader
	 *
	 * @param tokens string of tokens
	 */
	public void matchRespectCase(String tokens) throws IOExceptionWithLineNumber {
		try (var s = new NexusStreamTokenizer(new StringReader(tokens))) {
			s.setSquareBracketsSurroundComments(isSquareBracketsSurroundComments());
			while (s.nextToken() != NexusStreamParser.TT_EOF) {
				nextToken();
				if (!toString().equals(s.toString())) {
					throw new IOExceptionWithLineNumber(s + "' expected, got: '" + this + "'", lineno());
				}
			}
		} catch (IOException ex) {
			throw new IOExceptionWithLineNumber(lineno(), ex);
		}
	}

	/**
	 * Match the given word in the string with the next one obtained from the
	 * reader
	 *
	 * @param str string containing one word
	 */
	public void matchWordIgnoreCase(String str) throws IOExceptionWithLineNumber {
		try {
			nextToken();
		} catch (IOException ex) {
			throw new IOExceptionWithLineNumber(lineno(), ex);
		}

		if (!toString().equalsIgnoreCase(str)) {
			throw new IOExceptionWithLineNumber(str + "' expected, got: '" + this + "'", lineno());
		}
	}

	/**
	 * Match the given word in the string with the next one obtained from the
	 * reader
	 *
	 * @param str string containing one word
	 */
	public void matchWordRespectCase(String str) throws IOExceptionWithLineNumber {
		try {
			nextToken();
		} catch (IOException ex) {
			throw new IOExceptionWithLineNumber(lineno(), ex);
		}
		if (!toString().equals(str)) {
			throw new IOExceptionWithLineNumber(str + "' expected, got: '" + this + "'", lineno());
		}
	}

	/**
	 * match next token with 'begin NAME;' or 'beginblock NAME;'
	 *
	 * @param blockName name of block
	 */
	public void matchBeginBlock(String blockName) throws IOExceptionWithLineNumber {
		matchAnyTokenIgnoreCase("begin beginBlock");
		matchIgnoreCase(blockName + ";");
	}

	/**
	 * match next token with 'end;' or 'endblock;'
	 */
	public void matchEndBlock() throws IOExceptionWithLineNumber {
		matchAnyTokenIgnoreCase("end endBlock");
		matchRespectCase(";");
	}

	/**
	 * Match the given label in the string with the next one obtained from the
	 * reader
	 *
	 * @param str string containing one word
	 */
	public void matchLabelIgnoreCase(String str) throws IOExceptionWithLineNumber {
		pushPunctuationCharacters(LABEL_PUNCTUATION);
		try {
			try {
				nextToken();
			} catch (IOException ex) {
				throw new IOExceptionWithLineNumber(lineno(), ex);
			}
			if (!toString().equalsIgnoreCase(str)) {
				throw new IOExceptionWithLineNumber(str + "' expected, got: '" + this + "'", lineno());
			}
		} finally {
			popPunctuationCharacters();
		}
	}

	/**
	 * Match the given label in the string with the next one obtained from the
	 * reader
	 *
	 * @param str string containing one word
	 */
	public void matchLabelRespectCase(String str) throws IOExceptionWithLineNumber {
		pushPunctuationCharacters(LABEL_PUNCTUATION);
		try {
			try {
				nextToken();
			} catch (IOException ex) {
				throw new IOExceptionWithLineNumber(lineno(), ex);
			}
			if (!toString().equals(str)) {
				throw new IOExceptionWithLineNumber(str + "' expected, got: '" + this + "'", lineno());
			}
		} finally {
			popPunctuationCharacters();
		}
	}

	/**
	 * Compares the given string of tokens with the next tokens in the
	 * stream
	 *
	 * @param tokens string of tokens to be compared with the tokens in the input stream
	 * @return true, if all tokens match
	 */
	public boolean peekMatchIgnoreCase(String tokens) {
		final var echo = isEchoCommentsWithExclamationMark();
		setEchoCommentsWithExclamationMark(false);

		try (var s = new NexusStreamTokenizer(new StringReader(tokens))) {
			s.setSquareBracketsSurroundComments(isSquareBracketsSurroundComments());

			final var nvals = new ArrayList<Double>();
			final var svals = new ArrayList<String>();
			final var ttypes = new ArrayList<Integer>();
			final var lines = new ArrayList<Integer>();

			svals.add(sval);
			nvals.add(nval);
			ttypes.add(ttype);
			lines.add(lineno());

			var flag = true;
			while (s.nextToken() != NexusStreamParser.TT_EOF) {
				nextToken();
				svals.add(sval);
				nvals.add(nval);
				ttypes.add(ttype);
				lines.add(lineno());

				flag = toString().equalsIgnoreCase(s.toString());
				if (!flag)
					break;
			}
			pushBack(svals, nvals, ttypes, lines);
			nextToken();

			return flag;
		} catch (IOException ex) {
			return false;
		} finally {
			setEchoCommentsWithExclamationMark(echo);
		}
	}

	/**
	 * Compares the given string of tokens with the next tokens in the
	 * stream
	 *
	 * @param tokens string of tokens to be compared with the tokens in the input stream
	 * @return true, if all tokens match
	 */
	public boolean peekMatchRespectCase(String tokens) {
		final boolean echo = isEchoCommentsWithExclamationMark();
		setEchoCommentsWithExclamationMark(false);
		try (var s = new NexusStreamTokenizer(new StringReader(tokens))) {
			s.setSquareBracketsSurroundComments(isSquareBracketsSurroundComments());

			final var nvals = new ArrayList<Double>();
			final var svals = new ArrayList<String>();
			final var ttypes = new ArrayList<Integer>();
			final var lines = new ArrayList<Integer>();

			svals.add(sval);
			nvals.add(nval);
			ttypes.add(ttype);
			lines.add(lineno());

			var flag = true;
			while (s.nextToken() != NexusStreamParser.TT_EOF) {
				nextToken();
				svals.add(sval);
				nvals.add(nval);
				ttypes.add(ttype);
				lines.add(lineno());
				flag = toString().equals(s.toString());
				if (!flag)
					break;
			}
			pushBack(svals, nvals, ttypes, lines);
			nextToken();

			return flag;
		} catch (IOException ex) {
			return false;
		} finally {
			setEchoCommentsWithExclamationMark(echo);
		}
	}

	/**
	 * peeks at the next word
	 *
	 * @return next word
	 */
	public String peekNextWord() {
		final boolean echo = isEchoCommentsWithExclamationMark();
		setEchoCommentsWithExclamationMark(false);
		try {
			final var nvals = new ArrayList<Double>();
			final var svals = new ArrayList<String>();
			final var ttypes = new ArrayList<Integer>();
			final var lines = new ArrayList<Integer>();

			svals.add(sval);
			nvals.add(nval);
			ttypes.add(ttype);
			lines.add(lineno());

			String result = null;
			try {
				nextToken();
				svals.add(sval);
				nvals.add(nval);
				ttypes.add(ttype);
				lines.add(lineno());
				result = toString();
				pushBack(svals, nvals, ttypes, lines);
				nextToken();
			} catch (IOException ignored) {
			}
			return result;
		} finally {
			setEchoCommentsWithExclamationMark(echo);
		}
	}


	/**
	 * do the next tokens match 'begin NAME;'
	 *
	 * @return true, if begin of named block
	 */
	public boolean peekMatchBeginBlock(String blockName) {
		return peekMatchIgnoreCase("begin " + blockName + ";") || peekMatchIgnoreCase("BeginBlock " + blockName + ";");
	}

	/**
	 * do the next tokens match 'END;'
	 *
	 * @return true, if end of block
	 */
	public boolean peekMatchEndBlock() {
		return peekMatchIgnoreCase("end;") || peekMatchIgnoreCase("endblock;");
	}

	/**
	 * Returns a list of strings containing all tokens between
	 * 'first' and 'last' so that the next call of nextToken will return
	 * the token after 'last'
	 *
	 * @param first the current token must match this, or null
	 * @param last  all tokens before this one are returned, or null, to read to the end of the stream
	 * @return a list of strings containing all tokens between 'first'
	 * and 'last'
	 */
	public ArrayList<String> getTokensLowerCase(String first, String last) throws IOExceptionWithLineNumber {
		if (first != null)
			matchIgnoreCase(first);
		final ArrayList<String> list = new ArrayList<>();
		try {
			nextToken();
			while (last == null || !toString().equals(last)) {
				if (ttype == TT_EOF) {
					if (last == null)
						break;
					throw new IOExceptionWithLineNumber("'" + last + "' expected, got EOF", lineno());
				}
				list.add(toString().toLowerCase());
				nextToken();
			}
		} catch (IOException ex) {
			throw new IOExceptionWithLineNumber(lineno(), ex);
		}
		return list;
	}

	/**
	 * Returns a list of strings containing all tokens between
	 * 'first' and 'last' so that the next call of nextToken will return
	 * the token after 'last'
	 *
	 * @param first the current token must match this, or null
	 * @param last  all tokens before this one are returned
	 * @return a list of strings containing all tokens between 'first'
	 * and 'last'
	 */
	public ArrayList<String> getTokensRespectCase(String first, String last) throws IOExceptionWithLineNumber {
		return getTokensRespectCase(first, last, false);
	}

	public ArrayList<String> getTokensRespectCase(String first, String last, boolean surroundWithQuotes) throws IOExceptionWithLineNumber {
		if (first != null)
			matchIgnoreCase(first);
		final var list = new ArrayList<String>();
		try {
			nextToken();
			while (last == null || !toString().equals(last)) {
				if (ttype == TT_EOF) {
					if (last == null)
						break;
					throw new IOExceptionWithLineNumber("'" + last + "' expected, got EOF", lineno());
				}
				if (surroundWithQuotes)
					list.add("'" + toString() + "'");
				else
					list.add(toString());
				nextToken();
			}
		} catch (IOException ex) {
			throw new IOExceptionWithLineNumber(lineno(), ex);
		}
		return list;
	}

	/**
	 * Gets the next token as a word using ';' as punctuation character
	 *
	 * @return the next token as a word
	 */
	public String getWordFileNamePunctuation() throws IOExceptionWithLineNumber {
		pushPunctuationCharacters(SEMICOLON_PUNCTUATION);
		try {
			nextToken();
		} catch (IOException ex) {
			throw new IOExceptionWithLineNumber(lineno(), ex);
		} finally {
			popPunctuationCharacters();
		}

		return toString();
	}

	/**
	 * Gets the next token as an absolute file name. If word is relative file name, prepends current user.dir
	 *
	 * @return the next token as a word
	 */
	public String getAbsoluteFileName() throws IOExceptionWithLineNumber {
		var fileName = getWordFileNamePunctuation();
		if (fileName != null && fileName.length() > 0) {
			File file = new File(fileName);
			if ((file.getParent() == null || file.getParent().length() == 0) && !file.getPath().startsWith("DB:")
				&& !file.getPath().startsWith("WS:") && !file.getPath().startsWith("http") && !file.getPath().contains("::"))
				fileName = (new File(System.getProperty("user.dir"), file.getName())).getPath();
		}
		return fileName;
	}


	/**
	 * Returns a string containing all tokens between
	 * 'first' and 'last' separated by blanks.
	 * The next call of nextToken will return
	 * the token after 'last'
	 *
	 * @param first the current token must match this
	 * @param last  all tokens before this one are returned
	 * @return a string consisting of all tokens found
	 * and 'last'
	 */
	public String getTokensFileNamePunctuation(String first, String last) throws IOExceptionWithLineNumber {
		pushPunctuationCharacters(SEMICOLON_PUNCTUATION);

		var buf = new StringBuilder();
		try {
			if (first != null)
				matchIgnoreCase(first);
			nextToken();
			while (!toString().equals(last)) {
				buf.append(" ").append(this);
				if (ttype == TT_EOF)
					throw new IOExceptionWithLineNumber("'" + last + "' expected, got EOF", lineno());
				nextToken();
			}
		} catch (IOException ex) {
			throw new IOExceptionWithLineNumber(lineno(), ex);
		} finally {
			popPunctuationCharacters();
		}
		if (buf.length() == 0)
			return null;
		return buf.toString();
	}

	/**
	 * Returns a string containing all tokens between
	 * 'first' and 'last' separated by blanks.
	 * The next call of nextToken will return
	 * the token after 'last'.
	 *
	 * @param first the current token must match this
	 * @param last  all tokens before this one are returned
	 * @return a string consisting of all tokens found
	 * and 'last', all in single quotes
	 */
	public String getQuotedTokensRespectCase(String first, String last) throws IOExceptionWithLineNumber {
		final var buf = new StringBuilder();
		if (first != null)
			matchIgnoreCase(first);
		try {
			nextToken();
			while (!toString().equals(last)) {
				buf.append("'").append(this).append("'");
				if (ttype == TT_EOF)
					throw new IOExceptionWithLineNumber("'" + last + "' expected, got EOF", lineno());
				nextToken();
			}
		} catch (IOException ex) {
			throw new IOExceptionWithLineNumber(lineno(), ex);
		}

		if (buf.length() == 0)
			return null;
		return buf.toString();
	}

	/**
	 * Returns a string containing all tokens between
	 * 'first' and 'last' separated by blanks.
	 * The next call of nextToken will return
	 * the token after 'last'
	 *
	 * @param first the current token must match this
	 * @param last  all tokens before this one are returned
	 * @return a string consisting of all tokens found
	 * and 'last'
	 */
	public String getTokensStringLowerCase(String first, String last) throws IOExceptionWithLineNumber {
		matchIgnoreCase(first);
		return getTokensStringLowerCase(last);
	}

	/**
	 * Returns a string containing all tokens before
	 * 'last' separated by blanks.
	 * The next call of nextToken will return
	 * the token after 'last'
	 *
	 * @param last all tokens before this one are returned
	 * @return a string consisting of all tokens found
	 * and 'last'
	 */
	public String getTokensStringLowerCase(String last) throws IOExceptionWithLineNumber {
		var buf = new StringBuilder();
		try {
			nextToken();
			while (!toString().equalsIgnoreCase(last)) {
				buf.append(" ").append(toString().toLowerCase());
				if (ttype == TT_EOF)
					throw new IOExceptionWithLineNumber("'" + last + "' expected, got EOF", lineno());
				nextToken();
			}
		} catch (IOException ex) {
			throw new IOExceptionWithLineNumber(lineno(), ex);
		}
		if (buf.toString().equals(""))
			return null;
		return buf.toString();
	}


	/**
	 * Returns a string containing all tokens before
	 * 'last' separated by blanks.
	 * The next call of nextToken will return
	 * the token after 'last'
	 *
	 * @param last all tokens before this one are returned
	 * @return a string consisting of all tokens found
	 * and 'last'
	 */
	public String getTokensStringRespectCase(String last) throws IOExceptionWithLineNumber {
		var buf = new StringBuilder();
		try {
			nextToken();
			while (!toString().equals(last)) {
				buf.append(" ").append(this);
				if (ttype == TT_EOF)
					throw new IOExceptionWithLineNumber("'" + last + "' expected, got EOF", lineno());
				nextToken();
			}
		} catch (IOException ex) {
			throw new IOExceptionWithLineNumber(lineno(), ex);
		}
		if (buf.length() == 0)
			return null;
		return buf.toString().trim();
	}

	/**
	 * Searches for an occurrence of `token1 token2 token3 ...'
	 * in a list of tokens, returns value, if found and defaultValue, if not.
	 * If tokens are found, then they are removed from tokens
	 *
	 * @param tokens       a list of tokens
	 * @param query        the list of tokens to be found in tokens
	 * @param value        the return value, if first the list of tokens is found
	 * @param defaultValue the value to be returned, if the list of tokens is
	 *                     not found
	 * @return value
	 */
	public boolean findIgnoreCase(List<String> tokens, String query, boolean value, boolean defaultValue) throws IOExceptionWithLineNumber {
		if (tokens.size() == 0)
			return defaultValue;

		var result = defaultValue;
		var found = false;
		try (var s = new NexusStreamParser(new StringReader(List2String(tokens)))) {
			tokens.clear();

			while (s.ttype != NexusStreamParser.TT_EOF) {
				if (!found && s.peekMatchIgnoreCase(query)) {
					result = value;
					found = true;
					s.matchIgnoreCase(query);
				} else {
					if (s.nextToken() != NexusStreamParser.TT_EOF)
						tokens.add(s.toString());
				}
			}
		} catch (IOException ex) {
			throw new IOExceptionWithLineNumber(lineno(), ex);
		}
		return result;
	}

	/**
	 * Searches for an occurrence of "token1 token2 etc [number]", ie a
	 * list of tokens followed by an optional number, returns -1 of only the
	 * tokens are found, 0, if the tokens are not found and n otherwise,
	 * where n is the number read
	 * in a list of tokens, returns value, if found and defaultValue, if not.
	 * If tokens and the number are found, then they are removed from tokens
	 *
	 * @param tokens       a list of tokens
	 * @param query        the list of tokens to be found in tokens
	 * @param defaultValue the value to be returned, if the list of tokens is
	 *                     not found
	 * @return value
	 */
	public float findIgnoreCase(List<String> tokens, String query, float defaultValue) throws IOExceptionWithLineNumber {
		if (tokens.size() == 0)
			return defaultValue;

		var result = defaultValue;
		var found = false;
		try (final var s = new NexusStreamParser(new StringReader(List2String(tokens)))) {
			tokens.clear();

			while (s.ttype != NexusStreamParser.TT_EOF) {
				if (!found && s.peekMatchIgnoreCase(query)) {
					found = true;
					s.matchIgnoreCase(query);

					String str = s.getWordRespectCase();
					try {
						result = Float.parseFloat(str);
					} catch (NumberFormatException ex) {
						throw new IOExceptionWithLineNumber("Number expected, got: '" + str + "'", lineno());
					}
				} else // copy unused tokens back to token list
				{
					if (s.nextToken() != NexusStreamParser.TT_EOF)
						tokens.add(s.toString());
				}
			}
		} catch (IOException ex) {
			throw new IOExceptionWithLineNumber(lineno(), ex);
		}
		return result;
	}

	/**
	 * Searches for an occurrence of `token leftDelimiter value value ... rightDelimiter',
	 * where each value is a word.
	 * Returns a string containing all values, or
	 * a default value, if token does not occur
	 *
	 * @param tokens         the list of tokens
	 * @param token          the token to look for
	 * @param leftDelimiter  the left delimiter
	 * @param rightDelimiter the left delimiter
	 * @param defaultValue   the return value, if token not found
	 * @return the value
	 */
	public String findIgnoreCase(List<String> tokens, String token, String leftDelimiter, String rightDelimiter, String defaultValue) throws IOExceptionWithLineNumber {
		if (tokens.size() == 0)
			return defaultValue;

		var found = false;
		// The following line seems to be a bug - have replaced it
		//String result = defaultValue;
		var buf = new StringBuilder();
		try (var s = new NexusStreamParser(new StringReader(List2String(tokens)))) {
			tokens.clear();

			while (s.ttype != NexusStreamParser.TT_EOF) {
				if (!found && s.peekMatchIgnoreCase(token)) {
					s.matchIgnoreCase(token + leftDelimiter);
					while (true) {
						s.nextToken();
						String word = s.toString();
						found = true;

						if (word.equalsIgnoreCase(rightDelimiter))
							break;
						if (!buf.toString().equals(""))
							buf.append(" ");
						buf.append(word);
					}
				} else {
					if (s.nextToken() != NexusStreamParser.TT_EOF)
						tokens.add(s.toString());
				}
			}
		} catch (IOException ex) {
			throw new IOExceptionWithLineNumber(lineno(), ex);
		}
		if (buf.length() == 0)
			return defaultValue;
		else
			return buf.toString();
	}

	/**
	 * Searches for an occurrence of `token value', where value is a
	 * string occurring in legalValues, returning value, if found, or
	 * a default value, if token does not occur
	 *
	 * @param tokens       the list of tokens
	 * @param token        the token to look for
	 * @param legalValues  if not null, string containing all legal values of the token
	 * @param defaultValue the return value, if token not found
	 * @return the value
	 */
	public String findIgnoreCase(List<String> tokens, String token, final String legalValues, String defaultValue) throws IOExceptionWithLineNumber {
		if (tokens.size() == 0)
			return defaultValue;

		var found = false;
		var result = defaultValue;
		try (var s = new NexusStreamParser(new StringReader(List2String(tokens)))) {
			tokens.clear();

			while (s.ttype != NexusStreamParser.TT_EOF) {
				if (!found && s.peekMatchIgnoreCase(token)) {
					s.matchIgnoreCase(token);
					s.nextToken();
					result = s.toString();
					found = true;
					if (legalValues != null) {
						var legalTokens = legalValues.split("\\s+");
						var which = StringUtils.getIndexIgnoreCase(result, legalTokens);
						if (which == -1)
							throw new IOExceptionWithLineNumber(token + " '" + result + "': illegal value", lineno());
						else
							result = legalTokens[which];
					}
				} else {
					if (s.nextToken() != NexusStreamParser.TT_EOF)
						tokens.add(s.toString());
				}
			}
		} catch (IOException ex) {
			throw new IOExceptionWithLineNumber(lineno(), ex);
		}
		return result;
	}

	/**
	 * Searches for an occurrence of `token value', where value is a
	 * character occurring in legalValues, returning value, if found, or
	 * a default value, if token does not occur
	 *
	 * @param tokens       the list of tokens
	 * @param token        the token to look for
	 * @param legalValues  if not null, string containing all legal values of the character
	 * @param defaultValue the return value, if token not found
	 * @return the value
	 */
	public char findIgnoreCase(List<String> tokens, String token, String legalValues, char defaultValue) throws IOExceptionWithLineNumber {
		if (tokens.size() == 0)
			return defaultValue;

		var found = false;
		var result = defaultValue;
		try (var s = new NexusStreamParser(new StringReader(List2String(tokens)))) {
			tokens.clear();

			while (s.ttype != NexusStreamParser.TT_EOF) {
				if (!found && s.peekMatchIgnoreCase(token)) {
					s.matchIgnoreCase(token);
					s.nextToken();
					String str = s.toString();
					if (str.length() > 1)
						throw new IOExceptionWithLineNumber(token + " '" + result + "': char expected", lineno());
					result = str.charAt(0);
					found = true;
					if (legalValues != null) {
						int pos = legalValues.toLowerCase().indexOf(Character.toLowerCase(result));
						if (pos == -1)
							throw new IOExceptionWithLineNumber(token + " '" + result + "': illegal value", lineno());
						else
							result = legalValues.charAt(pos);

					}
				} else {
					if (s.nextToken() != NexusStreamParser.TT_EOF)
						tokens.add(s.toString());
				}
			}
		} catch (IOException ex) {
			throw new IOExceptionWithLineNumber(lineno(), ex);
		}
		return result;
	}

	/**
	 * Searches for an occurrence of a token=value, where value is a double
	 * between minValue and maxValue
	 *
	 * @param tokens       the list of tokens
	 * @param token        the token to look for
	 * @param minValue     the minimal value
	 * @param maxValue     the maximal value
	 * @param defaultValue the return value, if token not found
	 * @return the value
	 */
	public double findIgnoreCase(List<String> tokens, String token, double minValue, double maxValue, double defaultValue) throws IOExceptionWithLineNumber {
		if (tokens.size() == 0)
			return defaultValue;

		var found = false;
		var result = defaultValue;
		try (var s = new NexusStreamParser(new StringReader(List2String(tokens)))) {
			tokens.clear();

			while (s.ttype != NexusStreamParser.TT_EOF) {
				if (!found && s.peekMatchIgnoreCase(token)) {
					s.matchIgnoreCase(token);
					s.nextToken();
					try {
						result = Double.parseDouble(s.sval);
					} catch (Exception e) {
						throw new IOExceptionWithLineNumber(token + " '" + result + "': number expected", lineno());
					}
					if (result < minValue || result > maxValue)
						throw new IOExceptionWithLineNumber(token + " '" + result + "': out of range: "
															+ minValue + " - " + maxValue, lineno());
					found = true;
				} else {
					if (s.nextToken() != NexusStreamParser.TT_EOF)
						tokens.add(s.toString());
				}
			}
		} catch (IOException ex) {
			throw new IOExceptionWithLineNumber(lineno(), ex);
		}
		return result;
	}


	/**
	 * Searches for an occurrence of a token=value, where value is a double
	 * between minValue and maxValue
	 *
	 * @param tokens       the list of tokens
	 * @param token        the token to look for
	 * @param minValue     the minimal value
	 * @param maxValue     the maximal value
	 * @param defaultValue the return value, if token not found
	 * @return the value
	 */
	public int findIgnoreCase(List<String> tokens, String token, int minValue, int maxValue, int defaultValue) throws IOExceptionWithLineNumber {
		if (tokens.size() == 0)
			return defaultValue;

		var found = false;
		var result = defaultValue;
		try (var s = new NexusStreamParser(new StringReader(List2String(tokens)))) {
			tokens.clear();

			while (s.ttype != NexusStreamParser.TT_EOF) {
				if (!found && s.peekMatchIgnoreCase(token)) {
					s.matchIgnoreCase(token);
					s.nextToken();
					try {
						result = Integer.parseInt(s.sval);
					} catch (Exception e) {
						throw new IOExceptionWithLineNumber(token + " '" + result + "': number expected", lineno());
					}
					if (result < minValue || result > maxValue)
						throw new IOExceptionWithLineNumber(token + " '" + result + "': out of range: " + minValue + " - " + maxValue, lineno());
					found = true;
				} else {
					if (s.nextToken() != NexusStreamParser.TT_EOF)
						tokens.add(s.toString());
				}
			}
		} catch (IOException ex) {
			throw new IOExceptionWithLineNumber(lineno(), ex);
		}
		return result;
	}

	/**
	 * Searches for an occurrence of a token value, where value is a color
	 *
	 * @param tokens       the list of tokens
	 * @param token        the token to look for
	 * @param defaultValue the return value, if token not found
	 * @return the value
	 */
	public AColor findIgnoreCase(List<String> tokens, String token, AColor defaultValue) throws IOException {
		if (tokens.size() == 0)
			return defaultValue;

		var found = false;
		var result = defaultValue;
		try (var s = new NexusStreamParser(new StringReader(List2String(tokens)))) {
			tokens.clear();

			while (s.ttype != NexusStreamParser.TT_EOF) {
				if (!found && s.peekMatchIgnoreCase(token)) {
					s.matchIgnoreCase(token);
					try {
						result = s.getColor();
						found = true;
					} catch (Exception e) {
						throw new IOException("Line " + lineno() + ": " + token + ": color or null expected");
					}
				} else {
					if (s.nextToken() != NexusStreamParser.TT_EOF)
						tokens.add(s.toString());
				}
			}
		}
		return result;
	}

	/**
	 * Determines whether a given token occurs anywhere in a list of tokens
	 *
	 * @param tokens list of tokens
	 * @param token  the token to find
	 * @return true, if token contained in values
	 */
	public boolean findIgnoreCase(List<String> tokens, String token) throws IOExceptionWithLineNumber {
		if (tokens.size() == 0)
			return false;

		var result = false;
		try (var s = new NexusStreamParser(new StringReader(List2String(tokens)))) {
			tokens.clear();

			while (s.ttype != NexusStreamParser.TT_EOF) {
				if (s.peekMatchIgnoreCase(token)) {
					result = true;
					s.matchIgnoreCase(token);
				} else {
					if (s.nextToken() != NexusStreamParser.TT_EOF)
						tokens.add(s.toString());
				}
			}
		} catch (IOException ex) {
			throw new IOExceptionWithLineNumber(lineno(), ex);
		}
		return result;
	}

	/**
	 * Determines whether a given token occurs anywhere in a string
	 * containing tokens
	 *
	 * @param vals  string of tokens
	 * @param token the token to find
	 * @return true, if token contained in values
	 */
	public boolean findIgnoreCase(String vals, String token) throws IOExceptionWithLineNumber {
		try (var s = new NexusStreamParser(new StringReader(vals))) {
			while (s.ttype != NexusStreamParser.TT_EOF) {
				if (s.peekMatchIgnoreCase(token))
					return true;
				s.nextToken();
			}
		} catch (IOException ex) {
			throw new IOExceptionWithLineNumber(lineno(), ex);
		}
		return false;
	}

	/**
	 * check that find has exhausted all tokens
	 */
	public void checkFindDone(List<String> tokens) throws IOExceptionWithLineNumber {
		if (tokens.size() != 0)
			throw new IOExceptionWithLineNumber("unexpected tokens: " + tokens, lineno());
	}

	/**
	 * Get an integer from the reader
	 *
	 * @return integer read
	 */
	public int getInt() throws IOExceptionWithLineNumber {
		pushPunctuationCharacters(NEGATIVE_INTEGER_PUNCTUATION);
		try {
			nextToken();
			nval = Integer.parseInt(sval);
		} catch (Exception ex) {
			popPunctuationCharacters();
			throw new IOExceptionWithLineNumber("INTEGER expected, got: '" + sval + "'", lineno());
		}
		popPunctuationCharacters();
		return (int) nval;
	}

	/**
	 * Get an integer from the reader
	 *
	 * @param low  smallest legal value
	 * @param high highest legal value
	 * @return integer read
	 */
	public int getInt(int low, int high) throws IOExceptionWithLineNumber {
		var result = getInt();

		if (result < low || result > high) {
			if (low > Integer.MIN_VALUE && high == Integer.MAX_VALUE)
				throw new IOExceptionWithLineNumber("value " + result + " smaller than minimum: " + low, lineno());
			else if (low == Integer.MIN_VALUE)
				throw new IOExceptionWithLineNumber("value " + result + " larger than maximum: " + high, lineno());
			else
				throw new IOExceptionWithLineNumber("value " + result + " out of range: " + low + " - " + high, lineno());
		}
		return result;
	}

	/**
	 * Get a long from the reader
	 *
	 * @return integer read
	 */
	public long getLong() throws IOExceptionWithLineNumber {
		pushPunctuationCharacters(NEGATIVE_INTEGER_PUNCTUATION);
		try {
			nextToken();
			nval = Long.parseLong(sval);
		} catch (Exception ex) {
			popPunctuationCharacters();
			throw new IOExceptionWithLineNumber("LONG expected, got: '" + sval + "'", lineno());
		}
		popPunctuationCharacters();
		return (long) nval;
	}

	/**
	 * Get a double from the reader
	 *
	 * @return double read
	 */
	public double getDouble() throws IOExceptionWithLineNumber {
		pushPunctuationCharacters(LABEL_PUNCTUATION);
		try {
			nextToken();
			nval = Double.parseDouble(sval);
		} catch (Exception ex) {
			popPunctuationCharacters();
			throw new IOExceptionWithLineNumber("DOUBLE expected, got: '" + sval + "'", lineno());
		}
		popPunctuationCharacters();
		return nval;
		/* The code below doesn't work when number contains E-4 etc: */
        /*
        setParseNumbers(true);
        if(nextToken()!=TT_NUMBER)
        {
            setParseNumbers(false);
            throw new IOExceptionWithLineNumber(lineno(),"DOUBLE expected, got: '"+toString()+"'");
        }
        setParseNumbers(false);
        return nval;
        */
	}

	/**
	 * Get an integer from the reader
	 *
	 * @param low  smallest legal value
	 * @param high highest legal value
	 * @return integer read
	 */
	public double getDouble(double low, double high) throws IOExceptionWithLineNumber {
		var result = getDouble();

		if (result < low || result > high) {
			if (low > Double.NEGATIVE_INFINITY && high == Double.POSITIVE_INFINITY)
				throw new IOExceptionWithLineNumber("value " + result + " smaller than minimum: " + low, lineno());
			else if (low == Double.NEGATIVE_INFINITY && high < Double.POSITIVE_INFINITY)
				throw new IOExceptionWithLineNumber("value " + result + " larger than maximum: " + high, lineno());
			else
				throw new IOExceptionWithLineNumber("value " + result + " out of range: " + low + " - " + high, lineno());
		}
		return result;
	}


	/**
	 * Get a boolean from the reader
	 *
	 * @return boolean read
	 */
	public boolean getBoolean() throws IOExceptionWithLineNumber {
		pushPunctuationCharacters(LABEL_PUNCTUATION);
		boolean value;
		try {
			nextToken();
			if (sval.equalsIgnoreCase("true"))
				value = true;
			else if (sval.equalsIgnoreCase("false"))
				value = false;
			else
				throw new IOExceptionWithLineNumber("Not a boolean: " + sval, lineno());
		} catch (Exception ex) {
			popPunctuationCharacters();
			throw new IOExceptionWithLineNumber("Boolean expected, got: '" + sval + "'", lineno());
		}
		popPunctuationCharacters();
		return value;
	}

	/**
	 * Get a word from the reader
	 *
	 * @return word read
	 */
	public String getWordRespectCase() throws IOExceptionWithLineNumber {
		try {
			nextToken();
		} catch (IOException ex) {
			throw new IOExceptionWithLineNumber(lineno(), ex);
		}
		return toString();
	}

	/**
	 * gets a taxon or label
	 *
	 * @return a label
	 */
	public String getLabelRespectCase() throws IOExceptionWithLineNumber {
		pushPunctuationCharacters(LABEL_PUNCTUATION);
		String result;
		try {
			result = getWordRespectCase();
		} finally {
			popPunctuationCharacters();
		}
		return result;
	}

	/**
	 * Convert a list of tokens to a string
	 *
	 * @param tokens list of tokens
	 * @return string representation of list of tokens
	 */
	static String List2String(List<String> tokens) {
		var buf = new StringBuilder();

		var it = tokens.listIterator();

		boolean first = true;
		while (it.hasNext()) {
			if (first) {
				buf.append("'").append(it.next()).append("'");
				first = false;
			} else
				buf.append(" '").append(it.next()).append("'");
		}
		return buf.toString();
	}

	/**
	 * Convert a abbreviated block into a full block.
	 * For example, convert "assume disttransform=NJ;"
	 * into "begin st_assumptions; distransform=NJ;end;"
	 * Uses only ';' as punctuation character
	 *
	 * @param firstSourceLabel the first source label such as "assume"
	 * @param lastSourceLabel  the last source label such as ";"
	 * @param blockName        the name of the block
	 * @return the full block
	 */
	public String convertToBlock(String firstSourceLabel, String lastSourceLabel, String blockName) throws IOException {
		pushPunctuationCharacters(SEMICOLON_PUNCTUATION);
		var buf = new StringBuilder("begin " + blockName + ";");
		try {
			var tokens = getTokensRespectCase(firstSourceLabel, lastSourceLabel);
			for (var token : tokens) buf.append(" ").append(token);
			buf.append(";end;");
		} finally {
			popPunctuationCharacters();
		}
		return buf.toString();
	}

	/**
	 * Peeks at the next token and attempts to match it to any of the tokens
	 * present in str
	 *
	 * @param tokens a string of tokens
	 */
	public boolean peekMatchAnyTokenIgnoreCase(String tokens) {
		var echo = isEchoCommentsWithExclamationMark();
		setEchoCommentsWithExclamationMark(false);
		try (var s = new NexusStreamTokenizer(new StringReader(tokens))) {
			s.setSquareBracketsSurroundComments(isSquareBracketsSurroundComments());

			while (s.nextToken() != NexusStreamParser.TT_EOF) {
				if (peekMatchIgnoreCase(s.toString()))
					return true;
			}
		} catch (IOException ex) {
			jloda.util.Basic.caught(ex);
		} finally {
			setEchoCommentsWithExclamationMark(echo);

		}
		return false;
	}

	/**
	 * Attempts to match the next token to any of the tokens in the given string
	 *
	 * @param tokens a string of tokens
	 */
	public void matchAnyTokenIgnoreCase(String tokens) throws IOExceptionWithLineNumber {
		try (var s = new NexusStreamTokenizer(new StringReader(tokens))) {
			s.setSquareBracketsSurroundComments(isSquareBracketsSurroundComments());

			while (s.nextToken() != NexusStreamParser.TT_EOF) {
				if (peekMatchIgnoreCase(s.toString())) {
					matchIgnoreCase(s.toString());
					return;
				}
			}
		} catch (IOException ex) {
			jloda.util.Basic.caught(ex);
		}
		throw new IOExceptionWithLineNumber("any of '" + tokens.toLowerCase() + "' expected", lineno());
	}


	/**
	 * Peeks at the next token and attempts to match it to any of the tokens
	 * present in str
	 *
	 * @param tokens a string of tokens
	 */
	public boolean peekMatchAnyTokenRespectCase(String tokens) {
		final var echo = isEchoCommentsWithExclamationMark();
		setEchoCommentsWithExclamationMark(false);

		try (var s = new NexusStreamTokenizer(new StringReader(tokens))) {
			s.setSquareBracketsSurroundComments(isSquareBracketsSurroundComments());

			while (s.nextToken() != NexusStreamParser.TT_EOF) {
				if (peekMatchRespectCase(s.toString()))
					return true;
			}
		} catch (IOException ex) {
			jloda.util.Basic.caught(ex);
		} finally {
			setEchoCommentsWithExclamationMark(echo);
		}
		return false;
	}


	/**
	 * returns all words between first and last using ';' as punctuation character
	 *
	 * @param first first word or null
	 * @param last  last word
	 * @return all words between first and last token
	 */
	public List<String> getWordsRespectCase(String first, String last) throws IOExceptionWithLineNumber {
		pushPunctuationCharacters(SEMICOLON_PUNCTUATION);

		final var list = new ArrayList<String>();
		try {
			if (first != null)
				matchIgnoreCase(first);
			nextToken();
			while (!toString().equals(last)) {
				list.add(toString());
				if (ttype == TT_EOF)
					throw new IOExceptionWithLineNumber("'" + last + "' expected, got EOF", lineno());
				nextToken();
			}
		} catch (IOException ex) {
			throw new IOExceptionWithLineNumber(lineno(), ex);
		} finally {
			popPunctuationCharacters();
		}
		return list;
	}

	/**
	 * returns the next n words
	 *
	 * @param n number of words
	 * @return next n words
	 */
	public List<String> getWordsRespectCase(int n) throws IOExceptionWithLineNumber {
		pushPunctuationCharacters(SEMICOLON_PUNCTUATION);
		var list = new ArrayList<String>(n);

		try {
			for (var i = 0; i < n; i++)
				list.add(getWordRespectCase());

		} catch (IOExceptionWithLineNumber ex) {
			popPunctuationCharacters();
			throw ex;
		}
		popPunctuationCharacters();
		return list;
	}

	/**
	 * returns the list of all positive integers found between first and last token.
	 * Integers are separated by commas. A range of positive integers is specified as i - j
	 *
	 * @return all integers
	 */
	public List<Integer> getIntegerList(String firstToken, String lastToken) throws IOExceptionWithLineNumber {
		List<String> tokens;
		try {
			pushPunctuationCharacters(STRICT_PUNCTUATION);
			tokens = getTokensLowerCase(firstToken, lastToken);
		} finally {
			popPunctuationCharacters();
		}

		var result = new ArrayList<Integer>();
		var seen = new BitSet();

		var inState = 0; // 0: expecting first number, 1: expecting new number or -2: expecting second number
		var firstNumber = 0;
		int secondNumber;
		final var it = tokens.listIterator();
		while (it.hasNext()) {
			var label = it.next();
			if (label.equalsIgnoreCase("none")) {
				if (!it.hasNext())
					throw new IOExceptionWithLineNumber("unexpected: " + label, lineno());
				return result; // return empty list
			}

			switch (inState) {
				case 1:      // expecting number or -
					if (label.equals("-")) {
						inState = 2;
						break; // end of case 1
					}
					if (!seen.get(firstNumber)) {
						result.add(firstNumber);
						seen.set(firstNumber);
					}
					// fall through to case 0:
				case 0: // expecting first number
					try {
						firstNumber = Integer.parseInt(label);

					} catch (Exception ex) {
						throw new IOExceptionWithLineNumber("number expected: " + label, lineno());
					}
					inState = 1;
					break;
				case 2: // expecting second number
					try {
						secondNumber = Integer.parseInt(label);
					} catch (Exception ex) {
						throw new IOExceptionWithLineNumber("number expected: " + label, lineno());
					}

					var imin = Math.min(firstNumber, secondNumber);
					var imax = Math.max(firstNumber, secondNumber);
					for (var i = imin; i <= imax; i++) {
						if (!seen.get(i)) {
							result.add(i);
							seen.set(i);
						}
					}
					inState = 0;
					break;
				default:
					break;
			}
		}
		switch (inState) {
			case 1:
				if (!seen.get(firstNumber)) {
					result.add(firstNumber);
					seen.set(firstNumber);
				}
				break;
			case 2:
				throw new IOExceptionWithLineNumber("second number expected", lineno());
			default:
				break;

		}
		return result;
	}

	/**
	 * get the line number mentioned in an exception or 0
	 *
	 * @param ex exception
	 * @return line number or 0
	 */
	static public int getLineNumber(Exception ex) {
		try (var np = new NexusStreamParser(new StringReader(ex.toString()))) {
			while (np.peekNextToken() != NexusStreamParser.TT_EOF
				   && !np.peekMatchIgnoreCase("line"))
				np.getWordRespectCase();
			np.getWordRespectCase();
			return np.getInt();
		} catch (Exception ignored) {
		}
		return 0;
	}

	/**
	 * gets the legal token matched by next word in stream
	 *
	 * @return matched token
	 */
	public String getWordMatchesIgnoringCase(String legalTokens) throws IOExceptionWithLineNumber {
		final var word = getWordRespectCase();
		try (var np = new NexusStreamParser(new StringReader(legalTokens))) {
			while (np.peekNextToken() != NexusStreamParser.TT_EOF) {
				if (np.peekMatchIgnoreCase(word))
					return np.getWordRespectCase();
				else
					np.getWordRespectCase();
			}
		} catch (IOException ex) {
			throw new IOExceptionWithLineNumber(lineno(), ex);
		}
		throw new IOExceptionWithLineNumber("input '" + word + "' does not match any of legal tokens: " + legalTokens, lineno());
	}

	/**
	 * gets the legal token matched by next word in stream
	 *
	 * @return matched token
	 */
	public String getWordMatchesRespectingCase(String legalTokens) throws IOExceptionWithLineNumber {
		final var word = getWordRespectCase();
		try (var np = new NexusStreamParser(new StringReader(legalTokens))) {
			while (np.peekNextToken() != NexusStreamParser.TT_EOF) {
				if (np.peekMatchRespectCase(word))
					return np.getWordRespectCase();
				else
					np.getWordRespectCase();
			}
		} catch (IOException ex) {
			throw new IOExceptionWithLineNumber(lineno(), ex);
		}
		throw new IOExceptionWithLineNumber("input '" + word + "' does not match any of legal tokens: " + legalTokens, lineno());
	}

	/**
	 * gets the legal token matched by next word in stream
	 *
	 * @return matched token
	 */
	public String getWordMatchesRespectingCase(Collection<String> legalTokens) throws IOExceptionWithLineNumber {
		final var word = getWordRespectCase();
		for (var legalToken : legalTokens)
			if (word.equals(legalToken))
				return legalToken;
		throw new IOExceptionWithLineNumber("input '" + word + "' does not match any of legal tokens: " + StringUtils.toString(legalTokens, " "), lineno());
	}


	/**
	 * gets the legal token matched by next word in stream
	 *
	 * @return matched token
	 */
	public String getWordMatchesRespectingCase(String[] legalTokens) throws IOExceptionWithLineNumber {
		final var word = getWordRespectCase();
		for (var legalToken : legalTokens)
			if (word.equals(legalToken))
				return legalToken;
		throw new IOExceptionWithLineNumber("input '" + word + "' does not match any of legal tokens: " + StringUtils.toString(legalTokens, " "), lineno());
	}

	/**
	 * gets the legal token matched by next word in stream
	 *
	 * @return matched token
	 */
	public String getWordMatchesIgnoringCase(Collection<String> legalTokens) throws IOExceptionWithLineNumber {
		final var word = getWordRespectCase();
		for (var legalToken : legalTokens)
			if (word.equalsIgnoreCase(legalToken))
				return legalToken;
		throw new IOExceptionWithLineNumber("input '" + word + "' does not match any of legal tokens: " + StringUtils.toString(legalTokens, ", "), lineno());
	}

	/**
	 * gets the legal token matched by next word in stream
	 *
	 * @return matched token
	 */
	public String getWordMatchesIgnoringCase(String[] legalTokens) throws IOExceptionWithLineNumber {
		final var word = getWordRespectCase();
		for (var legalToken : legalTokens)
			if (word.equalsIgnoreCase(legalToken))
				return legalToken;
		throw new IOExceptionWithLineNumber("input '" + word + "' does not match any of legal tokens: " + StringUtils.toString(legalTokens, ", "), lineno());
	}


	/**
	 * get a color, either from a name or from r g b (a) or from #rrggbb or #rrggbbaa
	 *
	 * @return color
	 */
	public AColor getColor() throws IOException {

		try {
			int r = 0, g = 0, b = 0, a = 0;
			for (var i = 0; i < 4; i++) {
				var word = getWordRespectCase();
				switch (i) {
					case 0 -> {
						if (word.equals("null"))
							return null;
						if (word.startsWith("#")) // format #rrggbb
						{
							if (word.startsWith("#")) {
								word = word.substring(1);
								if (word.length() == 6) {
									r = Integer.parseInt(word.substring(0, 2), 16);
									g = Integer.parseInt(word.substring(2, 4), 16);
									b = Integer.parseInt(word.substring(4, 6), 16);
									return new AColor(r, g, b);
								} else if (word.length() == 8) {
									r = Integer.parseInt(word.substring(0, 2), 16);
									g = Integer.parseInt(word.substring(2, 4), 16);
									b = Integer.parseInt(word.substring(4, 6), 16);
									a = Integer.parseInt(word.substring(6, 8), 16);
									return new AColor(r, g, b, a);
								}
							}
						}
						if (isHexInt(word)) {
							var value = parseHexInt(word);
							r = (value >> 16) & 0xFF;
							g = (value >> 8) & 0xFF;
							b = (value) & 0xFF;
							a = (value >> 24) & 0xff;
							return new AColor(r, g, b, a);
						} else if (NumberUtils.isInteger(word)) {
							r = Integer.parseInt(word);
						} else {
							return AColor.parseColor(word);
						}
					}
					case 1 -> g = Integer.parseInt(word);
					case 2 -> {
						b = Integer.parseInt(word);
						if (!NumberUtils.isInteger(peekNextWord())) {
							return new AColor(r, g, b);
						}
					}
					case 3 -> a = Integer.parseInt(word);
				}
			}
			return new AColor(r, g, b, a);
		} catch (Exception ex) {
			throw new IOException("line " + lineno() + ": color expected, either X11-name or value (c, r g b, or r g b a)");
		}
	}

	public static boolean isHexInt(String value) {
		try {
			if (value.startsWith("0x"))
				Integer.parseInt(value.substring(2), 16);
			else
				return false;
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	public int parseHexInt(String value) {
		if (isHexInt(value))
			return Integer.parseInt(value.substring(2), 16);
		else
			throw new NumberFormatException("Not hex: " + value);
	}


	public void close() throws IOException {
		reader.close();
	}

	/**
	 * attempts to skip a block. If successful, returns the name of the block
	 *
	 * @return name of block skipped or null
	 */
	public String skipBlock() throws IOExceptionWithLineNumber {
		final var echo = isEchoCommentsWithExclamationMark();
		setEchoCommentsWithExclamationMark(false);
		String blockName = null;
		try {
			while (!peekMatchIgnoreCase("end;")) {
				if (blockName == null && peekMatchIgnoreCase("begin")) {
					matchIgnoreCase("begin");
					blockName = getWordRespectCase();
					matchIgnoreCase(";");
				}
				nextToken();
			}
			if (peekMatchIgnoreCase("end;"))
				matchIgnoreCase("end;");
		} catch (IOException ex) {
			throw new IOExceptionWithLineNumber(lineno(), ex);
		} finally {
			setEchoCommentsWithExclamationMark(echo);
		}
		return blockName;
	}

	public boolean peekInteger() {
		return NumberUtils.isInteger(peekNextWord());
	}

	public static String getQuotedString(NexusStreamParser np) throws IOException {
		np.matchIgnoreCase("\"");
		var words = new ArrayList<String>();
		while (!np.peekMatchIgnoreCase("\""))
			words.add(np.getWordRespectCase());
		np.matchIgnoreCase("\"");
		return StringUtils.toString(words, " ");
	}

	public boolean isAtBeginOfBlock(String blockName) {
		return peekMatchIgnoreCase("begin " + blockName + ";") || peekMatchIgnoreCase("BeginBlock " + blockName + ";");
	}
}

// EOF
