package dev.juherr.mobilityid4j;

import java.util.Locale;
import java.util.regex.Pattern;

record PartyCode(String value) {
    private static final Pattern PATTERN = Pattern.compile("[A-Za-z0-9]{3}");

    PartyCode {
        if (!PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("OperatorId must have a length of 3 and be ASCII letters or digits");
        }
        value = value.toUpperCase(Locale.ROOT);
    }

    @Override
    public String toString() {
        return value;
    }
}
