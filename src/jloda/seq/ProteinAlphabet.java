/*
 * ProteinAlphabet.java Copyright (C) 2022 Daniel H. Huson
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
 * protein alphabet
 * Daniel Huson, 2014
 */
public class ProteinAlphabet extends Alphabet {
    private static ProteinAlphabet instance;

    /**
     * gets the single instance of the protein alphabet
     *
     * @return instance
     */
    public static ProteinAlphabet getInstance() {
        if (instance == null)
            instance = new ProteinAlphabet();
        return instance;
    }

    /**
     * constructor
     */
    private ProteinAlphabet() {
        super("A C D E F G H I K L M N P Q R S T V W Y", (byte) 'X');
    }
}
