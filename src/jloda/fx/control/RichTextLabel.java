/*
 * RichTextLabel.java Copyright (C) 2020. Daniel H. Huson
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

import javafx.beans.property.*;
import javafx.scene.control.Control;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * A simple RichTextLabel. A number of html and html-like tags are interpreted.
 * The text can optionally be enclosed in \<html\> \</html\> tags.
 * List of tags:
 * \<i\> and \</i\> - italics
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
    private final ObjectProperty<Font> font = new SimpleObjectProperty<>(Font.font("serif", 14));
    private final StringProperty text = new SimpleStringProperty();
    private final BooleanProperty requireHTMLTag = new SimpleBooleanProperty(false);
    private final ObjectProperty<Paint> textFill = new SimpleObjectProperty<>(Color.BLACK);

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
        setText(text);
    }

    public String getText() {
        return text.get();
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

    private void update() {
        getChildren().clear();

        if (getText().length() > 0) {
            final ArrayList<Event> events = new ArrayList<>();
            {
                final Event event = Event.getEventAtPos(getText(), 0);
                if (event != null && event.getChange().equals(Event.Change.htmlStart)) {
                    events.add(event);
                } else {
                    if (isRequireHTMLTag()) { // require leading HTML tag, but none found, return non-styled text
                        final Text text = new Text(getText());
                        text.setFont(getFont());
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
                    if (event.getChange().equals(Event.Change.htmlEnd))
                        break;
                }
            }
            if (events.size() <= 1 || !events.get(events.size() - 1).getChange().equals(Event.Change.htmlEnd))
                events.add(new Event(Event.Change.htmlEnd, getText().length(), getText().length(), null));

            double offset = 0;
            Font font = getFont();
            double fontSize = font.getSize();
            Paint textFill = getTextFill();

            final Map<String, Boolean> active = new HashMap<>();

            final Stack<Font> fontStack = new Stack<>();
            final Stack<Double> fontSizeStack = new Stack<>();
            final Stack<Paint> colorStack = new Stack<>();

            int segmentStart = events.get(0).getSegmentStart();

            for (int i = 1; i < events.size(); i++) {
                final Event event = events.get(i);

                if (event.getPos() > segmentStart) {
                    final Text textItem = new Text(getText().substring(segmentStart, event.getPos()));
                    textItem.setFill(textFill);

                    final FontWeight weight;
                    final Boolean bold = active.get("bold");
                    if (bold == null)
                        weight = font.getName().contains("Bold") ? FontWeight.BOLD : FontWeight.NORMAL;
                    else if (bold)
                        weight = FontWeight.BOLD;
                    else
                        weight = FontWeight.NORMAL;

                    final FontPosture posture;
                    final Boolean italics = active.get("italics");
                    if (italics == null)
                        posture = font.getName().contains("Italics") ? FontPosture.ITALIC : FontPosture.REGULAR;
                    else if (italics)
                        posture = FontPosture.ITALIC;
                    else
                        posture = FontPosture.REGULAR;

                    textItem.setFont(Font.font(font.getFamily(), weight, posture, fontSize));

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

                if (event.getChange().getChangeType().equals("fontFamily")) {
                    if (event.getChange().isStart()) {
                        final String argument = event.getArgument();
                        if (argument != null) {
                            fontStack.push(font);
                            font = new Font(argument, getFont().getSize());
                        }
                    } else {
                        if (fontStack.size() > 0)
                            font = fontStack.pop();
                    }
                }
                if (event.getChange().getChangeType().equals("fontSize")) {
                    if (event.getChange().isStart()) {
                        final String argument = event.getArgument();
                        if (argument != null) {
                            fontSizeStack.push(fontSize);
                            try {
                                fontSize = Double.parseDouble(argument);
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    } else {
                        if (fontSizeStack.size() > 0)
                            fontSize = fontSizeStack.pop();
                    }
                }
                if (event.getChange().getChangeType().equals("color")) {
                    if (event.getChange().isStart()) {
                        final String argument = event.getArgument();
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

                if (event.getChange().getChangeType().equals("sup") && !event.getChange().isStart()) {
                    offset += 0.3 * fontSize;
                    fontSize *= 1 / 0.8;
                } else if (event.getChange().getChangeType().equals("sub") && !event.getChange().isStart()) {

                    offset -= 0.3 * fontSize;
                    fontSize *= 1 / 0.8;
                }

                if (event.getChange().getChangeType().equals("sup") && event.getChange().isStart()) {
                    offset -= 0.3 * fontSize;
                    fontSize *= 0.8;
                } else if (event.getChange().getChangeType().equals("sub") && event.getChange().isStart()) {

                    offset += 0.3 * fontSize;
                    fontSize *= 0.8;
                }

                active.put(event.getChange().getChangeType(), event.getChange().isStart());

                segmentStart = event.getSegmentStart();
            }
        }
    }

    static private class Event {
        enum Change {
            htmlStart("<html>"), htmlEnd("</html>"),
            italicsStart("<i>"), italicsEnd("</i>"),
            boldStart("<b>"), boldEnd("</b>"),
            strikeStart("<a>"), strikeEnd("</a>"),
            underlineStart("<u>"), underlineEnd("</u>"),
            supStart("<sup>"), supEnd("</sup>"),
            subStart("<sub>"), subEnd("</sub>"),
            colorStart("<c "), colorEnd("</c>"),
            fontSizeStart("<size "), fontSizeEnd("</size>"),
            fontFamilyStart("<font "), fontFamilyEnd("</font>");

            private final String tag;

            Change(String tag) {
                this.tag = tag;
            }

            public String getTag() {
                return tag;
            }

            public String getChangeType() {
                return name().replaceAll("Start$", "").replaceAll("End$", "");
            }

            public boolean isStart() {
                return name().endsWith("Start");
            }
        }

        private final Event.Change change;
        private final int pos;
        private final int segmentStart;
        private final String argument;

        public Event(Event.Change change, int pos, int segmentStart, String argument) {
            this.change = change;
            this.pos = pos;
            this.segmentStart = segmentStart;
            this.argument = argument;
        }

        public Event.Change getChange() {
            return change;
        }

        public int getPos() {
            return pos;
        }

        public int getSegmentStart() {
            return segmentStart;
        }

        public static Event getEventAtPos(String line, int pos) {
            line = line.substring(pos);
            for (Event.Change change : Event.Change.values()) {
                if (line.startsWith(change.getTag())) {
                    if (change.getTag().endsWith(" ")) // requires argument
                    {
                        int startPos = change.getTag().length();
                        int endPos = line.indexOf(">");
                        String argument = (startPos < endPos ? line.substring(startPos, endPos).trim() : null);
                        if (argument != null && argument.startsWith("\"") && argument.endsWith("\""))
                            argument = argument.substring(1, argument.length() - 1);
                        return new Event(change, pos, pos + endPos + 1, argument);
                    } else
                        return new Event(change, pos, pos + change.getTag().length(), null);
                }
            }
            return null;
        }

        public String getArgument() {
            return argument;
        }
    }
}
