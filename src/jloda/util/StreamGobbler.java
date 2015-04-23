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

package jloda.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Use this to monitor processes run with Runtime.exec()
 * Date: 21-Nov-2004
 */
public class StreamGobbler extends Thread {
    boolean stopped = false;
    final InputStream inputStream;
    final String prompt;

    /**
     * construct a gobbler
     *
     * @param inputStream input stream to monitor
     * @param prompt      label to put in front of output, or null
     */
    public StreamGobbler(InputStream inputStream, String prompt) {
        this.inputStream = inputStream;
        this.prompt = prompt;
    }

    /**
     * the run method
     */
    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(inputStream);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null) {
                if (prompt == null)
                    System.err.println(line);
                else
                    System.err.println(prompt + "> " + line);
                if (stopped)
                    break;
            }
        } catch (IOException ex) {
            Basic.caught(ex);
        }
    }

    /**
     * finish gobbling
     */
    public void finish() {
        stopped = true;
    }
}

