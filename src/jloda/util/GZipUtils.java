/**
 * GZipUtils.java 
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * GZIP utilities
 * Daniel Huson, 6.2014
 */
public class GZipUtils {

    /**
     * deflate a file in gzip format
     *
     * @param sourceFile
     * @param compressedFile
     */
    public static void deflate(String sourceFile, String compressedFile) {
        byte[] buffer = new byte[512000];
        try {
            final ProgressPercentage progress = new ProgressPercentage("Deflating file: " + sourceFile, ((new File(sourceFile)).length()));
            long total = 0;

            final FileInputStream fileInput = new FileInputStream(sourceFile);
            final GZIPOutputStream gzipOuputStream = new GZIPOutputStream(new FileOutputStream(compressedFile));

            int numberOfBytes;
            while ((numberOfBytes = fileInput.read(buffer)) > 0) {
                gzipOuputStream.write(buffer, 0, numberOfBytes);
                progress.setProgress(total += numberOfBytes);
            }

            fileInput.close();

            gzipOuputStream.finish();
            gzipOuputStream.close();

            progress.close();

        } catch (IOException ex) {
            Basic.caught(ex);
        }
    }


    /**
     * inflate a gzip file
     *
     * @param compressedFile
     * @param decompressedFile
     */
    public static void inflate(String compressedFile, String decompressedFile) {
        byte[] buffer = new byte[512000];

        try {
            final ProgressPercentage progress = new ProgressPercentage("Inflating file: " + compressedFile, ((new File(compressedFile)).length()));
            long total = 0;

            final GZIPInputStream gZIPInputStream = new GZIPInputStream(new FileInputStream(compressedFile));
            final FileOutputStream fileOutputStream = new FileOutputStream(decompressedFile);

            int numberOfBytes;
            while ((numberOfBytes = gZIPInputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, numberOfBytes);
                progress.setProgress(total += numberOfBytes);
            }

            gZIPInputStream.close();
            fileOutputStream.close();

            progress.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}

