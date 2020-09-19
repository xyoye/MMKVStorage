package com.google.common.base;

/**
 * Created by xyoye on 2020/9/19.
 */

public abstract class CharMatcher {

    /**
     * Determines a true or false value for the given character.
     */
    public abstract boolean matches(char c);

    abstract static class FastMatcher extends CharMatcher {

    }

    private static final class Is extends FastMatcher {

        private final char match;

        Is(char match) {
            this.match = match;
        }

        @Override
        public boolean matches(char c) {
            return c == match;
        }

        @Override
        public String toString() {
            return "CharMatcher.is('" + showCharacter(match) + "')";
        }
    }

    /**
     * Implementation of {@link #inRange(char, char)}.
     */
    private static final class InRange extends FastMatcher {

        private final char startInclusive;
        private final char endInclusive;

        InRange(char startInclusive, char endInclusive) {
            this.startInclusive = startInclusive;
            this.endInclusive = endInclusive;
        }

        @Override
        public boolean matches(char c) {
            return startInclusive <= c && c <= endInclusive;
        }

        @Override
        public String toString() {
            return "CharMatcher.inRange('"
                    + showCharacter(startInclusive)
                    + "', '"
                    + showCharacter(endInclusive)
                    + "')";
        }
    }


    public static CharMatcher is(final char match) {
        return new Is(match);
    }

    public static CharMatcher inRange(final char startInclusive, final char endInclusive) {
        return new InRange(startInclusive, endInclusive);
    }

    public int indexIn(CharSequence sequence, int start) {
        int length = sequence.length();
        checkPositionIndex(start, length);
        for (int i = start; i < length; i++) {
            if (matches(sequence.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    private static String showCharacter(char c) {
        String hex = "0123456789ABCDEF";
        char[] tmp = {'\\', 'u', '\0', '\0', '\0', '\0'};
        for (int i = 0; i < 4; i++) {
            tmp[5 - i] = hex.charAt(c & 0xF);
            c = (char) (c >> 4);
        }
        return String.copyValueOf(tmp);
    }

    public static int checkPositionIndex(int index, int size) {
        return checkPositionIndex(index, size, "index");
    }

    public static int checkPositionIndex(int index, int size, String desc) {
        // Carefully optimized for execution by hotspot (explanatory comment above)
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException(String.format(desc, index, size));
        }
        return index;
    }
}
