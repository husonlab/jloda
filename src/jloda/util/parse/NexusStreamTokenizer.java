/**
 * NexusStreamTokenizer.java
 * Copyright (C) 2019 Daniel H. Huson
 * <p>
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * tokenizer for nexus streams and similar input
 *
 * @author Daniel Huson, 2002
 * <p>
 */
package jloda.util.parse;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.Collection;
import java.util.EmptyStackException;
import java.util.LinkedList;
import java.util.Stack;

/**
 * tokenizer for nexus streams and similar input
 *
 * @author Daniel Huson, 2002
 *
 */
public class NexusStreamTokenizer extends StreamTokenizer {
    final public static String STRICT_PUNCTUATION = "(){}/\\,;:=*\"`+-<>";
    final public static String NEGATIVE_INTEGER_PUNCTUATION = "(){}/\\,;:=*\"`+<>";
    final public static String LABEL_PUNCTUATION = "(),;:=\"`{}";
    final public static String ASSIGNMENT_PUNCTUATION = "=;";
    final public static String SEMICOLON_PUNCTUATION = ";";
    final public static String EOL_SPACE = "\f\n\r";
    final public static String SPACE = " \f\n\r\t";
    final public static String ILLEGAL_CHARS = "\f\n\r\t()[]{}/\\,;:=*'\"`<>";

    private boolean parsenumbers = false;

    private boolean squareBracketsSurroundComments = true;

    private String punctchars = NEGATIVE_INTEGER_PUNCTUATION;
    private final Stack<String> punctCharsStack = new Stack<>();

    private String spaceChars = SPACE;
    private final Stack<String> spaceCharsStack = new Stack<>();
    private boolean eolsignificant = false;

    public double nval = 0;
    public String sval = "";
    public int ttype = 0;
    private int line = 0;

    private boolean collectAllComments = false;
    private String comment = null;
    private boolean echoCommentsWithExclamationMark = true;

    // we need these so that we can peek ahead as far as we like
    private final LinkedList<Double> nvals = new LinkedList<>();
    private final LinkedList<String> svals = new LinkedList<>();
    private final LinkedList<Integer> ttypes = new LinkedList<>();
    private final LinkedList<Integer> lines = new LinkedList<>();


    /**
     * Construct a new NexusBlock object for the specified reader
     */
    public NexusStreamTokenizer(Reader r) {
        super(r);
        setSyntax();
    }

    /**
     * Get the next token and returns its type.
     *
     * @return the type of the token
     */
    public int nextToken() throws java.io.IOException {
        int tt;

        if (ttypes.size() > 0) {
            if (nvals.getFirst() == null) {
                nval = 0;
                nvals.removeFirst();
            } else
                nval = nvals.removeFirst();
            sval = svals.removeFirst();
            ttype = ttypes.removeFirst();
            line = lines.removeFirst();
            return ttype;
        } else {
            tt = super.nextToken();
            sval = super.sval;
            nval = super.nval;
            ttype = super.ttype;
            line = super.lineno();
        }
        // The following lines skip comments of the form enclosed by [ and ]
        // Comments enclosed by [! and ] are printed to standard err
        if (squareBracketsSurroundComments) {
            while (tt == (int) '[') // start of comment
            {
                int cline = lineno();
                boolean verbose = false;

                setCommentSyntax();
                tt = super.nextToken();
                sval = super.sval;

// Set the comment String

                if (sval != null) {
                    if (collectAllComments && comment != null) {
                        comment += "\n" + (sval.startsWith("!") ? sval.substring(1) : sval);
                    } else
                        comment = (sval.startsWith("!") ? sval.substring(1) : sval);
                }
                nval = super.nval;
                ttype = super.ttype;
                line = super.lineno();
                if (ttype == TT_WORD && sval.charAt(0) == '!') {
                    verbose = true;
                    if (echoCommentsWithExclamationMark)
                        System.err.print("[");
                }
                while (tt != (int) ']') {
                    if (tt == TT_EOF) {
                        setSyntax();
                        throw new java.io.IOException("Line " + cline + ": start of unterminated comment");
                    }
                    if (verbose && ttype == TT_WORD) {
                        if (echoCommentsWithExclamationMark)
                            System.err.println(sval);
                    }
                    tt = super.nextToken();
                    sval = super.sval;
                    if (sval != null) {
                        if (comment == null)
                            comment = (sval.startsWith("!") ? sval.substring(1) : sval);
                        else
                            comment += "\n" + (sval.startsWith("!") ? sval.substring(1) : sval);
                    }
                    nval = super.nval;
                    ttype = super.ttype;
                    line = super.lineno();
                }
                if (verbose && echoCommentsWithExclamationMark)
                    System.err.println("]");
                setSyntax();
                tt = super.nextToken();
                sval = super.sval;
                nval = super.nval;
                ttype = super.ttype;
                line = super.lineno();
            }
        }
        return tt;
    }

    /**
     * Gets all comments since last call of getComment
     *
     * @return comments
     */
    public String getComment() {
        String result = comment;
        comment = null;
        return result;
    }

    /**
     * Push the current token onto the token stream
     */
    public void pushBack() {
        svals.add(0, sval);
        nvals.add(0, nval);
        ttypes.add(0, ttype);
        lines.add(0, lineno());
    }

    /**
     * Push the given token onto the token stream
     *
     * @param sval  the string value
     * @param nval  the number value
     * @param ttype the token type
     * @param line  the line number
     */
    public void pushBack(String sval, double nval, int ttype, int line) {
        svals.add(0, sval);
        nvals.add(0, nval);
        ttypes.add(0, ttype);
        lines.add(0, line);
    }

    /**
     * Push the given tokens onto the token stream
     *
     * @param svals  a collection of string values
     * @param nvals  a collection of number values
     * @param ttypes a collection of token types
     * @param lines  a collection of line numbers
     */
    public void pushBack(Collection<String> svals, Collection<Double> nvals, Collection<Integer> ttypes, Collection<Integer> lines) {
        this.svals.addAll(0, svals);
        this.nvals.addAll(0, nvals);
        this.ttypes.addAll(0, ttypes);
        this.lines.addAll(0, lines);
    }

    /**
     * Peeks at the next token
     *
     * @return ttype of next token
     */
    public int peekNextToken() throws IOException {
        int tt = nextToken();
        pushBack();
        pushBack(); //TODO: I don't understand whats going on here! - David.
        nextToken();
        return tt;
    }


    /**
     * Set the current punctuation characters
     *
     * @param s string of punctuation characters
     */
    public void setPunctuationCharacters(String s) {
        punctchars = s;
        setSyntax();
    }

    /**
     * Get the current punctuation characters
     *
     * @return string of punctuation characters
     */
    public String getPunctuationCharacters() {
        return punctchars;
    }

    /**
     * Push the current punctuation characters
     *
     * @param s string of punctuation characters
     */
    public void pushPunctuationCharacters(String s) {
        punctCharsStack.push(punctchars);
        setPunctuationCharacters(s);
    }

    /**
     * Pop the current punctuation characters
     */
    public void popPunctuationCharacters() throws EmptyStackException {
        setPunctuationCharacters(punctCharsStack.pop());
    }

    /**
     * Set the current space characters
     *
     * @param s string of space characters
     */
    public void setSpaceCharacters(String s) {
        spaceChars = s;
        setSyntax();
    }

    /**
     * Push the current space characters
     *
     * @param s string of space characters
     */
    public void pushSpaceCharacters(String s) {
        spaceCharsStack.push(spaceChars);
        setSpaceCharacters(s);
    }

    /**
     * Pop the current space characters
     */
    public void popSpaceCharacters() throws EmptyStackException {
        setSpaceCharacters(spaceCharsStack.pop());
    }


    /**
     * Parse numbers or not
     *
     * @param flag parse numbers or not
     */
    public void setParseNumbers(boolean flag) {
        parsenumbers = flag;
        setSyntax();
    }

    /**
     * End-of-line is significant or not?
     *
     * @return boolean true if eoln returned as separate token
     */
    public boolean isEolSignificant() {
        return eolsignificant;
    }

    /**
     * End-of-line is significant or not?
     *
     * @param flag significant or not
     */
    public void setEolIsSignificant(boolean flag) {
        eolsignificant = flag;
        setSyntax();
    }

    /**
     * Reset the syntax using current settings
     */
    public void setSyntax() {
        resetSyntax();
        if (parsenumbers)
            parseNumbers();
        eolIsSignificant(eolsignificant);
        lowerCaseMode(false);
        wordChars(33, 126);

        for (int i = 0; i < punctchars.length(); i++)
            ordinaryChar(punctchars.charAt(i));
        ordinaryChar('['); // always need this to identify comments
        ordinaryChar(']'); // always need this to identify comments
        for (int i = 0; i < spaceChars.length(); i++)
            whitespaceChars(spaceChars.charAt(i), spaceChars.charAt(i));
        quoteChar('\'');
    }

    /**
     * sets the syntax without "'" as quote character.
     */
    public void setSyntaxNoQuote() {
        resetSyntax();
        if (parsenumbers)
            parseNumbers();
        eolIsSignificant(eolsignificant);
        lowerCaseMode(false);
        wordChars(33, 126);
        for (int i = 0; i < punctchars.length(); i++)
            ordinaryChar(punctchars.charAt(i));
        for (int i = 0; i < spaceChars.length(); i++)
            whitespaceChars(spaceChars.charAt(i), spaceChars.charAt(i));
    }

    /**
     * Sets the syntax so that all characters upto the end of the comment
     * are returned as one token
     */
    void setCommentSyntax() {
        resetSyntax();
        wordChars(1, 126);
        eolIsSignificant(true);
        ordinaryChar(']');
        whitespaceChars('\n', '\n');
    }

    /**
     * Returns the current token as a string
     *
     * @return current token as a string
     */
    public String toString() {
        if (ttype == TT_WORD || ttype == (int) '\'')
            return sval;
        else if (ttype == TT_NUMBER)
            return String.valueOf(nval);
        else
            return "" + (char) ttype;
    }

    /**
     * Returns the current line number
     *
     * @return current line number
     */
    public int lineno() {
        return line;
    }

    /**
     * Is given character a label punctuation character?
     *
     * @param ch a character
     * @return true, if ch is contained in LABEL_PUNCTUATION
     */
    static public boolean isLabelPunctuation(char ch) {
        return LABEL_PUNCTUATION.indexOf(ch) != -1;
    }

    /**
     * Is given character a space character?
     *
     * @param ch a character
     * @return true, if ch is contained in SPACE
     */
    static public boolean isSpace(char ch) {
        return SPACE.indexOf(ch) != -1;
    }

    /**
     * if set, getComment will return all comments encountered since last call of getComment, otherwise
     * will only return last comment
     *
     * @return true, if all comments are to be collected
     */
    public boolean isCollectAllComments() {
        return collectAllComments;
    }

    /**
     * if set, getComment will return all comments encountered since last call of getComment, otherwise
     * will only return last comment
     *
     * @param collectAllComments
     */
    public void setCollectAllComments(boolean collectAllComments) {
        this.collectAllComments = collectAllComments;
    }

    public boolean isSquareBracketsSurroundComments() {
        return squareBracketsSurroundComments;
    }

    public void setSquareBracketsSurroundComments(boolean squareBracketsSurroundComments) {
        this.squareBracketsSurroundComments = squareBracketsSurroundComments;
    }

    public boolean isEchoCommentsWithExclamationMark() {
        return echoCommentsWithExclamationMark;
    }

    public void setEchoCommentsWithExclamationMark(boolean echoCommentsWithExclamationMark) {
        this.echoCommentsWithExclamationMark = echoCommentsWithExclamationMark;
    }
}

// EOF
