package com.opencode.bridge

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.diagnostic.Logger
import com.opencode.bridge.mcp.McpServer

class PluginLifecycleListener : ProjectManagerListener {
    private val LOG = Logger.getInstance(PluginLifecycleListener::class.java)

    private val servers = mutableMapOf<Project, McpServer>()

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun projectOpened(project: Project) {
        val server = McpServer(project)
        servers[project] = server
        try {
            server.start()
            LOG.info("MCP Server started for project: ${project.name}")
        } catch (e: Exception) {
            LOG.error("Failed to start MCP Server for project: ${project.name}", e)
            servers.remove(project)
        }
    }

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun projectClosed(project: Project) {
        val server = servers.remove(project)
        if (server != null) {
            try {
                server.stop()
                LOG.info("MCP Server stopped for project: ${project.name}")
            } catch (e: Exception) {
                LOG.error("Failed to stop MCP Server for project: ${project.name}", e)
            }
        }
    }
}
