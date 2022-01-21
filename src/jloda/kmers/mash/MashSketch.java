/*
 *  MashSketch.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.kmers.mash;

import jloda.kmers.bloomfilter.BloomFilter;
import jloda.seq.SequenceUtils;
import jloda.thirdparty.MurmurHash;
import jloda.util.*;
import jloda.util.progress.ProgressListener;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * a Mash sketch
 * Daniel Huson, 1.2019
 */
public class MashSketch {
    public static int MAGIC_INT = 1213415757; // 1213415757

    private final int sketchSize;
    private final int kSize;
    private final String name;
    private final boolean isNucleotides;

    private long[] hashValues;
    private byte[][] kmers;

    /**
     * construct a new sketch
     *
     * @param sketchSize
     * @param kMerSize
     * @param name
     * @param isNucleotides
     */
    public MashSketch(int sketchSize, int kMerSize, String name, boolean isNucleotides) {
        this.sketchSize = sketchSize;
        this.kSize = kMerSize;
        this.name = name;
        this.isNucleotides = isNucleotides;
    }

    /**
     * compute a mash sketch
     */
    public static MashSketch compute(String name, Collection<byte[]> sequences, boolean isNucleotides, int sketchSize, int kMerSize, int seed, boolean filterUniqueKMers, ProgressListener progress) {
        return compute(name, sequences, isNucleotides, sketchSize, kMerSize, seed, filterUniqueKMers, false, progress);
    }

    /**
     * compute a mash sketch
     */
    public static MashSketch compute(String name, Collection<byte[]> sequences, boolean isNucleotides, int sketchSize, int kMerSize, int seed, boolean filterUniqueKMers, boolean saveKMers, ProgressListener progress) {
        final MashSketch sketch = new MashSketch(sketchSize, kMerSize, name, isNucleotides);

        final TreeSet<Long> sortedSet = new TreeSet<>();
        sortedSet.add(Long.MAX_VALUE);

        final Map<Long, byte[]> hash2kmer = saveKMers ? new HashMap<>() : null;

        final BloomFilter bloomFilter;
        if (filterUniqueKMers)
            bloomFilter = new BloomFilter(sequences.stream().mapToInt(s -> s.length).sum(), 500000000);
        else
            bloomFilter = null;

        try {
            final byte[] kMer = new byte[kMerSize]; // will reuse
            final byte[] kMerReverseComplement = new byte[kMerSize]; // will reuse

            for (byte[] sequence : sequences) {
                final int top = sequence.length - kMerSize;
                for (int offset = 0; offset < top; offset++) {
                    if (isNucleotides) {
						final int ambiguousPos = StringUtils.lastIndexOf(sequence, offset, kMerSize, 'N'); // don't use k-mers with ambiguity letters
                        if (ambiguousPos != -1) {
                            offset = ambiguousPos; // skip to last ambiguous so that increment will move past
                            continue;
                        }
                    }

                    SequenceUtils.getSegment(sequence, offset, kMerSize, kMer);

                    final byte[] kMerUse;
                    if (isNucleotides) {
                        SequenceUtils.getReverseComplement(sequence, offset, kMerSize, kMerReverseComplement);

                        if (SequenceUtils.compare(kMer, kMerReverseComplement) <= 0) {
                            kMerUse = kMer;
                        } else {
                            kMerUse = kMerReverseComplement;
                        }
                    } else
                        kMerUse = kMer;

                    if (bloomFilter != null && bloomFilter.add(kMerUse)) {
                        continue; // first time we have seen this k-mer
                    }

                    final long hash = MurmurHash.hash64(kMerUse, 0, kMerSize, seed);

                    //  final long hash=(use64Bits? NTHash.NTP64(seqUse,kMerSize,offsetUse):MASK_32BIT&NTHash.NTP64(seqUse,kMerSize,offsetUse));

                    if (hash < sortedSet.last()) {
                        if (hash2kmer == null) {
                            if (sortedSet.add(hash) && sortedSet.size() > sketchSize)
                                sortedSet.pollLast();
                        } else {
                            if (sortedSet.add(hash)) {
                                hash2kmer.put(hash, kMerUse.clone());
                                if (sortedSet.size() > sketchSize) {
                                    Long removedHash = sortedSet.pollLast();
                                    if (removedHash != null)
                                        hash2kmer.remove(removedHash);
                                }
                            }
                        }
                    }
                    progress.checkForCancel();
                }
            }
            {
                if (sortedSet.contains(Long.MAX_VALUE)) {
                    sortedSet.remove(Long.MAX_VALUE);
                    System.err.printf("Warning: Computing sketch %s: Too few k-mers: %,d of %,d%n", sketch.getName(), sortedSet.size(), sketchSize);
                }
                sketch.hashValues = new long[sortedSet.size()];
                int pos = 0;
                for (Long value : sortedSet)
                    sketch.hashValues[pos++] = value;
            }
            progress.incrementProgress();
        } catch (CanceledException ignored) {
        }

        if (saveKMers && hash2kmer != null) {
            sketch.kmers = new byte[hash2kmer.size()][];
            int i = 0;
            for (byte[] kmer : hash2kmer.values()) {
                sketch.kmers[i++] = kmer;
            }
        }
        progress.reportTaskCompleted();
        return sketch;
    }

    public String getHeader() {
        return String.format("##ComputeMashSketch name='%s' sketchSize=%d kSize=%d type=%s\n", name, sketchSize, kSize, isNucleotides ? "nucl" : "aa");
    }

    public String toString() {
        return String.format("name='%s' sketchSize=%d kSize=%d type=%s", name, sketchSize, kSize, isNucleotides ? "nucl" : "aa");
    }

    public int getSketchSize() {
        return sketchSize;
    }

    public int getkSize() {
        return kSize;
    }

    public String getName() {
        return name;
    }

    public boolean isNucleotides() {
        return isNucleotides;
    }

    public static boolean canCompare(MashSketch a, MashSketch b) {
        return a.getSketchSize() == b.getSketchSize() && a.getkSize() == b.getkSize() && a.isNucleotides() == b.isNucleotides();
    }

    public String getString() {
		return String.format("s=%d k=%d:%s", sketchSize, kSize, StringUtils.toString(getValues(), ","));
    }

    public static MashSketch parse(String string) throws IOException {
        int sketchSize = NumberUtils.parseInt(StringUtils.getWordAfter("s=", string));
        int kMerSize = NumberUtils.parseInt(StringUtils.getWordAfter("k=", string));
        final String[] numbers = StringUtils.split(StringUtils.getWordAfter(":", string), ',');

        if (numbers.length != sketchSize)
            throw new IOException("Expected sketch size " + sketchSize + ", found: " + numbers.length);

        final MashSketch sketch = new MashSketch(sketchSize, kMerSize, "", true);
        sketch.hashValues = new long[numbers.length];
        for (int i = 0; i < numbers.length; i++) {
            sketch.hashValues[i] = NumberUtils.parseLong(numbers[i]);
        }
		return sketch;
    }

    public byte[] getBytes() {
        ByteOutputBuffer bytes = new ByteOutputBuffer();
        bytes.writeIntLittleEndian(MAGIC_INT);
        bytes.writeIntLittleEndian(sketchSize);
        bytes.writeIntLittleEndian(kSize);
        for (int i = 0; i < sketchSize; i++) {
            bytes.writeLongLittleEndian(hashValues[i]);
        }
        return bytes.copyBytes();
    }

    public static MashSketch parse(byte[] bytes) throws IOException {
        final ByteInputBuffer buffer = new ByteInputBuffer(bytes);

        if (buffer.readIntLittleEndian() != MAGIC_INT)
            throw new IOException("Incorrect magic number");
        int sketchSize = buffer.readIntLittleEndian();
        int kMerSize = buffer.readIntLittleEndian();

        final MashSketch sketch = new MashSketch(sketchSize, kMerSize, "", true);
        sketch.hashValues = new long[sketchSize];
        for (int i = 0; i < sketchSize; i++) {
            sketch.hashValues[i] = buffer.readLongLittleEndian();
        }
        return sketch;
    }

    public String getKMersString() {
        final StringBuilder buf = new StringBuilder();
        if (kmers != null) {
            for (byte[] kmer : kmers) {
				buf.append(StringUtils.toString(kmer)).append("\n");
            }
        }
        return buf.toString();
    }

    public byte[][] getKmers() {
        return kmers;
    }

    public long[] getValues() {
        return hashValues;
    }

    public long getValue(int i) {
        return hashValues[i];
    }
}
