/**
 * GraphEditor.java 
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
package jloda.graphview;

/**
 * @version $Id: GraphEditor.java,v 1.10 2006-01-19 12:00:33 huson Exp $
 *
 * Graph editor class.
 *
 * @author Daniel Huson
 */

import jloda.graph.Edge;
import jloda.graph.Graph;
import jloda.graph.Node;
import jloda.util.Basic;
import jloda.util.NotOwnerException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.print.PrinterJob;

/**
 * A graph editor.
 */
public class GraphEditor extends GraphView {
    private MenuBar MB; // the menu bar.
    private GraphEditorActionListener actionListener; // handles menus
    private GraphEditorWindowListener windowListener; // handles window events

    static int numberEditors = 0;
    static boolean exitOnLastClose = false;

    /**
     * Constructs a GraphEditor.
     *
     * @param G graph to be viewed
     */
    public GraphEditor(Graph G) {
        super(G, 400, 400);
        init(G, "GraphEditor", 400, 400);
    }

    /**
     * Constructs a GraphEditor.
     *
     * @param G     graph to be viewed
     * @param title title of frame
     */
    public GraphEditor(Graph G, String title) {
        super(G, 400, 400);
        init(G, title, 400, 400);
    }

    /**
     * Constructs a GraphEditor.
     *
     * @param G     graph to be viewed
     * @param title title of frame
     * @param w     width of frame
     * @param h     height of frame
     */
    public GraphEditor(Graph G, String title, int w, int h) {
        super(G, w, h);
        init(G, title, w, h);
    }

    /**
     * Sets the associated Frame.
     *
     * @param frame the Frame
     */
    public void setFrame(JFrame frame) {
        super.setFrame(frame);
        frame.addWindowListener(windowListener);
    }

    /**
     * Does initialization.
     *
     * @param G     Graph
     * @param title String
     * @param w     int the width of frame
     * @param h     int the height of frame
     */
    private void init(Graph G, String title, int w, int h) {
        frame = new JFrame(title);
        frame.setSize(getSize());
        frame.add(this);
        MB = new MenuBar();

        actionListener = new GraphEditorActionListener(this);
        windowListener = new GraphEditorWindowListener(this);
        frame.addWindowListener(windowListener);

        addFileMenu();
        addEditMenu();
        addFormatMenu();
        addLayoutMenu();

        frame.setMenuBar(MB);
    }

    /* Setup the File menu. */

    void addFileMenu() {
        Menu fileMenu = new Menu("File", true);
        fileMenu.add("New");
        fileMenu.addSeparator();
        fileMenu.add("Open...");
        fileMenu.add("Save");
        fileMenu.add("Save As...");
        fileMenu.addSeparator();
        fileMenu.add("Print...");
        fileMenu.addSeparator();
        fileMenu.add("Close");
        fileMenu.addSeparator();
        fileMenu.add("Quit");

        fileMenu.addActionListener(actionListener);

        MB.add(fileMenu);
    }

    /* Setup the Edit menu. */

    void addEditMenu() {
        Menu editMenu = new Menu("Edit", true);
        editMenu.add("Cut");
        editMenu.add("Copy");
        editMenu.add("Paste");
        editMenu.addSeparator();
        editMenu.add("Delete");
        editMenu.addSeparator();
        editMenu.add("Select All");
        editMenu.add("Select All Nodes");
        editMenu.add("Select All Edges");
        editMenu.addSeparator();
        editMenu.add("Horizontal Flip");
        editMenu.add("Vertical Flip");

        editMenu.addActionListener(actionListener);

        MB.add(editMenu);
    }

    /* Setup the Format menu. */

    void addFormatMenu() {
        Menu formatMenu = new Menu("Format", true);

        Menu linewidthMenu = new Menu("Line Width", true);
        linewidthMenu.add("0pt");
        linewidthMenu.add("1pt");
        linewidthMenu.add("2pt");
        linewidthMenu.add("4pt");
        linewidthMenu.add("8pt");
        linewidthMenu.add("10pt");
        linewidthMenu.addActionListener(actionListener);

        formatMenu.add(linewidthMenu);

        Menu lineColorMenu = new Menu("Line Color", true);
        lineColorMenu.add("black");
        lineColorMenu.add("blue");
        lineColorMenu.add("cyan");
        lineColorMenu.add("darkGray");
        lineColorMenu.add("gray");
        lineColorMenu.add("green");
        lineColorMenu.add("lightGray");
        lineColorMenu.add("magenta");
        lineColorMenu.add("orange");
        lineColorMenu.add("pink");
        lineColorMenu.add("red");
        lineColorMenu.add("white");
        lineColorMenu.add("yellow");
        lineColorMenu.addSeparator();
        lineColorMenu.add("none");
        lineColorMenu.addActionListener(new ColorActionListener(this, "line"));
        formatMenu.add(lineColorMenu);

        Menu fillColorMenu = new Menu("Fill Color", true);
        fillColorMenu.add("black");
        fillColorMenu.add("blue");
        fillColorMenu.add("cyan");
        fillColorMenu.add("darkGray");
        fillColorMenu.add("gray");
        fillColorMenu.add("green");
        fillColorMenu.add("lightGray");
        fillColorMenu.add("magenta");
        fillColorMenu.add("orange");
        fillColorMenu.add("pink");
        fillColorMenu.add("red");
        fillColorMenu.add("white");
        fillColorMenu.add("yellow");
        fillColorMenu.addSeparator();
        fillColorMenu.add("none");
        fillColorMenu.addActionListener(new ColorActionListener(this, "fill"));
        formatMenu.add(fillColorMenu);

        Menu labelColorMenu = new Menu("Label Color", true);
        labelColorMenu.add("black");
        labelColorMenu.add("blue");
        labelColorMenu.add("cyan");
        labelColorMenu.add("darkGray");
        labelColorMenu.add("gray");
        labelColorMenu.add("green");
        labelColorMenu.add("lightGray");
        labelColorMenu.add("magenta");
        labelColorMenu.add("orange");
        labelColorMenu.add("pink");
        labelColorMenu.add("red");
        labelColorMenu.add("white");
        labelColorMenu.add("yellow");
        labelColorMenu.addSeparator();
        labelColorMenu.add("none");
        labelColorMenu.addActionListener(new ColorActionListener(this, "label"));
        formatMenu.add(labelColorMenu);

        formatMenu.addSeparator();
        formatMenu.add("Style");
        formatMenu.add("Size");

        formatMenu.addSeparator();
        formatMenu.add("Label");

        formatMenu.addActionListener(actionListener);

        MB.add(formatMenu);
    }

    /* Setup the Layout menu. */

    void addLayoutMenu() {
        Menu layoutMenu = new Menu("Layout", true);
        layoutMenu.add("Zoom to Graph");
        layoutMenu.addSeparator();
        layoutMenu.add("Zoom In");
        layoutMenu.add("Zoom Out");
        layoutMenu.addSeparator();
        layoutMenu.add("Spring Embedding");

        layoutMenu.addActionListener(actionListener);

        MB.add(layoutMenu);
    }


    /**
     * This closes the editor.
     */
    public void close() {
        frame.dispose();
        frame = null;
    }

    /**
     * Set whether program should exit when last open GraphEditor
     * is closed
     *
     * @param yes true, if exit is desired
     */
    static public void setExitOnLastClose(boolean yes) {
        exitOnLastClose = yes;
    }

    /**
     * Will program exit when last open GraphEditor is closed
     *
     * @return true, if program will exit
     */
    static public boolean getExitOnLastClose() {
        return exitOnLastClose;
    }

    /**
     * How many GraphEditors are open?
     *
     * @return number of open GraphEditors
     */
    static public int getNumberEditors() {
        return numberEditors;
    }
}


class GraphEditorActionListener implements ActionListener {
    private GraphEditor GE;

    GraphEditorActionListener(GraphEditor GE) {
        this.GE = GE;
    }

    /**
     * This takes care of menu events
     *
     * @param ev ActionEvent
     */
    public void actionPerformed(ActionEvent ev) {
        // Basic.message("Menu action: "+ev);

        if (ev.getActionCommand().equals("New")) {

            // this shouldn't work! once we leave the scope of
                // ed, you'ed expect ed to go away, but it doesn't...
                GraphEditor ed = new GraphEditor(GE.getGraph(), "New", 400, 400);
                ed.getFrame().setResizable(true);
                ed.getFrame().setVisible(true);
        } else if (ev.getActionCommand().equals("Print...")) {
            PrinterJob job = PrinterJob.getPrinterJob();
            // Specify the Printable is an instance of SimplePrint
            job.setPrintable(GE);
            // Put up the dialog box
            if (job.printDialog()) {
                // Print the job if the user didn't cancel printing
                try {
                    job.print();
                } catch (Exception ex) {
                    Basic.caught(ex);
                }
            }
        } else if (ev.getActionCommand().equals("Select All")) {
            GE.selectAllNodes(true);
            GE.selectAllEdges(true);
            GE.repaint();
        } else if (ev.getActionCommand().equals("Horizontal Flip")) {
            GE.horizontalFlipSelected();
            GE.repaint();
        } else if (ev.getActionCommand().equals("Vertical Flip")) {
            GE.verticalFlipSelected();
            GE.repaint();
        } else if (ev.getActionCommand().equals("Close")) {
            GE.close();
            GE = null;
        } else if (ev.getActionCommand().equals("Quit")) {
            // need to replace this by code that closes all windows
            System.exit(0);
        } else if (ev.getActionCommand().equals("Delete")) {
            GE.delSelectedEdges();
            GE.delSelectedNodes();
            GE.repaint();
        } else if (ev.getActionCommand().equals("Select All")) {
            GE.selectAllNodes(true);
            GE.selectAllEdges(true);
            GE.repaint();
        } else if (ev.getActionCommand().equals("Select All Nodes")) {
            GE.selectAllNodes(true);
            GE.repaint();
        } else if (ev.getActionCommand().equals("Select All Edges")) {
            GE.selectAllEdges(true);
            GE.repaint();
        } else if (ev.getActionCommand().equals("Zoom to Graph")) {
            GE.fitGraphToWindow();
            GE.repaint();
        } else if (ev.getActionCommand().equals("Zoom In")) {
            double s = 1.5;
            GE.trans.composeScale(1.0 / s, 1.0 / s);
            GE.repaint();
        } else if (ev.getActionCommand().equals("Zoom Out")) {
            double s = 1.5;
            GE.trans.composeScale(s, s);
            GE.repaint();
        } else if (ev.getActionCommand().equals("Spring Embedding")) {
            GE.computeSpringEmbedding(100, true);
            GE.fitGraphToWindow();
            GE.repaint();
        } else if (ev.getActionCommand().equals("0pt")) {
            GE.setLineWidthSelected(0);
            GE.repaint();
        } else if (ev.getActionCommand().equals("1pt")) {
            GE.setLineWidthSelected(1);
            GE.repaint();
        } else if (ev.getActionCommand().equals("2pt")) {
            GE.setLineWidthSelected(2);
            GE.repaint();
        } else if (ev.getActionCommand().equals("4pt")) {
            GE.setLineWidthSelected(4);
            GE.repaint();
        } else if (ev.getActionCommand().equals("8pt")) {
            GE.setLineWidthSelected(8);
            GE.repaint();
        } else if (ev.getActionCommand().equals("10pt")) {
            GE.setLineWidthSelected(10);
            GE.repaint();
        } else if (ev.getActionCommand().equals("Label")) {
            String label = JOptionPane.showInputDialog("Enter label");
            if (label != null) {
                try {
                    Graph graph = GE.getGraph();

                    for (Node v = graph.getFirstNode(); v != null; v = graph.getNextNode(v)) {
                        if (GE.getSelected(v))
                            GE.setLabel(v, label);
                    }
                    for (Edge e = graph.getFirstEdge(); e != null; e = graph.getNextEdge(e)) {
                        if (GE.getSelected(e))
                            GE.setLabel(e, label);
                    }
                } catch (NotOwnerException ex) {
                    Basic.caught(ex);
                }
                GE.repaint();
            }
        } else
            System.err.println("Not implemented: " + ev);
    }
}

class ColorActionListener implements ActionListener {
    private final GraphEditor GE;
    private final String kind;

    /**
     * constructor of ColorActionListener
     *
     * @param GE   GraphEditor
     * @param kind String
     */
    ColorActionListener(GraphEditor GE, String kind) {
        this.GE = GE;
        this.kind = kind;
    }

    /**
     * This takes care of menu events
     *
     * @param ev ActionEvent
     */
    public void actionPerformed(ActionEvent ev) {
        if (ev.getActionCommand().equals("black")) {
            GE.setColorSelected(Color.black, kind);
            GE.repaint();
        } else if (ev.getActionCommand().equals("blue")) {
            GE.setColorSelected(Color.blue, kind);
            GE.repaint();
        } else if (ev.getActionCommand().equals("cyan")) {
            GE.setColorSelected(Color.cyan, kind);
            GE.repaint();
        } else if (ev.getActionCommand().equals("darkGray")) {
            GE.setColorSelected(Color.darkGray, kind);
            GE.repaint();
        } else if (ev.getActionCommand().equals("gray")) {
            GE.setColorSelected(Color.gray, kind);
            GE.repaint();
        } else if (ev.getActionCommand().equals("green")) {
            GE.setColorSelected(Color.green, kind);
            GE.repaint();
        } else if (ev.getActionCommand().equals("lightGray")) {
            GE.setColorSelected(Color.lightGray, kind);
            GE.repaint();
        } else if (ev.getActionCommand().equals("magenta")) {
            GE.setColorSelected(Color.magenta, kind);
            GE.repaint();
        } else if (ev.getActionCommand().equals("orange")) {
            GE.setColorSelected(Color.orange, kind);
            GE.repaint();
        } else if (ev.getActionCommand().equals("pink")) {
            GE.setColorSelected(Color.pink, kind);
            GE.repaint();
        } else if (ev.getActionCommand().equals("red")) {
            GE.setColorSelected(Color.red, kind);
            GE.repaint();
        } else if (ev.getActionCommand().equals("white")) {
            GE.setColorSelected(Color.white, kind);
            GE.repaint();
        } else if (ev.getActionCommand().equals("yellow")) {
            GE.setColorSelected(Color.yellow, kind);
            GE.repaint();
        } else if (ev.getActionCommand().equals("none")) {
            GE.setColorSelected(null, kind);
            GE.repaint();
        } else
            System.err.println("Not implemented: " + ev);
    }
}

class GraphEditorWindowListener implements WindowListener {
    final GraphEditor GE;

    /**
     * the constructor of GraphEditorWindowListener
     *
     * @param GE GraphEditor
     */
    GraphEditorWindowListener(GraphEditor GE) {
        this.GE = GE;
    }

    public void windowActivated(WindowEvent e) {
        // Basic.message("activiated: "+e);
    }

    public void windowClosed(WindowEvent e) {
        // Basic.message("closed: "+e);
        GraphEditor.numberEditors--;
        if (GraphEditor.getExitOnLastClose()
                && GraphEditor.getNumberEditors() == 0)
            System.exit(0);
    }

    public void windowClosing(WindowEvent e) {
        // ask whether graph should be saved here!
        // Basic.message("closing: "+e);
        GE.getFrame().dispose();
        GE.setFrame(null);
    }

    public void windowDeactivated(WindowEvent e) {
        // Basic.message("deactiviated: "+e);
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowOpened(WindowEvent e) {
        GraphEditor.numberEditors++;
        // Basic.message("opened: "+e);
    }
}

// EOF
