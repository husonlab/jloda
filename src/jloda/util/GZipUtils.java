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

