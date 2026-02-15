package dev.juherr.mobilityid4j;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

class ContractIdTest {
    @Test
    void parsesAndNormalizesIsoContractIds() {
        List<ContractId> ids = List.of(
                ContractId.parseStrict(ContractIdStandard.ISO, "NL-TNM-000122045-U"),
                ContractId.parseStrict(ContractIdStandard.ISO, "Nl-TnM-000122045-U"),
                ContractId.parseStrict(ContractIdStandard.ISO, "NLTNM000122045"));

        assertThat(ids).containsOnly(ContractId.of(ContractIdStandard.ISO, "NL", "TNM", "000122045"));
    }

    @Test
    void rendersCompactAndCanonicalForms() {
        ContractId id = ContractId.of(ContractIdStandard.ISO, "NL", "TNM", "000722345");

        assertThat(id.toString()).isEqualTo("NL-TNM-000722345-X");
        assertThat(id.toCompactString()).isEqualTo("NLTNM000722345X");
        assertThat(id.toCompactStringWithoutCheckDigit()).isEqualTo("NLTNM000722345");
    }

    @Test
    void convertsBetweenStandards() {
        ContractId din = ContractId.parseStrict(ContractIdStandard.DIN, "NL-TNM-012204-5");

        assertThat(din.convertTo(ContractIdStandard.EMI3).toString()).isEqualTo("NL-TNM-C00122045-K");
        assertThat(din.convertTo(ContractIdStandard.ISO).toString()).isEqualTo("NL-TNM-000122045-U");
        assertThat(ContractId.parseStrict(ContractIdStandard.EMI3, "NL-TNM-C00122045-K")
                        .convertTo(ContractIdStandard.DIN)
                        .toString())
                .isEqualTo("NL-TNM-012204-5");
    }

    @Test
    void rejectsInvalidInputs() {
        assertThatThrownBy(() -> ContractId.parseStrict(ContractIdStandard.ISO, "NL-TNM-000122045-X"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ContractId.of(ContractIdStandard.ISO, "A", "TNM", "000122045"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ContractId.of(ContractIdStandard.ISO, "NL", "TNM", "72245"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
