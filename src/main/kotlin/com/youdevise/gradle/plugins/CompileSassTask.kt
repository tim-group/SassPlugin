package com.youdevise.gradle.plugins

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.script.ScriptEngineManager

open class CompileSassTask : DefaultTask() {
    @InputDirectory
    val inputDir = project.objects.directoryProperty().convention(project.layout.projectDirectory.dir("src").dir("main").dir(
            "sass"))

    @OutputDirectory
    val outputDir = project.objects.directoryProperty().convention(project.layout.buildDirectory.dir("sass"))

    @Internal
    val cacheLocation = project.objects.directoryProperty().convention(project.layout.buildDirectory.dir("sass-cache"))

    init {
        group = "build"
        description = "Compiles SASS or SCSS files to CSS"
    }

    @TaskAction
    fun compileSass() {
        val inputDirFile = inputDir.asFile.get()
        val outputDirFile = outputDir.asFile.get()
        val cacheLocationFile = cacheLocation.asFile.get()

        val mappings = mutableMapOf<String, String>()
        inputDirFile.walk()
                .onEnter {
                    !(it.isHidden)
                }
                .forEach { inputFile ->
                    if (inputFile.isFile && (inputFile.name.endsWith(".sass") || inputFile.name.endsWith(".scss"))) {
                        val outputFile = File(outputDirFile, relativePath(inputDirFile, inputFile) + ".css")
                        outputFile.parentFile.mkdirs()
                        mappings[inputFile.path] = outputFile.path
                    }
                }

        ScriptEngineManager().getEngineByName("jruby")!!.run {
            put("cacheLocation", cacheLocationFile.path)
            put("mappings", mappings)
            eval(driverScript)
        }
    }
}

private val driverScript: String by lazy {
    CompileSassTask::class.java.getResourceAsStream("driver_script.rb")!!.use { stream ->
        stream.reader().readText()
    }
}

private fun relativePath(root: File, descendant: File): String {
    val rootPath = root.absolutePath
    val descendantPath = descendant.absolutePath
    require(descendantPath.startsWith(rootPath))
    return descendantPath.substring(rootPath.length + 1)
}
