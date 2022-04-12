/*
 * RichTextLabel.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.fx.control;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import jloda.fx.util.BasicFX;
import jloda.fx.util.GeometryUtilsFX;
import jloda.fx.util.ProgramExecutorService;
import jloda.fx.window.NotificationManager;
import jloda.util.Basic;
import jloda.util.HTMLConvert;
import jloda.util.NumberUtils;
import jloda.util.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple RichTextLabel. A number of html and html-like tags are interpreted.
 * The text can optionally be enclosed in \<html\> \</html\> tags.
 * All errors in tags are silently ignored.
 * <p>
 * Daniel Huson, 6.2020
 */
public class RichTextLabel extends TextFlow {
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
               "<c \"value\">font-color</c>, " +
               "<bg \"value\">background-color</bg>, " +
               "<box width=\"value\" height=\"value\" fill=\"color\" stroke=\"color\"> adds a box, " +
               "<img src=\"url\" alt=\"text\" width=\"value\" height=\"value\"> adds an image. Supports HTML numeric codes.";
    }

    public final static Font DEFAULT_FONT = Font.font("Arial", 14);
    private static final Map<String, Image> file2image = new ConcurrentHashMap<>();

    @Deprecated // want to remove this
    private Font _font = DEFAULT_FONT;
    private ObjectProperty<Font> font;

    private String _text;
    private StringProperty text;

    private boolean _requireHTMLTag = false;
    private BooleanProperty requireHTMLTag;

    private Node _graphic;
    private ObjectProperty<Node> graphic;

    private ContentDisplay _contentDisplay = ContentDisplay.TOP;
    private ObjectProperty<ContentDisplay> contentDisplay;

    private double _scale = 1.0;
    private DoubleProperty scale;

    private transient boolean _inUprighting = false;

    // global styling
    private BooleanProperty bold;
    private BooleanProperty italic;
    private BooleanProperty strike;
    private BooleanProperty subscript;
    private BooleanProperty superscript;
    private BooleanProperty underline;
    private ObjectProperty<Paint> textFill;
    private ObjectProperty<Paint> backgroundColor;
    private DoubleProperty fontSize;
    private StringProperty fontFamily;

    /**
     * constructor
     */
    public RichTextLabel() {
        setMaxWidth(Control.USE_PREF_SIZE);
        setMaxHeight(Control.USE_PREF_SIZE);
    }

    /**
     * constructor
     *
     * @param text text
     */
    public RichTextLabel(String text) {
        this();
        setText(text);
    }

    /**
     * copy constructor. Copies everything except for the graphics node, if set
     *
     * @param that label to be copied.
     */
    public RichTextLabel(RichTextLabel that) {
        this();
        setFont(that.getFont());
        if (that.getTextFill() != null)
            setTextFill(that.getTextFill());
        setRequireHTMLTag(that.isRequireHTMLTag());
        setContentDisplay(that.getContentDisplay());
        setScale(that.getScale());
        setGraphic(that.getGraphic());
        setText(that.getText());
    }

    public String getText() {
        var result = (text == null ? _text : text.get());
        return result == null ? "" : result;
    }

    public StringProperty textProperty() {
        if (text == null) {
            text = new SimpleStringProperty(_text);
            textProperty().addListener(c -> update());
        }
        return text;
    }


    public void setText(String text) {
        if (text != null) {
            if (text.contains("'"))
                text = text.replaceAll("'", "\"");
            if (text.contains("\n"))
                text = text.replaceAll("\\n", "<br>");
        }
        if (this.text != null)
            this.text.set(text);
        else {
            this._text = text;
            Platform.runLater(this::update);
        }
    }


    @Deprecated
    public Font getFont() {
        return font == null ? _font : font.get();
    }

    @Deprecated
    public ObjectProperty<Font> fontProperty() {
        if (font == null) {
            font = new SimpleObjectProperty<>(_font);
            fontProperty().addListener(c -> update());
        }
        return font;
    }

    @Deprecated
    public void setFont(Font font) {
        if (this.font != null)
            this.font.set(font);
        else {
            this._font = font;
            Platform.runLater(this::update);
        }
    }

    public boolean isRequireHTMLTag() {
        return requireHTMLTag == null ? _requireHTMLTag : requireHTMLTag.get();
    }

    public BooleanProperty requireHTMLTagProperty() {
        if (requireHTMLTag == null) {
            requireHTMLTag = new SimpleBooleanProperty(_requireHTMLTag);
            requireHTMLTagProperty().addListener(c -> update());
        }
        return requireHTMLTag;
    }

    public void setRequireHTMLTag(boolean requireHTMLTag) {
        if (this.requireHTMLTag != null)
            this.requireHTMLTag.set(requireHTMLTag);
        else {
            this._requireHTMLTag = requireHTMLTag;
            Platform.runLater(this::update);
        }
    }

    public Node getGraphic() {
        return graphic == null ? _graphic : graphic.get();
    }

    public ObjectProperty<Node> graphicProperty() {
        if (graphic == null) {
            graphic = new SimpleObjectProperty<>(_graphic);
            graphicProperty().addListener(c -> update());
        }
        return graphic;
    }

    public void setGraphic(Node graphic) {
        if (this.graphic != null)
            this.graphic.set(graphic);
        else {
            this._graphic = graphic;
            Platform.runLater(this::update);
        }
    }

    public ContentDisplay getContentDisplay() {
        return contentDisplay == null ? _contentDisplay : contentDisplay.get();
    }

    public ObjectProperty<ContentDisplay> contentDisplayProperty() {
        if (contentDisplay == null) {
            contentDisplay = new SimpleObjectProperty<>(_contentDisplay);
            contentDisplayProperty().addListener(c -> update());
        }
        return contentDisplay;
    }

    public void setContentDisplay(ContentDisplay contentDisplay) {
        if (this.contentDisplay != null) {
            this.contentDisplay.set(contentDisplay);
        } else {
            this._contentDisplay = contentDisplay;
            Platform.runLater(this::update);
        }
    }

    public double getScale() {
        return scale == null ? _scale : scale.get();
    }

    public DoubleProperty scaleProperty() {
        if (scale == null) {
            scale = new SimpleDoubleProperty(_scale);
            scale.addListener((v, o, n) -> {
                if (n.doubleValue() > 0)
                    update();
                else
                    Platform.runLater(() -> setScale(0.001));
            });
        }
        return scale;
    }

    public void setScale(double scale) {
        if (this.scale != null)
            this.scale.set(scale);
        else if (scale != this._scale) {
            this._scale = scale;
            if (scale > 0)
                Platform.runLater(this::update);
            else
                Platform.runLater(() -> setScale(0.001));
        }
    }

    /**
     * ensure text is upright
     */
    public void ensureUpright() {
        if (!_inUprighting) {
            Platform.runLater(() -> {
                if (!_inUprighting) {
                    _inUprighting = true;
                    try {
                        var mirrored = BasicFX.isMirrored(this);
                        if (mirrored.isPresent() && mirrored.get()) {
                            setScaleX(-getScaleX());
                        }
                        var screenAngle = BasicFX.getAngleOnScreen(this);
                        if (screenAngle.isPresent() && screenAngle.get() > 90 && screenAngle.get() < 270) {
                            setRotate(GeometryUtilsFX.modulo360(getRotate() + 180.0));
                        }
                    } finally {
                        _inUprighting = false;
                    }
                }
            });
        }
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
        return text.replaceAll("\\s+", " ").replaceAll("&#[x0-9a-eA-E]+™;", "").replaceAll("&[a-zA-Z]+;", "");
    }

     @Override
     public String toString() {
        return getRawText();
    }

    private void update() {
        getChildren().clear();
        setBackground(null);

        var workingText = HTMLConvert.convertHtmlToCharacters(getText());

        if (getGraphic() != null && (getContentDisplay() != ContentDisplay.TOP || getContentDisplay() != ContentDisplay.LEFT))
            getChildren().add(getGraphic());

        if (workingText.length() > 0 && getContentDisplay() != ContentDisplay.GRAPHIC_ONLY) {
            final ArrayList<Event> events = new ArrayList<>();
            {
                final var event = Event.getEventAtPos(workingText, 0);
                if (event != null && event.change().equals(Event.Change.htmlStart)) {
                    events.add(event);
                } else {
                    if (isRequireHTMLTag()) { // require leading HTML tag, but none found, return non-styled text
                        final var text = new Text(workingText);
                        text.getStyleClass().add("rich-text-label");
                        if (getScale() == 1.0)
                            text.setFont(getFont());
                        else
                            text.setFont(new Font(getFont().getName(), getScale() * getFont().getSize()));
                        getChildren().add(text);
                        return;
                    } else
                        events.add(new Event(Event.Change.htmlStart, 0, 0, null));
                }
            }

            for (int pos = 0; pos < workingText.length(); pos++) {
                final Event event = Event.getEventAtPos(workingText, pos);
                if (event != null) {
                    events.add(event);
                    if (event.change().equals(Event.Change.htmlEnd))
                        break;
                }
            }
            if (events.size() <= 1 || !events.get(events.size() - 1).change().equals(Event.Change.htmlEnd))
                events.add(new Event(Event.Change.htmlEnd, workingText.length(), workingText.length(), null));

            var offset = 0.0;
            var currentFont = getFont();
            var fontSize = currentFont.getSize();
            Paint textFill = null;

            final var active = new HashMap<String, Boolean>();

            final var fontStack = new Stack<Font>();
            final var fontSizeStack = new Stack<Double>();
            final var colorStack = new Stack<Paint>();

            var segmentStart = events.get(0).segmentStart();

            for (var i = 1; i < events.size(); i++) {
                final Event event = events.get(i);

                if (event.pos() > segmentStart) {
                    final var textItem = new Text(workingText.substring(segmentStart, event.pos()));

                    if (textFill != null)
                        textItem.setFill(textFill);
                    else
                        textItem.getStyleClass().add("rich-text-label");

                    final FontWeight weight;
                    final var bold = active.get("bold");
                    if (bold == null)
                        weight = BasicFX.getWeight(currentFont);
                    else if (bold)
                        weight = FontWeight.BOLD;
                    else
                        weight = FontWeight.NORMAL;

                    final FontPosture posture;
                    final var italic = active.get("italic");
                    if (italic == null)
                        posture = BasicFX.getPosture(currentFont);
                    else if (italic)
                        posture = FontPosture.ITALIC;
                    else
                        posture = FontPosture.REGULAR;

                    textItem.setFont(Font.font(currentFont.getFamily(), weight, posture, getScale() * fontSize));

                    final var strike = active.get("strike");
                    if (strike != null)
                        textItem.setStrikethrough(strike);
                    final var underline = active.get("underline");
                    if (underline != null)
                        textItem.setUnderline(underline);

                    if (offset != 0)
                        textItem.setTranslateY(offset);

                    getChildren().add(textItem);
                }

                if (event.getChangeType().equals("fontFamily")) {
                    if (event.isStart()) {
                        var family = event.argument();
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
                        final var argument = event.argument();
                        if (argument != null && NumberUtils.isDouble(argument)) {
                            fontSizeStack.push(currentFont.getSize());
                            fontSize = NumberUtils.parseDouble(argument);
                        }
                    } else if (fontSizeStack.size() > 0) {
                        fontSize = fontSizeStack.pop();
                    }

                }
                if (event.getChangeType().equals("color")) {
                    if (event.isStart()) {
                        final var argument = event.argument();
                        if (argument != null) {
                            try {
                                final Color newColor = BasicFX.parseColor(argument);
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

                if (event.change() == Event.Change.background) {
                    final var argument = event.argument();
                    if (argument != null) {
                        try {
                            final Color color = BasicFX.parseColor(argument);
                            setBackground(new Background(new BackgroundFill(color, null, null)));
                        } catch (IllegalArgumentException ignored) {
                        }
                    }
                } else if (event.change() == Event.Change.box) {
                    final var node = getBox(event.argument());
                    if (node != null) {
                        node.setTranslateY(offset);
                        getChildren().add(node);
                        offset -= 0.5 * (node.prefHeight(0) - fontSize);
                    }
                } else if (event.change() == Event.Change.image) {
                    final var node = getImageNode(event.argument());
                    if (node != null) {
                        node.setTranslateY(offset);
                        getChildren().add(node);
                        offset -= 0.5 * (node.prefHeight(0) - fontSize);
                    }
                } else if (event.change() == Event.Change.lineBreak) {
                    getChildren().add(new Text("\n"));
                    offset = 0;
                }
            }
        }

        if (getGraphic() != null && (getContentDisplay() != ContentDisplay.BOTTOM || getContentDisplay() != ContentDisplay.RIGHT))
            getChildren().add(getGraphic());

        if (getChildren().size() == 0)
            getChildren().add(new Text(" ")); // don't want this to be empty
        if (false)
            Platform.runLater(this::requestLayout);
    }

    private Node getBox(String specification) {
        if (specification != null) {
            final var map = getMap(specification);
            final var width = getScale() * (map.containsKey("width") && NumberUtils.isDouble(map.get("width")) ? NumberUtils.parseDouble(map.get("width")) : getFontSize());
            final var height = getScale() * (map.containsKey("height") && NumberUtils.isDouble(map.get("height")) ? NumberUtils.parseDouble(map.get("height")) : getFontSize());

            final var fill = (map.containsKey("fill") && BasicFX.isColor(map.get("fill")) ? BasicFX.parseColor(map.get("fill")) : getTextFill());
            final var stroke = (map.containsKey("stroke") && BasicFX.isColor(map.get("stroke")) ? BasicFX.parseColor(map.get("stroke")) : null);

            var rectangle = new Rectangle(width, height);
            rectangle.setFill(fill);
            rectangle.setStroke(stroke);
            return rectangle;
        }
        return null;
    }

    private Node getImageNode(String specification) {
        if (specification != null) {
            final var map = getMap(specification);
            if (map.containsKey("src")) {
                final String src = (StringUtils.isHttpOrFileURL(map.get("src")) ? "" : "file://") + map.get("src");
                try {
                    final double width = (map.containsKey("width") && NumberUtils.isDouble(map.get("width")) ? NumberUtils.parseDouble(map.get("width")) : -1);
                    final double height = (map.containsKey("height") && NumberUtils.isDouble(map.get("height")) ? NumberUtils.parseDouble(map.get("height")) : -1);

                    final ImageView imageView;

                    if (file2image.containsKey(src))
                        imageView = new ImageView(file2image.get(src));
                    else {
                        System.err.println("Loading: " + src);
                        final var image = new Image(src, true);
                        file2image.put(src, image);
                        imageView = new ImageView(image);
                        image.exceptionProperty().addListener((c, o, n) -> {
                            NotificationManager.showError("Failed to load image: " + n.getMessage() + ", src='" + src + "'");
                            file2image.remove(src);
                            if (n.getMessage().contains("response code: 429")) { // too many requests
                                ProgramExecutorService.submit(500, () -> {
                                    file2image.put(src, new Image(src, true));
                                    imageView.setImage(file2image.get(src));
                                });
                            }
                        });
                    }
                    Tooltip.install(imageView, new Tooltip(src));

                    if (width == -1 || height == -1)
                        imageView.setPreserveRatio(true);
                    if (width != -1)
                        imageView.setFitWidth(getScale() * width);
                    if (height != -1)
                        imageView.setFitHeight(getScale() * height);
                    return imageView;
                } catch (Exception ex) {
                    NotificationManager.showError("Failed to load image: " + ex);
                    Basic.caught(ex);
                }
            }
            if (map.get("alt") != null)
                return new Text(map.get("alt"));
        }
        return null;
    }

    private static Map<String, String> getMap(String specification) {
        specification = specification.replaceAll("\\s+\"", " \"").replaceAll("\"\\s+", "\" ").replaceAll(" =", "=").replaceAll("= ", "=");

        final var map = new HashMap<String, String>();
        final var tokens = specification.split(" ");
        for (var token : tokens) {
            final var pair = token.split("=");
            if (pair.length == 2) {
                final var key = pair[0].trim();
                var value = pair[1].trim();
                if (value.endsWith("<br"))
                    value = value.substring(0, value.length() - 3);
                if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2)
                    value = value.substring(1, value.length() - 1);
                if (key.length() > 0)
                    map.put(key, value);
            }
        }
        return map;
    }

    public static Event getPrefixElement(String text, String changeType) {
        var pos = 0;
        while (pos < text.length()) {
            var event = Event.getEventAtPos(text, pos);
            if (event != null) {
                if (event.getChangeType().equals(changeType))
                    return event;
                else
                    pos = event.segmentStart();
            } else
                break; // have hit text...
        }
        return null;
    }

    private static String removePrefixElement(String text, Event prefixElement) {
        if (prefixElement != null)
            return text.substring(0, prefixElement.pos()) + text.substring(prefixElement.segmentStart());
        else
            return null;
    }

    private static String insertPrefix(String text, String prefix) {
        return prefix + text;
    }

    private static String set(String text, Event.Change changeStart, Boolean value) {
        var prefixElement = getPrefixElement(text, changeStart.type());
        if (value != null && value) {
            if (prefixElement == null) {
                return insertPrefix(text, changeStart.tag());
            }
        } else {
            if (prefixElement != null)
                return removePrefixElement(text, prefixElement);
        }
        return text;
    }

    private static boolean isSet(String text, Event.Change changeStart) {
        return getPrefixElement(text, changeStart.type()) != null;
    }

    public static String setBold(String text, Boolean value) {
        return set(text, Event.Change.boldStart, value);
    }

    public void setBold(Boolean value) {
        setText(setBold(getText(), value));
    }

    public static boolean isBold(String text) {
        return isSet(text, Event.Change.boldStart);
    }

    public boolean isBold() {
        return isBold(getText());
    }

    public BooleanProperty boldProperty() {
        if (bold == null)
            bold = new SimpleBooleanProperty(isBold());
        bold.addListener((v, o, n) -> setBold(n));
        textProperty().addListener(e -> bold.set(isBold()));
        return bold;
    }

    public static String setItalic(String text, Boolean value) {
        return set(text, Event.Change.italicStart, value);
    }

    public void setItalic(Boolean value) {
        setText(setItalic(getText(), value));
    }

    public static boolean isItalic(String text) {
        return isSet(text, Event.Change.italicStart);
    }

    public boolean isItalic() {
        return isItalic(getText());
    }

    public BooleanProperty italicProperty() {
        if (italic == null)
            italic = new SimpleBooleanProperty(isItalic());
        italic.addListener((v, o, n) -> setItalic(n));
        textProperty().addListener(e -> italic.set(isItalic()));
        return italic;
    }

    public static String setStrike(String text, Boolean value) {
        return set(text, Event.Change.strikeStart, value);
    }

    public void setStrike(Boolean value) {
        setText(setStrike(getText(), value));
    }

    public static boolean isStrike(String text) {
        return isSet(text, Event.Change.strikeStart);
    }

    public boolean isStrike() {
        return isStrike(getText());
    }

    public BooleanProperty strikeProperty() {
        if (strike == null)
            strike = new SimpleBooleanProperty(isStrike());
        strike.addListener((v, o, n) -> setStrike(n));
        textProperty().addListener(e -> strike.set(isStrike()));
        return strike;
    }

    public static String setSubscript(String text, Boolean value) {
        return set(text, Event.Change.subStart, value);
    }

    public void setSubscript(Boolean value) {
        setText(setSubscript(getText(), value));
    }

    public static boolean isSubscript(String text) {
        return isSet(text, Event.Change.subStart);
    }

    public boolean isSubscript() {
        return isSubscript(getText());
    }

    public BooleanProperty subscriptProperty() {
        if (subscript == null)
            subscript = new SimpleBooleanProperty(isSubscript());
        subscript.addListener((v, o, n) -> setSubscript(n));
        textProperty().addListener(e -> subscript.set(isSubscript()));
        return subscript;
    }

    public static String setSuperscript(String text, Boolean value) {
        return set(text, Event.Change.supStart, value);
    }

    public void setSuperscript(Boolean value) {
        setText(setSuperscript(getText(), value));
    }

    public static boolean isSuperscript(String text) {
        return isSet(text, Event.Change.supStart);
    }

    public boolean isSuperscript() {
        return isSuperscript(getText());
    }


    public BooleanProperty superscriptProperty() {
        if (superscript == null)
            superscript = new SimpleBooleanProperty(isSuperscript());
        superscript.addListener((v, o, n) -> setSuperscript(n));
        textProperty().addListener(e -> superscript.set(isSuperscript()));
        return superscript;
    }

    public static String setUnderline(String text, Boolean value) {
        return set(text, Event.Change.underlineStart, value);
    }

    public void setUnderline(Boolean value) {
        setText(setUnderline(getText(), value));
    }

    public static boolean isUnderline(String text) {
        return isSet(text, Event.Change.underlineStart);
    }

    public boolean isUnderline() {
        return isUnderline(getText());
    }

    public BooleanProperty underlineProperty() {
        if (underline == null)
            underline = new SimpleBooleanProperty(isUnderline());
        underline.addListener((v, o, n) -> setUnderline(n));
        textProperty().addListener(e -> underline.set(isUnderline()));
        return underline;
    }

    public static String setTextFill(String text, Paint textFill) {
        var prefixElement = getPrefixElement(text, Event.Change.colorStart.type());
        if (prefixElement != null)
            text = removePrefixElement(text, prefixElement);
        if (textFill != null)
            return insertPrefix(text, String.format("<c \"%s\">", textFill));
        else
            return text;
    }

    public static Paint getTextFill(String text) {
        var prefixElement = getPrefixElement(text, Event.Change.colorStart.type());
        if (prefixElement != null) {
            final var argument = prefixElement.argument();
            if (argument != null) {
                try {
                    return BasicFX.parseColor(argument);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        return null;
    }

    public void setTextFill(Paint textFill) {
        setText(setTextFill(getText(), textFill));
    }

    public Paint getTextFill() {
        return getTextFill(getText());
    }

    public ObjectProperty<Paint> textFillProperty() {
        if (textFill == null)
            textFill = new SimpleObjectProperty<>(getTextFill());
        textFill.addListener((v, o, n) -> setTextFill(n));
        textProperty().addListener(e -> textFill.set(getTextFill()));
        return textFill;
    }

    public static String setFontSize(String text, Double size) {
        var prefixElement = getPrefixElement(text, Event.Change.fontSizeStart.type());
        if (prefixElement != null)
            text = removePrefixElement(text, prefixElement);
        if (size != null)
            return insertPrefix(text, String.format("<size \"%.1f\">", size));
        else
            return text;
    }

    public void setFontSize(double size) {
        setText(setFontSize(getText(), size));
    }

    public static double getFontSize(String text) {
        var prefixElement = getPrefixElement(text, Event.Change.fontSizeStart.type());
        if (prefixElement != null) {
            final var argument = prefixElement.argument();
            if (argument != null && NumberUtils.isDouble(argument)) {
                return NumberUtils.parseDouble(argument);
            }
        }
        return DEFAULT_FONT.getSize();
    }

    public double getFontSize() {
        return getFontSize(getText());
    }

    public DoubleProperty fontSizeProperty() {
        if (fontSize == null)
            fontSize = new SimpleDoubleProperty(getFontSize());
        fontSize.addListener((v, o, n) -> setFontSize(n.doubleValue()));
        textProperty().addListener(e -> fontSize.set(getFontSize()));
        return fontSize;
    }

    private static final Set<String> warned = new HashSet<>();

    public static String setFontFamily(String text, String fontFamily) {
        var prefixElement = getPrefixElement(text, Event.Change.fontFamilyStart.type());
        if (prefixElement != null)
            text = removePrefixElement(text, prefixElement);
        if (fontFamily != null && !fontFamily.isBlank()) {
            fontFamily = fontFamily.trim();
            if (Font.getFamilies().contains(fontFamily))
                return insertPrefix(text, String.format("<font \"%s\">", fontFamily));
            else if (!warned.contains(fontFamily)) {
                System.err.println("Unknown font family: " + fontFamily);
                System.err.println("Known fonts: " + StringUtils.toString(Font.getFamilies(), "\n"));
                warned.add(fontFamily);
            }
        }
        return text;
    }

    public void setFontFamily(String fontFamily) {
        setText(setFontFamily(getText(), fontFamily));
    }

    public static String getFontFamily(String text) {
        var prefixElement = getPrefixElement(text, Event.Change.fontFamilyStart.type());
        if (prefixElement != null && Font.getFamilies().contains(prefixElement.argument()))
            return prefixElement.argument();
        else
            return DEFAULT_FONT.getFamily();
    }

    public String getFontFamily() {
        return getFontFamily(getText());
    }

    public StringProperty fontFamilyProperty() {
        if (fontFamily == null)
            fontFamily = new SimpleStringProperty(getFontFamily());
        fontFamily.addListener((v, o, n) -> setFontFamily(n));
        textProperty().addListener(e -> fontFamily.set(getFontFamily()));
        return fontFamily;
    }

    public static String setBackgroundColor(String text, Paint background) {
        var prefixElement = getPrefixElement(text, Event.Change.background.type());
        if (prefixElement != null)
            text = removePrefixElement(text, prefixElement);
        if (background != null)
            return insertPrefix(text, String.format("<bg \"%s\">", background));
        else
            return text;
    }

    public static Paint getBackgroundColor(String text) {
        var prefixElement = getPrefixElement(text, Event.Change.background.type());
        if (prefixElement != null) {
            final var argument = prefixElement.argument();
            if (argument != null) {
                try {
                    return BasicFX.parseColor(argument);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        return null;
    }

    public void setBackgroundColor(Paint background) {
        setText(setBackgroundColor(getText(), background));
    }

    public Paint getBackgroundColor() {
        return getBackgroundColor(getText());
    }

    public ObjectProperty<Paint> backgroundColorProperty() {
        if (backgroundColor == null)
            backgroundColor = new SimpleObjectProperty<>(getBackgroundColor());
        backgroundColor.addListener((v, o, n) -> setBackgroundColor(n));
        textProperty().addListener(e -> backgroundColor.set(getBackgroundColor()));
        return backgroundColor;
    }

    public static String setBox(String text, Double width, Double height, Color fill, Color stroke) {
        var prefixElement = getPrefixElement(text, Event.Change.box.type());
        if (prefixElement != null)
            text = removePrefixElement(text, prefixElement);
        var tag = "<box ";

        if (width != null)
            tag += String.format(" width=\"%.2f\"", width);
        if (height != null)
            tag += String.format(" height=\"%.2f\"", height);
        if (fill != null)
            tag += String.format(" fill=\"%s\"", fill);
        if (stroke != null)
            tag += String.format(" stroke=\"%s\"", stroke);

        tag += ">";
        return insertPrefix(text, tag);
    }

    public void setBox(Double width, Double height, Color fill, Color stroke) {
        setBox(getText(), width, height, fill, stroke);
    }

    public static String setImage(String text, String url, String alt, Double width, Double height) {
        var prefixElement = getPrefixElement(text, Event.Change.image.type());
        if (prefixElement != null)
            text = removePrefixElement(text, prefixElement);
        if (url != null) {
            var tag = String.format("<image url=\"%s\"", url);
            if (alt != null)
                tag += String.format(" alt=\"%s\"", alt);
            if (width != null)
                tag += String.format(" width=\"%.2f\"", width);
            if (height != null)
                tag += String.format(" height=\"%.2f\"", height);
            tag += ">";
            return insertPrefix(text, tag);
        } else
            return text;
    }

    public void setImage(String url, String alt, Double width, Double height) {
        setText(setImage(getText(), url, alt, width, height));
    }

    /**
     * gets a very rough estimate of the width
     *
     * @return estimated width
     */
    public double getEstimatedWidth() {
        return getRawText().length() * 0.7 * getFont().getSize();
    }

    public record Event(RichTextLabel.Event.Change change, int pos, int segmentStart, String argument) {
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
            background("<bg "),
            box("<box "),
            image("<img "),
            lineBreak("<br>");

            private final String tag;

            Change(String tag) {
                this.tag = tag;
            }

            public String tag() {
                return tag;
            }

            public String type() {
                return name().replaceAll("Start$", "").replaceAll("End$", "");
            }

            public boolean isStart() {
                return name().endsWith("Start");
            }
        }

        public static String[] listTypes() {
            var types = new String[Change.values().length / 2];
            for (var i = 0; i < types.length; i++) {
                types[i] = Event.Change.values()[2 * i].type();
            }
            return types;
        }

        public String getChangeType() {
            return change().type();
        }

        public boolean isStart() {
            return change().isStart();
        }

        public static Event getEventAtPos(String line, int pos) {
            line = line.substring(pos);
            if (line.startsWith("<box>")) {
                return new Event(Change.box, pos, pos + 5, "");
            }
            for (Event.Change change : Event.Change.values()) {
                var tag = change.tag();
                if (line.startsWith(tag)) {
                    if (tag.endsWith(" ")) { // requires an argument
                        var startPos = tag.length() - 1; // start at the trailing " "
                        var endPos = line.indexOf(">");
                        var argument = (startPos < endPos ? line.substring(startPos, endPos).trim() : null);
                        if (argument != null && argument.startsWith("\"") && argument.endsWith("\""))
                            argument = argument.substring(1, argument.length() - 1);
                        return new Event(change, pos, pos + endPos + 1, argument);
                    } else
                        return new Event(change, pos, pos + tag.length(), null);
                }
            }
            return null;
        }
    }
}
