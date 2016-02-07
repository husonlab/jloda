/*
 *  Copyright (C) 2015 Daniel H. Huson
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
package jloda.map;

import jloda.util.CanceledException;
import jloda.util.ProgressPercentage;

import java.io.*;

/**
 * long file getter in memory
 * Daniel Huson, 5.2015
 */
public class LongFileGetterInMemory implements ILongGetter {
    private final long[][] data;
    private final long limit;
    private final int length0;

    /**
     * long file getter in memory
     *
     * @param file
     * @throws IOException
     * @throws CanceledException
     */
    public LongFileGetterInMemory(File file) throws IOException {
        limit = file.length() / 8;

        data = new long[(int) ((limit >> 30)) + 1][];
        length0 = (int) (Math.min(limit, 1 << 30));
        for (int i = 0; i < data.length; i++) {
            int length = Math.min(length0, dataPos(limit) + 1);
            data[i] = new long[length];
        }

        try (InputStream ins = new BufferedInputStream(new FileInputStream(file))) {
            final ProgressPercentage progress = new ProgressPercentage("Reading file: " + file, limit);
            for (long pos = 0; pos < limit; pos++) {
                data[dataIndex(pos)][dataPos(pos)] = (((long) (ins.read()) & 0xFF) << 56) + (((long) (ins.read()) & 0xFF) << 48) + (((long) (ins.read()) & 0xFF) << 40) + (((long) (ins.read()) & 0xFF) << 32)
                        + (((long) (ins.read()) & 0xFF) << 24) + (((long) (ins.read()) & 0xFF) << 16) + (((long) (ins.read()) & 0xFF) << 8) + (((long) (ins.read()) & 0xFF));
                progress.setProgress(pos);
            }
            progress.close();
        }
    }

    /**
     * gets value for given index
     *
     * @param index
     * @return value or 0
     */
    @Override
    public long get(long index) {
        return data[dataIndex(index)][dataPos(index)];
    }

    /**
     * length of array
     *
     * @return array length
     * @throws IOException
     */
    @Override
    public long limit() {
        return limit;
    }

    /**
     * close the array
     */
    @Override
    public void close() {

    }

    private int dataIndex(long index) {
        return (int) ((index >> 30));
    }

    private int dataPos(long index) {
        return (int) (index - (index >> 30) * length0);
    }
}
