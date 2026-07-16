# Changelog

All notable changes to the OpenCode Bridge plugin will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- MCP server exposing IDE capabilities to the OpenCode CLI over HTTP/JSON-RPC:
  - `get_diagnostics` — read IDE diagnostics (errors, warnings) for the active file.
  - `get_selection` — read the current selection / cursor position.
  - `read_file` — read a file by absolute path.
- Bearer-token auth for the MCP server, with the token and port persisted via
  `TokenManager` (`PersistentStateComponent`).
- `Open OpCode` action (Tools menu, `Ctrl+Esc`) that opens the `opencode` CLI in
  the built-in terminal.
- Unit tests for all three MCP tools (13 tests, all passing).

### Fixed
- Build now pins Gradle to the system JDK 17 (`org.gradle.java.home`) so the
  Kotlin compiler is not fed an unsupported JDK 26.
- Corrected IntelliJ platform API usage: `TerminalToolWindowManager`
  (replacing deprecated `TerminalView`), `DaemonCodeAnalyzerEx.processHighlights`
  (replacing the non-existent `getHighlights`), `VirtualFile.contentsToByteArray`
  (replacing the non-existent `VirtualFileManager.getNioPath`), and
  `Application.invokeAndWait` (removed invalid type parameters).
- `Open OpCode` action moved from `EditorToolBar` to `ToolsMenu` so the plugin
  loads in headless / test environments where the editor toolbar is absent.
- `GetDiagnosticsTool` now returns "No diagnostics found" when every highlight is
  filtered out as INFORMATION-level (previously returned an empty string).

### Notes
- Code instrumentation (`instrumentCode` / `instrumentTestCode`) is disabled in
  `build.gradle` because the required `java-compiler-ant-tasks` artifact cannot
  be fetched from the JetBrains cloudfront CDN in some environments. This is an
  optional optimisation and does not affect plugin functionality.
- The IntelliJ test framework (`test-framework`) is resolved from a local SDK
  mirror under `test-sdk/` because the maven artifact is also behind the same
  unreachable CDN.

## [0.1.0] - 2026-07-16

### Added
- Initial scaffolding: Gradle + Kotlin IntelliJ Platform plugin targeting
  IC 2024.2 (since-build 242, until-build 252.*).
- Project structure: `McpServer`, `TokenManager`, `OpenOpencodeAction`, and the
  `McpTool` abstraction with three tool implementations.
