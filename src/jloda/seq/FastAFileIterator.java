/*
 * FastAFileIterator.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.seq;

import jloda.util.*;

import java.io.*;
import java.util.LinkedList;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * an iterator over a multi-fastA file
 * Daniel Huson, 10.2011
 */
public class FastAFileIterator implements IFastAIterator, Closeable {
    private final String fileName;
    private final BufferedReader r;
    private boolean isClosed = false;
    private String nextHeader = null;
    private String nextSequence = null;
    private String nextNextHeader = null;
    private final long endOfLineBytes;

    private long currentHeaderPosition;
    private long nextHeaderPosition;
    private long nextNextHeaderPosition;
    private long position;

    private int numberOfSequencesRead = 0;

    private final long maxProgress;

    private final LinkedList<String> parts = new LinkedList<>();

    /**
     * constructor
     *
	 */
    public FastAFileIterator(String fileName) throws IOException {
        this.fileName = fileName;
        maxProgress = FileUtils.guessUncompressedSizeOfFile(fileName);
        r = new BufferedReader(new InputStreamReader(FileUtils.getInputStreamPossiblyZIPorGZIP(fileName)));
        endOfLineBytes = FileUtils.determineEndOfLinesBytes(new File(fileName));
        moveToFirst();
    }

    public FastAFileIterator(String fileName, BufferedReader reader, long position) throws IOException {
        this.fileName = fileName;
        maxProgress = FileUtils.guessUncompressedSizeOfFile(fileName);
        this.position = position;
        r = reader;
        endOfLineBytes = FileUtils.determineEndOfLinesBytes(new File(fileName));
        moveToFirst();
    }

    /**
     * move to first fastA record
     *
	 */
    private void moveToFirst() throws IOException {
        nextHeaderPosition = position;
        nextHeader = r.readLine();
        if (nextHeader != null) {
            position += nextHeader.length() + endOfLineBytes;
            String aLine;
            while ((aLine = r.readLine()) != null) {
                if (aLine.startsWith(">")) {
                    nextNextHeaderPosition = position;
                    nextNextHeader = aLine;
                    position += aLine.length() + endOfLineBytes;
                    break;
                } else {
                    position += aLine.length() + endOfLineBytes;
                    parts.add(aLine);
                }
            }
            nextSequence = StringUtils.concatenateAndRemoveWhiteSpaces(parts);
            parts.clear();
        } else
            close();
    }

    /**
     * has next fastA record?
     *
     * @return true, if another fastA record available
     */
    public boolean hasNext() {
        return !isClosed && nextHeader != null;
    }

    /**
     * gets next fastA record
     *
     * @return header and sequence
     */
    public Pair<String, String> next() {
        currentHeaderPosition = nextHeaderPosition;
        final Pair<String, String> result = new Pair<>(nextHeader, nextSequence);
        nextHeader = nextNextHeader;
        nextHeaderPosition = nextNextHeaderPosition;
        nextSequence = null;
        nextNextHeader = null;
        if (nextHeader != null) {
            String aLine;
            try {
                while ((aLine = r.readLine()) != null) {
                    if (aLine.startsWith(">")) {
                        nextNextHeader = aLine;
                        nextNextHeaderPosition = position;
                        position += aLine.length() + endOfLineBytes;
                        break;
                    } else {
                        position += aLine.length() + endOfLineBytes;
                        parts.add(aLine);
                    }
                }
                numberOfSequencesRead++;
            } catch (IOException e) {
                System.err.println("IOException file=" + fileName + " : " + e.getMessage());
            }
            nextSequence = StringUtils.concatenateAndRemoveWhiteSpaces(parts);
            parts.clear();
        } else
            try {
                close();
            } catch (IOException ex) {
                Basic.caught(ex);
            }
        return result;
    }

    /**
     * gets the next fastA record for the given first word
     *
     * @return fastA record or null
     */
    public Pair<String, String> next(String firstWordInHeader) {
        while (hasNext()) {
            Pair<String, String> pair = next();
            if (StringUtils.getFirstWord(StringUtils.swallowLeadingGreaterSign(pair.getFirst())).equals(firstWordInHeader))
                return pair;
        }
        return null;
    }

    /**
     * gets the position in the file of the last fastA record obtained by next()
     *
     * @return position
     */
    public long getPosition() {
        return currentHeaderPosition;
    }

    /**
     * gets the number of bytes associated with the current fastA record. This can be used to access the record directly
     *
     * @return size of current record in file
     */
    public long getNumberOfBytes() {
        if (nextHeaderPosition > currentHeaderPosition)
            return nextHeaderPosition - currentHeaderPosition;
        else
            return getFileSize() - currentHeaderPosition;
    }

    public long getFileSize() {
        return maxProgress;
    }

    public void remove() {
    }

    public void close() throws IOException {
        if (!isClosed) {
            isClosed = true;
            r.close();
        }
    }

    /**
     * gets the maximum progress value
     *
     * @return maximum progress value
     */
    public long getMaximumProgress() {
        return maxProgress;
    }

    /**
     * gets the current progress value
     *
     * @return current progress value
     */
    public long getProgress() {
        return getPosition();
    }

    /**
     * gets the number of sequences read so far
     *
     * @return number of sequences
     */
    public int getNumberOfSequencesRead() {
        return numberOfSequencesRead;
    }

    public Iterable<Pair<String, String>> records() {
        return () -> FastAFileIterator.this;
    }

    public Stream<Pair<String, String>> stream() {
        return StreamSupport.stream(records().spliterator(), false);
    }

    public String getFileName() {
        return fileName;
    }

    /**
     * choose fastA or fastQ as fastA getLetterCodeIterator
     *
     * @return getLetterCodeIterator
	 */
    public static IFastAIterator getFastAOrFastQAsFastAIterator(String inputFile) throws IOException {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(FileUtils.getInputStreamPossiblyZIPorGZIP(inputFile)))) {
            String aLine = r.readLine();
            if (aLine.startsWith(">"))
                return new FastAFileIterator(inputFile);
            else if (aLine.startsWith("@"))
                return new
                        FastQAsFastAFileIterator(inputFile);
            else
                throw new IOException("File empty or not in FastA or FastQ format: " + inputFile);
        }
    }
}
