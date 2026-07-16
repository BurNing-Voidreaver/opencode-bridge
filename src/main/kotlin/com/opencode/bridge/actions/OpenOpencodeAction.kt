package com.opencode.bridge.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import org.jetbrains.plugins.terminal.TerminalToolWindowManager
import java.io.File

class OpenOpencodeAction : AnAction() {
    private val LOG = Logger.getInstance(OpenOpencodeAction::class.java)

    init {
        templatePresentation.text = "Open Code"
        templatePresentation.description = "Open OpenCode in terminal"
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }

    @Suppress("DEPRECATION")
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        if (!isOpencodeInstalled()) {
            Messages.showErrorDialog(
                project,
                "opencode CLI is not found in PATH.\n\n" +
                    "Please install opencode first:\n" +
                    "  https://opencode.ai\n\n" +
                    "Make sure the `opencode` command is available in your terminal.",
                "Open Code — opencode not found"
            )
            return
        }

        val terminalManager = TerminalToolWindowManager.getInstance(project)

        // createLocalShellWidget is deprecated in 2024.2 but remains the only
        // API that returns a ShellTerminalWidget with executeCommand(); it is
        // supported across the whole since/until build range, so we keep it.
        val widget = terminalManager.createLocalShellWidget(
            project.basePath ?: ".",
            "Open Code"
        )

        val command = getOpencodeCommand()
        widget.executeCommand(command)
    }

    /** Check whether the `opencode` executable is reachable on the system PATH. */
    private fun isOpencodeInstalled(): Boolean {
        val pathEnv = System.getenv("PATH") ?: return false
        val pathDirs = pathEnv.split(File.pathSeparator)
        // 1) Look for an executable file named "opencode" on PATH.
        for (dir in pathDirs) {
            val candidate = File(dir, "opencode")
            if (candidate.isFile && candidate.canExecute()) return true
        }
        // 2) Fallback: ask the shell (works for aliases / shell functions on Unix).
        return try {
            val pb = ProcessBuilder(if (System.getProperty("os.name").lowercase().contains("win"))
                listOf("where", "opencode") else
                listOf("sh", "-c", "command -v opencode"))
            pb.redirectErrorStream(true)
            val process = pb.start()
            val finished = process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)
            if (!finished) { process.destroy(); return false }
            process.exitValue() == 0
        } catch (e: Exception) {
            LOG.warn("Failed to detect opencode installation", e)
            false
        }
    }

    private fun getOpencodeCommand(): String {
        return "opencode"
    }
}
