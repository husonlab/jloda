/*
 * ByteOutputBuffer.java Copyright (C) 2022 Daniel H. Huson
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

/**
 * simple byte buffer
 * Daniel Huson, 8.2015
 */
public class ByteOutputBuffer {
    private int size;
    private byte[] bytes;

    /**
     * constructor
     */
    public ByteOutputBuffer() {
        this(100);
    }


    /**
     * clear
     */
    public void clear() {
        size = 0;
    }

    /**
     * constructor
     */
    public ByteOutputBuffer(int initialCapacity) {
        size = 0;
        bytes = new byte[initialCapacity];
    }

    /**
     * get size
     *
     * @return size
     */
    public int size() {
        return size;
    }


    public void write(byte b) {
        ensureSize(size + 1);
        bytes[size++] = b;
    }

    public void write(byte[] b) {
        ensureSize(size + b.length);
        System.arraycopy(b, 0, bytes, size, b.length);
        size += b.length;
    }

    public void writeString(String str) {
        byte[] b = str.getBytes();
        ensureSize(size + b.length);
        System.arraycopy(b, 0, bytes, size, b.length);
        size += b.length;
    }

    private void ensureSize(int n) {
        if (bytes.length <= n) {
            byte[] tmp = new byte[Math.min(Basic.MAX_ARRAY_SIZE, Math.max(n, 2 * bytes.length))];
            System.arraycopy(bytes, 0, tmp, 0, size);
            bytes = tmp;
        }
    }

    /**
     * write int, little endian
     */
    public void writeIntLittleEndian(int a) {
        ensureSize(size + 4);
        bytes[size++] = ((byte) (a));
        bytes[size++] = ((byte) (a >> 8));
        bytes[size++] = ((byte) (a >> 16));
        bytes[size++] = ((byte) (a >> 24));
    }

    /**
     * write long, little endian
     */
    public void writeLongLittleEndian(long a) {
        ensureSize(size + 8);
        bytes[size++] = ((byte) (a));
        bytes[size++] = ((byte) (a >> 8));
        bytes[size++] = ((byte) (a >> 16));
        bytes[size++] = ((byte) (a >> 24));
        bytes[size++] = ((byte) (a >> 32));
        bytes[size++] = ((byte) (a >> 40));
        bytes[size++] = ((byte) (a >> 48));
        bytes[size++] = ((byte) (a >> 56));
    }

    /**
     * write double, little endian
     */
    public void writeDoubleLittleEndian(double a) {
        writeLongLittleEndian(Double.doubleToRawLongBits(a));
    }

    public static byte[] getIntLittleEndian(int a) {
        return new byte[]{((byte) (a)), ((byte) (a >> 8)), ((byte) (a >> 16)), ((byte) (a >> 24))};
    }

    public static byte[] getLongLittleEndian(long a) {
        return new byte[]{((byte) (a)), ((byte) (a >> 8)), ((byte) (a >> 16)), ((byte) (a >> 24)),
                ((byte) (a >> 32)), ((byte) (a >> 40)), ((byte) (a >> 48)), ((byte) (a >> 56))};
    }

    public void rewind() {
        size = 0;
    }

    public String toString() {
        return StringUtils.toString(bytes, 0, size);
    }

    public byte[] copyBytes() {
        byte[] result = new byte[size];
        System.arraycopy(bytes, 0, result, 0, size);
        return result;
    }

    public byte[] getBytes() {
        return bytes;
    }
}
