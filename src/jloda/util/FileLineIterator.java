/*
 * FileLineIterator.java Copyright (C) 2019. Daniel H. Huson
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

package jloda.util;

import java.io.*;

/**
 * iterates over all lines in a file. File can also be a .gz file.
 * Daniel Huson, 3.2012
 */
public class FileLineIterator implements  ICloseableIterator<String> {
    public static final String PREFIX_TO_INDICATE_TO_PARSE_FILENAME_STRING = "!!!";
    private final BufferedReader reader;
    private String nextLine = null;
    private long lineNumber = 0;
    private boolean done;
    private long position = -1;
    private long numberOfBytes = 0;
    private final int endOfLineBytes;
    private boolean skipEmptyLines = false;
    private boolean skipCommentLines = false;
    public static final int bufferSize = 128000;

    private final long maxProgress;

    private String pushedBackLine = null;

    private String fileName;
    private ProgressPercentage progress;

    /**
     * constructor
     *
     * @param fileName
     * @throws java.io.FileNotFoundException
     */
    public FileLineIterator(String fileName) throws IOException {
        this(fileName, false);
    }

    /**
     * constructor
     *
     * @param file
     * @throws java.io.FileNotFoundException
     */
    public FileLineIterator(File file, boolean reportProgress) throws IOException {
        this(file.getPath(), reportProgress);
    }

    /**
     * constructor
     *
     * @param file
     * @throws java.io.FileNotFoundException
     */
    public FileLineIterator(File file) throws IOException {
        this(file, false);
    }

    /**
     * constructor
     *
     * @param fileName
     * @throws java.io.FileNotFoundException
     */
    public FileLineIterator(String fileName, boolean reportProgress) throws IOException {
        this.fileName = fileName;

        if (fileName.startsWith(PREFIX_TO_INDICATE_TO_PARSE_FILENAME_STRING)) {
            reader = new BufferedReader(new StringReader(fileName.substring(3)));
            endOfLineBytes = 1;
            maxProgress = fileName.length() - PREFIX_TO_INDICATE_TO_PARSE_FILENAME_STRING.length();
        } else {
            final File file = new File(fileName);
            if (Basic.isZIPorGZIPFile(file.getPath())) {
                reader = new BufferedReader(new InputStreamReader(Basic.getInputStreamPossiblyZIPorGZIP(file.getPath())));
                endOfLineBytes = Basic.determineEndOfLinesBytes(new File(fileName));
                maxProgress = 5 * file.length(); // assuming compression factor of 5-to-1
            } else {
                reader = new BufferedReader(new FileReader(file), bufferSize);
                endOfLineBytes = 1;
                maxProgress = file.length();
            }
        }
        done = (maxProgress <= 0);
        setReportProgress(reportProgress);
    }


    /**
     * constructor
     *
     * @param r
     * @throws java.io.FileNotFoundException
     */
    public FileLineIterator(Reader r, String fileName) throws IOException {
        this(r, fileName, false);
    }

    /**
     * constructor
     *
     * @param r
     * @throws java.io.FileNotFoundException
     */
    public FileLineIterator(Reader r, String fileName, boolean reportProgress) throws IOException {
        this.fileName = fileName;

        if (fileName.startsWith(PREFIX_TO_INDICATE_TO_PARSE_FILENAME_STRING)) {
            reader = new BufferedReader(new StringReader(fileName.substring(3)));
            endOfLineBytes = 1;
            maxProgress = fileName.length() - PREFIX_TO_INDICATE_TO_PARSE_FILENAME_STRING.length();
        } else {
            reader = new BufferedReader(r, bufferSize);
            endOfLineBytes = Basic.determineEndOfLinesBytes(new File(fileName));

            File file = new File(fileName);
            if (file.exists())
                maxProgress = file.length();
            else
                maxProgress = 10000000;  // unknown
        }

        setReportProgress(reportProgress);
    }

    /**
     * report progress
     */
    public void setReportProgress(boolean reportProgress) {
        if (reportProgress) {
            if (progress == null) {
                if (!fileName.startsWith(PREFIX_TO_INDICATE_TO_PARSE_FILENAME_STRING))
                    progress = new ProgressPercentage("Processing file: " + fileName, getMaximumProgress());
                else
                    progress = new ProgressPercentage("Processing string", getMaximumProgress());
            }
        } else {
            if (progress != null) {
                progress.close();
                progress = null;
            }
        }
    }

    /**
     * position of item in file
     *
     * @return position
     */
    public long getPosition() {
        return position;
    }

    /**
     * number of bytes of item in file
     *
     * @return number of bytes
     */
    public long getNumberOfBytes() {
        return numberOfBytes;
    }

    /**
     * close associated file or database
     */
    public void close() throws IOException {
        reader.close();
        if (progress != null)
            progress.close();
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
        return position;
    }

    /**
     * is there another line
     *
     * @return true, if there is another line in the file
     */
    public boolean hasNext() {
        if (pushedBackLine != null)
            return true;
        if (done)
            return false;
        if (nextLine != null)
            return true;
        try {
            position += numberOfBytes + endOfLineBytes;
            nextLine = reader.readLine();

            if (nextLine != null) {
                numberOfBytes = nextLine.length();
                if ((skipEmptyLines && nextLine.length() == 0) || (skipCommentLines && nextLine.startsWith("#"))) {
                    nextLine = null;
                    return hasNext();
                }
            }
        } catch (IOException e) {
            done = true;
            nextLine = null;
        }
        return nextLine != null;
    }

    /**
     * gets the next line in the file
     *
     * @return next line
     */
    public String next() {
        if (pushedBackLine != null) {
            String value = pushedBackLine;
            pushedBackLine = null;
            return value;
        }
        if (done)
            return null;
        if (nextLine == null)
            hasNext();
        if (nextLine != null) {
            final String result = nextLine;
            nextLine = null;
            lineNumber++;
            if (progress != null) {
                progress.setProgress(position);
            }
            return result;
        }
        return null;
    }

    public long getLineNumber() {
        return lineNumber;
    }

    public void remove() {
    }

    public boolean isSkipEmptyLines() {
        return skipEmptyLines;
    }

    public void setSkipEmptyLines(boolean skipEmptyLines) {
        this.skipEmptyLines = skipEmptyLines;
    }

    public boolean isSkipCommentLines() {
        return skipCommentLines;
    }

    public void setSkipCommentLines(boolean skipCommentLines) {
        this.skipCommentLines = skipCommentLines;
    }

    public void pushBack(String aLine) throws IOException {
        if (pushedBackLine != null)
            throw new IOException("FileLineIterator: pushBack buffer overflow");
        pushedBackLine = aLine;
    }
}
