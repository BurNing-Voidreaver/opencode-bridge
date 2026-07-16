package com.opencode.bridge.mcp.tools

import com.google.gson.JsonObject
import com.intellij.testFramework.UsefulTestCase
import com.intellij.openapi.fileTypes.FileTypes

/**
 * Tests for [GetSelectionTool].
 *
 * Uses the fixture so we get a real editor with a document that we can configure
 * with text and a selection range.
 */
class GetSelectionToolTest : CodeInsightFixtureTestBase() {

    private val tool = GetSelectionTool()

    override fun setUp() {
        super.setUp()
        tool.project = fixture.project
        fixture.configureByText(FileTypes.PLAIN_TEXT, "alpha\nbeta\ngamma\n")
    }

    fun testNoSelectionReportsCursorPosition() {
        // Place caret at the start without selecting anything.
        fixture.editor.selectionModel.setSelection(0, 0)

        val result = tool.execute(JsonObject()) as String

        assertContains(result, "Cursor:")
        assertContains(result, "File:")
        // No selection means the "---" separator and selected-text block are absent.
        UsefulTestCase.assertFalse("Should not contain selection separator", result.contains("---"))
    }

    fun testSelectionReturnsSelectedText() {
        // Select "beta" (offsets 6..10 in "alpha\nbeta\ngamma\n").
        fixture.editor.selectionModel.setSelection(6, 10)

        val result = tool.execute(JsonObject()) as String

        assertContains(result, "Selection:")
        assertContains(result, "---")
        assertContains(result, "beta")
    }

    fun testSelectionReportsStartAndEndLines() {
        // Select from "a" in alpha to "g" in gamma.
        fixture.editor.selectionModel.setSelection(0, 15)

        val result = tool.execute(JsonObject()) as String

        // Spans line 1 to line 3.
        assertContains(result, "L1:")
        assertContains(result, "L3:")
    }

    private fun assertContains(haystack: String, needle: String) {
        UsefulTestCase.assertTrue(
            "Expected output to contain \"$needle\" but was: $haystack",
            haystack.contains(needle)
        )
    }
}
