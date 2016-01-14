/**
 * ApproximateBinaryExpansion.java 
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * approximate binary expansion of number between 0 and 1
 * Daniel Huson, 12.2011
 */
public class ApproximateBinaryExpansion {
    /**
     * approximate square root of two
     *
     * @param args
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
        // get parameters:
        System.out.println("Approximate binary expansion of x between 0 and 1");
        BufferedReader r = (new BufferedReader(new InputStreamReader(System.in)));
        System.out.print("Enter x: ");
        System.out.flush();
        double x = Double.parseDouble(r.readLine());
        if (x < 0 || x > 1)
            throw new IOException("Out of range: " + x);
        System.out.print("Enter n: ");
        System.out.flush();
        int n = Integer.parseInt(r.readLine());

        // run algorithm:
        System.out.print("Binary expansion: 0.");

        double a = 0, b = 1;
        for (int i = 0; i < n; i++) {
            double c = (a + b) / 2;
            if (c < x) {
                System.out.print("1");
                a = c;
            } else {
                System.out.print("0");
                b = c;
            }
        }

        System.out.println();
    }
}
