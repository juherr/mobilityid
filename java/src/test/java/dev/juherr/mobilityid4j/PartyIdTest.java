package dev.juherr.mobilityid4j;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PartyIdTest {
    @Test
    void parsesAllAcceptedRepresentations() {
        assertThat(PartyId.parse("NL-TNM")).isPresent();
        assertThat(PartyId.parse("NL*TNM")).isPresent();
        assertThat(PartyId.parse("NLTNM")).isPresent();
    }

    @Test
    void rejectsNonsense() {
        assertThat(PartyId.parse("NLTNMA")).isEmpty();
        assertThat(PartyId.parse("XYTNM")).isEmpty();
        assertThat(PartyId.parse("NL%(@$")).isEmpty();
    }
}
