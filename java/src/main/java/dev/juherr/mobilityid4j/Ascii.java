package dev.juherr.mobilityid4j;

final class Ascii {
    private Ascii() {}

    static boolean isUpper(char c) {
        return c >= 'A' && c <= 'Z';
    }

    static boolean isLower(char c) {
        return c >= 'a' && c <= 'z';
    }

    static boolean isLetter(char c) {
        return isUpper(c) || isLower(c);
    }

    static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    static boolean isUpperOrDigit(char c) {
        return isUpper(c) || isDigit(c);
    }
}
