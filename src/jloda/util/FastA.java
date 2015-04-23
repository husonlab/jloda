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

/**
 * Fasta i/o
 * Daniel Huson, 12.10.2003
 */


import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Vector;

/**
 * Fasta i/o  class
 */
public class FastA {
    private int size;
    private final Vector<String> headers;
    private final Vector<String> sequences;

    /**
     * constructor
     */
    public FastA() {
        size = 0;
        headers = new Vector<>();
        sequences = new Vector<>();

    }

    /**
     * construct a new FastA object and add the given record
     *
     * @param header   header of record
     * @param sequence sequence of record
     */
    public FastA(String header, String sequence) {
        this();
        add(header, sequence);
    }

    /**
     * add a header and sequence
     *
     * @param header
     * @param sequence
     */
    public void add(String header, String sequence) {
        set(getSize(), header, sequence);
    }

    /**
     * erase the data
     */
    public void clear() {
        headers.clear();
        sequences.clear();
        size = 0;
    }

    /**
     * gets the header
     *
     * @param i the index
     * @return header string
     */
    public String getHeader(int i) {
        return headers.get(i);
    }

    /**
     * sets the header and sequence
     *
     * @param i      the index
     * @param header
     */
    public void set(int i, String header, String sequence) {
        if (header.startsWith(">"))
            header = header.substring(1);
        if (i < getSize()) {
            this.headers.set(i, header);
            this.sequences.set(i, sequence);
        } else {
            setSize(i + 1);
            this.headers.add(i, header);
            this.sequences.add(i, sequence);
        }
    }

    /**
     * gets the sequence
     *
     * @param i the index
     * @return the sequence
     */
    public String getSequence(int i) {
        return sequences.get(i);
    }

    /**
     * gets the first header
     *
     * @return first header
     */
    public String getFirstHeader() {
        return headers.firstElement();
    }

    /**
     * gets the first sequence
     *
     * @return first sequence
     */
    public String getFirstSequence() {
        return sequences.firstElement();
    }

    /**
     * sets the size of sequences
     *
     * @param n the size
     */
    public void setSize(int n) {
        if (n > size) {
            headers.setSize(n);
            sequences.setSize(n);
        }
        size = n;
    }

    /**
     * get the size of sequences
     *
     * @return size of sequences
     */
    public int getSize() {
        return size;
    }

    /**
     * read header and sequence in fastA format
     *
     * @param r
     * @throws java.io.IOException
     */
    public void read(Reader r) throws IOException {
        clear();

        BufferedReader br = new BufferedReader(r);

        String header = "";
        StringBuilder sequence = null;

        String aLine = br.readLine();

        while (aLine != null) {
            aLine = aLine.trim();
            if (aLine.length() > 0) {

                if (aLine.charAt(0) == '>') // new fasta header
                {
                    if (header.length() > 0 && sequence != null) {
                        add(header, sequence.toString());
                    }
                    header = aLine.substring(1).trim();
                    sequence = new StringBuilder();
                } else if (sequence != null)
                    sequence.append(aLine);
            }
            aLine = br.readLine();
        }
        if (header.length() > 0) {
            add(header, sequence != null ? sequence.toString() : null);
        }
    }

    /**
     * write header and sequence in fastA format
     *
     * @param w
     * @throws IOException
     */
    public void write(Writer w) throws IOException {
        for (int i = 0; i < getSize(); i++) {
            if (getHeader(i) != null) {
                String header = getHeader(i);
                if (!header.startsWith(">"))
                    w.write(">");
                w.write(header);
                if (!header.endsWith("\n"))
                    w.write("\n");
                int lineLength = 0;
                for (int c = 0; c < getSequence(i).length(); c++) {
                    int ch = getSequence(i).charAt(c);
                    if (!Character.isSpaceChar(ch)) {
                        w.write(ch);
                        lineLength++;
                        if (lineLength == 0) {
                            w.write('\n');
                            lineLength = 0;
                        }
                    }
                }
                if (lineLength > 0)
                    w.write('\n');
            }
        }
        w.flush();
    }
}
