/*
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

import { defineConfig, type UserConfig } from "vite-plus";

// Every TypeScript file carries the header from license-header.txt (Julien Herr only; root
// AGENTS.md "Provenance").
const canonicalHeader = readFileSync(
  path.resolve(import.meta.dirname, "license-header.txt"),
  "utf8",
)
  .replaceAll("\r\n", "\n")
  .trimEnd();
const blockHeaderLines = `\n${canonicalHeader.split("\n").slice(1, -1).join("\n")}\n `;

// Explicit type: `isolatedDeclarations` refuses an inferred default export.
const config: UserConfig = defineConfig({
  fmt: {
    // Markdown is left to editors: the macOS and Linux oxfmt binaries disagree on the final newline.
    ignorePatterns: ["dist/**", "bun.lock", "**/*.md"],
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
      include: ["src/**/*.ts"],
      // Enforced by `vp test --coverage` (bun run test, bun run check, CI); a filtered run
      // (`vp test ContractId`) skips coverage on purpose.
      thresholds: {
        statements: 90,
        branches: 85,
        functions: 90,
        lines: 90,
      },
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

export default config;
