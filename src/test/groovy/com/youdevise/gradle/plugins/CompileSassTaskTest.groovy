package com.youdevise.gradle.plugins

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class CompileSassTaskTest extends Specification {
    @Rule public final TemporaryFolder testProjectDir = new TemporaryFolder()

    File buildFile

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
    }

    def "task compiles sass files"() {
        given:
        buildFile << """
plugins {
  id 'com.youdevise.sass'
}
"""

        testProjectDir.newFolder("src", "main", "sass")
        testProjectDir.newFile("src/main/sass/hello.sass") << this.class.getResourceAsStream("hello.sass")

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("compileSass")
            .withPluginClasspath()
            .build()

        then:
        result.task(":compileSass").outcome == TaskOutcome.SUCCESS
        this.class.getResourceAsStream("hello.css").text == new File(testProjectDir.getRoot(), "build/sass/hello.sass.css").text
    }

    def "task compiles scss files"() {
        given:
        buildFile << """
plugins {
  id 'com.youdevise.sass'
}
"""

        testProjectDir.newFolder("src", "main", "sass")
        testProjectDir.newFile("src/main/sass/hello.scss") << this.class.getResourceAsStream("hello.scss")

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("compileSass")
            .withPluginClasspath()
            .build()

        then:
        result.task(":compileSass").outcome == TaskOutcome.SUCCESS
        this.class.getResourceAsStream("hello.css").text == new File(testProjectDir.getRoot(), "build/sass/hello.scss.css").text
    }

    def "task handles imports"() {
        given:
        buildFile << """
plugins {
  id 'com.youdevise.sass'
}
"""

        testProjectDir.newFolder("src", "main", "sass")
        testProjectDir.newFile("src/main/sass/hello.sass") << this.class.getResourceAsStream("importer.sass")
        testProjectDir.newFile("src/main/sass/imported.sass") << this.class.getResourceAsStream("hello.sass")

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("compileSass")
            .withPluginClasspath()
            .build()

        then:
        result.task(":compileSass").outcome == TaskOutcome.SUCCESS
        this.class.getResourceAsStream("hello.css").text == new File(testProjectDir.getRoot(), "build/sass/hello.sass.css").text
    }

    def "task ignores files which are not sass files"() {
        given:
        buildFile << """
plugins {
  id 'com.youdevise.sass'
}
"""

        testProjectDir.newFolder("src", "main", "sass")
        testProjectDir.newFile("src/main/sass/hello.txt").text = "Ceci n'est pas de Sass"

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("compileSass")
                .withPluginClasspath()
                .build()

        then:
        result.task(":compileSass").outcome == TaskOutcome.SUCCESS
        new File(testProjectDir.getRoot(), "build/sass").list() == null
    }

    def "task recurses into subdirectories"() {
        given:
        buildFile << """
plugins {
  id 'com.youdevise.sass'
}
"""

        testProjectDir.newFolder("src", "main", "sass", "sub")
        testProjectDir.newFile("src/main/sass/sub/hello.sass") << this.class.getResourceAsStream("hello.sass")

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("compileSass")
                .withPluginClasspath()
                .build()

        then:
        result.task(":compileSass").outcome == TaskOutcome.SUCCESS
        this.class.getResourceAsStream("hello.css").text == new File(testProjectDir.getRoot(), "build/sass/sub/hello.sass.css").text
    }

    def "task does not recurse into hidden directories"() {
        given:
        buildFile << """
plugins {
  id 'com.youdevise.sass'
}
"""

        testProjectDir.newFolder("src", "main", "sass", ".svn")
        testProjectDir.newFile("src/main/sass/.svn/hello.sass") << this.class.getResourceAsStream("hello.sass")

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("compileSass")
                .withPluginClasspath()
                .build()

        then:
        result.task(":compileSass").outcome == TaskOutcome.SUCCESS
        new File(testProjectDir.getRoot(), "build/sass").list() == null
    }
}
