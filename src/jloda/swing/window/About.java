/*
 * About.java Copyright (C) 2024 Daniel H. Huson
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

package jloda.swing.window;

import jloda.fx.util.ProgramExecutorService;
import jloda.swing.util.ProgramProperties;
import jloda.swing.util.ResourceManager;
import jloda.util.Basic;
import jloda.util.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

/**
 * splashes an about window on the screen
 *
 * @author huson
 * Date: 11-Feb-2004
 */
public class About {
    private String versionString;
    private static Point versionStringOffset = new Point(20, 20);
    private final BufferedImage aboutImage;
    private final JDialog aboutDialog;
    private boolean hasPainted = false;
    private String additionalString;
    private int additionalStringVerticalPosition = 100;

    static private About instance = null;

    /**
     * get the current about window
     *
     * @return about window or null
     */
    public static About getAbout() {
        return instance;
    }

    /**
     * set the current about window
     *
	 */
    public static void setAbout(String fileName, String version, int closeOperation) {
        instance = new About(fileName, version, closeOperation,1f);
    }
    /**
     * set the current about window
     *
	 */
    public static void setAbout(String fileName, String version, int closeOperation,float scaleFactor) {
        instance = new About(fileName, version, closeOperation,scaleFactor);
    }

    /**
     * constructs an about message for splashing the screen
     *
     * @param fileName       name of image file
     * @param version0       version string to include in message
     * @param closeOperation default close operation, e.g. JDialog.HIDE_ON_CLOSE
     */
    private About(String fileName, String version0, int closeOperation,float scaleFactor) {
        this.versionString = version0;
        aboutImage = ResourceManager.getImage(fileName);


        //if this fails with null, check whether the resources are in place
//                File file = Basic.getFileInPackage(packageName, fileName);
//                if (file == null || file.isFile() == false) {
//                    return;
//                }
//                // system.err.println("Found file: "+file);
//
//                if (aboutImage == null)
//                    aboutImage = ImageIO.read(file);
        aboutDialog = new JDialog(null, Dialog.ModalityType.APPLICATION_MODAL);
        aboutDialog.setUndecorated(true);
        aboutDialog.setTitle("About " + versionString);
        aboutDialog.setDefaultCloseOperation(closeOperation);
        var width = Math.round(scaleFactor * (aboutImage != null ? aboutImage.getWidth() : 200));
        var height = Math.round(scaleFactor * (aboutImage != null ? aboutImage.getHeight() : 200));
        aboutDialog.setSize(width, height);
        var d = Toolkit.getDefaultToolkit().getScreenSize();
        aboutDialog.setLocation((d.width - width) / 2, (d.height - height) / 2);

        var pane = new JPanel();
        pane.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        pane.setLayout(new BorderLayout());
        pane.add(new Component() {
            public void paint(Graphics gc) {
                gc.setFont(new Font(Font.DIALOG, Font.PLAIN, 11));
                if (aboutImage != null)
                    gc.drawImage(aboutImage.getScaledInstance(width,height,Image.SCALE_SMOOTH), 0, 0, this);
                else {
                    gc.setColor(Color.WHITE);
                    ((Graphics2D) gc).fill(this.getBounds());
                }
                gc.setColor(Color.DARK_GRAY);
                var line=0;
                if (versionString != null) {
                    var tokens = StringUtils.split(versionString, '\n');
                    for (line = 0; line < tokens.length; line++) {
                        gc.drawString(tokens[line], versionStringOffset.x, versionStringOffset.y + 15 * line);
                    }
                }
                if (additionalString != null) {
                    gc.drawString(additionalString, versionStringOffset.x,versionStringOffset.y + 15 * line);
                }
                if (!hasPainted) {
                    hasPainted = true;
                    synchronized (aboutDialog) {
                        aboutDialog.notifyAll();
                    }
                }
            }
        }, BorderLayout.CENTER);

        aboutDialog.getContentPane().setLayout(new BorderLayout());
        aboutDialog.getContentPane().add(pane, BorderLayout.CENTER);

        aboutDialog.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                hideSplash();
            }
        });
        aboutDialog.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent event) {
                hideSplash();
            }
        });

        aboutDialog.addWindowFocusListener(new WindowFocusListener() {
            public void windowGainedFocus(WindowEvent event) {
            }

            public void windowLostFocus(WindowEvent event) {
                if (false) hideSplash();
            }
        });
    }

    /**
     * shows the about message on the screen
     */
    public void showAboutModal() {
        ProgramProperties.checkState();

        if (aboutDialog != null) {
            hasPainted = false;
            aboutDialog.setModal(true);
            aboutDialog.setVisible(true);
            aboutDialog.toFront();
            aboutDialog.setAlwaysOnTop(true);
        } else
            JOptionPane.showMessageDialog(null, versionString);
    }

    /**
     * splashs the about message on the screen until hideAbout is called
     */
    public void showAbout() {
        if (aboutDialog != null) {
            hasPainted = false;
            aboutDialog.setModal(false);
            aboutDialog.setVisible(true);
            aboutDialog.toFront();
            aboutDialog.setAlwaysOnTop(true);

            // give user chance to see splash screen:
            while (!hasPainted) {
                synchronized (aboutDialog) {
                    try {
                        aboutDialog.wait();
                    } catch (Exception ex) {
                        Basic.caught(ex);
                        break;
                    }
                }
            }
            hideAfter(4000);
        }
    }

    /**
     * hide the splash screen again
     */
    public void hideSplash() {
        if (aboutDialog != null)
            aboutDialog.setVisible(false);
    }

    /**
     * set the version string offset
     */
    static public void setVersionStringOffset(int x, int y) {
        versionStringOffset = new Point(x, y);
    }

    public void setVersion(String version) {
        this.versionString = version;
    }

    public static boolean isSet() {
        return instance != null;
    }

    public String getAdditionalString() {
        return additionalString;
    }

    public void setAdditionalString(String additionalString) {
        this.additionalString = additionalString;
    }

    public int getAdditionalStringVerticalPosition() {
        return additionalStringVerticalPosition;
    }

    public void setAdditionalStringVerticalPosition(int additionalStringVerticalPosition) {
        this.additionalStringVerticalPosition = additionalStringVerticalPosition;
    }

    /**
     * hide after given number of milliseconds
     *
     * @param millis time to wait before hiding
     */
    public void hideAfter(final int millis) {
        ProgramExecutorService.submit(millis, this::hideSplash);
    }
}
