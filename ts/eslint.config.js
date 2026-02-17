import { readFileSync } from "node:fs";
import path from "node:path";

import js from "@eslint/js";
import tsParser from "@typescript-eslint/parser";
import tsPlugin from "@typescript-eslint/eslint-plugin";
import headerPlugin from "@tony.ganchev/eslint-plugin-header";
import prettier from "eslint-config-prettier";

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
    files: ["scripts/**/*.mjs"],
    languageOptions: {
      globals: {
        console: "readonly",
        process: "readonly",
      },
    },
  },
  js.configs.recommended,
  {
    files: ["**/*.ts"],
    languageOptions: {
      parser: tsParser,
      parserOptions: {
        project: ["./tsconfig.json"],
        tsconfigRootDir: import.meta.dirname,
      },
    },
    plugins: {
      "@typescript-eslint": tsPlugin,
      header,
    },
    rules: {
      ...tsPlugin.configs["eslint-recommended"].overrides?.[0]?.rules,
      ...tsPlugin.configs.strict.rules,
      "header/header": ["error", "block", headerLines, 1],
      "@typescript-eslint/consistent-type-imports": "error",
      "@typescript-eslint/no-explicit-any": "error",
      "@typescript-eslint/no-unused-vars": ["error", { argsIgnorePattern: "^_" }],
    },
  },
  prettier,
];
