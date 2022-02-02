/*
 * BloomFilter.java Copyright (C) 2022 Daniel H. Huson
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

import jloda.thirdparty.MurmurHash;
import jloda.util.ByteInputBuffer;
import jloda.util.ByteOutputBuffer;
import jloda.util.NumberUtils;
import jloda.util.StringUtils;

import java.io.IOException;
import java.util.Collection;

/**
 * implementation of a Bloom filter
 * See https://en.wikipedia.org/wiki/Bloom_filter
 * Daniel Huson, 1.2019
 */
public class BloomFilter {
    public static final int MAGIC_INT = 1179405634; // BMFL
    private final int bitsPerItem;
    private final int numberOfHashFunctions;
    private final LongBitSet bitSet;
    private final long totalBits;
    private final long hashBits;
    private int itemsAdded = 0;

    /**
     * basic constructor
     *
     * @param totalBits             the total number of bits to use
     * @param bitsPerItem           bits per item
     * @param numberOfHashFunctions the number of hash functions to use
     */
    public BloomFilter(long totalBits, int bitsPerItem, int numberOfHashFunctions) {
        this.bitsPerItem = bitsPerItem;
        this.numberOfHashFunctions = numberOfHashFunctions;
        this.totalBits = totalBits;
        this.hashBits = totalBits - 1;
        this.bitSet = new LongBitSet(totalBits);
    }

    /**
     * Constructor for expected number of items and total size to use
     *
     * @param totalNumberOfBytes    bytes to use
     */
    public BloomFilter(int expectedNumberOfItems, int totalNumberOfBytes) {
        this(expectedNumberOfItems, -1.0, totalNumberOfBytes);
    }

    /**
     * constructor for expected number of items and max false positive probability
     */
    public BloomFilter(int expectedNumberOfItems, double falsePositiveProbability) {
        this(expectedNumberOfItems, -falsePositiveProbability, -1);
    }

    /**
     * constructor for expected number of items and max false positive probability and max number of bytes
     */
    public BloomFilter(int expectedNumberOfItems, double falsePositiveProbability, int maxNumberOfBytes) {
        if (useMaxNumberOfBytes(expectedNumberOfItems, falsePositiveProbability, maxNumberOfBytes)) {
            this.bitsPerItem = Math.min(128, (int) Math.ceil((8d * maxNumberOfBytes) / expectedNumberOfItems));
            this.numberOfHashFunctions = (int) Math.ceil(bitsPerItem * Math.log(2));
            this.totalBits = ceilingPowerOf2((long) Math.ceil(expectedNumberOfItems * bitsPerItem));
            this.hashBits = totalBits - 1;
            this.bitSet = new LongBitSet(totalBits);
        } else {
            if (falsePositiveProbability <= 0 || falsePositiveProbability >= 1) {
                System.err.print("Warning: invalid falsePositiveProbability=" + falsePositiveProbability + ", changed to: 0.0001");
                falsePositiveProbability = 0.0001;
            }
            this.bitsPerItem = (int) Math.ceil(-Math.log(falsePositiveProbability) / (Math.log(2) * Math.log(2))); // m/n = -(log_2(p)/ln(2)) = -(ln(p)/(ln(2)*ln(2))
            this.totalBits = ceilingPowerOf2((long) Math.ceil(expectedNumberOfItems * bitsPerItem));
            this.numberOfHashFunctions = (int) (Math.ceil(-Math.log(falsePositiveProbability) / Math.log(2))); //  k = -ln(p)/(ln(2)
            this.hashBits = totalBits - 1;
            this.bitSet = new LongBitSet(totalBits);
        }
    }

    /**
     * determine whether to use max number of bytes, rather than false positive probability, to initialize
     *
     * @return true, if and only if using falsePositiveProbability would use more than max number of bytes
     */
    private static boolean useMaxNumberOfBytes(int expectedNumberOfItems, double falsePositiveProbability, int maxNumberOfBytes) {
        if (falsePositiveProbability <= 0)
            return true;
        else if (maxNumberOfBytes <= 0)
            return false;
        final int bitsPerItemForFalsePositiveProbability = (int) Math.ceil(-Math.log(falsePositiveProbability) / (Math.log(2) * Math.log(2)));
        final int bitsPerItemForMaxNumberOfBytes = Math.min(128, (int) Math.ceil((8d * maxNumberOfBytes) / expectedNumberOfItems));
        return bitsPerItemForMaxNumberOfBytes < bitsPerItemForFalsePositiveProbability;

    }

    public String toString() {
        return String.format("Bloom filter %,d items added", itemsAdded);
    }

    private static long ceilingPowerOf2(long value) {
        if (value <= 0L)
            return 0L;
        else
            return 1L << (long) Math.ceil(Math.log(value) / Math.log(2));
    }

    /**
     * adds a string
     */
    public boolean add(byte[] string) {
        return add(string, 0, string.length);
    }

    public int addAll(Collection<byte[]> strings) {
        int count = 0;
        for (byte[] string : strings) {
            if (add(string, 0, string.length))
                count++;
        }
        return count;
    }

    public int addAll(byte[]... strings) {
        int count = 0;
        for (byte[] string : strings) {
            if (add(string, 0, string.length))
                count++;
        }
        return count;
    }

    /**
     * adds a string
     *
     * @return true, if definitely newly added
     */
    public synchronized boolean add(byte[] string, int offset, int length) {
        boolean definitelyAdded = false;
        for (int i = 0; i < numberOfHashFunctions; i++) {
            long hash = Math.abs(MurmurHash.hash64(string, offset, length, i));
            if (bitSet.add(hash & hashBits))
                definitelyAdded = true;
        }
        itemsAdded++;
        return definitelyAdded;
    }

    /**
     * adds a string
     *
     * @return true, if definitely not previously added
     */
    public boolean isContainedProbably(byte[] string) {
        for (int i = 0; i < numberOfHashFunctions; i++) {
            long hash = Math.abs(MurmurHash.hash64(string, 0, string.length, i));
            if (!bitSet.contains(hash & hashBits))
                return false;
        }
        return true;
    }

    public double expectedFalsePositiveRate() {
        return Math.pow((1 - Math.exp(-numberOfHashFunctions * (double) itemsAdded / (double) totalBits)), numberOfHashFunctions);
    }

    public int cardinality() {
        return itemsAdded;
    }

    public String getString() {
		return String.format("b=%d i=%d h=%d a=%d:%s", totalBits, bitsPerItem, numberOfHashFunctions, itemsAdded, StringUtils.toString(bitSet.getBits(), ","));
    }

    public static BloomFilter parseString(String string) {
        long totalBits = NumberUtils.parseLong(StringUtils.getWordAfter("b=", string));
        int bitsPerItem = NumberUtils.parseInt(StringUtils.getWordAfter("i=", string));
        int numberOfHashFunctions = NumberUtils.parseInt(StringUtils.getWordAfter("h=", string));
        int itemsAdded = NumberUtils.parseInt(StringUtils.getWordAfter("a=", string));
        final BloomFilter bloomFilter = new BloomFilter(totalBits, bitsPerItem, numberOfHashFunctions);
        bloomFilter.itemsAdded = itemsAdded;
        String[] numbers = StringUtils.split(StringUtils.getWordAfter(":", string), ',');
        for (int i = 0; i < numbers.length; i++)
            bloomFilter.bitSet.getBits()[i] = NumberUtils.parseLong(numbers[i]);
        return bloomFilter;
    }

    public byte[] getBytes() {
        final ByteOutputBuffer buffer = new ByteOutputBuffer();
        buffer.writeIntLittleEndian(MAGIC_INT);
        buffer.writeLongLittleEndian(totalBits);
        buffer.writeIntLittleEndian(bitsPerItem);
        buffer.writeIntLittleEndian(numberOfHashFunctions);
        buffer.writeIntLittleEndian(itemsAdded);
        buffer.write(bitSet.getBytes());
        return buffer.copyBytes();
    }

    public static BloomFilter parseBytes(byte[] bytes) throws IOException {
        final ByteInputBuffer buffer = new ByteInputBuffer(bytes);
        if (buffer.readIntLittleEndian() != MAGIC_INT)
            throw new IOException("Incorrect magic number");

        final long totalBits = buffer.readLongLittleEndian();
        final int bitsPerItem = buffer.readIntLittleEndian();
        final int numberOfHashFunctions = buffer.readIntLittleEndian();
        final int itemsAdded = buffer.readIntLittleEndian();
        final BloomFilter bloomFilter = new BloomFilter(totalBits, bitsPerItem, numberOfHashFunctions);
        bloomFilter.itemsAdded = itemsAdded;
        bloomFilter.bitSet.copy(LongBitSet.parseBytes(buffer));
        return bloomFilter;
    }

    public int countContainedProbably(Iterable<String> queries) {
        int count = 0;
        for (String query : queries) {
            if (isContainedProbably(query.getBytes()))
                count++;
        }
        return count;
    }
}
