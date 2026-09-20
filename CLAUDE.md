# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

`CONTRIBUTING.md` is the source of truth for the change workflow, commit conventions, changelog
and release process. Shared, tool-agnostic guidance (layout, domain architecture, build/test
commands, code style, quality gates) lives in `AGENTS.md` and is imported below. Each workspace (`scala/`, `java/`,
`go/`, `php/`, `ts/`) has its own `AGENTS.md` + `CLAUDE.md` pair, loaded automatically when you
work on files there. Keep the `AGENTS.md` files as the single source of truth for anything other
coding agents also need; only Claude Code specifics belong in `CLAUDE.md` files.

@AGENTS.md

## Claude Code specifics

- Shell commands are proxied through `rtk` by a hook (see `~/.claude/RTK.md`); write plain commands
  (`sbt test`, `./gradlew test`), the rewrite is transparent.
- `mcp__codegraph__codegraph_explore` is available for this repo (`.codegraph/` index at the root):
  prefer it over grep/Read loops for "where is X / who calls X" questions across the five workspaces.
- Project permissions live in `.claude/settings.local.json`, which is not versioned: a fresh clone
  prompts on first use for `sbt`, `./gradlew build`, PHPUnit, PHPStan and `composer test`.
- Run the single-suite command from the workspace `AGENTS.md` first, then its full gate.
- Language policy from the user config applies: discussion in French, code, comments, commits, PR
  titles/descriptions and changelog entries in English, Conventional Commits.
