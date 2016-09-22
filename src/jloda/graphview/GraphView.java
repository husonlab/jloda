/**
 * GraphView.java 
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
/**
 * @version $Id: GraphView.java,v 1.189 2010-06-08 08:55:32 huson Exp $
 *
 * Graph view class.
 *
 * @author Daniel Huson
 */
package jloda.graphview;

import jloda.export.ExportManager;
import jloda.graph.*;
import jloda.util.*;
import jloda.util.parse.NexusStreamParser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.*;
import java.util.List;

/**
 * This class implements the visualization of a graph as a Canvas.
 */
public class GraphView extends JPanel implements Printable, Scrollable, INodeEdgeFormatable {

    private final NodeArray<NodeView> nodeViews;
    private final EdgeArray<EdgeView> edgeViews;

    private final java.util.List<NodeActionListener> nodeActionListeners = new LinkedList<>();
    private final java.util.List<EdgeActionListener> edgeActionListeners = new LinkedList<>();
    private final java.util.List<PanelActionListener> panelActionListeners = new LinkedList<>();

    private IGraphViewListener graphViewListener = null;
    private IPopupListener popupListener = null;

    public final static NodeView defaultNodeView = new NodeView(); // must be static for SplitsTree!
    public final static EdgeView defaultEdgeView = new EdgeView(); // must be static for SplitsTree!

    public final NodeSet selectedNodes;
    public final EdgeSet selectedEdges;

    private boolean allowEdit = true;
    private boolean allowNewNodeDoubleClick = true;
    private boolean maintainEdgeLengths = false; // in inaction, maintain lengths
    private boolean allowMoveNodes = true;
    private boolean allowMoveInternalEdgePoints = true;

    private boolean allowRubberbandNodes = true; // allow rubberbanding of nodes?
    private boolean allowRubberbandEdges = true;

    private boolean allowInternalEdgePoints = false; // allow user to interactively insert and move internal edge points

    private boolean allowEditNodeLabelsOnDoubleClick = false;
    private boolean allowEditEdgeLabelsOnDoubleClick = false;

    private boolean allowRotation = true;
    private boolean allowRotationArbitraryAngle = true;

    private final JScrollPane scrollPane;

    private boolean repaintOnGraphHasChanged = true;

    private Node foundNode;

    // do away with this:
    protected Color canvasColor = Color.lightGray;

    private boolean drawScaleBar = false;
    static protected final Font scaleBarFont = Font.decode("Helvetica-ITALIC-10");

    private boolean autoLayoutLabels = false; // attempt to do smart layout of labels?

    private final Graph G;

    private Font font = Font.decode("Helvetica-PLAIN-11");
    protected final Font poweredByFont = Font.decode("Helvetica-ITALIC-9");

    public final Transform trans;
    private String POWEREDBY = null;

    public boolean inPrint = false; // are we printing?

    final static public double XMIN_SCALE = 0.00000001;
    final static public double YMIN_SCALE = 0.00000001;
    final static public double XMAX_SCALE = 100000000;
    final static public double YMAX_SCALE = 100000000;

    private IGraphDrawer graphDrawer;

    protected JFrame frame;

    protected boolean locked = false; // are critical user actions currently locked?

    /**
     * Construct a GraphView for a graph G.
     *
     * @param G graph
     */
    public GraphView(Graph G) {
        this(G, 400, 400, Color.white);
    }

    /**
     * Construct a GraphView for a graph G.
     *
     * @param G graph
     * @param w width of canvas
     * @param h height of canvas
     */
    public GraphView(Graph G, int w, int h) {
        this(G, w, h, Color.white);
    }

    /**
     * Construct a GraphView for a graph G.
     *
     * @param G   graph
     * @param w   width of canvas
     * @param h   height of canvas
     * @param col color of canvas
     */
    public GraphView(Graph G, int w, int h, Color col) {
        super();
        this.G = G;

        nodeViews = new NodeArray<>(G);
        edgeViews = new EdgeArray<>(G);

        defaultNodeView.setFont(ProgramProperties.get(ProgramProperties.DEFAULT_FONT, defaultNodeView.getFont()));
        defaultEdgeView.setFont(ProgramProperties.get(ProgramProperties.DEFAULT_FONT, defaultEdgeView.getFont()));

        selectedNodes = new NodeSet(G);
        selectedEdges = new EdgeSet(G);

        setGraphArrays();

        setBackground(col);

        setSize(w, h);
        setPreferredSize(new Dimension(w, h));
        scrollPane = new JScrollPane(this);
        scrollPane.setWheelScrollingEnabled(true);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        scrollPane.setAutoscrolls(true);

        JButton zoomButton = new JButton();
        zoomButton.setAction(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                fitGraphToWindow();
            }
        });
        scrollPane.setCorner(JScrollPane.LOWER_RIGHT_CORNER, zoomButton);

        trans = new Transform(this);
        trans.addChangeListener(new ITransformChangeListener() {
            public void hasChanged(Transform trans) {
                recomputeMargins();
            }
        });

        getScrollPane().addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent event) {
                    //centerGraph();
            }
        });

        G.addGraphUpdateListener(new GraphUpdateAdapter() {
            public void graphHasChanged() {
                if (isRepaintOnGraphHasChanged())
                    GraphView.this.repaint();
            }

            public void deleteNode(Node v) {
                if (selectedNodes.contains(v))
                    selectedNodes.remove(v);
            }

            public void deleteEdge(Edge e) {
                if (selectedEdges.contains(e))
                    selectedEdges.remove(e);
            }
        });

        // set the default graph dendroscope
        this.graphDrawer = new DefaultGraphDrawer(this);

        // process mouse, key and component events
        setGraphViewListener(new GraphViewListener(this));

        resetCursor();

        /* NodeViews and EdgeViews are set when they are first referenced */
    }

    public Font getScaleBarFont() {
        return scaleBarFont;
    }

    /**
     * set the graphViewListener. Remove the old one
     *
     * @param graphViewListener
     */
    public void setGraphViewListener(IGraphViewListener graphViewListener) {
        if (this.graphViewListener != null) {
            removeMouseListener(this.graphViewListener);
            removeMouseMotionListener(this.graphViewListener);
            removeKeyListener(this.graphViewListener);
            removeComponentListener(this.graphViewListener);
            removeMouseWheelListener(this.graphViewListener);
        }
        this.graphViewListener = graphViewListener;
        addMouseListener(graphViewListener);
        addMouseMotionListener(graphViewListener);
        addKeyListener(graphViewListener);
        addComponentListener(graphViewListener);
        addMouseWheelListener(graphViewListener);
    }

    /**
     * gets the current graph view listener
     *
     * @return graph view listener
     */
    public IGraphViewListener getGraphViewListener() {
        return graphViewListener;
    }

    /**
     * Sets graph-related arrays
     */
    public void setGraphArrays() {
    }

    /**
     * Set the status of interactive editing of graph.
     *
     * @param ok allow editing
     */
    public void setAllowEdit(boolean ok) {
        allowEdit = ok;
    }

    /**
     * Get status of interactive editing of graph.
     *
     * @return editing allowed
     */
    public boolean getAllowEdit() {
        return allowEdit;
    }

    /**
     * allowed to interactive move nodes?
     *
     * @return move nodes
     */
    public boolean getAllowMoveNodes() {
        return allowMoveNodes;
    }

    /**
     * allow or disallow interactive moving of nodes
     *
     * @param allowMoveNodes
     */
    public void setAllowMoveNodes(boolean allowMoveNodes) {
        this.allowMoveNodes = allowMoveNodes;
    }

    /**
     * allow to move internal edge points
     *
     * @return true, if allowed
     */
    public boolean isAllowMoveInternalEdgePoints() {
        return allowMoveInternalEdgePoints;
    }

    /**
     * allow to move internal edge points
     *
     * @param allowMoveInternalEdgePoints
     */
    public void setAllowMoveInternalEdgePoints(boolean allowMoveInternalEdgePoints) {
        this.allowMoveInternalEdgePoints = allowMoveInternalEdgePoints;
    }

    /**
     * Set the status of interactive creation of new nodes
     *
     * @param ok allow double click creation of new node
     */
    public void setAllowNewNodeDoubleClick(boolean ok) {
        allowNewNodeDoubleClick = ok;
    }

    /**
     * Gets the  status of interactive creation of new nodes
     *
     * @return allow double click creation of new node
     */
    public boolean getAllowNewNodeDoubleClick() {
        return allowNewNodeDoubleClick;
    }


    /**
     * Set the status of maintaining edge lengths in interaction.
     *
     * @param ok maintain edge lengths
     */
    public void setMaintainEdgeLengths(boolean ok) {
        maintainEdgeLengths = ok;
    }

    /**
     * Get status of maintaining edge lengths in interaction.
     *
     * @return editing allowed
     */
    public boolean getMaintainEdgeLengths() {
        return maintainEdgeLengths;
    }

    /**
     * is a scale bar being displayed?
     *
     * @return display scalebar
     */
    public boolean isDrawScaleBar() {
        return drawScaleBar;
    }

    /**
     * determine whether to display a scale bar
     *
     * @param drawScaleBar
     */
    public void setDrawScaleBar(boolean drawScaleBar) {
        this.drawScaleBar = drawScaleBar;
    }

    /**
     * lock aspect ratio
     *
     * @return true, if aspect ratio is locked
     */
    public boolean isKeepAspectRatio() {
        return trans.getLockXYScale();
    }

    /**
     * set lock aspect ratio
     *
     * @param keepAspectRatio
     */
    public void setKeepAspectRatio(boolean keepAspectRatio) {
        trans.setLockXYScale(keepAspectRatio);
    }

    /**
     * is rotation by arbitrary angle allowed?
     *
     * @return true, if rotation by arbitary angle is allowed
     */
    public boolean isAllowRotationArbitraryAngle() {
        return allowRotationArbitraryAngle;
    }

    /**
     * is rotation by arbitary angle allowed
     *
     * @param allowRotationArbitraryAngle
     */
    public void setAllowRotationArbitraryAngle(boolean allowRotationArbitraryAngle) {
        this.allowRotationArbitraryAngle = allowRotationArbitraryAngle;
    }

    public boolean isAllowRotation() {
        return allowRotation;
    }

    public void setAllowRotation(boolean allowRotation) {
        this.allowRotation = allowRotation;
    }

    /**
     * Set the default node label.
     *
     * @param a the default value
     */
    public void setDefaultNodeLabel(String a) {
        defaultNodeView.setLabel(a);
    }

    /**
     * sets the default node font
     *
     * @param font
     */
    public void setDefaultNodeFont(Font font) {
        defaultNodeView.setFont(font);
    }

    /**
     * Set the default node location in world coordinates.
     *
     * @param p the default value
     */
    public void setDefaultNodeLocation(Point2D p) {
        defaultNodeView.setLocation(p);
    }

    /**
     * Set the default node location in world coordinates.
     *
     * @param x the default x coordinate
     * @param y the default y coordinate
     */
    public void setDefaultNodeLocation(double x, double y) {
        defaultNodeView.setLocation(new Point2D.Double(x, y));
    }

    /**
     * Set the default node color.
     *
     * @param a the default value
     */
    public void setDefaultNodeColor(Color a) {
        defaultNodeView.setColor(a);
    }

    /**
     * Set the default node background color.
     *
     * @param a the default value
     */
    public void setDefaultNodeBackgroundColor(Color a) {
        defaultNodeView.setBackgroundColor(a);
    }

    /**
     * Set the default node label color.
     *
     * @param a the default value
     */
    public void setDefaultNodeLabelColor(Color a) {
        defaultNodeView.setLabelColor(a);
    }

    /**
     * Set the default node width.
     *
     * @param a the default value
     */
    public void setDefaultNodeWidth(int a) {
        defaultNodeView.setWidth(a);
    }

    /**
     * get the default node width.
     *
     * @return the default value
     */
    public int getDefaultNodeWidth() {
        return defaultNodeView.getWidth();
    }

    /**
     * Set the default node height.
     *
     * @param a the default value
     */
    public void setDefaultNodeHeight(int a) {
        defaultNodeView.setHeight(a);
    }

    /**
     * Set the default node line width.
     *
     * @param a the default value
     */
    public void setDefaultNodeLineWidth(int a) {
        defaultNodeView.setLineWidth((byte) a);
    }

    /**
     * Set the default node shape.
     *
     * @param a the default value
     */
    public void setDefaultNodeShape(byte a) {
        defaultNodeView.setShape(a);
    }

    /**
     * get the default node shape
     *
     * @return node shape
     */
    public byte getDefaultNodeShape() {
        return defaultNodeView.getShape();
    }

    /**
     * Set the default edge label.
     *
     * @param a the default value
     */
    public void setDefaultEdgeLabel(String a) {
        defaultEdgeView.setLabel(a);
    }

    public void setDefaultEdgeFont(Font font) {
        defaultEdgeView.setFont(font);
    }

    /**
     * Set the default edge color.
     *
     * @param a the default value
     */
    public void setDefaultEdgeColor(Color a) {
        defaultEdgeView.setColor(a);
    }

    /**
     * Set the default edge label color.
     *
     * @param a the default value
     */
    public void setDefaultEdgeLabelColor(Color a) {
        defaultEdgeView.setLabelColor(a);
    }

    /**
     * Set the default edge line width.
     *
     * @param a the default value
     */
    public void setDefaultEdgeLineWidth(int a) {
        defaultEdgeView.setLineWidth((byte) a);
    }

    /**
     * Set the default edge shape.
     *
     * @param a the default value
     */
    public void setDefaultEdgeShape(byte a) {
        defaultEdgeView.setShape(a);
    }

    /**
     * get the default edge shape
     *
     * @return edge shape
     */
    public byte getDefaultEdgeShape() {
        return defaultEdgeView.getShape();
    }

    /**
     * Set the default edge direction.
     *
     * @param a the default value
     */
    public void setDefaultEdgeDirection(byte a) {
        defaultEdgeView.setDirection(a);
    }

    /**
     * Sets the default node label position.
     *
     * @param a the position (CENTAL_POS, NORTHWEST_POS,...)
     */
    public void setDefaultNodeLabelLayout(byte a) {
        defaultNodeView.setLabelLayout(a);
    }

    /**
     * Gets the node label.
     *
     * @param v the node
     * @return node label
     */
    public String getLabel(Node v) {
        return getNV(v).getLabel();
    }

    /**
     * Gets the selection state of a node.
     *
     * @param v the node
     * @return selection state
     */
    public boolean getSelected(Node v) {
        return selectedNodes.contains(v);
    }


    /**
     * Gets the node location.
     *
     * @param v the node
     * @return location
     */
    public Point2D getLocation(Node v) {
        return getNV(v).getLocation();
    }

    /**
     * Gets the node foreground color.
     *
     * @param v the node
     * @return foreground color
     */
    public Color getColor(Node v) {
        return getNV(v).getColor();
    }

    /**
     * Gets the node background color.
     *
     * @param v the node
     * @return background color
     */
    public Color getBackgroundColor(Node v) {
        return getNV(v).getBackgroundColor();
    }

    /**
     * Gets the node border color.
     *
     * @param v the node
     * @return background color
     */
    public Color getBorderColor(Node v) {
        return getNV(v).getBorderColor();
    }

    /**
     * Gets the node label color.
     *
     * @param v the node
     * @return label color
     */
    public Color getLabelColor(Node v) {
        return getNV(v).getLabelColor();
    }

    /**
     * Gets the node
     *
     * @param v the node
     * @return node width
     */
    public int getWidth(Node v) {
        return getNV(v).getWidth();
    }

    /**
     * Gets the node height.
     *
     * @param v the node
     * @return node height
     */
    public int getHeight(Node v) {
        return getNV(v).getHeight();
    }

    /**
     * Gets the node line width.
     *
     * @param v the node
     * @return line width
     */
    public int getLineWidth(Node v) {
        return getNV(v).getLineWidth();
    }

    /**
     * Gets the node bounding box in device coordinates
     *
     * @param v the node
     * @return device bounding box
     */
    public Rectangle getBox(Node v) {
        return getNV(v).getBox(trans);
    }

    /**
     * Gets the node label bounding box in device coordinates
     *
     * @param v the node
     * @return device bounding box
     */
    public Rectangle getLabelRect(Node v) {
        return getNV(v).getLabelRect(trans);
    }

    /**
     * Gets the edge label bounding box in device coordinates
     *
     * @param e the node
     * @return device bounding box
     */
    public Rectangle getLabelRect(Edge e) {
        return getEV(e).getLabelRect(trans);
    }

    /**
     * gets the list of internal points associated with an edge, or null
     *
     * @param e
     * @return list of internal points, or null
     * @
     */
    public java.util.List<Point2D> getInternalPoints(Edge e) {
        return getEV(e).getInternalPoints();
    }

    /**
     * sets the list of internal points associated with an edge, or null
     *
     * @param e
     * @param list
     * @
     */
    public void setInternalPoints(Edge e, java.util.List<Point2D> list) {
        getEV(e).setInternalPoints(list);
    }

    /**
     * Sets the label of a node.
     *
     * @param v the node
     * @param a the label
     */
    public void setLabel(Node v, String a) {
        getNV(v).setLabel(a);
    }

    /**
     * Sets the node label position for a node
     *
     * @param v the node
     * @param a a position (e.g., GraphView.CENTRAL_POS, GraphView.NORTH_POS...)
     */
    public void setLabelLayout(Node v, byte a) {
        getNV(v).setLabelLayout(a);
    }

    /**
     * gets the label layout mode of the node
     *
     * @param v
     * @return layout mode
     */
    public byte getLabelLayout(Node v) {
        return getNV(v).getLabelLayout();
    }

    /**
     * Sets the edge label position for a edge
     *
     * @param e the edge
     * @param a a position (e.g., GraphView.CENTRAL_POS, GraphView.NORTH_POS...)
     */
    public void setLabelLayout(Edge e, byte a) {
        getEV(e).setLabelLayout(a);
    }

    /**
     * gets the label layout mode of the edge
     *
     * @param e
     * @return layout mode
     */
    public byte getLabelLayout(Edge e) {
        return getEV(e).getLabelLayout();
    }

    /**
     * Is the label visible ?
     *
     * @param v
     * @return visibility of the label
     */
    public boolean isLabelVisible(Node v) {
        try {
            return getNV(v).isLabelVisible();
        } catch (NotOwnerException ex) {
            Basic.caught(ex);
            return false;
        }
    }

    /**
     * Is the label visible ?
     *
     * @param e the concerned edge
     * @return visibility of the label
     */
    public boolean isLabelVisible(Edge e) {
        try {
            return getEV(e).isLabelVisible();
        } catch (NotOwnerException ex) {
            Basic.caught(ex);
            return false;
        }
    }

    /**
     * Set label visibility
     *
     * @param e            the  edge
     * @param labelVisible visibility of the label
     */
    public void setLabelVisible(Edge e, boolean labelVisible) {
        try {
            getEV(e).setLabelVisible(labelVisible);
        } catch (NotOwnerException ex) {
            Basic.caught(ex);
        }
    }

    /**
     * is label visible?
     *
     * @return true, if  visible
     */

    public boolean getLabelVisible(Edge e) {
        return getEV(e).getLabelVisible();
    }

    /**
     * Set label visibility
     *
     * @param v            the node
     * @param labelVisible visibility of the label
     */
    public void setLabelVisible(Node v, boolean labelVisible) {
        getNV(v).setLabelVisible(labelVisible);
    }

    /**
     * is label visible?
     *
     * @return true, if  visible
     */
    public boolean getLabelVisible(Node v) {
        return getNV(v).getLabelVisible();
    }


    /**
     * Set the node label position for all nodes
     *
     * @param a a position (e.g., GraphView.CENTRAL_POS, GraphView.NORTH_POS...)
     */
    public void setLabelLayoutAllNodes(byte a) {
        try {
            Graph graph = this.getGraph();
            for (Node v = graph.getFirstNode(); v != null; v = graph.getNextNode(v)) {
                setLabelLayout(v, a);
            }
        } catch (Exception ex) {
            Basic.caught(ex);
        }
    }

    /**
     * Sets the selection state.
     *
     * @param v      the node
     * @param select the selection state
     */
    public void setSelected(Node v, boolean select) {
        if (select) {
            if (!selectedNodes.contains(v)) {
                selectedNodes.add(v);
                NodeSet aset = new NodeSet(G);
                aset.add(v);
                fireDoSelect(aset);
            }
        } else {
            if (selectedNodes.contains(v)) {
                NodeSet aset = new NodeSet(G);
                aset.add(v);
                selectedNodes.remove(v);
                fireDoDeselect(aset);
            }
        }
    }

    /**
     * Sets the selection state.
     *
     * @param nodes the nodes
     * @param a     the selection state
     */
    public void setSelected(NodeSet nodes, boolean a) {
        if (a) {
            if (!selectedNodes.containsAll(nodes)) {
                selectedNodes.addAll(nodes);
                fireDoSelect(nodes);
            }
        } else {
            if (selectedNodes.intersects(nodes)) {
                selectedNodes.removeAll(nodes);
                fireDoDeselect(nodes);
            }
        }
    }

    /**
     * Sets the location.
     *
     * @param v the node
     * @param p the location
     */
    public void setLocation(Node v, Point2D p) {
        getNV(v).setLocation(p);
    }

    /**
     * Sets the location.
     *
     * @param v the node
     * @param x the x-coordinate of the location
     * @param y the y-coordinate of the location
     */
    public void setLocation(Node v, double x, double y) {
        getNV(v).setLocation(new Point2D.Double(x, y));
    }

    /**
     * set the relative label location for a node
     *
     * @param v  the node
     * @param pt the offset in device coordinates
     */
    public void setLabelPositionRelative(Node v, Point pt) {
        getNV(v).setLabelPositionRelative(pt.x, pt.y);
    }

    /**
     * set the relative label location for an edge
     *
     * @param e        edge
     * @param location in device coordinates
     */
    public void setLabelPositionRelative(Edge e, Point location) {
        getEV(e).setLabelPositionRelative(location);
    }

    /**
     * Sets the foreground color.
     *
     * @param v the node
     * @param a the color
     */
    public void setColor(Node v, Color a) {
        getNV(v).setColor(a);
    }

    /**
     * Sets the background color.
     *
     * @param v the node
     * @param a the color
     */
    public void setBackgroundColor(Node v, Color a) {
        getNV(v).setBackgroundColor(a);
    }

    /**
     * Sets the border color.
     *
     * @param v the node
     * @param a the color
     */
    public void setBorderColor(Node v, Color a) throws
            NotOwnerException {
        getNV(v).setBorderColor(a);
    }

    /**
     * Sets the label color.
     *
     * @param v the node
     * @param a the color
     */
    public void setLabelColor(Node v, Color a) {
        if (getNV(v).getLabelVisible())
            getNV(v).setLabelColor(a);
    }

    /**
     * Sets the label background color.
     *
     * @param v the node
     * @param a the color
     */
    public void setLabelBackgroundColor(Node v, Color a) {
        if (getNV(v).getLabelVisible())
            getNV(v).setLabelBackgroundColor(a);
    }

    /**
     * gets the labels background color
     *
     * @param v
     * @return color
     */
    public Color getLabelBackgroundColor(Node v) {
        return getNV(v).getLabelBackgroundColor();
    }

    /**
     * Sets the width.
     *
     * @param v the node
     * @param a the width
     */
    public void setWidth(Node v, int a) {
        getNV(v).setWidth(a);
    }

    /**
     * Sets the height.
     *
     * @param v the node
     * @param a the height
     */
    public void setHeight(Node v, int a) {
        getNV(v).setHeight(a);
    }

    /**
     * Sets the line width.
     *
     * @param v the node
     * @param a the line width
     */
    public void setLineWidth(Node v, int a) {
        getNV(v).setLineWidth((byte) a);
    }

    /**
     * Sets the line width for all selected nodes and edges
     *
     * @param a the line width
     */
    public void setLineWidthSelected(int a) {
        setLineWidthSelectedNodes((byte) a);
        setLineWidthSelectedEdges((byte) a);
    }

    /**
     * Sets the line width for all selected nodes
     *
     * @param a the line width
     */
    public void setLineWidthSelectedNodes(byte a) {
        try {
            for (Node v = selectedNodes.getFirstElement(); v != null;
                 v = selectedNodes.getNextElement(v)) {
                setLineWidth(v, a);
            }
        } catch (NotOwnerException ex) {
            Basic.caught(ex);
        }
    }

    /**
     * Sets the line width for all selected edges
     *
     * @param a the line width
     */
    public void setLineWidthSelectedEdges(byte a) {
        try {
            for (Edge e = selectedEdges.getFirstElement(); e != null;
                 e = selectedEdges.getNextElement(e)) {
                setLineWidth(e, a);
            }
        } catch (NotOwnerException ex) {
            Basic.caught(ex);
        }
    }


    /**
     * Sets the shape.
     *
     * @param v the node
     * @param a the shape
     */
    public void setShape(Node v, byte a) {
        getNV(v).setShape(a);
    }

    /**
     * Gets the shape.
     *
     * @param v the node
     * @return the shape
     */

    public byte getShape(Node v) {
        return getNV(v).getShape();
    }

    /**
     * Gets the edge label.
     *
     * @param e the edge
     * @return the label
     */
    public String getLabel(Edge e) {
        return getEV(e).getLabel();
    }

    /**
     * Gets the selection state.
     *
     * @param e the edge
     * @return the selection state
     */
    public boolean getSelected(Edge e) {
        return selectedEdges.contains(e);
    }


    /**
     * Gets the edge foreground color.
     *
     * @param e the edge
     * @return the color
     */
    public Color getColor(Edge e) {
        return getEV(e).getColor();
    }

    /**
     * Gets the edge label color.
     *
     * @param e the edge
     * @return the color
     */
    public Color getLabelColor(Edge e) {
        return getEV(e).getLabelColor();
    }

    /**
     * Sets the label background color.
     *
     * @param e the edge
     * @param a the color
     */
    public void setLabelBackgroundColor(Edge e, Color a) {
        if (getEV(e).isLabelVisible())
            getEV(e).setLabelBackgroundColor(a);
    }

    /**
     * gets the labels background color
     *
     * @param e
     * @return color
     */
    public Color getLabelBackgroundColor(Edge e) {
        return getEV(e).getLabelBackgroundColor();
    }


    /**
     * Gets the edge line width.
     *
     * @param e the edge
     * @return the line width
     */
    public int getLineWidth(Edge e) {
        return getEV(e).getLineWidth();
    }

    /**
     * Gets the edges direction.
     *
     * @param e the edge
     * @return the direction
     */
    public int getDirection(Edge e) {
        return getEV(e).getDirection();
    }

    /**
     * Sets the label.
     *
     * @param e the edge
     * @param a the label
     */
    public void setLabel(Edge e, String a) {
        getEV(e).setLabel(a);
    }

    /**
     * Sets the selection state of an edge.
     *
     * @param e the edge
     * @param a the selection state
     */
    public void setSelected(Edge e, boolean a) {
        if (a) {
            if (!selectedEdges.contains(e)) {
                selectedEdges.add(e);
                EdgeSet aset = new EdgeSet(G);
                aset.add(e);
                fireDoSelect(aset);
            }
        } else {
            if (selectedEdges.contains(e)) {
                EdgeSet aset = new EdgeSet(G);
                aset.add(e);
                fireDoDeselect(aset);
                selectedEdges.remove(e);
            }
        }
    }

    /**
     * Sets the selection state.
     *
     * @param edges the edges
     * @param a     the selection state
     */
    public void setSelected(EdgeSet edges, boolean a) {
        if (a) {
            if (!selectedEdges.containsAll(edges)) {
                selectedEdges.addAll(edges);
                fireDoSelect(edges);
            }
        } else {
            if (selectedEdges.intersects(edges)) {
                selectedEdges.removeAll(edges);
                fireDoDeselect(edges);
            }
        }
    }


    /**
     * Sets the foreground color.
     *
     * @param e the edge
     * @param a the color
     */
    public void setColor(Edge e, Color a) {
        getEV(e).setColor(a);
    }

    /**
     * set the color of selected nodes and edges
     *
     * @param a    color
     * @param kind "line", "fill" or "label"
     */
    public boolean setColorSelected(Color a, String kind) {
        boolean changed = false;
        switch (kind) {
            case "line":
                if (setColorSelectedNodes(a))
                    changed = true;
                if (setColorSelectedEdges(a)) changed = true;
                break;
            case "fill":
                if (setBackgroundColorSelectedNodes(a)) changed = true;
                break;
            case "label":
                if (setLabelColorSelectedNodes(a)) changed = true;
                if (setLabelColorSelectedEdges(a)) changed = true;
                break;
        }
        return changed;
    }

    /**
     * Sets the color for all selected nodes and edges
     *
     * @param a the color
     */
    public boolean setColorSelectedEdges(Color a) {
        boolean changed = false;
        for (Edge e = selectedEdges.getFirstElement(); e != null;
             e = selectedEdges.getNextElement(e)) {
            if (getColor(e) == null || !getColor(e).equals(a)) {
                changed = true;
                setColor(e, a);
            }
        }
        return changed;
    }

    /**
     * Sets the color for all selected nodes and edges
     *
     * @param a the color
     */
    public boolean setLabelColorSelectedEdges(Color a) {
        boolean changed = false;
        for (Edge e = selectedEdges.getFirstElement(); e != null;
             e = selectedEdges.getNextElement(e)) {
            if (getLabelVisible(e) && (getLabelColor(e) == null || !getLabelColor(e).equals(a))) {
                setLabelColor(e, a);
                changed = true;
            }
        }
        return changed;
    }


    /**
     * Sets the color for all selected nodes
     *
     * @param a the color
     */
    public boolean setColorSelectedNodes(Color a) {
        boolean changed = false;
        for (Node v = selectedNodes.getFirstElement(); v != null;
             v = selectedNodes.getNextElement(v)) {
            if (getColor(v) == null || !getColor(v).equals(a)) {
                changed = true;
                setColor(v, a);
            }
        }
        return changed;
    }

    /**
     * Sets the label color for all selected nodes
     *
     * @param a the color
     */
    public boolean setLabelColorSelectedNodes(Color a) {
        boolean changed = false;
        for (Node v = selectedNodes.getFirstElement(); v != null;
             v = selectedNodes.getNextElement(v)) {
            if (getLabelVisible(v) && (getLabelColor(v) == null || !getLabelColor(v).equals(a))) {
                changed = true;
                setLabelColor(v, a);
            }
        }
        return changed;
    }

    /**
     * Sets the background color for all selected nodes
     *
     * @param a the color
     */
    public boolean setBackgroundColorSelectedNodes(Color a) {
        boolean changed = false;
        for (Node v = selectedNodes.getFirstElement(); v != null;
             v = selectedNodes.getNextElement(v)) {
            if (getBackgroundColor(v) == null || !getBackgroundColor(v).equals(a)) {
                changed = true;
                setBackgroundColor(v, a);
            }
        }
        return changed;
    }

    /**
     * Sets the label color.
     *
     * @param e the edge
     * @param a the color
     */
    public void setLabelColor(Edge e, Color a) {
        if (getEV(e).isLabelVisible())
            getEV(e).setLabelColor(a);
    }

    /**
     * Sets the line width.
     *
     * @param e the edge
     * @param a the line width
     */
    public void setLineWidth(Edge e, int a) {
        getEV(e).setLineWidth((byte) Math.min(a, Byte.MAX_VALUE));
    }

    /**
     * Sets the shape.
     *
     * @param e the edge
     * @param a the shape
     */
    public void setShape(Edge e, byte a) {
        getEV(e).setShape(a);
    }

    /**
     * gets the shape.
     *
     * @param e the edge
     * @return a the shape
     */
    public byte getShape(Edge e) {
        return getEV(e).getShape();
    }

    /**
     * Sets the edges direction.
     *
     * @param e the edge
     * @param a the direction
     */
    public void setDirection(Edge e, byte a) {
        getEV(e).setDirection(a);
    }

    public Font getFont(Node v) {
        if (getNV(v).getFont() != null)
            return getNV(v).getFont();
        else
            return getFont();
    }

    public void setFont(Node v, Font font) {
        getNV(v).setFont(font);
    }

    public Font getFont(Edge e) {
        if (getEV(e).getFont() != null)
            return getEV(e).getFont();
        else
            return getFont();
    }

    public void setFont(Edge e, Font font) {
        getEV(e).setFont(font);
    }


    /**
     * Sets the font for all selected nodes and edges
     *
     * @param font
     */
    public void setFontSelected(Font font) {
        setFontSelectedNodes(font);
        setFontSelectedEdges(font);
    }


    /**
     * Sets the font for all selected edges
     *
     * @param font
     */
    public void setFontSelectedEdges(Font font) {
        for (Edge e = selectedEdges.getFirstElement(); e != null;
             e = selectedEdges.getNextElement(e)) {
            setFont(e, font);
        }
    }


    /**
     * Sets the font for all selected edges. Only sets those parts that are not null or -1
     *
     * @param family
     * @param bold
     * @param italics
     * @param size
     * @return changed?
     */
    public boolean setFontSelectedEdges(String family, int bold, int italics, int size) {
        boolean changed = false;
        for (Edge e : getSelectedEdges()) {
            String familyE = getFont(e).getFamily();
            int styleE = getFont(e).getStyle();
            int sizeE = getFont(e).getSize();
            int style = 0;
            if (bold == 1 || (bold == -1 && (styleE == Font.BOLD || styleE == Font.BOLD + Font.ITALIC)))
                style += Font.BOLD;
            if (italics == 1 || (italics == -1 && (styleE == Font.ITALIC || styleE == Font.BOLD + Font.ITALIC)))
                style += Font.ITALIC;

            Font font = new Font(family != null ? family : familyE, style != -1 ? style : styleE, size != -1 ? size : sizeE);
            if (getFont(e) == null || !getFont(e).equals(font)) {
                changed = true;
                setFont(e, font);
            }
        }
        return changed;
    }


    /**
     * Sets the font for all selected nodes. Only sets those parts that are not null or -1
     *
     * @param family
     * @param bold
     * @param italics
     * @param size
     * @return changed?
     */
    public boolean setFontSelectedNodes(String family, int bold, int italics, int size) {
        boolean changed = false;
        for (Node v : getSelectedNodes()) {
            String familyE = getFont(v).getFamily();
            int styleE = getFont(v).getStyle();
            int sizeE = getFont(v).getSize();
            int style = 0;
            if (bold == 1 || (bold == -1 && (styleE == Font.BOLD || styleE == Font.BOLD + Font.ITALIC)))
                style += Font.BOLD;
            if (italics == 1 || (italics == -1 && (styleE == Font.ITALIC || styleE == Font.BOLD + Font.ITALIC)))
                style += Font.ITALIC;

            Font font = new Font(family != null ? family : familyE, style != -1 ? style : styleE, size != -1 ? size : sizeE);
            if (getFont(v) == null || !getFont(v).equals(font)) {
                changed = true;
                setFont(v, font);
            }
        }
        return changed;
    }


    /**
     * Sets the font for all selected nodes
     *
     * @param font
     */
    public void setFontSelectedNodes(Font font) {
        try {
            for (Node v = selectedNodes.getFirstElement(); v != null;
                 v = selectedNodes.getNextElement(v)) {
                setFont(v, font);
            }
        } catch (NotOwnerException ex) {
            Basic.caught(ex);
        }
    }

    /**
     * sets the size of all selected nodes
     *
     * @param width
     * @param height
     */
    public void setSizeSelectedNodes(byte width, byte height) {
        try {
            for (Node v = selectedNodes.getFirstElement(); v != null;
                 v = selectedNodes.getNextElement(v)) {
                setWidth(v, width);
                setHeight(v, height);
            }
        } catch (NotOwnerException ex) {
            Basic.caught(ex);
        }
    }


    /**
     * Returns the NodeView corresponding to v.
     *
     * @param v the node
     * @return the NodeView
     */
    public NodeView getNV(Node v) {
        if (nodeViews.get(v) == null) {
            NodeView nv = new NodeView(defaultNodeView);
            nodeViews.set(v, nv);
            if (nv.getLocation() == null)
                nv.setLocation(trans.getRandomVisibleLocation());
        }
        return nodeViews.get(v);
    }

    /**
     * Returns the EdgeView corresponding to e.
     *
     * @param e the edge
     * @return the EdgeView
     */
    public EdgeView getEV(Edge e) {
        if (edgeViews.get(e) == null) {
            EdgeView ev = new EdgeView(defaultEdgeView);
            edgeViews.set(e, ev);
        }
        return edgeViews.get(e);
    }

    /**
     * Returns the graph.
     *
     * @return the graph
     */
    public Graph getGraph() {
        return G;
    }


    /**
     * Select all nodes.
     *
     * @param select value to set selection states to
     * @return true, if selection state of at least one node changed
     */
    public boolean selectAllNodes(boolean select) {
        if (!select) {
            if (selectedNodes.size() > 0) {
                NodeSet oldSelected = (NodeSet) selectedNodes.clone();
                selectedNodes.clear();
                fireDoDeselect(oldSelected);
                return true;
            }
        } else {
            if (selectedNodes.size() < getGraph().getNumberOfNodes()) {
                selectedNodes.addAll();
                fireDoSelect(selectedNodes);
                return true;
            }
        }
        return false;
    }

    /**
     * Select all inner nodes.
     *
     * @param select value to set selection states to
     * @return true, if selection state of at least one node changed
     */
    public boolean selectAllInnerNodes(boolean select) {
        NodeSet changed = new NodeSet(getGraph());

        for (Node v = getGraph().getFirstNode(); v != null; v = v.getNext()) {
            if (v.getDegree() > 1 && getSelected(v) != select) {
                if (select)
                    selectedNodes.add(v);
                else
                    selectedNodes.remove(v);
                changed.add(v);
            }
        }
        if (select)
            fireDoSelect(changed);
        else
            fireDoSelect(changed);
        return changed.size() > 0;
    }

    /**
     * Select all leaf nodes.
     *
     * @param select value to set selection states to
     * @return true, if selection state of at least one node changed
     */
    public boolean selectAllLeafNodes(boolean select) {
        NodeSet changed = new NodeSet(getGraph());

        for (Node v = getGraph().getFirstNode(); v != null; v = v.getNext()) {
            if (v.getDegree() == 1 && getSelected(v) != select) {
                if (select)
                    selectedNodes.add(v);
                else
                    selectedNodes.remove(v);
                changed.add(v);
            }
        }
        if (select)
            fireDoSelect(changed);
        else
            fireDoSelect(changed);
        return changed.size() > 0;
    }

    /**
     * Select all edges.
     *
     * @param select value to set selection states to
     * @return true, if selection state of at least one edge changed
     */
    public boolean selectAllEdges(boolean select) {
        if (!select) {
            if (selectedEdges.size() > 0) {
                EdgeSet oldSelected = (EdgeSet) selectedEdges.clone();
                selectedEdges.clear();
                fireDoDeselect(oldSelected);
                return true;
            }
        } else {
            if (selectedEdges.size() < getGraph().getNumberOfEdges()) {
                selectedEdges.addAll();
                fireDoSelect(selectedEdges);
                return true;
            }
        }
        return false;
    }


    /**
     * inverts the selection state of all nodes and edges
     */
    public void invertSelection() {
        invertNodeSelection();
        invertEdgeSelection();
    }

    /**
     * inverts the selection state of all nodes
     */
    public void invertNodeSelection() {
        NodeSet oldSelected = (NodeSet) selectedNodes.clone();
        for (Node a = G.getFirstNode(); a != null; a = G.getNextNode(a)) {
            if (selectedNodes.contains(a))
                selectedNodes.remove(a);

            else
                selectedNodes.add(a);
        }
        fireDoDeselect(oldSelected);
        fireDoSelect(selectedNodes);
    }

    /**
     * inverts the selection state of all edges
     */
    public void invertEdgeSelection() {
        EdgeSet oldSelected = (EdgeSet) selectedEdges.clone();
        for (Edge a = G.getFirstEdge(); a != null; a = G.getNextEdge(a)) {
            if (selectedEdges.contains(a))
                selectedEdges.remove(a);

            else
                selectedEdges.add(a);
        }
        fireDoDeselect(oldSelected);
        fireDoSelect(selectedEdges);
    }

    /**
     * delete all selected nodes.
     */
    public void delSelectedNodes() {
        Graph G = getGraph();
        // synchronized (G)
        {

            for (Node v : selectedNodes) {
                fireDoDelete(v);
                G.deleteNode(v);
            }
        }
    }

    /**
     * delete all selected edges.
     */
    public void delSelectedEdges() {
        Graph G = getGraph();
        for (Edge e : selectedEdges) {
            fireDoDelete(e);
            G.deleteEdge(e);
        }
    }

    /**
     * Creates a new node and informs the listeners of it. These in turn may
     * cancel the node creation.
     *
     * @return the new node or null if creation was cancelled.
     */
    public Node newNode() {
        Node v = G.newNode();
        fireDoNew(v);
        if (v.getOwner() != null) {
            return v;
        } else {
            return null;
        }
    }

    /**
     * Creates a new edge and informs the listeners of it. These in turn may
     * cancel the edge creation.
     *
     * @param v the source vertex.
     * @param w the target vertex.
     * @return the new edge or null if creation was cancelled..
     * @ if the source or target is not ours.
     */
    public Edge newEdge(Node v, Node w) {
        if (w == null) {
            w = G.newNode();
            fireDoNew(v, w);
        }
        if (w != null && w.getOwner() != null) {
            Edge e = G.newEdge(v, w);
            if (e != null) {
                fireDoNew(e);
                if (e.getOwner() != null) {
                    return e;
                }
            }
        }
        return null;
    }

    /**
     * Horizonally flips all selected nodes.
     */
    public void horizontalFlipSelected() {
        Graph G = getGraph();
        double minx = 10000000;
        double maxx = -10000000;

        try {
            for (Node v = G.getFirstNode(); v != null; v = G.getNextNode(v)) {
                if (getSelected(v)) {
                    Point2D p = getLocation(v);
                    if (p.getX() < minx)
                        minx = p.getX();
                    if (p.getX() > maxx)
                        maxx = p.getX();
                }
            }
            double pivot = (maxx + minx);
            for (Node v = G.getFirstNode(); v != null; v = G.getNextNode(v)) {
                if (getSelected(v)) {
                    Point2D p = getLocation(v);
                    p.setLocation(pivot - p.getX(), p.getY());
                }
            }
        } catch (NotOwnerException ex) {
            Basic.caught(ex);
        }
    }

    /**
     * Vertically flips all selected nodes.
     */
    public void verticalFlipSelected() {
        Graph G = getGraph();
        double miny = 10000000;
        double maxy = -10000000;

        try {
            for (Node v = G.getFirstNode(); v != null; v = G.getNextNode(v)) {
                if (getSelected(v)) {
                    Point2D p = getLocation(v);
                    if (p.getY() < miny)
                        miny = p.getY();
                    if (p.getY() > maxy)
                        maxy = p.getY();
                }
            }
            double pivot = (maxy + miny);
            for (Node v = G.getFirstNode(); v != null; v = G.getNextNode(v)) {
                if (getSelected(v)) {
                    Point2D p = getLocation(v);
                    p.setLocation(p.getX(), pivot - p.getY());
                }
            }
        } catch (NotOwnerException ex) {
            Basic.caught(ex);
        }
    }

    /**
     * Computes a spring embedding of the graph
     *
     * @param iterations       the number of iterations used
     * @param startFromCurrent use current node positions
     */
    public void computeSpringEmbedding(int iterations, boolean startFromCurrent) {
        try {
            final Graph G = getGraph();

            final double width = getSize().width;
            final double height = getSize().height;

            if (G.getNumberOfNodes() < 2)
                return;

            // Initial positions are on a circle:
            final NodeDoubleArray xPos = new NodeDoubleArray(G);
            final NodeDoubleArray yPos = new NodeDoubleArray(G);

            int i = 0;
            for (Node v = G.getFirstNode(); v != null; v = G.getNextNode(v)) {
                if (startFromCurrent) {
                    Point2D p = getLocation(v);
                    xPos.set(v, p.getX());
                    yPos.set(v, p.getY());
                } else {
                    xPos.set(v, 1000 * Math.sin(6.24 * i / G.getNumberOfNodes()));
                    yPos.set(v, 1000 * Math.cos(6.24 * i / G.getNumberOfNodes()));
                    i++;
                }
            }

            // run iterations of spring embedding:
            double log2 = Math.log(2);
            for (int count = 1; count < iterations; count++) {
                final double k = Math.sqrt(width * height / G.getNumberOfNodes()) / 2;
                final double l2 = 25 * log2 * Math.log(1 + count);
                final double tx = width / l2;
                final double ty = height / l2;

                final NodeDoubleArray xDispl = new NodeDoubleArray(G);
                final NodeDoubleArray yDispl = new NodeDoubleArray(G);

                // repulsive forces

                for (Node v = G.getFirstNode(); v != null; v = G.getNextNode(v)) {
                    double xv = xPos.getValue(v);
                    double yv = yPos.getValue(v);

                    for (Node u = G.getFirstNode(); u != null; u = G.getNextNode(u)) {
                        if (u == v)
                            continue;
                        double xDist = xv - xPos.getValue(u);
                        double yDist = yv - yPos.getValue(u);
                        double dist = xDist * xDist + yDist * yDist;
                        if (dist < 1e-3)
                            dist = 1e-3;
                        double repulse = k * k / dist;
                        xDispl.set(v, xDispl.getValue(v) + repulse * xDist);
                        yDispl.set(v, yDispl.getValue(v) + repulse * yDist);
                    }

                    for (Edge e = G.getFirstEdge(); e != null; e = G.getNextEdge(e)) {
                        final Node a = G.getSource(e);
                        final Node b = G.getTarget(e);
                        if (a == v || b == v)
                            continue;
                        double xDist = xv - (xPos.getValue(a) + xPos.getValue(b)) / 2;
                        double yDist = yv - (yPos.getValue(a) + yPos.getValue(b)) / 2;
                        double dist = xDist * xDist + yDist * yDist;
                        if (dist < 1e-3)
                            dist = 1e-3;
                        double repulse = k * k / dist;
                        xDispl.set(v, xDispl.getValue(v) + repulse * xDist);
                        yDispl.set(v, yDispl.getValue(v) + repulse * yDist);
                    }
                }

                // attractive forces

                for (Edge e = G.getFirstEdge(); e != null; e = G.getNextEdge(e)) {
                    final Node u = G.getSource(e);
                    final Node v = G.getTarget(e);

                    double xDist = xPos.getValue(v) - xPos.getValue(u);
                    double yDist = yPos.getValue(v) - yPos.getValue(u);

                    double dist = Math.sqrt(xDist * xDist + yDist * yDist);

                    dist /= ((G.getDegree(u) + G.getDegree(v)) / 16.0);

                    xDispl.set(v, xDispl.getValue(v) - xDist * dist / k);
                    yDispl.set(v, yDispl.getValue(v) - yDist * dist / k);
                    xDispl.set(u, xDispl.getValue(u) + xDist * dist / k);
                    yDispl.set(u, yDispl.getValue(u) + yDist * dist / k);
                }

                // preventions

                for (Node v = G.getFirstNode(); v != null; v = G.getNextNode(v)) {
                    double xd = xDispl.getValue(v);
                    double yd = yDispl.getValue(v);

                    final double dist = Math.sqrt(xd * xd + yd * yd);

                    xd = tx * xd / dist;
                    yd = ty * yd / dist;

                    xPos.set(v, xPos.getValue(v) + xd);
                    yPos.set(v, yPos.getValue(v) + yd);
                }
            }

            // set node positions
            for (Node v = G.getFirstNode(); v != null; v = G.getNextNode(v)) {
                setLocation(v, xPos.getValue(v), yPos.getValue(v));
            }
        } catch (Exception ex) {
            Basic.caught(ex);
        }
    }

    /**
     * Paint.
     *
     * @param gc0 the Graphics
     */
    public void paint(Graphics gc0) {
        boolean inDrawOnScreen = (!ExportManager.inWriteToFileOrGetData() && !inPrint);

        Graphics2D gc = (Graphics2D) gc0;

        Rectangle totalRect;
        Rectangle frameRect;
        frameRect = new Rectangle(getScrollPane().getHorizontalScrollBar().getValue(),
                getScrollPane().getVerticalScrollBar().getValue(),
                getScrollPane().getHorizontalScrollBar().getVisibleAmount(),
                getScrollPane().getVerticalScrollBar().getVisibleAmount());

        if (inDrawOnScreen)
            totalRect = frameRect;
        else
            totalRect = trans.getPreferredRect();

        /*
        gc.setColor(Color.GREEN);
        gc.draw(getBBox());
        */

        if (canvasColor != null) {
            gc.setColor(inDrawOnScreen ? canvasColor : Color.WHITE);
            if (!inPrint)
                gc.fill(totalRect);
        }

        if (inDrawOnScreen && trans.getMagnifier().isActive())
            trans.getMagnifier().draw(gc);

        /*
        Point2D c = trans.getDeviceRotationCenter();
        Rectangle2D r = new Rectangle2D.Double(c.getX()-3,c.getY()-3,6,6);
        gc.setColor(Color.red);
        gc.draw(r);
         gc.fill(r);
        */

        gc.setColor(Color.BLACK);

        // gc.draw(trans.w2d(getBBox()));

        if (graphDrawer != null)
            graphDrawer.paint(gc, inDrawOnScreen ? totalRect : null);

        if (getFoundNode() != null && (getFoundNode().getOwner() == null || !getSelected(getFoundNode()))) {
            setFoundNode(null);
        }

        if (getFoundNode() != null) {
            Node v = getFoundNode();
            NodeView nv = getNV(v);
            boolean selected = getSelected(v);
            if (selected) {
                if (nv.getLabel() != null)
                    nv.setLabelSize(Basic.getStringSize(gc, getLabel(v), getFont(v)));
                Color saveColor = nv.getLabelBackgroundColor();
                nv.setLabelBackgroundColor(Color.YELLOW);
                nv.drawLabel(gc, trans, getFont(), true);
                nv.setLabelBackgroundColor(saveColor);
            }
        }
        drawScaleBar(gc, inPrint ? totalRect : frameRect);
        drawPoweredBy(gc, inPrint ? totalRect : frameRect);
    }

    /**
     * draws a scale bar
     */
    protected void drawScaleBar(Graphics2D gc, Rectangle rect) {

        if (isDrawScaleBar() && getGraph().getNumberOfNodes() > 0) {
            gc.setColor(Color.gray);
            gc.setStroke(new BasicStroke(1));


            double d = 0.00001;
            for (; d < 1000000; d *= 10) {
                Point2D start = trans.w2d(new Point2D.Double(0, 0));
                Point2D finish = trans.w2d(new Point2D.Double(10 * d, 0));
                if (Math.abs(start.getX() - finish.getX()) > 150)
                    break;
            }

            int x = rect.x + 20;
            int y = rect.y + 20;

            Point2D start = trans.w2d(new Point2D.Double(0, 0));
            Point2D finish = trans.w2d(new Point2D.Double(d, 0));
            finish = new Point2D.Double(start.distance(finish) + x, y);
            start = new Point2D.Double(x, y);

            gc.drawLine((int) start.getX(), (int) start.getY(), (int) finish.getX(), (int) finish.getY());

            Font oldFont = gc.getFont();
            gc.setFont(scaleBarFont);

            gc.drawLine((int) start.getX(), (int) start.getY() - 3, (int) start.getX(), (int) start.getY() + 3);
            gc.drawLine((int) finish.getX(), (int) finish.getY() - 3, (int) finish.getX(), (int) finish.getY() + 3);

            gc.drawString("" + d, (int) finish.getX() + 2, (int) finish.getY() + 6);

            gc.setFont(oldFont);
        }
    }

    /**
     * draws the powered by logo
     *
     * @param gc
     */
    protected void drawPoweredBy(Graphics2D gc, Rectangle rect) {
        if (POWEREDBY != null && POWEREDBY.length() > 2) {
            gc.setColor(Color.gray);
            gc.setStroke(new BasicStroke(1));
            gc.setFont(poweredByFont);
            int width = (int) Basic.getStringSize(gc, POWEREDBY, gc.getFont()).getWidth();
            int x = rect.x + rect.width - width - 2;
            int y = rect.y + rect.height - 2;
            gc.drawString(POWEREDBY, x, y);
        }
    }

    /**
     * Fit graph to canvas.
     */
    public void fitGraphToWindow() {
        Dimension size = scrollPane.getSize();
        if (getGraph().getNumberOfNodes() > 0)
            trans.fitToSize(new Dimension(size.width - 200, size.height - 200));
        else
            trans.fitToSize(new Dimension(0, 0));
        centerGraph();
    }

    /**
     * centers the graph
     */
    public void centerGraph() {
        JScrollBar hScrollBar = getScrollPane().getHorizontalScrollBar();
        hScrollBar.setValue((hScrollBar.getMaximum() - hScrollBar.getModel().getExtent() + hScrollBar.getMinimum()) / 2);
        JScrollBar vScrollBar = getScrollPane().getVerticalScrollBar();
        vScrollBar.setValue((vScrollBar.getMaximum() - vScrollBar.getModel().getExtent() + vScrollBar.getMinimum()) / 2);
/*
        JScrollBar hScrollBar = getScrollPane().getHorizontalScrollBar();
        int x = trans.getLeftMargin() - 100;
        hScrollBar.setValue(x);
        JScrollBar vScrollBar = getScrollPane().getVerticalScrollBar();
        int y = trans.getTopMargin() - 100;
        vScrollBar.setValue(y);
        getScrollPane().getViewport().setViewPosition(new Point(x, y));
        */
        //revalidate();
    }

    /**
     * reset the transform margins after a resize or center graph operation.
     * This automatically sets the margins to half of the width or height of the pane
     */
    public void recomputeMargins() {
        Dimension size = scrollPane.getSize();
        if (size.width == 0 || size.height == 0) {
            scrollPane.setSize(getSize());
            size = getSize();
        }

        trans.setLeftMargin(size.width / 2);
        trans.setRightMargin(size.width / 2);
        trans.setTopMargin(size.height / 2);
        trans.setBottomMargin(size.height / 2);

        Dimension ps = trans.getPreferredSize();
        if (Math.abs(size.width - ps.width) > 3 || Math.abs(size.height - ps.height) > 3) {
            setPreferredSize(ps);
            getScrollPane().getViewport().setViewSize(ps);
        }
        //revalidate();
    }

    /**
     * We want bidirectional edges to be drawn parallel, not on top of each
     * other and this method does the necessary coordinate adjustments.
     *
     * @param pv source position in device coordinates
     * @param pw target position in device coordinates
     */
    public void adjustBiEdge(Point pv, Point pw) {
        Point2D p = new Point2D.Double(pw.getX() - pv.getX(), pw.getY() - pv.getY());
        if (p.getX() == 0 && p.getY() == 0)
            return;
        double alpha = Geometry.computeAngle(p) + 1.57079632679489661923; // PI/2
        p = Geometry.translateByAngle(pv, alpha, 3);
        pv.setLocation(p.getX(), p.getY());
        p = Geometry.translateByAngle(pw, alpha, 3);
        pw.setLocation(p.getX(), p.getY());
    }

    /**
     * We want multiedges to be drawn parallel, not on top of each
     * other.
     * This method does the necessary coordinate adjustments.
     *
     * @param i  the rank 0..n-1 of the edge
     * @param n  the number of parallel edges
     * @param pv source position in device coordinates
     * @param pw target position in device coordinates
     */
    public void adjustMultiEdge(int i, int n, Point pv, Point pw) {
        Point2D p = new Point2D.Double(pw.getX() - pv.getX(), pw.getY() - pv.getY());
        if (p.getX() == 0 && p.getY() == 0)
            return;
        double offset = 2.0 * (i - 0.5 * (n - 1));
        double alpha = Geometry.computeAngle(p) + 1.57079632679489661923; // PI/2
        p = Geometry.translateByAngle(pv, alpha, offset);
        pv.setLocation(p.getX(), p.getY());
        p = Geometry.translateByAngle(pw, alpha, offset);
        pw.setLocation(p.getX(), p.getY());
    }


    /**
     * Update.
     *
     * @param gc the graphics context.
     */
    public void update(Graphics gc) {
        paint(gc);
    }

    /**
     * Print the graph associated with this viewer.
     *
     * @param gc0        the graphics context.
     * @param format     page format
     * @param pagenumber page index
     */
    public int print(Graphics gc0, PageFormat format, int pagenumber) throws PrinterException {
        if (pagenumber == 0) {
            Graphics2D gc = ((Graphics2D) gc0);
            gc.setFont(getFont());

            Dimension dim = trans.getPreferredSize();

            int image_w = dim.width;
            int image_h = dim.height;

            double paper_x = format.getImageableX() + 1;
            double paper_y = format.getImageableY() + 1;
            double paper_w = format.getImageableWidth() - 2;
            double paper_h = format.getImageableHeight() - 2;

            double scale_x = paper_w / image_w;
            double scale_y = paper_h / image_h;
            double scale = (scale_x <= scale_y) ? scale_x : scale_y;

            double shift_x = paper_x + (paper_w - scale * image_w) / 2.0;
            double shift_y = paper_y + (paper_h - scale * image_h) / 2.0;

            gc.translate(shift_x, shift_y);
            gc.scale(scale, scale);

            gc.setStroke(new BasicStroke(1.0f));
            gc.setColor(Color.BLACK);

            boolean save = getAutoLayoutLabels();
            setAutoLayoutLabels(false);

            inPrint = true;
            paint(gc);
            inPrint = false;

            setAutoLayoutLabels(save);

            return Printable.PAGE_EXISTS;
        } else
            return Printable.NO_SUCH_PAGE;
    }


    /**
     * gets the canvas color
     *
     * @return canvas color
     */
    public Color getCanvasColor() {
        return canvasColor;
    }

    /**
     * sets the canvas color
     *
     * @param canvasColor the new color
     */

    public void setCanvasColor(Color canvasColor) {
        this.canvasColor = canvasColor;
    }

    /**
     * Add a NodeActionListener
     *
     * @param nal the NodeActionListener to be added
     */
    public void addNodeActionListener(NodeActionListener nal) {
        nodeActionListeners.add(nal);
    }

    /**
     * Remove a NodeActionListener
     *
     * @param nal the NodeActionListener to be removed
     */
    public void removeNodeActionListener(NodeActionListener nal) {
        nodeActionListeners.remove(nal);
    }

    public void removeAllNodeActionListeners() {
        nodeActionListeners.clear();
    }

    /* Fire doNew */
    public void fireDoNew(Node v) {
        for (NodeActionListener lis : nodeActionListeners) {
            if (v.getOwner() == null) break; // has been deleted
            lis.doNew(v);
        }
    }

    /**
     * Fire doNew
     *
     * @param v the source node of the new edge leading to the new node
     * @param w the new node
     */
    public void fireDoNew(Node v, Node w) {

        for (NodeActionListener lis : nodeActionListeners) {
            if (v.getOwner() == null) break; // has been deleted
            lis.doNew(v, w);
        }
    }


    /**
     * Fire doDelete
     *
     * @param v Node
     */
    public void fireDoDelete(Node v) {

        for (NodeActionListener lis : nodeActionListeners) {
            if (v.getOwner() == null) break; // has been deleted
            lis.doDelete(v);
        }
    }

    /**
     * Fire doClick
     *
     * @param nodes  NodeSet
     * @param clicks int
     */
    public void fireDoClick(NodeSet nodes, int clicks) {

        for (NodeActionListener lis : nodeActionListeners) {
            lis.doClick(nodes, clicks);
        }
    }

    /**
     * Fire doClickLabels
     *
     * @param nodes  NodeSet
     * @param clicks int
     */
    public void fireDoClickLabel(NodeSet nodes, int clicks) {

        for (NodeActionListener lis : nodeActionListeners) {
            lis.doClickLabel(nodes, clicks);
        }
    }

    public void fireDoClickPanel(int x, int y) {
    }

    /**
     * Fire doPress
     *
     * @param nodes NodeSet
     */
    public void fireDoPress(NodeSet nodes) {
        for (NodeActionListener lis : nodeActionListeners) {
            lis.doPress(nodes);
        }
    }

    /**
     * Fire doRelease
     *
     * @param nodes NodeSet
     */
    public void fireDoRelease(NodeSet nodes) {

        for (NodeActionListener lis : nodeActionListeners) {
            lis.doRelease(nodes);
        }
    }

    /**
     * Fire doSelect
     *
     * @param nodes NodeSet
     */
    public void fireDoSelect(NodeSet nodes) {
        for (NodeActionListener lis : nodeActionListeners) {
            lis.doSelect(nodes);
        }
    }

    /**
     * Fire doDeselect
     *
     * @param nodes NodeSet
     */
    public void fireDoDeselect(NodeSet nodes) {

        for (NodeActionListener lis : nodeActionListeners) {
            lis.doDeselect(nodes);
        }
    }

    /**
     * fire this whenever nodes have been moved
     */
    public void fireDoNodesMoved() {

        for (NodeActionListener lis : nodeActionListeners) {
            lis.doNodesMoved();
        }
    }

    /**
     * fire this whenever node labels have been moved
     */
    public void fireDoNodeLabelsMoved(NodeSet nodes) {

        for (NodeActionListener lis : nodeActionListeners) {
            lis.doMoveLabel(nodes);
        }
    }

    /**
     * fire this whenever edge labels have been moved
     */
    public void fireDoEdgeLabelsMoved(EdgeSet edges) {

        for (EdgeActionListener lis : edgeActionListeners) {
            lis.doLabelMoved(edges);
        }
    }

    /**
     * Add a EdgeActionListener
     *
     * @param eal the EdgeActionListener to be added
     */
    public void addEdgeActionListener(EdgeActionListener eal) {
        edgeActionListeners.add(eal);
    }

    /**
     * Remove a EdgeActionListener
     *
     * @param eal the EdgeActionListener to be removed
     */
    public void removeEdgeActionListener(EdgeActionListener eal) {
        edgeActionListeners.remove(eal);
    }

    public void removeAllEdgeActionListeners() {
        edgeActionListeners.clear();
    }

    /**
     * Fire doNew
     *
     * @param e Edge
     */
    public void fireDoNew(Edge e) {

        for (EdgeActionListener edgeActionListener : edgeActionListeners) {
            if (e == null || e.getOwner() == null) break; // has been deleted
            edgeActionListener.doNew(e);
        }
    }

    /**
     * Fire doDelete
     *
     * @param e Edge
     */
    public void fireDoDelete(Edge e) {

        for (EdgeActionListener edgeActionListener : edgeActionListeners) {
            if (e == null || e.getOwner() == null) break; // has been deleted
            edgeActionListener.doDelete(e);
        }
    }

    /**
     * Fire doClick
     *
     * @param edges  EdgeSet
     * @param clicks int
     */
    public void fireDoClick(EdgeSet edges, int clicks) {

        for (EdgeActionListener lis : edgeActionListeners) {
            lis.doClick(edges, clicks);
        }
    }

    /**
     * Fire doClickLabel
     *
     * @param edges  EdgeSet
     * @param clicks int
     */
    public void fireDoClickLabel(EdgeSet edges, int clicks) {

        for (EdgeActionListener lis : edgeActionListeners) {
            lis.doClickLabel(edges, clicks);
        }
    }

    /**
     * Fire doPress
     *
     * @param edges EdgeSet
     */
    public void fireDoPress(EdgeSet edges) {

        for (EdgeActionListener lis : edgeActionListeners) {
            lis.doPress(edges);
        }
    }

    /**
     * Fire doRelease
     *
     * @param edges EdgeSet
     */
    public void fireDoRelease(EdgeSet edges) {

        for (EdgeActionListener lis : edgeActionListeners) {
            lis.doRelease(edges);
        }
    }

    /**
     * Fire doSelect
     *
     * @param edges EdgeSet
     */
    public void fireDoSelect(EdgeSet edges) {

        for (EdgeActionListener lis : edgeActionListeners) {
            lis.doSelect(edges);
        }
    }

    /**
     * Fire doDeselect
     *
     * @param edges EdgeSet
     */
    public void fireDoDeselect(EdgeSet edges) {

        for (EdgeActionListener lis : edgeActionListeners) {
            lis.doDeselect(edges);
        }
    }


    /**
     * Add a panel action listener
     *
     * @param nal the NodeActionListener to be added
     */
    public void addPanelActionListener(PanelActionListener nal) {
        panelActionListeners.add(nal);
    }

    /**
     * Remove a PanelActionListener
     *
     * @param nal the PanelActionListener to be removed
     */
    public void removePanelActionListener(PanelActionListener nal) {
        panelActionListeners.remove(nal);
    }


    public void firePanelClicked(MouseEvent ev) {
        for (PanelActionListener pal : panelActionListeners) {
            pal.doMouseClicked(ev);
        }
    }

    /**
     * Gets the set of all selected nodes
     *
     * @return selected nodes
     */
    public NodeSet getSelectedNodes() {
        return selectedNodes;
    }

    /**
     * gets an iterator over all selected nodes, if any selected, otherwise over all nodes
     *
     * @return iterator
     */
    public Iterator<Node> getSelectedOrAllNodesIterator() {
        if (selectedNodes.size() > 0)
            return selectedNodes.iterator();
        else return getGraph().nodeIterator();
    }

    /**
     * Gets the set of all selected edges
     *
     * @return selected edges
     */
    public EdgeSet getSelectedEdges() {
        return selectedEdges;
    }

    /**
     * gets an iterator over all selected edges, if any selected, otherwise over all edges
     *
     * @return iterator
     */
    public Iterator getSelectedOrAllEdgesIterator() {
        if (selectedEdges.size() > 0)
            return selectedEdges.iterator();
        else
            return getGraph().edgeIterator();
    }

    /**
     * Gets the number of selected edges
     *
     * @return the number of selected edges
     */
    public int getNumberSelectedEdges() {
        return selectedEdges.size();
    }

    /**
     * Gets the number of selected nodes
     *
     * @return the number of selected nodes
     */
    public int getNumberSelectedNodes() {
        return selectedNodes.size();
    }

    /**
     * is user allowed to rubberband nodes?
     *
     * @return allowed to rubberband select nodes?
     */
    public boolean isAllowRubberbandNodes() {
        return allowRubberbandNodes;
    }

    /**
     * set user is allowed to rubberband select nodes
     *
     * @param allowRubberbandNodes
     */
    public void setAllowRubberbandNodes(boolean allowRubberbandNodes) {
        this.allowRubberbandNodes = allowRubberbandNodes;
    }

    /**
     * is user allowed to rubberband selecte edges?
     *
     * @return allowed to rubberband select edges?
     */
    public boolean isAllowRubberbandEdges() {
        return allowRubberbandEdges;
    }

    /**
     * allow interactive insertion and moving of edge internal points?
     *
     * @return true, if user is allowed to add internal points
     */
    public boolean isAllowInternalEdgePoints() {
        return allowInternalEdgePoints;
    }

    /**
     * set internal edge points insertion mode.
     *
     * @param allowInternalEdgePoints
     */
    public void setAllowInternalEdgePoints(boolean allowInternalEdgePoints) {
        this.allowInternalEdgePoints = allowInternalEdgePoints;
    }

    /**
     * set user is allowed to rubberband select edges
     *
     * @param allowRubberbandEdges
     */
    public void setAllowRubberbandEdges(boolean allowRubberbandEdges) {
        this.allowRubberbandEdges = allowRubberbandEdges;
    }

    /**
     * allow edit edge label on double click on edge label?
     *
     * @return allow edit
     */
    public boolean isAllowEditEdgeLabelsOnDoubleClick() {
        return allowEditEdgeLabelsOnDoubleClick;
    }

    /**
     * allow edit edge label on double click on edge label?
     *
     * @param allowEditEdgeLabelsOnDoubleClick
     */
    public void setAllowEditEdgeLabelsOnDoubleClick(boolean allowEditEdgeLabelsOnDoubleClick) {
        this.allowEditEdgeLabelsOnDoubleClick = allowEditEdgeLabelsOnDoubleClick;
    }

    /**
     * allow edit edge label on double click on edge label?
     *
     * @return allow edge
     */
    public boolean isAllowEditNodeLabelsOnDoubleClick() {
        return allowEditNodeLabelsOnDoubleClick;
    }

    /**
     * allow undo node label on double click on node?
     *
     * @param allowEditNodeLabelsOnDoubleClick
     */
    public void setAllowEditNodeLabelsOnDoubleClick(boolean allowEditNodeLabelsOnDoubleClick) {
        this.allowEditNodeLabelsOnDoubleClick = allowEditNodeLabelsOnDoubleClick;
    }

    /**
     * gets the bounding box of this graph in world coordinates
     *
     * @return bounding box
     */
    public Rectangle2D getBBox() {
        double xmin = Double.MIN_VALUE, xmax = Double.MIN_VALUE, ymin = Double.MAX_VALUE, ymax = Double.MAX_VALUE;
        boolean first = true;
        try {
            for (Node v = getGraph().getFirstNode(); v != null; v = getGraph().getNextNode(v)) {
                if (getLocation(v) == null)
                    continue;
                double x = getLocation(v).getX();
                double y = getLocation(v).getY();
                if (first) {
                    xmin = xmax = x;
                    ymin = ymax = y;
                    first = false;
                } else {
                    if (x < xmin) xmin = x;
                    if (x > xmax) xmax = x;
                    if (y < ymin) ymin = y;
                    if (y > ymax) ymax = y;
                }
            }
            for (Edge e = getGraph().getFirstEdge(); e != null; e = e.getNext()) {
                List<Point2D> internalPoints = getInternalPoints(e);
                if (internalPoints != null) {
                    for (Point2D apt : internalPoints) {
                        double x = apt.getX();
                        double y = apt.getY();
                        if (first) {
                            xmin = xmax = x;
                            ymin = ymax = y;
                            first = false;
                        } else {
                            if (x < xmin) xmin = x;
                            if (x > xmax) xmax = x;
                            if (y < ymin) ymin = y;
                            if (y > ymax) ymax = y;
                        }
                    }
                }
            }

        } catch (NotOwnerException ex) {
            //Basic.caught(ex);
        }
        Rectangle2D rect = new Rectangle2D.Double(xmin, ymin, xmax - xmin, ymax - ymin);

        // add in the world shapes, too
        //  for (Iterator it = glyphs.keySet().iterator(); it.hasNext();) {
        // WorldShape ws = (WorldShape) it.next();  @todo update
        //rect.add(ws.getShape().getBounds2D());
        //  }

        if (rect.getX() == Double.MIN_VALUE) // hasn't been set
            rect.setRect(0, 0, 100, 100);
        if (rect.getWidth() == 0 || rect.getHeight() == 0) {
            double m = Math.max(rect.getWidth(), rect.getHeight());
            if (m == 0)
                m = 100;
            rect.setRect(rect.getX(), rect.getY(), m, m);
        }
        return rect;
    }


    /**
     * get use label layouter?
     *
     * @return use layouter
     */
    public boolean getAutoLayoutLabels() {
        return autoLayoutLabels;
    }

    /**
     * set use label layouter
     *
     * @param autoLayoutLabels
     */
    public void setAutoLayoutLabels(boolean autoLayoutLabels) {
        this.autoLayoutLabels = autoLayoutLabels;
    }

    /**
     * gets the current font
     *
     * @return font
     */
    public Font getFont() {
        return font;
    }

    /**
     * sets the font
     *
     * @param font
     */
    public void setFont(Font font) {
        this.font = font;
    }


    /**
     * set  draw nodes at fixed size
     *
     * @param fixedNodeSize
     */
    public void setFixedNodeSize(boolean fixedNodeSize) {
        for (Node v = getGraph().getFirstNode(); v != null; v = v.getNext()) {
            getNV(v).setFixedSize(fixedNodeSize);
        }
        defaultNodeView.setFixedSize(fixedNodeSize);
    }

    /**
     * remove all internal points contained in an edge
     */
    public void removeAllInternalPoints() {
        for (Edge e = getGraph().getFirstEdge(); e != null; e = e.getNext())
            setInternalPoints(e, null);
    }

    /**
     * remove all internal points contained in an edge
     */
    public void removeAllLocations() {
        for (Node v = getGraph().getFirstNode(); v != null; v = v.getNext())
            setLocation(v, null);
    }

    /**
     * sets the dendroscope used to draw the graph and to handle mouse interactions
     *
     * @param graphDrawer
     */
    public void setGraphDrawer(IGraphDrawer graphDrawer) {
        this.graphDrawer = graphDrawer;
    }

    /**
     * gets the dendroscope used to draw the graph and to handle mouse interactions
     *
     * @return current graph dendroscope
     */
    public IGraphDrawer getGraphDrawer() {
        return graphDrawer;
    }

    /**
     * Returns the preferred size of the viewport for a view component.
     *
     * @return The preferredSize of a JViewport whose view is this Scrollable.
     * @see javax.swing.JViewport#getPreferredSize
     */
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    /**
     * @param visibleRect The view area visible within the viewport
     * @param orientation Either SwingConstants.VERTICAL or SwingConstants.HORIZONTAL.
     * @param direction   Less than zero to scroll up/left, greater than zero for down/right.
     * @return The "block" increment for scrolling in the specified direction.
     * This value should always be positive.
     * @see javax.swing.JScrollBar#setBlockIncrement
     */
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 200;
    }

    /**
     * Return true if a viewport should always force the height of this
     * Scrollable to match the height of the viewport.  For example a
     * columnar text view that flowed text in left to right columns
     * could effectively disable vertical scrolling by returning
     * true here.
     * <p/>
     * Scrolling containers, like JViewport, will use this method each
     * time they are validated.
     *
     * @return True if a viewport should force the Scrollables height to match its own.
     */
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    /**
     * Return true if a viewport should always force the width of this
     * <code>Scrollable</code> to match the width of the viewport.
     * For example a normal
     * text view that supported line wrapping would return true here, since it
     * would be undesirable for wrapped lines to disappear beyond the right
     * edge of the viewport.  Note that returning true for a Scrollable
     * whose ancestor is a JScrollPane effectively disables horizontal
     * scrolling.
     * <p/>
     * Scrolling containers, like JViewport, will use this method each
     * time they are validated.
     *
     * @return True if a viewport should force the Scrollables width to match its own.
     */
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    /**
     * Components that display logical rows or columns should compute
     * the scroll increment that will completely expose one new row
     * or column, depending on the value of orientation.  Ideally,
     * components should handle a partially exposed row or column by
     * returning the distance required to completely expose the item.
     * <p/>
     * Scrolling containers, like JScrollPane, will use this method
     * each time the user requests a unit scroll.
     *
     * @param visibleRect The view area visible within the viewport
     * @param orientation Either SwingConstants.VERTICAL or SwingConstants.HORIZONTAL.
     * @param direction   Less than zero to scroll up/left, greater than zero for down/right.
     * @return The "unit" increment for scrolling in the specified direction.
     * This value should always be positive.
     * @see javax.swing.JScrollBar#setUnitIncrement
     */
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 10;
    }

    /**
     * gets the scrollpane associated with this viewer
     *
     * @return scroll pane
     */
    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    /**
     * get the current popup listener
     *
     * @return graph popup listener
     */
    public IPopupListener getPopupListener() {
        return popupListener;
    }

    /**
     * sets the popup listener
     *
     * @param popupListener
     */
    public void setPopupListener(IPopupListener popupListener) {
        this.popupListener = popupListener;
    }

    /**
     * fire the node popup menu
     *
     * @param me
     * @param nodes
     */
    public void fireNodePopup(MouseEvent me, NodeSet nodes) {
        if (popupListener != null) popupListener.doNodePopup(me, nodes);
    }

    /**
     * fire the node label popup menu
     *
     * @param me
     * @param nodes
     */
    public void fireNodeLabelPopup(MouseEvent me, NodeSet nodes) {
        if (popupListener != null) popupListener.doNodeLabelPopup(me, nodes);
    }

    /**
     * fire the edge popup menu
     *
     * @param me
     * @param edges
     */
    public void fireEdgePopup(MouseEvent me, EdgeSet edges) {
        if (popupListener != null) popupListener.doEdgePopup(me, edges);
    }

    /**
     * fire the edge label popup menu
     *
     * @param me
     * @param edges
     */
    public void fireEdgeLabelPopup(MouseEvent me, EdgeSet edges) {
        if (popupListener != null) popupListener.doEdgeLabelPopup(me, edges);
    }

    /**
     * fire the panel popup (when nothing was hit)
     *
     * @param me
     */
    public void firePanelPopup(MouseEvent me) {
        if (popupListener != null) popupListener.doPanelPopup(me);
    }

    private NodeSet origNodeSelection = null;

    /**
     * replace current selection of nodes by given one. Do not fire any
     * node deselection events
     *
     * @param nodes
     */
    public void pushNodeSelection(NodeSet nodes) {
        if (origNodeSelection != null)
            throw new RuntimeException("pushNodeSelection(): stack full");
        origNodeSelection = new NodeSet(getGraph());
        origNodeSelection.addAll(selectedNodes);
        selectedNodes.clear();
        for (Node v = nodes.getFirstElement(); v != null; v = nodes.getNextElement(v))
            setSelected(v, true);
    }

    /**
     * restores node selection to original one. Doesn't fire any node selection events
     */
    public void popNodeSelection() {
        if (origNodeSelection == null)
            throw new RuntimeException("popNodeSelection(): stack empty");
        selectedNodes.clear();
        for (Node v = origNodeSelection.getFirstElement(); v != null; v = origNodeSelection.getNextElement(v))
            setSelected(v, true);

        origNodeSelection = null;

    }

    /**
     * currently locked for critical user input?
     *
     * @return true, if locked
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * sets the cursor
     *
     * @param cursor
     */
    public void setCursor(Cursor cursor) {
        getScrollPane().setCursor(cursor);
        getScrollPane().getHorizontalScrollBar().setCursor(Cursor.getDefaultCursor());
        getScrollPane().getVerticalScrollBar().setCursor(Cursor.getDefaultCursor());
    }

    /**
     * gets the cursor
     *
     * @return cursor
     */
    public Cursor getCursor() {
        return getScrollPane().getCursor();
    }

    /**
     * reset cursor to open hand cursor
     */
    public void resetCursor() {
        if (isLocked())
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        else
            //setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            setCursor(Cursors.getOpenHand());
    }

    /**
     * flip node label layout horizontally, vertically, or both
     *
     * @param hflip
     * @param vflip
     */
    public void flipNodeLabels(boolean hflip, boolean vflip) {
        for (Node v = getGraph().getFirstNode(); v != null; v = v.getNext()) {
            switch (getLabelLayout(v)) {
                case NodeView.EAST:
                    if (hflip) setLabelLayout(v, NodeView.WEST);
                    break;
                case NodeView.WEST:
                    if (hflip) setLabelLayout(v, NodeView.EAST);
                    break;
                case NodeView.NORTH:
                    if (vflip) setLabelLayout(v, NodeView.SOUTH);
                    break;
                case NodeView.NORTHEAST:
                    if (hflip && vflip)
                        setLabelLayout(v, NodeView.SOUTHWEST);
                    else if (hflip)
                        setLabelLayout(v, NodeView.NORTHWEST);
                    else if (vflip) setLabelLayout(v, NodeView.SOUTHEAST);
                    break;
                case NodeView.NORTHWEST:
                    if (hflip && vflip)
                        setLabelLayout(v, NodeView.SOUTHEAST);
                    else if (hflip)
                        setLabelLayout(v, NodeView.NORTHEAST);
                    else if (vflip) setLabelLayout(v, NodeView.SOUTHWEST);
                    break;
                case NodeView.SOUTH:
                    if (vflip) setLabelLayout(v, NodeView.NORTH);
                    break;

                case NodeView.SOUTHEAST:
                    if (hflip && vflip)
                        setLabelLayout(v, NodeView.NORTHWEST);
                    else if (hflip)
                        setLabelLayout(v, NodeView.SOUTHWEST);
                    else if (vflip) setLabelLayout(v, NodeView.NORTHEAST);
                    break;
                case NodeView.SOUTHWEST:
                    if (hflip && vflip)
                        setLabelLayout(v, NodeView.NORTHEAST);
                    else if (hflip)
                        setLabelLayout(v, NodeView.SOUTHEAST);
                    else if (vflip) setLabelLayout(v, NodeView.NORTHWEST);
                    break;
                default:
                    break;
            }
        }
    }


    /**
     * writes the graphview
     *
     * @param w
     * @throws IOException
     */
    public void write(Writer w) throws IOException {
        Graph graph = getGraph();
        Map<Integer, Integer> nodeId2Number = new HashMap<>();
        Map<Integer, Integer> edgeId2Number = new HashMap<>();

        int count = 0;
        for (Node v = graph.getFirstNode(); v != null; v = v.getNext()) {
            nodeId2Number.put(v.getId(), ++count);
        }
        count = 0;
        for (Edge e = graph.getFirstEdge(); e != null; e = e.getNext()) {
            edgeId2Number.put(e.getId(), ++count);
        }
        write(w, nodeId2Number, edgeId2Number);
    }

    /**
     * writes the graphview
     *
     * @param w
     * @param nodeId2Number the node-id to number mapping established by Graph.write
     * @param edgeId2Number the edge-id to number mapping established by Graph.write
     * @throws IOException
     */
    public void write(Writer w, Map nodeId2Number, Map edgeId2Number) throws IOException {
        Graph graph = getGraph();
        w.write("{GRAPHVIEW\n");
        w.write("nnodes=" + graph.getNumberOfNodes() + " nedges=" + graph.getNumberOfEdges() + "\n");
        w.write("nodes\n");
        NodeView prevNV = null;
        for (Node v = graph.getFirstNode(); v != null; v = v.getNext()) {
            w.write(nodeId2Number.get(v.getId()) + ":");
            getNV(v).write(w, prevNV);
            prevNV = getNV(v);
        }
        w.write("edges\n");
        EdgeView prevEV = null;
        for (Edge e = graph.getFirstEdge(); e != null; e = e.getNext()) {
            w.write((edgeId2Number.get(e.getId())) + ":");
            getEV(e).write(w, prevEV);
            prevEV = getEV(e);
        }
        w.write("}\n");
    }


    /**
     * read graph and graphview.
     *
     * @param r
     * @throws IOException
     */
    public void read(Reader r) throws IOException {
        final Graph graph = getGraph();

        Num2NodeArray num2node = new Num2NodeArray(graph.getNumberOfNodes() + 1);
        Num2EdgeArray num2edge = new Num2EdgeArray(graph.getNumberOfEdges() + 1);

        int count = 0;
        for (Node v = graph.getFirstNode(); v != null; v = v.getNext())
            num2node.put(++count, v);


        count = 0;
        for (Edge e = graph.getFirstEdge(); e != null; e = e.getNext())
            num2edge.put(++count, e);

        read(r, num2node, num2edge);
    }

    /**
     * read graph and graphview.
     *
     * @param r
     * @param num2node the num2node map computed by Graph.read
     * @param num2edge the num2edge map computed by Graph.read
     * @throws IOException
     */
    public void read(Reader r, Num2NodeArray num2node, Num2EdgeArray num2edge) throws IOException {
        final Graph graph = getGraph();

        NexusStreamParser np = new NexusStreamParser(r);
        np.matchRespectCase("{GRAPHVIEW\n");
        np.matchRespectCase("nnodes = " + graph.getNumberOfNodes() + " nedges = " + graph.getNumberOfEdges());

        np.matchRespectCase("nodes");
        NodeView prevNV = defaultNodeView;
        while (!np.peekMatchRespectCase("edges")) {
            int vid = np.getInt(1, graph.getNumberOfNodes());
            Node v = num2node.get(vid);
            NodeView nv = getNV(v);
            nv.read(np, np.getTokensRespectCase(":", ";"), prevNV);
            prevNV = nv;
        }

        np.matchRespectCase("edges");
        EdgeView prevEV = defaultEdgeView;
        while (!np.peekMatchRespectCase("}")) {
            int eid = np.getInt(1, graph.getNumberOfEdges());
            Edge e = num2edge.get(eid);
            EdgeView ev = getEV(e);
            ev.read(np, np.getTokensRespectCase(":", ";"), prevEV);
            prevEV = ev;
        }
        np.matchRespectCase("}");
    }

    /**
     * rotateAbout labels of all selected nodes and edges
     *
     * @param percent
     */
    public void rotateLabels(NodeSet nodes, EdgeSet edges, int percent) {
        float angle = (float) (Math.PI / 50.0 * percent);
        if (nodes != null)
            for (Node v : nodes) {
                NodeView nv = getNV(v);
                if (nv.getLabelVisible() && nv.getLabel() != null && nv.getLabel().length() > 0) {
                    nv.setLabelAngle(nv.getLabelAngle() + angle);
                    nv.setLabelLayout(NodeView.USER);
                }
            }
        if (edges != null)
            for (Edge e : edges) {
                EdgeView ev = getEV(e);
                if (ev.getLabelVisible() && ev.getLabel() != null && ev.getLabel().length() > 0) {
                    ev.setLabelAngle(ev.getLabelAngle() + angle);
                    ev.setLabelLayout(EdgeView.USER);
                }
            }
    }


    /**
     * node found by search, must be drawn  if !=null
     *
     * @return found node
     */
    public Node getFoundNode() {
        return foundNode;
    }

    /**
     * node found by search, must be drawn if !=null
     *
     * @param foundNode
     */
    public void setFoundNode(Node foundNode) {
        this.foundNode = foundNode;
    }

    /**
     * automatically repaint on graph change?
     *
     * @return true, if automatically repainted
     */
    public boolean isRepaintOnGraphHasChanged() {
        return repaintOnGraphHasChanged;
    }

    /**
     * automatically repaint on graph change?
     *
     * @param repaintOnGraphHasChanged
     */
    public void setRepaintOnGraphHasChanged(boolean repaintOnGraphHasChanged) {
        this.repaintOnGraphHasChanged = repaintOnGraphHasChanged;
    }

    /**
     * set the default label positions
     *
     * @param resetAll reset all labels, including user modified ones
     */
    public void resetLabelPositions(boolean resetAll) {
        if (getGraphDrawer() != null)
            getGraphDrawer().resetLabelPositions(resetAll);
    }

    public void reset() {
        //nodeViews = new NodeArray<NodeView>(G);
        //edgeViews = new EdgeArray<EdgeView>(G);

        /*trans = new Transform(this);
        trans.addChangeListener(new TransformChangedListener() {
            public void hasChanged(Transform trans) {
                recomputeMargins();
            }
        });*/
        trans.reset();
    }

    /**
     * select the given edges and all nodes and edges below
     *
     * @param edges
     */
    public void selectAllBelow(EdgeSet edges) {
        NodeSet seen = new NodeSet(getGraph());
        Stack<Node> stack = new Stack<>();
        for (Edge e = edges.getFirstElement(); e != null; e = edges.getNextElement(e)) {
            setSelected(e, true);
            Node v = e.getTarget();
            stack.push(v);
            seen.add(v);
            while (stack.size() > 0) {
                v = stack.pop();
                setSelected(v, true);
                for (Edge f = v.getFirstOutEdge(); f != null; f = v.getNextOutEdge(f)) {
                    setSelected(f, true);
                    Node w = f.getTarget();
                    if (!seen.contains(w)) {
                        stack.add(w);
                        seen.add(w);
                    }
                }
            }
        }
    }

    public Color getColorSelectedNodes() {
        Color color = null;
        for (Node v : getSelectedNodes()) {
            if (color == null)
                color = getColor(v);
            else if (!color.equals(getColor(v)))
                return null;
        }
        return color;
    }

    public Color getBackgroundColorSelectedNodes() {
        Color color = null;
        for (Node v : getSelectedNodes()) {
            if (color == null)
                color = getBackgroundColor(v);
            else if (!color.equals(getBackgroundColor(v)))
                return null;
        }
        return color;
    }

    public Color getBorderColorSelectedNodes() {
        Color color = null;
        for (Node v : getSelectedNodes()) {
            if (color == null)
                color = getBorderColor(v);
            else if (!color.equals(getBorderColor(v)))
                return null;
        }
        return color;
    }

    public Color getLabelColorSelectedNodes() {
        Color color = null;
        for (Node v : getSelectedNodes()) {
            if (color == null)
                color = getLabelColor(v);
            else if (!color.equals(getLabelColor(v)))
                return null;
        }
        return color;
    }

    public Color getLabelBackgroundColorSelectedNodes() {
        Color color = null;
        for (Node v : getSelectedNodes()) {
            if (color == null)
                color = getLabelBackgroundColor(v);
            else if (!color.equals(getLabelBackgroundColor(v)))
                return null;
        }
        return color;
    }


    public int getWidthSelectedNodes() {
        int width = -1;
        for (Node v : getSelectedNodes()) {
            if (width == -1)
                width = getWidth(v);
            else if (width != getWidth(v))
                return -1;
        }
        return width;
    }

    public int getHeightSelectedNodes() {
        int height = -1;
        for (Node v : getSelectedNodes()) {
            if (height == -1)
                height = getHeight(v);
            else if (height != getHeight(v))
                return -1;
        }
        return height;
    }

    public int getLineWidthSelectedNodes() {
        int linewidth = -1;
        for (Node v : getSelectedNodes()) {
            if (linewidth == -1)
                linewidth = getLineWidth(v);
            else if (linewidth != getLineWidth(v))
                return -1;
        }
        return linewidth;
    }

    public void setBorderColorSelectedNodes(Color a) {
        for (Node v : getSelectedNodes()) {
            setBorderColor(v, a);
        }
    }

    public boolean setLabelBackgroundColorSelectedNodes(Color a) {
        boolean changed = false;
        for (Node v : getSelectedNodes()) {
            if (isLabelVisible(v) && getLabelBackgroundColor(v) == null || !getLabelBackgroundColor(v).equals(a)) {
                changed = true;
                setLabelBackgroundColor(v, a);
            }
        }
        return changed;
    }


    public void setWidthSelectedNodes(byte a) {
        for (Node v : getSelectedNodes()) {
            setWidth(v, a);
        }
    }

    public void setHeightSelectedNodes(byte a) {
        for (Node v : getSelectedNodes()) {
            setHeight(v, a);
        }
    }

    public void setShapeSelectedNodes(byte a) {
        for (Node v : getSelectedNodes()) {
            setShape(v, a);
        }
    }

    public byte getShapeSelectedNodes() {
        byte value = 0;
        for (Node v : getSelectedNodes()) {
            if (value == 0)
                value = getShape(v);
            else if (value != getShape(v))
                return 0;
        }
        return value;
    }

    public Font getFontSelected() {
        Font font = null;
        for (Node v : getSelectedNodes()) {
            if (font == null)
                font = getFont(v);
            else if (getFont(v) != null && !font.equals(getFont(v)))
                return null;
        }
        for (Edge e : getSelectedEdges()) {
            if (font == null)
                font = getFont(e);
            else if (getFont(e) != null && !font.equals(getFont(e)))
                return null;
        }
        return font;
    }


    public Color getColorSelectedEdges() {
        Color color = null;
        for (Edge e : getSelectedEdges()) {
            if (color == null)
                color = getColor(e);
            else if (!color.equals(getColor(e)))
                return null;
        }
        return color;
    }

    public Color getLabelColorSelectedEdges() {
        Color color = null;
        for (Edge e : getSelectedEdges()) {
            if (color == null)
                color = getLabelColor(e);
            else if (!color.equals(getLabelColor(e)))
                return null;
        }
        return color;
    }

    public Color getLabelBackgroundColorSelectedEdges() {
        Color color = null;
        for (Edge e : getSelectedEdges()) {
            if (color == null)
                color = getLabelBackgroundColor(e);
            else if (!color.equals(getLabelBackgroundColor(e)))
                return null;
        }
        return color;
    }

    public boolean setLabelBackgroundColorSelectedEdges(Color a) {
        boolean changed = false;
        for (Edge e : getSelectedEdges()) {
            if (getLabelVisible(e) && (getLabelBackgroundColor(e) == null || !getLabelBackgroundColor(e).equals(a))) {
                changed = true;
                setLabelBackgroundColor(e, a);
            }
        }
        return changed;
    }

    public int getLineWidthSelectedEdges() {
        int value = -1;
        for (Edge e : getSelectedEdges()) {
            if (value == -1)
                value = getLineWidth(e);
            else if (value != getLineWidth(e))
                return -1;
        }
        return value;
    }

    public int getDirectionSelectedEdges() {
        int value = -1;
        for (Edge e : getSelectedEdges()) {
            if (value == -1)
                value = getDirection(e);
            else if (value != getDirection(e))
                return -1;
        }
        return value;
    }

    public void setShapeSelectedEdges(byte a) {
        for (Edge e : getSelectedEdges()) {
            setShape(e, a);
        }
    }

    public byte getShapeSelectedEdges() {
        byte value = 0;
        for (Edge e : getSelectedEdges()) {
            if (value == 0)
                value = getShape(e);
            else if (value != getShape(e))
                return 0;
        }
        return value;
    }

    public void setDirectionSelectedEdges(byte a) {
        for (Edge e : getSelectedEdges()) {
            setDirection(e, a);
        }
    }

    public Font getFontSelectedEdges() {
        Font font = null;
        for (Edge e : getSelectedEdges()) {
            if (font == null)
                font = getFont(e);
            else if (getFont(e) != null && !font.equals(getFont(e)))
                return null;
        }
        return font;
    }

    public boolean hasSelectedNodes() {
        return getSelectedNodes().size() > 0;
    }

    public boolean hasSelectedEdges() {
        return getSelectedEdges().size() > 0;
    }

    public void setLabelVisibleSelectedNodes(boolean visible) {
        for (Node v : getSelectedNodes())
            setLabelVisible(v, visible);
    }

    public boolean hasLabelVisibleSelectedNodes() {
        for (Node v : getSelectedNodes())
            if (getLabelVisible(v) && getLabel(v) != null)
                return true;
        return false;
    }

    public void setLabelVisibleSelectedEdges(boolean visible) {
        for (Edge e : getSelectedEdges())
            setLabelVisible(e, visible);
    }

    public boolean hasLabelVisibleSelectedEdges() {
        for (Edge e : getSelectedEdges())
            if (getLabelVisible(e) && getLabel(e) != null)
                return true;
        return false;
    }

    public boolean getLockXYScale() {
        return trans.getLockXYScale();
    }

    public void rotateLabelsSelectedNodes(int percent) {
        float angle = (float) (Math.PI / 50.0 * percent);
        for (Node v : getSelectedNodes()) {
            NodeView nv = getNV(v);
            if (nv.getLabelVisible() && nv.getLabel() != null && nv.getLabel().length() > 0) {
                nv.setLabelAngle(nv.getLabelAngle() + angle);
                nv.setLabelLayout(NodeView.USER);
            }
        }

    }

    public void rotateLabelsSelectedEdges(int percent) {
        float angle = (float) (Math.PI / 50.0 * percent);
        for (Edge e : getSelectedEdges()) {
            EdgeView nv = getEV(e);
            if (nv.getLabelVisible() && nv.getLabel() != null && nv.getLabel().length() > 0) {
                nv.setLabelAngle(nv.getLabelAngle() + angle);
                nv.setLabelLayout(EdgeView.USER);
            }
        }
    }

    public JPanel getPanel() {
        return this;
    }

    public void setRandomColorsSelectedNodes(boolean foreground, boolean background, boolean labelforeground, boolean labelbackgrond) {
        Random rand = new Random();
        for (Node v : getSelectedNodes()) {
            Color color = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
            if (foreground)
                setColor(v, color);
            if (background)
                setBackgroundColor(v, color);
            if (isLabelVisible(v)) {
                if (labelforeground)
                    setLabelColor(v, color);
                if (labelbackgrond)
                    setLabelBackgroundColor(v, color);
            }
        }
    }

    public void setRandomColorsSelectedEdges(boolean foreground, boolean labelforeground, boolean labelbackgrond) {
        Random rand = new Random();
        for (Edge e : getSelectedEdges()) {
            Color color = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
            if (foreground)
                setColor(e, color);
            if (isLabelVisible(e)) {
                if (labelforeground)
                    setLabelColor(e, color);
                if (labelbackgrond)
                    setLabelBackgroundColor(e, color);
            }
        }
    }

    /**
     * set the tool tip text to the label of the given node
     *
     * @param v
     */
    public void setToolTipText(Node v) {
        setToolTipText(getLabel(v));
    }

    public String getPOWEREDBY() {
        return POWEREDBY;
    }

    public void setPOWEREDBY(String POWEREDBY) {
        this.POWEREDBY = POWEREDBY;
    }

    /**
     * selects all connected components containing any of the given nodes
     *
     * @param nodes
     */
    public void selectConnectedComponents(NodeSet nodes) {
        final NodeSet nodesToSelect = new NodeSet(G);
        for (Node v : nodes) {
            G.visitConnectedComponent(v, nodesToSelect);
        }
        nodesToSelect.removeAll(getSelectedNodes());
        if (nodesToSelect.size() > 0)
            setSelected(nodesToSelect, true);
    }

    public JFrame getFrame() {
        return frame;
    }

    public void setFrame(JFrame frame) {
        this.frame = frame;
    }
}

// EOF
