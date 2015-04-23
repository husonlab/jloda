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
