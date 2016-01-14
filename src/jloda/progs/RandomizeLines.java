/**
 * RandomizeLines.java 
 * Copyright (C) 2016 Daniel H. Huson
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
package jloda.progs;

import jloda.util.Basic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * randomize all lines of input, end input with '.'
 * Daniel Huson, 5.2009
 */
public class RandomizeLines {
    static public void main(String[] args) throws IOException {
        List lines = new LinkedList();


        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
        String aLine;
        while ((aLine = r.readLine()) != null) {
            if (aLine.startsWith("."))
                break;
            lines.add(aLine);
        }
        for (Iterator it = Basic.randomize(lines.iterator(), 666); it.hasNext(); ) {
            System.out.println(it.next());
        }
    }
}
