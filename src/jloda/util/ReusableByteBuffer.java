/*
 *  ReusableByteBuffer.java Copyright (C) 2019 Daniel H. Huson
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

/**
 * a reusable byte buffer
 * Daniel Huson, 8.2014
 */
public class ReusableByteBuffer {
    private byte[] bytes;
    private int pos = 0;

    /**
     * constructor
     *
     * @param size
     */
    public ReusableByteBuffer(int size) {
        bytes = new byte[size];
    }

    /**
     * write string
     *
     * @param str
     */
    public void writeAsAscii(String str) {
        if (pos + str.length() >= bytes.length) {
            bytes = resize(bytes, pos + str.length() + 1024);
        }
        for (int i = 0; i < str.length(); i++) {
            bytes[pos++] = (byte) str.charAt(i);
        }
    }

    /**
     * write bytes
     *
     * @param add
     */
    public void write(byte[] add) {
        if (pos + add.length >= bytes.length) {
            bytes = resize(bytes, pos + add.length + 1024);
        }
        System.arraycopy(add, 0, bytes, pos, add.length);
        pos += add.length;
    }

    /**
     * write char as byte
     *
     * @param add
     */
    public void write(char add) {
        if (pos + 1 >= bytes.length) {
            bytes = resize(bytes, pos + 1024);
        }
        bytes[pos++] = (byte) add;
    }

    /**
     * write byte
     *
     * @param add
     */
    public void write(byte add) {
        if (pos + 1 >= bytes.length) {
            bytes = resize(bytes, pos + 1024);
        }
        bytes[pos++] = add;
    }

    /**
     * write bytes
     *
     * @param add
     * @param offset
     * @param length
     */
    public void write(byte[] add, int offset, int length) {
        if (pos + length >= bytes.length) {
            bytes = resize(bytes, pos + length + 1024);
        }
        System.arraycopy(add, offset, bytes, pos, length);
        pos += length;
    }

    /**
     * erase
     */
    public void reset() {
        pos = 0;
    }

    /**
     * return a copy of the byte buffer
     *
     * @return copy
     */
    public byte[] makeCopy() {
        byte[] result = new byte[pos];
        System.arraycopy(bytes, 0, result, 0, pos);
        return result;
    }

    private byte[] resize(byte[] array, int newSize) {
        byte[] result = new byte[newSize];
        System.arraycopy(array, 0, result, 0, Math.min(newSize, array.length));
        return result;
    }
}
