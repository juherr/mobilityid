package dev.juherr.mobilityid4j;

/** Common operator identifier abstraction across ISO and DIN formats. */
public sealed interface OperatorId permits OperatorIdIso, OperatorIdDin {
    /**
     * Returns the normalized operator identifier value.
     *
     * @return operator identifier value
     */
    String id();
}
