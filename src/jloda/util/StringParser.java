/**
 * StringParser.java 
 * Copyright (C) 2016 Daniel H. Huson
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

import java.awt.*;
import java.io.IOException;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * parses strings in format label=value
 * Daniel Huson   , 1.2006
 */
public class StringParser implements Iterator {
    String pushedBack = null;
    final StringTokenizer strTok;

    public StringParser(final String input) {
        this.strTok = new StringTokenizer(input);
    }

    /**
     * has another token
     *
     * @return true, if has another token
     */
    public boolean hasNext() {
        return strTok.hasMoreTokens();
    }

    /**
     * assumes the next entry is label=int
     *
     * @param label
     * @return
     * @throws IOException
     */
    public int getInt(final String label) throws IOException {
        matchLabel(label);
        try {
            return (Integer.parseInt(nextToken()));
        } catch (Exception ex) {
            throw new IOException("Integer expected");
        }
    }

    /**
     * assumes the next entry is label=byte
     *
     * @param label
     * @return
     * @throws IOException
     */
    public byte getByte(final String label) throws IOException {
        matchLabel(label);
        try {
            return (Byte.parseByte(nextToken()));
        } catch (Exception ex) {
            throw new IOException("Byte expected");
        }
    }

    /**
     * assumes the next entry is label=byte
     *
     * @param label
     * @return
     * @throws IOException
     */
    public double getDouble(final String label) throws IOException {
        matchLabel(label);
        try {
            return (Double.parseDouble(nextToken()));
        } catch (Exception ex) {
            throw new IOException("Double expected");
        }
    }

    /**
     * assumes the next entry is label=color
     *
     * @param label
     * @return
     * @throws IOException
     */
    public Color getColor(final String label) throws IOException {
        matchLabel(label);
        try {
            return Color.decode(nextToken());
        } catch (Exception ex) {
            throw new IOException("Color expected");
        }
    }

    public String getString(final String label) throws IOException {
        matchLabel(label);
        try {
            return nextToken();
        } catch (Exception ex) {
            throw new IOException("String expected");
        }
    }

    public Object next() {
        return nextToken();
    }

    /**
     * gets the next token
     *
     * @return next token
     */
    public String nextToken() {
        if (pushedBack != null) {
            String result = pushedBack;
            pushedBack = null;
            return result;
        } else
            return strTok.nextToken();
    }

    public String peek() {
        if (pushedBack != null)
            return pushedBack;
        else {
            if (!strTok.hasMoreTokens())
                return null;
            pushedBack = strTok.nextToken();
            return pushedBack;
        }
    }

    private void matchLabel(final String label) throws IOException {
        String str = nextToken();
        if (!(str.equals(label + "=") || (str + nextToken()).equals(label + "=")))
            throw new IOException("Expected '" + label + "=', got: " + str);
    }

    /**
     * need this for the iterator interface
     */
    public void remove() {
    }
}
