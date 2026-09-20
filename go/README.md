# mobilityid-go

Go port of the `mobilityid` Scala library: parsing, validation, rendering and conversion of
EV charging identifiers (contract ids in the ISO 15118, EMI3 and DIN SPEC 91286 formats, EVSE
ids in the ISO and DIN formats, and their building blocks). This document records the design
and tooling choices of the port; the domain model itself is described in the root `AGENTS.md`.

## Install

```sh
go get mobilityid.juherr.dev/go/mobilityid
```

Module path `mobilityid.juherr.dev/go` (vanity import served by `docs/index.html`), package
`mobilityid.juherr.dev/go/mobilityid`. Versions follow the common `vX.Y.Z` line of the
repository: the `Release` workflow tags `go/vX.Y.Z` on the very commit it tags `vX.Y.Z`, so a Go
version always matches the Java, Scala, TypeScript and PHP releases. The module has no
third-party dependency.

`go.mod` retracts `v0.1.0` (accepted country codes outside ISO 3166-1, DIN check-digit overflow)
and `[v1.0.0, v1.1.1]`: `v1.0.0` and `v1.1.0` are Scala-only root tags of the monorepo that
proxy.golang.org turned into empty module versions, and `go/v1.1.1` exists only to carry the
retractions (the `go` command reads them from the highest version). `go list -m -versions
mobilityid.juherr.dev/go` therefore lists the usable versions only; add `-retracted` to see the
others. Never tag a Go-only version number.

```go
import (
	"errors"
	"fmt"

	"mobilityid.juherr.dev/go/mobilityid"
)

cid, err := mobilityid.NewContractID("nltnm000122045", mobilityid.ContractIDStandardISO)
if errors.Is(err, mobilityid.ErrInvalidContractID) {
	// ...
}
fmt.Println(cid) // NL-TNM-000122045-U

evse, _ := mobilityid.NewEvseID("DE*AB7*E840*6487") // ISO tried before DIN
fmt.Println(evse.IsISO(), evse.PowerOutletID())     // true 840*6487
```

The `Example*` functions in `mobilityid/example_test.go` (shown on pkg.go.dev) cover the main
entry points.

## API design decisions

- **Idiomatic Go, not a 1:1 Scala mirror.** `New...` constructors are strict and return
  `(*T, error)`; nothing panics on user input. The `Parse...` helpers in `parser.go` are the
  same constructors under the name the other ports use.
- **Immutable values.** Identifiers are structs with unexported fields, built only through their
  constructor, which normalizes to uppercase and validates by regex plus semantic checks
  (country code, lengths, check digits).
- **Canonical rendering.** `String()` (`fmt.Stringer`) is the canonical form
  (`NL-TNM-000122045-U`, `NL*TNM*E840*6487`); `ToCompactString()` drops the separators.
  Accessors expose the components (`CountryCode()`, `ProviderID()`, `InstanceValue()`,
  `CheckDigit()`, `PartyID()`).
- **Typed errors.** Every failure wraps one of the exported sentinels (`ErrInvalidCountryCode`,
  `ErrInvalidProviderID`, `ErrInvalidOperatorID`, `ErrInvalidPartyID`, `ErrInvalidContractID`,
  `ErrInvalidCheckDigit`, `ErrInvalidCheckDigitInput`, `ErrInvalidEvseID`,
  `ErrUnsupportedStandard`, `ErrUnconvertibleContractID`, ...) with `%w`, and the message names
  the offending input. A composite identifier wraps both its own sentinel and the failing
  component's, so an unknown country inside a contract id satisfies `errors.Is` for
  `ErrInvalidContractID` and `ErrInvalidCountryCode`.
- **Text and JSON.** Every identifier implements `encoding.TextMarshaler` with its canonical
  form, so `encoding/json` writes it as a string; zero values refuse to marshal. Every type
  whose format is unambiguous also implements `encoding.TextUnmarshaler` through its
  constructor (validation, normalization and sentinel errors apply). `ContractID` only
  marshals: its standard cannot be told from the string alone (every EMI3 id is also a valid
  ISO id), so decode the string and call `NewContractID(s, standard)` with the standard you
  expect. `EvseID` decodes with the ISO-before-DIN precedence of `NewEvseID`.
- **One `ContractID` type, three standards.** `ContractIDStandardISO`, `ContractIDStandardEMI3`
  and `ContractIDStandardDIN` select the parser; `ToISO()`, `ToEMI3()` and `ToDIN()` convert
  between them with the rules of the Scala `ContractIdConverter` instances.
- **Country codes** are validated against the ISO 3166-1 alpha-2 list of the JDK
  (`Locale.getISOCountries()`, the source the Scala reference and the Java port use), embedded
  in `mobilityid/iso3166_alpha2.go` and regenerated with `scripts/generate-iso3166.sh` (needs
  the Java version pinned in `../mise.toml`). CLDR region codes that ISO 3166-1 does not assign
  (`UK`, `EU`, `XK`, `SU`, ...) are rejected, as in every other port.
- **Check digits.** `CalculateISO7064Mod37_2` (ISO 15118 / EMI3, 14-character payload) and
  `CalculateDIN7064ModXY` (DIN) are exported pure functions. The DIN sum is reduced modulo 11 at
  every step, so the result is a valid digit for any input length (the reference overflows past
  the 11-character DIN payload). Both are checked against the 400 cross-language fixtures in
  `mobilityid/testdata/`.
- **Scala parity first.** The port supports at least every Scala `core` capability, with the
  same positive and negative fixtures. Go-specific extensions are allowed, never as a
  replacement for Scala-compatible behavior.

## Tooling

- **Go:** `go.mod` declares the oldest Go version still supported upstream (`go 1.26.0`) and no
  `toolchain` directive, so consumers on that version are not forced to download another
  toolchain. CI runs the two supported versions (1.26.x and 1.27.x) with `GOTOOLCHAIN=local`;
  `mise.toml` pins the development version.
- **Quality gates** (`../.github/workflows/ci-go.yml`): `golangci-lint run` on the v2 config
  (`.golangci.yml`: `gofumpt` and `goimports` as formatters; `errorlint`, `gocritic`,
  `copyloopvar`, `misspell`, `revive`, `govet` with shadow checking and `goheader` for the
  license header), `go vet`, `go test -race -cover` and `govulncheck`.
- **Tests:** standard `testing` package, table-driven; `Example*` functions with verified
  output; `Fuzz*` targets (parsers, `FromParts` builders, check digits) whose seeds run with
  `go test` and which can be explored with `go test ./... -run='^$' -fuzz=FuzzNewContractID
  -fuzztime=30s`.
- **Releases:** the common `Release` workflow (`../.github/workflows/release.yml`) runs the same
  gates plus `scripts/tests/retract.test.sh` (a file-based module proxy proving the `retract`
  directives make `@latest` resolve to the released version) in a `Preflight Go` job, then
  creates the signed `go/vX.Y.Z` tag next to `vX.Y.Z`; see `CONTRIBUTING.md`.
