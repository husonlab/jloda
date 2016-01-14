/**
 * Gunzip.java 
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

import jloda.util.UsageException;

import java.io.*;
import java.util.zip.GZIPInputStream;

/**
 * gunzip a file
 * Daniel Huson, 10.2008
 */
public class Gunzip {
    public static void main(String[] args) throws UsageException, IOException {
        if (args.length != 2)
            throw new UsageException("gunzip infile outfile");

        BufferedReader r = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(args[0]))));

        BufferedWriter w = new BufferedWriter(new FileWriter(new File(args[1])));
        String aLine;
        while ((aLine = r.readLine()) != null) {
            w.write(aLine + "\n");
        }
        r.close();
        w.close();
    }
}
