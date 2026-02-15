package dev.juherr.mobilityid4j;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * ISO/EMI3 check digit calculator for contract identifiers.
 */
public final class CheckDigitIso {
    private static final Matrix P1 = new Matrix(0, 1, 1, 1);
    private static final Matrix P2 = new Matrix(0, 1, 1, 2);
    private static final Matrix NEG_P2_MINUS_15 = new Matrix(0, 2, 2, 1);
    private static final Matrix[] P1S = powers(P1, 14);
    private static final Matrix[] P2S = powers(P2, 14);
    private static final Map<Character, Matrix> ENCODING = buildEncoding();
    private static final Map<Matrix, Character> DECODING = buildDecoding(ENCODING);

    private CheckDigitIso() {}

    /**
     * Computes the ISO/EMI3 check digit for a contract ID body.
     *
     * @param code identifier payload without computed check digit
     * @return computed check digit
     */
    public static char compute(String code) {
        String normalized = code.toUpperCase(Locale.ROOT);
        if (normalized.length() != P1S.length) {
            throw new IllegalArgumentException("Code must have a length of " + P1S.length);
        }
        if (normalized.length() != P2S.length) {
            throw new IllegalArgumentException("Code must have a length of " + P2S.length);
        }
        for (int i = 0; i < normalized.length(); i++) {
            if (!Ascii.isUpperOrDigit(normalized.charAt(i))) {
                throw new IllegalArgumentException("Code must consist of uppercase ASCII letters and digits");
            }
        }

        Vec t1 = sumEq(normalized, P1S, true);
        Vec t2 = sumEq(normalized, P2S, false).times(NEG_P2_MINUS_15);
        Matrix m15 = new Matrix(t1.v1 & 1, t1.v2 & 1, t2.v1 % 3, t2.v2 % 3);
        Character decoded = DECODING.get(m15);
        if (decoded == null) {
            throw new IllegalStateException("Undecodable matrix: " + m15 + ".");
        }
        return decoded;
    }

    private static Vec sumEq(String code, Matrix[] ps, boolean firstRow) {
        Vec v = new Vec(0, 0);
        for (int i = 0; i < ps.length; i++) {
            char ch = code.charAt(i);
            Matrix encoded = ENCODING.get(ch);
            if (encoded == null) {
                throw new IllegalArgumentException("Invalid character: " + ch + ".");
            }
            Vec row = firstRow ? new Vec(encoded.m11, encoded.m12) : new Vec(encoded.m21, encoded.m22);
            v = v.plus(row.times(ps[i]));
        }
        return v;
    }

    private static Matrix[] powers(Matrix base, int count) {
        Matrix[] result = new Matrix[count];
        Matrix current = base;
        for (int i = 0; i < count; i++) {
            result[i] = current;
            current = current.times(base);
        }
        return result;
    }

    private static Matrix decode(int x) {
        return new Matrix(x & 1, (x >> 1) & 1, (x >> 2) & 3, x >> 4);
    }

    private static Map<Character, Matrix> buildEncoding() {
        Map<Character, Integer> raw = new HashMap<>();
        raw.put('0', 0);
        raw.put('1', 16);
        raw.put('2', 32);
        raw.put('3', 4);
        raw.put('4', 20);
        raw.put('5', 36);
        raw.put('6', 8);
        raw.put('7', 24);
        raw.put('8', 40);
        raw.put('9', 2);
        raw.put('A', 18);
        raw.put('B', 34);
        raw.put('C', 6);
        raw.put('D', 22);
        raw.put('E', 38);
        raw.put('F', 10);
        raw.put('G', 26);
        raw.put('H', 42);
        raw.put('I', 1);
        raw.put('J', 17);
        raw.put('K', 33);
        raw.put('L', 5);
        raw.put('M', 21);
        raw.put('N', 37);
        raw.put('O', 9);
        raw.put('P', 25);
        raw.put('Q', 41);
        raw.put('R', 3);
        raw.put('S', 19);
        raw.put('T', 35);
        raw.put('U', 7);
        raw.put('V', 23);
        raw.put('W', 39);
        raw.put('X', 11);
        raw.put('Y', 27);
        raw.put('Z', 43);

        Map<Character, Matrix> encoded = new HashMap<>();
        raw.forEach((k, v) -> encoded.put(k, decode(v)));
        return Map.copyOf(encoded);
    }

    private static Map<Matrix, Character> buildDecoding(Map<Character, Matrix> encoding) {
        Map<Matrix, Character> reversed = new HashMap<>();
        encoding.forEach((k, v) -> reversed.put(v, k));
        return Map.copyOf(reversed);
    }

    private record Matrix(int m11, int m12, int m21, int m22) {
        Matrix times(Matrix other) {
            return new Matrix(
                    m11 * other.m11 + m12 * other.m21,
                    m11 * other.m12 + m12 * other.m22,
                    m21 * other.m11 + m22 * other.m21,
                    m21 * other.m12 + m22 * other.m22);
        }
    }

    private record Vec(int v1, int v2) {
        Vec plus(Vec other) {
            return new Vec(v1 + other.v1, v2 + other.v2);
        }

        Vec times(Matrix m) {
            return new Vec(v1 * m.m11 + v2 * m.m21, v1 * m.m12 + v2 * m.m22);
        }
    }
}
