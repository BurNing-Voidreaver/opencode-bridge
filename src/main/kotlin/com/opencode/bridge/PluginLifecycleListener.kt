package com.opencode.bridge

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.diagnostic.Logger
import com.opencode.bridge.mcp.McpServer

class PluginLifecycleListener : ProjectManagerListener {
    private val LOG = Logger.getInstance(PluginLifecycleListener::class.java)
    private val mcpServer = McpServer()

    override fun projectOpened(project: Project) {
        try {
            mcpServer.start()
            LOG.info("OpenCode Bridge MCP Server started")
        } catch (e: Exception) {
            LOG.error("Failed to start MCP Server", e)
        }
    }

    override fun projectClosed(project: Project) {
        try {
            mcpServer.stop()
            LOG.info("OpenCode Bridge MCP Server stopped")
        } catch (e: Exception) {
            LOG.error("Failed to stop MCP Server", e)
        }
    }
}
