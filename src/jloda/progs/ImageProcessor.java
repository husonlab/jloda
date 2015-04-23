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

/**
 * a simple image processing program
 * Daniel Huson, 3.2008
 */

import jloda.util.CommandLineOptions;
import jloda.util.UsageException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

/**
 * a simple image processor
 * Daniel Huson, 3.2008
 */
public class ImageProcessor extends Component {
    public static void main(String[] args) throws Exception {
        ImageProcessor traitMapper = new ImageProcessor();

        traitMapper.run(args);
    }

    /**
     * run the trait mapper
     *
     * @param args
     * @throws UsageException
     */
    public void run(String[] args) throws Exception {
        CommandLineOptions options = new CommandLineOptions(args);
        options.setDescription(ImageProcessor.class.getName() + "- simple image processor: replaces all non-green pixels by black");

        String inputFile = options.getMandatoryOption("-i", "Image file", "");
        int patchSize = options.getOption("-p", "pixel patch size", 3);
        String outputFormat = options.getOption("-f", "output format", ImageIO.getWriterFormatNames(), "png");
        String outputFile = options.getOption("-o", "Output file", "out");
        options.done();

        BufferedImage inputImage = readImage(inputFile);
        BufferedImage outputImage = filterGreen(inputImage, patchSize);

        writeImage(outputFile, outputFormat, outputImage);
    }

    /**
     * computes a new image from the input image replacing everything that is not green
     *
     * @param originalImage
     * @return new image
     * @throws IOException
     * @throws InterruptedException
     */
    public BufferedImage filterGreen(BufferedImage originalImage, int patchSize) throws IOException, InterruptedException {
        int width = originalImage.getWidth(this);
        int height = originalImage.getHeight(this);

        // BufferedImage newImage = new BufferedImage(width, height,originalImage.getType());
        Hashtable properties = new Hashtable();
        String[] names = originalImage.getPropertyNames();
        if (names != null) {
            for (String name : names) properties.put(name, originalImage.getProperty(name));
        }

        // todo: I don't know whether this really produces a new image. Perhaps one needs to some cloning somewhere?
        BufferedImage newImage = new BufferedImage(originalImage.getColorModel(), originalImage.getRaster(), originalImage.isAlphaPremultiplied(), properties);

        newImage.setData(originalImage.getRaster());
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++) {
                if (x <= patchSize || y <= patchSize || x >= width - patchSize || y >= height - patchSize || !isGreen(originalImage, x, y, patchSize))
                    newImage.setRGB(x, y, Color.BLACK.getRGB());
            }
        return newImage;
    }

    /**
     * is this pixel green?
     *
     * @param originalImage
     * @param x
     * @param y
     * @param delta
     * @return true, if green
     */
    private boolean isGreen(BufferedImage originalImage, int x, int y, int delta) {
        int max = (2 * delta + 1) * (2 * delta + 1);

        int count = 0;
        for (int dx = -delta; dx <= delta; dx++) {
            for (int dy = -delta; dy <= delta; dy++) {
                Color originalColor = new Color(originalImage.getRGB(x + dx, y + dy));
                if (originalColor.getGreen() > 120 && (originalColor.getRed() < originalColor.getGreen() - 15 || originalColor.getBlue() < originalColor.getGreen() - 15))
                    count++;
            }
        }
        return count > 0.5 * max;
    }

    /**
     * read a buffered image from a file
     *
     * @param fileName
     * @return image
     * @throws IOException
     */
    public static BufferedImage readImage(String fileName) throws IOException {
        System.err.print("Reading image file '" + fileName + "':");
        if (!(new File(fileName)).canRead())
            throw new IOException("Can't read file '" + fileName + "'");
        BufferedImage image = ImageIO.read(new File(fileName));
        System.err.println(" done");
        return image;
    }

    /**
     * write a buffered image to a file
     *
     * @param fileName
     * @param formatName
     * @param bufferedImage
     * @throws IOException
     */
    public static void writeImage(String fileName, String formatName, BufferedImage bufferedImage) throws IOException {
        System.err.print("Writing image file '" + fileName + "." + formatName + "':");
        File file = new File(fileName + "." + formatName);
        ImageIO.write(bufferedImage, formatName, file);
        System.err.println(" done");
    }
}
