package com.opencode.bridge.mcp.tools

import com.google.gson.JsonObject
import com.intellij.testFramework.UsefulTestCase
import java.io.File
import java.nio.file.Files

/**
 * Tests for [ReadFileTool].
 *
 * These exercise the tool against real files on disk via the IntelliJ VirtualFileSystem,
 * using the fixture only to provide a running application environment.
 */
class ReadFileToolTest : CodeInsightFixtureTestBase() {

    private val tool = ReadFileTool()

    fun testReadExistingFileReturnsContents() {
        val dir = Files.createTempDirectory("read-file-test").toFile()
        val file = File(dir, "sample.txt")
        file.writeText("hello world\nsecond line")

        val args = JsonObject().apply { addProperty("file_path", file.absolutePath) }

        val result = tool.execute(args) as String

        assertContains(result, "hello world")
        assertContains(result, "second line")

        file.delete()
        dir.delete()
    }

    fun testReadFilePreservesUtf8Contents() {
        val dir = Files.createTempDirectory("read-file-test").toFile()
        val file = File(dir, "utf8.txt")
        file.writeText("中文内容 ✨ café")

        val args = JsonObject().apply { addProperty("file_path", file.absolutePath) }

        val result = tool.execute(args) as String

        assertContains(result, "中文内容 ✨ café")

        file.delete()
        dir.delete()
    }

    fun testMissingFilePathArgumentReturnsError() {
        val result = tool.execute(JsonObject()) as String
        assertContains(result, "Missing file_path")
    }

    fun testNonExistentFileReturnsError() {
        val args = JsonObject().apply {
            addProperty("file_path", "/non/existent/path/does-not-exist-12345.txt")
        }

        val result = tool.execute(args) as String
        assertContains(result, "File not found")
    }

    fun testDirectoryPathReturnsError() {
        val dir = Files.createTempDirectory("read-file-dir-test").toFile()
        val args = JsonObject().apply { addProperty("file_path", dir.absolutePath) }

        val result = tool.execute(args) as String
        assertContains(result, "Path is a directory")

        dir.delete()
    }

    private fun assertContains(haystack: String, needle: String) {
        UsefulTestCase.assertTrue(
            "Expected output to contain \"$needle\" but was: $haystack",
            haystack.contains(needle)
        )
    }
}
