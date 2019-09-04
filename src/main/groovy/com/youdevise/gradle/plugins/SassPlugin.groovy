package com.youdevise.gradle.plugins

import groovy.io.FileVisitResult
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

import javax.script.ScriptEngineManager

class SassPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.tasks.register("compileSass", CompileSassTask)
    }
}

class CompileSassTask extends DefaultTask {

    @InputDirectory
    DirectoryProperty inputDir = project.objects.directoryProperty().convention(project.layout.projectDirectory.dir("src").dir("main").dir("sass"))

    @OutputDirectory
    DirectoryProperty outputDir = project.objects.directoryProperty().convention(project.layout.buildDirectory.dir("sass"))

    @Internal
    DirectoryProperty cacheLocation = project.objects.directoryProperty().convention(project.layout.buildDirectory.dir("sass-cache"))

    private String driverScript = '''
        require 'rubygems'
        require 'sass'

        $mappings.each do |src, dst|
            engine = Sass::Engine.for_file(src, {:cache_location => $cacheLocation})
            css = engine.render
            File.open(dst, 'w') {|f| f.write(css) }
            puts "#{src} -> #{dst}"
        end
    '''

    @TaskAction
    def compileSass() {
        def mappings = new TreeMap()

        def inputDirFile = inputDir.getAsFile().get()
        def outputDirFile = outputDir.getAsFile().get()
        def cacheLocationFile = cacheLocation.getAsFile().get()

        inputDirFile.traverse(preDir: { if (it.isHidden()) return FileVisitResult.SKIP_SUBTREE }) { inputFile ->
            if (inputFile.isFile() && (inputFile.name.endsWith('.sass') || inputFile.name.endsWith('.scss'))) {
                def outputFile = new File(outputDirFile, relativePath(inputDirFile, inputFile) + '.css')
                outputFile.parentFile.mkdirs()
                mappings.put(inputFile.path, outputFile.path)
            }
        }

        def engine = new ScriptEngineManager().getEngineByName("jruby")
        engine.put("cacheLocation", cacheLocationFile.path)
        engine.put("mappings", mappings)
        engine.eval(driverScript)
    }

    static def relativePath(File root, File descendant) {
        def rootPath = root.absolutePath
        def descendantPath = descendant.absolutePath
        assert descendantPath.startsWith(rootPath)
        return descendantPath.substring(rootPath.length() + 1)
    }
}
