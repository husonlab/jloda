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
