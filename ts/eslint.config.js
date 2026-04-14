import { readFileSync } from "node:fs";
import path from "node:path";

import tsParser from "@typescript-eslint/parser";
import headerPlugin from "eslint-plugin-header";

const header = {
  ...headerPlugin,
  rules: {
    ...headerPlugin.rules,
    header: {
      ...headerPlugin.rules.header,
      meta: {
        ...headerPlugin.rules.header.meta,
        schema: false,
      },
    },
  },
};

const canonicalHeader = readFileSync(
  path.resolve(import.meta.dirname, "license-header.txt"),
  "utf8",
)
  .replaceAll("\r\n", "\n")
  .trimEnd()
  .split("\n");
const headerLines = ["", ...canonicalHeader.slice(1, -1), " "];

export default [
  {
    ignores: ["dist/**", "node_modules/**"],
  },
  {
    files: ["**/*.ts"],
    languageOptions: {
      parser: tsParser,
      parserOptions: {
        sourceType: "module",
      },
    },
    plugins: {
      header,
    },
    rules: {
      "header/header": ["error", "block", headerLines, 1],
    },
  },
];
