/**
 * StreamGobbler.java 
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

