package com.youdevise.gradle.plugins

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class CompileSassTaskTest {

    def task

    @Before
    void task() {
        Project project = ProjectBuilder.builder().build()
        project.apply plugin: 'com.youdevise.sass'

        task = project.tasks.compileSass
    }

    @Test
    void pluginCreatesTheTask() throws Exception {
        Assert.assertTrue(task instanceof CompileSassTask)
    }

    @Test
    void taskCompilesSassFiles() throws Exception {
        task.inputDir.mkdirs()
        new File(task.inputDir, "hello.sass") << getClass().getResourceAsStream("hello.sass")
        task.outputDir.mkdirs()

        task.compileSass()

        Assert.assertEquals(getClass().getResourceAsStream("hello.css").text, new File(task.outputDir, "hello.sass.css").text)
    }

    @Test
    void taskCompilesScssFiles() throws Exception {
        task.inputDir.mkdirs()
        new File(task.inputDir, "hello.scss") << getClass().getResourceAsStream("hello.scss")
        task.outputDir.mkdirs()

        task.compileSass()

        Assert.assertEquals(getClass().getResourceAsStream("hello.css").text, new File(task.outputDir, "hello.scss.css").text)
    }

    @Test
    void taskHandlesImports() throws Exception {
        task.inputDir.mkdirs()
        new File(task.inputDir, "hello.sass") << getClass().getResourceAsStream("importer.sass")
        new File(task.inputDir, "imported.sass") << getClass().getResourceAsStream("hello.sass")
        task.outputDir.mkdirs()

        task.compileSass()

        Assert.assertEquals(getClass().getResourceAsStream("hello.css").text, new File(task.outputDir, "hello.sass.css").text)
    }

    @Test
    void taskIgnoresFilesWhichAreNotSassFiles() throws Exception {
        task.inputDir.mkdirs()
        new File(task.inputDir, "hello.txt").write("Ceci n'est pas de Sass")
        task.outputDir.mkdirs()

        task.compileSass()

        Assert.assertArrayEquals(new File[0], task.outputDir.listFiles())
    }

    @Test
    void taskRecursesIntoSubdirectories() throws Exception {
        def subDir = new File(task.inputDir, 'sub')
        subDir.mkdirs()
        new File(subDir, "hello.sass") << getClass().getResourceAsStream("hello.sass")
        task.outputDir.mkdirs()

        task.compileSass()

        Assert.assertEquals(getClass().getResourceAsStream("hello.css").text, new File(task.outputDir, "sub/hello.sass.css").text)
    }

    @Test
    void taskDoesNotRecurseIntoHiddenDirectories() throws Exception {
        def dotDir = new File(task.inputDir, '.svn')
        dotDir.mkdirs()
        new File(dotDir, "hello.sass") << getClass().getResourceAsStream("hello.sass")
        task.outputDir.mkdirs()

        task.compileSass()

        Assert.assertArrayEquals(new File[0], task.outputDir.listFiles())
    }

}
