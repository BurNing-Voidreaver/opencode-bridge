package com.opencode.bridge.mcp.tools

import com.google.gson.JsonObject

abstract class McpTool {
    abstract val name: String
    abstract val description: String
    abstract val inputSchema: String

    abstract fun execute(arguments: JsonObject?): Any
}

open class GetDiagnosticsTool : McpTool() {
    override val name = "get_diagnostics"
    override val description = "Get IDE diagnostics (errors, warnings, inspections) for the current file or a specified file"
    override val inputSchema = """
    {
        "type": "object",
        "properties": {
            "file_path": {
                "type": "string",
                "description": "Optional absolute path to a file. If not provided, uses the currently active editor file."
            }
        }
    }
    """.trimIndent()

    override fun execute(arguments: JsonObject?): Any {
        val project = com.intellij.openapi.project.ProjectManager.getInstance().openProjects.firstOrNull()
            ?: return "No open project"

        val filePath = arguments?.get("file_path")?.asString

        val editor = com.intellij.openapi.fileEditor.FileEditorManager.getInstance(project).selectedTextEditor
        val virtualFile = if (filePath != null) {
            com.intellij.openapi.vfs.LocalFileSystem.getInstance().findFileByPath(filePath)
        } else {
            editor?.virtualFile ?: com.intellij.openapi.fileEditor.FileEditorManager.getInstance(project).openFiles.firstOrNull()
        }

        if (virtualFile == null) return "No file found"

        val document = editor?.document ?: return "No editor document"

        val highlights = collectHighlights(document, project)

        val meaningful = highlights.filter {
            it.severity != com.intellij.lang.annotation.HighlightSeverity.INFORMATION
        }
        if (meaningful.isEmpty()) return "No diagnostics found"

        return meaningful.joinToString("\n") { highlight ->
                val severity = when (highlight.severity) {
                    com.intellij.lang.annotation.HighlightSeverity.ERROR -> "ERROR"
                    com.intellij.lang.annotation.HighlightSeverity.WARNING -> "WARNING"
                    com.intellij.lang.annotation.HighlightSeverity.WEAK_WARNING -> "WEAK_WARNING"
                    else -> "INFO"
                }
                val line = document.getLineNumber(highlight.startOffset) + 1
                val col = highlight.startOffset - document.getLineStartOffset(line - 1) + 1
                "[$severity] Line $line, Col $col: ${highlight.description}"
            }
    }

    /**
     * Collect the diagnostics for [document] if its PSI file is known.
     *
     * Extracted as an open method so tests can override it with canned highlights
     * without driving the real (asynchronous) daemon.
     */
    protected open fun collectHighlights(
        document: com.intellij.openapi.editor.Document,
        project: com.intellij.openapi.project.Project
    ): List<com.intellij.codeInsight.daemon.impl.HighlightInfo> {
        val psiFile = com.intellij.psi.PsiDocumentManager.getInstance(project).getPsiFile(document)
            ?: return emptyList()

        val highlights = mutableListOf<com.intellij.codeInsight.daemon.impl.HighlightInfo>()
        com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerEx.processHighlights(
            document, project, com.intellij.lang.annotation.HighlightSeverity.INFORMATION,
            0, document.textLength,
            com.intellij.util.Processor { info ->
                highlights.add(info)
                true
            }
        )
        return highlights
    }
}

class GetSelectionTool : McpTool() {
    override val name = "get_selection"
    override val description = "Get the currently selected text and cursor position from the active editor"
    override val inputSchema = """
    {
        "type": "object",
        "properties": {}
    }
    """.trimIndent()

    override fun execute(arguments: JsonObject?): Any {
        val project = com.intellij.openapi.project.ProjectManager.getInstance().openProjects.firstOrNull()
            ?: return "No open project"

        val editor = com.intellij.openapi.fileEditor.FileEditorManager.getInstance(project).selectedTextEditor
            ?: return "No active editor"

        val selectionModel = editor.selectionModel
        val document = editor.document
        val virtualFile = editor.virtualFile ?: return "No file open"

        val selectedText = selectionModel.selectedText
        val startOffset = selectionModel.selectionStart
        val endOffset = selectionModel.selectionEnd

        val startLine = document.getLineNumber(startOffset) + 1
        val startCol = startOffset - document.getLineStartOffset(startLine - 1) + 1
        val endLine = document.getLineNumber(endOffset) + 1
        val endCol = endOffset - document.getLineStartOffset(endLine - 1) + 1

        return buildString {
            appendLine("File: ${virtualFile.path}")
            if (selectedText != null) {
                appendLine("Selection: L$startLine:$startCol - L$endLine:$endCol")
                appendLine("---")
                appendLine(selectedText)
            } else {
                appendLine("Cursor: L$startLine:$startCol (no selection)")
            }
        }.trimEnd()
    }
}

class ReadFileTool : McpTool() {
    override val name = "read_file"
    override val description = "Read the contents of a file by absolute path"
    override val inputSchema = """
    {
        "type": "object",
        "properties": {
            "file_path": {
                "type": "string",
                "description": "Absolute path to the file to read"
            }
        },
        "required": ["file_path"]
    }
    """.trimIndent()

    override fun execute(arguments: JsonObject?): Any {
        val filePath = arguments?.get("file_path")?.asString ?: return "Missing file_path argument"
        val virtualFile = com.intellij.openapi.vfs.LocalFileSystem.getInstance().findFileByPath(filePath)
            ?: return "File not found: $filePath"

        if (virtualFile.isDirectory) return "Path is a directory: $filePath"

        return try {
            com.intellij.openapi.application.ReadAction.compute<String, Throwable> {
                String(virtualFile.contentsToByteArray(), virtualFile.charset)
            }
        } catch (e: Exception) {
            "Failed to read file: $filePath (${e.message})"
        }
    }
}
