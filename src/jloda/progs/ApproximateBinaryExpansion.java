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
