/**
 * DrawOval.java 
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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Draw an oval
 * Daniel Huson, 2014
 */
public class DrawOval {

    public static void main(String[] args) {
        final LinkedList<Oval> list = new LinkedList<>();
        final LinkedList<Point> points = new LinkedList<>();

        final JFrame frame = new JFrame();
        frame.setSize(500, 500);
        final JPanel panel = new JPanel() {
            @Override
            public void paint(Graphics g0) {
                Graphics2D g = (Graphics2D) g0;
                super.paint(g);
                for (Oval oval : list) {
                    oval.draw(g);
                }
                g.setColor(Color.BLACK);
                for (Point point : points) {
                    g.drawRect(point.x - 1, point.y - 1, 2, 2);
                }
            }
        };
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(panel);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        panel.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                panel.repaint();
            }
        });


        points.add(new Point(105, 100));
        points.add(new Point(405, 300));
        points.add(new Point(150, 120));

        list.add(Oval.createOval(points));

        list.add(new Oval(new Point(100, 100), 50, 100, 0));


        for (float z = (float) Math.PI; z >= 0f; z -= 0.4f)
            list.add(new Oval(new Point(300, 300), 120, 10, z));

        list.add(new Oval(new Point(200, 100), 50, 100, 0));
    }

    public static Point getCenter(Collection<Point> points) {
        double x = 0;
        double y = 0;
        for (Point point : points) {
            x += point.x;
            y += point.y;
        }
        x /= points.size();
        y /= points.size();
        return new Point((int) Math.round(x), (int) Math.round(y));

    }

    public static float getAngleOfMainDirection(Collection<Point> points, Point center) {
        points = normalize(points, center);

        final Point result = new Point();

        for (Point point : points) {
            if (point.x < 0) {
                result.x += -point.x;
                result.y += -point.y;
            } else {
                result.x += point.x;
                result.y += point.y;
            }
        }
        result.x = (int) ((float) result.x / points.size());
        result.y = (int) ((float) result.y / points.size());
        return (float) Geometry.computeAngle(result);
    }

    private static Collection<Point> normalize(Collection<Point> points, Point center) {
        ArrayList<Point> result = new ArrayList<>(points.size());
        for (Point point : points) {
            result.add(new Point(point.x - center.x, point.y - center.y));
        }
        return result;
    }
}

class Oval {
    Point center;
    int width;
    int height;
    float angle;

    public Oval() {
    }

    public Oval(Point center, int width, int height, float angle) {
        this.center = center;
        this.width = width;
        this.height = height;
        this.angle = angle;
    }

    public void draw(Graphics2D gc) {
        gc.setColor(Color.BLUE);
        gc.drawRect(0, 0, 100, 100);
        if (angle != 0.0) {
            AffineTransform saveTransform = gc.getTransform();
            gc.rotate(angle, center.getX(), center.getY());
            gc.drawOval(center.x - width / 2, center.y - height / 2, width, height);
            gc.setTransform(saveTransform);
        } else
            gc.drawOval(center.x - width / 2, center.y - height / 2, width, height);
    }

    public static Oval createOval(Collection<Point> points) {
        Oval oval = new Oval();
        oval.center = DrawOval.getCenter(points);
        oval.angle = DrawOval.getAngleOfMainDirection(points, oval.center);
        oval.width = 100;
        oval.height = 50;
        System.err.println("Center: " + oval.center);
        System.err.println("Angle: " + oval.angle);
        return oval;

    }

}
