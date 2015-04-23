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

import java.io.*;

/**
 * iterates over all lines in a file. File can also be a .gz file.
 * Daniel Huson, 3.2012
 */
public class FileInputIterator implements ICloseableIterator<String>, Closeable {
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

    /**
     * constructor
     *
     * @param fileName
     * @throws java.io.FileNotFoundException
     */
    public FileInputIterator(String fileName) throws IOException {
        this(new File(fileName));
    }

    /**
     * constructor
     *
     * @param r
     * @throws java.io.FileNotFoundException
     */
    public FileInputIterator(Reader r, String fileName) throws IOException {
        reader = new BufferedReader(r, bufferSize);
        endOfLineBytes = System.getProperty("line.separator").length();

        File file = new File(fileName);
        if (file.exists())
            maxProgress = file.length();
        else
            maxProgress = 10000000;  // unknown
    }

    /**
     * constructor
     *
     * @param file
     * @throws java.io.FileNotFoundException
     */
    public FileInputIterator(File file) throws IOException {
        if (Basic.isZIPorGZIPFile(file.getPath())) {
            reader = new BufferedReader(new InputStreamReader(Basic.getInputStreamPossiblyZIPorGZIP(file.getPath())));
            endOfLineBytes = 1;
            maxProgress = file.length() / 10;
        } else {
            reader = new BufferedReader(new FileReader(file), bufferSize);
            endOfLineBytes = System.getProperty("line.separator").length();
            maxProgress = file.length();
        }
        done = (file.length() == 0);
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
            String result = nextLine;
            nextLine = null;
            lineNumber++;
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
            throw new IOException("FileInputIterator: pushBack buffer overflow");
        pushedBackLine = aLine;
    }
}
