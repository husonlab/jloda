/**
 * Date2Number.java 
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
