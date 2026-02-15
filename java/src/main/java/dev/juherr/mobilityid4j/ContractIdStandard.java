package dev.juherr.mobilityid4j;

/** Supported contract ID standards. */
public enum ContractIdStandard {
    /** ISO 15118-1 format. */
    ISO("ISO 15118-1"),
    /** EMI3 format. */
    EMI3("EMI3"),
    /** DIN SPEC 91286 format. */
    DIN("DIN SPEC 91286");

    private final String displayName;

    ContractIdStandard(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the human-readable standard name.
     *
     * @return display name
     */
    public String displayName() {
        return displayName;
    }
}
