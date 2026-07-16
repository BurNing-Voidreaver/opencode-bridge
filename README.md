# OpenCode Bridge

A JetBrains IDE plugin that connects your IDE to the [OpenCode AI coding assistant](https://opencode.ai) running in the terminal.

## Features

- **One-click launch** — Open OpenCode from the main toolbar or with `Ctrl+Esc`
- **IDE context sharing** — OpenCode sees diagnostics, your selection, and file contents via an integrated MCP server
- **Secure** — Bearer-token authenticated MCP server, auto-starts when you open a project
- **Smart detection** — Warns you if the `opencode` CLI is missing

## Requirements

- JetBrains IDE (IntelliJ IDEA, PyCharm, etc.) 2024.2 or newer
- [opencode CLI](https://opencode.ai) installed on your system

## Installation

### From JetBrains Marketplace

*Coming soon — pending review.*

### From Disk

1. Download the latest release from the [Releases](https://github.com/BurNing-Voidreaver/opencode-bridge/releases) page
2. In your IDE: **Settings → Plugins → ⚙️ → Install from Disk...**
3. Select the downloaded `.zip` file and restart the IDE

## Usage

- Click the OpenCode icon in the main toolbar (top-right), or
- Open **Tools → Open Code**, or
- Press `Ctrl+Esc`

OpenCode will launch in a new terminal tab inside your IDE.

## Development

```bash
# Build the plugin
./gradlew buildPlugin

# Run tests
./gradlew test

# Launch a sandbox IDE with the plugin installed
./gradlew runIde
```

## License

[MIT](LICENSE)
