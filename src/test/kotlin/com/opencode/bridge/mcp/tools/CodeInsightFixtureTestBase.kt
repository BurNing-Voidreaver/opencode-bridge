package com.opencode.bridge.mcp.tools

import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.testFramework.fixtures.impl.TempDirTestFixtureImpl

/**
 * Lightweight base class that creates a [CodeInsightTestFixture] manually via
 * [IdeaTestFixtureFactory], bypassing [com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase]
 * (which cannot instantiate a builder from an interface class).
 *
 * Subclasses access the fixture via [fixture] and use [UsefulTestCase] assertions.
 */
abstract class CodeInsightFixtureTestBase : UsefulTestCase() {

    protected lateinit var fixture: CodeInsightTestFixture
        private set

    private lateinit var fixtureDisposable: com.intellij.openapi.Disposable

    override fun setUp() {
        super.setUp()
        val factory = IdeaTestFixtureFactory.getFixtureFactory()
        // useDefaultProject = true gives us a real project without needing a module builder.
        val builder = factory.createFixtureBuilder("test", true)
        val projectFixture = builder.fixture
        val tempDir = TempDirTestFixtureImpl()
        fixture = factory.createCodeInsightFixture(projectFixture, tempDir)
        fixture.setUp()
        fixtureDisposable = projectFixture.testRootDisposable
    }

    override fun tearDown() {
        try {
            if (this::fixture.isInitialized) {
                fixture.tearDown()
            }
        } catch (e: Exception) {
            // Avoid masking the original test failure with a tearDown error.
            addSuppressedException(e)
        } finally {
            super.tearDown()
        }
    }
}
