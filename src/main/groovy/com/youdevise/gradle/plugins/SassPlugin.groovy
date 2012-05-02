package com.youdevise.gradle.plugins

import java.io.File
import java.util.TreeMap

import javax.script.ScriptEngineManager;

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction

class SassPlugin implements Plugin<Project> {
    def void apply(Project project) {
        def compileSass = project.tasks.add("compileSass", CompileSassTask)
        compileSass.inputDir = project.file('src/main/sass')
        compileSass.outputDir = new File(project.buildDir, 'sass')
        compileSass.cacheLocation = new File(project.buildDir, 'sass-cache')
        // should add our output to the war task's input ... somewhere; somewhere it will be right if it's changed by the script
    }
}

class CompileSassTask extends DefaultTask {

    @InputDirectory
    public File inputDir

    @OutputDirectory
    public File outputDir

    public File cacheLocation

    String driverScript = '''
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

        inputDir.eachFileRecurse { inputFile ->
            def outputFile = new File(outputDir, relativePath(inputDir, inputFile) + '.css')
            outputFile.parentFile.mkdirs()
            mappings.put(inputFile.path, outputFile.path)
        }

        def engine = new ScriptEngineManager().getEngineByName("jruby")
        engine.put("cacheLocation", cacheLocation.path)
        engine.put("mappings", mappings)
        engine.eval(driverScript)
    }

    def relativePath(File root, File descendant) {
        def rootPath = root.absolutePath
        def descendantPath = descendant.absolutePath
        assert descendantPath.startsWith(rootPath)
        return descendantPath.substring(rootPath.length() + 1)
    }

}
