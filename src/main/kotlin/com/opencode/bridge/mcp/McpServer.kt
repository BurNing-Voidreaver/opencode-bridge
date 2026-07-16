package com.opencode.bridge.mcp

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.sun.net.httpserver.HttpServer
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.diagnostic.Logger
import com.opencode.bridge.auth.TokenManager
import com.opencode.bridge.mcp.tools.GetDiagnosticsTool
import com.opencode.bridge.mcp.tools.GetSelectionTool
import com.opencode.bridge.mcp.tools.ReadFileTool
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors

class McpServer(private val project: Project) {
    private val LOG = Logger.getInstance(McpServer::class.java)
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private var server: HttpServer? = null
    private val tools = mapOf(
        "get_diagnostics" to GetDiagnosticsTool(),
        "get_selection" to GetSelectionTool(),
        "read_file" to ReadFileTool()
    ).onEach { (_, tool) -> tool.project = project }

    fun start() {
        if (server != null) return

        val httpServer = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        httpServer.createContext("/mcp") { exchange ->
            try {
                if (exchange.requestMethod == "POST") {
                    val body = BufferedReader(InputStreamReader(exchange.requestBody, StandardCharsets.UTF_8)).readText()
                    val authHeader = exchange.requestHeaders.getFirst("Authorization")
                    val expectedToken = "Bearer ${TokenManager.getInstance().getToken()}"
                    if (authHeader != expectedToken) {
                        sendResponse(exchange, 401, """{"jsonrpc":"2.0","error":{"code":-32000,"message":"Unauthorized"},"id":null}""")
                        return@createContext
                    }
                    val response = handleMessage(body)
                    sendResponse(exchange, 200, response)
                } else {
                    sendResponse(exchange, 405, """{"jsonrpc":"2.0","error":{"code":-32600,"message":"Method not allowed"},"id":null}""")
                }
            } catch (e: Exception) {
                LOG.error("MCP Server error", e)
                sendResponse(exchange, 500, """{"jsonrpc":"2.0","error":{"code":-32603,"message":"Internal error: ${e.message}"},"id":null}""")
            }
        }

        httpServer.executor = Executors.newFixedThreadPool(2)
        httpServer.start()

        val port = httpServer.address.port
        TokenManager.getInstance().setPort(port)
        server = httpServer

        LOG.info("MCP Server started on 127.0.0.1:$port")
    }

    fun stop() {
        server?.stop(0)
        server = null
        TokenManager.getInstance().setPort(0)
        LOG.info("MCP Server stopped")
    }

    private fun handleMessage(body: String): String {
        val json = JsonParser.parseString(body)
        if (json.isJsonArray) {
            val responses = json.asJsonArray.map { processMessage(it) }
            return gson.toJson(responses)
        }
        return processMessage(json)
    }

    private fun processMessage(json: JsonElement): String {
        val obj = json.asJsonObject
        val id = if (obj.has("id") && !obj.get("id").isJsonNull) obj.get("id") else null
        val method = obj.get("method")?.asString ?: return errorResponse(id, -32600, "Invalid Request")
        val params = if (obj.has("params") && !obj.get("params").isJsonNull) obj.get("params").asJsonObject else null

        return when (method) {
            "initialize" -> handleInitialize(id, params)
"initialized" -> """{"jsonrpc":"2.0","id":$id,"result":{}}"""
            "tools/list" -> handleToolsList(id)
            "tools/call" -> handleToolsCall(id, params)
            else -> errorResponse(id, -32601, "Method not found: $method")
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun handleInitialize(id: JsonElement?, params: com.google.gson.JsonObject?): String {
        return """
        {
            "jsonrpc": "2.0",
            "id": $id,
            "result": {
                "protocolVersion": "2025-03-26",
                "capabilities": {
                    "tools": {
                        "listChanged": false
                    }
                },
                "serverInfo": {
                    "name": "opencode-bridge",
                    "version": "0.1.0"
                }
            }
        }
        """.trimIndent()
    }

    private fun handleToolsList(id: JsonElement?): String {
        val toolsJson = tools.map { (name, tool) ->
            """
            {
                "name": "$name",
                "description": "${tool.description}",
                "inputSchema": ${tool.inputSchema}
            }
            """.trimIndent()
        }.joinToString(",")
        return """
        {
            "jsonrpc": "2.0",
            "id": $id,
            "result": {
                "tools": [$toolsJson]
            }
        }
        """.trimIndent()
    }

    private fun handleToolsCall(id: JsonElement?, params: com.google.gson.JsonObject?): String {
        if (params == null) return errorResponse(id, -32602, "Invalid params")
        val name = params.get("name")?.asString ?: return errorResponse(id, -32602, "Missing tool name")
        val arguments = if (params.has("arguments") && !params.get("arguments").isJsonNull)
            params.get("arguments").asJsonObject else null

        val tool = tools[name] ?: return errorResponse(id, -32601, "Tool not found: $name")

        val resultRef = com.intellij.openapi.util.Ref<Any>()
        ApplicationManager.getApplication().invokeAndWait {
            ApplicationManager.getApplication().runReadAction {
                resultRef.set(tool.execute(arguments))
            }
        }
        val result = resultRef.get()

        return """
        {
            "jsonrpc": "2.0",
            "id": $id,
            "result": {
                "content": [
                    {
                        "type": "text",
                        "text": ${gson.toJson(result.toString())}
                    }
                ]
            }
        }
        """.trimIndent()
    }

    private fun errorResponse(id: JsonElement?, code: Int, message: String): String {
        return """
        {
            "jsonrpc": "2.0",
            "id": ${id ?: "null"},
            "error": {
                "code": $code,
                "message": "$message"
            }
        }
        """.trimIndent()
    }

    private fun sendResponse(exchange: com.sun.net.httpserver.HttpExchange, code: Int, body: String) {
        val bytes = body.toByteArray(StandardCharsets.UTF_8)
        exchange.responseHeaders.add("Content-Type", "application/json")
        exchange.sendResponseHeaders(code, bytes.size.toLong())
        exchange.responseBody.use { it.write(bytes) }
    }
}
