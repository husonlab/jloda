/*
 * DNA5Alphabet.java Copyright (C) 2022 Daniel H. Huson
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
 * DNA5 alphabet
 * Created by huson on 9/30/14.
 */
public class DNA5Alphabet extends Alphabet {
    private static DNA5Alphabet instance;

    final static private byte[] normalizedComplement = {
            'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N',
            'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N',
            'N', '-', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'T',
            'N', 'G', 'N', 'N', 'N', 'C', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'A', 'A', 'N', 'N',
            'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'T', 'N', 'G', 'N', 'N', 'N', 'C', 'N', 'N', 'N', 'N', 'N', 'N',
            'N', 'N', 'N', 'N', 'N', 'N', 'A', 'A', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N'
    };


    /**
     * gets the single instance of the protein alphabet
     *
     * @return instance
     */
    public static DNA5Alphabet getInstance() {
        if (instance == null)
            instance = new DNA5Alphabet();
        return instance;
    }

    /**
     * constructor
     */
    private DNA5Alphabet() {
        super("A C G TU", (byte) 'N');
    }

    /**
     * gets the reverse complement
     *
     * @param reverseComplement can be null
     * @return reverse complement
     */
    public static byte[] reverseComplement(byte[] sequence, byte[] reverseComplement) {
        if (reverseComplement == null)
            reverseComplement = new byte[sequence.length];
        for (int i = 0; i < sequence.length; i++) {
            reverseComplement[sequence.length - i - 1] = normalizedComplement[sequence[i]];
        }
        return reverseComplement;
    }

    /**
     * does normalized sequence contain an N
     *
     * @return true, if N present
     */
    public static boolean containsN(byte[] sequence, int offset, int len) {
        len += offset;
        for (int i = offset; i < len; i++) {
            if (sequence[i] == 'N')
                return true;
        }
        return false;
    }
}
