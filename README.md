# OpenCode Bridge

English | [中文](#opencode-bridge-1)

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

---

# OpenCode Bridge

[English](#opencode-bridge) | 中文

一个 JetBrains IDE 插件，将你的 IDE 与终端中运行的 [OpenCode AI 编程助手](https://opencode.ai) 连接起来。

## 功能

- **一键启动** — 从主工具栏点击图标，或按 `Ctrl+Esc` 打开 OpenCode
- **IDE 上下文共享** — 通过内置 MCP 服务器，OpenCode 能看到 IDE 诊断信息、当前选区和文件内容
- **安全可靠** — 基于 Bearer Token 认证的 MCP 服务器，打开项目时自动启动
- **智能检测** — 如果本地未安装 `opencode` 命令行工具，会弹出友好提示

## 环境要求

- JetBrains IDE（IntelliJ IDEA、PyCharm 等）2024.2 或更高版本
- 系统中已安装 [opencode CLI](https://opencode.ai)

## 安装方式

### 通过 JetBrains 插件市场

*即将上线 — 审核中。*

### 通过磁盘安装

1. 从 [Releases](https://github.com/BurNing-Voidreaver/opencode-bridge/releases) 页面下载最新版本的 zip
2. 在 IDE 中：**Settings → Plugins → ⚙️ → Install from Disk...**
3. 选择下载的 `.zip` 文件并重启 IDE

## 使用方法

- 点击主工具栏右上角 OpenCode 图标，或
- 打开 **Tools → Open Code**，或
- 按下 `Ctrl+Esc`

OpenCode 将在 IDE 内的终端标签页中启动。

## 开发

```bash
# 构建插件
./gradlew buildPlugin

# 运行测试
./gradlew test

# 启动带插件的沙箱 IDE
./gradlew runIde
```

## 许可证

[MIT](LICENSE)
