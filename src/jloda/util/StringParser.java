/**
 * Copyright 2015, Daniel Huson
 * Author Daniel Huson
 *(Some files contain contributions from other authors, who are then mentioned separately)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
