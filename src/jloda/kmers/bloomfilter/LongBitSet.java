/*
 * LongBitSet.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.kmers.bloomfilter;

import jloda.util.ByteInputBuffer;
import jloda.util.ByteOutputBuffer;

import java.util.Arrays;
import java.util.Iterator;

/**
 * bit set that works for longs upto 64*max-int
 * Daniel Huson, 1.2019
 */
public class LongBitSet implements Iterable<Long> {
    private final long[] bits;
    private long cardinality = 0;

    private final Object[] sync = new Object[1024];

    /**
     * constructor
     *
	 */
    public LongBitSet(long maxCardinality) {
        bits = new long[(int) (maxCardinality / 64) + 1];
        for (int i = 0; i < sync.length; i++) {
            sync[i] = new Object();
        }
    }

    /**
     * add a bit, thread-safe
     *
     * @return true, if bit was added, false, if already present
     */
    public boolean add(long bit) {
        final int a = (int) (bit >>> 6);
        final long b = (1L << ((bit & 63L) - 1L));

        synchronized ((sync[a & 1023])) {
            try {
                if ((bits[a] & b) == 0L) {
                    bits[a] |= b;
                    cardinality++;
                    return true;
                } else
                    return false;
            } catch (IndexOutOfBoundsException ex) {
                throw new IndexOutOfBoundsException("invalid value: " + bit + " >= " + bits.length * 64);
            }
        }
    }

    /**
     * remove a bit, thread-safe
     *
     * @return true, if bit was removed, false, if not present
     */
    public boolean remove(long bit) {
        final int a = (int) (bit >>> 6);
        final long b = (1L << ((bit & 63L) - 1L));

        synchronized ((sync[a & 1023])) {
            try {
                if ((bits[a] & b) == 0L)
                    return false;
                else {
                    bits[a] &= ~b;
                    cardinality--;
                    return true;
                }
            } catch (IndexOutOfBoundsException ex) {
                throw new IndexOutOfBoundsException("invalid value: " + bit + " >= " + bits.length * 64);
            }
        }
    }

    /**
     * tests containment, thread-safe
     *
     * @return true, if contained
     */
    public boolean contains(long bit) {
        final int a = (int) (bit >>> 6);
        final long b = (1L << ((bit & 63L) - 1L));
        synchronized ((sync[a & 1023])) {
            try {
                return (bits[a] & b) != 0;
            } catch (IndexOutOfBoundsException ex) {
                throw new IndexOutOfBoundsException("invalid value: " + bit + " >= " + bits.length * 64);
            }
        }
    }

    /**
     * clear the set
     */
    public void clear() {
        Arrays.fill(bits, 0);
        cardinality = 0;
    }

    /**
     * gets the number of elements in the set
     *
     * @return number of elements
     */
    public long cardinality() {
        return cardinality;
    }

    /**
     * iterator over all members
     *
     * @return iterator
     */
    @Override
    public Iterator<Long> iterator() {
        return new Iterator<>() {
            long pos = 0;
            long count = 0;

            @Override
            public boolean hasNext() {
                return count < cardinality;
            }

            @Override
            public Long next() {
                while (!contains(pos))
                    pos++;
                count++;
                return pos++;
            }
        };
    }

    public long[] getBits() {
        return bits;
    }

    /**
     * gets a byte representation of the Bloom filter
     *
     * @return bytes
     */
    public byte[] getBytes() {
        final ByteOutputBuffer buffer = new ByteOutputBuffer();
        buffer.writeLongLittleEndian(cardinality);
        buffer.writeIntLittleEndian(bits.length);
        for (long word : bits)
            buffer.writeLongLittleEndian(word);
        return buffer.copyBytes();
    }

    /**
     * parses the bytes representation
     */
    public static LongBitSet parseBytes(ByteInputBuffer buffer) {
        final long cardinality = buffer.readLongLittleEndian();
        final int bitsLength = buffer.readIntLittleEndian();

        final LongBitSet bitset = new LongBitSet(64L * bitsLength - 1L);
        for (int i = 0; i < bitsLength; i++)
            bitset.getBits()[i] = buffer.readLongLittleEndian();
        bitset.cardinality = cardinality;
        return bitset;
    }

    public void copy(LongBitSet bitSet) {
        System.arraycopy(bitSet.getBits(), 0, this.bits, 0, bitSet.getBits().length);
        cardinality = bitSet.cardinality;
    }
}
