/**
 * RTree.java 
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

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

/**
 * two-dimensional R-tree
 * Daniel Huson, 7.2012
 */
public class RTree<T> {
    final static private int MAX_NUMBER_CHILDREN = 3;
    private RNode root;
    private int size;
    private RNode head;
    private RNode tail;
    private RNode lastHit;
    private int numberOfComparisons = 0;

    /**
     * constructor
     */
    public RTree() {
        root = null;
        size = 0;
        lastHit = null;
    }

    /**
     * add a rectangle and associated data to the RTree
     *
     * @param rect
     * @param data
     */
    public void add(Rectangle2D rect, T data) {
        if (root == null) {
            root = new RNode(rect);
        }
        RNode v = new RNode(rect, data);
        if (head == null)
            head = v;
        if (tail != null)
            tail.setNext(v);
        tail = v;
        RNode split = addBelowRec(root, v);
        if (split != null) {
            Rectangle2D newRect = (Rectangle2D) root.getRect().clone();
            newRect.add(rect);
            RNode newRoot = new RNode(newRect);
            newRoot.addChild(root);
            newRoot.addChild(split);
            root = newRoot;
        }
        size++;
    }

    /**
     * add data as close as possible to the given location without overlapping an data already contained in the RTree
     *
     * @param location
     * @param dimension
     * @param data
     */
    public Point addCloseTo(int seed, Point location, int minDx, int minDy, boolean left, Dimension dimension, T data) {
        int x = location.x;
        int y = location.y;


        Rectangle bbox = new Rectangle();
        bbox.setSize(dimension);

        if (size() == 0) {
            if (!overlaps(bbox)) {
                bbox.setLocation(x, y);
                add(bbox, data);
                return new Point(x, y);
            }
        }

        Random rand = new Random(seed);
        boolean upDown = rand.nextBoolean();
        boolean leftRight = rand.nextBoolean();

        int direction = 3;
        for (int k = 1; true; k++) { // number steps in a direction
            for (int i = 0; i < 2; i++) {  // two different directions
                if (direction == 3)
                    direction = 0;
                else
                    direction++;
                for (int j = 0; j <= k; j++) {  // the steps in the direction
                    switch (direction) {
                        case 0:
                            if (left)
                                x = location.x + (leftRight ? 1 : -1) * (minDx + k * 2);
                            else
                                x = location.x - (leftRight ? 1 : -1) * (minDx + k * 2);
                            break;
                        case 1:
                            if (!left)
                                y = location.y + (upDown ? 1 : -1) * (minDy + k * 2);
                            else
                                y = location.y - (upDown ? 1 : -1) * (minDy + k * 2);
                            break;
                        case 2:
                            if (!left)
                                y = location.y - (upDown ? 1 : -1) * (minDy + k * 2);
                            else
                                y = location.y + (upDown ? 1 : -1) * (minDy - k * 2);
                            break;
                    }
                    bbox.setLocation((x >= location.x ? x : x - dimension.width), y);
                    if (!overlaps(bbox)) {
                        add(bbox, data);
                        return new Point(bbox.x, bbox.y);
                    }
                }
            }
        }
    }

    /**
     * add the node v below the given node
     *
     * @param parent
     * @param v
     */
    private RNode addBelowRec(RNode parent, RNode v) {
        parent.rect.add(v.rect);
        if (parent.data != null)
            System.err.println("Entered addBelowRec with leaf node: " + parent);
        RNode child = parent.chooseOverlappingInternalChild(v);
        if (child != null) {
            return addBelowRec(child, v);
        } else {
            if (parent.addChild(v)) {
                return null;
            } else {
                return parent.split(v);
            }
        }
    }

    /**
     * get a hit data item, if one exists
     *
     * @param rect
     * @return data item
     */
    public T getHitData(Rectangle2D rect) {
        numberOfComparisons = 0;
        if (root == null)
            return null;
        if (lastHit != null && rect.intersects(lastHit.getRect())) {
            numberOfComparisons++;
            return lastHit.getData();
        }
        RNode node = getHitRec(root, rect);
        if (node == null)
            return null;
        else {
            lastHit = node;
            return node.getData();
        }
    }

    /**
     * determines whether given rect overlaps with any of the contained rectangles
     *
     * @param rect
     * @return true, if an overlap was detected
     */
    public boolean overlaps(Rectangle2D rect) {
        return getHitData(rect) != null;
    }

    public int getNumberOfComparisonsUsedOnLastQuery() {
        return numberOfComparisons;
    }

    /**
     * recursively do the work
     *
     * @param node
     * @param rect
     * @return hit data item or null
     */
    private RNode getHitRec(RNode node, Rectangle2D rect) {
        if (node.rect.intersects(rect)) {
            numberOfComparisons++;
            if (node.data != null)
                return node;
            else {
                for (int i = 0; i < node.getNumberOfChildren(); i++) {
                    RNode result = getHitRec(node.getChild(i), rect);
                    if (result != null)
                        return result;
                }
            }
        }
        return null;
    }

    /**
     * erase
     */
    public void clear() {
        root = null;
        size = 0;
        lastHit = null;
        head = null;
        tail = null;
    }

    /**
     * size
     *
     * @return size
     */
    public int size() {
        return size;
    }

    /**
     * gets the bounding box
     *
     * @param bbox bounding box
     */
    public void getBoundingBox(Rectangle2D bbox) {
        if (root == null)
            bbox.setRect(0, 0, 0, 0);
        else
            bbox.setRect(root.rect);
    }

    public Iterator<Pair<Rectangle2D, T>> iterator() {
        return new Iterator<Pair<Rectangle2D, T>>() {
            RNode node = head;

            public boolean hasNext() {
                return node != null;
            }

            public Pair<Rectangle2D, T> next() {
                if (node == null)
                    return null;
                Pair<Rectangle2D, T> result = new Pair<>(node.getRect(), node.getData());
                node = node.getNext();
                return result;
            }

            public void remove() {
            }
        };
    }

    public void draw(Graphics gc) {
        if (root != null)
            drawRec(root, gc, 1);
    }

    private void drawRec(RNode v, Graphics gc, int depth) {
        for (int i = 0; i < v.getNumberOfChildren(); i++)
            drawRec(v.getChild(i), gc, depth + 1);
        if (v.isLeaf()) {
            gc.setColor(new Color(0, 255, 0, 140));
            gc.drawString(v.getData().toString() + " (@ " + depth + ")", (int) v.getRect().getX(), (int) v.getRect().getY());
            ((Graphics2D) gc).draw(v.getRect());
        } else {
            Random random = new Random(depth);
            gc.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255), 140));
            Rectangle2D rect = (Rectangle2D) v.getRect().clone();
            rect.setRect(rect.getX() - depth, rect.getY() - depth, rect.getWidth() + 2 * depth, rect.getHeight() + 2 * depth);
            gc.drawString("" + depth, (int) v.getRect().getX() + 10 * depth, (int) v.getRect().getY());
            ((Graphics2D) gc).draw(rect);

        }
    }

    public abstract class RTreeVisitor {
        public void visit(Rectangle2D rect, T data) {
        }
    }

    /**
     * node in Rtree
     */
    private class RNode {
        private Rectangle2D rect;
        private final RNode[] children;
        private RNode next;
        private int numberOfChildren;
        private T data;

        RNode(Rectangle2D rect) {
            this.rect = (Rectangle2D) rect.clone();
            children = (RNode[]) Array.newInstance(RNode.class, MAX_NUMBER_CHILDREN);
        }

        RNode(Rectangle2D rect, T data) {
            this.rect = (Rectangle2D) rect.clone();
            this.data = data;
            children = null;
        }

        int getNumberOfChildren() {
            return numberOfChildren;
        }

        Rectangle2D getRect() {
            return rect;
        }

        T getData() {
            return data;
        }

        RNode getChild(int i) {
            return children[i];
        }

        boolean addChild(RNode node) {
            if (numberOfChildren < MAX_NUMBER_CHILDREN) {
                if (rect == null)
                    rect = (Rectangle2D) node.rect.clone();
                else
                    rect.add(node.rect);
                children[numberOfChildren++] = node;
                return true;
            } else
                return false;
        }

        RNode chooseOverlappingInternalChild(RNode node) {
            if (numberOfChildren == 0)
                return null;
            double bestArea = Double.MAX_VALUE;
            int bestI = -1;
            for (int i = 0; i < numberOfChildren; i++) {
                if (children[i].numberOfChildren > 0) {
                    double area = computeAreaOfUnion(children[i].rect, node.rect);
                    if (area < bestArea) {
                        bestArea = area;
                        bestI = i;
                    }
                }
            }
            if (bestI >= 0)
                return children[bestI];
            else
                return null;
        }

        /**
         * split a node and return the part to be reinserted below parent node
         *
         * @return split off part
         */
        RNode split(RNode v) {
            if (children == null)
                System.err.println("Splitting leaf: " + this);

            RNode[] all = Arrays.copyOf(children, numberOfChildren + 1);
            all[all.length - 1] = v;

            int worstI = -1;
            int worstJ = -1;
            double worstArea = -1;

            for (int i = 0; i < all.length; i++) {
                for (int j = i + 1; j < all.length; j++) {
                    double area = computeAreaOfUnion(all[i].rect, all[j].rect);
                    if (area > worstArea) {
                        worstArea = area;
                        worstI = i;
                        worstJ = j;
                    }
                }
            }
            RNode a = this;
            a.clearChildren();
            a.setRect(all[worstI].getRect());
            a.addChild(all[worstI]);

            RNode b = new RNode(all[worstJ].getRect());
            b.addChild(all[worstJ]);

            for (int i = 0; i < all.length; i++) {
                if (i != worstI && i != worstJ) {
                    double aArea = computeAreaOfUnion(a.rect, all[i].rect);
                    double bArea = computeAreaOfUnion(b.rect, all[i].rect);
                    if (aArea <= bArea)
                        a.addChild(all[i]);
                    else
                        b.addChild(all[i]);
                }
            }
            return b;
        }

        boolean isLeaf() {
            return data != null;
        }

        void clearChildren() {
            for (int i = 0; i < numberOfChildren; i++)
                children[i] = null;
            numberOfChildren = 0;
        }

        void setRect(Rectangle2D rect) {
            this.rect = (Rectangle2D) rect.clone();
        }

        double computeAreaOfUnion(Rectangle2D rect1, Rectangle2D rect2) {
            return (Math.max(rect1.getMaxX(), rect2.getMaxX()) - Math.min(rect1.getMinX(), rect2.getMinX()))
                    * (Math.max(rect1.getMaxY(), rect2.getMaxY()) - Math.min(rect1.getMinY(), rect2.getMinY()));
        }

        RNode getNext() {
            return next;
        }

        void setNext(RNode next) {
            this.next = next;
        }
    }

    public static void main(String[] args) {
        final RTree<Integer> rtree = new RTree<>();

        int numberOfComparisons = 0;
        int numberOfTries = 0;

        Random random = new Random(666);

        for (int i = 0; i < 50; i++) {
            int x = random.nextInt(1000);
            int y = random.nextInt(800);
            int width = random.nextInt(1000 - x);
            int height = random.nextInt(Math.min(64, 800 - y));
            Rectangle2D rect = new Rectangle(x, y, width, height);
            if (rtree.getHitData(rect) == null) {
                rtree.add(rect, i);
            } else i--;
            numberOfComparisons += rtree.numberOfComparisons;
            numberOfTries++;
            // System.err.println("getHitData comparisons: "+rtree.numberOfComparisons);
        }

        System.err.println("Average number of comparisons: " + ((float) numberOfComparisons / (float) numberOfTries));

        JPanel panel = new JPanel() {
            public void paint(Graphics graphics) {
                super.paint(graphics);
                rtree.draw(graphics);
            }
        };
        JFrame frame = new JFrame();
        frame.setSize(1100, 900);
        frame.add(panel);
        frame.setVisible(true);
    }
}
