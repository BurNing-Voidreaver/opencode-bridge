package com.opencode.bridge.mcp.tools

import com.google.gson.JsonObject
import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.codeInsight.daemon.impl.HighlightInfoType
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.fileTypes.FileTypes
import com.intellij.openapi.util.TextRange
import com.intellij.testFramework.UsefulTestCase

/**
 * Tests for [GetDiagnosticsTool].
 *
 * The real daemon is asynchronous and hard to drive from a unit test, so we subclass
 * the tool and override [GetDiagnosticsTool.collectHighlights] to return canned
 * [HighlightInfo] values. This lets us verify the tool's formatting and filter logic
 * deterministically.
 */
class GetDiagnosticsToolTest : CodeInsightFixtureTestBase() {

    fun testNoFileFoundWhenEditorIsEmpty() {
        val tool = GetDiagnosticsTool()
        // Do not configure any file; the active editor has no document/file.
        val result = tool.execute(JsonObject()) as String
        UsefulTestCase.assertEquals("No file found", result)
    }

    fun testNoDiagnosticsWhenCollectorReturnsEmpty() {
        val tool = TestableGetDiagnosticsTool(emptyList())
        fixture.configureByText(FileTypes.PLAIN_TEXT, "just a plain line\n")

        val result = tool.execute(JsonObject()) as String
        UsefulTestCase.assertEquals("No diagnostics found", result)
    }

    fun testOnlyInformationSeverityIsFilteredOut() {
        // INFORMATION-level diagnostics must not appear in the output.
        val infoOnly = listOf(
            fakeHighlight(HighlightSeverity.INFORMATION, "info thing", 0, 4)
        )
        val tool = TestableGetDiagnosticsTool(infoOnly)
        fixture.configureByText(FileTypes.PLAIN_TEXT, "just a plain line\n")

        val result = tool.execute(JsonObject()) as String
        UsefulTestCase.assertEquals("No diagnostics found", result)
    }

    fun testErrorAndWarningAreFormatted() {
        val highlights = listOf(
            fakeHighlight(HighlightSeverity.ERROR, "unexpected token", 0, 4),
            fakeHighlight(HighlightSeverity.WARNING, "unused value", 10, 20)
        )
        val tool = TestableGetDiagnosticsTool(highlights)
        fixture.configureByText(FileTypes.PLAIN_TEXT, "just a plain line with more text\n")

        val result = tool.execute(JsonObject()) as String

        UsefulTestCase.assertFalse(
            "Should not report 'No diagnostics found' when diagnostics exist: $result",
            result == "No diagnostics found"
        )
        UsefulTestCase.assertTrue("Expected [ERROR] tag: $result", result.contains("[ERROR]"))
        UsefulTestCase.assertTrue("Expected [WARNING] tag: $result", result.contains("[WARNING]"))
        UsefulTestCase.assertTrue("Expected 'Line' reference: $result", result.contains("Line"))
        UsefulTestCase.assertTrue("Expected description: $result", result.contains("unexpected token"))
        UsefulTestCase.assertTrue("Expected description: $result", result.contains("unused value"))
    }

    fun testLineAndColumnAreComputedFromOffset() {
        // Text: line1\nline2 is 11 chars; an ERROR at offset 8 ("ne2") is on line 2.
        val highlights = listOf(
            fakeHighlight(HighlightSeverity.ERROR, "boom", 8, 11)
        )
        val tool = TestableGetDiagnosticsTool(highlights)
        fixture.configureByText(FileTypes.PLAIN_TEXT, "line1\nline2\n")

        val result = tool.execute(JsonObject()) as String

        // offset 8 = line 2 (0-indexed line 1) + col 3.
        UsefulTestCase.assertTrue("Expected Line 2 Col 3: $result", result.contains("Line 2, Col 3"))
    }

    /** A [GetDiagnosticsTool] subclass that returns fixed highlights. */
    private class TestableGetDiagnosticsTool(
        private val canned: List<HighlightInfo>
    ) : GetDiagnosticsTool() {
        override fun collectHighlights(
            document: com.intellij.openapi.editor.Document,
            project: com.intellij.openapi.project.Project
        ): List<HighlightInfo> = canned
    }

    private fun fakeHighlight(
        severity: HighlightSeverity,
        description: String,
        start: Int,
        end: Int
    ): HighlightInfo {
        return HighlightInfo.newHighlightInfo(HighlightInfoType.ERROR)
            .severity(severity)
            .description(description)
            .range(TextRange(start, end))
            .create()!!
    }
}
