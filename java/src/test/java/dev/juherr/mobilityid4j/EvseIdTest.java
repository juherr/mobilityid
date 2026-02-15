package dev.juherr.mobilityid4j;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class EvseIdTest {
    @Test
    void parsesIsoWithAndWithoutSeparators() {
        assertThat(EvseId.parse("DE*AB7*E840*6487")).isPresent();
        assertThat(EvseId.parse("DEAB7E8406487")).isPresent();
    }

    @Test
    void parsesDinFormat() {
        assertThat(EvseId.parse("+49*810*000*438")).isPresent();
        assertThat(EvseIdDin.parse("+49*810*000*438")).isPresent();
    }

    @Test
    void buildsIsoAndDinFromFields() {
        EvseId iso = EvseId.of("NL", "TNM", "840*6487");
        EvseId din = EvseId.of("+31", "745", "840*6487");

        assertThat(iso).isInstanceOf(EvseIdIso.class);
        assertThat(iso.toString()).isEqualTo("NL*TNM*E840*6487");
        assertThat(din).isInstanceOf(EvseIdDin.class);
        assertThat(din.toString()).isEqualTo("+31*745*840*6487");
    }

    @Test
    void rejectsMixedInvalidFormats() {
        assertThatThrownBy(() -> EvseId.of("+31", "ABC", "840*6487")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> EvseId.of("+31", "745", "E840*6487")).isInstanceOf(IllegalArgumentException.class);
    }
}
