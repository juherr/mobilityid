package dev.juherr.mobilityid4j;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CheckDigitDinTest {
    @Test
    void matchesKnownExamples() {
        assertThat(calculate(71)).isEqualTo('9');
        assertThat(calculate(110)).isEqualTo('X');
        assertThat(calculate(124)).isEqualTo('0');
        assertThat(calculate(114)).isEqualTo('6');
        assertThat(calculate(191)).isEqualTo('5');
    }

    private static char calculate(int instance) {
        return CheckDigitDin.compute("INTNM" + String.format("%06d", instance));
    }
}
