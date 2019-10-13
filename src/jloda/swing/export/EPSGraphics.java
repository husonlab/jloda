/*
 * EPSGraphics.java Copyright (C) 2019. Daniel H. Huson
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

package jloda.swing.export;

import jloda.util.Basic;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextLayout;
import java.awt.geom.*;
import java.awt.image.*;
import java.awt.image.renderable.RenderableImage;
import java.io.*;
import java.text.AttributedCharacterIterator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;


/**
 * A basic implementation of Graphics2D for output of the
 * <i>encapsulated post script</i> file type.
 *
 * @author Daniel Huson, Michael Schroeder
 */
public class EPSGraphics extends Graphics2D {

    /**
     * the writer which writes the eps document in a givenOutputStream.
     */
    private final Writer w;
    /**
     * a Map of currently supported mappings of java symbolic fontnames
     * to postscript fontnames.
     */
    private Map fonts;

    /**
     * width of the eps page.
     */
    private final int width;
    /**
     * height of the eps page
     */
    private final int height;

    /**
     * the current Font.
     */
    private Font font;
    /**
     * the current FontRenderContext
     */
    private final FontRenderContext fontRenderContext;
    /**
     * the current Color.
     */
    private Color color;
    /**
     * the current backgorund color.
     */
    private Color background;
    /**
     * the current Stroke
     */
    private BasicStroke stroke;
    /**
     * the current clipping bounds
     */
    private Shape clip;
    /**
     * the current transformation matrix
     */
    private AffineTransform tx;

    private final boolean drawTextAsOutlines;

    private static final int DRAW_SHAPE = 0;
    private static final int FILL_SHAPE = 1;
    private static final int CLIP_SHAPE = 2;

    public static final boolean FONT_OUTLINES = true;
    public static final boolean FONT_TEXT = false;

    /**
     * CONSTRUCTORS
     */

    /**
     * the eps document will be written directly to the given
     * <code>OutputStream</code>.
     * after writing to EPSGraphics is finished, {@link #finish() finish()} needs to be called in order to
     * close the BufferedWriter explicitly.
     *
     * @param width  the width of the eps document,
     * @param height the height of the eps document.
     * @param stream the <code>OutputStream</code> to write to, e.g. <code>FileOutputStream</code>
     *               or <code>ByteArrayOutputStream</code>.
     */
    public EPSGraphics(int width, int height, OutputStream stream) {

        this(width, height, stream, FONT_TEXT);
    }

    public EPSGraphics(int width, int height, OutputStream stream, boolean drawTextAsOutlines) {

        this.w = new BufferedWriter(new OutputStreamWriter(stream));
        this.width = width;
        this.height = height;
        this.clip = new Rectangle(width, height);
        this.tx = new AffineTransform();
        this.background = Color.WHITE;
        this.fontRenderContext = new FontRenderContext(null, false, true);
        this.drawTextAsOutlines = drawTextAsOutlines;

        initFonts();
        writeHeader();
    }


    /**
     * creates a new EPSGraphics with the same
     * configuration as the given EPSGraphics
     *
     * @param g the EPSGraphics to copy field values from
     */
    protected EPSGraphics(EPSGraphics g) {

        this.w = g.w;
        this.width = g.width;
        this.height = g.height;
        this.clip = g.clip;
        this.font = g.font;
        this.fontRenderContext = g.fontRenderContext;
        this.fonts = g.fonts;
        this.color = g.color;
        this.background = g.background;
        this.tx = g.tx;
        this.drawTextAsOutlines = g.drawTextAsOutlines;
        this.stroke=g.stroke;

    }

    public Graphics create() {
        return new EPSGraphics(this);
    }


    /**
     * overriden methods of <code>java.awt.Graphics</code>
     */


    /**
     * methods of <code>java.awt.Graphics</code> supported by <code>EPSGraphics</code>
     */


    public Color getColor() {
        return color;
    }

    public Font getFont() {
        return font;
    }

    /**
     * maps ranges of 256 integer rgb color values to
     * the interval between 0 and 1 before writing postscript.
     *
     * @param c the color
     */
    public void setColor(Color c) {
        this.color = c;
        float r = c.getRed() / 255.0f;
        float g = c.getGreen() / 255.0f;
        float b = c.getBlue() / 255.0f;
        writeToFile(r + " " + g + " " + b + " setrgbcolor\r\n");
    }

    /**
     * sets the current font.
     *
     * @param font the font
     */
    public void setFont(Font font) {

        if (!drawTextAsOutlines) {

            if (fonts.containsKey(font.getFontName())) {
                this.font = font;
                writeToFile("/" + fonts.get(font.getFontName()) + " findfont\r\n" +
                        font.getSize() + " scalefont\r\n" +
                        "setfont\r\n");
            } else {
                this.font = font;
                writeToFile("/" + font.getPSName() + " findfont\r\n" +
                        font.getSize() + " scalefont\r\n" +
                        "setfont\r\n");
            }
        } else {
            this.font = font;
        }
    }

    public void drawLine(int x1, int y1, int x2, int y2) {

        Shape s = new Line2D.Float(x1, y1, x2, y2);
        draw(s, DRAW_SHAPE);
    }

    public void fillRect(int ulx, int uly, int width, int height) {

        Rectangle2D rect = new Rectangle2D.Float(ulx, uly, width, height);
        draw(rect, FILL_SHAPE);
    }


    /**
     * draws an oval.
     *
     * @param x      the x-coordinate of the upper left corner of the oval's bounding box.
     * @param y      the y-coordinate of the upper left corner of the oval's bounding box.
     * @param width  the width of the oval's bounding box
     * @param height the height of the oval's bounding box
     */
    public void drawOval(int x, int y, int width, int height) {

        Ellipse2D ellipse = new Ellipse2D.Float(x, y, width, height);
        draw(ellipse, DRAW_SHAPE);

    }

    /**
     * draws a filled oval.
     * see {@link #drawOval(int x, int y, int width, int height)} for details.
     *
     * @param x      the x-coordinate of the upper left corner of the oval's bounding box.
     * @param y      the y-coordinate of the upper left corner of the oval's bounding box.
     * @param width  the width of the oval's bounding box
     * @param height the height of the oval's bounding box
     */
    public void fillOval(int x, int y, int width, int height) {

        Ellipse2D ellipse = new Ellipse2D.Float(x, y, width, height);
        draw(ellipse, FILL_SHAPE);
    }

    public void drawArc(int x, int y, int width, int height,
                        int startAngle, int arcAngle) {

        Arc2D arc = new Arc2D.Float(x, y, width, height, startAngle, arcAngle, Arc2D.OPEN);
        draw(arc, DRAW_SHAPE);

    }

    public void fillArc(int x, int y, int width, int height,
                        int startAngle, int arcAngle) {

        Arc2D arc = new Arc2D.Float(x, y, width, height, startAngle, arcAngle, Arc2D.PIE);
        draw(arc, FILL_SHAPE);

    }

    public void drawPolyline(int[] xPoints, int[] yPoints,
                             int nPoints) {
        if (0 < nPoints) {
            GeneralPath path = new GeneralPath();
            path.moveTo(xPoints[0], yPoints[0]);
            for (int i = 1; i < nPoints; i++) {
                path.lineTo(xPoints[i], yPoints[i]);
            }
            draw(path, DRAW_SHAPE);
        }
    }

    public void drawPolygon(int[] xPoints, int[] yPoints,
                            int nPoints) {
        if (nPoints > 1) {
            Polygon poly = new Polygon(xPoints, yPoints, nPoints);
            draw(poly, DRAW_SHAPE);
        }
    }

    public void fillPolygon(int[] xPoints, int[] yPoints,
                            int nPoints) {
        if (0 < nPoints) {
            GeneralPath path = new GeneralPath();
            path.moveTo(xPoints[0], yPoints[0]);
            for (int i = 1; i < nPoints; i++) {
                path.lineTo(xPoints[i], yPoints[i]);
            }
            draw(path, FILL_SHAPE);
        }
    }

    public void drawRoundRect(int x, int y, int width, int height,
                              int arcWidth, int arcHeight) {
        notSupported("drawRoundRect(int x, int y, int width, int height,int arcWidth, int arcHeight)");
    }

    public void fillRoundRect(int x, int y, int width, int height,
                              int arcWidth, int arcHeight) {
        notSupported("fillRoundRect(int x, int y, int width, int height,int arcWidth, int arcHeight)");
    }

    public void translate(int x, int y) {

        this.tx.concatenate(AffineTransform.getTranslateInstance(x, y));
    }

    public void setClip(int ulx, int uly, int width, int height) {
        clip = new Rectangle(ulx, uly, width, height);

        int[] p = {ulx, uly + height};
        toPsCoords(p);
        writeToFile(ulx + " " + uly + " " + width + " " + height + " rectclip\r\n");

    }

    public Shape getClip() {
        return clip;
    }

    public Rectangle getClipBounds() {
        if (clip == null) return null;
        return clip.getBounds();
    }

    public void setClip(Shape clip) {
        draw(clip, CLIP_SHAPE);
    }

    /**
     * methods of <code>java.awt.Graphics</code> NOT supported by <code>EPSGraphics</code>
     */


    public void setPaintMode() {
        notSupported("setPaintMode()");
    }

    public void setXORMode(Color c1) {
        notSupported("setXORMode(Color c1)");
    }


    public FontMetrics getFontMetrics(Font f) {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        return g.getFontMetrics(f);
    }

    public void clipRect(int x, int y, int width, int height) {
        Rectangle2D rect = new Rectangle2D.Float(x, y, width, height);
        draw(rect, CLIP_SHAPE);
    }

    public void copyArea(int x, int y, int width, int height,
                         int dx, int dy) {
        notSupported("copyArea(int x, int y, int width, int height,int dx, int dy)");
    }

    public void clearRect(int x, int y, int width, int height) {
        float[] bg = background.getColorComponents(null);
        float[] c = color.getColorComponents(null);
        writeToFile(bg[0] + " " + bg[1] + " " + bg[2] + " setcolor\r\n");
        Rectangle2D rect = new Rectangle2D.Float(x, y, width, height);
        draw(rect, FILL_SHAPE);
        writeToFile(c[0] + " " + c[1] + " " + c[2] + " setcolor\r\n");
    }

    public Paint getPaint() {
        notSupported("getPaint()");
        return null;
    }

    public Composite getComposite() {
        notSupported("getComposite()");
        return null;
    }

    public void setBackground(Color color) {
        this.background = color;
    }

    public Color getBackground() {
        return this.background;
    }

    public Stroke getStroke() {
        return stroke;
    }

    public void clip(Shape s) {
        draw(s, CLIP_SHAPE);
    }

    public FontRenderContext getFontRenderContext() {

        return this.fontRenderContext;
    }


    /**
     * overriden methods of <code>java.awt.Graphics2D</code>
     */


    /**
     * methods of <code>java.awt.Graphics2D</code> supported by <code>EPSGraphics</code>
     */

    /**
     * draws a string at (<code>x,y</code>).
     * since the postscript user space is flipped horizontically in order
     * to draw in java coordinates, the string itself has to be flipped again.
     *
     * @param str the string to be drawn.
     * @param x   the x-coordinate of the lower left corner
     * @param y   the y-coordinate of the lower left corner
     */

    public void drawString(String str, int x, int y) {

        drawString(str, (float) x, (float) y);
    }

    public void drawString(String str, float x, float y) {

        if (drawTextAsOutlines) {
            //System.err.println("rendering outlines: font="+font);
            TextLayout layout = new TextLayout(str, font, fontRenderContext);
            Shape s = layout.getOutline(AffineTransform.getTranslateInstance(x, y));
            fill(s);
        } else if (!drawTextAsOutlines) {
            //System.err.println("rendering text");
            AffineTransform m = getTransform();
            m.rotate(Math.PI);
            double[] gm = new double[6];
            m.getMatrix(gm);
            writeToFile("gsave\r\n");
            writeToFile("[" + -gm[0] + " " + gm[1] + " " + gm[2] + " " + -gm[3] + " " + gm[4] + " " + (height - gm[5]) + "] concat\r\n");
            writeToFile(x + " " + -y + " m\r\n");
            writeToFile("(" + str + ") show\r\n");
            writeToFile("grestore\r\n");
        }
    }

    public void translate(double tx, double ty) {
        this.tx.concatenate(AffineTransform.getTranslateInstance(tx, ty));
    }

    public void rotate(double theta) {
        this.tx.concatenate(AffineTransform.getRotateInstance(theta));
    }

    public void rotate(double theta, double x, double y) {
        this.tx.concatenate(AffineTransform.getRotateInstance(theta, x, y));
    }

    public void scale(double sx, double sy) {
        this.tx.concatenate(AffineTransform.getScaleInstance(sx, sy));
    }

    public void shear(double shx, double shy) {
        this.tx.concatenate(AffineTransform.getShearInstance(shx, shy));
    }

    public void transform(AffineTransform Tx) {
        this.tx.concatenate(Tx);

    }

    public void setTransform(AffineTransform Tx) {
        this.tx = new AffineTransform(Tx);

    }

    public AffineTransform getTransform() {
        return new AffineTransform(this.tx);
    }

    /**
     * sets the current linewidth.
     *
     * @param s a stroke object to get the linewidth from.
     */
    public void setStroke(Stroke s) {

        this.stroke = (BasicStroke) s;

        // endcap
        int endCap = stroke.getEndCap();
        int psEndCap = -1;
        switch (endCap) {
            case BasicStroke.CAP_BUTT:
                psEndCap = 0;
                break;
            case BasicStroke.CAP_ROUND:
                psEndCap = 1;
                break;
            case BasicStroke.CAP_SQUARE:
                psEndCap = 2;
                break;
        }
        if (-1 != psEndCap) writeToFile(psEndCap + " setlinecap\r\n");

        // line join
        int lineJoin = stroke.getLineJoin();
        int psLineJoin = -1;
        switch (lineJoin) {
            case BasicStroke.JOIN_BEVEL:
            case BasicStroke.JOIN_MITER:
            case BasicStroke.JOIN_ROUND:
                psLineJoin = 1;
        }
        if (-1 != psLineJoin) writeToFile(psLineJoin + " setlinejoin\r\n");
        if (1 <= stroke.getMiterLimit()) writeToFile(stroke.getMiterLimit() + " setmiterlimit\r\n");
        writeToFile(stroke.getLineWidth() + " setlinewidth\r\n");
    }


    /**
     * methods of <code>java.awt.Graphics2D</code> NOT supported by <code>EPSGraphics</code>
     */

    public void dispose() {
        //notSupported("dispose()");
    }

    public void draw(Shape s) {

        draw(s, DRAW_SHAPE);
    }

    public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
        AffineTransform at = getTransform();
        transform(xform);
        boolean st = drawImage(img, 0, 0, obs);
        setTransform(at);
        return st;
    }


    public void drawImage(BufferedImage img,
                          BufferedImageOp op,
                          int x,
                          int y) {
        notSupported("drawImage(BufferedImage img,BufferedImageOp op,int x,int y)");
    }

    public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
        Hashtable properties = new Hashtable();
        String[] names = img.getPropertyNames();
        for (String name : names) {
            properties.put(name, img.getProperty(name));
        }

        ColorModel cm = img.getColorModel();
        WritableRaster wr = img.copyData(null);
        BufferedImage img1 = new BufferedImage(cm, wr, cm.isAlphaPremultiplied(), properties);
        AffineTransform at = AffineTransform.getTranslateInstance(img.getMinX(), img.getMinY());
        at.preConcatenate(xform);
        drawImage(img1, at, null);
    }


    public void drawRenderableImage(RenderableImage img,
                                    AffineTransform xform) {
        notSupported("drawRenderableImage(RenderableImage img,AffineTransform xform)");
    }

    public void drawString(AttributedCharacterIterator iterator,
                           int x, int y) {
        notSupported("drawString(AttributedCharacterIterator iterator,int x, int y)");
    }

    public boolean drawImage(Image img, int x, int y,
                             ImageObserver observer) {
        return drawImage(img, x, y, Color.WHITE, observer);
    }

    public boolean drawImage(Image img, int x, int y,
                             Color bgcolor,
                             ImageObserver observer) {
        int width = img.getWidth(observer);
        int height = img.getHeight(observer);
        return drawImage(img, x, y, width, height, bgcolor, observer);
    }

    public boolean drawImage(Image img, int x, int y,
                             int width, int height,
                             Color bgcolor,
                             ImageObserver observer) {
        return drawImage(img, x, y, x + width, y + height, 0, 0, width, height, observer);
    }

    public boolean drawImage(Image img, int x, int y,
                             int width, int height,
                             ImageObserver observer) {
        return drawImage(img, x, y, width, height, Color.WHITE, observer);
    }


    public boolean drawImage(Image img,
                             int dx1, int dy1, int dx2, int dy2,
                             int sx1, int sy1, int sx2, int sy2,
                             ImageObserver observer) {
        return drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, Color.WHITE, observer);
    }

    public boolean drawImage(Image img,
                             int dx1, int dy1, int dx2, int dy2,
                             int sx1, int sy1, int sx2, int sy2,
                             Color bgcolor,
                             ImageObserver observer) {
        notSupported("drawImage");
        return false;
    }

    public void drawString(AttributedCharacterIterator iterator,
                           float x, float y) {
        notSupported("drawString(AttributedCharacterIterator iterator,float x, float y)");
    }

    public void drawGlyphVector(GlyphVector g, float x, float y) {
        notSupported("drawGlyphVector(GlyphVector g, float x, float y)");
    }

    public void fill(Shape s) {
        draw(s, FILL_SHAPE);
    }

    public void draw(Shape s, int operator) {
        Shape st = tx.createTransformedShape(s);

        PathIterator it = st.getPathIterator(null);

        writeToFile("newpath\r\n");
        float[] pts = new float[6];
        float p0x = 0f;
        float p0y = 0f;

        while (!it.isDone()) {

            int type = it.currentSegment(pts);

            float p1x = pts[0];
            float p1y = height - pts[1];
            float p2x = pts[2];
            float p2y = height - pts[3];
            float p3x = pts[4];
            float p3y = height - pts[5];

            switch (type) {

                case PathIterator.SEG_MOVETO:

                    writeToFile(p1x + " " + p1y + " m\r\n");
                    p0x = p1x;
                    p0y = p1y;
                    break;

                case PathIterator.SEG_LINETO:

                    writeToFile(p1x + " " + p1y + " l\r\n");
                    p0x = p1x;
                    p0y = p1y;
                    break;

                case PathIterator.SEG_CUBICTO:

                    writeToFile(p1x + " " + p1y + " " + p2x + " " + p2y + " " + p3x + " " + p3y + " c\r\n");
                    p0x = p3x;
                    p0y = p3y;
                    break;

                case PathIterator.SEG_QUADTO: // @todo


                    float c1x = p0x + 2f / 3f * (p1x - p0x);
                    float c1y = p0y + 2f / 3f * (p1y - p0y);
                    float c2x = p1x + 1f / 3f * (p2x - p1x);
                    float c2y = p1y + 1f / 3f * (p2y - p1y);

                    writeToFile(c1x + " " + c1y + " " + c2x + " " + c2y + " " + p2x + " " + p2y + " c\r\n");
                    p0x = p2x;
                    p0y = p2y;

                    break;

                case PathIterator.SEG_CLOSE:
                    writeToFile("closepath\r\n");
                    break;

            }

            it.next();

        }
        switch (operator) {
            case DRAW_SHAPE:
                writeToFile("stroke\r\n");
                break;
            case FILL_SHAPE:
                writeToFile("fill\r\n");
                break;
            case CLIP_SHAPE:
                writeToFile("clip\r\n");
                break;
        }
    }

    public boolean hit(Rectangle rect,
                       Shape s,
                       boolean onStroke) {
        return s.intersects(rect);
    }

    public GraphicsConfiguration getDeviceConfiguration() {
        GraphicsConfiguration gc = null;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gds = ge.getScreenDevices();
        for (GraphicsDevice gd : gds) {
            GraphicsConfiguration[] gcs = gd.getConfigurations();
            if (gcs.length > 0) {
                return gcs[0];
            }
        }
        return gc;
    }

    public void setComposite(Composite comp) {
        notSupported("setComposite(Composite comp)");
    }

    public void setPaint(Paint paint) {
        notSupported("setPaint(Paint paint)");
    }

    public void setRenderingHint(RenderingHints.Key hintKey, Object hintValue) {
        notSupported("setRenderingHint(RenderingHints.Key hintKey, Object hintValue)");
    }

    public Object getRenderingHint(RenderingHints.Key hintKey) {
        notSupported("getRenderingHint(RenderingHints.Key hintKey)");
        return null;
    }

    public void setRenderingHints(Map hints) {
        notSupported("setRenderingHints(Map hints)");
    }

    public void addRenderingHints(Map hints) {
        notSupported("addRenderingHints(Map hints)");
    }

    public RenderingHints getRenderingHints() {
        notSupported("RenderingHints getRenderingHints()");
        return null;
    }


    /**
     * additional methods
     */

    private void writeToFile(String s) {
        try {
            w.write(s);
        } catch (IOException e) {
            Basic.caught(e);
        }
    }

    private void toPsCoords(int[] p) {
        p[1] = height - p[1];
    }


    private void psUserPathStart(Shape s) {

        Rectangle2D.Float bounds = (Rectangle2D.Float) s.getBounds2D();
        //ul = tx.transform(bounds.getLocation(),ul);

        float llx = bounds.x;
        float lly = height - (bounds.y + bounds.height);
        float urx = bounds.x + bounds.width;
        float ury = height - bounds.y;

        writeToFile("{" + llx + " " + lly + " " + urx + " " + ury + " setbbox\r\n");

    }


    /**
     * write the eps header.
     */
    private void writeHeader() {
        writeToFile("%!PS-Adobe-3.0 EPSF-3.0\r\n" +
                "%%BoundingBox: 0 0 " + width + " " + height + "\r\n" +
                "%%Creator: jloda\r\n" +
                "%%EndComments\r\n" +
                "/c {curveto} bind def\r\n" +
                "/m {moveto} bind def\r\n" +
                "/l {lineto} bind def\r\n");
    }

    /**
     * write the eps trailer
     */
    private void writeTrailer() {
        writeToFile("showpage\r\n%%EOF");
    }

    /**
     * finish writing the document.
     * this method needs to be called after a Component has
     * painted on <code>EPSGraphics</code>.
     */
    public void finish() {
        writeTrailer();
        try {
            w.close();
        } catch (IOException e) {
            Basic.caught(e);
        }
    }

    /**
     * initialize a mappping of symbolic java fontnames
     * to postscript fontnames
     */
    private void initFonts() {

        /*String defaultFont = "Default";

        fonts = new HashMap();
        fonts.put(defaultFont + ".plain","SansSerif");
        fonts.put(defaultFont + ".italic","SansSerifOblique");
        fonts.put(defaultFont + ".bold","SansSerifBold");

        this.font = new Font("Default",Font.PLAIN,12); */

        Font defaultPlain = new Font("Default", Font.PLAIN, 10);

        fonts = new HashMap();
        fonts.put("Default.plain", "SansSerif");
        fonts.put("Default.italic", "SansSerifOblique");
        fonts.put("Default.bold", "SansSerifBold");

        this.font = defaultPlain;
    }

    /**
     * add a mapping of a symbolic java fontname
     * to a postscript fontname
     *
     * @param fontName java symbolic fontname
     * @param psName   postscript fontname
     */
    public void addFont(String fontName, String psName) {
        fonts.put(fontName, psName);
    }

    private void notSupported(String methodName) {
        System.err.println("Method not supported by " + this.getClass().getName() + ": " + methodName);
    }

}
