/*
 * QuadTree.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.graph.algorithms;

import jloda.fx.util.BBoxUtils;
import jloda.util.APoint2D;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * a quad tree
 * Source: https://www.baeldung.com/java-range-search
 * Daniel Huson, 3.2021
 */
public class QuadTree {
private static final int MAX_POINTS = 3;
private final QuadTreeNode root;
private int size;

    /**
     * construct a new quad tree
     */
    public QuadTree(Region area) {
        root=new QuadTreeNode(area);
    }

    /**
     * construct a new quad tree and add all given points
     * @param points non-empty set of points
     */
    public QuadTree(Collection<APoint2D<?>> points) {
        var bbox=BBoxUtils.computeBBox(points);
        root=new QuadTreeNode(new Region(bbox[0],bbox[1],bbox[2],bbox[3]));
        for(var p:points) {
            addPoint(p);
        }
    }

    public int size() {
        return size;
    }

    public void clear() {
        size=0;
        root.points.clear();
        root.nodes.clear();
    }

    public void  addPoint(APoint2D<?> point) {
         root.addPoint(point);
         size++;
    }

    public List<APoint2D<?>> search(Region searchRegion, List<APoint2D<?>> matches) {
        if (matches == null) {
            matches = new ArrayList<>();
        }
        root.search(searchRegion,matches);
        return matches;
    }

    public List<APoint2D<?>> search(double xmin,double ymin,double xmax,double ymax, List<APoint2D<?>> matches) {
        return search(new Region(xmin,ymin,xmax,ymax),matches);
    }

    public List<APoint2D<?>> search (double dx,double dy,APoint2D<?> point, List<APoint2D<?>> matches) {
        return search(point.getX()-dx,point.getY()-dy,point.getX()+dx,point.getY()+dy,matches);
    }

    public Region getArea () {
        return root.area;
    }

    private static class QuadTreeNode {
        private final Region area;
        private final List<APoint2D<?>> points = new ArrayList<>();
        private final List<QuadTreeNode> nodes = new ArrayList<>();

         QuadTreeNode (Region area) {
            this.area = area;
        }

        boolean addPoint(APoint2D<?> point) {
            if (area.contains(point)) {
                if (this.points.size() < MAX_POINTS) {
                    this.points.add(point);
                    return true;
                } else {
                    if (nodes.size() == 0) {
                        createQuadrants();
                    }
                    return addPointToOneQuadrant(point);
                }
            }
            return false;
        }

         void search(Region searchRegion, List<APoint2D<?>> matches) {
            if (this.area.overlaps(searchRegion)) {
                for (var point : points) {
                    if (searchRegion.contains(point)) {
                        matches.add(point);
                    }
                }
                if (nodes.size() > 0) {
                    for (int i = 0; i < 4; i++) {
                       nodes.get(i).search(searchRegion, matches);
                    }
                }
            }
        }

        private boolean addPointToOneQuadrant(APoint2D<?> point) {
            boolean isPointAdded;
            for (int i = 0; i < 4; i++) {
                    isPointAdded = nodes.get(i).addPoint(point);
                if (isPointAdded)
                    return true;
            }
            return false;
        }

        private void createQuadrants() {
            Region region;
            for(var q:Region.Quadrant.values()) {
                region = area.getQuadrant(q);
                nodes.add(new QuadTreeNode(region));
            }
        }

    }
   public static class Region {
    public enum Quadrant {SW, NW, NE, SE}

       private final float xmin;
        private final float ymin;
        private final float xmax;
        private final float ymax;

        public Region(double xmin, double ymin, double xmax, double ymax) {
            this.xmin = (float)xmin;
            this.ymin = (float)ymin;
            this.xmax = (float)xmax;
            this.ymax = (float)ymax;
        }

       public boolean contains(APoint2D<?> point) {
           return point.getX() >= this.xmin
                   && point.getX() <= this.xmax
                   && point.getY() >= this.ymin
                   && point.getY() <= this.ymax;
       }

       public boolean overlaps(Region that) {
            return !(xmax <that.xmin || xmin >that.xmax || ymax <that.xmin || ymin >that.ymax);
       }

       public Region getQuadrant(Quadrant quadrantIndex) {
           float quadrantWidth = (this.xmax - this.xmin) / 2;
           float quadrantHeight = (this.ymax - this.ymin) / 2;

           // 0=SW, 1=NW, 2=NE, 3=SE
           return switch (quadrantIndex) {
               case SW -> new Region(xmin, ymin, xmin + quadrantWidth, ymin + quadrantHeight);
               case NW -> new Region(xmin, ymin + quadrantHeight, xmin + quadrantWidth, ymax);
               case NE -> new Region(xmin + quadrantWidth, ymin + quadrantHeight, xmax, ymax);
               case SE -> new Region(xmin + quadrantWidth, ymin, xmax, ymin + quadrantHeight);
           };
       }

       public float getMinX() {
           return xmin;
       }

        public float getMinY() {
           return ymin;
       }


       public float getMaxX() {
           return xmax;
       }


       public float getMaxY() {
           return ymax;
       }

       public float getWidth() {
            return xmax-xmin;
       }

       public float getHeight() {
            return ymax-ymin;
       }
   }


}
