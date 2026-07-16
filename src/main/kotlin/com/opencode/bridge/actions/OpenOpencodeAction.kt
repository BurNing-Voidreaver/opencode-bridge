package com.opencode.bridge.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.ToolWindowManager
import org.jetbrains.plugins.terminal.TerminalToolWindowFactory
import org.jetbrains.plugins.terminal.TerminalToolWindowManager
import java.io.File

class OpenOpencodeAction : AnAction() {
    private val LOG = Logger.getInstance(OpenOpencodeAction::class.java)

    companion object {
        private const val TAB_NAME = "Open Code"
    }

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

        val existing = findOpenCodeTab(terminalManager)
        if (existing != null) {
            focusExistingTab(project, terminalManager, existing)
            return
        }

        val widget = terminalManager.createLocalShellWidget(
            project.basePath ?: ".",
            "Open Code"
        )

        val command = getOpencodeCommand()
        widget.executeCommand(command)
    }

    private fun findOpenCodeTab(manager: TerminalToolWindowManager) =
        manager.terminalWidgets.firstOrNull { widget ->
            manager.getContainer(widget)?.content?.displayName == TAB_NAME
        }

    private fun focusExistingTab(
        project: Project,
        manager: TerminalToolWindowManager,
        widget: com.intellij.terminal.ui.TerminalWidget
    ) {
        ApplicationManager.getApplication().invokeLater {
            if (project.isDisposed) return@invokeLater
            val toolWindow = manager.getToolWindow()
                ?: ToolWindowManager.getInstance(project)
                    .getToolWindow(TerminalToolWindowFactory.TOOL_WINDOW_ID)
            val content = manager.getContainer(widget)?.content ?: return@invokeLater
            toolWindow?.contentManager?.setSelectedContent(content, true)
            toolWindow?.activate({ widget.requestFocus() }, true)
        }
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
