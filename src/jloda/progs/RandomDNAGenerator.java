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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Random;
import java.util.StringTokenizer;

/**
 * generates random DNA
 * Daniel Huson, 1.2008
 */
public class RandomDNAGenerator {
    /**
     * generate a random DNA sequence with user-specified repeats
     *
     * @param args
     * @throws UsageException
     * @throws IOException
     */
    public static void main(String[] args) throws UsageException, IOException {
        CommandLineOptions options = new CommandLineOptions(args);
        options.setDescription("Generates Random DNA");
        options.done();


        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter length:");
        int length = Integer.parseInt(r.readLine());

        char[] sequence = makeRandomSequence(length);


        String aLine;
        System.out.println("Enter repeat length and list of start positions (or . to finish): ");

        while ((aLine = r.readLine()) != null) {
            aLine = aLine.trim();
            if (aLine.length() == 0 || aLine.startsWith("#"))
                continue;
            else if (aLine.equals("."))
                break;

            StringTokenizer tok = new StringTokenizer(aLine);

            if (tok.hasMoreTokens()) {
                int repeatLength = Integer.parseInt(tok.nextToken());
                int first = -1;
                while (tok.hasMoreTokens()) {
                    int pos = Integer.parseInt(tok.nextToken());
                    if (first == -1)
                        first = pos;
                    else {
                        for (int i = 0; i < repeatLength; i++) {
                            if (pos + i >= sequence.length)
                                break;
                            sequence[pos + i] = sequence[first + i];
                        }
                    }

                }

                System.out.println("Enter more repeats or . to finish: ");
            }
        }

        FastA fastA = new FastA();
        fastA.add("Genome", new String(sequence));
        StringWriter w = new StringWriter();
        fastA.write(w);
        System.out.println(w.toString());
    }

    /**
     * generate a random DNA sequence
     *
     * @param length
     * @return sequence
     */
    private static char[] makeRandomSequence(int length) {
        char[] sequence = new char[length];

        Random r = new Random();

        for (int i = 0; i < length; i++) {
            switch (r.nextInt(4)) {
                case 0:
                    sequence[i] = 'a';
                    break;
                case 1:
                    sequence[i] = 'c';
                    break;
                case 2:
                    sequence[i] = 'g';
                    break;
                case 3:
                    sequence[i] = 't';
                    break;
            }
        }
        return sequence;
    }
}
