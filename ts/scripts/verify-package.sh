#!/usr/bin/env bash
# Builds and packs @juherr/mobilityid, asserts the tarball content, then installs that tarball in a
# throw-away consumer project and uses it from Node (ESM import) and from TypeScript (type
# declarations resolve through `exports`). Usage: scripts/verify-package.sh [version] [out-dir]
set -euo pipefail

cd "$(dirname "$0")/.."

version="${1:-$(node -p "require('./package.json').version")}"
out_dir="${2:-$(pwd)/build/npm-package}"
repo_root="$(cd .. && pwd)"
tarball="${out_dir}/juherr-mobilityid-${version}.tgz"

# Never wipe a caller-provided directory: create it if needed and replace only our own tarball.
mkdir -p "${out_dir}"
rm -f "${tarball}"

# The version is stamped into package.json for the pack only; the working copy is restored on exit
# (from a byte copy, not from git, so uncommitted edits survive). The consumer project is a
# temporary directory removed by the same trap, whether the run succeeds or fails.
manifest_backup=$(mktemp)
consumer=$(mktemp -d)
cp package.json "${manifest_backup}"
trap 'cp "${manifest_backup}" package.json; rm -f "${manifest_backup}"; rm -rf "${consumer}"' EXIT
if [[ "${version}" != "$(node -p "require('./package.json').version")" ]]; then
  vp exec bun pm pkg set version="${version}" >/dev/null
fi

vp exec bun run check
npm pack --silent --pack-destination "${out_dir}"
"${repo_root}/scripts/verify-npm-package.sh" "${tarball}" "${version}"

# publint and Are The Types Wrong (pinned devDependencies, run from the lockfile) on the tarball that
# ships: `exports` consistency, and type resolution from every module system consumers may use
# (the package is ESM-only, so the CommonJS failures are expected and ignored by the profile).
node_modules/.bin/publint --strict "${tarball}"
node_modules/.bin/attw --profile esm-only "${tarball}"

cat > "${consumer}/package.json" <<JSON
{ "name": "mobilityid-consumer-smoke", "private": true, "type": "module" }
JSON
npm install --silent --no-audit --no-fund --prefix "${consumer}" "${tarball}"

cat > "${consumer}/smoke.mjs" <<'JS'
import { ContractId, ContractIdStandards, CountryCode, ISO_3166_ALPHA2, MobilityIdParsers, ValidationError } from "@juherr/mobilityid";

const strict = ContractId.parseStrict(ContractIdStandards.ISO, "NL-TNM-000122045-U");
if (strict.toCompactString() !== "NLTNM000122045U") throw new Error("unexpected rendering " + strict);
if (ContractId.parse(ContractIdStandards.ISO, "NL-TNM-000122045-X") !== null) throw new Error("tolerant parser should return null");
try {
  ContractId.parseStrict(ContractIdStandards.ISO, "NL-TNM-000122045-X");
  throw new Error("strict parser should throw");
} catch (error) {
  if (!(error instanceof ValidationError) || !(error instanceof TypeError)) throw error;
}
const evse = MobilityIdParsers.parseEvseId("+49*810*000*438");
if (evse === null) throw new Error("expected a DIN EVSE id");
if (strict.countryCode !== "NL") throw new Error("string identifiers are plain strings at runtime");
if (ISO_3166_ALPHA2.length !== 249 || !ISO_3166_ALPHA2.includes("NL")) throw new Error("ISO_3166_ALPHA2 is not the 249-code table");
if (CountryCode.isValid("EU") || !CountryCode.isValid("nl")) throw new Error("CountryCode must accept ISO 3166-1 codes only");
console.log("@juherr/mobilityid consumer smoke (node): OK", String(strict), String(evse));
JS
node "${consumer}/smoke.mjs"

# Type declarations must resolve through `exports` under NodeNext; the tolerant contract is `T | null`,
# `tryParse` is a discriminated union, `CountryCode` is a literal union and the open-ended
# identifiers keep their brand through the packed declarations.
cat > "${consumer}/smoke.ts" <<'TS'
import {
  ContractId,
  ContractIdStandards,
  CountryCode,
  ISO_3166_ALPHA2,
  OperatorIdIso,
  ProviderId,
  type ContractIdStandard,
  type ParseResult,
} from "@juherr/mobilityid";

const standard: ContractIdStandard = ContractIdStandards.ISO;
const strict: ContractId = ContractId.parseStrict(standard, "NL-TNM-000122045-U");
const tolerant: ContractId | null = ContractId.parse(standard, "NL-TNM-000122045-X");
// @ts-expect-error tolerant parsers are nullable
const notNull: ContractId = ContractId.parse(standard, "NL-TNM-000122045-X");
// The result union narrows on `ok` without a cast.
const result: ParseResult<ContractId> = ContractId.tryParse(standard, "NL-TNM-000122045-X");
const outcome: string = result.ok ? result.value.toString() : result.error;
// `CountryCode` is a literal union: a known code type-checks as is, a string does not, and the
// lowercase form only gets in through the parser.
const country: CountryCode = CountryCode.from("nl");
const literal: CountryCode = "NL";
const first: CountryCode = ISO_3166_ALPHA2[0];
const widened: string = country;
// @ts-expect-error a string is not a CountryCode
const unbranded: CountryCode = "NL" as string;
// @ts-expect-error the union holds the uppercase codes only
const lowercase: CountryCode = "nl";
// Brands stay distinct through the packed declarations, even between identifiers that accept
// the same values.
// @ts-expect-error a ProviderId is not an OperatorIdIso
const crossed: OperatorIdIso = ProviderId.from("TNM");
export { strict, tolerant, notNull, outcome, literal, first, widened, unbranded, lowercase, crossed };
TS
cat > "${consumer}/tsconfig.json" <<'JSON'
{ "compilerOptions": { "module": "NodeNext", "moduleResolution": "NodeNext", "strict": true, "noEmit": true, "skipLibCheck": false, "types": [] }, "files": ["smoke.ts"] }
JSON
node_modules/.bin/tsc -p "${consumer}/tsconfig.json"
echo "@juherr/mobilityid consumer smoke (typescript): OK"
echo "verified package: ${tarball}"
