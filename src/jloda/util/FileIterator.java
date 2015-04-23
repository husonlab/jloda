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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;

/**
 * File iterator
 * Daniel Huson, 2014
 */
public class FileIterator implements ICloseableIterator<byte[]>, Iterator<byte[]>, Closeable {
    private byte[] bytes = new byte[1000];

    private final InputStreamReader reader;

    private long linePosition = 0;
    private int lineLength = 0;

    private byte firstByteOfNextLine = 0;

    private long position = 0; // this is the position in the unzipped file

    private final long maxProgress;

    /**
     * constructor
     *
     * @param fileName
     * @throws IOException
     */
    public FileIterator(String fileName) throws IOException {
        reader = new InputStreamReader(Basic.getInputStreamPossiblyZIPorGZIP(fileName));
        if (Basic.isZIPorGZIPFile(fileName))
            maxProgress = 20 * ((new File(fileName))).length();
        else
            maxProgress = ((new File(fileName))).length();

        // get first letter
        firstByteOfNextLine = (byte) reader.read();
        if (hasNext())
            position++;
        while (firstByteOfNextLine == '\r' || firstByteOfNextLine == '\n' && hasNext()) {
            firstByteOfNextLine = (byte) reader.read();
            if (hasNext())
                position++;
        }

    }

    @Override
    public boolean hasNext() {
        return firstByteOfNextLine != -1;
    }

    /**
     * get the next newline terminated line
     *
     * @return next line
     */
    @Override
    public byte[] next() { // get bytes as 0 terminated
        try {
            linePosition = position - 1; // -1 because we have already read the first character on the line

            lineLength = 0;
            while (firstByteOfNextLine != '\r' && firstByteOfNextLine != '\n') {
                if ((lineLength + 3) == bytes.length) {
                    byte[] tmp = new byte[2 * bytes.length];
                    System.arraycopy(bytes, 0, tmp, 0, bytes.length);
                    bytes = tmp;
                }
                bytes[lineLength++] = firstByteOfNextLine;
                if (hasNext()) {
                    firstByteOfNextLine = (byte) reader.read();
                    if (hasNext())
                        position++;
                } else
                    break;
            }
            bytes[lineLength++] = '\n';
            bytes[lineLength] = 0;

            // move to next first letter...
            firstByteOfNextLine = (byte) reader.read();
            if (hasNext())
                position++;
            while (firstByteOfNextLine == '\r' || firstByteOfNextLine == '\n' && hasNext()) {
                firstByteOfNextLine = (byte) reader.read();
                if (hasNext())
                    position++;
            }

        } catch (IOException e) {
            return null;
        }
        return bytes;
    }

    /**
     * peeks at the next byte.
     *
     * @return next byte or -1, if no next line
     */
    public byte peekNextByte() {
        return firstByteOfNextLine;
    }

    /**
     * get the line return by the last next() call
     *
     * @return last line
     */
    public byte[] getLine() {
        return bytes;
    }

    /**
     * gets the length of latest returned line
     *
     * @return
     */
    public int getLineLength() {
        return lineLength;
    }

    /**
     * get the position of a line (in the uncompressed file)
     *
     * @return position of last line retrieved by next()
     */
    public long getLinePosition() {
        return linePosition;
    }

    /**
     * get current position in (uncompressed) file. Equals file length, once getLetterCodeIterator has completed
     *
     * @return current position
     */
    public long getPosition() {
        return position;
    }

    @Override
    public void remove() {

    }

    @Override
    public void close() {
        try {
            reader.close();
        } catch (IOException e) {
            Basic.caught(e);
        }
    }

    @Override
    public long getMaximumProgress() {
        return maxProgress;
    }

    @Override
    public long getProgress() {
        return position;
    }
}
