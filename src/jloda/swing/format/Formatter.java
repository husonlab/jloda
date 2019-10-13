/*
 * Formatter.java Copyright (C) 2019. Daniel H. Huson
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

package jloda.swing.format;

/**
 * format nodes and edges
 * Daniel Huson, 2.2007
 */

import jloda.graph.EdgeSet;
import jloda.graph.NodeSet;
import jloda.swing.commands.CommandManager;
import jloda.swing.director.IDirectableViewer;
import jloda.swing.director.IDirector;
import jloda.swing.graphview.*;
import jloda.swing.util.ChooseColorDialog;
import jloda.swing.window.WindowListenerAdapter;
import jloda.util.ProgramProperties;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;

/**
 * format nodes and edges
 */
public class Formatter implements IDirectableViewer {
    public static final String CONFIGURATOR_GEOMETRY = "ConfiguratorGeometry";

    private final java.util.List<IFormatterListener> formatterListeners = new LinkedList<>();

    private boolean isLocked = false;

    private boolean uptodate = false;
    private IDirector dir;
    private INodeEdgeFormatable viewer;
    private final FormatterActions actions;
    private final FormatterMenuBar menuBar;
    private final JFrame frame;
    static Cursor waitCursor = new Cursor(Cursor.WAIT_CURSOR);

    private static Formatter instance = null;

    private JComboBox<String> nodeSize, fontName, fontSize, edgeShape, edgeWidth;
    private JComboBox<NodeShape> nodeShape;
    private JCheckBox boldFont, italicFont, labels, foregroundColor, backgroundColor, labelForegroundColor,
            labelBackgroundColor;
    private JColorChooser colorChooser;

    private final JScrollBar alphaValueSBar = new JScrollBar(JScrollBar.HORIZONTAL, 255, 1, 0, 256);
    private boolean noAlphaBounce = false;

    /**
     * constructor
     *
     * @param dir               the director
     * @param viewer            the graph tree
     * @param showRotateButtons show label rotate buttons?
     */
    public Formatter(final IDirector dir, final INodeEdgeFormatable viewer, boolean showRotateButtons) {
        this.viewer = viewer;
        this.dir = dir;
        actions = new FormatterActions(this, dir, viewer);
        menuBar = new FormatterMenuBar(this, dir);
        setUptoDate(true);

        frame = new JFrame();
        frame.setIconImages(ProgramProperties.getProgramIconImages());
        frame.setJMenuBar(menuBar);
        frame.setLocationRelativeTo(viewer.getFrame());
        final int[] geometry = ProgramProperties.get(CONFIGURATOR_GEOMETRY, new int[]{100, 100, 585, 475});
        frame.setSize(geometry[2], geometry[3]);

        //dir.setViewerLocation(this);
        frame.setResizable(true);
        setTitle(dir);

        frame.getContentPane().add(getPanel(showRotateButtons));
        frame.setVisible(true);

        frame.addWindowListener(new WindowListenerAdapter() {
            public void windowActivated(WindowEvent windowEvent) {
                updateView("selection");
            }
        });
        frame.addWindowListener(new WindowListenerAdapter() {
            public void windowDeactivated(WindowEvent windowEvent) {
                dir.notifyUpdateViewer(IDirector.ENABLE_STATE);
            }
        });
        frame.addComponentListener(new ComponentAdapter() {
            public void componentMoved(ComponentEvent e) {
                componentResized(e);
            }

            public void componentResized(ComponentEvent event) {
                if ((event.getID() == ComponentEvent.COMPONENT_RESIZED || event.getID() == ComponentEvent.COMPONENT_MOVED) &&
                        (frame.getExtendedState() & JFrame.MAXIMIZED_HORIZ) == 0
                        && (frame.getExtendedState() & JFrame.MAXIMIZED_VERT) == 0) {
                    ProgramProperties.put(CONFIGURATOR_GEOMETRY, new int[]
                            {frame.getLocation().x, frame.getLocation().y, frame.getSize().width,
                                    frame.getSize().height});
                }
            }
        });

        final NodeActionListener nal = new NodeActionAdapter() {
            public void doSelect(NodeSet nodes) {
                // todo: update too expensive at present to call after change of selection
                //updateView("selection");
            }

            public void doDeselect(NodeSet nodes) {
                // updateView("selection");
            }

        };
        viewer.addNodeActionListener(nal);
        final EdgeActionListener eal = new EdgeActionAdapter() {
            public void doSelect(EdgeSet edges) {
                // updateView("selection");
            }

            public void doDeselect(EdgeSet edges) {
                //updateView("selection");
            }
        };
        viewer.addEdgeActionListener(eal);

        frame.addWindowListener(new WindowListenerAdapter() {
            public void windowClosing(WindowEvent event) {
                viewer.removeNodeActionListener(nal);
                viewer.removeEdgeActionListener(eal);
                dir.removeViewer(Formatter.this);
            }
        });
        updateView(IDirector.ENABLE_STATE);
    }

    /**
     * set the viewer to a new viewer.
     * If this is used, frame is set not to destroy itself
     *
     * @param dir
     * @param viewer
     */
    public void setViewer(IDirector dir, INodeEdgeFormatable viewer) {
        this.frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        this.viewer = viewer;
        this.dir = dir;
        actions.setViewer(dir, viewer);
        menuBar.setViewer(dir);
        setUptoDate(true);
        setTitle(dir);
    }

    /**
     * sets the title
     *
     * @param dir the director
     */
    public void setTitle(IDirector dir) {
        String newTitle;

        if (dir.getID() == 1)
            newTitle = "Format - " + dir.getTitle() + " - " + ProgramProperties.getProgramName();
        else
            newTitle = "Format - " + dir.getTitle() + " [" + dir.getID() + "] - " + ProgramProperties.getProgramName();
        if (!frame.getTitle().equals(newTitle))
            frame.setTitle(newTitle);
    }

    /**
     * returns the actions object associated with the window
     *
     * @return actions
     */

    public FormatterActions getActions() {
        return actions;
    }

    /**
     * is viewer uptodate?
     *
     * @return uptodate
     */
    public boolean isUptoDate() {
        return uptodate;
    }

    /**
     * ask tree to update itself. This is method is wrapped into a runnable object
     * and put in the swing event queue to avoid concurrent modifications.
     *
     * @param what is to be updated
     */
    public void updateView(String what) {
        if (what.equals(IDirector.TITLE)) {
            setTitle(dir);
            return;
        }

        if (isLocked) {
            return;
        }

        getActions().setIgnore(true); // only want to update stuff, ignore requests to perform events

        uptodate = false;
        getActions().setEnableCritical(true);
        getActions().updateEnableState();
        if (what.equals("selection") || what.equals(IDirector.ALL)) {

            int nSize = viewer.getWidthSelectedNodes();
            if (nSize == -1)
                nodeSize.setSelectedIndex(-1);
            else
                nodeSize.setSelectedItem(Integer.toString(nSize));
            int nShape = viewer.getShapeSelectedNodes();
            if (nShape == -1)
                nodeShape.setSelectedIndex(-1);
            else
                nodeShape.setSelectedIndex(nShape);

            Color color = null;
            int colorIsDefined = 0; // -1 over defined

            if (colorIsDefined != -1 && ((JCheckBox) actions.getForegroundColorAction(null).getValue(FormatterActions.CHECKBOXITEM)).isSelected()) {
                Color aColor = viewer.getColorSelectedNodes();
                if (aColor != null) {
                    if (colorIsDefined == 0) {
                        color = aColor;
                        colorIsDefined = 1;
                    } else if (!aColor.equals(color))
                        colorIsDefined = -1;
                }
                if (colorIsDefined != -1) {
                    aColor = viewer.getColorSelectedEdges();
                    if (aColor != null) {
                        if (colorIsDefined == 0) {
                            color = aColor;
                            colorIsDefined = 1;
                        } else if (!aColor.equals(color))
                            colorIsDefined = -1;
                    }
                }
            }
            if (colorIsDefined != -1 && ((JCheckBox) actions.getBackgroundColorAction(null).getValue(FormatterActions.CHECKBOXITEM)).isSelected()) {
                Color aColor = viewer.getBackgroundColorSelectedNodes();
                if (aColor != null) {
                    if (colorIsDefined == 0) {
                        color = aColor;
                        colorIsDefined = 1;
                    } else if (!aColor.equals(color))
                        colorIsDefined = -1;
                }
            }
            if (colorIsDefined != -1 && ((JCheckBox) actions.getLabelForegroundColorAction(null).getValue(FormatterActions.CHECKBOXITEM)).isSelected()) {
                Color aColor = viewer.getLabelColorSelectedNodes();
                if (aColor != null) {
                    if (colorIsDefined == 0) {
                        color = aColor;
                        colorIsDefined = 1;
                    } else if (!aColor.equals(color))
                        colorIsDefined = -1;
                }
                if (colorIsDefined != -1) {
                    aColor = viewer.getLabelColorSelectedEdges();
                    if (aColor != null) {
                        if (colorIsDefined == 0) {
                            color = aColor;
                            colorIsDefined = 1;
                        } else if (!aColor.equals(color))
                            colorIsDefined = -1;
                    }
                }
            }
            if (colorIsDefined != -1 && ((JCheckBox) actions.getLabelBackgroundColorAction(null).getValue(FormatterActions.CHECKBOXITEM)).isSelected()) {
                Color aColor = viewer.getLabelBackgroundColorSelectedNodes();
                if (aColor != null) {
                    if (colorIsDefined == 0) {
                        color = aColor;
                        colorIsDefined = 1;
                    } else if (!aColor.equals(color))
                        colorIsDefined = -1;
                }
                if (colorIsDefined != -1) {
                    aColor = viewer.getLabelBackgroundColorSelectedEdges();
                    if (aColor != null) {
                        if (colorIsDefined == 0) {
                            color = aColor;
                            colorIsDefined = 1;
                        } else if (!aColor.equals(color))
                            colorIsDefined = -1;
                    }
                }
            }
            noAlphaBounce = true;
            if (colorIsDefined == 1) {
                //System.err.println("Selected color: " + color);
                colorChooser.getSelectionModel().setSelectedColor(color);
                alphaValueSBar.setValue(color.getAlpha());
            } else
                alphaValueSBar.setValue(255);
            noAlphaBounce = false;

            Font font = viewer.getFontSelected();
            if (font == null) {
                fontSize.setSelectedIndex(-1);
                boldFont.setSelected(false);
                italicFont.setSelected(false);
                fontName.setSelectedIndex(-1);
            } else {
                fontSize.setSelectedItem(Integer.toString(font.getSize()));
                if (font.getStyle() == Font.BOLD) {
                    boldFont.setSelected(true);
                    italicFont.setSelected(false);
                }
                if (font.getStyle() == Font.ITALIC) {
                    boldFont.setSelected(false);
                    italicFont.setSelected(true);
                }
                if (font.getStyle() == Font.ITALIC + Font.BOLD) {
                    boldFont.setSelected(true);
                    italicFont.setSelected(true);
                }
                if (font.getStyle() == Font.PLAIN) {
                    boldFont.setSelected(false);
                    italicFont.setSelected(false);
                }
                fontName.setSelectedItem(font.getName());
            }

            int eWidth = viewer.getLineWidthSelectedEdges();

            if (eWidth == -1)
                edgeWidth.setSelectedIndex(-1);
            else
                edgeWidth.setSelectedItem(Integer.toString(eWidth));

            int eShape = viewer.getShapeSelectedEdges();
            if (eShape == -1)
                edgeShape.setSelectedIndex(-1);
            else {
                int i = 0;
                if (eShape == EdgeView.STRAIGHT_EDGE)
                    i = 1;
                else if (eShape == EdgeView.QUAD_EDGE)
                    i = 2;
                edgeShape.setSelectedIndex(i);
            }
            labels.setSelected(viewer.hasLabelVisibleSelectedNodes() || viewer.hasLabelVisibleSelectedEdges());

            getActions().getSaveDefaultFont().setEnabled(fontName.getSelectedIndex() != -1);
            frame.repaint();
            getActions().setIgnore(false); // ignore firing of events
        }

        colorChooser.setEnabled(viewer.hasSelectedNodes() || viewer.hasSelectedEdges());
        uptodate = true;
    }

    /**
     * ask tree to prevent user input
     */

    public void lockUserInput() {
        isLocked = true;
        getActions().setEnableCritical(false);
        frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        colorChooser.setEnabled(false);
        frame.getContentPane().setEnabled(false);
    }

    /**
     * ask tree to allow user input
     */
    public void unlockUserInput() {
        colorChooser.setEnabled(true);
        frame.setCursor(Cursor.getDefaultCursor());
        isLocked = false;
    }

    /**
     * ask tree to destroy itself
     */
    public void destroyView() {
        dir.removeViewer(this);
        frame.dispose();
    }

    /**
     * set uptodate state
     *
     * @param flag
     */
    public void setUptoDate(boolean flag) {
        uptodate = flag;
    }

    /**
     * returns the frame of the window
     */
    public JFrame getFrame() {
        return frame;
    }

    private JPanel getPanel(boolean showRotateButtons) {
        JPanel topPanel = new JPanel();
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        //topPanel.setLayout(new GridLayout(5,1));

        JPanel fontPanel = new JPanel();
        fontPanel.setLayout(new BoxLayout(fontPanel, BoxLayout.X_AXIS));
        fontPanel.add(new JLabel("Font:"));
        fontPanel.add(fontName = makeFont());
        fontPanel.add(new JLabel("Size:"));
        fontPanel.add(fontSize = makeFontSize());
        fontPanel.add(boldFont = makeBold());
        fontPanel.add(italicFont = makeItalic());
        fontPanel.setPreferredSize(new Dimension(600, 30));
        fontPanel.setMinimumSize(fontPanel.getPreferredSize());
        fontPanel.setMaximumSize(fontPanel.getPreferredSize());
        topPanel.add(fontPanel);

        JPanel colorPanel0 = new JPanel();
        colorPanel0.setLayout(new BoxLayout(colorPanel0, BoxLayout.Y_AXIS));
        colorPanel0.setBorder(BorderFactory.createEtchedBorder());

        JPanel colorPanel1 = new JPanel();
        colorPanel1.setLayout(new BoxLayout(colorPanel1, BoxLayout.X_AXIS));
        colorPanel1.add(colorChooser = makeColor());
        JPanel colorSubPanel = new JPanel();
        colorSubPanel.setBorder(BorderFactory.createEtchedBorder());
        colorSubPanel.setLayout(new GridLayout(4, 2));
        colorSubPanel.add(foregroundColor = makeForegroundColor());
        foregroundColor.setText("Line Color");
        foregroundColor.setSelected(true);
        colorSubPanel.add(backgroundColor = makeBackgroundColor());
        backgroundColor.setText("Fill Color");
        colorSubPanel.add(labelForegroundColor = makeLabelForegroundColor());
        labelForegroundColor.setText("Label Color");
        colorSubPanel.add(labelBackgroundColor = makeLabelBackgroundColor());
        labelBackgroundColor.setText("Label Fill Color");
        colorPanel1.add(colorSubPanel);
        colorPanel0.add(colorPanel1);

        JPanel colorPanel2 = new JPanel();
        colorPanel2.setLayout(new BoxLayout(colorPanel2, BoxLayout.X_AXIS));
        colorPanel2.add(new JLabel("Alpha:"));
        colorPanel2.add(alphaValueSBar);
        alphaValueSBar.addAdjustmentListener(adjustmentEvent -> {
            if (!noAlphaBounce && !adjustmentEvent.getValueIsAdjusting()) {
                System.err.println("Changed");
                colorStateChanged();
            }
        });

        colorPanel2.add(new JButton(actions.getRandomColorActionAction()));
        colorPanel2.add(new JButton(actions.getNoColorActionAction()));
        colorPanel2.add(new JButton(actions.getApplyColorAction()));
        colorPanel0.add(colorPanel2);

        topPanel.add(colorPanel0);

        JPanel nodePanel = new JPanel();
        nodePanel.setLayout(new BoxLayout(nodePanel, BoxLayout.X_AXIS));
        nodePanel.add(new JLabel("Node size: "));
        nodePanel.add(nodeSize = makeNodeSize());
        nodePanel.add(new JLabel("Node shape:"));
        nodePanel.add(nodeShape = makeNodeShape());
        topPanel.add(nodePanel);

        JPanel edgePanel = new JPanel();
        edgePanel.setLayout(new BoxLayout(edgePanel, BoxLayout.X_AXIS));
        edgePanel.add(new JLabel("Edge width:"));
        edgePanel.add(edgeWidth = makeEdgeWidth());
        edgePanel.add(new JLabel("Edge Style:"));
        edgePanel.add(edgeShape = makeEdgeShape());
        topPanel.add(edgePanel);

        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
        labelPanel.add(new JLabel("Show Labels:"));
        labelPanel.add(labels = makeLabels());
        // labels.setText("Show Labels");
        if (showRotateButtons) {
            labelPanel.add(new JLabel("   Rotate Node Labels: "));
            JButton rotateLabelsLeft;
            labelPanel.add(rotateLabelsLeft = new JButton(actions.getRotateLabelsLeft()));
            JButton rotateLabelsRight;
            labelPanel.add(rotateLabelsRight = new JButton(actions.getRotateLabelsRight()));
        }
        topPanel.add(labelPanel);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEtchedBorder());
        bottomPanel.add(new JButton(actions.getClose()), BorderLayout.EAST);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        return panel;
    }

    /**
     * @return Returns the viewer.
     */
    public INodeEdgeFormatable getViewer() {
        return viewer;
    }

    private JComboBox<String> makeNodeSize() {
        String[] possibleValues = {"1", "2", "3", "4", "5", "6", "7", "8", "10"};
        JComboBox<String> box = new JComboBox<>(possibleValues);
        box.setEditable(true);
        box.setMinimumSize(box.getPreferredSize());
        box.setAction(actions.getNodeSize());
        return box;
    }

    private JComboBox<NodeShape> makeNodeShape() {
        JComboBox<NodeShape> box = new JComboBox<>(NodeShape.values());
        box.setMinimumSize(box.getPreferredSize());
        box.setAction(actions.getNodeShape());
        return box;
    }


    private JComboBox<String> makeEdgeShape() {
        JComboBox<String> box = new JComboBox<>(new String[]{"angular", "straight", "curved"});
        box.setMinimumSize(box.getPreferredSize());
        box.setAction(actions.getEdgeShape());
        return box;
    }

    private JComboBox<String> makeFont() {
        JComboBox<String> box = new JComboBox<>(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        box.setAction(actions.getFont());
        box.setMinimumSize(box.getPreferredSize());
        return box;
    }

    private JComboBox<String> makeFontSize() {
        String[] possibleValues = {"8", "10", "12", "14", "16", "18", "20", "22", "24", "26", "28", "32", "36", "40", "44"};
        JComboBox<String> box = new JComboBox<>(possibleValues);
        box.setEditable(true);
        box.setAction(actions.getFontSize());
        box.setMinimumSize(box.getPreferredSize());
        return box;
    }

    private JCheckBox makeBold() {
        JCheckBox box = new JCheckBox("Bold");
        box.setAction(actions.getNodeFontBold());
        return box;
    }

    private JCheckBox makeItalic() {
        JCheckBox box = new JCheckBox("Italic");
        box.setAction(actions.getNodeFontItalic());
        return box;
    }


    private JCheckBox makeLabels() {
        JCheckBox box = new JCheckBox();
        box.setAction(actions.getShowLabels(box));
        return box;
    }

    private JCheckBox makeForegroundColor() {
        JCheckBox cbox = new JCheckBox();
        cbox.setAction(actions.getForegroundColorAction(cbox));
        return cbox;
    }

    private JCheckBox makeBackgroundColor() {
        JCheckBox cbox = new JCheckBox();
        cbox.setAction(actions.getBackgroundColorAction(cbox));
        return cbox;
    }

    private JCheckBox makeLabelForegroundColor() {
        JCheckBox cbox = new JCheckBox();
        cbox.setAction(actions.getLabelForegroundColorAction(cbox));
        return cbox;
    }

    private JCheckBox makeLabelBackgroundColor() {
        JCheckBox cbox = new JCheckBox();
        cbox.setAction(actions.getLabelBackgroundColorAction(cbox));
        return cbox;
    }

    private JColorChooser makeColor() {
        final JColorChooser chooser = ChooseColorDialog.colorChooser;

        chooser.setPreviewPanel(new JPanel());

        chooser.getSelectionModel().addChangeListener(ev -> colorStateChanged());
        return chooser;
    }

    private void colorStateChanged() {
        boolean changed = false;
        Color color = getColor();
        if (viewer.hasSelectedNodes()) {
            if (foregroundColor.isSelected()) {
                if (viewer.setColorSelectedNodes(color))
                    changed = true;
            }
            if (backgroundColor.isSelected()) {
                if (viewer.setBackgroundColorSelectedNodes(color)) changed = true;
            }
            if (labelForegroundColor.isSelected()) {
                if (viewer.setLabelColorSelectedNodes(color)) changed = true;
            }
            if (labelBackgroundColor.isSelected()) {
                if (viewer.setLabelBackgroundColorSelectedNodes(color)) changed = true;
            }
            if (changed)
                fireNodeFormatChanged(viewer.getSelectedNodes());
        }
        if (viewer.hasSelectedEdges()) {
            if (foregroundColor.isSelected()) {
                if (viewer.setColorSelectedEdges(color)) changed = true;
            }
            if (labelForegroundColor.isSelected()) {
                if (viewer.setLabelColorSelectedEdges(color)) changed = true;
            }
            if (labelBackgroundColor.isSelected()) {
                if (viewer.setLabelBackgroundColorSelectedEdges(color)) changed = true;
            }
            if (changed)
                fireEdgeFormatChanged(viewer.getSelectedEdges());
        }
        if (changed) {
            dir.setDirty(true);
            viewer.repaint();
        }
    }

    private JComboBox makeEdgeWidth() {
        Object[] possibleValues = {"1", "2", "3", "4", "5", "6", "7", "8", "10"};
        JComboBox box = new JComboBox(possibleValues);
        box.setEditable(true);
        box.setMinimumSize(box.getPreferredSize());
        box.setAction(actions.getEdgeWidth());
        return box;
    }

    /**
     * gets the title of this viewer
     *
     * @return title
     */
    public String getTitle() {
        return frame.getTitle();
    }

    /**
     * fire node format changed
     *
     * @param nodes
     */
    void fireNodeFormatChanged(NodeSet nodes) {
        if (nodes != null && nodes.size() > 0) {
            for (Object formatterListener : formatterListeners) {
                IFormatterListener listener = (IFormatterListener) formatterListener;
                listener.nodeFormatChanged(nodes);
            }
        }
    }

    /**
     * fire edge format changed
     *
     * @param edges
     */
    void fireEdgeFormatChanged(EdgeSet edges) {
        if (edges != null && edges.size() > 0) {
            for (Object formatterListener : formatterListeners) {
                IFormatterListener listener = (IFormatterListener) formatterListener;
                listener.edgeFormatChanged(edges);
            }
        }
    }

    /**
     * add a formatter listener
     *
     * @param listener
     */
    public void addFormatterListener(IFormatterListener listener) {
        formatterListeners.add(listener);
    }

    /**
     * remove a formatter listener
     *
     * @param listener
     */
    public void removeFormatterListener(IFormatterListener listener) {
        formatterListeners.remove(listener);
    }

    public void saveFontAsDefault() {
        try {
            String family = fontName.getSelectedItem().toString();
            int size = Integer.parseInt(fontSize.getSelectedItem().toString());
            if (size > 0) {
                boolean bold = boldFont.isSelected();
                boolean italics = italicFont.isSelected();
                int style = 0;
                if (bold)
                    style += Font.BOLD;
                if (italics)
                    style += Font.ITALIC;
                ProgramProperties.put(ProgramProperties.DEFAULT_FONT, family, style, size);
            }
        } catch (Exception ex) {
        }
    }

    public JColorChooser getColorChooser() {
        return colorChooser;
    }

    public static Formatter getInstance() {
        return instance;
    }

    public static void setInstance(Formatter instance) {
        Formatter.instance = instance;
    }

    public IDirector getDir() {
        return dir;
    }

    public CommandManager getCommandManager() {
        return null;
    }

    /**
     * is viewer currently locked?
     *
     * @return true, if locked
     */
    public boolean isLocked() {
        return isLocked;
    }

    protected Color getColor() {
        if (alphaValueSBar.getValue() == 255)
            return colorChooser.getColor();
        else {
            Color color = colorChooser.getColor();
            return new Color(color.getRed(), color.getGreen(), color.getBlue(), alphaValueSBar.getValue());
        }
    }

    /**
     * get the name of the class
     *
     * @return class name
     */
    @Override
    public String getClassName() {
        return "Formatter";
    }
}
