/*
 * SequenceUtils.java Copyright (C) 2019. Daniel H. Huson
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

import java.io.StringWriter;

/**
 * some utilities for DNA and amino acid sequences
 * Daniel Huson, 9.2011
 */
public class SequenceUtils {
    private final static byte[][][] codon2aminoAcid = new byte[127][127][127];

    static {
        // initialize the codon2aminoAcid table
        String nucleotides = "actgACGTuUN-";
        for (int i = 0; i < nucleotides.length(); i++) {
            char a = nucleotides.charAt(i);
            for (int j = 0; j < nucleotides.length(); j++) {
                char b = nucleotides.charAt(j);
                for (int k = 0; k < nucleotides.length(); k++) {
                    char c = nucleotides.charAt(k);
                    codon2aminoAcid[(int) a][(int) b][(int) c] = getAminoAcidInit(a, b, c);
                }
            }
        }
    }

    /**
     * translate DNA into amino acids
     *
     * @param c1
     * @param c2
     * @param c3
     * @return amino acid
     */
    static private byte getAminoAcidInit(int c1, int c2, int c3) {
        c1 = Character.toUpperCase(c1);
        if (c1 == 'T')
            c1 = 'U';
        c2 = Character.toUpperCase(c2);
        if (c2 == 'T')
            c2 = 'U';
        c3 = Character.toUpperCase(c3);
        if (c3 == 'T')
            c3 = 'U';

        if (c1 == '-' || c2 == '-' || c3 == '-')
            return '-';

        switch (c1) {
            case 'U':
                switch (c2) {
                    case 'U':
                        switch (c3) {
                            case 'U':
                            case 'C':
                                return 'F';
                            case 'A':
                                return 'L';
                            case 'G':
                                return 'L';
                            default:
                                return 'X';
                        }
                    case 'C':
                        switch (c3) {
                            case 'U':
                                return 'S';
                            case 'C':
                                return 'S';
                            case 'A':
                                return 'S';
                            case 'G':
                                return 'S';
                            default:
                                return 'S';
                        }
                    case 'A':
                        switch (c3) {
                            case 'U':
                                return 'Y';
                            case 'C':
                                return 'Y';
                            case 'A':
                                return '*';
                            case 'G':
                                return '*';
                            default:
                                return 'X';
                        }
                    case 'G':
                        switch (c3) {
                            case 'U':
                                return 'C';
                            case 'C':
                                return 'C';
                            case 'A':
                                return '*';
                            case 'G':
                                return 'W';
                            default:
                                return 'X';
                        }
                    default:
                        return 'X';
                }
            case 'C':
                switch (c2) {
                    case 'U':
                        switch (c3) {
                            case 'U':
                                return 'L';
                            case 'C':
                                return 'L';
                            case 'A':
                                return 'L';
                            case 'G':
                                return 'L';
                            default:
                                return 'L';
                        }
                    case 'C':
                        switch (c3) {
                            case 'U':
                                return 'P';
                            case 'C':
                                return 'P';
                            case 'A':
                                return 'P';
                            case 'G':
                                return 'P';
                            default:
                                return 'P';
                        }
                    case 'A':
                        switch (c3) {
                            case 'U':
                                return 'H';
                            case 'C':
                                return 'H';
                            case 'A':
                                return 'Q';
                            case 'G':
                                return 'Q';
                            default:
                                return 'X';
                        }
                    case 'G':
                        switch (c3) {
                            case 'U':
                                return 'R';
                            case 'C':
                                return 'R';
                            case 'A':
                                return 'R';
                            case 'G':
                                return 'R';
                            default:
                                return 'R';
                        }
                    default:
                        return 'X';
                }
            case 'A':
                switch (c2) {
                    case 'U':
                        switch (c3) {
                            case 'U':
                                return 'I';
                            case 'C':
                                return 'I';
                            case 'A':
                                return 'I';
                            case 'G':
                                return 'M';
                            default:
                                return 'X';
                        }
                    case 'C':
                        switch (c3) {
                            case 'U':
                                return 'T';
                            case 'C':
                                return 'T';
                            case 'A':
                                return 'T';
                            case 'G':
                                return 'T';
                            default:
                                return 'T';
                        }
                    case 'A':
                        switch (c3) {
                            case 'U':
                                return 'N';
                            case 'C':
                                return 'N';
                            case 'A':
                                return 'K';
                            case 'G':
                                return 'K';
                            default:
                                return 'X';
                        }
                    case 'G':
                        switch (c3) {
                            case 'U':
                                return 'S';
                            case 'C':
                                return 'S';
                            case 'A':
                                return 'R';
                            case 'G':
                                return 'R';
                            default:
                                return 'X';
                        }
                    default:
                        return 'X';
                }
            case 'G':
                switch (c2) {
                    case 'U':
                        switch (c3) {
                            case 'U':
                                return 'V';
                            case 'C':
                                return 'V';
                            case 'A':
                                return 'V';
                            case 'G':
                                return 'V';
                            default:
                                return 'V';
                        }
                    case 'C':
                        switch (c3) {
                            case 'U':
                                return 'A';
                            case 'C':
                                return 'A';
                            case 'A':
                                return 'A';
                            case 'G':
                                return 'A';
                            default:
                                return 'A';
                        }
                    case 'A':
                        switch (c3) {
                            case 'U':
                                return 'D';
                            case 'C':
                                return 'D';
                            case 'A':
                                return 'E';
                            case 'G':
                                return 'E';
                            default:
                                return 'X';
                        }
                    case 'G':
                        switch (c3) {
                            case 'U':
                                return 'G';
                            case 'C':
                                return 'G';
                            case 'A':
                                return 'G';
                            case 'G':
                                return 'G';
                            default:
                                return 'G';
                        }
                    default:
                        return 'X';
                }
            default:
                return 'X';
        }
    }

    /**
     * gets the amino acid for the codon starting the given position
     *
     * @param sequence
     * @param pos
     * @return amino acid
     */
    static public byte getAminoAcid(byte[] sequence, int pos) {
        /*
        byte result=getAminoAcid(sequence[pos], sequence[pos + 1], sequence[pos + 2]);
        System.err.println(String.format("%c%c%c -> %c",sequence[pos],sequence[pos+1],sequence[pos+2],result));
        return result;
        */
        return getAminoAcid(sequence[pos], sequence[pos + 1], sequence[pos + 2]);
    }

    /**
     * gets the amino acid for the codon starting at the given position in the reverse strand.
     * To get the amino acid sequence of the reverse strand of DNA using this method,
     * start at beginning of leading strand, calling this method repeatedly, building the protein sequence from the end to the beginning
     *
     * @param sequence
     * @param pos
     * @return amino acid
     */
    static public byte getAminoAcidReverse(byte[] sequence, int pos) {
        return getAminoAcid(getComplement(sequence[pos + 2]), getComplement(sequence[pos + 1]), getComplement(sequence[pos]));
    }

    /**
     * gets the amino acid for the codon a,b,cin the reverse strand.
     * To get the amino acid sequence of the reverse strand of DNA using this method,
     * start at the end of the leading strand and repeatedly call this method with letters at positions pos, pos-1, pos-2
     *
     * @param a
     * @param b
     * param c
     * @return amino acid
     */
    static public byte getAminoAcidReverse(byte a, byte b, byte c) {
        return getAminoAcid(getComplement(a), getComplement(b), getComplement(c));
    }

    /**
     * gets the amino acid for the codon starting the given position
     *
     * @param sequence
     * @param pos
     * @return amino acid
     */
    static public byte getAminoAcid(String sequence, int pos) {
        return getAminoAcid(sequence.charAt(pos), sequence.charAt(++pos), sequence.charAt(++pos));
    }

    /**
     * gets the amino acid for the codon starting at the given position in the reverse strand.
     * To get the amino acid sequence of the reverse strand of DNA using this method,
     * start at beginning of leading strand, calling this method repeatedly, building the protein sequence from the end to the beginning
     *
     * @param sequence
     * @param pos
     * @return amino acid
     */
    static public byte getAminoAcidReverse(String sequence, int pos) {
        return getAminoAcid(getComplement((byte) sequence.charAt(pos + 2)), getComplement((byte) sequence.charAt(pos + 1)), getComplement((byte) sequence.charAt(pos)));
    }

    /**
     * translate DNA into amino acids
     *
     * @param c1
     * @param c2
     * @param c3
     * @return amino acid
     */
    static public byte getAminoAcid(int c1, int c2, int c3) {
        try {
            byte aa = codon2aminoAcid[c1][c2][c3];
            if (aa != 0) {
                return aa;
            }
        } catch (Exception ex) {
        }
        return 'X';
    }

    /**
     * is this a valid nucleotide (with ambiguity codes)
     *
     * @param ch
     * @return true, if nucleotide
     */
    public static boolean isNucleotide(int ch) {
        return "atugckmryswbvhdxn".indexOf(Character.toLowerCase(ch)) != -1;
    }

    /**
     * reverse complement of string
     *
     * @param readSequence
     * @return reverse complement
     */
    public static String getReverseComplement(String readSequence) {
        StringBuilder buf = new StringBuilder();
        for (int i = readSequence.length() - 1; i >= 0; i--) {
            buf.append((char) getComplement((byte) readSequence.charAt(i)));
        }
        return buf.toString();
    }

    /**
     * gets the  complement of a nucleotide. Returns ambiguity codes unaltered.
     *
     * @param nucleotide
     * @return reverse complement
     */
    public static byte getComplement(byte nucleotide) {
        switch (nucleotide) {
            case 'a':
                return 't';
            case 'A':
                return 'T';
            case 'c':
                return 'g';
            case 'C':
                return 'G';
            case 'g':
                return 'c';
            case 'G':
                return 'C';
            case 't':
                return 'a';
            case 'T':
                return 'A';
            default:
                return nucleotide;
        }
    }

    /**
     * reverses (but does NOT complement) a sequence
     *
     * @param sequence
     * @return reverse string (but not complemented
     */
    public static String getReverse(String sequence) {
        StringWriter w = new StringWriter();
        for (int i = sequence.length() - 1; i >= 0; i--) {
            w.write(sequence.charAt(i));
        }
        return w.toString();
    }

    /**
     * reverses (but does NOT complement) a sequence
     *
     * @param sequence
     * @return reverse string (but not complemented)
     */
    public static byte[] getReverse(byte[] sequence) {
        byte[] result = new byte[sequence.length];
        for (int i = 0; i < sequence.length; i++) {
            result[i] = sequence[sequence.length - 1 - i];
        }
        return result;
    }

    /**
     * gets the reverse complement
     *
     * @param sequence
     * @return reverse complement
     */
    public static byte[] getReverseComplement(byte[] sequence) {
        byte[] result = new byte[sequence.length];
        for (int i = 0; i < sequence.length; i++) {
            result[i] = getComplement(sequence[sequence.length - 1 - i]);
        }
        return result;
    }


    /**
     * translate a DNA sequence into protein
     *
     * @param reverse
     * @param sequence
     * @return
     */
    public static byte[] translate(boolean reverse, int shift, String sequence) {
        byte[] result = new byte[(sequence.length() - shift) / 3];
        if (!reverse) {
            int pos = 0;
            for (int i = shift; i <= sequence.length() - 3; i += 3) {
                result[pos++] = getAminoAcid(sequence, i);
            }
        } else // reverse complement
        {
            int pos = 0;
            for (int i = sequence.length() - 1 - shift; i >= 2; i -= 3) {
                if (i + 2 < sequence.length())
                    result[pos++] = getAminoAcidReverse(sequence, i);
            }
        }
        return result;
    }

    /**
     * copies a string to a byte array, 0 terminated.
     * Note that the length of bytes is usually larger than the string length
     *
     * @param string
     * @param bytes
     * @return 0-terminated bytes
     */
    public static byte[] getBytes0Terminated(String string, byte[] bytes) {
        if (bytes.length < string.length() + 1)
            bytes = new byte[2 * string.length() + 1];
        for (int i = 0; i < string.length(); i++)
            bytes[i] = (byte) string.charAt(i);
        bytes[string.length()] = 0;
        return bytes;

    }

    /**
     * convert 0 terminated bytes to string
     *
     * @param bytes
     * @return string
     */
    public static String getStringFromBytes0Terminated(byte[] bytes) {
        StringBuilder buf = new StringBuilder();
        for (byte aByte : bytes) {
            if (aByte == 0)
                break;
            buf.append((char) aByte);
        }
        return buf.toString();
    }

    /**
     * counts how many times each of the given symbols have been used
     *
     * @param sequence
     * @param symbols
     * @return usage
     */
    public static int[] computeUsageCounts(byte[] sequence, byte[] symbols) {
        int[] counts = new int[symbols.length];

        for (byte b : sequence) {
            for (int j = 0; j < symbols.length; j++) {
                if (symbols[j] == b)
                    counts[j]++;
            }
        }
        return counts;
    }

    /**
     * count the number of gaps ('-') in a sequence
     *
     * @param sequence
     * @return number of gaps
     */
    public static int countGaps(String sequence) {
        int count = 0;
        for (int i = 0; i < sequence.length(); i++)
            if (sequence.charAt(i) == '-')
                count++;
        return count;
    }

    /**
     * count the number of gaps ('-') in a sequence
     *
     * @param sequence
     * @return number of gaps
     */
    public static int countGaps(byte[] sequence) {
        int count = 0;
        for (byte aSequence : sequence)
            if (aSequence == '-')
                count++;
        return count;
    }
}
