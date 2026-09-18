# mobilityid-go

Go port of the `mobilityid` Scala library: parsing, validation, rendering and conversion of
EV charging identifiers (contract ids in the ISO 15118, EMI3 and DIN SPEC 91286 formats, EVSE
ids in the ISO and DIN formats, and their building blocks). This document records the design
and tooling choices of the port; the domain model itself is described in the root `AGENTS.md`.

## Install

```sh
go get mobilityid.juherr.dev/go/mobilityid@v0.1.0
```

Module path `mobilityid.juherr.dev/go` (vanity import served by `docs/index.html`), package
`mobilityid.juherr.dev/go/mobilityid`. Use an explicit version for now: proxy.golang.org holds
a phantom `v1.1.0` synthesized from the Scala `v1.1.0` tag before the Go module existed, so
`@latest` does not resolve to a usable version until the next release
(see [#70](https://github.com/juherr/mobilityid/issues/70)). Versions follow the common
`vX.Y.Z` line of the repository, tagged `go/vX.Y.Z`. The module has no third-party dependency.

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
  (`.golangci.yml`: `gofmt`, `gofumpt` and `goimports` as formatters; `errorlint`, `gocritic`,
  `copyloopvar`, `misspell`, `revive`, `govet` with shadow checking and `goheader` for the
  license header), `go vet`, `go test -race -cover`, `go build` and `govulncheck`.
- **Tests:** standard `testing` package, table-driven; `Example*` functions with verified
  output; `Fuzz*` targets (parsers, `FromParts` builders, check digits) whose seeds run with
  `go test` and which can be explored with `go test ./... -run='^$' -fuzz=FuzzNewContractID
  -fuzztime=30s`.
- **Releases:** `go/vX.Y.Z` tags trigger `../.github/workflows/release-go.yml`, which runs the
  same gates before creating the GitHub release. The version follows the common release line;
  see `CONTRIBUTING.md`.
