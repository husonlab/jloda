package jloda.util;

/**
 * separator
 * Daniel Huson, 1.2022
 */
public enum Separator {
    tab("\t"), csv(","), semicolon(";");
    private final String ch;

    Separator(String ch) {
        this.ch = ch;
    }

    public static char guessChar(String line) {
        var sep = guess(line);
        return sep == null ? 0 : sep.getChar().charAt(0);
    }

    public static Separator guess(String line) {
        for (var s : values()) {
            if (line.contains(s.getChar()))
                return s;
        }
        return null;
    }

    public String getChar() {
        return ch;
    }
}
