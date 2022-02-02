/*
 * GuessSequenceType.java Copyright (C) 2022 Daniel H. Huson
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

import jloda.util.IFastAIterator;

import java.io.IOException;
import java.util.BitSet;

/**
 * methods for guessing the sequence type in a file
 * Daniel Huson, 1.2019
 */
public class GuessSequenceType {

    /**
     * does file contain nucleotides
     *
	 */
    public static boolean isFileContainsNucleotides(String fileName) throws IOException {
        final BitSet chars = new BitSet();
        int count = 0;

        try (IFastAIterator it = FastAFileIterator.getFastAOrFastQAsFastAIterator(fileName)) {
            loop:
            while (it.hasNext()) {
                final String sequence = it.next().getSecond();
                for (int i = 0; i < sequence.length(); i++) {
                    chars.set(Character.toUpperCase(sequence.charAt(i)));
                    count++;
                    if (count >= 5000)
                        break loop;
                }
            }
        }
        boolean result = allNucleotides(chars) && chars.get('A') && chars.get('C') && chars.get('G') && (chars.get('T') || chars.get('U'));
        if (!result) {
            System.err.println("Chars: ");
            for (int i = chars.nextSetBit(0); i != -1; i = chars.nextSetBit(i + 1)) {
                System.err.print((char) i);
            }
            System.err.println();
        }
        return result;
    }

    private static boolean allNucleotides(BitSet set) {
        final String nucleotides = "ACGHKMRSTUVWY";

        boolean ok = true;
        for (int c = set.nextSetBit(0); c != -1; c = set.nextSetBit(c + 1)) {
            if (nucleotides.indexOf(c) == -1) {
                if (ok)
                    ok = false;
                else
                    return false;
            }
        }
        return true;
    }
}

