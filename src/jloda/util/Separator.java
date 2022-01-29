package jloda.util;

/**
 * separator
 * Daniel Huson, 1.2022
 */
public enum Separator {
    csv(","), tab("\t"), semicolon(";");
    private final String ch;

    Separator(String ch) {
        this.ch = ch;
    }

    public String getChar() {
        return ch;
    }
}
