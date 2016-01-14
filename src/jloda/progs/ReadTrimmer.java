/**
 * ReadTrimmer.java 
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

import jloda.util.CommandLineOptions;
import jloda.util.FastA;
import jloda.util.UsageException;

import java.io.*;

/**
 * trims reads to a specific size
 * Daniel Huson, 9.2009
 */
public class ReadTrimmer {
    public static void main(String[] args) throws UsageException, IOException {
        CommandLineOptions options = new CommandLineOptions(args);
        options.setDescription("ReadTrimmer - trims reads");

        String infile = options.getMandatoryOption("-i", "Input file", "");
        String outfile = options.getMandatoryOption("-o", "Output file", "");
        int start = options.getOption("-s", "Start position in read", 0);
        int length = options.getOption("-l", "Maximum length of read", 250);
        options.done();

        Reader r = new FileReader(new File(infile));

        FastA fastA = new FastA();
        fastA.read(r);
        r.close();

        for (int i = 0; i < fastA.getSize(); i++) {
            String seq = fastA.getSequence(i);
            if (start > 0 && seq.length() > start)
                seq = seq.substring(start);
            if (seq.length() > length)
                seq = seq.substring(0, length);
            fastA.set(i, fastA.getHeader(i), seq);
        }

        Writer w = new FileWriter(new File(outfile));
        fastA.write(w);
        w.close();
    }
}
