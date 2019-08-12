/*
 * NexusStreamParser.java Copyright (C) 2019. Daniel H. Huson
 *
 *  (Some files contain contributions from other authors, who are then mentioned separately.)
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
 */

package jloda.util.parse;

import jloda.swing.util.Colors;
import jloda.util.Basic;
import jloda.util.IOExceptionWithLineNumber;

import java.awt.*;
import java.io.*;
import java.util.List;
import java.util.*;

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
     * Match the tokens in the string with the one obtained from the reader
     *
     * @param str string of tokens
     */
    public void matchIgnoreCase(String str) throws IOExceptionWithLineNumber {
        NexusStreamTokenizer sst = new NexusStreamTokenizer(new StringReader(str));
        sst.setSquareBracketsSurroundComments(isSquareBracketsSurroundComments());
        try {
            while (sst.nextToken() != NexusStreamParser.TT_EOF) {
                nextToken();
                if (!toString().equalsIgnoreCase(sst.toString())) {
                    throw new IOExceptionWithLineNumber("'" + sst.toString() + "' expected, got: '" + toString() + "'", lineno());
                }
            }
        } catch (IOException ex) {
            throw new IOExceptionWithLineNumber(lineno(), ex);
        }
    }

    /**
     * Match the tokens in the string with the one obtained from the reader
     *
     * @param str string of tokens
     */
    public void matchRespectCase(String str) throws IOExceptionWithLineNumber {
        NexusStreamTokenizer sst = new NexusStreamTokenizer(new StringReader(str));
        sst.setSquareBracketsSurroundComments(isSquareBracketsSurroundComments());

        try {
            while (sst.nextToken() != NexusStreamParser.TT_EOF) {
                nextToken();
                if (!toString().equals(sst.toString())) {
                    throw new IOExceptionWithLineNumber(sst.toString() + "' expected, got: '" + toString() + "'", lineno());
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
            throw new IOExceptionWithLineNumber(str + "' expected, got: '" + toString() + "'", lineno());
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
            throw new IOExceptionWithLineNumber(str + "' expected, got: '" + toString() + "'", lineno());
        }
    }

    /**
     * match next token with 'begin NAME;' or 'beginblock NAME;'
     *
     * @param blockName name of block
     * @throws IOExceptionWithLineNumber
     */
    public void matchBeginBlock(String blockName) throws IOExceptionWithLineNumber {
        matchAnyTokenIgnoreCase("begin beginBlock");
        matchIgnoreCase(blockName + ";");
    }

    /**
     * match next token with 'end;' or 'endblock;'
     *
     * @throws IOExceptionWithLineNumber
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
                throw new IOExceptionWithLineNumber(str + "' expected, got: '" + toString() + "'", lineno());
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
                throw new IOExceptionWithLineNumber(str + "' expected, got: '" + toString() + "'", lineno());
            }
        } finally {
            popPunctuationCharacters();
        }
    }

    /**
     * Compares the given string of tokens with the next tokens in the
     * stream
     *
     * @param s string of tokens to be compared with the tokens in the input stream
     * @return true, if all tokens match
     */
    public boolean peekMatchIgnoreCase(String s) {
        final boolean echo = isEchoCommentsWithExclamationMark();
        setEchoCommentsWithExclamationMark(false);

        try {
            final NexusStreamTokenizer sst = new NexusStreamTokenizer(new StringReader(s));
            sst.setSquareBracketsSurroundComments(isSquareBracketsSurroundComments());

            final ArrayList<Double> nvals = new ArrayList<>();
            final ArrayList<String> svals = new ArrayList<>();
            final ArrayList<Integer> ttypes = new ArrayList<>();
            final ArrayList<Integer> lines = new ArrayList<>();

            svals.add(sval);
            nvals.add(nval);
            ttypes.add(ttype);
            lines.add(lineno());

            boolean flag = true;
            try {
                while (sst.nextToken() != NexusStreamParser.TT_EOF) {
                    nextToken();
                    svals.add(sval);
                    nvals.add(nval);
                    ttypes.add(ttype);
                    lines.add(lineno());

                    flag = toString().equalsIgnoreCase(sst.toString());
                    if (!flag)
                        break;
                }
                pushBack(svals, nvals, ttypes, lines);
                nextToken();
            } catch (IOException ex) {
                return false;
            }
            return flag;
        } finally {
            setEchoCommentsWithExclamationMark(echo);
        }
    }

    /**
     * Compares the given string of tokens with the next tokens in the
     * stream
     *
     * @param s string of tokens to be compared with the tokens in the input stream
     * @return true, if all tokens match
     */
    public boolean peekMatchRespectCase(String s) {
        final boolean echo = isEchoCommentsWithExclamationMark();
        setEchoCommentsWithExclamationMark(false);
        try {
            final NexusStreamTokenizer sst = new NexusStreamTokenizer(new StringReader(s));
            sst.setSquareBracketsSurroundComments(isSquareBracketsSurroundComments());

            final ArrayList<Double> nvals = new ArrayList<>();
            final ArrayList<String> svals = new ArrayList<>();
            final ArrayList<Integer> ttypes = new ArrayList<>();
            final ArrayList<Integer> lines = new ArrayList<>();

            svals.add(sval);
            nvals.add(nval);
            ttypes.add(ttype);
            lines.add(lineno());

            boolean flag = true;
            try {
                while (sst.nextToken() != NexusStreamParser.TT_EOF) {
                    nextToken();
                    svals.add(sval);
                    nvals.add(nval);
                    ttypes.add(ttype);
                    lines.add(lineno());
                    flag = toString().equals(sst.toString());
                    if (!flag)
                        break;
                }
                pushBack(svals, nvals, ttypes, lines);
                nextToken();
            } catch (IOException ex) {
                return false;
            }
            return flag;
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
            final ArrayList<Double> nvals = new ArrayList<>();
            final ArrayList<String> svals = new ArrayList<>();
            final ArrayList<Integer> ttypes = new ArrayList<>();
            final ArrayList<Integer> lines = new ArrayList<>();

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
            } catch (IOException ex) {
            }
            return result;
        } finally {
            setEchoCommentsWithExclamationMark(echo);
        }
    }


    /**
     * do the next tokens match 'begin NAME;'
     *
     * @param blockName
     * @return true, if begin of named block
     */
    public boolean peekMatchBeginBlock(String blockName) {
        return peekMatchIgnoreCase("begin " + blockName + ";") || peekMatchIgnoreCase("Beginblock " + blockName + ";");
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
    public List<String> getTokensLowerCase(String first, String last) throws IOExceptionWithLineNumber {
        if (first != null)
            matchIgnoreCase(first);
        final LinkedList<String> list = new LinkedList<>();
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
    public List<String> getTokensRespectCase(String first, String last) throws IOExceptionWithLineNumber {
        if (first != null)
            matchIgnoreCase(first);
        final LinkedList<String> list = new LinkedList<>();
        try {
            nextToken();
            while (last == null || !toString().equals(last)) {
                if (ttype == TT_EOF) {
                    if (last == null)
                        break;
                    throw new IOExceptionWithLineNumber("'" + last + "' expected, got EOF", lineno());
                }
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
        String fileName = getWordFileNamePunctuation();
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

        StringBuilder result = new StringBuilder();
        try {
            if (first != null)
                matchIgnoreCase(first);
            nextToken();
            while (!toString().equals(last)) {
                result.append(" ").append(toString());
                if (ttype == TT_EOF)
                    throw new IOExceptionWithLineNumber("'" + last + "' expected, got EOF", lineno());
                nextToken();
            }
        } catch (IOException ex) {
            throw new IOExceptionWithLineNumber(lineno(), ex);
        } finally {
            popPunctuationCharacters();
        }
        if (result.toString().equals(""))
            return null;
        return result.toString();
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
        final StringBuilder buf = new StringBuilder();
        if (first != null)
            matchIgnoreCase(first);
        try {
            nextToken();
            while (!toString().equals(last)) {
                buf.append("'").append(toString()).append("'");
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
        StringBuilder result = new StringBuilder();
        try {
            nextToken();
            while (!toString().equalsIgnoreCase(last)) {
                result.append(" ").append(toString().toLowerCase());
                if (ttype == TT_EOF)
                    throw new IOExceptionWithLineNumber("'" + last + "' expected, got EOF", lineno());
                nextToken();
            }
        } catch (IOException ex) {
            throw new IOExceptionWithLineNumber(lineno(), ex);
        }
        if (result.toString().equals(""))
            return null;
        return result.toString();
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
        StringBuilder result = new StringBuilder();
        try {
            nextToken();
            while (!toString().equals(last)) {
                result.append(" ").append(toString());
                if (ttype == TT_EOF)
                    throw new IOExceptionWithLineNumber("'" + last + "' expected, got EOF", lineno());
                nextToken();
            }
        } catch (IOException ex) {
            throw new IOExceptionWithLineNumber(lineno(), ex);
        }
        if (result.toString().equals(""))
            return null;
        return result.toString().trim();
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

        boolean result = defaultValue;
        boolean found = false;
        String str = List2String(tokens);
        final NexusStreamParser s = new NexusStreamParser(new StringReader(str));
        tokens.clear();

        try {
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

        float result = defaultValue;
        boolean found = false;
        final NexusStreamParser s = new NexusStreamParser(new StringReader(List2String(tokens)));
        tokens.clear();

        try {
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

        boolean found = false;
        // The following line seems to be a bug - have replaced it
        //String result = defaultValue;
        StringBuilder result = new StringBuilder();

        NexusStreamParser s =
                new NexusStreamParser(new StringReader(List2String(tokens)));
        tokens.clear();

        try {
            while (s.ttype != NexusStreamParser.TT_EOF) {
                if (!found && s.peekMatchIgnoreCase(token)) {
                    s.matchIgnoreCase(token + leftDelimiter);
                    while (true) {
                        s.nextToken();
                        String word = s.toString();
                        found = true;

                        if (word.equalsIgnoreCase(rightDelimiter))
                            break;
                        if (!result.toString().equals(""))
                            result.append(" ");
                        result.append(word);
                    }
                } else {
                    if (s.nextToken() != NexusStreamParser.TT_EOF)
                        tokens.add(s.toString());
                }
            }
        } catch (IOException ex) {
            throw new IOExceptionWithLineNumber(lineno(), ex);
        }
        if (result.toString().equals(""))
            result = new StringBuilder(defaultValue);
        return result.toString();
    }

    /**
     * Searches for an occurrence of `token value', where value is a
     * string occuring in legalValues, returning value, if found, or
     * a default value, if token does not occur
     *
     * @param tokens       the list of tokens
     * @param token        the token to look for
     * @param legalValues  if not null, string containing all legal values of the token
     * @param defaultValue the return value, if token not found
     * @return the value
     */
    public String findIgnoreCase(List<String> tokens, String token, String legalValues, String defaultValue) throws IOExceptionWithLineNumber {
        if (tokens.size() == 0)
            return defaultValue;

        boolean found = false;
        String result = defaultValue;
        final NexusStreamParser s = new NexusStreamParser(new StringReader(List2String(tokens)));
        tokens.clear();

        try {
            while (s.ttype != NexusStreamParser.TT_EOF) {
                if (!found && s.peekMatchIgnoreCase(token)) {
                    s.matchIgnoreCase(token);
                    s.nextToken();
                    result = s.toString();
                    found = true;
                    if (legalValues != null && !findIgnoreCase(legalValues, result))
                        throw new IOExceptionWithLineNumber(token + " '" + result + "': illegal value", lineno());
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
     * character occuring in legalValues, returning value, if found, or
     * a default value, if token does not occur
     *
     * @param tokens       the list of tokens
     * @param token        the token to look for
     * @param legalValues  if not null, string containing all legal values of the
     *                     character
     * @param defaultValue the return value, if token not found
     * @return the value
     */
    public char findIgnoreCase(List<String> tokens, String token, String legalValues, char defaultValue) throws IOExceptionWithLineNumber {
        if (tokens.size() == 0)
            return defaultValue;

        boolean found = false;
        char result = defaultValue;
        final NexusStreamParser s = new NexusStreamParser(new StringReader(List2String(tokens)));
        tokens.clear();

        try {
            while (s.ttype != NexusStreamParser.TT_EOF) {
                if (!found && s.peekMatchIgnoreCase(token)) {
                    s.matchIgnoreCase(token);
                    s.nextToken();
                    String str = s.toString();
                    if (str.length() > 1)
                        throw new IOExceptionWithLineNumber(token + " '" + result + "': char expected", lineno());
                    result = str.charAt(0);
                    found = true;
                    if (legalValues != null && legalValues.indexOf(result) == -1)
                        throw new IOExceptionWithLineNumber(token + " '" + result + "': illegal value", lineno());
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

        boolean found = false;
        double result = defaultValue;
        final NexusStreamParser s = new NexusStreamParser(new StringReader(List2String(tokens)));
        tokens.clear();

        try {
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

        boolean found = false;
        int result = defaultValue;
        final NexusStreamParser s = new NexusStreamParser(new StringReader(List2String(tokens)));
        tokens.clear();

        try {
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
     * @param tokens the list of tokens
     * @param token    the token to look for
     * @param defaultValue the return value, if token not found
     * @return the value
     */
    public Color findIgnoreCase(List<String> tokens, String token, Color defaultValue) throws IOException {
        if (tokens.size() == 0)
            return defaultValue;

        boolean found = false;
        java.awt.Color result = defaultValue;
        final NexusStreamParser s = new NexusStreamParser(new StringReader(List2String(tokens)));
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

        boolean result = false;
        final NexusStreamParser s = new NexusStreamParser(new StringReader(List2String(tokens)));
        tokens.clear();

        try {
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
        final NexusStreamParser s = new NexusStreamParser(new StringReader(vals));
        try {
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
     *
     * @param tokens
     * @throws IOExceptionWithLineNumber
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
            nval = Integer.valueOf(sval);
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
        int result = getInt();

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
            nval = Long.valueOf(sval);
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
            nval = Double.valueOf(sval);
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
        double result = getDouble();

        if (result < low || result > high) {
            if (low > Double.MIN_VALUE && high == Double.MAX_VALUE)
                throw new IOExceptionWithLineNumber("value " + result + " smaller than minimum: " + low, lineno());
            else if (low == Double.MIN_VALUE && high < Double.MAX_VALUE)
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
     * gets a taxon or set label
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
    static String List2String(List tokens) {
        StringBuilder sb = new StringBuilder();

        ListIterator it = tokens.listIterator();

        boolean first = true;
        while (it.hasNext()) {
            if (first) {
                sb.append("'").append(it.next()).append("'");
                first = false;
            } else
                sb.append(" '").append(it.next()).append("'");
        }
        return sb.toString();
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
    public String convertToBlock(String firstSourceLabel, String lastSourceLabel, String blockName) throws Exception {
        pushPunctuationCharacters(SEMICOLON_PUNCTUATION);
        StringBuilder str = new StringBuilder("begin " + blockName + ";");
        try {
            List<String> tokens = getTokensRespectCase(firstSourceLabel, lastSourceLabel);
            for (String token : tokens) str.append(" ").append(token);

            str.append(";end;");
        } finally {
            popPunctuationCharacters();
        }
        return str.toString();
    }

    /**
     * Peeks at the next token and attempts to match it to any of the tokens
     * present in str
     *
     * @param s a string of tokens
     */
    public boolean peekMatchAnyTokenIgnoreCase(String s) {
        final boolean echo = isEchoCommentsWithExclamationMark();
        setEchoCommentsWithExclamationMark(false);
        try {
            final NexusStreamTokenizer sst = new NexusStreamTokenizer(new StringReader(s));
            sst.setSquareBracketsSurroundComments(isSquareBracketsSurroundComments());

            while (sst.nextToken() != NexusStreamParser.TT_EOF) {
                if (peekMatchIgnoreCase(sst.toString()))
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
     * Peeks at the next token and attempts to match it to any of the tokens
     * present in str
     *
     * @param s a string of tokens
     */
    public void matchAnyTokenIgnoreCase(String s) throws IOExceptionWithLineNumber {
        try {
            final NexusStreamTokenizer sst = new NexusStreamTokenizer(new StringReader(s));
            sst.setSquareBracketsSurroundComments(isSquareBracketsSurroundComments());

            while (sst.nextToken() != NexusStreamParser.TT_EOF) {
                if (peekMatchIgnoreCase(sst.toString())) {
                    matchIgnoreCase(sst.toString());
                    return;
                }
            }
        } catch (IOException ex) {
            jloda.util.Basic.caught(ex);
        }
        throw new IOExceptionWithLineNumber("any of '" + s.toLowerCase() + "' expected", lineno());
    }


    /**
     * Peeks at the next token and attempts to match it to any of the tokens
     * present in str
     *
     * @param s a string of tokens
     */
    public boolean peekMatchAnyTokenRespectCase(String s) {
        final boolean echo = isEchoCommentsWithExclamationMark();
        setEchoCommentsWithExclamationMark(false);
        try {
            final NexusStreamTokenizer sst = new NexusStreamTokenizer(new StringReader(s));
            sst.setSquareBracketsSurroundComments(isSquareBracketsSurroundComments());

            while (sst.nextToken() != NexusStreamParser.TT_EOF) {
                if (peekMatchRespectCase(sst.toString()))
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
     * @param first
     * @param last
     * @return all words between first and last token
     */
    public List<String> getWordsRespectCase(String first, String last) throws IOExceptionWithLineNumber {
        pushPunctuationCharacters(SEMICOLON_PUNCTUATION);

        final LinkedList<String> list = new LinkedList<>();
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
     * @param n number of wordxs
     * @return next n words
     */
    public List getWordsRespectCase(int n) throws IOExceptionWithLineNumber {
        pushPunctuationCharacters(SEMICOLON_PUNCTUATION);
        LinkedList<String> list = new LinkedList<>();

        try {
            for (int i = 0; i < n; i++)
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
     * @param firstToken
     * @param lastToken
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

        List<Integer> result = new LinkedList<>();
        BitSet seen = new BitSet();

        int inState = 0; // 0: expecting first number, 1: expecting new number or -2: expecting second number
        int firstNumber = 0;
        int secondNumber;
        final Iterator<String> it = tokens.listIterator();
        while (it.hasNext()) {
            String label = it.next();

            if (label.equalsIgnoreCase("none")) {
                if (!it.hasNext())
                    throw new IOExceptionWithLineNumber("unexcepted: " + label, lineno());
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

                    int imin = Math.min(firstNumber, secondNumber);
                    int imax = Math.max(firstNumber, secondNumber);
                    for (int i = imin; i <= imax; i++) {
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
        try {
            final NexusStreamParser np = new NexusStreamParser(new StringReader(ex.toString()));
            while (np.peekNextToken() != NexusStreamParser.TT_EOF
                    && !np.peekMatchIgnoreCase("line"))
                np.getWordRespectCase();
            np.getWordRespectCase();
            return np.getInt();
        } catch (Exception ex2) {
        }
        return 0;
    }

    /**
     * gets the legal token matched by next word in stream
     *
     * @param legalTokens
     * @return matched token
     * @throws IOExceptionWithLineNumber
     */
    public String getWordMatchesIgnoringCase(String legalTokens) throws IOExceptionWithLineNumber {
        final String word = getWordRespectCase();
        final NexusStreamParser np = new NexusStreamParser(new StringReader(legalTokens));
        try {
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
     * @param legalTokens
     * @return matched token
     * @throws IOExceptionWithLineNumber
     */
    public String getWordMatchesRespectingCase(String legalTokens) throws IOExceptionWithLineNumber {
        final String word = getWordRespectCase();
        final NexusStreamParser np = new NexusStreamParser(new StringReader(legalTokens));
        try {
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
     * @param legalTokens
     * @return matched token
     * @throws IOExceptionWithLineNumber
     */
    public String getWordMatchesRespectingCase(Collection<String> legalTokens) throws IOExceptionWithLineNumber {
        final String word = getWordRespectCase();
        for (String legalToken : legalTokens)
            if (word.equals(legalToken))
                return legalToken;
        throw new IOExceptionWithLineNumber("input '" + word + "' does not match any of legal tokens: " + Basic.toString(legalTokens, " "), lineno());
    }


    /**
     * gets the legal token matched by next word in stream
     *
     * @param legalTokens
     * @return matched token
     * @throws IOExceptionWithLineNumber
     */
    public String getWordMatchesRespectingCase(String[] legalTokens) throws IOExceptionWithLineNumber {
        final String word = getWordRespectCase();
        for (String legalToken : legalTokens)
            if (word.equals(legalToken))
                return legalToken;
        throw new IOExceptionWithLineNumber("input '" + word + "' does not match any of legal tokens: " + Basic.toString(legalTokens, " "), lineno());
    }

    /**
     * gets the legal token matched by next word in stream
     *
     * @param legalTokens
     * @return matched token
     * @throws IOExceptionWithLineNumber
     */
    public String getWordMatchesIgnoringCase(Collection<String> legalTokens) throws IOExceptionWithLineNumber {
        final String word = getWordRespectCase();
        for (String legalToken : legalTokens)
            if (word.equalsIgnoreCase(legalToken))
                return legalToken;
        throw new IOExceptionWithLineNumber("input '" + word + "' does not match any of legal tokens: " + Basic.toString(legalTokens, ", "), lineno());
    }

    /**
     * gets the legal token matched by next word in stream
     *
     * @param legalTokens
     * @return matched token
     * @throws IOExceptionWithLineNumber
     */
    public String getWordMatchesIgnoringCase(String[] legalTokens) throws IOExceptionWithLineNumber {
        final String word = getWordRespectCase();
        for (String legalToken : legalTokens)
            if (word.equalsIgnoreCase(legalToken))
                return legalToken;
        throw new IOExceptionWithLineNumber("input '" + word + "' does not match any of legal tokens: " + Basic.toString(legalTokens, ", "), lineno());
    }


    /**
     * get a color, either from a name or from r g b
     *
     * @return color
     * @throws IOException
     */
    public Color getColor() throws IOException {

        try {
            int r = 0, g = 0, b = 0, a = 0;
            for (int i = 0; i < 4; i++) {
                String word = getWordRespectCase();
                switch (i) {
                    case 0:
                        if (word.equals("null"))
                            return null;
                        if (word.startsWith("#")) // format #rrggbb
                        {
                            javafx.scene.paint.Color fx = javafx.scene.paint.Color.web(word);
                            return new java.awt.Color((float) fx.getRed(),
                                    (float) fx.getGreen(),
                                    (float) fx.getBlue(),
                                    (float) fx.getOpacity());
                        }
                        if (isHexInt(word)) {
                            r = parseHexInt(word);
                            return new Color(r);
                        } else if (Basic.isInteger(word)) {
                            r = Integer.parseInt(word);
                        } else {
                            return Colors.parseColor(word);
                        }
                        break;
                    case 1:
                        g = Integer.parseInt(word);
                        break;
                    case 2:
                        b = Integer.parseInt(word);
                        if (!Basic.isInteger(peekNextWord())) {
                            return new Color(r, g, b);
                        }
                        break;
                    case 3:
                        a = Integer.parseInt(word);
                        break;
                }
            }
            return new Color(r, g, b, a);
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
        if (value.startsWith("0x"))
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
     * @throws IOExceptionWithLineNumber
     */
    public String skipBlock() throws IOExceptionWithLineNumber {
        final boolean echo = isEchoCommentsWithExclamationMark();
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
}

// EOF
