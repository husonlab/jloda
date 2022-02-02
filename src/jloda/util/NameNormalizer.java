/*
 * NameNormalizer.java Copyright (C) 2022 Daniel H. Huson
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

/**
 * normalizes names using a simple edit
 * Daniel Huson, 2.2019
 */
public class NameNormalizer {
    private final String a;
    private final String b;

    /**
     * constructor
     *
     * @param expression must be in the format A/B where A and B are regular expressions to which replaceAll(A,B) is applied
     */
    public NameNormalizer(String expression) {
        if (expression.startsWith("/"))
            expression = expression.substring(1);
        if (expression.endsWith("/"))
            expression = expression.substring(0, expression.length() - 1);
        final String[] tokens = expression.split("/");
        if (tokens.length == 1) {
            a = tokens[0].trim();
            b = "";
        } else if (tokens.length == 2) {
            a = tokens[0].trim();
            b = tokens[1].trim();
        } else
            a = b = null;
    }

    /**
     * apply replaceAll(A,B) to name
     *
     * @return modified name
     */
    public String apply(String name) {
        if (a == null)
            return name;
        else
            return name.replaceAll(a, b);
    }
}
