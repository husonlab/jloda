/**
 * MASampler.java 
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
import jloda.util.PhylipUtils;

import java.io.FileReader;
import java.util.Random;

/**
 * Samples fragments from a sequence multiple alignment
 *
 * @author huson
 *         Date: 13-Aug-2004
 */
public class MASampler {
    static public void main(String[] args) throws Exception {
        CommandLineOptions options = new CommandLineOptions(args);
        options.setDescription("Sample fragments from a multiple alignment");
        String cname = options.getMandatoryOption("-i", "input file in Phylip format", "");
        int meanLength = options.getOption("-m", "mean length of a fragment", 500);
        int percentStdDev = options.getOption("-d", "standard deviation in percent", 10);
        int seed = options.getOption("-s", "random number seed", 666);
        options.done();
        Random rand = new Random(seed);

        int stdDev = (int) (meanLength / 100.0 * percentStdDev);

        System.err.println("# MASampler: m=" + meanLength + " sd= " + stdDev);

        //System.err.println("# Options: " + options);

        String[][] inData = new String[2][0];
        FileReader r = new FileReader(cname);
        PhylipUtils.read(inData, r);
        int numSequences = inData[0].length - 1;
        int length = inData[1][1].length();
        System.err.println("# Input <" + cname + ">: " + numSequences + " sequences of length " + length);

        String[][] outData = new String[2][numSequences + 1];
        for (int i = 1; i <= numSequences; i++) {
            int newLength = (int) (meanLength + rand.nextGaussian() * stdDev);
            int newStart = rand.nextInt(length - newLength);
            int newStop = newStart + newLength;
            StringBuilder buf = new StringBuilder();
            for (int p = 0; p < newStart; p++)
                buf.append("-");
            buf.append(inData[1][i].substring(newStart, newStop));
            for (int p = newStop; p < length; p++)
                buf.append("-");

            outData[0][i] = inData[0][i];
            outData[1][i] = buf.toString();
        }
        PhylipUtils.print(outData, System.out);
    }
}
