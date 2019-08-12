/*
 * FormatterActions.java Copyright (C) 2019. Daniel H. Huson
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

import jloda.swing.director.IDirector;
import jloda.swing.export.TransferableGraphic;
import jloda.swing.graphview.EdgeView;
import jloda.swing.graphview.INodeEdgeFormatable;
import jloda.swing.graphview.NodeShape;
import jloda.swing.util.Alert;
import jloda.swing.util.ResourceManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.List;

/**
 * actions associated with a node-edge-configurator window
 */
public class FormatterActions {
    public final static String CHECKBOXITEM = "CheckBox";
    public final static String DEPENDS_ON_NODESELECTION = "NSEL";
    public final static String DEPENDS_ON_ONE_NODE_OR_EDGE = "ONORE";
    public final static String DEPENDS_ON_NODE_OR_EDGE = "NORE";
    public final static String DEPENDS_ON_XYLOCKED = "LOCK";
    public final static String DEPENDS_ON_NOT_XYLOCKED = "NLOCK";
    public final static String DEPENDS_ON_EDGESELECTION = "ESEL";
    public final static String TEXTAREA = "TA"; // text area object
    public final static String CRITICAL = "Critical"; // is action critical? bool

    private final Formatter formatter;
    private IDirector dir;
    private final List<AbstractAction> all = new LinkedList<>();
    private INodeEdgeFormatable viewer;

    private boolean ignore = false; // ignore firing when in update only of controls

    /**
     * constructor
     *
     * @param formatter
     * @param dir
     */
    FormatterActions(Formatter formatter, IDirector dir, INodeEdgeFormatable viewer) {
        this.formatter = formatter;
        this.dir = dir;
        this.viewer = viewer;
    }

    public void setViewer(IDirector dir, INodeEdgeFormatable viewer) {
        this.viewer = viewer;
        this.dir = dir;
    }

    /**
     * enable or disable critical actions
     *
     * @param on show or hide?
     */
    public void setEnableCritical(boolean on) {
        if (viewer == null)
            on = false;
        for (Action action : all) {
            if (viewer == null || action.getValue(CRITICAL) != null
                    && action.getValue(CRITICAL).equals(Boolean.TRUE))
                action.setEnabled(on);
        }
        if (on)
            updateEnableState();
    }

    /**
     * This is where we update the enable state of all actions!
     */
    public void updateEnableState() {
        for (AbstractAction action : all) {
            Boolean dependsOnNodeSelection = (Boolean) action.getValue(DEPENDS_ON_NODESELECTION);
            Boolean dependsOnEdgeSelection = (Boolean) action.getValue(DEPENDS_ON_EDGESELECTION);
            Boolean dependsOnOneNodeOrEdge = (Boolean) action.getValue(DEPENDS_ON_ONE_NODE_OR_EDGE);
            Boolean dependsOnNodeOrEdge = (Boolean) action.getValue(DEPENDS_ON_NODE_OR_EDGE);
            Boolean dependsOnXYLocked = (Boolean) action.getValue(DEPENDS_ON_XYLOCKED);

            action.setEnabled(true);
            if (dependsOnNodeSelection != null && dependsOnNodeSelection) {
                boolean enable = (viewer.hasSelectedNodes());
                action.setEnabled(enable);
            }
            if (dependsOnEdgeSelection != null && dependsOnEdgeSelection) {
                boolean enable = (viewer.hasSelectedEdges());
                action.setEnabled(enable);
            }
            if (dependsOnXYLocked != null && dependsOnXYLocked) {
                action.setEnabled(viewer.getLockXYScale());
            }
            if (dependsOnNodeOrEdge != null && dependsOnNodeOrEdge) {
                boolean enable = (viewer.hasSelectedNodes()) || (viewer.hasSelectedEdges());
                action.setEnabled(enable);
            }
        }
    }

    /**
     * returns all actions
     *
     * @return actions
     */
    public List getAll() {
        return all;
    }

    // here we define the configurator window specific actions:

    private AbstractAction close;

    /**
     * close this viewer
     *
     * @return close action
     */
    public AbstractAction getClose() {
        AbstractAction action = close;
        if (action != null) return action;

        action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                dir.removeViewer(formatter);
                formatter.getFrame().setVisible(false);
                formatter.getFrame().dispose();
            }
        };
        action.putValue(AbstractAction.NAME, "Close");
        action.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_W,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        action.putValue(AbstractAction.MNEMONIC_KEY, (int) 'C');
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Close this window");
        action.putValue(AbstractAction.SMALL_ICON, ResourceManager.getIcon("Close16.gif"));
        // close is critical because we can't easily kill the worker thread

        all.add(action);
        return close = action;
    }

    private AbstractAction edgeWidth;

    public AbstractAction getEdgeWidth() {
        AbstractAction action = edgeWidth;
        if (action != null) return action;

        action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                if (!ignore) {
                    Object selectedValue = ((JComboBox) event.getSource()).getSelectedItem();
                    if (selectedValue != null) {
                        byte size = 1;
                        try {
                            size = Byte.parseByte((String) selectedValue);
                        } catch (Exception ex) {
                        }
                        viewer.setLineWidthSelectedEdges(size);
                        dir.setDirty(true);
                        formatter.fireEdgeFormatChanged(viewer.getSelectedEdges());

                    }
                    viewer.repaint();
                }
            }
        };
        action.putValue(AbstractAction.NAME, "Edge Width");
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Set edge width");
        action.putValue(CRITICAL, Boolean.TRUE);
        action.putValue(DEPENDS_ON_EDGESELECTION, Boolean.TRUE);
        all.add(action);
        return edgeWidth = action;
    }

    private AbstractAction showLabels;

    public AbstractAction getShowLabels(final JCheckBox cbox) {
        AbstractAction action = showLabels;
        if (action != null) return action;

        action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                if (!ignore) {
                    viewer.setLabelVisibleSelectedNodes(cbox.isSelected());
                    formatter.fireNodeFormatChanged(viewer.getSelectedNodes());
                    viewer.setLabelVisibleSelectedEdges(cbox.isSelected());
                    formatter.fireEdgeFormatChanged(viewer.getSelectedEdges());
                    viewer.repaint();
                    dir.setDirty(true);
                }
            }
        };
        //action.putValue(AbstractAction.NAME, "Labels");
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Show labels");
        action.putValue(CRITICAL, Boolean.TRUE);
        action.putValue(DEPENDS_ON_NODE_OR_EDGE, Boolean.TRUE);
        all.add(action);
        return showLabels = action;
    }

    private AbstractAction font;

    public AbstractAction getFont() {
        AbstractAction action = font;
        if (action != null) return action;

        action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                if (!ignore) {
                    Object selectedValue = ((JComboBox) event.getSource()).getSelectedItem();
                    if (selectedValue != null) {
                        String family = selectedValue.toString();
                        boolean changed = false;
                        if (setNodeFont(family, -1, -1, -1))
                            changed = true;
                        if (setEdgeFont(family, -1, -1, -1))
                            changed = true;
                        if (changed) {
                            viewer.repaint();
                            dir.setDirty(true);
                        }
                    }
                }
            }
        };
        action.putValue(AbstractAction.NAME, "Font");
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Set label font");
        action.putValue(CRITICAL, Boolean.TRUE);
        action.putValue(DEPENDS_ON_NODE_OR_EDGE, Boolean.TRUE);
        all.add(action);
        return font = action;
    }

    private AbstractAction fontSize;

    public Action getFontSize() {
        AbstractAction action = fontSize;
        if (action != null) return action;

        action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                if (!ignore && event != null && (event.getActionCommand() == null || event.getActionCommand().equals("comboBoxChanged"))) {
                    Object source = event.getSource();
                    if (source instanceof JComboBox) {
                        Object selectedValue = ((JComboBox) event.getSource()).getSelectedItem();
                        if (selectedValue != null) {
                            int size;
                            try {
                                size = Integer.parseInt((String) selectedValue);
                            } catch (NumberFormatException e) {
                                new Alert(formatter.getFrame(), "Font Size must be an integer! Size set to 10.");
                                size = 10;
                                ((JComboBox) event.getSource()).setSelectedItem("10");
                            }
                            boolean changed = false;
                            if (setNodeFont(null, -1, -1, size))
                                changed = true;
                            if (setEdgeFont(null, -1, -1, size))
                                changed = true;
                            if (changed) {
                                viewer.repaint();
                                dir.setDirty(true);
                            }
                        }
                    }
                }
            }
        };
        action.putValue(AbstractAction.NAME, "Font Size");
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Set label font size");
        action.putValue(CRITICAL, Boolean.TRUE);
        action.putValue(DEPENDS_ON_NODE_OR_EDGE, Boolean.TRUE);
        all.add(action);
        return fontSize = action;
    }

    private AbstractAction bold;

    public Action getNodeFontBold() {
        AbstractAction action = bold;
        if (action != null) return action;

        action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                if (!ignore) {
                    int state = ((JCheckBox) event.getSource()).isSelected() ? 1 : 0;
                    boolean changed = false;
                    if (setNodeFont(null, state, -1, -1))
                        changed = true;
                    if (setEdgeFont(null, state, -1, -1))
                        changed = true;
                    if (changed) {
                        viewer.repaint();
                        dir.setDirty(true);
                    }
                }
            }
        };
        action.putValue(AbstractAction.NAME, "Bold");
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Set label font bold");
        action.putValue(CRITICAL, Boolean.TRUE);
        action.putValue(DEPENDS_ON_NODE_OR_EDGE, Boolean.TRUE);
        all.add(action);
        return bold = action;
    }

    private AbstractAction italic;

    public Action getNodeFontItalic() {
        AbstractAction action = italic;
        if (action != null) return action;

        action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                if (!ignore) {
                    int state = ((JCheckBox) event.getSource()).isSelected() ? 1 : 0;
                    boolean changed = false;
                    if (setNodeFont(null, -1, state, -1))
                        changed = true;
                    if (setEdgeFont(null, -1, state, -1))
                        changed = true;
                    if (changed) {
                        viewer.repaint();
                        dir.setDirty(true);
                    }
                }
            }
        };
        action.putValue(AbstractAction.NAME, "Italic");
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Set label font italic");
        action.putValue(CRITICAL, Boolean.TRUE);
        action.putValue(DEPENDS_ON_NODE_OR_EDGE, Boolean.TRUE);
        all.add(action);
        return italic = action;
    }

    private AbstractAction nodeSize;

    public AbstractAction getNodeSize() {
        AbstractAction action = nodeSize;
        if (action != null) return action;

        action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                if (!ignore) {
                    Object selectedValue = ((JComboBox) event.getSource()).getSelectedItem();
                    if (selectedValue != null) {
                        Byte size = 1;
                        try {
                            size = Byte.parseByte((String) selectedValue);
                        } catch (Exception ex) {
                        }
                        viewer.setWidthSelectedNodes(size);
                        viewer.setHeightSelectedNodes(size);
                        formatter.fireNodeFormatChanged(viewer.getSelectedNodes());
                    }
                    viewer.repaint();
                    dir.setDirty(true);
                }
            }
        };
        action.putValue(AbstractAction.NAME, "Node Size");
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Set node size");
        action.putValue(CRITICAL, Boolean.TRUE);
        action.putValue(DEPENDS_ON_NODESELECTION, Boolean.TRUE);
        all.add(action);
        return nodeSize = action;
    }

    private AbstractAction nodeShape;

    public Action getNodeShape() {

        AbstractAction action = nodeShape;
        if (action != null) return action;

        action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                if (!ignore) {
                    NodeShape shape = (NodeShape) ((JComboBox) event.getSource()).getSelectedItem();
                    if (shape != null) {
                        viewer.setShapeSelectedNodes((byte) shape.ordinal());
                        formatter.fireNodeFormatChanged(viewer.getSelectedNodes());
                        viewer.repaint();
                        dir.setDirty(true);
                    }
                }
            }
        };
        action.putValue(AbstractAction.NAME, "Node Shape");
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Set node shape");
        action.putValue(CRITICAL, Boolean.TRUE);
        action.putValue(DEPENDS_ON_NODESELECTION, Boolean.TRUE);
        all.add(action);
        return nodeShape = action;
    }

    private AbstractAction edgeShape;

    public Action getEdgeShape() {

        AbstractAction action = edgeShape;
        if (action != null) return action;

        action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                if (!ignore) {
                    Object selectedValue = ((JComboBox) event.getSource()).getSelectedItem();
                    if (selectedValue != null) {
                        // todo: this kind of operation should only apply to uncollapsed nodes
                        byte shape = -1;
                        if (selectedValue == "angular") shape = EdgeView.POLY_EDGE;
                        if (selectedValue == "straight") shape = EdgeView.STRAIGHT_EDGE;
                        if (selectedValue == "curved") shape = EdgeView.QUAD_EDGE;
                        viewer.setShapeSelectedEdges(shape);
                        formatter.fireEdgeFormatChanged(viewer.getSelectedEdges());
                    }
                    viewer.repaint();
                    dir.setDirty(true);
                }
            }
        };
        action.putValue(AbstractAction.NAME, "Edge Shape");
        action.putValue(CRITICAL, Boolean.TRUE);
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Set edge shape");
        action.putValue(DEPENDS_ON_EDGESELECTION, Boolean.TRUE);
        all.add(action);
        return edgeShape = action;
    }

    private AbstractAction rotateLabelsLeft = getRotateLabelsLeft();

    public AbstractAction getRotateLabelsLeft() {
        AbstractAction action = rotateLabelsLeft;
        if (action != null) return action;

        action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                viewer.rotateLabelsSelectedNodes(-1);
                formatter.fireNodeFormatChanged(viewer.getSelectedNodes());
                viewer.rotateLabelsSelectedEdges(-1);
                formatter.fireEdgeFormatChanged(viewer.getSelectedEdges());
                dir.setDirty(true);
                viewer.repaint();
            }
        };
        action.putValue(AbstractAction.NAME, "Left");
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Rotate labels left");
        action.putValue(CRITICAL, Boolean.TRUE);
        action.putValue(AbstractAction.SMALL_ICON, ResourceManager.getIcon("RotateLeft16.gif"));
        action.putValue(DEPENDS_ON_NODESELECTION, Boolean.TRUE);
        all.add(action);
        return rotateLabelsLeft = action;
    }

    private AbstractAction rotateLabelsRight = getRotateLabelsRight();

    public AbstractAction getRotateLabelsRight() {
        AbstractAction action = rotateLabelsRight;
        if (action != null) return action;

        action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                viewer.rotateLabelsSelectedNodes(1);
                formatter.fireNodeFormatChanged(viewer.getSelectedNodes());
                viewer.rotateLabelsSelectedEdges(1);
                formatter.fireEdgeFormatChanged(viewer.getSelectedEdges());

                dir.setDirty(true);
                viewer.repaint();

            }
        };
        action.putValue(AbstractAction.NAME, "Right");
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Rotate labels right");
        action.putValue(CRITICAL, Boolean.TRUE);
        action.putValue(AbstractAction.SMALL_ICON, ResourceManager.getIcon("RotateRight16.gif"));
        action.putValue(DEPENDS_ON_NODESELECTION, Boolean.TRUE);
        all.add(action);
        return rotateLabelsRight = action;
    }

    /**
     * get ignore firing of events
     *
     * @return true, if we are currently ignoring firing of events
     */
    public boolean getIgnore() {
        return ignore;
    }

    /**
     * set ignore firing of events
     *
     * @param ignore
     */
    public void setIgnore(boolean ignore) {
        this.ignore = ignore;
    }

    /**
     * set the edge font
     *
     * @param family
     * @param bold
     * @param italics
     * @param size
     * @return true, if anything changed
     */
    public boolean setEdgeFont(String family, int bold, int italics, int size) {
        if (viewer.setFontSelectedEdges(family, bold, italics, size)) {
            formatter.fireEdgeFormatChanged(viewer.getSelectedEdges());
            return true;
        }
        return false;
    }

    /**
     * set the node font
     *
     * @param family
     * @param bold
     * @param italics
     * @param size
     * @return true, if anything changed
     */
    public boolean setNodeFont(String family, int bold, int italics, final int size) {
        if (viewer.setFontSelectedNodes(family, bold, italics, size)) {
            formatter.fireNodeFormatChanged(viewer.getSelectedNodes());
            return true;
        }
        return false;
    }

    private AbstractAction cut = getCut();

    public AbstractAction getCut() {
        AbstractAction action = cut;
        if (action != null) return action;

        action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                TransferableGraphic tg = new TransferableGraphic(viewer.getPanel());
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(tg, tg);
            }
        };
        action.putValue(AbstractAction.NAME, "Cut");
        action.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() /*| java.awt.event.InputEvent.SHIFT_DOWN_MASK*/));
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Cut");
        action.putValue(AbstractAction.SMALL_ICON, ResourceManager.getIcon("sun/Cut16.gif"));
        action.putValue(CRITICAL, Boolean.TRUE);
        all.add(action);
        return cut = action;
    }

    private AbstractAction copy = getCopy();

    public AbstractAction getCopy() {
        AbstractAction action = copy;
        if (action != null) return action;

        action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {

                TransferableGraphic tg = new TransferableGraphic(viewer.getPanel(), viewer.getScrollPane());
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(tg, tg);
            }
        };
        action.putValue(AbstractAction.NAME, "Copy");
        action.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() /*| java.awt.event.InputEvent.SHIFT_DOWN_MASK*/));
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Copy graph to clipboard");
        action.putValue(CRITICAL, Boolean.TRUE);
        action.putValue(AbstractAction.SMALL_ICON, ResourceManager.getIcon("sun/Copy16.gif"));
        all.add(action);
        return copy = action;
    }


    private AbstractAction paste = getPaste();

    public AbstractAction getPaste() {
        AbstractAction action = paste;
        if (action != null) return action;

        action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                TransferableGraphic tg = new TransferableGraphic(viewer.getPanel());
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(tg, tg);
            }
        };
        action.putValue(AbstractAction.NAME, "Paste");
        action.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Paste");
        action.putValue(CRITICAL, Boolean.TRUE);
        action.putValue(AbstractAction.SMALL_ICON, ResourceManager.getIcon("sun/Paste16.gif"));
        all.add(action);
        return paste = action;
    }

    private AbstractAction saveDefaultFont = getSaveDefaultFont();

    public AbstractAction getSaveDefaultFont() {
        AbstractAction action = saveDefaultFont;
        if (action != null) return action;

        action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                formatter.saveFontAsDefault();
            }
        };
        action.putValue(AbstractAction.NAME, "Set Font as Default");
        action.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Set current font as default");
        action.putValue(CRITICAL, Boolean.TRUE);
        action.putValue(AbstractAction.SMALL_ICON, ResourceManager.getIcon("Empty16.gif"));
        all.add(action);
        return saveDefaultFont = action;
    }

    private AbstractAction foregroundColorAction;

    public AbstractAction getForegroundColorAction(final JCheckBox cbox) {
        AbstractAction action = foregroundColorAction;
        if (action != null) return action;

        action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
            }
        };
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Color");
        action.putValue(DEPENDS_ON_NODE_OR_EDGE, Boolean.TRUE);
        action.putValue(CRITICAL, Boolean.TRUE);
        action.putValue(CHECKBOXITEM, cbox);
        all.add(action);
        return foregroundColorAction = action;
    }

    private AbstractAction backgroundColorAction;

    public AbstractAction getBackgroundColorAction(final JCheckBox cbox) {
        AbstractAction action = backgroundColorAction;
        if (action != null) return action;

        action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
            }
        };
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Back Color");
        action.putValue(CHECKBOXITEM, cbox);
        action.putValue(CRITICAL, Boolean.TRUE);
        action.putValue(DEPENDS_ON_NODE_OR_EDGE, Boolean.TRUE);
        action.putValue(CHECKBOXITEM, cbox);
        all.add(action);
        return backgroundColorAction = action;
    }

    private AbstractAction labelForegroundColorAction;

    public AbstractAction getLabelForegroundColorAction(final JCheckBox cbox) {
        AbstractAction action = labelForegroundColorAction;
        if (action != null) return action;

        action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
            }
        };
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Label Color");
        action.putValue(CHECKBOXITEM, cbox);
        action.putValue(DEPENDS_ON_NODE_OR_EDGE, Boolean.TRUE);
        action.putValue(CRITICAL, Boolean.TRUE);
        action.putValue(CHECKBOXITEM, cbox);
        all.add(action);
        return labelForegroundColorAction = action;
    }

    private AbstractAction labelBackgroundColorAction;

    public AbstractAction getLabelBackgroundColorAction(final JCheckBox cbox) {
        AbstractAction action = labelBackgroundColorAction;
        if (action != null) return action;

        action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
            }
        };
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Label Back Color");
        action.putValue(DEPENDS_ON_NODE_OR_EDGE, Boolean.TRUE);
        action.putValue(CRITICAL, Boolean.TRUE);
        action.putValue(CHECKBOXITEM, cbox);
        all.add(action);
        return labelBackgroundColorAction = action;
    }

    private AbstractAction randomColorActionAction;

    public AbstractAction getRandomColorActionAction() {
        AbstractAction action = randomColorActionAction;
        if (action != null) return action;

        action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                boolean changed = false;
                boolean fore = ((JCheckBox) foregroundColorAction.getValue(CHECKBOXITEM)).isSelected();
                boolean back = ((JCheckBox) backgroundColorAction.getValue(CHECKBOXITEM)).isSelected();
                boolean labelfore = ((JCheckBox) labelForegroundColorAction.getValue(CHECKBOXITEM)).isSelected();
                boolean labelback = ((JCheckBox) labelBackgroundColorAction.getValue(CHECKBOXITEM)).isSelected();

                if (viewer.hasSelectedNodes()) {
                    viewer.setRandomColorsSelectedNodes(fore, back, labelfore, labelback);
                    formatter.fireNodeFormatChanged(viewer.getSelectedNodes());
                    changed = true;
                }

                if (viewer.hasSelectedEdges()) {
                    viewer.setRandomColorsSelectedEdges(fore, labelfore, labelback);
                    formatter.fireEdgeFormatChanged(viewer.getSelectedEdges());
                    changed = true;
                }

                if (changed) {
                    dir.setDirty(true);
                    viewer.repaint();
                }
            }
        };
        action.putValue(AbstractAction.NAME, "Random Colors");
        action.putValue(CRITICAL, Boolean.TRUE);
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Randomly color nodes, edges and labels");
        action.putValue(DEPENDS_ON_NODE_OR_EDGE, Boolean.TRUE);
        all.add(action);
        return randomColorActionAction = action;
    }

    private AbstractAction noColorActionAction;

    public AbstractAction getNoColorActionAction() {
        AbstractAction action = noColorActionAction;
        if (action != null) return action;

        action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                boolean changed = false;
                boolean fore = ((JCheckBox) foregroundColorAction.getValue(CHECKBOXITEM)).isSelected();
                boolean back = ((JCheckBox) backgroundColorAction.getValue(CHECKBOXITEM)).isSelected();
                boolean labelfore = ((JCheckBox) labelForegroundColorAction.getValue(CHECKBOXITEM)).isSelected();
                boolean labelback = ((JCheckBox) labelBackgroundColorAction.getValue(CHECKBOXITEM)).isSelected();

                if (viewer.hasSelectedNodes()) {
                    if (fore)
                        viewer.setColorSelectedNodes(null);
                    if (back)
                        viewer.setBackgroundColorSelectedNodes(null);
                    if (labelfore)
                        viewer.setLabelColorSelectedNodes(null);
                    if (labelback)
                        viewer.setLabelBackgroundColorSelectedNodes(null);
                    changed = true;
                    formatter.fireNodeFormatChanged(viewer.getSelectedNodes());
                    viewer.selectAllNodes(false);
                }
                if (viewer.hasSelectedEdges()) {
                    if (fore)
                        viewer.setColorSelectedEdges(null);
                    if (labelfore)
                        viewer.setLabelColorSelectedEdges(null);
                    if (labelback)
                        viewer.setLabelBackgroundColorSelectedEdges(null);
                    changed = true;
                    formatter.fireEdgeFormatChanged(viewer.getSelectedEdges());
                    viewer.selectAllEdges(false);
                }

                if (changed) {
                    dir.setDirty(true);
                    viewer.repaint();
                }
            }
        };
        action.putValue(AbstractAction.NAME, "Invisible");
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Set color to invisible");
        action.putValue(CRITICAL, Boolean.TRUE);
        action.putValue(DEPENDS_ON_NODE_OR_EDGE, Boolean.TRUE);
        all.add(action);
        return noColorActionAction = action;
    }


    private AbstractAction applyColorAction;

    public AbstractAction getApplyColorAction() {
        AbstractAction action = applyColorAction;
        if (action != null) return action;

        action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                Color color = formatter.getColor();
                boolean changed = false;
                boolean fore = ((JCheckBox) foregroundColorAction.getValue(CHECKBOXITEM)).isSelected();
                boolean back = ((JCheckBox) backgroundColorAction.getValue(CHECKBOXITEM)).isSelected();
                boolean labelfore = ((JCheckBox) labelForegroundColorAction.getValue(CHECKBOXITEM)).isSelected();
                boolean labelback = ((JCheckBox) labelBackgroundColorAction.getValue(CHECKBOXITEM)).isSelected();

                if (viewer.hasSelectedNodes()) {
                    if (fore)
                        viewer.setColorSelectedNodes(color);
                    if (back)
                        viewer.setBackgroundColorSelectedNodes(color);
                    if (labelfore)
                        viewer.setLabelColorSelectedNodes(color);
                    if (labelback)
                        viewer.setLabelBackgroundColorSelectedNodes(color);
                    changed = true;
                    formatter.fireNodeFormatChanged(viewer.getSelectedNodes());
                }
                if (viewer.hasSelectedEdges()) {
                    if (fore)
                        viewer.setColorSelectedEdges(color);
                    if (labelfore)
                        viewer.setLabelColorSelectedEdges(color);
                    if (labelback)
                        viewer.setLabelBackgroundColorSelectedEdges(color);
                    changed = true;
                    formatter.fireEdgeFormatChanged(viewer.getSelectedEdges());
                }

                if (changed) {
                    dir.setDirty(true);
                    viewer.repaint();
                }

            }
        };
        action.putValue(AbstractAction.NAME, "Again");
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Apply the last chosen color again");
        action.putValue(CRITICAL, Boolean.TRUE);
        action.putValue(DEPENDS_ON_NODE_OR_EDGE, Boolean.TRUE);
        all.add(action);
        return applyColorAction = action;
    }
}
