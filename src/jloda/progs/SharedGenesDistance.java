/*
 * SharedGenesDistance.java Copyright (C) 2019. Daniel H. Huson
 *
 *  (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * gene content distance calculation
 * @version $Id: SharedGenesDistance.java,v 1.4 2009-09-25 13:47:13 huson Exp $
 * @author Daniel Huson
 * 9.2003
 */

package jloda.progs;

import jloda.swing.util.CommandLineOptions;
import jloda.util.PhylipUtils;

import java.io.File;
import java.io.FileReader;
import java.util.BitSet;

/**
 * computes shared genes distance as
 * in Snel, Bork and Huynen, Nature 1999
 * or using ML based distance of Huson and Steel 2003
 */
public class SharedGenesDistance {
    /**
     * run the program
     */
    public static void main(String[] args) throws Exception {
        CommandLineOptions options = new CommandLineOptions(args);
        options.setDescription("SharedGenesDistance" +
                "- compute distances based on shared genes");

        String fileName = options.getMandatoryOption("-i", "Input tree file", "");
        boolean useMLDistance = options.getOption("-m", "use ML distance of Huson and Steel 2003", true, false);
        options.done();

        // read sequences in phylip format
        String[][] data = new String[2][];
        PhylipUtils.read(data, new FileReader(new File(fileName)));
        String[] names = data[0];
        String[] sequences = data[1];

        BitSet[] genes = computeGenes(sequences);
        int ntax = names.length - 1;

        float[][] dist;

        if (!useMLDistance)
            dist = computeSnelBorkDistance(ntax, genes);
        else
            dist = computeMLDistance(ntax, genes);

        PhylipUtils.print(names, dist, System.out);
    }

    /**
     * comnputes the SnelBork et al distance
     *
     * @param ntax
     * @param genes
     * @return the distance matrix
     */
    private static float[][] computeSnelBorkDistance(int ntax, BitSet[] genes) {

        float[][] dist = new float[ntax + 1][ntax + 1];
        for (int i = 1; i <= ntax; i++) {
            dist[i][i] = 0;
            for (int j = i + 1; j <= ntax; j++) {
                BitSet intersection = ((BitSet) (genes[i]).clone());
                intersection.and(genes[j]);
                dist[i][j] = dist[j][i] = (float) (
                        1.0 - ((float) intersection.cardinality()
                                / (float) Math.min(genes[i].cardinality(),
                                genes[j].cardinality())));

            }
        }
        return dist;
    }

    /**
     * comnputes the maximum likelihood estimator distance Huson and Steel 2003
     *
     * @param ntax
     * @param genes
     * @return the distance matrix
     */
    private static float[][] computeMLDistance(int ntax, BitSet[] genes) {

        // dtermine average genome size:
        double m = 0;
        for (int i = 1; i <= ntax; i++) {
            m += genes[i].cardinality();
        }
        m /= ntax;

        double[] ai = new double[ntax + 1];
        double[][] aij = new double[ntax + 1][ntax + 1];
        for (int i = 1; i <= ntax; i++) {
            ai[i] = ((double) genes[i].cardinality()) / m;
        }
        for (int i = 1; i <= ntax; i++) {
            for (int j = i + 1; j <= ntax; j++) {
                BitSet intersection = ((BitSet) (genes[i]).clone());
                intersection.and(genes[j]);
                aij[i][j] = aij[j][i] = ((double) intersection.cardinality()) / m;
            }
        }

        float[][] dist = new float[ntax + 1][ntax + 1];
        for (int i = 1; i <= ntax; i++) {
            dist[i][i] = 0;
            for (int j = i + 1; j <= ntax; j++) {
                double b = 1.0 + aij[i][j] - ai[i] - ai[j];

                dist[i][j] = dist[j][i] =
                        (float) -Math.log(0.5 * (b + Math.sqrt(b * b + 4.0 * aij[i][j] * aij[i][j])));
                if (dist[i][j] < 0)
                    dist[i][j] = dist[j][i] = 0;
            }
        }
        return dist;
    }


    /**
     * computes gene sets from strings
     *
     * @param sequences as strings
     * @return sets of genes
     */
    static private BitSet[] computeGenes(String[] sequences) {
        BitSet[] genes = new BitSet[sequences.length];

        for (int s = 1; s < sequences.length; s++) {
            genes[s] = new BitSet();
            String seq = sequences[s];
            for (int i = 0; i < seq.length(); i++) {
                if (seq.charAt(i) == '1')
                    genes[s].set(i);
            }
        }
        return genes;
    }
}
