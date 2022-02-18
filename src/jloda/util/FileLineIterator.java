/*
 * FileLineIterator.java Copyright (C) 2022 Daniel H. Huson
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

import jloda.util.progress.ProgressListener;
import jloda.util.progress.ProgressPercentage;

import java.io.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * iterates over all lines in a file. File can also be a .gz file.
 * Daniel Huson, 3.2012
 */
public class FileLineIterator implements ICloseableIterator<String> {
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

    private final String fileName;
    private ProgressListener progress;

    /**
     * constructor
     *
	 */
    public FileLineIterator(String fileName) throws IOException {
        this(fileName, false);
    }

    /**
     * constructor
     *
     */
    public FileLineIterator(File file, boolean reportProgress) throws IOException {
        this(file.getPath(), reportProgress);
    }

    /**
     * constructor
     *
     */
    public FileLineIterator(File file) throws IOException {
        this(file, false);
    }

    /**
     * constructor
     *
     */
    public FileLineIterator(String fileName, ProgressListener progress) throws IOException {
        this.fileName = fileName;

        if (fileName.startsWith(PREFIX_TO_INDICATE_TO_PARSE_FILENAME_STRING)) {
            reader = new BufferedReader(new StringReader(fileName.substring(3)));
            endOfLineBytes = 1;
            maxProgress = fileName.length() - PREFIX_TO_INDICATE_TO_PARSE_FILENAME_STRING.length();
        } else if (fileName.equals("stdin")) {
            reader = new BufferedReader(new InputStreamReader(FileUtils.getInputStreamPossiblyGZIP(System.in, "stdin")));
            maxProgress = 1000000;
            endOfLineBytes = 1;
        } else {

            if (FileUtils.fileExistsAndIsNonEmpty(fileName)) {
                maxProgress = (FileUtils.isZIPorGZIPFile(fileName) ? 5 : 1) * (new File(fileName)).length();
            } else
                maxProgress = 10000000;  // unknown
            reader = new BufferedReader(new InputStreamReader(FileUtils.getInputStreamPossiblyZIPorGZIP(fileName)));
            endOfLineBytes = FileUtils.determineEndOfLinesBytes(fileName);
        }
        done = (maxProgress <= 0);
        if (progress != null)
            progress.setProgress(0L);
        this.progress = progress;
    }

    /**
     * constructor
     *
     */
    public FileLineIterator(String fileName, boolean reportProgress) throws IOException {
        this(fileName, null);
        setReportProgress(reportProgress);
    }

    /**
     * constructor
     *
     */
    public FileLineIterator(Reader r, String fileName) throws IOException {
        this(r, fileName, false);
    }

    /**
     * constructor
     *
     */
    public FileLineIterator(Reader r, String fileName, boolean reportProgress) {
        this.fileName = fileName;

        if (fileName.startsWith(PREFIX_TO_INDICATE_TO_PARSE_FILENAME_STRING)) {
            reader = new BufferedReader(new StringReader(fileName.substring(3)));
            endOfLineBytes = 1;
            maxProgress = fileName.length() - PREFIX_TO_INDICATE_TO_PARSE_FILENAME_STRING.length();
        } else {
            reader = new BufferedReader(r, bufferSize);
            endOfLineBytes = FileUtils.determineEndOfLinesBytes(new File(fileName));

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
            progress.reportTaskCompleted();
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
                try {
                    progress.setProgress(position);
                } catch (CanceledException ignored) {
                }
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

    public Iterable<String> lines() {
        return () -> FileLineIterator.this;
    }

    public Stream<String> stream() {
        return StreamSupport.stream(lines().spliterator(), false);
    }
}
