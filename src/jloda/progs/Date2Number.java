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

import jloda.util.Basic;
import jloda.util.UsageException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.util.Date;

/**
 * converts a date into a long
 * Daniel Huson Jun 8, 2006
 */
public class Date2Number {
    public static void main(String[] args) throws Exception {
        if (args.length != 0)
            throw new UsageException("Date2Number");
        Date date = new Date();
        System.out.println("Current date=" + date + "=" + date.getTime());

        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
        String aLine;
        while ((aLine = r.readLine()) != null) {
            if (aLine.equals("q"))
                break;
            if (aLine.length() > 0) {
                try {
                    date = DateFormat.getDateInstance().parse(aLine);
                    System.out.print("" + aLine + "=" + date + "=" + date.getTime() + "L");
                } catch (Exception ex) {
                    Basic.caught(ex);
                }
            }
        }
    }

}
