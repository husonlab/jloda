/*
 * ByteInputBuffer.java Copyright (C) 2022 Daniel H. Huson
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
 * simple byte input buffer
 * Daniel Huson, 8.2015
 */
public class ByteInputBuffer {
    private int size; // current size
    private byte[] bytes = new byte[0];

    private int pos = 0; // position during read

    /**
     * constructor
     */
    public ByteInputBuffer() {
    }

    /**
     * constructor
     *
     * @param bytes load these bytes into the buffer
     */
    public ByteInputBuffer(byte[] bytes) {
        setSize(bytes.length);
        System.arraycopy(bytes, 0, getBytes(), 0, bytes.length);
    }

    /**
     * set the size of the buffer
     *
	 */
    public void setSize(int size) {
        ensureSize(size);
        this.size = size;
    }

    /**
     * get bytes
     *
     * @return bytes
     */
    public byte[] getBytes() {
        return bytes;
    }

    /**
     * get size
     *
     * @return size
     */
    public int size() {
        return size;
    }

    public void rewind() {
        pos = 0;
    }

    public int getPosition() {
        return pos;
    }

    /**
     * read a single byte
     *
     * @return byte
     */
    public int read() {
        return bytes[pos++] & 0xFF;
    }

    /**
     * read int, little endian
     */
    public int readIntLittleEndian() {
        return (((int) bytes[pos++] & 0xFF))
                | (((int) bytes[pos++] & 0xFF) << 8)
                | (((int) bytes[pos++] & 0xFF) << 16)
                | (((int) bytes[pos++]) << 24);
    }

    /**
     * read long, little endian
     */
    public long readLongLittleEndian() {
        return (((long) bytes[pos++] & 0xFF))
                | (((long) bytes[pos++] & 0xFF) << 8)
                | (((long) bytes[pos++] & 0xFF) << 16)
                | (((long) bytes[pos++] & 0xFF) << 24)
                | (((long) bytes[pos++] & 0xFF) << 32)
                | (((long) bytes[pos++] & 0xFF) << 40)
                | (((long) bytes[pos++] & 0xFF) << 48)
                | (((long) bytes[pos++] & 0xFF) << 56);
    }

    private int readCharLittleEndian() {
        return (((int) bytes[pos++] & 0xFF) | (((int) bytes[pos++] & 0xFF) << 8)) & 0xFFFF;
    }

    public int readCharBigEndian() {
        return ((((int) bytes[pos++] & 0xFF) << 8) | ((int) bytes[pos++] & 0xFF)) & 0xFFFF;
    }

    public byte[] readBytesNullTerminated() {
        int b = pos;
        while (b < size && bytes[b] != 0)
            b++;
        byte[] result = new byte[b - pos];
        if (b > pos) {
            System.arraycopy(bytes, pos, result, 0, b - pos);
            pos = b;
        }
        return result;
    }

    public byte[] readBytes(int size) {
        final byte[] result = new byte[size];
        System.arraycopy(bytes, pos, result, 0, size);
        pos += size;
        return result;
    }

    /**
     * read packed value
     *
     * @param kind 0=byte, 1=char, 2=int
     * @return value
     */
    public int readPacked(int kind) {
        return switch (kind) {
            case 0 -> // byte
                    read();
            case 1 -> // char:
                    readCharLittleEndian();
            case 2 -> // int
                    readIntLittleEndian();
            default -> throw new RuntimeException("unknown kind");
        };
    }

    private void ensureSize(int n) {
        if (bytes.length <= n) {
            final byte[] tmp = new byte[Math.max(n, Math.min(Basic.MAX_ARRAY_SIZE, 2 * bytes.length))];
            System.arraycopy(bytes, 0, tmp, 0, size);
            bytes = tmp;
        }
    }

    public String toString() {
        return StringUtils.toString(bytes, 0, size);
    }

    public static int readIntLittleEndian(byte[] bytes) {
        return (((int) bytes[0] & 0xFF)) | (((int) bytes[1] & 0xFF) << 8) | (((int) bytes[2] & 0xFF) << 16) | (((int) bytes[3]) << 24);
    }

}
