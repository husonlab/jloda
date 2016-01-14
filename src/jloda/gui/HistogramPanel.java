/**
 * HistogramPanel.java 
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
package jloda.gui;

import jloda.util.Alert;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.Rectangle2D;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;

/**
 * a panel containing a histogram
 * Daniel Huson , 3.2006
 */
public class HistogramPanel extends JPanel {
    final List data;
    int numberOfBuckets = 64;
    float minValue = 0;
    float maxValue = 0;
    float minCount = 0;
    float maxCount = 0;
    float bucketWidth = 0;
    float[] buckets = null;

    float threshold = 0;
    String text = "";
    boolean includeZero = false;
    boolean integerSteps = false;

    final JPanel topPanel;
    final JPanel centerPanel;
    final JPanel bottomPanel;
    final JLabel label;
    final JTextField input;
    final JSlider slider;
    boolean reverse = false;

    Color color = Color.BLACK;
    private int decimalDigits = 8; // digits after "."


    /**
     * constructor
     */
    public HistogramPanel() {
        super();
        setLayout(new BorderLayout());

        topPanel = new JPanel();
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 100, 5, 100));
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));
        input = new JTextField(8);
        input.addActionListener(new AbstractAction() {
            /**
             * Invoked when an action occurs.
             */
            public void actionPerformed(ActionEvent e) {
                try {
                    setThreshold(new Float(input.getText()));
                } catch (Exception ex) {
                }
            }
        });
        /*
        input.addKeyListener(new KeyAdapter(){
            public void keyTyped(KeyEvent keyEvent) {
                try
                {
                setThreshold((new Float(input.getText())).floatValue());
            }
                catch(Exception ex)
                {
                }
            }
        });
        */
        topPanel.add(input);
        label = new JLabel(text);
        topPanel.add(label);
        add(topPanel, BorderLayout.NORTH);

        centerPanel = new CenterPanel();
        add(centerPanel, BorderLayout.CENTER);

        slider = new JSlider();
        slider.addChangeListener(new ChangeListener() {
            /**
             * Invoked when the target of the listener has changed its state.
             *
             * @param e a ChangeEvent object
             */
            public void stateChanged(ChangeEvent e) {
                int newValue = ((JSlider) e.getSource()).getValue();
                if (!inSetThreshold)  // in explicit setThreshold, don't clobber the set value
                    setThreshold(minValue + newValue * bucketWidth);
                centerPanel.repaint();
            }
        });
        bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout(0, 0));
        bottomPanel.add(slider, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        data = new LinkedList();
    }

    /**
     * set values
     *
     * @param values
     */
    public void setValues(int[] values) {
        data.clear();
        for (int value : values) data.add((float) value);
        computeBuckets();
    }

    /**
     * set values
     *
     * @param values
     */
    public void setValues(BitSet values) {
        data.clear();
        for (int i = values.nextSetBit(0); i >= 0; i = values.nextSetBit(i + 1))
            data.add((float) i);
    }

    /**
     * set values
     *
     * @param values
     */
    public void setValues(float[] values) {
        data.clear();
        for (float value : values) data.add(value);
        computeBuckets();
    }

    /**
     * set values
     *
     * @param values
     */
    public void setValues(double[] values) {
        data.clear();
        for (double value : values) data.add(new Float(value));
        computeBuckets();
    }

    /**
     * set the values
     *
     * @param values list of Float values
     */
    public void setValues(List values) {
        data.clear();
        data.addAll(values);
        computeBuckets();
    }

    /**
     * erase the data
     */
    public void clear() {
        minValue = maxValue = bucketWidth = 0;
        minCount = maxCount = 0;
        buckets = new float[numberOfBuckets];
    }

    /**
     * compute the buckets
     */
    private void computeBuckets() {
        clear();
        if (data.size() > 0) {
            if (includeZero)
                minValue = 0;
            else
                minValue = Float.MAX_VALUE;
            maxValue = -Float.MAX_VALUE;
            minCount = 0;
            maxCount = 0;
            for (Object aData1 : data) {
                float f = (Float) aData1;
                if (f < minValue)
                    minValue = f;
                if (f > maxValue)
                    maxValue = f;
            }
            if (minValue == maxValue) {
                maxValue += 1;
            }


            bucketWidth = (maxValue - minValue) / (numberOfBuckets - 1);
            for (Object aData : data) {
                float f = (Float) aData;
                int i = getBucketForValue(f);
                buckets[i]++;
                if (buckets[i] < minCount)
                    minCount = buckets[i];
                if (buckets[i] > maxCount)
                    maxCount = buckets[i];
            }
        }

        if (isReverse())
            minValue -= (maxValue - minValue) / numberOfBuckets;


        slider.setMinimum(0);
        slider.setMaximum(numberOfBuckets);
        // reset slider:
        setThreshold(getThreshold());
    }

    /**
     * given a value f, returns its bucket
     *
     * @param f
     * @return bucket
     */
    private int getBucketForValue(float f) {
        return (int) Math.floor((f - minValue) / bucketWidth);
    }

    /**
     * center panel needs to be able to paint itself
     */
    class CenterPanel extends JPanel {
        CenterPanel() {
            super();
            setPreferredSize(new Dimension(400, 100));
        }

        /**
         * paint the histogram
         *
         * @param g0
         */
        public void paint(Graphics g0) {
            super.paint(g0);
            Graphics2D g = (Graphics2D) g0;
            float xoffset = 10;
            float width = (float) getBounds().getWidth();
            float height = (float) getBounds().getHeight();
            float dy = maxCount - minCount;
            int countIn = 0;
            int countTotal = 0;
            for (int i = 0; i < numberOfBuckets; i++) {
                countTotal += buckets[i];
                float x = i * (width - 2 * xoffset) / numberOfBuckets + xoffset;

                float y = height - buckets[i] * height / dy;
                int currentBucket = getBucketForValue(getThreshold());

                if (isReverse()) {
                    x = width - x - width / numberOfBuckets;
                    if (i < currentBucket) {

                        g.setColor(color);
                        countIn += buckets[i];
                    } else {
                        g.setColor(Color.GRAY);
                    }
                } else {
                    if (i < currentBucket) {
                        g.setColor(Color.GRAY);
                    } else {
                        g.setColor(color);
                        countIn += buckets[i];
                    }
                }
                g.fill(new Rectangle2D.Float(x, y, width / numberOfBuckets, height - y));
                g.setColor(color);
                g.draw(new Rectangle2D.Float(x, y, width / numberOfBuckets, height - y));
            }
            if (isIntegerSteps()) {
                input.setText("" + getThresholdInt());
                label.setText(" (" + countIn + " of " + countTotal + ")");
            } else {
                String str = "" + getThreshold();
                int p = str.lastIndexOf(".");
                if (p > -1 && p < str.length() - (decimalDigits + 1))
                    str = str.substring(0, p + decimalDigits + 1);
                input.setText("" + str);
                label.setText(" (" + countIn + " of " + countTotal + ")");
            }
        }
    }

    public float getThreshold() {
        return threshold;
    }

    public int getThresholdInt() {
        if (!reverse)
            return (int) Math.ceil(getThreshold());
        else
            return (int) Math.floor(getThreshold());

    }

    boolean inSetThreshold = false;

    public void setThreshold(float threshold) {
        this.threshold = threshold;
        int i = (int) ((threshold - minValue) / bucketWidth);
        inSetThreshold = true;
        slider.setValue(i);
        inSetThreshold = false;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setNumberOfBuckets(int numberOfBuckets) {
        this.numberOfBuckets = numberOfBuckets;
        computeBuckets();
        repaint();
    }

    public int getNumberOfBuckets() {
        return numberOfBuckets;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isIncludeZero() {
        return includeZero;
    }

    public void setIncludeZero(boolean includeZero) {
        this.includeZero = includeZero;
        computeBuckets();
    }

    public boolean isReverse() {
        return reverse;
    }

    public void setReverse(boolean reverse) {
        this.reverse = reverse;
        slider.setInverted(reverse);
        computeBuckets();
        if (reverse)
            setThreshold(maxValue);
        else
            setThreshold(minValue);
    }

    public boolean isIntegerSteps() {
        return integerSteps;
    }

    public void setIntegerSteps(boolean integerSteps) {
        this.integerSteps = integerSteps;
    }

    /**
     * open a dialog to choose dialog
     *
     * @param parent
     * @param title
     * @param initialThreshold
     * @return threshold chosen, or null, if canceled
     */
    public Float showThresholdDialog(JFrame parent, String title, float initialThreshold) {
        setThreshold(initialThreshold);
        return showThresholdDialog(parent, title);
    }

    Float result;

    /**
     * open a dialog to choose dialog
     *
     * @param parent
     * @param title
     * @return threshold chosen, or null, if canceled
     */
    public Float showThresholdDialog(final JFrame parent, String title) {
        final JDialog dialog = new JDialog(parent, title, true);
        dialog.setSize(400, 150);
        dialog.setLocationRelativeTo(parent);

        dialog.getContentPane().setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEtchedBorder());
        dialog.getContentPane().add(this, BorderLayout.CENTER);
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setBorder(BorderFactory.createEtchedBorder());

        JButton lessButton = new JButton(new AbstractAction() {
            /**
             * Invoked when an action occurs.
             */
            public void actionPerformed(ActionEvent e) {
                setNumberOfBuckets(Math.max(2, getNumberOfBuckets() / 2));
            }
        });
        lessButton.setText("-");
        buttonsPanel.add(lessButton);

        JButton moreButton = new JButton(new AbstractAction() {
            /**
             * Invoked when an action occurs.
             */
            public void actionPerformed(ActionEvent e) {
                setNumberOfBuckets(Math.min(1024, 2 * getNumberOfBuckets()));
            }
        });
        moreButton.setText("+");
        buttonsPanel.add(moreButton);

        JButton cancelButton = new JButton(new AbstractAction() {
            /**
             * Invoked when an action occurs.
             */
            public void actionPerformed(ActionEvent e) {
                dialog.setVisible(false);
                dialog.dispose();
                result = null;
            }
        });
        cancelButton.setText("Cancel");
        buttonsPanel.add(cancelButton);

        JButton applyButton = new JButton(new AbstractAction() {
            /**
             * Invoked when an action occurs.
             */
            public void actionPerformed(ActionEvent e) {
                dialog.setVisible(false);
                dialog.dispose();
                try {
                    result = new Float(input.getText());
                } catch (Exception ex) {
                    new Alert(parent, "Illegal input: " + input.getText());
                }
            }
        });
        applyButton.setText("Apply");
        buttonsPanel.add(applyButton);
        dialog.getContentPane().add(buttonsPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
        return result;
    }

    /**
     * get max number of decimal digits
     *
     * @return factional digits
     */
    public int getDecimalDigits() {
        return decimalDigits;
    }

    /**
     * set decimal digits
     *
     * @param decimalDigits
     */
    public void setDecimalDigits(int decimalDigits) {
        this.decimalDigits = decimalDigits;
    }
}
