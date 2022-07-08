/*
 * PhyloTreeNetworkIOUtils.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.phylo;

/**
 * some utilities used in phylotree to write and parse rooted networks
 * Daniel Huson, 8.2007
 */
public class PhyloTreeNetworkIOUtils {
    /**
     * looks for a suffix of the label that starts with '#'
     *
     * @return label or null
     */
    public static String findReticulateLabel(String label) {
		int pos = label.lastIndexOf("#"); // look for last instance of '#',
		// followed by H or R or L
		if (pos >= 0 && pos < label.length() - 1 && "HhLlRr".indexOf(label.charAt(pos + 1)) != -1)
			return label.substring(pos + 1);
		else
			return null;
	}

    /**
     * determines whether this a reticulate node
     *
     * @param label the node label
     * @return true, if label contains # followed by H L h or l
     */
    public static boolean isReticulateNode(String label) {
        label = label.toUpperCase();
        return label.contains("#H") || label.contains("#L") || label.contains("#R");
    }

    /**
     * determines whether the edge leading to this instance of a reticulate node
     * should be treated as an acceptor edge, i.e. as a tree edge that is the
     * target of of HGT edge. At most one such edge per reticulate node is
     * allowed
     *
     * @param label the node label
     * @return true, if label contains ## followed by H L h or l
     */
    public static boolean isReticulateAcceptorEdge(String label) {
		return label.startsWith("##");
    }

    /**
     * removes the reticulate node string from the node label
     *
     * @param label the node label
     * @return string without label or null, if string only consisted of
     * substring
     */
    public static String removeReticulateNodeSuffix(String label) {
        var pos = label.indexOf("#");
        if (pos == -1)
            return label;
        else if (pos == 0)
            return null;
        else
            return label.substring(0, pos);
    }

    /**
     * makes the node label for a reticulate node
     *
     * @return label
     */
    static String makeReticulateNodeLabel(boolean asAcceptorEdgeTarget, int number) {
        if (asAcceptorEdgeTarget)
            return "##H" + number;
        else
            return "#H" + number;
    }
}
