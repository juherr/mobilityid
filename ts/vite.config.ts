/*
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

import { readFileSync } from "node:fs";
import path from "node:path";

import { defineConfig } from "vite-plus";

const canonicalHeader = readFileSync(
  path.resolve(import.meta.dirname, "license-header.txt"),
  "utf8",
)
  .replaceAll("\r\n", "\n")
  .trimEnd();
const blockHeaderLines = `\n${canonicalHeader.split("\n").slice(1, -1).join("\n")}\n `;

export default defineConfig({
  fmt: {
    ignorePatterns: ["dist/**", "bun.lock"],
    semi: true,
    singleQuote: false,
    sortPackageJson: false,
  },
  lint: {
    ignorePatterns: ["dist/**"],
    options: {
      typeAware: true,
      typeCheck: true,
    },
    overrides: [
      {
        files: ["src/**/*.ts", "test/**/*.ts"],
        jsPlugins: ["@tony.ganchev/eslint-plugin-header"],
        rules: {
          "@tony.ganchev/header/header": [
            "error",
            {
              header: {
                commentType: "block",
                lines: [blockHeaderLines],
              },
            },
          ],
        },
      },
    ],
  },
  test: {
    environment: "node",
    include: ["test/**/*.test.ts"],
    coverage: {
      provider: "v8",
      reporter: ["text", "lcov"],
    },
  },
  pack: {
    entry: ["src/index.ts"],
    dts: true,
    format: ["esm"],
    outDir: "dist",
    outExtensions: () => ({
      js: ".js",
      dts: ".d.ts",
    }),
    sourcemap: true,
    clean: true,
  },
});
