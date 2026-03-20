# Contributing to Foto Gallery Preview Watermarker

Thank you for taking the time to contribute! 🎉  
All contributions — bug reports, feature ideas, documentation improvements, and code changes — are warmly welcome.

---

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [How to Contribute](#how-to-contribute)
  - [Reporting Bugs](#reporting-bugs)
  - [Suggesting Features](#suggesting-features)
  - [Submitting Pull Requests](#submitting-pull-requests)
- [Development Setup](#development-setup)
- [Coding Standards](#coding-standards)
- [Commit Messages](#commit-messages)

---

## Code of Conduct

This project is governed by our [Code of Conduct](CODE_OF_CONDUCT.md). By participating you agree to uphold it.

---

## How to Contribute

### Reporting Bugs

1. **Search existing issues** to avoid duplicates.
2. If none exists, open a new issue using the **Bug report** template.
3. Include as much detail as possible:
   - Steps to reproduce
   - Expected vs actual behaviour
   - Java version (`java -version`) and OS
   - Relevant log output

### Suggesting Features

1. **Search existing issues and discussions** to see if the idea has already been raised.
2. Open a new issue using the **Feature request** template.
3. Describe the use-case clearly and explain how it benefits other users.

### Submitting Pull Requests

1. **Fork** the repository and create a branch from `main`:
   ```bash
   git checkout -b feature/your-feature-name
   ```
2. Make your changes and add tests where applicable.
3. Ensure the build and tests pass:
   ```bash
   ./mvnw clean verify
   ```
4. Run the code-cleanup profile to apply formatting and static analysis:
   ```bash
   ./mvnw validate -Pcode-cleanup
   ```
5. Push your branch and open a **Pull Request** against `main`.
6. Fill in the PR description explaining *what* and *why*.

---

## Development Setup

| Requirement | Version |
|-------------|---------|
| Java (JDK)  | 25+     |
| Maven       | 3.9+ (or use the included `./mvnw` wrapper) |

```bash
# Build and run tests
./mvnw clean verify

# Build the fat JAR (skip tests)
./mvnw clean package -DskipTests

# Build a GraalVM native image (requires GraalVM 25+)
./mvnw clean package -Pnative -DskipTests
```

---

## Coding Standards

- Code is formatted with [Spring Java Format](https://github.com/spring-io/spring-javaformat). Run the code-cleanup profile before committing.
- Architecture rules are enforced by [Taikai](https://github.com/enofex/taikai) — violations fail the build.
- Keep new code covered by unit tests under `src/test/`.
- Prefer constructor injection over field injection.

---

## Commit Messages

Every commit **must** follow the [Conventional Commits](https://www.conventionalcommits.org/) specification.
Small, focused commits are preferred over large, catch-all ones — each commit should represent a
single logical change that can be understood and reviewed in isolation.

### Format

```
<type>(<scope>): <short imperative summary>

[optional body — wrap at 72 chars]

[optional footer(s): Co-authored-by, Fixes #<issue>, BREAKING CHANGE: …]
```

### Types

| Type       | When to use |
|------------|-------------|
| `feat`     | A new feature visible to users |
| `fix`      | A bug fix |
| `docs`     | Documentation-only changes |
| `style`    | Formatting, whitespace — no logic change |
| `refactor` | Code restructuring without adding features or fixing bugs |
| `perf`     | Performance improvement |
| `test`     | Adding or correcting tests |
| `build`    | Changes to the build system or dependencies (Maven, pom.xml) |
| `ci`       | Changes to CI/CD configuration files and scripts |
| `chore`    | Maintenance tasks that don't affect production code |
| `revert`   | Reverts a previous commit |

### Scope (optional)

Use a scope to narrow the area of change. Suggested scopes for this project:

- `watermark` — core watermarking logic
- `config` — application configuration / properties
- `runner` — the `GalleryRunner` entry point
- `scripts` — `watermark.sh` / `watermark.bat` helper scripts
- `ci` — GitHub Actions workflows
- `deps` — dependency updates

### Subject line rules

- Use the **imperative mood** ("add", "fix", "remove" — not "added", "fixing")
- Keep it under **72 characters**
- Do **not** end with a period

### Breaking changes

Append `!` after the type/scope and add a `BREAKING CHANGE:` footer:

```
feat(config)!: rename gallery.input-dir to gallery.source-dir

BREAKING CHANGE: The property key gallery.input-dir has been renamed to
gallery.source-dir. Update your application.properties accordingly.
```

### Examples

```
feat: add support for HEIC input format
fix(watermark): handle empty input directory gracefully
docs: update configuration table in README
test(watermark): add edge-case tests for transparent PNG overlays
ci: cache Maven dependencies between workflow runs
chore(deps): bump Spring Boot from 4.0.2 to 4.0.3
refactor(runner): extract directory traversal into dedicated service
```
