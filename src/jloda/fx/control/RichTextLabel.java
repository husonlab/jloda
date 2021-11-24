/*
 * RichTextLabel.java Copyright (C) 2021. Daniel H. Huson
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

package jloda.fx.control;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.*;
import jloda.fx.util.BasicFX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.Executors;

/**
 * A simple RichTextLabel. A number of html and html-like tags are interpreted.
 * The text can optionally be enclosed in \<html\> \</html\> tags.
 * List of tags:
 * \<i\> and \</i\> - italic
 * \<b\>  and \</b\> - bold
 * \<u\> and \</u\> - underline
 * \<a\> and \</a\> - strike through
 * \<sup\> and \</sup\> - super script
 * \<sub\> and \</sub\> - sub script
 * \<c "color"\> and \</c\> - text color
 * \<font "name"\> and \</font\> - font
 * \<size "number"\> and \</size\> - font size
 * All errors in tags are silently ignored.
 * <p>
 * Daniel Huson, 6.2020
 */
public class RichTextLabel extends TextFlow {
    public final static Font DEFAULT_FONT = Font.font("Arial", 14);
    private static final Map<String, Image> file2image = new HashMap<>();

    private final ObjectProperty<Font> font = new SimpleObjectProperty<>(DEFAULT_FONT);
    private final StringProperty text = new SimpleStringProperty();
    private final BooleanProperty requireHTMLTag = new SimpleBooleanProperty(false);
    private final ObjectProperty<Paint> textFill = new SimpleObjectProperty<>(Color.BLACK);
    private final ObjectProperty<Node> graphic = new SimpleObjectProperty<>(null);
    private final DoubleProperty scale = new SimpleDoubleProperty(1.0);

    private final ObjectProperty<ContentDisplay> contentDisplay = new SimpleObjectProperty<>(ContentDisplay.TOP);

    /**
     * constructor
     */
    public RichTextLabel() {
        this("");
    }

    /**
     * constructor
     *
     * @param text text
     */
    public RichTextLabel(String text) {
        setMaxWidth(Control.USE_PREF_SIZE);
        setMaxHeight(Control.USE_PREF_SIZE);

        textProperty().addListener(c -> update());
        fontProperty().addListener(c -> update());
        textFillProperty().addListener(c -> update());
        requireHTMLTagProperty().addListener(c -> update());
        graphicProperty().addListener(c -> update());
        contentDisplayProperty().addListener(c -> update());

        scale.addListener((v, o, n) -> {
            if (n.doubleValue() > 0)
                update();
            else
                Platform.runLater(() -> setScale(0.001));
        });
        setText(text);
    }

    /**
     * copy constructor. Copies everything except for the graphics node, if set
     *
     * @param that label to be copied.
     */
    public RichTextLabel(RichTextLabel that) {
        this(that.getText());
        setFont(that.getFont());
        setTextFill(that.getTextFill());
        setRequireHTMLTag(that.isRequireHTMLTag());
        setContentDisplay(that.getContentDisplay());
    }

    public String getText() {
        return text.get().replaceAll("\\n", "<n>");
    }

    public StringProperty textProperty() {
        return text;
    }

    public void setText(String text) {
        this.text.set(text);
    }

    public Font getFont() {
        return font.get();
    }

    public ObjectProperty<Font> fontProperty() {
        return font;
    }

    public void setFont(Font font) {
        this.font.set(font);
    }

    public Paint getTextFill() {
        return textFill.get();
    }

    public ObjectProperty<Paint> textFillProperty() {
        return textFill;
    }

    public void setTextFill(Paint textFill) {
        this.textFill.set(textFill);
    }

    public boolean isRequireHTMLTag() {
        return requireHTMLTag.get();
    }

    public BooleanProperty requireHTMLTagProperty() {
        return requireHTMLTag;
    }

    public void setRequireHTMLTag(boolean requireHTMLTag) {
        this.requireHTMLTag.set(requireHTMLTag);
    }

    public Node getGraphic() {
        return graphic.get();
    }

    public ObjectProperty<Node> graphicProperty() {
        return graphic;
    }

    public void setGraphic(Node graphic) {
        this.graphic.set(graphic);
    }

    public ContentDisplay getContentDisplay() {
        return contentDisplay.get();
    }

    public ObjectProperty<ContentDisplay> contentDisplayProperty() {
        return contentDisplay;
    }

    public void setContentDisplay(ContentDisplay contentDisplay) {
        this.contentDisplay.set(contentDisplay);
    }

    public String getRawText() {
        return getRawText(getText());
    }

    public static String getRawText(String text) {
        while (text.contains("<")) {
            int a = text.indexOf("<");
            int b = text.indexOf(">", a);
            if (b != -1)
                text = text.substring(0, a) + text.substring(b + 1);
        }
        return text.replaceAll("\\s+", " ");
    }

    public static String getSupportedHTMLTags() {
        return "<i>italics</i>, " +
               "<b>bold</b>, " +
               "<sup>super-script</sup>, " +
               "<sub>sub-script</sub>, " +
               "<u>underline</u>, " +
               "<a>strike-through</a>, " +
               "<br>new-line, " +
               "<font \"name\">font-name</font>, " +
               "<size \"value\">font-size</size>, " +
               "<c \"value\">font-color</c>," +
               "<img src=\"url\" alt=\"text\" width=\"value\" height=\"value\"> adds an image";
    }

    @Override
    public String toString() {
        return getRawText();
    }

    private void update() {
        getChildren().clear();

        setText(getText().replaceAll("'", "\""));

        if (getGraphic() != null && (getContentDisplay() != ContentDisplay.TOP || getContentDisplay() != ContentDisplay.LEFT))
            getChildren().add(getGraphic());

        if (getText().length() > 0 && getContentDisplay() != ContentDisplay.GRAPHIC_ONLY) {
            final ArrayList<Event> events = new ArrayList<>();
            {
                final Event event = Event.getEventAtPos(getText(), 0);
                if (event != null && event.change().equals(Event.Change.htmlStart)) {
                    events.add(event);
                } else {
                    if (isRequireHTMLTag()) { // require leading HTML tag, but none found, return non-styled text
                        final Text text = new Text(getText());
                        if (getScale() == 1.0)
                            text.setFont(getFont());
                        else
                            text.setFont(new Font(getFont().getName(), getScale() * getFont().getSize()));

                        if (getTextFill() != Color.BLACK)
                            text.setFill(getTextFill());
                        getChildren().add(text);
                        return;
                    } else
                        events.add(new Event(Event.Change.htmlStart, 0, 0, null));
                }
            }

            for (int pos = 0; pos < getText().length(); pos++) {
                final Event event = Event.getEventAtPos(getText(), pos);
                if (event != null) {
                    events.add(event);
                    if (event.change().equals(Event.Change.htmlEnd))
                        break;
                }
            }
            if (events.size() <= 1 || !events.get(events.size() - 1).change().equals(Event.Change.htmlEnd))
                events.add(new Event(Event.Change.htmlEnd, getText().length(), getText().length(), null));

            var offset = 0.0;
            var currentFont = getFont();
            var fontSize = currentFont.getSize();
            var textFill = getTextFill();

            final Map<String, Boolean> active = new HashMap<>();

            final Stack<Font> fontStack = new Stack<>();
            final Stack<Double> fontSizeStack = new Stack<>();
            final Stack<Paint> colorStack = new Stack<>();

            var segmentStart = events.get(0).segmentStart();

            for (var i = 1; i < events.size(); i++) {
                final Event event = events.get(i);

                if (event.pos() > segmentStart) {
                    final Text textItem = new Text(getText().substring(segmentStart, event.pos()));
                    if (textFill != Color.BLACK)
                        textItem.setFill(textFill);

                    final FontWeight weight;
                    final Boolean bold = active.get("bold");
                    if (bold == null)
                        weight = BasicFX.getWeight(currentFont);
                    else if (bold)
                        weight = FontWeight.BOLD;
                    else
                        weight = FontWeight.NORMAL;

                    final FontPosture posture;
                    final Boolean italic = active.get("italic");
                    if (italic == null)
                        posture = BasicFX.getPosture(currentFont);
                    else if (italic)
                        posture = FontPosture.ITALIC;
                    else
                        posture = FontPosture.REGULAR;

                    textItem.setFont(Font.font(currentFont.getFamily(), weight, posture, getScale() * fontSize));

                    final Boolean strike = active.get("strike");
                    if (strike != null)
                        textItem.setStrikethrough(strike);
                    final Boolean underline = active.get("underline");
                    if (underline != null)
                        textItem.setUnderline(underline);

                    if (offset != 0)
                        textItem.setTranslateY(offset);

                    getChildren().add(textItem);
                }

                if (event.getChangeType().equals("fontFamily")) {
                    if (event.isStart()) {
                        String family = event.argument();
                        if (!Font.getFamilies().contains(family)) {
                            family = DEFAULT_FONT.getFamily();
                        }
                        if (family != null) {
                            fontStack.push(currentFont);
                            currentFont = Font.font(family, currentFont.getSize());
                        }
                    } else {
                        if (fontStack.size() > 0)
                            currentFont = fontStack.pop();
                    }
                }
                if (event.getChangeType().equals("fontSize")) {
                    if (event.isStart()) {
                        final String argument = event.argument();
                        if (argument != null) {
                            fontSizeStack.push(currentFont.getSize());
                            try {
                                fontSize = Double.parseDouble(argument);
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    } else if (fontSizeStack.size() > 0) {
                        fontSize = fontSizeStack.pop();
                    }

                }
                if (event.getChangeType().equals("color")) {
                    if (event.isStart()) {
                        final String argument = event.argument();
                        if (argument != null) {
                            try {
                                final Color newColor = Color.valueOf(argument);
                                colorStack.push(textFill);
                                textFill = newColor;
                            } catch (IllegalArgumentException ignored) {
                            }
                        }
                    } else {
                        if (colorStack.size() > 0)
                            textFill = colorStack.pop();
                    }
                }

                if (event.getChangeType().equals("sup")) {
                    if (event.isStart()) {
                        fontSize *= 0.8;
                        offset -= 0.3 * fontSize;
                    } else {
                        fontSize *= 1 / 0.8;
                        offset += 0.3 * fontSize;
                    }
                }

                if (event.getChangeType().equals("sub")) {
                    if (event.isStart()) {
                        offset += 0.3 * fontSize;
                        fontSize *= 0.8;
                    } else {
                        fontSize *= 1 / 0.8;
                        offset -= 0.3 * fontSize;
                    }
                }

                active.put(event.getChangeType(), event.isStart());

                segmentStart = event.segmentStart();

                if (event.change() == Event.Change.image) {
                    final Node node = getImageNode(event.argument());
                    if (node != null) {
                        node.setTranslateY(offset);
                        getChildren().add(node);
                    }
                } else if (event.change() == Event.Change.lineBreak) {
                    getChildren().add(new Text("\n"));
                }
            }
        }

        if (getGraphic() != null && (getContentDisplay() != ContentDisplay.BOTTOM || getContentDisplay() != ContentDisplay.RIGHT))
            getChildren().add(getGraphic());
    }

    private Node getImageNode(String specification) {
        if (specification != null) {
            specification = specification.replaceAll("\\s+\"", " \"").replaceAll("\"\\s+", "\" ");

            final Map<String, String> map = new HashMap<>();
            final String[] tokens = specification.split(" ");
            for (String token : tokens) {
                final String[] pair = token.split("=");
                if (pair.length == 2) {
                    final String key = pair[0].trim();
                    String value = pair[1].trim();
                    if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2)
                        value = value.substring(1, value.length() - 1);
                    if (key.length() > 0)
                        map.put(key, value);
                }
            }
            if (map.containsKey("src")) {
                final String src = map.get("src");
                try {
                    final double width = (map.containsKey("width") ? Double.parseDouble(map.get("width")) : -1);
                    final double height = (map.containsKey("height") ? Double.parseDouble(map.get("height")) : -1);

                    final ImageView imageView;

                    synchronized (file2image) {
                        if (file2image.containsKey(src))
                            imageView = new ImageView(file2image.get(src));
                        else {
                            final Image image = new Image(src);
                            imageView = new ImageView(image);

                            Executors.newSingleThreadExecutor().submit(() -> {
                                        // note that the same file might get processed multiple times...
                                        // to fix this, we will implement storage of the images in a new nexus resources block
                                        final Image image2 = BasicFX.copyAndRemoveWhiteBackground(imageView.getImage(), 240, true);
                                        if (image2 != null)
                                            Platform.runLater(() -> {
                                                imageView.setImage(image2);
                                                file2image.put(src, image2);
                                            });
                                    }
                            );
                        }
                    }

                    if (width == -1 || height == -1)
                        imageView.setPreserveRatio(true);
                    if (width != -1)
                        imageView.setFitWidth(getScale() * width);
                    if (height != -1)
                        imageView.setFitHeight(getScale() * height);
                    return imageView;
                } catch (Exception ignored) {
                }
            }
            if (map.get("alt") != null)
                return new Text(map.get("alt"));
        }
        return null;
    }

    public double getScale() {
        return scale.get();
    }

    public DoubleProperty scaleProperty() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale.set(scale);
    }

    /**
     * gets a very rough estimate of the width
     *
     * @return estimated width
     */
    public double getEstimatedWidth() {
        return getRawText().length() * 0.7 * getFont().getSize();
    }

    private record Event(RichTextLabel.Event.Change change, int pos, int segmentStart, String argument) {
        enum Change {
            htmlStart("<html>"), htmlEnd("</html>"),
            italicStart("<i>"), italicEnd("</i>"),
            boldStart("<b>"), boldEnd("</b>"),
            strikeStart("<a>"), strikeEnd("</a>"),
            underlineStart("<u>"), underlineEnd("</u>"),
            supStart("<sup>"), supEnd("</sup>"),
            subStart("<sub>"), subEnd("</sub>"),
            colorStart("<c "), colorEnd("</c>"),
            fontSizeStart("<size "), fontSizeEnd("</size>"),
            fontFamilyStart("<font "), fontFamilyEnd("</font>"),
            lineBreak("<br>"),
            image("<img ");

            private final String tag;

            Change(String tag) {
                this.tag = tag;
            }

            public String tag() {
                return tag;
            }
        }

        public String getChangeType() {
            return change().name().replaceAll("Start$", "").replaceAll("End$", "");
        }

        public boolean isStart() {
            return change().name().endsWith("Start");
        }

        public static Event getEventAtPos(String line, int pos) {
            line = line.substring(pos);
            for (Event.Change change : Event.Change.values()) {
                if (line.startsWith(change.tag())) {
                    if (change.tag().endsWith(" ")) // requires argument
                    {
                        int startPos = change.tag().length();
                        int endPos = line.indexOf(">");
                        String argument = (startPos < endPos ? line.substring(startPos, endPos).trim() : null);
                        if (argument != null && argument.startsWith("\"") && argument.endsWith("\""))
                            argument = argument.substring(1, argument.length() - 1);
                        return new Event(change, pos, pos + endPos + 1, argument);
                    } else
                        return new Event(change, pos, pos + change.tag().length(), null);
                }
            }
            return null;
        }
    }
}
