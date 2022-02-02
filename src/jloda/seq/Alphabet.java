/*
 * Alphabet.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.seq;

/**
 * Alphabet base class
 * <p/>
 * Created by huson on 9/30/14.
 */
public class Alphabet implements INormalizer {
    protected final byte alphabetSize;
    protected final long[] letter2code;
    protected final byte[] letter2normalized;
    protected final byte[] code2letter;
    protected final int bitsPerLetter;
    protected final int unusedBits;
    protected final int lettersPerWord;
    protected final long letterMask;
    protected final byte undefinedLetterCode;
    protected final String definitionString;

    /**
     * constructor
     *
	 */
    public Alphabet(String definitionString, byte undefinedLetter) {
        boolean isUndefinedContained = (definitionString.indexOf(undefinedLetter) != -1);
        this.definitionString = definitionString.replaceAll("\\[", "").replaceAll("]", "").replaceAll(" {2}", " ");
        String[] letterGroups = this.definitionString.split(" ");
        alphabetSize = (byte) (letterGroups.length + (isUndefinedContained ? 0 : 1));

        {
            int bits = 1;
            while (!(Math.pow(2, bits) > alphabetSize)) {
                bits++;
            }
            bitsPerLetter = bits;
        }
        letterMask = (1L << bitsPerLetter) - 1;
        lettersPerWord = 64 / bitsPerLetter;
        unusedBits = 64 - lettersPerWord * bitsPerLetter;

        //System.err.println("Alphabet: " + definitionString + " bits: " + bitsPerLetter);

        code2letter = new byte[alphabetSize + 1];

        undefinedLetterCode = alphabetSize;
        letter2code = new long[127];
        letter2normalized = new byte[127];

        for (int i = 0; i < 127; i++) {
            letter2code[i] = undefinedLetterCode;
            letter2normalized[i] = undefinedLetter;
        }
        code2letter[undefinedLetterCode] = undefinedLetter;

        int bits = 1;
        for (String letterGroup : letterGroups) {
            for (int j = 0; j < letterGroup.length(); j++) {
                int letter = Character.toLowerCase(letterGroup.charAt(j));
                letter2code[letter] = bits;
                letter = Character.toUpperCase(letterGroup.charAt(j));
                letter2code[letter] = bits;
                letter2normalized[letter] = (byte) letterGroup.charAt(0);
                if (j == 0)
                    code2letter[bits] = (byte) letter;
            }
            // System.err.println(letterGroups[i]+" -> "+Integer.toBinaryString(bits)+" -> "+(char)code2letter[bits]);
            bits++;
        }
    }

    /**
     * gets the alphabet size
     *
     * @return alphabet size
     */
    public byte getAlphabetSize() {
        return alphabetSize;
    }

    /**
     * gets the number of bits used to encode a letter
     *
     * @return number of bits
     */
    public int getBitsPerLetter() {
        return bitsPerLetter;
    }

    /**
     * gets the letter to code mapping
     *
     * @return letter to code
     */
    public long[] getLetter2Code() {
        return letter2code;
    }


    /**
     * gets the code to letter mapping
     *
     * @return code to letter
     */
    public byte[] getCode2Letter() {
        return code2letter;
    }

    /**
     * gets the mask used for a single letter
     *
     * @return letter mask
     */
    public long getLetterMask() {
        return letterMask;
    }

    /**
     * gets the number of letters per 64-bit word
     *
     * @return letters per word
     */
    public int getLettersPerWord() {
        return lettersPerWord;
    }

    /**
     * gets the number of unused bits
     *
     * @return number of unused (per 64-bit word)
     */
    public int getUnusedBits() {
        return unusedBits;
    }

    /**
     * gets the code assigned to undefined letter
     *
     * @return code
     */
    public byte getUndefinedLetterCode() {
        return undefinedLetterCode;
    }

    /**
     * gets the definition string
     *
     * @return defintion
     */
    public String getDefinitionString() {
        return definitionString;
    }

    /**
     * returns normalized letter
     *
     * @return normalized letter
     */
    @Override
    public byte getNormalized(byte letter) {
        return letter2normalized[letter];
    }
}
