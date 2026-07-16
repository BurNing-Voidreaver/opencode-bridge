package com.opencode.bridge.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import org.jetbrains.plugins.terminal.TerminalToolWindowManager

class OpenOpencodeAction : AnAction() {
    private val LOG = Logger.getInstance(OpenOpencodeAction::class.java)

    init {
        templatePresentation.text = "Open OpenCode"
        templatePresentation.description = "Open OpenCode in terminal"
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }

    @Suppress("DEPRECATION")
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val terminalManager = TerminalToolWindowManager.getInstance(project)

        // createLocalShellWidget is deprecated in 2024.2 but remains the only
        // API that returns a ShellTerminalWidget with executeCommand(); it is
        // supported across the whole since/until build range, so we keep it.
        val widget = terminalManager.createLocalShellWidget(
            project.basePath ?: ".",
            "opencode"
        )

        val command = getOpencodeCommand()
        widget.executeCommand(command)
    }

    private fun getOpencodeCommand(): String {
        return "opencode"
    }
}
