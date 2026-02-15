package dev.juherr.mobilityid4j.interpolators;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MobilityIdParsersTest {
    @Test
    void parsesMainTypes() {
        assertThat(MobilityIdParsers.parseContractIdIso("NL-TNM-000722345-X")).isPresent();
        assertThat(MobilityIdParsers.parseEvseId("NL*TNM*E840*6487")).isPresent();
        assertThat(MobilityIdParsers.parseProviderId("ABC")).isPresent();
    }
}
