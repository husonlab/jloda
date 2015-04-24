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

package jloda.gui;

import jloda.util.Basic;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

/**
 * splashes an about window on the screen
 *
 * @author huson
 *         Date: 11-Feb-2004
 */
public class About {
    private String version;
    private final BufferedImage aboutImage;
    private final JDialog aboutDialog;
    boolean hasPainted = false;
    static Point versionStringOffset = new Point(20, 20);

    static private About instance=null;

    /**
     * get the current about window
     * @return about window or null
     */
    public static About getAbout() {
        return instance;
    }

    /**
     * set the current about window
     * @param packageName
     * @param fileName
     * @param version
     */
    public static void setAbout (String packageName, String fileName, final String version){
        setAbout(packageName, fileName, version, JDialog.HIDE_ON_CLOSE);

    }

    /**
     * set the current about window
     * @param packageName
     * @param fileName
     * @param version
     * @param closeOperation
     */
    public static void setAbout(String packageName, String fileName, String version, int closeOperation) {
    instance=new About(packageName,fileName,version,closeOperation);
    }

    /**
     * constructs an about message for splashing the screen
     *
     * @param packageName    name of package containing image file
     * @param fileName       name of image file
     * @param version0       version string to include in message
     * @param closeOperation default close operation, e.g. JDialog.HIDE_ON_CLOSE
     */
    private About(String packageName, String fileName, String version0, int closeOperation) {
        this.version = version0;

        BufferedImage image = null;
        //MZ: 2006-01-28
        try {
            image = ImageIO.read(Basic.getBasicClassLoader().getResourceAsStream(packageName.replaceAll("\\.", "/") + "/" + fileName));
        } catch (Exception e) {
            Basic.caught(e);
            //new Alert("ERROR: couldn't find SPLASH screen, corrupt installation?");
        }
        aboutImage = image;

        //if this fails with null, check whether the resources are in place
//                File file = Basic.getFileInPackage(packageName, fileName);
//                if (file == null || file.isFile() == false) {
//                    return;
//                }
//                // system.err.println("Found file: "+file);
//
//                if (aboutImage == null)
//                    aboutImage = ImageIO.read(file);
        aboutDialog = new JDialog();
        aboutDialog.setUndecorated(true);
        aboutDialog.setTitle("About " + version);
        aboutDialog.setDefaultCloseOperation(closeOperation);
        int width = (aboutImage != null ? aboutImage.getWidth() : 200);
        int height = (aboutImage != null ? aboutImage.getHeight() : 200);
        aboutDialog.setSize(width, height);
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        aboutDialog.setLocation((d.width - width) / 2, (d.height - height) / 2);

        JPanel pane = new JPanel();
        pane.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        pane.setLayout(new BorderLayout());
        pane.add(new Component() {
            public void paint(Graphics gc) {
                if (aboutImage != null)
                    gc.drawImage(aboutImage, 0, 0, this);
                else {
                    gc.setColor(Color.WHITE);
                    ((Graphics2D) gc).fill(this.getBounds());
                }
                gc.setColor(Color.BLACK);
                if (version != null)
                    gc.drawString(version, versionStringOffset.x, versionStringOffset.y);
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
                hideSplash();
            }
        });
    }

    /**
     * shows the about message on the screen
     */
    public void showAboutModal() {
        if (aboutDialog != null) {
            hasPainted = false;
            aboutDialog.setModal(true);
            aboutDialog.setVisible(true);
            aboutDialog.toFront();
            aboutDialog.setAlwaysOnTop(true);
        } else
            JOptionPane.showMessageDialog(null, version);
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
     *
     * @param x
     * @param y
     */
    static public void setVersionStringOffset(int x, int y) {
        versionStringOffset = new Point(x, y);
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public static boolean isSet() {
        return instance!=null;
    }
}
