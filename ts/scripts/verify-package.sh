#!/usr/bin/env bash
# Builds and packs @juherr/mobilityid, asserts the tarball content, then installs that tarball in a
# throw-away consumer project and uses it from Node (ESM import) and from TypeScript (type
# declarations resolve through `exports`). Usage: scripts/verify-package.sh [version] [out-dir]
set -euo pipefail

cd "$(dirname "$0")/.."

version="${1:-$(node -p "require('./package.json').version")}"
out_dir="${2:-$(pwd)/build/npm-package}"
repo_root="$(cd .. && pwd)"

rm -rf "${out_dir}"
mkdir -p "${out_dir}"

# The version is stamped into package.json for the pack only; the working copy is restored on exit
# (from a byte copy, not from git, so uncommitted edits survive).
manifest_backup=$(mktemp)
cp package.json "${manifest_backup}"
trap 'cp "${manifest_backup}" package.json; rm -f "${manifest_backup}"' EXIT
if [[ "${version}" != "$(node -p "require('./package.json').version")" ]]; then
  vp exec bun pm pkg set version="${version}" >/dev/null
fi

vp exec bun run check
npm pack --silent --pack-destination "${out_dir}"
tarball="${out_dir}/juherr-mobilityid-${version}.tgz"
"${repo_root}/scripts/verify-npm-package.sh" "${tarball}" "${version}"

# publint: exports/main/types consistency of the packed package.
npx --yes publint@latest "${tarball}"

consumer=$(mktemp -d)
cat > "${consumer}/package.json" <<JSON
{ "name": "mobilityid-consumer-smoke", "private": true, "type": "module" }
JSON
npm install --silent --no-audit --no-fund --prefix "${consumer}" "${tarball}"

cat > "${consumer}/smoke.mjs" <<'JS'
import { ContractId, ContractIdStandards, MobilityIdParsers } from "@juherr/mobilityid";

const strict = ContractId.parseStrict(ContractIdStandards.ISO, "NL-TNM-000122045-U");
if (strict.toCompactString() !== "NLTNM000122045U") throw new Error("unexpected rendering " + strict);
if (ContractId.parse(ContractIdStandards.ISO, "NL-TNM-000122045-X") !== null) throw new Error("tolerant parser should return null");
const evse = MobilityIdParsers.parseEvseId("+49*810*000*438");
if (evse === null) throw new Error("expected a DIN EVSE id");
console.log("@juherr/mobilityid consumer smoke (node): OK", String(strict), String(evse));
JS
node "${consumer}/smoke.mjs"

# Type declarations must resolve through `exports` under NodeNext; the tolerant contract is `T | null`.
cat > "${consumer}/smoke.ts" <<'TS'
import { ContractId, ContractIdStandards, type ContractIdStandard } from "@juherr/mobilityid";

const standard: ContractIdStandard = ContractIdStandards.ISO;
const strict: ContractId = ContractId.parseStrict(standard, "NL-TNM-000122045-U");
const tolerant: ContractId | null = ContractId.parse(standard, "NL-TNM-000122045-X");
// @ts-expect-error tolerant parsers are nullable
const notNull: ContractId = ContractId.parse(standard, "NL-TNM-000122045-X");
export { strict, tolerant, notNull };
TS
cat > "${consumer}/tsconfig.json" <<'JSON'
{ "compilerOptions": { "module": "NodeNext", "moduleResolution": "NodeNext", "strict": true, "noEmit": true, "skipLibCheck": false, "types": [] }, "files": ["smoke.ts"] }
JSON
node_modules/.bin/tsc -p "${consumer}/tsconfig.json"
echo "@juherr/mobilityid consumer smoke (typescript): OK"
rm -rf "${consumer}"
echo "verified package: ${tarball}"
