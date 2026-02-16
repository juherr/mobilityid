import path from "node:path";
import process from "node:process";

import { hasValidHeader, listTypeScriptFiles } from "./license-common.mjs";

async function main() {
  const root = path.resolve(import.meta.dirname, "..");
  const files = await listTypeScriptFiles(root);
  const invalidFiles = [];

  for (const file of files) {
    const valid = await hasValidHeader(file);
    if (!valid) {
      invalidFiles.push(path.relative(root, file));
    }
  }

  if (invalidFiles.length > 0) {
    console.error("Missing or invalid Apache-2.0 headers in:");
    for (const file of invalidFiles) {
      console.error(`- ${file}`);
    }
    process.exit(1);
  }

  console.log(`License headers are valid in ${files.length} TypeScript files.`);
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
