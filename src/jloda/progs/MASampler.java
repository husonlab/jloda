/**
 * Copyright 2015, Daniel Huson
 * Author Daniel Huson
 *(Some files contain contributions from other authors, who are then mentioned separately)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
