import path from "node:path";
import process from "node:process";

import { applyHeader, listTypeScriptFiles } from "./license-common.mjs";

async function main() {
  const root = path.resolve(import.meta.dirname, "..");
  const files = await listTypeScriptFiles(root);
  const changed = [];

  for (const file of files) {
    const updated = await applyHeader(file);
    if (updated) {
      changed.push(path.relative(root, file));
    }
  }

  if (changed.length === 0) {
    console.log("All TypeScript files already have the expected header.");
    return;
  }

  console.log(`Applied headers to ${changed.length} files:`);
  for (const file of changed) {
    console.log(`- ${file}`);
  }
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
