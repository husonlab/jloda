/**
 * Lines2FastA.java 
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
import jloda.util.UsageException;

import java.io.*;

/**
 * converts lines of sequences into fastA format
 * Daniel Huson, 10.2010
 */
public class Lines2FastA {
    /**
     * run the tool
     *
     * @param args
     * @throws UsageException
     * @throws IOException
     */
    public static void main(String[] args) throws UsageException, IOException {
        if (args.length == 0)
            args = new String[]{"-i", "/Users/huson/SecondExpt_Batch1Samples_Peptides_in_in-house_DB.txt",
                    "-o", "/Users/huson/data/megan/sludge-peptides/SecondExpt_Batch1Samples_Peptides_in_in-house_DB.fasta",
                    "-p", "peptide"
            };

        CommandLineOptions options = new CommandLineOptions(args);
        options.setDescription("Convert lines of sequence to FastA");


        String infile = options.getOption("-i", "Input file", "");
        String outfile = options.getOption("-o", "Output file", "");
        int firstId = options.getOption("-n", "Numbering start", 1);
        String prefix = options.getOption("-p", "Read name prefix", "r");
        options.done();

        BufferedReader r;
        BufferedWriter w;
        if (infile.length() == 0) {
            r = (new BufferedReader(new InputStreamReader(System.in)));
        } else {
            r = (new BufferedReader(new FileReader(infile)));
        }
        if (outfile.length() == 0) {
            w = (new BufferedWriter(new OutputStreamWriter(System.out)));
        } else {
            w = (new BufferedWriter(new FileWriter(outfile)));
        }

        String aLine;
        int count = 0;
        while ((aLine = r.readLine()) != null) {
            aLine = aLine.trim();
            if (aLine.length() == 0 || aLine.startsWith("#"))
                continue;
            w.write(">" + prefix + (firstId + count) + "\n" + aLine + "\n");
            count++;
        }
        w.close();
        r.close();
        System.err.println("Done (" + count + ")");
    }
}
