import { readdir, readFile, writeFile } from "node:fs/promises";
import path from "node:path";

export const APACHE_HEADER = `/*
 * Copyright (c) 2014 The New Motion team, and respective contributors
 * Copyright (c) 2026 Julien Herr, and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
`;

const HEADER_BLOCK = `${APACHE_HEADER}\n`;
const POSSIBLE_LEADING_COMMENT = /^\/\*[\s\S]*?\*\/\s*/;

export async function listTypeScriptFiles(baseDirectory) {
  const absoluteBase = path.resolve(baseDirectory);
  const locations = [path.join(absoluteBase, "src"), path.join(absoluteBase, "test")];
  const files = [];

  for (const location of locations) {
    await walk(location, files);
  }

  return files;
}

export async function hasValidHeader(filePath) {
  const content = await readFile(filePath, "utf8");
  return content.startsWith(HEADER_BLOCK);
}

export async function applyHeader(filePath) {
  const content = await readFile(filePath, "utf8");
  if (content.startsWith(HEADER_BLOCK)) {
    return false;
  }

  const withoutLeadingComment = content.replace(POSSIBLE_LEADING_COMMENT, "");
  await writeFile(filePath, `${HEADER_BLOCK}${withoutLeadingComment}`, "utf8");
  return true;
}

async function walk(directoryPath, files) {
  const entries = await readdir(directoryPath, { withFileTypes: true });
  for (const entry of entries) {
    const nextPath = path.join(directoryPath, entry.name);
    if (entry.isDirectory()) {
      await walk(nextPath, files);
      continue;
    }

    if (entry.isFile() && nextPath.endsWith(".ts")) {
      files.push(nextPath);
    }
  }
}
