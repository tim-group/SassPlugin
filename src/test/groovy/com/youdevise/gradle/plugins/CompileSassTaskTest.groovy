package com.youdevise.gradle.plugins

import org.junit.Assert
import org.junit.Test
import org.junit.Before
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

class CompileSassTaskTest {

    def task

    @Before
    void task() {
        Project project = ProjectBuilder.builder().build()
        project.apply plugin: 'sass'

        task = project.tasks.compileSass
    }

    @Test
    void pluginCreatesTheTask() throws Exception {
        Assert.assertTrue(task instanceof CompileSassTask)
    }

    @Test
    void taskDoesTheRightThing() throws Exception {
        task.inputDir.mkdirs()
        new File(task.inputDir, "hello.sass") << getClass().getResourceAsStream("hello.sass")
        task.outputDir.mkdirs()

        task.compileSass()

        Assert.assertEquals(getClass().getResourceAsStream("hello.css").text, new File(task.outputDir, "hello.sass.css").text)
    }

}
