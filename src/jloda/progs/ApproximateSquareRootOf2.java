/**
 * ApproximateSquareRootOf2.java 
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
 * approximate square root of 2
 * Daniel Huson, 12.2011
 */
public class ApproximateSquareRootOf2 {
    /**
     * approximate square root of two
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        // print prompt:
        System.out.println("Approximation of square root of 2");
        System.out.println("Using a=0 and b=2");
        System.out.print("Enter max error: ");
        System.out.flush();
        // get parameters:
        double maxError = Double.parseDouble((new BufferedReader(new InputStreamReader(System.in))).readLine());

        // run algorithm:
        double a = 0, b = 2;
        while (b - a > maxError) {
            double c = (a + b) / 2;

            System.out.println(String.format("a=%1.12g b=%1.12g   c=%1.12g   b-a=%g", a, b, c, b - a));

            if (c * c < 2)
                a = c;
            else
                b = c;
        }
        // output:
        System.out.println("Approximation: " + a);
    }
}
