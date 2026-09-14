# AGENTS.md (go workspace)

Go port, module `mobilityid.juherr.dev/go`, package `go/mobilityid`. Shared guidance (domain model,
parity rule, license header text, release tags) is in the root `AGENTS.md`; API design decisions
and status are in `README.md`. Run every command below from `go/`.

## Layout

- `mobilityid/` -> one file per identifier (`contract_id.go`, `evse_id_iso.go`, ...), check digits
  (`check_digit_iso.go`, `check_digit_din.go`), parser helpers (`parser.go`), `_test.go` next to each.
- Vanity import path is served by `../docs/index.html` (`go-import` meta tag); do not break it.

## Toolchain

- Go 1.26 (`mise.toml`), `gofmt`, `go vet`, golangci-lint 2.9 with `goheader` (`.golangci.yml`).
- CI (`.github/workflows/ci-go.yml`): golangci-lint, gofmt check, `go vet`, `go test`, `go build`.
- Releases use `go/vX.Y.Z` tags (`.github/workflows/release-go.yml`), separate from the dispatched `Release` workflow that tags `vX.Y.Z`.

## Commands

- Build / test: `go build ./...`, `go test ./...`.
- One test: `go test ./... -run TestContractID`.
- Format / lint: `gofmt -w .`, `go vet ./...`, `golangci-lint run`.
- License headers: add the Apache block comment before `package`; `golangci-lint run` validates it.

## Code Style

- Idiomatic Go, not a 1:1 Scala mirror: `New...` factories return `(*T, error)`, never panic on
  user input; immutable structs with unexported fields; `String()` gives the canonical rendering;
  accessor methods expose components.
- Scala parity first: Go-specific extensions are allowed, never as a replacement for
  Scala-compatible behavior.
