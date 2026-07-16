package com.youdevise.gradle.plugins

import com.natpryce.hamkrest.MatchResult
import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.readText
import kotlin.io.path.writeBytes
import kotlin.io.path.writeText
import kotlin.streams.toList

@OptIn(ExperimentalPathApi::class)
class CompileSassTaskTest {
    @TempDir
    lateinit var testProjectDir: Path

    @BeforeEach
    fun setup() {
        val buildFile = testProjectDir.resolve("build.gradle")
        buildFile.writeText("""
            plugins {
                id 'com.youdevise.sass'
            }
        """.trimIndent())
    }

    @Test
    fun `task compiles sass files`() {
        testProjectDir.mkdirs("src/main/sass")
        testProjectDir.resolve("src/main/sass/hello.sass").copyFromResource("hello.sass")

        val result = GradleRunner.create()
                .withProjectDir(testProjectDir.toFile())
                .withArguments("compileSass")
                .withPluginClasspath()
                .build()

        assertThat(result.task(":compileSass")?.outcome, equalTo(TaskOutcome.SUCCESS))
        assertThat(testProjectDir.resolve("build/sass/hello.sass.css").readText(),
                equalTo(javaClass.getResourceAsStream("hello.css")!!.reader().readText()))
    }

    @Test
    fun `task compiles scss files`() {
        testProjectDir.mkdirs("src", "main", "sass")
        testProjectDir.resolve("src/main/sass/hello.scss").copyFromResource("hello.scss")

        val result = GradleRunner.create()
                .withProjectDir(testProjectDir.toFile())
                .withArguments("compileSass")
                .withPluginClasspath()
                .build()

        assertThat(result.task(":compileSass")?.outcome, equalTo(TaskOutcome.SUCCESS))
        assertThat(testProjectDir.resolve("build/sass/hello.scss.css").readText(),
                equalTo(javaClass.getResourceAsStream("hello.css")!!.reader().readText()))
    }

    @Test
    fun `task handles imports`() {
        testProjectDir.mkdirs("src", "main", "sass")
        testProjectDir.resolve("src/main/sass/hello.sass").copyFromResource("importer.sass")
        testProjectDir.resolve("src/main/sass/imported.sass").copyFromResource("hello.sass")

        val result = GradleRunner.create()
                .withProjectDir(testProjectDir.toFile())
                .withArguments("compileSass")
                .withPluginClasspath()
                .build()

        assertThat(result.task(":compileSass")?.outcome, equalTo(TaskOutcome.SUCCESS))
        assertThat(testProjectDir.resolve("build/sass/hello.sass.css").readText(),
                equalTo(javaClass.getResourceAsStream("hello.css")!!.reader().readText()))
    }

    @Test
    fun `task ignores files which are not sass files`() {
        testProjectDir.mkdirs("src", "main", "sass")
        testProjectDir.resolve("src/main/sass/hello.txt").writeText("Ceci n'est pas de Sass")

        val result = GradleRunner.create()
                .withProjectDir(testProjectDir.toFile())
                .withArguments("compileSass")
                .withPluginClasspath()
                .build()

        assertThat(result.task(":compileSass")?.outcome, equalTo(TaskOutcome.SUCCESS))
        assertThat(testProjectDir.resolve("build/sass"), isEmptyDirectory)
    }

    @Test
    fun `task recurses into subdirectories`() {
        testProjectDir.mkdirs("src", "main", "sass", "sub")
        testProjectDir.resolve("src/main/sass/sub/hello.sass").copyFromResource("hello.sass")

        val result = GradleRunner.create()
                .withProjectDir(testProjectDir.toFile())
                .withArguments("compileSass")
                .withPluginClasspath()
                .build()

        assertThat(result.task(":compileSass")?.outcome, equalTo(TaskOutcome.SUCCESS))
        assertThat(testProjectDir.resolve("build/sass/sub/hello.sass.css").readText(),
                equalTo(javaClass.getResourceAsStream("hello.css")!!.reader().readText()))
    }

    @Test
    fun `task does not recurse into hidden directories`() {
        testProjectDir.mkdirs("src", "main", "sass", ".svn")
        testProjectDir.resolve("src/main/sass/.svn/hello.sass").copyFromResource("hello.sass")

        val result = GradleRunner.create()
                .withProjectDir(testProjectDir.toFile())
                .withArguments("compileSass")
                .withPluginClasspath()
                .build()

        assertThat(result.task(":compileSass")?.outcome, equalTo(TaskOutcome.SUCCESS))
        assertThat(testProjectDir.resolve("build/sass"), isEmptyDirectory)
    }

    private fun Path.mkdirs(vararg elements: String) {
        var target = this
        for (path in elements) {
            target = target.resolve(path)
        }
        Files.createDirectories(target)
    }

    private fun Path.copyFromResource(resourceName: String) {
        val resource = CompileSassTaskTest::class.java.getResource(resourceName)
        require(resource != null) { "Resource $resourceName not found" }
        writeBytes(resource.readBytes())
    }

    private val isEmptyDirectory = object : Matcher.Primitive<Path>() {
        override fun invoke(actual: Path): MatchResult {
            if (!Files.isDirectory(actual))
                return MatchResult.Mismatch("not a directory")
            val entries = Files.list(actual).use { f -> f.map { it.fileName.toString() }.toList() }
            if (entries.isEmpty())
                return MatchResult.Match
            return MatchResult.Mismatch("$actual contains: ${entries.sorted().joinToString(", ")}")
        }

        override val description: String = "is an empty directory"
    }
}
