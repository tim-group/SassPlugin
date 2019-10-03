package com.youdevise.gradle.plugins

import com.natpryce.hamkrest.MatchResult
import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class CompileSassTaskTest {
    @get:Rule
    val testProjectDir = TemporaryFolder()

    lateinit var buildFile: File

    @Before
    fun setup() {
        buildFile = testProjectDir.newFile("build.gradle")
    }

    @Test
    fun `task compiles sass files`() {
        buildFile.writeText("""
            plugins {
                id 'com.youdevise.sass'
            }
        """.trimIndent())

        testProjectDir.newFolder("src", "main", "sass")
        testProjectDir.newFile("src/main/sass/hello.sass").copyFromResource("hello.sass")

        val result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("compileSass")
                .withPluginClasspath()
                .build()

        assertThat(result.task(":compileSass")?.outcome, equalTo(TaskOutcome.SUCCESS))
        assertThat(File(testProjectDir.root, "build/sass/hello.sass.css").readText(),
                equalTo(javaClass.getResourceAsStream("hello.css").reader().readText()))
    }

    @Test
    fun `task compiles scss files`() {
        buildFile.writeText("""
            plugins {
                id 'com.youdevise.sass'
            }
        """.trimIndent())

        testProjectDir.newFolder("src", "main", "sass")
        testProjectDir.newFile("src/main/sass/hello.scss").copyFromResource("hello.scss")

        val result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("compileSass")
                .withPluginClasspath()
                .build()

        assertThat(result.task(":compileSass")?.outcome, equalTo(TaskOutcome.SUCCESS))
        assertThat(File(testProjectDir.root, "build/sass/hello.scss.css").readText(),
                equalTo(javaClass.getResourceAsStream("hello.css").reader().readText()))
    }

    @Test
    fun `task handles imports`() {
        buildFile.writeText("""
            plugins {
                id 'com.youdevise.sass'
            }
        """.trimIndent())

        testProjectDir.newFolder("src", "main", "sass")
        testProjectDir.newFile("src/main/sass/hello.sass").copyFromResource("importer.sass")
        testProjectDir.newFile("src/main/sass/imported.sass").copyFromResource("hello.sass")

        val result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("compileSass")
                .withPluginClasspath()
                .build()

        assertThat(result.task(":compileSass")?.outcome, equalTo(TaskOutcome.SUCCESS))
        assertThat(File(testProjectDir.root, "build/sass/hello.sass.css").readText(),
                equalTo(javaClass.getResourceAsStream("hello.css").reader().readText()))
    }

    @Test
    fun `task ignores files which are not sass files`() {
        buildFile.writeText("""
            plugins {
                id 'com.youdevise.sass'
            }
        """.trimIndent())

        testProjectDir.newFolder("src", "main", "sass")
        testProjectDir.newFile("src/main/sass/hello.txt").writeText("Ceci n'est pas de Sass")

        val result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("compileSass")
                .withPluginClasspath()
                .build()

        assertThat(result.task(":compileSass")?.outcome, equalTo(TaskOutcome.SUCCESS))
        assertThat(File(testProjectDir.root, "build/sass"), isEmptyDirectory)
    }

    @Test
    fun `task recurses into subdirectories`() {
        buildFile.writeText("""
            plugins {
                id 'com.youdevise.sass'
            }
        """.trimIndent())

        testProjectDir.newFolder("src", "main", "sass", "sub")
        testProjectDir.newFile("src/main/sass/sub/hello.sass").copyFromResource("hello.sass")

        val result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("compileSass")
                .withPluginClasspath()
                .build()

        assertThat(result.task(":compileSass")?.outcome, equalTo(TaskOutcome.SUCCESS))
        assertThat(File(testProjectDir.root, "build/sass/sub/hello.sass.css").readText(),
                equalTo(javaClass.getResourceAsStream("hello.css").reader().readText()))
    }

    @Test
    fun `task does not recurse into hidden directories`() {
        buildFile.writeText("""
            plugins {
                id 'com.youdevise.sass'
            }
        """.trimIndent())

        testProjectDir.newFolder("src", "main", "sass", ".svn")
        testProjectDir.newFile("src/main/sass/.svn/hello.sass").copyFromResource("hello.sass")

        val result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("compileSass")
                .withPluginClasspath()
                .build()

        assertThat(result.task(":compileSass")?.outcome, equalTo(TaskOutcome.SUCCESS))
        assertThat(File(testProjectDir.root, "build/sass"), isEmptyDirectory)
    }

    private fun File.copyFromResource(resourceName: String) {
        val resource = CompileSassTaskTest::class.java.getResource(resourceName)
        require(resource != null) { "Resource $resourceName not found" }
        writeBytes(resource.readBytes())
    }

    private val isEmptyDirectory = object : Matcher.Primitive<File>() {
        override fun invoke(actual: File): MatchResult {
            if (!actual.isDirectory)
                return MatchResult.Mismatch("not a directory")
            val entries = actual.list() ?: emptyArray()
            if (entries.isEmpty())
                return MatchResult.Match
            return MatchResult.Mismatch("$actual contains: ${entries.sorted().joinToString(", ")}")
        }

        override val description: String = "is an empty directory"
    }
}
