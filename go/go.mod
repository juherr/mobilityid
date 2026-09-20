module mobilityid.juherr.dev/go

go 1.26.0

retract (
	// v1.0.0 and v1.1.0 are Scala-only root tags of the monorepo that proxy.golang.org synthesized
	// into module versions without any Go code; v1.1.1 only exists to carry these retractions so that
	// `@latest` resolves to the highest non-retracted version.
	[v1.0.0, v1.1.1]
	// Accepted CLDR region codes outside ISO 3166-1 and overflowed the DIN check digit on long payloads.
	v0.1.0
)
