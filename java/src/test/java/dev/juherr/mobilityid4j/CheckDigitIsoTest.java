package dev.juherr.mobilityid4j;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

class CheckDigitIsoTest {
    @Test
    void computesKnownCheckDigits() {
        List<String> contractIds = List.of(
                "NN123ABCDEFGHI",
                "FRXYZ123456789",
                "ITA1B2C3E4F5G6",
                "ESZU8WOX834H1D",
                "PT73902837ABCZ",
                "DE83DUIEN83QGZ",
                "DE83DUIEN83ZGQ",
                "DE8AA001234567");

        String computed = contractIds.stream()
                .map(CheckDigitIso::compute)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
        assertThat(computed).isEqualTo("T24RZDM0");
    }

    @Test
    void rejectsMalformedInput() {
        assertThatThrownBy(() -> CheckDigitIso.compute("")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> CheckDigitIso.compute("DE8AA0012345678")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> CheckDigitIso.compute("DE٨٣DUIEN٨٣QGZ")).isInstanceOf(IllegalArgumentException.class);
    }
}
